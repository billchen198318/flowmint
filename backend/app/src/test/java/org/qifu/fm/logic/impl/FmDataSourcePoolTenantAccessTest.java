package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.datasource.FmDataSourcePasswordCipher;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.qifu.fm.service.IFmTenantService;

class FmDataSourcePoolTenantAccessTest {

    @Test
    void loadRequiresAccessToStoredPoolsTenant() throws Exception {
        IFmDataSourcePoolService pools = mock(IFmDataSourcePoolService.class);
        FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
        FmDataSourcePool pool = new FmDataSourcePool();
        pool.setTenantId("other-tenant");
        when(pools.selectByPrimaryKey("pool")).thenReturn(result(pool));
        org.mockito.Mockito.doThrow(new ServiceException("無權存取"))
                .when(access).requireAccess("other-tenant");
        var logic = new FmDataSourcePoolLogicServiceImpl(pools,
                mock(IFmTenantService.class), mock(FmDataSourcePasswordCipher.class), access);

        assertThrows(ServiceException.class, () -> logic.load("pool", "loaded"));
        verify(access).requireAccess("other-tenant");
    }

    @Test
    void tenantOptionsOnlyExposeReadableTenants() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
        FmTenant own = tenant("own", "OWN");
        FmTenant other = tenant("other", "OTHER");
        when(access.readableTenantIds()).thenReturn(Set.of("own"));
        when(tenants.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(result(List.of(own, other)));
        var logic = new FmDataSourcePoolLogicServiceImpl(
                mock(IFmDataSourcePoolService.class), tenants,
                mock(FmDataSourcePasswordCipher.class), access);

        assertEquals(1, logic.tenantOptions().getValue().size());
        assertEquals("own", logic.tenantOptions().getValue().get(0).value());
    }

    private static FmTenant tenant(String id, String code) {
        FmTenant tenant = new FmTenant();
        tenant.setTenantId(id);
        tenant.setTenantCode(code);
        tenant.setTenantName(code);
        return tenant;
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
