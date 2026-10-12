package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.mapper.FmFormVersionMapper;
import org.springframework.test.util.ReflectionTestUtils;

class FmFormDraftLockTest {

    @Test
    void publishedVersionReadAfterLockCannotBeEditedAsDraft() {
        FmFormVersionMapper mapper = mock(FmFormVersionMapper.class);
        FmFormVersion version = new FmFormVersion();
        version.setTenantId("tenant");
        version.setVersionStatus("PUBLISHED");
        when(mapper.selectForUpdate("version-oid")).thenReturn(version);
        FmFormDefLogicServiceImpl logic = new FmFormDefLogicServiceImpl(
                null, null, mapper, null, null, null, null, null, null, null,
                mock(FmTenantAccessGuard.class));

        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(logic, "draft", "version-oid"));
    }
}
