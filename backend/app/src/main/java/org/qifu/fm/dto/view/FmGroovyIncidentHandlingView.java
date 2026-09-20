package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmGroovyIncidentHandlingView(String status, int revision, List<Action> history, boolean hasMore) {

    public record Action(int revision, String fromStatus, String toStatus, String reason, String actor, Date date,
            String actionType, String targetInvocationId) {
    }
}
