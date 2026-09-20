package org.qifu.fm.domain.workflow;

import org.qifu.base.exception.ServiceException;
import org.qifu.core.model.PermissionType;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.springframework.stereotype.Component;

@Component
public class FmGroovyOperateAccess {

    private final FmTenantAccessGuard tenants;

    public FmGroovyOperateAccess(FmTenantAccessGuard tenants) {
        this.tenants = tenants;
    }

    public void require(String tenantId) throws ServiceException {
        var user = UserUtils.getCurrentUser();
        boolean explicit = user != null && user.isEnabled() && user.getRoles() != null && user.getRoles().stream()
                .filter(role -> role != null && role.getRolePermission() != null)
                .flatMap(role -> role.getRolePermission().stream())
                .anyMatch(permission -> permission != null
                        && PermissionType.CONTROLLER.name().equals(permission.getType())
                        && "FLOWMINT_GROOVY_OPERATE".equals(permission.getPermission()));
        if (!explicit || !(UserUtils.isAdmin() || UserUtils.hasRole("FLOWMINT_OPERATIONS"))) {
            throw new ServiceException("GROOVY_OPERATE_DENIED");
        }
        tenants.requireAccess(tenantId);
    }
}
