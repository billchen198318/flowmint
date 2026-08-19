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
import org.qifu.fm.service.IFmTenantAccountService;

class FmTenantAccessGuardTest {

    @Test
    void adminMayAccessAnyTenant() {
        FmTenantAccessGuard guard = new FmTenantAccessGuard(
                mock(IFmTenantAccountService.class));
        try (MockedStatic<UserUtils> users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            assertDoesNotThrow(() -> guard.requireAccess("T-ANY"));
            assertDoesNotThrow(() -> guard.requireQueryAccess(null));
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
            FmTenantAccessGuard guard = new FmTenantAccessGuard(service);

            assertDoesNotThrow(() -> guard.requireAccess("T-ALLOWED"));
            assertThrows(ServiceException.class, () -> guard.requireAccess("T-DENIED"));
            assertThrows(ServiceException.class, () -> guard.requireQueryAccess(null));
        }
    }
}
