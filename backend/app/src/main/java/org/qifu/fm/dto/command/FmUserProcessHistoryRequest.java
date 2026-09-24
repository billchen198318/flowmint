package org.qifu.fm.dto.command;

public record FmUserProcessHistoryRequest(
        String relation,
        String status,
        String outcome,
        String keyword,
        String startDate,
        String endDate,
        String actionStartDate,
        String actionEndDate,
        Integer page,
        Integer pageSize) {
}
