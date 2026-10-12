package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.test.util.ReflectionTestUtils;

class FmIncidentOperationsTenantAccessTest {

    @Test
    void operationsRoleCannotQueryTenantWithoutMembership() throws Exception {
        FmIncidentOperationsLogicServiceImpl logic =
                mock(FmIncidentOperationsLogicServiceImpl.class, CALLS_REAL_METHODS);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        DefaultResult<List<FmTenantAccount>> rows = new DefaultResult<>();
        rows.setValue(List.of());
        when(memberships.selectListByParams(anyMap())).thenReturn(rows);
        ReflectionTestUtils.setField(logic, "tenantAccountService", memberships);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("operator");

        try (var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::getCurrentUser).thenReturn(user);
            users.when(() -> UserUtils.hasRole("FLOWMINT_OPERATIONS")).thenReturn(true);

            assertThrows(ServiceException.class, () -> logic.incidents("OTHER", null));
            assertThrows(ServiceException.class, () -> logic.reassignOptions("OTHER"));
        }
    }

    @Test
    void reassignRoleCannotUseAnotherTenant() throws Exception {
        FmIncidentOperationsLogicServiceImpl logic =
                mock(FmIncidentOperationsLogicServiceImpl.class, CALLS_REAL_METHODS);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        DefaultResult<List<FmTenantAccount>> rows = new DefaultResult<>();
        rows.setValue(List.of());
        when(memberships.selectListByParams(anyMap())).thenReturn(rows);
        ReflectionTestUtils.setField(logic, "tenantAccountService", memberships);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("operator");

        try (var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::getCurrentUser).thenReturn(user);
            users.when(() -> UserUtils.hasRole("FLOWMINT_REASSIGN")).thenReturn(true);

            assertThrows(ServiceException.class, () -> logic.taskReassignOptions("OTHER"));
        }
    }
}
