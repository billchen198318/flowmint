package org.qifu.fm.domain.attachment;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.view.FmAttachmentView;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FmAttachmentDownloadService {

    private static final String ATTACHMENT_SELECT = """
            SELECT a.ATTACHMENT_ID, a.FIELD_KEY, a.FILE_OID, a.FILE_NAME,
                   a.CONTENT_TYPE, a.FILE_SIZE, a.CDATE,
                   fd.OWNER_ACCOUNT, pi.INITIATOR_ACCOUNT, pi.PROCESS_INSTANCE_ID
              FROM fm_attachment a
              JOIN fm_form_data fd
                ON fd.TENANT_ID = a.TENANT_ID
               AND fd.FORM_DATA_ID = a.FORM_DATA_ID
              LEFT JOIN fm_process_instance pi
                ON pi.TENANT_ID = fd.TENANT_ID
               AND pi.FORM_DATA_ID = fd.FORM_DATA_ID
             WHERE a.TENANT_ID = :tenantId
               AND a.STATUS = 'ACTIVE'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FmAttachmentStorageService storageService;
    private final TaskService taskService;

    public FmAttachmentDownloadService(
            NamedParameterJdbcTemplate jdbcTemplate,
            FmAttachmentStorageService storageService,
            TaskService taskService) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
        this.taskService = taskService;
    }

    public List<FmAttachmentView> listByProcess(String tenantId, String processInstanceId)
            throws ServiceException {
        String account = currentAccount(tenantId);
        MapSqlParameterSource parameters = parameters(tenantId)
                .addValue("processInstanceId", processInstanceId);
        List<Map<String, Object>> processes = jdbcTemplate.queryForList("""
                SELECT fd.OWNER_ACCOUNT, pi.INITIATOR_ACCOUNT, pi.PROCESS_INSTANCE_ID
                  FROM fm_process_instance pi
                  JOIN fm_form_data fd
                    ON fd.TENANT_ID = pi.TENANT_ID
                   AND fd.FORM_DATA_ID = pi.FORM_DATA_ID
                 WHERE pi.TENANT_ID = :tenantId
                   AND pi.PROCESS_INSTANCE_ID = :processInstanceId
                """, parameters);
        authorize(processes, account);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                ATTACHMENT_SELECT + " AND pi.PROCESS_INSTANCE_ID = :processInstanceId"
                        + " ORDER BY a.FIELD_KEY, a.CDATE, a.OID",
                parameters);
        return rows.stream().map(this::view).toList();
    }

    public List<FmAttachmentView> listByTask(String tenantId, String taskId)
            throws ServiceException {
        String account = currentAccount(tenantId);
        Task task = taskService.createTaskQuery()
                .taskId(taskId).taskCandidateOrAssigned(account).singleResult();
        if (task == null) throw new ServiceException("找不到待辦或沒有附件檢視權限");
        return listByProcess(tenantId, task.getProcessInstanceId());
    }

    public DownloadFile download(String tenantId, String attachmentId)
            throws ServiceException {
        String account = currentAccount(tenantId);
        MapSqlParameterSource parameters = parameters(tenantId)
                .addValue("attachmentId", attachmentId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                ATTACHMENT_SELECT + " AND a.ATTACHMENT_ID = :attachmentId", parameters);
        authorize(rows, account);
        Map<String, Object> row = rows.get(0);
        byte[] content = storageService.readFormal(tenantId,
                String.valueOf(row.get("FILE_OID")), (Date) row.get("CDATE"));
        return new DownloadFile(String.valueOf(row.get("FILE_NAME")),
                String.valueOf(row.get("CONTENT_TYPE")), content);
    }

    @Transactional
    public boolean delete(String tenantId, String attachmentId) throws ServiceException {
        String account = currentAccount(tenantId);
        MapSqlParameterSource parameters = parameters(tenantId)
                .addValue("attachmentId", attachmentId)
                .addValue("account", account)
                .addValue("now", new Date());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                ATTACHMENT_SELECT + " AND a.ATTACHMENT_ID = :attachmentId", parameters);
        if (rows.isEmpty()) throw new ServiceException("找不到可刪除的附件");
        Map<String, Object> row = rows.get(0);
        if (!account.equals(row.get("OWNER_ACCOUNT"))
                && !account.equals(row.get("INITIATOR_ACCOUNT"))) {
            throw new ServiceException("沒有附件刪除權限");
        }
        Integer running = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM fm_process_instance
                 WHERE TENANT_ID = :tenantId
                   AND PROCESS_INSTANCE_ID = :processInstanceId
                   AND INSTANCE_STATUS = 'RUNNING'
                """, parameters.addValue(
                        "processInstanceId", row.get("PROCESS_INSTANCE_ID")), Integer.class);
        if (running == null || running != 1) {
            throw new ServiceException("流程已結束，附件不可刪除");
        }
        return jdbcTemplate.update("""
                UPDATE fm_attachment
                   SET STATUS = 'DELETED', UUSERID = :account, UDATE = :now
                 WHERE TENANT_ID = :tenantId
                   AND ATTACHMENT_ID = :attachmentId
                   AND STATUS = 'ACTIVE'
                """, parameters) == 1;
    }

    private void authorize(List<Map<String, Object>> rows, String account)
            throws ServiceException {
        if (rows.isEmpty()) throw new ServiceException("找不到附件或沒有檢視權限");
        Map<String, Object> row = rows.get(0);
        if (account.equals(row.get("OWNER_ACCOUNT"))
                || account.equals(row.get("INITIATOR_ACCOUNT"))) return;
        String processInstanceId = String.valueOf(row.get("PROCESS_INSTANCE_ID"));
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskCandidateOrAssigned(account)
                .active().listPage(0, 1).stream().findFirst().orElse(null);
        if (task == null) throw new ServiceException("找不到附件或沒有檢視權限");
    }

    private String currentAccount(String tenantId) throws ServiceException {
        if (StringUtils.isBlank(tenantId)) throw new ServiceException("Tenant 不可為空");
        return UserUtils.getCurrentUser().getUsername();
    }

    private MapSqlParameterSource parameters(String tenantId) {
        return new MapSqlParameterSource().addValue("tenantId", tenantId);
    }

    private FmAttachmentView view(Map<String, Object> row) {
        return new FmAttachmentView(String.valueOf(row.get("ATTACHMENT_ID")),
                String.valueOf(row.get("FIELD_KEY")), String.valueOf(row.get("FILE_NAME")),
                String.valueOf(row.get("CONTENT_TYPE")),
                ((Number) row.get("FILE_SIZE")).longValue());
    }

    public record DownloadFile(
            String fileName,
            String contentType,
            byte[] content) {
    }
}
