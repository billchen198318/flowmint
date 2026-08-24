package org.qifu.fm.logic;

import java.util.Date;

import org.qifu.fm.dto.command.FmAssignmentSnapshotCommand;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;

public interface IFmRuntimeAuditLogicService {

    String recordAssignmentSnapshot(FmAssignmentSnapshotCommand command, Date now);

    void recordSubmit(
            String tenantId,
            String processInstanceId,
            FmFormData formData,
            String actorAccount,
            String applicantAccount,
            Date now);

    void recordTaskAction(
            String tenantId,
            String processInstanceId,
            String taskId,
            String taskDefKey,
            String actionType,
            String outcome,
            String actorAccount,
            String principalAccount,
            String comment,
            String reason,
            FmFormData formData,
            FmTaskAssignmentSnapshot assignmentSnapshot,
            Date now);

    String recordParallelAddSignAction(
            String tenantId,
            String processInstanceId,
            String taskId,
            String taskDefKey,
            String actionType,
            String outcome,
            String actorAccount,
            String principalAccount,
            String comment,
            String reason,
            FmFormData formData,
            Date now);
}
