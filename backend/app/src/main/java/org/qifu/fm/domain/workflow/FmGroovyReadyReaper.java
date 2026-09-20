package org.qifu.fm.domain.workflow;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.qifu.fm.logic.IFmGroovyReadyRecoveryLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Bounded scans; each item enters a fresh transaction through the recovery service proxy. */
@Component
public class FmGroovyReadyReaper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FmGroovyReadyReaper.class);
    private final IFmSystemTaskExecutionService executions;
    private final IFmGroovyReadyRecoveryLogicService recovery;
    private final boolean enabled;
    private final List<String> tenants;
    private final Map<String, String> cursors = new ConcurrentHashMap<>();

    public FmGroovyReadyReaper(IFmSystemTaskExecutionService executions, IFmGroovyReadyRecoveryLogicService recovery,
            @Value("${flowmint.groovy.recovery.enabled:false}") boolean recoveryEnabled,
            @Value("${flowmint.groovy.recovery.ready-enabled:false}") boolean readyEnabled,
            @Value("${flowmint.groovy.recovery.tenant-ids:}") String tenantIds) {
        this.executions = executions;
        this.recovery = recovery;
        enabled = recoveryEnabled && readyEnabled;
        tenants = Arrays.stream(tenantIds.split(",")).map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
        if (tenants.size() > 100 || tenants.stream().anyMatch(value -> value.length() > 36) || enabled && tenants.isEmpty()) {
            throw new IllegalArgumentException("GROOVY_RECOVERY_TENANTS_INVALID");
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 30000)
    public void reap() {
        if (!enabled) return;
        var cutoff = Date.from(Instant.now().minusSeconds(IFmGroovyReadyRecoveryLogicService.GRACE_SECONDS));
        for (String tenantId : tenants) {
            try {
                var rows = executions.findStalledReady(tenantId, cutoff, cursors.get(tenantId), 100);
                if (rows == null || rows.size() > 100) throw new IllegalStateException("GROOVY_READY_SCAN_INVALID");
                String lastOid = null;
                for (var row : rows) {
                    try {
                        if (row == null || !tenantId.equals(row.getTenantId()) || row.getGeneration() == null
                                || row.getOid() == null || !row.getOid().matches("[A-Za-z0-9_-]{1,36}")) {
                            throw new IllegalStateException("GROOVY_READY_RECOVERY_IDENTITY_INVALID");
                        }
                        lastOid = row.getOid();
                        recovery.recover(tenantId, row.getInvocationId(), row.getGeneration());
                    } catch (Exception failure) {
                        LOGGER.warn("Groovy ready recovery deferred for an invocation in tenant {}", tenantId);
                    }
                }
                // Keep moving past intentionally suspended/queued jobs; wrap after the last page.
                if (rows.size() < 100) cursors.remove(tenantId);
                else if (lastOid != null) cursors.put(tenantId, lastOid);
            } catch (Exception failure) {
                LOGGER.warn("Groovy ready scan deferred for tenant {}", tenantId);
            }
        }
    }
}
