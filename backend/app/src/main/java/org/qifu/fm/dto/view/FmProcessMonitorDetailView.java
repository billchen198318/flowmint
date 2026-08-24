package org.qifu.fm.dto.view;

import java.util.List;

public record FmProcessMonitorDetailView(
        FmProcessMonitorView process,
        boolean canReassign,
        List<FmActiveTaskOperationsView> activeTasks,
        List<FmTaskActionView> actions,
        List<FmFormSnapshotView> snapshots,
        List<FmParallelAddSignDetailView> parallelAddSigns) {
}
