package org.qifu.fm.domain.workflow;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.flowable.job.api.Job;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.json.JsonMapper;

/** Immutable engine identity captured before crossing the FutureJavaDelegate thread boundary. */
public record FmGroovyJobIdentity(String tenantId, String executionId, String processInstanceId,
        String processDefinitionId, String nodeId) {

    public FmGroovyJobIdentity {
        for (String value : List.of(tenantId, executionId, processInstanceId, processDefinitionId, nodeId)) {
            if (value.isBlank() || value.length() > 200) {
                throw new IllegalArgumentException("GROOVY_JOB_IDENTITY_INVALID");
            }
        }
    }

    public String invocation(Job job) throws ServiceException {
        if (job == null || !tenantId.equals(job.getTenantId()) || !executionId.equals(job.getExecutionId())
                || !processInstanceId.equals(job.getProcessInstanceId())
                || !processDefinitionId.equals(job.getProcessDefinitionId()) || !nodeId.equals(job.getElementId())
                || !"async-continuation".equals(job.getJobHandlerType())
                || !Job.JOB_TYPE_MESSAGE.equals(job.getJobType()) || !job.isExclusive()
                || job.getId() == null || job.getId().isBlank() || job.getId().length() > 64
                || job.getCorrelationId() == null || job.getCorrelationId().isBlank()
                || job.getCorrelationId().length() > 64) {
            throw new ServiceException("GROOVY_JOB_IDENTITY_INVALID");
        }
        // Flowable 8 preserves correlationId when moving jobs. Technical IDs alone are not stable.
        return invocationForCorrelation(tenantId, job.getCorrelationId());
    }

    public static String invocationForCorrelation(String tenantId, String correlationId) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || correlationId == null
                || !correlationId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new ServiceException("GROOVY_JOB_IDENTITY_INVALID");
        }
        String identity = JsonMapper.builder().build().writeValueAsString(
                List.of("flowmint-groovy-job-v1", tenantId, correlationId));
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
