package org.qifu.fm.domain.runtime;

import java.util.Date;
import java.util.List;

import org.qifu.fm.entity.FmProcessStartProxy;
import org.springframework.stereotype.Component;

@Component
public class FmProcessStartProxyEvaluator {

    public boolean isAuthorized(
            String starterAccount,
            String applicantAccount,
            String processDefId,
            List<FmProcessStartProxy> proxies,
            Date now) {
        if (starterAccount == null || applicantAccount == null) {
            return false;
        }
        if (starterAccount.equals(applicantAccount)) {
            return true;
        }
        if (proxies == null || now == null) {
            return false;
        }
        return proxies.stream()
                .filter(value -> "ACTIVE".equals(value.getStatus()))
                .filter(value -> !value.getEffectiveFrom().after(now)
                        && value.getEffectiveTo().after(now))
                .anyMatch(value -> "ALL".equals(value.getScopeType())
                        || "PROCESS".equals(value.getScopeType())
                                && processDefId.equals(value.getScopeRefId()));
    }
}
