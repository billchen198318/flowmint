package org.qifu.fm.domain.tenant;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Component;

@Component
public class FmTenantStatusGuard {

    private final IFmTenantService tenantService;

    public FmTenantStatusGuard(IFmTenantService tenantService) {
        this.tenantService = tenantService;
    }

    public void requireActive(String tenantId) throws ServiceException {
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("Tenant 不可為空");
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        boolean active = tenantService.selectListByParams(parameters).getValue().stream()
                .anyMatch(tenant -> tenantId.equals(tenant.getTenantId())
                        && "ACTIVE".equals(tenant.getStatus()));
        if (!active) {
            throw new ServiceException("Tenant 不存在或已停用");
        }
    }
}
