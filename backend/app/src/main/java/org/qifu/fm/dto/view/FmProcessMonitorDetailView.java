package org.qifu.fm.dto.view;

import java.util.List;

public record FmProcessMonitorDetailView(
        FmProcessMonitorView process,
        List<FmTaskActionView> actions,
        List<FmFormSnapshotView> snapshots) {
}
