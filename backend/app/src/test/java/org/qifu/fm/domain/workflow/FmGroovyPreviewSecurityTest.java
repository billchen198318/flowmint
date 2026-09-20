package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.RolePermissionAttr;
import org.qifu.base.model.UserRoleAndPermission;
import org.qifu.core.model.PermissionType;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;

class FmGroovyPreviewSecurityTest {

    @Test
    void administratorStillNeedsExplicitGroovyCapability() throws Exception {
        FmTenantAccessGuard tenant = mock(FmTenantAccessGuard.class);
        var guard = new FmGroovyDesignAccess(tenant);
        try (var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            users.when(() -> UserUtils.isPermitted("FM_PROG004D0001U", PermissionType.CONTROLLER.name())).thenReturn(true);
            users.when(UserUtils::getCurrentUser).thenReturn(new User("admin", "", "Y", List.of()));
            assertThrows(ServiceException.class, () -> guard.require("T1"));
            verifyNoInteractions(tenant);
            users.when(UserUtils::getCurrentUser).thenReturn(user("CONTROLLER", "FLOWMINT_GROOVY_DESIGN", "Y"));
            assertDoesNotThrow(() -> guard.require("T1"));
            verify(tenant).requireAccess("T1");
        }
    }

    @Test
    void wrongPermissionTypeInactiveAccountAndMissingProgramAreDenied() {
        FmTenantAccessGuard tenant = mock(FmTenantAccessGuard.class);
        var guard = new FmGroovyDesignAccess(tenant);
        try (var users = mockStatic(UserUtils.class)) {
            users.when(() -> UserUtils.isPermitted("FM_PROG004D0001U", PermissionType.CONTROLLER.name())).thenReturn(true);
            for (User user : List.of(user("VIEW", "FLOWMINT_GROOVY_DESIGN", "Y"),
                    user("CONTROLLER", "FLOWMINT_GROOVY_PUBLISH", "Y"),
                    user("CONTROLLER", "FLOWMINT_GROOVY_DESIGN", "N"))) {
                users.when(UserUtils::getCurrentUser).thenReturn(user);
                assertThrows(ServiceException.class, () -> guard.require("T1"));
            }
            users.when(UserUtils::getCurrentUser).thenReturn(user("CONTROLLER", "FLOWMINT_GROOVY_DESIGN", "Y"));
            users.when(() -> UserUtils.isPermitted("FM_PROG004D0001U", PermissionType.CONTROLLER.name())).thenReturn(false);
            assertThrows(ServiceException.class, () -> guard.require("T1"));
            verifyNoInteractions(tenant);
        }
    }

    @Test
    void quotaEnforcesTenantAndGlobalLimitsAndReleasesIdempotently() throws Exception {
        var quota = new FmGroovyPreviewQuota();
        var permits = new ArrayList<FmGroovyPreviewQuota.Permit>();
        try {
            for (int index = 0; index < 4; index++) {
                permits.add(quota.acquire("T" + index));
                permits.add(quota.acquire("T" + index));
                int tenantIndex = index;
                assertThrows(ServiceException.class, () -> quota.acquire("T" + tenantIndex));
            }
            assertThrows(ServiceException.class, () -> quota.acquire("another"));
            permits.getFirst().close();
            permits.getFirst().close();
            permits.add(quota.acquire("another"));
            assertThrows(ServiceException.class, () -> quota.acquire("one-too-many"));
        } finally {
            permits.forEach(FmGroovyPreviewQuota.Permit::close);
        }
        try (var permit = quota.acquire("T0")) {
            assertDoesNotThrow(permit::close);
        }
    }

    private User user(String type, String capability, String enabled) {
        RolePermissionAttr permission = new RolePermissionAttr();
        permission.setType(type);
        permission.setPermission(capability);
        UserRoleAndPermission role = new UserRoleAndPermission();
        role.setRole("designer");
        role.setRolePermission(List.of(permission));
        return new User("designer", "", enabled, List.of(role));
    }
}
