package org.qifu.fm.domain.workflow;

import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.qifu.fm.logic.IFmGroovyAutomaticRetryLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** One retry authority; persisted failure time and generation survive scheduler restarts. */
@Component
public class FmGroovyAutomaticRetryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FmGroovyAutomaticRetryScheduler.class);
    private final IFmSystemTaskExecutionService executions;
    private final IFmGroovyAutomaticRetryLogicService retries;
    private final boolean enabled;
    private final List<String> tenants;
    private final Clock clock;
    private final Map<String, String> cursors = new ConcurrentHashMap<>();

    @Autowired
    public FmGroovyAutomaticRetryScheduler(IFmSystemTaskExecutionService executions,
            IFmGroovyAutomaticRetryLogicService retries,
            @Value("${flowmint.groovy.automatic-retry.enabled:false}") boolean retryEnabled,
            @Value("${flowmint.groovy.runtime.enabled:false}") boolean runtimeEnabled,
            @Value("${flowmint.groovy.recovery.tenant-ids:}") String tenantIds) {
        this(executions, retries, retryEnabled && runtimeEnabled, tenantIds, Clock.systemUTC());
    }

    public FmGroovyAutomaticRetryScheduler(IFmSystemTaskExecutionService executions,
            IFmGroovyAutomaticRetryLogicService retries, boolean enabled, String tenantIds, Clock clock) {
        this.executions = executions;
        this.retries = retries;
        this.enabled = enabled;
        this.clock = clock;
        tenants = Arrays.stream(tenantIds.split(",")).map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
        if (tenants.size() > 100 || tenants.stream().anyMatch(value -> value.length() > 36) || enabled && tenants.isEmpty()) {
            throw new IllegalArgumentException("GROOVY_RETRY_TENANTS_INVALID");
        }
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 1000)
    public void retryDue() {
        if (!enabled) {
            return;
        }
        var now = Date.from(clock.instant());
        for (String tenantId : tenants) {
            try {
                var rows = executions.findAutomaticRetries(tenantId, now, cursors.get(tenantId), 100);
                if (rows == null || rows.size() > 100) {
                    throw new IllegalStateException("GROOVY_RETRY_SCAN_INVALID");
                }
                String lastOid = null;
                for (var row : rows) {
                    try {
                        if (row == null || !tenantId.equals(row.getTenantId()) || row.getGeneration() == null
                                || row.getOid() == null || !row.getOid().matches("[A-Za-z0-9_-]{1,36}")) {
                            throw new IllegalStateException("GROOVY_RETRY_SCAN_IDENTITY_INVALID");
                        }
                        lastOid = row.getOid();
                        retries.retry(tenantId, row.getInvocationId(), row.getGeneration());
                    } catch (Exception failure) {
                        LOGGER.warn("Groovy automatic retry deferred for an invocation in tenant {}", tenantId);
                    }
                }
                if (rows.size() < 100) {
                    cursors.remove(tenantId);
                } else if (lastOid != null) {
                    cursors.put(tenantId, lastOid);
                }
            } catch (Exception failure) {
                LOGGER.warn("Groovy automatic retry scan deferred for tenant {}", tenantId);
            }
        }
    }
}
