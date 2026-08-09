package org.qifu.fm.dto.command;

import java.util.List;

import org.qifu.fm.dto.view.FmResolverCandidateView;

public record FmAssignmentSnapshotCommand(
        String tenantId,
        String formDataId,
        String processInstanceId,
        String taskId,
        String taskDefKey,
        String resolverType,
        String sourceAccount,
        String sourceOrgUnitId,
        String resolutionContext,
        String resultType,
        List<FmResolverCandidateView> candidates) {
}
