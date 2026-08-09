package org.qifu.fm.logic.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.fm.dto.command.FmAssignmentSnapshotCommand;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.entity.FmOrgUnitVersion;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;
import org.qifu.fm.entity.FmTaskAssignmentSnapshotDtl;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTaskActionService;
import org.qifu.fm.service.IFmTaskAssignmentSnapshotDtlService;
import org.qifu.fm.service.IFmTaskAssignmentSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmRuntimeAuditLogicServiceImpl implements IFmRuntimeAuditLogicService {

	private final IFmFormDataService formDataService;
	private final IFmFormSnapshotService formSnapshotService;
	private final IFmTaskActionService taskActionService;
	private final IFmTaskAssignmentSnapshotService assignmentSnapshotService;
	private final IFmTaskAssignmentSnapshotDtlService assignmentSnapshotDtlService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmOrgTitleService orgTitleService;
	private final IFmOrgApprovalLevelService approvalLevelService;

	public FmRuntimeAuditLogicServiceImpl(
			IFmFormDataService formDataService,
			IFmFormSnapshotService formSnapshotService,
			IFmTaskActionService taskActionService,
			IFmTaskAssignmentSnapshotService assignmentSnapshotService,
			IFmTaskAssignmentSnapshotDtlService assignmentSnapshotDtlService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmOrgTitleService orgTitleService,
			IFmOrgApprovalLevelService approvalLevelService) {
		this.formDataService = formDataService;
		this.formSnapshotService = formSnapshotService;
		this.taskActionService = taskActionService;
		this.assignmentSnapshotService = assignmentSnapshotService;
		this.assignmentSnapshotDtlService = assignmentSnapshotDtlService;
		this.assignmentService = assignmentService;
		this.orgUnitVersionService = orgUnitVersionService;
		this.orgTitleService = orgTitleService;
		this.approvalLevelService = approvalLevelService;
	}

	@Override
	@Transactional(readOnly = false)
	public String recordAssignmentSnapshot(
			FmAssignmentSnapshotCommand command, Date now) {
		String lockedOid = formDataService.lockByFormDataId(
				command.tenantId(), command.formDataId());
		if (StringUtils.isBlank(lockedOid)) {
			throw new IllegalStateException("指派快照找不到所屬表單資料");
		}
		String snapshotId = UUID.randomUUID().toString();
		FmTaskAssignmentSnapshot snapshot = new FmTaskAssignmentSnapshot();
		snapshot.setTenantId(command.tenantId());
		snapshot.setAssignmentSnapshotId(snapshotId);
		snapshot.setProcessInstanceId(command.processInstanceId());
		snapshot.setTaskId(command.taskId());
		snapshot.setTaskDefKey(command.taskDefKey());
		snapshot.setResolutionSeq(assignmentSnapshotService.nextResolutionSeq(
				command.tenantId(), command.processInstanceId(), command.taskDefKey()));
		snapshot.setResolverType(command.resolverType());
		snapshot.setSourceAccount(command.sourceAccount());
		snapshot.setSourceOrgUnitId(command.sourceOrgUnitId());
		snapshot.setResolutionStatus("RESOLVED");
		snapshot.setResolutionContext(command.resolutionContext());
		snapshot.setResolvedDate(now);
		snapshot.setCuserid(command.sourceAccount());
		snapshot.setCdate(now);
		assignmentSnapshotService.insert(snapshot);

		int resultSeq = 1;
		for (FmResolverCandidateView candidate : command.candidates()) {
			assignmentSnapshotDtlService.insert(candidateDetail(
					command, snapshotId, candidate, resultSeq++, now));
		}
		return snapshotId;
	}

	@Override
	@Transactional(readOnly = false)
	public void recordSubmit(
			String tenantId,
			String processInstanceId,
			FmFormData formData,
			String actorAccount,
			String applicantAccount,
			Date now) {
		FmFormSnapshot snapshot = new FmFormSnapshot();
		snapshot.setTenantId(tenantId);
		snapshot.setFormSnapshotId(UUID.randomUUID().toString());
		snapshot.setFormDataId(formData.getFormDataId());
		snapshot.setProcessInstanceId(processInstanceId);
		snapshot.setActionType("SUBMIT");
		snapshot.setFormVersionNo(formData.getFormVersionNo());
		snapshot.setRevisionNo(formData.getRevisionNo());
		snapshot.setDataContent(formData.getDataContent());
		snapshot.setContentSha256(sha256(formData.getDataContent()));
		snapshot.setSnapshotDate(now);
		snapshot.setCuserid(actorAccount);
		snapshot.setCdate(now);
		formSnapshotService.insert(snapshot);

		FmTaskAction action = new FmTaskAction();
		action.setTenantId(tenantId);
		action.setTaskActionId(UUID.randomUUID().toString());
		action.setProcessInstanceId(processInstanceId);
		action.setActionType("SUBMIT");
		action.setOutcome("SUBMITTED");
		action.setActorAccount(actorAccount);
		action.setPrincipalAccount(applicantAccount);
		action.setFormSnapshotId(snapshot.getFormSnapshotId());
		action.setAssignmentSnapshotId(
				assignmentSnapshotService.firstAssignmentSnapshotId(
						tenantId, processInstanceId));
		action.setActionDate(now);
		action.setCuserid(actorAccount);
		action.setCdate(now);
		taskActionService.insert(action);
	}

	private FmTaskAssignmentSnapshotDtl candidateDetail(
			FmAssignmentSnapshotCommand command,
			String snapshotId,
			FmResolverCandidateView candidate,
			int resultSeq,
			Date now) {
		FmEmployeeOrgAssignment assignment = primaryAssignment(
				command.tenantId(), candidate.employeeId());
		FmOrgUnitVersion orgUnit = assignment == null ? null
				: activeOrgUnit(command.tenantId(), assignment.getOrgUnitId());
		FmOrgTitle title = assignment == null ? null
				: activeTitle(command.tenantId(), assignment.getTitleId());
		FmOrgApprovalLevel level = title == null ? null
				: activeLevel(command.tenantId(), title.getApprovalLevelId());

		FmTaskAssignmentSnapshotDtl detail = new FmTaskAssignmentSnapshotDtl();
		detail.setTenantId(command.tenantId());
		detail.setAssignmentSnapshotId(snapshotId);
		detail.setResultSeq(resultSeq);
		detail.setResultType(command.resultType());
		detail.setResultAccount(candidate.account());
		detail.setPrincipalAccount(candidate.account());
		detail.setOrgUnitId(assignment == null ? null : assignment.getOrgUnitId());
		detail.setOrgUnitName(orgUnit == null ? null : orgUnit.getUnitName());
		detail.setApprovalLevelId(level == null ? null : level.getApprovalLevelId());
		detail.setLevelCode(level == null ? null : level.getLevelCode());
		detail.setLevelName(level == null ? null : level.getLevelName());
		detail.setLevelOrder(level == null ? null : level.getLevelOrder());
		detail.setResolutionPath(command.resolutionContext());
		detail.setCuserid(command.sourceAccount());
		detail.setCdate(now);
		return detail;
	}

	private FmEmployeeOrgAssignment primaryAssignment(String tenantId, String employeeId) {
		Map<String, Object> parameters = activeParameters(tenantId);
		parameters.put("employeeId", employeeId);
		parameters.put("isPrimary", "Y");
		return assignmentService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo()))
				.findFirst().orElse(null);
	}

	private FmOrgUnitVersion activeOrgUnit(String tenantId, String orgUnitId) {
		Map<String, Object> parameters = activeParameters(tenantId);
		parameters.put("orgUnitId", orgUnitId);
		return orgUnitVersionService.selectListByParams(
				parameters, "VERSION_NO", "DESC").getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo()))
				.findFirst().orElse(null);
	}

	private FmOrgTitle activeTitle(String tenantId, String titleId) {
		if (StringUtils.isBlank(titleId)) {
			return null;
		}
		Map<String, Object> parameters = activeParameters(tenantId);
		parameters.put("titleId", titleId);
		return orgTitleService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo()))
				.findFirst().orElse(null);
	}

	private FmOrgApprovalLevel activeLevel(String tenantId, String levelId) {
		if (StringUtils.isBlank(levelId)) {
			return null;
		}
		Map<String, Object> parameters = activeParameters(tenantId);
		parameters.put("approvalLevelId", levelId);
		return approvalLevelService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo()))
				.findFirst().orElse(null);
	}

	private Map<String, Object> activeParameters(String tenantId) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("status", "ACTIVE");
		return parameters;
	}

	private boolean effective(Date from, Date to) {
		Date now = new Date();
		return (from == null || !from.after(now)) && (to == null || to.after(now));
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(
					digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}

}
