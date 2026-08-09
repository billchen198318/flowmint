package org.qifu.fm.dto.view;

import java.util.List;

public record FmResolverPreviewView(
        String taskDefKey,
        Integer ruleSeq,
        String resolverType,
        String resultStatus,
        String message,
        List<FmResolverCandidateView> candidates) {
}
