package org.qifu.fm.domain.tenant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.service.IFmTenantService;

class FmTenantStatusGuardTest {

    @Test
    void rejectsDisabledTenantEvenWhenItExists() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        FmTenant disabled = new FmTenant();
        disabled.setTenantId("T-1");
        disabled.setStatus("INACTIVE");
        DefaultResult<List<FmTenant>> result = new DefaultResult<>();
        result.setValue(List.of(disabled));
        when(tenants.selectListByParams(anyMap())).thenReturn(result);

        assertThrows(ServiceException.class,
                () -> new FmTenantStatusGuard(tenants).requireActive("T-1"));
    }

    @Test
    void acceptsActiveTenant() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        FmTenant active = new FmTenant();
        active.setTenantId("T-1");
        active.setStatus("ACTIVE");
        DefaultResult<List<FmTenant>> result = new DefaultResult<>();
        result.setValue(List.of(active));
        when(tenants.selectListByParams(anyMap())).thenReturn(result);

        assertDoesNotThrow(() -> new FmTenantStatusGuard(tenants).requireActive("T-1"));
    }
}
