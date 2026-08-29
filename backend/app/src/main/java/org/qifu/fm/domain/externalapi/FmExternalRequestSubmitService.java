package org.qifu.fm.domain.externalapi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmSystemFormFields;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.external.FmExternalRequestSubmitRequest;
import org.qifu.fm.dto.external.FmExternalRequestSubmitView;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.entity.FmApiRequest;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
import org.qifu.fm.service.IFmApiRequestService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

@Service
public class FmExternalRequestSubmitService {
	private static final Duration MAX_REQUEST_SKEW = Duration.ofMinutes(5);
	private final IFmApiRequestService apiRequestService;
	private final IFmProcessRuntimeLogicService runtimeLogicService;
	private final IFmProcessVersionService processVersionService;
	private final IFmFormVersionService formVersionService;
	private final IFmEmployeeService employeeService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final ObjectMapper canonicalMapper;

	public FmExternalRequestSubmitService(IFmApiRequestService apiRequestService,
			IFmProcessRuntimeLogicService runtimeLogicService,
			IFmProcessVersionService processVersionService,
			IFmFormVersionService formVersionService,
			IFmEmployeeService employeeService,
			IFmEmployeeOrgAssignmentService assignmentService,
			ObjectMapper objectMapper) {
		this.apiRequestService = apiRequestService;
		this.runtimeLogicService = runtimeLogicService;
		this.processVersionService = processVersionService;
		this.formVersionService = formVersionService;
		this.employeeService = employeeService;
		this.assignmentService = assignmentService;
		this.canonicalMapper = objectMapper.rebuild()
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).build();
	}

	public FmExternalRequestSubmitView submit(OffsetDateTime requestTime,
			FmExternalRequestSubmitRequest request, String idempotencyKey)
			throws ServiceException {
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"runtime.request.submit");
		validateRequestTime(requestTime);
		validate(request, idempotencyKey, principal);
		FmProcessVersion processVersion = publishedProcessVersion(principal.tenantId(),
				request.processDefId(), request.processVersionNo());
		FmFormVersion formVersion = publishedFormVersion(principal.tenantId(),
				request.formId(), request.formVersionNo());
		if (StringUtils.isNotBlank(request.formSchemaHash())
				&& !request.formSchemaHash().equalsIgnoreCase(formVersion.getContentSha256())) {
			throw new FmExternalApiConflictException("FORM_SCHEMA_HASH_MISMATCH",
					"The published form schema has changed.");
		}
		String idempotencyHash = hash(idempotencyKey.trim());
		String payloadHash = payloadHash(request);
		FmApiRequest existing = apiRequestService.findByIdempotency(principal.tenantId(),
				principal.clientId(), idempotencyHash);
		if (existing != null) {
			return replay(existing, payloadHash);
		}
		FmEmployeeOrgAssignment assignment = resolveAssignment(principal.tenantId(),
				request.applicantAccount(), request.applicantOrgUnitId());
		Map<String, Object> submission = new HashMap<>(request.submission());
		submission.put(FmSystemFormFields.APPLICANT_ACCOUNT, request.applicantAccount());
		submission.put(FmSystemFormFields.APPLICANT_ASSIGNMENT_ID,
				assignment.getEmployeeOrgAssignmentId());
		submission.put(FmSystemFormFields.APPLICANT_ORG_ID, assignment.getOrgUnitId());
		FmApiRequest ledger = reservation(principal, request, processVersion, formVersion,
				idempotencyHash, payloadHash);
		try {
			apiRequestService.insert(ledger);
		} catch (DataIntegrityViolationException exception) {
			FmApiRequest raced = apiRequestService.findByIdempotency(principal.tenantId(),
					principal.clientId(), idempotencyHash);
			if (raced != null) {
				return replay(raced, payloadHash);
			}
			throw new FmExternalApiConflictException("EXTERNAL_REFERENCE_CONFLICT",
					"The external reference has already been used.");
		}
		try {
			FmProcessSubmitView result = runtimeLogicService.submitAs(
					new FmProcessSubmitCommand(principal.tenantId(), request.processDefId(),
							request.formId(), formVersion.getVersionNo(),
							"external:" + ledger.getApiRequestId(),
							request.applicantAccount(), submission, null),
					request.initiatorAccount()).getValue();
			complete(ledger, result, processVersion.getVersionNo(),
					formVersion.getVersionNo());
			return view(ledger, false);
		} catch (ServiceException exception) {
			fail(ledger, "RUNTIME_SUBMIT_FAILED");
			throw exception;
		}
	}

	private void validate(FmExternalRequestSubmitRequest request, String idempotencyKey,
			FmExternalApiPrincipal principal) throws ServiceException {
		if (request == null || StringUtils.isAnyBlank(idempotencyKey,
				request.processDefId(), request.formId(), request.initiatorAccount(),
				request.applicantAccount()) || request.submission() == null) {
			throw new ServiceException("Required submit fields are missing.");
		}
		if (idempotencyKey.trim().length() > 200) {
			throw new ServiceException("Idempotency-Key must not exceed 200 characters.");
		}
		if (!principal.allowsProcess(request.processDefId())) {
			throw new ServiceException("The client is not allowed to start this process.");
		}
		if (!principal.allowsInitiator(request.initiatorAccount())) {
			throw new ServiceException("The initiator is not allowed for this client.");
		}
		validateExternalReference(request.externalReference());
	}

	private void validateRequestTime(OffsetDateTime requestTime) throws ServiceException {
		if (requestTime == null || Duration.between(requestTime, OffsetDateTime.now()).abs()
				.compareTo(MAX_REQUEST_SKEW) > 0) {
			throw new ServiceException("requestTime is outside the allowed five-minute window.");
		}
	}

	private void validateExternalReference(
			FmExternalRequestSubmitRequest.ExternalReference reference)
			throws ServiceException {
		if (reference == null) {
			return;
		}
		if (StringUtils.isAnyBlank(reference.sourceSystem(),
				reference.sourceDocumentType(), reference.sourceDocumentNo())) {
			throw new ServiceException("External reference fields must be provided together.");
		}
	}

	private FmProcessVersion publishedProcessVersion(String tenantId, String processDefId,
			Integer requestedVersion) throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processDefId", processDefId);
		parameters.put("versionStatus", "PUBLISHED");
		if (requestedVersion != null) {
			parameters.put("versionNo", requestedVersion);
		}
		return processVersionService.selectListByParams(parameters, "VERSION_NO", "DESC")
				.getValue().stream().findFirst()
				.orElseThrow(() -> new ServiceException("Published process version not found."));
	}

	private FmFormVersion publishedFormVersion(String tenantId, String formId,
			Integer requestedVersion) throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("formId", formId);
		parameters.put("versionStatus", "PUBLISHED");
		if (requestedVersion != null) {
			parameters.put("versionNo", requestedVersion);
		}
		return formVersionService.selectListByParams(parameters, "VERSION_NO", "DESC")
				.getValue().stream().findFirst()
				.orElseThrow(() -> new ServiceException("Published form version not found."));
	}

	private FmEmployeeOrgAssignment resolveAssignment(String tenantId, String account,
			String orgUnitId) throws ServiceException {
		Map<String, Object> employeeParameters = new HashMap<>();
		employeeParameters.put("tenantId", tenantId);
		employeeParameters.put("account", account);
		employeeParameters.put("status", "ACTIVE");
		FmEmployee employee = employeeService.selectListByParams(employeeParameters)
				.getValue().stream().findFirst()
				.orElseThrow(() -> new ServiceException("Applicant is not active."));
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("employeeId", employee.getEmployeeId());
		parameters.put("status", "ACTIVE");
		if (StringUtils.isBlank(orgUnitId)) {
			parameters.put("isPrimary", "Y");
		} else {
			parameters.put("orgUnitId", orgUnitId.trim());
		}
		Date now = new Date();
		List<FmEmployeeOrgAssignment> values = assignmentService
				.selectListByParams(parameters).getValue().stream()
				.filter(value -> (value.getEffectiveFrom() == null
						|| !value.getEffectiveFrom().after(now))
						&& (value.getEffectiveTo() == null || value.getEffectiveTo().after(now)))
				.toList();
		if (values.size() != 1) {
			throw new FmExternalApiConflictException("PRIMARY_ASSIGNMENT_AMBIGUOUS",
					"Applicant assignment could not be resolved uniquely.");
		}
		return values.getFirst();
	}

	private FmApiRequest reservation(FmExternalApiPrincipal principal,
			FmExternalRequestSubmitRequest request, FmProcessVersion processVersion,
			FmFormVersion formVersion, String idempotencyHash, String payloadHash) {
		Date now = new Date();
		FmApiRequest value = new FmApiRequest();
		value.setOid(UUID.randomUUID().toString());
		value.setTenantId(principal.tenantId());
		value.setApiRequestId(UUID.randomUUID().toString());
		value.setClientId(principal.clientId());
		value.setKeyId(principal.keyId());
		value.setIdempotencyKeyHash(idempotencyHash);
		value.setPayloadHash(payloadHash);
		value.setProcessDefId(request.processDefId());
		value.setProcessVersionNo(processVersion.getVersionNo());
		value.setFormId(request.formId());
		value.setFormVersionNo(formVersion.getVersionNo());
		value.setFormSchemaHash(formVersion.getContentSha256());
		value.setInitiatorAccount(request.initiatorAccount());
		value.setApplicantAccount(request.applicantAccount());
		value.setApplicantOrgUnitId(request.applicantOrgUnitId());
		if (request.externalReference() != null) {
			value.setSourceSystem(request.externalReference().sourceSystem());
			value.setSourceDocumentType(request.externalReference().sourceDocumentType());
			value.setSourceDocumentNo(request.externalReference().sourceDocumentNo());
		}
		value.setRequestStatus("PROCESSING");
		value.setLockVersion(0);
		value.setCuserid(principal.clientCode());
		value.setCdate(now);
		return value;
	}

	private void complete(FmApiRequest ledger, FmProcessSubmitView result,
			Integer processVersionNo, Integer formVersionNo) throws ServiceException {
		ledger.setRequestStatus("SUCCEEDED");
		ledger.setResultCode("SUBMITTED");
		ledger.setProcessInstanceId(result.processInstanceId());
		ledger.setFlowableProcessInstanceId(result.processInstanceId());
		ledger.setBusinessKey(result.businessKey());
		ledger.setDocumentNumber(result.documentNumber());
		ledger.setFormDataId(result.formDataId());
		ledger.setProcessVersionNo(processVersionNo);
		ledger.setFormVersionNo(formVersionNo);
		ledger.setCompletedAt(new Date());
		ledger.setUuserid(ledger.getCuserid());
		ledger.setUdate(ledger.getCompletedAt());
		if (apiRequestService.updateResult(ledger) != 1) {
			throw new ServiceException("Submit result ledger update conflict.");
		}
	}

	private void fail(FmApiRequest ledger, String code) {
		try {
			ledger.setRequestStatus("FAILED");
			ledger.setResultCode(code);
			ledger.setSafeErrorMessage("The request could not be submitted.");
			ledger.setCompletedAt(new Date());
			ledger.setUuserid(ledger.getCuserid());
			ledger.setUdate(ledger.getCompletedAt());
			apiRequestService.updateResult(ledger);
		} catch (Exception ignored) {
			// Preserve the original controlled runtime error.
		}
	}

	private FmExternalRequestSubmitView replay(FmApiRequest existing, String payloadHash) {
		if (!payloadHash.equals(existing.getPayloadHash())) {
			throw new FmExternalApiConflictException("IDEMPOTENCY_PAYLOAD_MISMATCH",
					"The Idempotency-Key was already used with a different payload.");
		}
		if (!"SUCCEEDED".equals(existing.getRequestStatus())) {
			throw new FmExternalApiConflictException("IDEMPOTENCY_REQUEST_NOT_REPLAYABLE",
					"The previous request has not completed successfully.");
		}
		return view(existing, true);
	}

	private FmExternalRequestSubmitView view(FmApiRequest value, boolean replay) {
		return new FmExternalRequestSubmitView(value.getProcessInstanceId(),
				value.getFlowableProcessInstanceId(), value.getBusinessKey(),
				value.getDocumentNumber(), value.getFormDataId(), value.getProcessDefId(),
				value.getProcessVersionNo(), value.getFormId(), value.getFormVersionNo(),
				"RUNNING", value.getCompletedAt(), replay);
	}

	private String payloadHash(FmExternalRequestSubmitRequest request)
			throws ServiceException {
		try {
			JsonNode node = canonicalMapper.valueToTree(request);
			return hash(canonicalMapper.writeValueAsString(node));
		} catch (JacksonException exception) {
			throw new ServiceException("Unable to canonicalize submit payload.");
		}
	}

	private String hash(String value) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable.", exception);
		}
	}
}
