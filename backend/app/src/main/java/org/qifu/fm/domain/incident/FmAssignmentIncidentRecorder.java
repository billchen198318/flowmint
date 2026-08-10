package org.qifu.fm.domain.incident;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

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
