package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.tenant.FmTenantStatusGuard;
import org.qifu.fm.service.IFmNotificationService;
import org.qifu.fm.service.IFmTenantAccountService;

class FmNotificationTenantStatusTest {

    @Test
    void disabledTenantIsRejectedBeforeReadingNotifications() throws Exception {
        IFmNotificationService notifications = mock(IFmNotificationService.class);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        FmTenantStatusGuard status = mock(FmTenantStatusGuard.class);
        doThrow(new ServiceException("Tenant 已停用")).when(status).requireActive("T-1");

        FmNotificationLogicServiceImpl logic = new FmNotificationLogicServiceImpl(
                notifications, memberships, status);
        assertThrows(ServiceException.class, () -> logic.inbox("T-1"));
        verifyNoInteractions(notifications, memberships);
    }
}
