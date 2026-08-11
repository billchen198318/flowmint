package org.qifu.fm.dto.view;

public record FmRuntimeTenantView(
        String tenantId,
        String tenantCode,
        String tenantName,
        boolean defaultTenant) {
}
