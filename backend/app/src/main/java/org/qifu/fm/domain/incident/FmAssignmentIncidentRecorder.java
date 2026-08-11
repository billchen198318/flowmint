package org.qifu.fm.domain.incident;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;

@Component
public class FmAssignmentIncidentRecorder {

    private static final String INSERT_SQL = """
            INSERT INTO fm_assignment_incident
                (OID, TENANT_ID, INCIDENT_ID, PROCESS_INSTANCE_ID, TASK_ID,
                 TASK_DEF_KEY, INCIDENT_TYPE, ERROR_CODE, ERROR_MESSAGE,
                 CONTEXT_DATA, INCIDENT_STATUS, CUSERID, CDATE)
            VALUES
                (:oid, :tenantId, :incidentId, :processInstanceId, :taskId,
                 :taskDefKey, :incidentType, :errorCode, :errorMessage,
                 :contextData, 'OPEN', :actor, :now)
            """;
    private static final String SELECT_SQL = """
            SELECT INCIDENT_ID, PROCESS_INSTANCE_ID, TASK_ID, TASK_DEF_KEY,
                   INCIDENT_TYPE, ERROR_CODE, ERROR_MESSAGE, CONTEXT_DATA,
                   INCIDENT_STATUS, RESOLVED_BY, RESOLVED_DATE, RESOLUTION_NOTE, CDATE
              FROM fm_assignment_incident
             WHERE TENANT_ID = :tenantId
               AND (:status IS NULL OR INCIDENT_STATUS = :status)
             ORDER BY CDATE DESC
            """;
    private static final String RESOLVE_SQL = """
            UPDATE fm_assignment_incident
               SET INCIDENT_STATUS = 'RESOLVED', RESOLVED_BY = :actor,
                   RESOLVED_DATE = :now, RESOLUTION_NOTE = :note,
                   UUSERID = :actor, UDATE = :now
             WHERE TENANT_ID = :tenantId AND INCIDENT_ID = :incidentId
               AND INCIDENT_STATUS = 'OPEN'
            """;
    private static final String IGNORE_PROCESS_SQL = """
            UPDATE fm_assignment_incident
               SET INCIDENT_STATUS = 'IGNORED', RESOLVED_BY = :actor,
                   RESOLVED_DATE = :now, RESOLUTION_NOTE = :note,
                   UUSERID = :actor, UDATE = :now
             WHERE TENANT_ID = :tenantId
               AND PROCESS_INSTANCE_ID = :processInstanceId
               AND INCIDENT_STATUS = 'OPEN'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public FmAssignmentIncidentRecorder(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String record(IncidentCommand command) {
        String incidentId = UUID.randomUUID().toString();
        jdbcTemplate.update(INSERT_SQL, Map.ofEntries(
                Map.entry("oid", UUID.randomUUID().toString()),
                Map.entry("tenantId", command.tenantId()),
                Map.entry("incidentId", incidentId),
                Map.entry("processInstanceId", command.processInstanceId()),
                Map.entry("taskId", command.taskId()),
                Map.entry("taskDefKey", command.taskDefKey()),
                Map.entry("incidentType", command.incidentType()),
                Map.entry("errorCode", command.errorCode()),
                Map.entry("errorMessage", truncate(command.errorMessage(), 2000)),
                Map.entry("contextData", command.contextData()),
                Map.entry("actor", command.actor()),
                Map.entry("now", command.now())));
        return incidentId;
    }

    public List<FmAssignmentIncidentView> find(String tenantId, String status) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", status);
        return jdbcTemplate.query(SELECT_SQL, parameters,
                (rs, rowNum) -> new FmAssignmentIncidentView(
                        rs.getString("INCIDENT_ID"), rs.getString("PROCESS_INSTANCE_ID"),
                        rs.getString("TASK_ID"), rs.getString("TASK_DEF_KEY"),
                        rs.getString("INCIDENT_TYPE"), rs.getString("ERROR_CODE"),
                        rs.getString("ERROR_MESSAGE"), rs.getString("CONTEXT_DATA"),
                        rs.getString("INCIDENT_STATUS"), rs.getString("RESOLVED_BY"),
                        rs.getTimestamp("RESOLVED_DATE"), rs.getString("RESOLUTION_NOTE"),
                        rs.getTimestamp("CDATE")));
    }

    public FmAssignmentIncidentView requiredOpen(String tenantId, String incidentId) {
        return find(tenantId, "OPEN").stream()
                .filter(value -> incidentId.equals(value.incidentId()))
                .findFirst().orElse(null);
    }

    public boolean resolve(
            String tenantId, String incidentId, String actor,
            String note, Date now) {
        return jdbcTemplate.update(RESOLVE_SQL, Map.of(
                "tenantId", tenantId, "incidentId", incidentId,
                "actor", actor, "note", truncate(note, 2000), "now", now)) == 1;
    }

    public int ignoreOpenForProcess(
            String tenantId, String processInstanceId,
            String actor, String note, Date now) {
        return jdbcTemplate.update(IGNORE_PROCESS_SQL, Map.of(
                "tenantId", tenantId, "processInstanceId", processInstanceId,
                "actor", actor, "note", truncate(note, 2000), "now", now));
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "Unknown assignment error";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record IncidentCommand(
            String tenantId,
            String processInstanceId,
            String taskId,
            String taskDefKey,
            String incidentType,
            String errorCode,
            String errorMessage,
            String contextData,
            String actor,
            Date now) {
    }
}
