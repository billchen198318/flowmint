package org.qifu.fm.domain.workflow;

import org.qifu.base.exception.ServiceException;
import org.qifu.core.model.PermissionType;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FmGroovyPublishAccess {

    private final FmTenantAccessGuard tenantAccess;
    private final ApplicationContext context;
    private final boolean enabled;

    public FmGroovyPublishAccess(FmTenantAccessGuard tenantAccess, ApplicationContext context,
            @Value("${flowmint.groovy.publish.enabled:false}") boolean enabled) {
        this.tenantAccess = tenantAccess;
        this.context = context;
        this.enabled = enabled;
    }

    public void require(String tenantId) throws ServiceException {
        var user = UserUtils.getCurrentUser();
        boolean explicit = user != null && user.isEnabled() && user.getRoles() != null
                && user.getRoles().stream()
                        .filter(role -> role != null && role.getRolePermission() != null)
                        .flatMap(role -> role.getRolePermission().stream())
                        .anyMatch(permission -> permission != null
                                && PermissionType.CONTROLLER.name().equals(permission.getType())
                                && "FLOWMINT_GROOVY_PUBLISH".equals(permission.getPermission()));
        if (!explicit || !UserUtils.isPermitted("FM_PROG004D0001X", PermissionType.CONTROLLER.name())) {
            throw new ServiceException("GROOVY_PUBLISH_DENIED");
        }
        tenantAccess.requireAccess(tenantId);
        // Require the live engine and matching enabled compiler/runtime profiles.
        var readiness = context.getBeanProvider(RuntimeReadiness.class).getIfUnique();
        if (!enabled || readiness == null || !readiness.isReady()) {
            throw new ServiceException("GROOVY_PUBLISH_UNAVAILABLE");
        }
    }

    public interface RuntimeReadiness {
        boolean isReady();
    }
}
