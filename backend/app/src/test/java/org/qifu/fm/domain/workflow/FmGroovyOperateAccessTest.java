package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.RolePermissionAttr;
import org.qifu.base.model.UserRoleAndPermission;
import org.qifu.core.model.PermissionType;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;

class FmGroovyOperateAccessTest {

    @Test
    void adminCannotBypassExplicitCapabilityAndDisabledAccountsAreRejected() {
        var tenants = mock(FmTenantAccessGuard.class);
        var access = new FmGroovyOperateAccess(tenants);
        try (var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            for (var user : List.of(user("FLOWMINT_GROOVY_DESIGN", "Y"), user("FLOWMINT_GROOVY_OPERATE", "N"))) {
                users.when(UserUtils::getCurrentUser).thenReturn(user);
                assertThrows(ServiceException.class, () -> access.require("T"));
            }
            verifyNoInteractions(tenants);
        }
    }

    @Test
    void operationsRoleCapabilityAndTenantAccessAreAllRequired() throws Exception {
        var tenants = mock(FmTenantAccessGuard.class);
        var access = new FmGroovyOperateAccess(tenants);
        try (var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::getCurrentUser).thenReturn(user("FLOWMINT_GROOVY_OPERATE", "Y"));
            assertThrows(ServiceException.class, () -> access.require("T"));
            users.when(() -> UserUtils.hasRole("FLOWMINT_OPERATIONS")).thenReturn(true);
            assertDoesNotThrow(() -> access.require("T"));
            doThrow(new ServiceException("DENIED")).when(tenants).requireAccess("OTHER");
            assertThrows(ServiceException.class, () -> access.require("OTHER"));
        }
    }

    private User user(String capability, String enabled) {
        var permission = new RolePermissionAttr();
        permission.setType(PermissionType.CONTROLLER.name());
        permission.setPermission(capability);
        var role = new UserRoleAndPermission();
        role.setRole("operator");
        role.setRolePermission(List.of(permission));
        return new User("operator", "", enabled, List.of(role));
    }
}
