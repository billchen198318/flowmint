package org.qifu.fm.domain.attachment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import tools.jackson.databind.ObjectMapper;

class FmAttachmentDownloadServiceTest {

    private final FmAttachmentDownloadService service =
            new FmAttachmentDownloadService(
                    mock(NamedParameterJdbcTemplate.class),
                    mock(FmAttachmentStorageService.class),
                    mock(TaskService.class),
                    new ObjectMapper(),
                    mock(org.qifu.fm.service.IFmTaskParallelAddSignService.class),
                    mock(org.qifu.fm.service.IFmTaskParallelAddSignMemberService.class));

    @Test
    void appliesTaskFieldPolicyToAttachmentField() throws Exception {
        String policy = """
                {
                  "default": "READ",
                  "fields": {
                    "privateFile": "HIDDEN",
                    "editableFile": "WRITE"
                  }
                }
                """;

        assertTrue(service.fieldVisible(policy, "ordinaryFile"));
        assertTrue(service.fieldVisible(policy, "editableFile"));
        assertFalse(service.fieldVisible(policy, "privateFile"));
    }

    @Test
    void supportsDefaultHiddenAndRejectsInvalidPolicy() throws Exception {
        assertFalse(service.fieldVisible(
                "{\"default\":\"HIDDEN\",\"fields\":{}}", "attachment"));
        assertThrows(ServiceException.class,
                () -> service.fieldVisible("not-json", "attachment"));
    }
}
