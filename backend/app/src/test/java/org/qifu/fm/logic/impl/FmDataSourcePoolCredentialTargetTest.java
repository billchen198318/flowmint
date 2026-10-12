package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.datasource.FmDataSourcePasswordCipher;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmDataSourcePoolCommand;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.qifu.fm.service.IFmTenantService;

class FmDataSourcePoolCredentialTargetTest {

    @Test
    void changingTargetCannotReuseStoredPasswordInTestOrUpdate() throws Exception {
        IFmDataSourcePoolService pools = mock(IFmDataSourcePoolService.class);
        IFmTenantService tenants = mock(IFmTenantService.class);
        FmDataSourcePasswordCipher cipher = mock(FmDataSourcePasswordCipher.class);
        FmDataSourcePool stored = new FmDataSourcePool();
        stored.setTenantId("tenant");
        stored.setPoolCode("POOL");
        stored.setDbType("MARIADB");
        stored.setJdbcUrl("jdbc:mariadb://trusted/database");
        stored.setUsername("app");
        stored.setLockVersion(1);
        when(pools.selectByPrimaryKey("pool-oid")).thenReturn(result(stored));
        FmTenant tenant = new FmTenant();
        tenant.setTenantId("tenant");
        tenant.setStatus("ACTIVE");
        when(tenants.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(result(List.of(tenant)));
        var logic = new FmDataSourcePoolLogicServiceImpl(pools, tenants, cipher,
                mock(FmTenantAccessGuard.class));
        FmDataSourcePoolCommand changed = new FmDataSourcePoolCommand(
                "pool-oid", "tenant", "POOL", "Pool", "MARIADB",
                "jdbc:mariadb://untrusted/database", "app", "", 10, 1,
                10000L, 600000L, 1800000L, "SELECT 1", "ACTIVE", 1, null);

        assertThrows(ServiceException.class, () -> logic.test(changed));
        assertThrows(ServiceException.class, () -> logic.update(changed));
        verify(cipher, never()).decrypt(anyString());
        verify(pools, never()).update(stored);
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
