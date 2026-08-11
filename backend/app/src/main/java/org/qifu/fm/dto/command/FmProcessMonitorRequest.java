package org.qifu.fm.dto.command;

public record FmProcessMonitorRequest(
        String status,
        String keyword,
        Integer page,
        Integer pageSize) {
}
