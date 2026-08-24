package org.qifu.fm.dto.command;

import java.util.List;

public record FmParallelAddSignStartRequest(
        String taskId,
        List<String> memberAccounts,
        String reason,
        String requestKey) {
}
