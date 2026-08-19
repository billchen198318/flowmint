package org.qifu.fm.domain.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Component;

@Component
public class FmTenantAccessGuard {

    private final IFmTenantAccountService tenantAccountService;

    public FmTenantAccessGuard(IFmTenantAccountService tenantAccountService) {
        this.tenantAccountService = tenantAccountService;
    }

    public void requireAccess(String tenantId) throws ServiceException {
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("Tenant 不可為空");
        }
        if (UserUtils.isAdmin()) {
            return;
        }
        if (!accessibleTenantIds().contains(tenantId)) {
            throw new ServiceException("目前帳號無權存取此 Tenant");
        }
    }

    public void requireQueryAccess(String tenantId) throws ServiceException {
        if (UserUtils.isAdmin()) {
            return;
        }
        requireAccess(tenantId);
    }

    public Set<String> accessibleTenantIds() throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("account", UserUtils.getCurrentUser().getUsername());
        parameters.put("status", "ACTIVE");
        Date now = new Date();
        return tenantAccountService.selectListByParams(parameters).getValue().stream()
                .filter(value -> effective(value, now))
                .map(FmTenantAccount::getTenantId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean effective(FmTenantAccount value, Date now) {
        return (value.getEffectiveFrom() == null || !value.getEffectiveFrom().after(now))
                && (value.getEffectiveTo() == null || value.getEffectiveTo().after(now));
    }
}
