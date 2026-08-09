package org.qifu.fm.service;

import java.util.Date;

import org.qifu.fm.dto.command.FmAssignmentSnapshotCommand;
import org.qifu.fm.entity.FmFormData;

public interface IFmRuntimeAuditService {

    String recordAssignmentSnapshot(FmAssignmentSnapshotCommand command, Date now);

    void recordSubmit(
            String tenantId,
            String processInstanceId,
            FmFormData formData,
            String actorAccount,
            String applicantAccount,
            Date now);
}
