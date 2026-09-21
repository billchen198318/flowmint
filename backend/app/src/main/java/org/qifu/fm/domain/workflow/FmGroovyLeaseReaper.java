package org.qifu.fm.domain.workflow;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.qifu.fm.logic.IFmGroovyRecoveryLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** No batch transaction: each invocation is recovered through its own proxied short transaction. */
@Component
public class FmGroovyLeaseReaper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FmGroovyLeaseReaper.class);
    private final IFmSystemTaskExecutionService executions;
    private final IFmGroovyRecoveryLogicService recovery;
    private final boolean enabled;
    private final List<String> tenants;

    public FmGroovyLeaseReaper(IFmSystemTaskExecutionService executions, IFmGroovyRecoveryLogicService recovery,
            @Value("${flowmint.groovy.recovery.enabled:false}") boolean enabled,
            @Value("${flowmint.groovy.recovery.tenant-ids:}") String tenantIds) {
        this.executions = executions;
        this.recovery = recovery;
        this.enabled = enabled;
        tenants = Arrays.stream(tenantIds.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                .distinct().toList();
        if (tenants.size() > 100 || tenants.stream().anyMatch(value -> value.length() > 36)
                || enabled && tenants.isEmpty()) {
            throw new IllegalArgumentException("GROOVY_RECOVERY_TENANTS_INVALID");
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 30000)
    public void reap() {
        if (!enabled) {
            return;
        }
        for (String tenantId : tenants) {
            try {
                var expired = executions.findExpired(tenantId, new Date(), 100);
                for (var row : expired) {
                    try {
                        if (!tenantId.equals(row.getTenantId()) || row.getGeneration() == null) {
                            throw new IllegalStateException("GROOVY_RECOVERY_IDENTITY_INVALID");
                        }
                        recovery.expire(tenantId, row.getInvocationId(), row.getGeneration());
                    } catch (Exception failure) {
                        // No worker content, exception text or input snapshots in operational logs.
                        LOGGER.warn("Groovy lease recovery deferred for an invocation in tenant {}", tenantId);
                    }
                }
            } catch (Exception failure) {
                LOGGER.warn("Groovy lease scan deferred for tenant {}", tenantId);
            }
        }
    }
}
