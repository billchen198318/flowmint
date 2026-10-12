package org.qifu.fm.domain.attachment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;

import tools.jackson.databind.ObjectMapper;

class FmAttachmentDownloadServiceTest {

    private final FmAttachmentDownloadService service =
            new FmAttachmentDownloadService(
                    mock(NamedParameterJdbcTemplate.class),
                    mock(org.qifu.fm.domain.tenant.FmTenantAccessGuard.class),
                    mock(FmAttachmentStorageService.class),
                    mock(TaskService.class),
                    new ObjectMapper(),
                    mock(org.qifu.fm.service.IFmTaskParallelAddSignService.class),
                    mock(org.qifu.fm.service.IFmTaskParallelAddSignMemberService.class));

    @Test
    void revokedTenantMembershipBlocksAllFormalAttachmentOperations() throws Exception {
        FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
        doThrow(new ServiceException("目前帳號無權存取此 Tenant"))
                .when(access).requireAccess("tenant");
        FmAttachmentDownloadService guarded = new FmAttachmentDownloadService(
                mock(NamedParameterJdbcTemplate.class), access,
                mock(FmAttachmentStorageService.class), mock(TaskService.class),
                new ObjectMapper(),
                mock(org.qifu.fm.service.IFmTaskParallelAddSignService.class),
                mock(org.qifu.fm.service.IFmTaskParallelAddSignMemberService.class));
        assertThrows(ServiceException.class, () -> guarded.listByProcess("tenant", "process"));
        assertThrows(ServiceException.class, () -> guarded.listByTask("tenant", "task"));
        assertThrows(ServiceException.class, () -> guarded.download("tenant", "attachment"));
        assertThrows(ServiceException.class, () -> guarded.delete("tenant", "attachment"));
    }

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
        assertFalse(service.fieldWritable(policy, "ordinaryFile"));
        assertFalse(service.fieldWritable(policy, "privateFile"));
        assertTrue(service.fieldWritable(policy, "editableFile"));
    }

    @Test
    void supportsDefaultHiddenAndRejectsInvalidPolicy() throws Exception {
        assertFalse(service.fieldVisible(
                "{\"default\":\"HIDDEN\",\"fields\":{}}", "attachment"));
        assertThrows(ServiceException.class,
                () -> service.fieldVisible("not-json", "attachment"));
    }

    @Test
    void activeTaskRequiresWritePolicyForFormalAttachmentDeletion() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        TaskService tasks = mock(TaskService.class);
        TaskQuery query = mock(TaskQuery.class);
        Task task = mock(Task.class);
        when(tasks.createTaskQuery()).thenReturn(query);
        when(query.processInstanceId(anyString())).thenReturn(query);
        when(query.taskCandidateOrAssigned(anyString())).thenReturn(query);
        when(query.active()).thenReturn(query);
        when(query.list()).thenReturn(List.of(task));
        when(task.getTaskDefinitionKey()).thenReturn("approval");
        FmAttachmentDownloadService guarded = new FmAttachmentDownloadService(
                jdbc, mock(org.qifu.fm.domain.tenant.FmTenantAccessGuard.class),
                mock(FmAttachmentStorageService.class), tasks, new ObjectMapper(),
                mock(org.qifu.fm.service.IFmTaskParallelAddSignService.class),
                mock(org.qifu.fm.service.IFmTaskParallelAddSignMemberService.class));
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class),
                eq(String.class))).thenReturn(List.of("{\"default\":\"READ\"}"));
        boolean readOnly = ReflectionTestUtils.invokeMethod(guarded,
                "mayDeleteForCurrentTask", "tenant", "process", "owner", "file");
        assertFalse(readOnly);
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class),
                eq(String.class))).thenReturn(List.of("{\"default\":\"READ\","
                        + "\"fields\":{\"file\":\"WRITE\"}}"));
        boolean writable = ReflectionTestUtils.invokeMethod(guarded,
                "mayDeleteForCurrentTask", "tenant", "process", "owner", "file");
        assertTrue(writable);
    }
}
