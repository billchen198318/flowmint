package org.qifu.fm.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.fm.mapper.FmProcessSystemTaskMapper;
import org.qifu.fm.service.impl.FmProcessSystemTaskServiceImpl;

class FmProcessSystemTaskServiceTest {

    @Test
    void findVersionAllowsBaseServiceToAttachOrderingParameters() throws Exception {
        var mapper = mock(FmProcessSystemTaskMapper.class);
        when(mapper.selectListByParams(org.mockito.ArgumentMatchers.anyMap())).thenReturn(List.of());
        var service = new FmProcessSystemTaskServiceImpl(mapper);

        assertDoesNotThrow(() -> service.findVersion("T1", "P1", 1));
        verify(mapper).selectListByParams(argThat(parameters ->
                "T1".equals(parameters.get("tenantId"))
                        && "P1".equals(parameters.get("processDefId"))
                        && Integer.valueOf(1).equals(parameters.get("versionNo"))
                        && "NODE_ID".equals(parameters.get("orderBy"))
                        && "ASC".equals(parameters.get("sortType"))));
    }
}
