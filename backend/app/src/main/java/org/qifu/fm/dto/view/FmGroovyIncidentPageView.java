package org.qifu.fm.dto.view;

import java.util.List;

public record FmGroovyIncidentPageView(List<FmGroovyIncidentView> items, boolean hasMore) {
}
