package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.context.support.StaticApplicationContext;

class FmGroovyPublishAccessTest {

    @Test
    void explicitPublishCapabilityAndProgramAreBothRequired() {
        var tenant = mock(FmTenantAccessGuard.class);
        try (var context = new StaticApplicationContext(); var users = mockStatic(UserUtils.class)) {
            context.getBeanFactory().registerSingleton("ready",
                    (FmGroovyPublishAccess.RuntimeReadiness) () -> true);
            var access = new FmGroovyPublishAccess(tenant, context, true);
            users.when(() -> UserUtils.isPermitted("FM_PROG004D0001X", PermissionType.CONTROLLER.name()))
                    .thenReturn(true);
            for (User user : List.of(user("FLOWMINT_GROOVY_DESIGN", "Y"),
                    user("FLOWMINT_GROOVY_PUBLISH", "N"), new User("admin", "", "Y", List.of()))) {
                users.when(UserUtils::getCurrentUser).thenReturn(user);
                assertThrows(ServiceException.class, () -> access.require("T"));
            }
            users.when(UserUtils::getCurrentUser).thenReturn(user("FLOWMINT_GROOVY_PUBLISH", "Y"));
            users.when(() -> UserUtils.isPermitted("FM_PROG004D0001X", PermissionType.CONTROLLER.name()))
                    .thenReturn(false);
            assertThrows(ServiceException.class, () -> access.require("T"));
            verifyNoInteractions(tenant);
        }
    }

    @Test
    void configurationCannotEnableMissingRuntimeReadiness() {
        try (var context = new StaticApplicationContext(); var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::getCurrentUser).thenReturn(user("FLOWMINT_GROOVY_PUBLISH", "Y"));
            users.when(() -> UserUtils.isPermitted("FM_PROG004D0001X", PermissionType.CONTROLLER.name()))
                    .thenReturn(true);
            var tenant = mock(FmTenantAccessGuard.class);
            var access = new FmGroovyPublishAccess(tenant, context, true);
            assertThrows(ServiceException.class, () -> access.require("T"));
            context.getBeanFactory().registerSingleton("ready",
                    (FmGroovyPublishAccess.RuntimeReadiness) () -> true);
            assertDoesNotThrow(() -> access.require("T"));
            assertThrows(ServiceException.class,
                    () -> new FmGroovyPublishAccess(tenant, context, false).require("T"));
        }
    }

    private User user(String capability, String enabled) {
        var permission = new RolePermissionAttr();
        permission.setType(PermissionType.CONTROLLER.name());
        permission.setPermission(capability);
        var role = new UserRoleAndPermission();
        role.setRole("publisher");
        role.setRolePermission(List.of(permission));
        return new User("publisher", "", enabled, List.of(role));
    }
}
