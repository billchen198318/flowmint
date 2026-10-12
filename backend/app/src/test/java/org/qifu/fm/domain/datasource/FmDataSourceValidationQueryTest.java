package org.qifu.fm.domain.datasource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.service.IFmDataSourcePoolService;

class FmDataSourceValidationQueryTest {

    @Test
    void onlyAllowsConstantConnectionProbes() {
        assertDoesNotThrow(() -> FmDataSourceValidationQuery.requireSafe(null));
        assertDoesNotThrow(() -> FmDataSourceValidationQuery.requireSafe("SELECT 1"));
        assertDoesNotThrow(() -> FmDataSourceValidationQuery.requireSafe(
                "select 1 from dual"));
        assertThrows(ServiceException.class, () -> FmDataSourceValidationQuery.requireSafe(
                "UPDATE orders SET status = 'DONE'"));
        assertThrows(ServiceException.class, () -> FmDataSourceValidationQuery.requireSafe(
                "SELECT 1; DELETE FROM orders"));
        assertThrows(ServiceException.class, () -> FmDataSourceValidationQuery.requireSafe(
                "SELECT dangerous_function()"));
    }

    @Test
    void existingUnsafePoolIsRejectedBeforePasswordDecryption() {
        IFmDataSourcePoolService pools = mock(IFmDataSourcePoolService.class);
        FmDataSourcePasswordCipher cipher = mock(FmDataSourcePasswordCipher.class);
        FmDataSourcePool pool = new FmDataSourcePool();
        pool.setValidationQuery("DELETE FROM orders");
        DefaultResult<List<FmDataSourcePool>> result = new DefaultResult<>();
        result.setValue(List.of(pool));
        when(pools.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(result);

        assertThrows(ServiceException.class,
                () -> new FmDataSourcePoolRegistry(pools, cipher).get("tenant", "pool"));
        verify(cipher, never()).decrypt(anyString());
    }
}
