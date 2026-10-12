package org.qifu.fm.domain.datasource;

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

class FmDataSourcePoolRegistrySelectionTest {

    @Test
    void refusesWrongOrInactivePoolReturnedByService() {
        IFmDataSourcePoolService pools = mock(IFmDataSourcePoolService.class);
        FmDataSourcePasswordCipher cipher = mock(FmDataSourcePasswordCipher.class);
        FmDataSourcePool wrong = new FmDataSourcePool();
        wrong.setTenantId("tenant");
        wrong.setPoolId("other-pool");
        wrong.setStatus("ACTIVE");
        FmDataSourcePool inactive = new FmDataSourcePool();
        inactive.setTenantId("tenant");
        inactive.setPoolId("requested-pool");
        inactive.setStatus("INACTIVE");
        DefaultResult<List<FmDataSourcePool>> result = new DefaultResult<>();
        result.setValue(List.of(wrong, inactive));
        when(pools.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(result);

        assertThrows(ServiceException.class,
                () -> new FmDataSourcePoolRegistry(pools, cipher)
                        .get("tenant", "requested-pool"));
        verify(cipher, never()).decrypt(anyString());
    }
}
