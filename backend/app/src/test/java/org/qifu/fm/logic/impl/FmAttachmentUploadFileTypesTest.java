package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.ObjectMapper;

class FmAttachmentUploadFileTypesTest {

    @Test
    void explicitlyInvalidTypesNeverExpandToFullAllowlist() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FmAttachmentUploadLogicServiceImpl logic = new FmAttachmentUploadLogicServiceImpl(
                null, null, null, null, null, null, mapper, null);
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                logic, "configuredExtensions", mapper.readTree("[{\"value\":\".exe\"}]")));
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                logic, "configuredExtensions", mapper.readTree("[{\"value\":\".pdf,.exe\"}]")));
        Set<?> allowed = ReflectionTestUtils.invokeMethod(
                logic, "configuredExtensions", mapper.readTree("[{\"value\":\".pdf\"}]"));
        assertEquals(Set.of("pdf"), allowed);
    }
}
