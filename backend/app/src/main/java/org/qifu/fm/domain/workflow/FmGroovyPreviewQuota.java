package org.qifu.fm.domain.workflow;

import java.util.HashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

/** Single application instance admission for Preview; no waiting while holding DB locks. */
@Component
public class FmGroovyPreviewQuota {

    private final Map<String, Integer> tenants = new HashMap<>();
    private int active;

    public synchronized Permit acquire(String tenantId) throws ServiceException {
        int count = tenants.getOrDefault(tenantId, 0);
        if (active >= 8 || count >= 2) {
            throw new ServiceException("GROOVY_PREVIEW_BUSY");
        }
        active++;
        tenants.put(tenantId, count + 1);
        return new Permit(tenantId);
    }

    public final class Permit implements AutoCloseable {

        private final String tenantId;
        private boolean closed;

        private Permit(String tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public void close() {
            synchronized (FmGroovyPreviewQuota.this) {
                if (closed) {
                    return;
                }
                closed = true;
                active--;
                int remaining = tenants.get(tenantId) - 1;
                if (remaining == 0) {
                    tenants.remove(tenantId);
                } else {
                    tenants.put(tenantId, remaining);
                }
            }
        }
    }
}
