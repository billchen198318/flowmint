package org.qifu.fm.domain.externalapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.external.FmExternalFormTemplateRequest;
import org.qifu.fm.dto.external.FmExternalFormTemplateView;
import org.qifu.fm.dto.external.FmExternalProcessFormsRequest;
import org.qifu.fm.dto.external.FmExternalProcessFormsView;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmExternalDesignQueryService {

	private static final List<String> SYSTEM_FIELDS = List.of("tenantId", "businessKey",
			"documentNumber", "status", "initiatorAccount", "applicantAccount",
			"applicantOrgUnitId", "processInstanceId", "formDataId");
	private final IFmProcessDefService processDefService;
	private final IFmProcessVersionService processVersionService;
	private final IFmTaskFormRuleService taskFormRuleService;
	private final IFmFormDefService formDefService;
	private final IFmFormVersionService formVersionService;
	private final ObjectMapper objectMapper;

	public FmExternalDesignQueryService(IFmProcessDefService processDefService,
			IFmProcessVersionService processVersionService,
			IFmTaskFormRuleService taskFormRuleService,
			IFmFormDefService formDefService,
			IFmFormVersionService formVersionService, ObjectMapper objectMapper) {
		this.processDefService = processDefService;
		this.processVersionService = processVersionService;
		this.taskFormRuleService = taskFormRuleService;
		this.formDefService = formDefService;
		this.formVersionService = formVersionService;
		this.objectMapper = objectMapper;
	}

	public FmExternalProcessFormsView processForms(FmExternalProcessFormsRequest request)
			throws ServiceException {
		if (request == null || StringUtils.isBlank(request.processDefId())) {
			throw new ServiceException("processDefId is required.");
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"design.process.read");
		String processDefId = request.processDefId().trim();
		if (!principal.allowsProcess(processDefId)) {
			return null;
		}
		FmProcessDef process = publishedProcess(principal.tenantId(), processDefId);
		if (process == null) {
			return null;
		}
		FmProcessVersion version = publishedProcessVersion(principal.tenantId(),
				processDefId, request.versionNo() == null ? process.getCurrentVersionNo()
						: request.versionNo());
		if (version == null) {
			return null;
		}
		Map<String, BindingBuilder> forms = new LinkedHashMap<>();
		for (FmTaskFormRule rule : taskFormRuleService.findByVersion(
				principal.tenantId(), processDefId, version.getVersionNo())) {
			String key = rule.getFormId() + ":" + rule.getFormVersionNo();
			BindingBuilder builder = forms.computeIfAbsent(key,
					ignored -> new BindingBuilder(rule.getFormId(), rule.getFormVersionNo()));
			builder.taskKeys.add(rule.getTaskDefKey());
			builder.fieldPolicies.add(policySummary(rule));
		}
		List<FmExternalProcessFormsView.FormBinding> bindings = new ArrayList<>();
		for (BindingBuilder builder : forms.values()) {
			FmFormDef form = publishedForm(principal.tenantId(), builder.formId);
			FmFormVersion formVersion = publishedFormVersion(principal.tenantId(),
					builder.formId, builder.versionNo);
			if (form != null && formVersion != null) {
				bindings.add(new FmExternalProcessFormsView.FormBinding(builder.formId,
						form.getFormName(), builder.versionNo, formVersion.getContentSha256(),
						List.of("START", "USER_TASK"), List.copyOf(builder.taskKeys),
						List.copyOf(builder.fieldPolicies)));
			}
		}
		return new FmExternalProcessFormsView(processDefId, process.getProcessName(),
				version.getVersionNo(), StringUtils.isBlank(version.getFlowableDeploymentId())
						? "PUBLISHED" : "DEPLOYED", version.getPublishedDate(),
				List.copyOf(bindings));
	}

	public FmExternalFormTemplateView formTemplate(FmExternalFormTemplateRequest request)
			throws ServiceException {
		if (request == null || StringUtils.isBlank(request.formId())) {
			throw new ServiceException("formId is required.");
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"design.form.read");
		FmFormDef form = publishedForm(principal.tenantId(), request.formId().trim());
		if (form == null) {
			return null;
		}
		Integer versionNo = request.versionNo() == null ? form.getCurrentVersionNo()
				: request.versionNo();
		FmFormVersion version = publishedFormVersion(principal.tenantId(),
				form.getFormId(), versionNo);
		if (version == null || !isAllowedBinding(principal, form.getFormId(), versionNo)) {
			return null;
		}
		JsonNode schema = parseSchema(version.getSchemaContent());
		List<FmExternalFormTemplateView.FieldContract> fields = new ArrayList<>();
		List<FmExternalFormTemplateView.AttachmentField> attachments = new ArrayList<>();
		collectFields(schema.path("components"), fields, attachments);
		return new FmExternalFormTemplateView(form.getFormId(), form.getFormName(),
				versionNo, "FORM_IO", version.getContentSha256(), schema,
				List.copyOf(fields), SYSTEM_FIELDS, List.copyOf(attachments),
				version.getPublishedDate());
	}

	private boolean isAllowedBinding(FmExternalApiPrincipal principal, String formId,
			Integer formVersionNo) throws ServiceException {
		Map<String, Object> parameters = parameters(principal.tenantId(), "PUBLISHED");
		for (FmProcessDef process : processDefService.selectListByParams(parameters).getValue()) {
			if (!principal.allowsProcess(process.getProcessDefId())) {
				continue;
			}
			FmProcessVersion version = publishedProcessVersion(principal.tenantId(),
					process.getProcessDefId(), process.getCurrentVersionNo());
			if (version != null && taskFormRuleService.findByVersion(principal.tenantId(),
					process.getProcessDefId(), version.getVersionNo()).stream()
					.anyMatch(rule -> formId.equals(rule.getFormId())
							&& formVersionNo.equals(rule.getFormVersionNo()))) {
				return true;
			}
		}
		return false;
	}

	private FmExternalProcessFormsView.FieldPolicySummary policySummary(
			FmTaskFormRule rule) {
		try {
			JsonNode policy = objectMapper.readTree(rule.getFieldPolicy());
			return new FmExternalProcessFormsView.FieldPolicySummary(rule.getTaskDefKey(),
					policy.path("default").asText("READ"), policy.path("fields").size());
		} catch (Exception exception) {
			return new FmExternalProcessFormsView.FieldPolicySummary(rule.getTaskDefKey(),
					"READ", 0);
		}
	}

	private JsonNode parseSchema(String content) throws ServiceException {
		try {
			return objectMapper.readTree(content);
		} catch (JacksonException exception) {
			throw new ServiceException("Published form schema is invalid.");
		}
	}

	private void collectFields(JsonNode components,
			List<FmExternalFormTemplateView.FieldContract> fields,
			List<FmExternalFormTemplateView.AttachmentField> attachments) {
		if (!components.isArray()) {
			return;
		}
		for (JsonNode component : components) {
			String key = component.path("key").asText();
			String type = component.path("type").asText();
			boolean input = component.path("input").asBoolean(false);
			boolean multiple = component.path("multiple").asBoolean(false);
			if (input && StringUtils.isNotBlank(key) && !SYSTEM_FIELDS.contains(key)) {
				fields.add(new FmExternalFormTemplateView.FieldContract(key, type,
						component.path("validate").path("required").asBoolean(false), multiple));
				if ("file".equals(type)) {
					attachments.add(new FmExternalFormTemplateView.AttachmentField(key,
							multiple));
				}
			}
			collectFields(component.path("components"), fields, attachments);
			for (JsonNode column : component.path("columns")) {
				collectFields(column.path("components"), fields, attachments);
			}
			for (JsonNode row : component.path("rows")) {
				for (JsonNode cell : row) {
					collectFields(cell.path("components"), fields, attachments);
				}
			}
		}
	}

	private FmProcessDef publishedProcess(String tenantId, String processDefId)
			throws ServiceException {
		Map<String, Object> parameters = parameters(tenantId, "PUBLISHED");
		parameters.put("processDefId", processDefId);
		return processDefService.selectListByParams(parameters).getValue().stream()
				.findFirst().orElse(null);
	}

	private FmProcessVersion publishedProcessVersion(String tenantId,
			String processDefId, Integer versionNo) throws ServiceException {
		if (versionNo == null || versionNo < 1) {
			return null;
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processDefId", processDefId);
		parameters.put("versionNo", versionNo);
		parameters.put("versionStatus", "PUBLISHED");
		return processVersionService.selectListByParams(parameters).getValue().stream()
				.findFirst().orElse(null);
	}

	private FmFormDef publishedForm(String tenantId, String formId)
			throws ServiceException {
		Map<String, Object> parameters = parameters(tenantId, "PUBLISHED");
		parameters.put("formId", formId);
		return formDefService.selectListByParams(parameters).getValue().stream()
				.findFirst().orElse(null);
	}

	private FmFormVersion publishedFormVersion(String tenantId, String formId,
			Integer versionNo) throws ServiceException {
		if (versionNo == null || versionNo < 1) {
			return null;
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("formId", formId);
		parameters.put("versionNo", versionNo);
		parameters.put("versionStatus", "PUBLISHED");
		return formVersionService.selectListByParams(parameters).getValue().stream()
				.findFirst().orElse(null);
	}

	private Map<String, Object> parameters(String tenantId, String status) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("status", status);
		return parameters;
	}

	private static final class BindingBuilder {
		private final String formId;
		private final Integer versionNo;
		private final LinkedHashSet<String> taskKeys = new LinkedHashSet<>();
		private final List<FmExternalProcessFormsView.FieldPolicySummary> fieldPolicies =
				new ArrayList<>();

		private BindingBuilder(String formId, Integer versionNo) {
			this.formId = formId;
			this.versionNo = versionNo;
		}
	}
}
