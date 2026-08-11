package org.qifu.fm.dto.view;

import java.util.Date;

public record FmTaskActionView(
        String actionType,
        String outcome,
        String actorAccount,
        String comment,
        String reason,
        Date actionDate) {
}
