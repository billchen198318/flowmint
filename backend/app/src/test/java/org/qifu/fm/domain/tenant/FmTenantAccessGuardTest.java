package org.qifu.fm.domain.tenant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;

class FmTenantAccessGuardTest {

    @Test
    void adminMayAccessAnyTenant() throws Exception {
        FmTenantAccessGuard guard = new FmTenantAccessGuard(
                mock(IFmTenantAccountService.class), activeTenants("T-ANY"));
        try (MockedStatic<UserUtils> users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            assertDoesNotThrow(() -> guard.requireAccess("T-ANY"));
            assertDoesNotThrow(() -> guard.requireQueryAccess("T-ANY"));
            assertThrows(ServiceException.class, () -> guard.requireQueryAccess(null));
        }
    }

    @Test
    void regularUserIsLimitedToActiveEffectiveMemberships() throws Exception {
        IFmTenantAccountService service = mock(IFmTenantAccountService.class);
        FmTenantAccount membership = new FmTenantAccount();
        membership.setTenantId("T-ALLOWED");
        DefaultResult<List<FmTenantAccount>> result = new DefaultResult<>();
        result.setValue(List.of(membership));
        when(service.<FmTenantAccount>selectListByParams(anyMap())).thenReturn(result);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("tester");

        try (MockedStatic<UserUtils> users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(false);
            users.when(UserUtils::getCurrentUser).thenReturn(user);
            FmTenantAccessGuard guard = new FmTenantAccessGuard(service, activeTenants("T-ALLOWED"));

            assertDoesNotThrow(() -> guard.requireAccess("T-ALLOWED"));
            assertThrows(ServiceException.class, () -> guard.requireAccess("T-DENIED"));
            assertThrows(ServiceException.class, () -> guard.requireQueryAccess(null));
        }
    }

    @Test
    void disabledTenantIsRejectedEvenForAdmin() throws Exception {
        IFmTenantService tenants = activeTenants("T-ACTIVE");
        FmTenantAccessGuard guard = new FmTenantAccessGuard(
                mock(IFmTenantAccountService.class), tenants);
        try (MockedStatic<UserUtils> users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            assertThrows(ServiceException.class, () -> guard.requireAccess("T-DISABLED"));
            assertThrows(ServiceException.class, () -> guard.requireQueryAccess("T-DISABLED"));
        }
    }

    private IFmTenantService activeTenants(String tenantId) throws Exception {
        IFmTenantService service = mock(IFmTenantService.class);
        FmTenant tenant = new FmTenant();
        tenant.setTenantId(tenantId);
        tenant.setStatus("ACTIVE");
        DefaultResult<List<FmTenant>> result = new DefaultResult<>();
        result.setValue(List.of(tenant));
        when(service.<FmTenant>selectListByParams(anyMap())).thenReturn(result);
        return service;
    }
}
