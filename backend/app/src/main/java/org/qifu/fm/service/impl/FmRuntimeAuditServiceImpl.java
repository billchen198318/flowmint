package org.qifu.fm.service.impl;

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
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.entity.FmOrgUnitVersion;
import org.qifu.fm.mapper.FmRuntimeAuditMapper;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmRuntimeAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmRuntimeAuditServiceImpl implements IFmRuntimeAuditService {

    private final FmRuntimeAuditMapper mapper;
    private final IFmEmployeeOrgAssignmentService assignmentService;
    private final IFmOrgUnitVersionService orgUnitVersionService;
    private final IFmOrgTitleService orgTitleService;
    private final IFmOrgApprovalLevelService approvalLevelService;

    public FmRuntimeAuditServiceImpl(
            FmRuntimeAuditMapper mapper,
            IFmEmployeeOrgAssignmentService assignmentService,
            IFmOrgUnitVersionService orgUnitVersionService,
            IFmOrgTitleService orgTitleService,
            IFmOrgApprovalLevelService approvalLevelService) {
        this.mapper = mapper;
        this.assignmentService = assignmentService;
        this.orgUnitVersionService = orgUnitVersionService;
        this.orgTitleService = orgTitleService;
        this.approvalLevelService = approvalLevelService;
    }

    @Override
    @Transactional(readOnly = false)
    public String recordAssignmentSnapshot(FmAssignmentSnapshotCommand command, Date now) {
        String snapshotId = UUID.randomUUID().toString();
        Map<String, Object> header = base(command.tenantId(), now);
        header.put("assignmentSnapshotId", snapshotId);
        header.put("processInstanceId", command.processInstanceId());
        header.put("taskId", command.taskId());
        header.put("taskDefKey", command.taskDefKey());
        header.put("resolutionSeq", mapper.selectNextResolutionSeq(
                command.tenantId(),
                command.processInstanceId(),
                command.taskDefKey()));
        header.put("resolverType", command.resolverType());
        header.put("sourceAccount", command.sourceAccount());
        header.put("sourceOrgUnitId", command.sourceOrgUnitId());
        header.put("resolutionStatus", "RESOLVED");
        header.put("resolutionContext", command.resolutionContext());
        header.put("cuserid", command.sourceAccount());
        mapper.insertAssignmentSnapshot(header);

        int resultSeq = 1;
        for (FmResolverCandidateView candidate : command.candidates()) {
            Map<String, Object> detail = candidateDetail(
                    command, snapshotId, candidate, resultSeq++, now);
            mapper.insertAssignmentSnapshotDetail(detail);
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
        String formSnapshotId = UUID.randomUUID().toString();
        Map<String, Object> snapshot = base(tenantId, now);
        snapshot.put("formSnapshotId", formSnapshotId);
        snapshot.put("formDataId", formData.getFormDataId());
        snapshot.put("processInstanceId", processInstanceId);
        snapshot.put("taskId", null);
        snapshot.put("actionType", "SUBMIT");
        snapshot.put("formVersionNo", formData.getFormVersionNo());
        snapshot.put("revisionNo", formData.getRevisionNo());
        snapshot.put("dataContent", formData.getDataContent());
        snapshot.put("contentSha256", sha256(formData.getDataContent()));
        snapshot.put("cuserid", actorAccount);
        mapper.insertFormSnapshot(snapshot);

        Map<String, Object> action = base(tenantId, now);
        action.put("taskActionId", UUID.randomUUID().toString());
        action.put("processInstanceId", processInstanceId);
        action.put("taskId", null);
        action.put("taskDefKey", null);
        action.put("actionType", "SUBMIT");
        action.put("outcome", "SUBMITTED");
        action.put("actorAccount", actorAccount);
        action.put("principalAccount", applicantAccount);
        action.put("fromAccount", null);
        action.put("toAccount", null);
        action.put("formSnapshotId", formSnapshotId);
        action.put("assignmentSnapshotId",
                mapper.selectFirstAssignmentSnapshotId(tenantId, processInstanceId));
        action.put("commentText", null);
        action.put("reason", null);
        action.put("contextData", null);
        action.put("cuserid", actorAccount);
        mapper.insertTaskAction(action);
    }

    private Map<String, Object> candidateDetail(
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

        Map<String, Object> detail = base(command.tenantId(), now);
        detail.put("assignmentSnapshotId", snapshotId);
        detail.put("resultSeq", resultSeq);
        detail.put("resultType", command.resultType());
        detail.put("resultAccount", candidate.account());
        detail.put("principalAccount", candidate.account());
        detail.put("orgUnitId", assignment == null ? null : assignment.getOrgUnitId());
        detail.put("orgUnitName", orgUnit == null ? null : orgUnit.getUnitName());
        detail.put("approvalLevelId", level == null ? null : level.getApprovalLevelId());
        detail.put("levelCode", level == null ? null : level.getLevelCode());
        detail.put("levelName", level == null ? null : level.getLevelName());
        detail.put("levelOrder", level == null ? null : level.getLevelOrder());
        detail.put("resolutionPath", command.resolutionContext());
        detail.put("cuserid", command.sourceAccount());
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
        return orgUnitVersionService.selectListByParams(parameters, "VERSION_NO", "DESC")
                .getValue().stream()
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

    private Map<String, Object> base(String tenantId, Date now) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oid", UUID.randomUUID().toString());
        parameters.put("tenantId", tenantId);
        parameters.put("actionDate", now);
        parameters.put("snapshotDate", now);
        parameters.put("resolvedDate", now);
        parameters.put("cuserid", "SYSTEM");
        parameters.put("cdate", now);
        return parameters;
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
