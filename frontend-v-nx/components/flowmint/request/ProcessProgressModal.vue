<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from "vue";
import BpmnViewer from "bpmn-js/lib/NavigatedViewer";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";

const props = defineProps<{
  show: boolean;
  tenantId: string;
  processInstanceId: string;
}>();
const emit = defineEmits<{ close: [] }>();

const canvasHost = ref<HTMLElement | null>(null);
const loading = ref(false);
const errorMessage = ref("");
const loaded = ref(false);
const processStatus = ref("");
let viewer: any = null;

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;

const destroyViewer = () => {
  viewer?.destroy?.();
  viewer = null;
  if (canvasHost.value) canvasHost.value.innerHTML = "";
};

const renderDiagram = async (diagram: any) => {
  await nextTick();
  if (!canvasHost.value) return;
  destroyViewer();
  viewer = new BpmnViewer({ container: canvasHost.value });
  await viewer.importXML(diagram.bpmnXml);
  const canvas = viewer.get("canvas");
  for (const activityId of diagram.completedActivityIds || []) {
    if (viewer.get("elementRegistry").get(activityId)) {
      canvas.addMarker(activityId, "flowmint-completed");
    }
  }
  for (const activityId of diagram.activeActivityIds || []) {
    if (viewer.get("elementRegistry").get(activityId)) {
      canvas.addMarker(activityId, "flowmint-active");
    }
  }
  canvas.zoom("fit-viewport", "auto");
};

const loadDiagram = async () => {
  if (loaded.value || loading.value) return;
  loading.value = true;
  errorMessage.value = "";
  try {
    const response: any = await useApi("/fm/requests/mine/diagram", {
      method: "POST",
      body: { processInstanceId: props.processInstanceId },
      headers: { "X-FlowMint-Tenant": props.tenantId },
    });
    if (!ok(response)) {
      errorMessage.value = response?.message || "無法載入流程進度";
      return;
    }
    processStatus.value = response.value?.processStatus || "";
    await renderDiagram(response.value);
    loaded.value = true;
  } catch {
    errorMessage.value = "無法載入流程進度";
  } finally {
    loading.value = false;
  }
};

const zoom = (direction: number) => {
  if (!viewer) return;
  const canvas = viewer.get("canvas");
  canvas.zoom(Math.max(0.2, Math.min(4, canvas.zoom() + direction)));
};
const resetView = () => viewer?.get("canvas")?.zoom("fit-viewport", "auto");

watch(() => props.show, async (show) => {
  if (!show) return;
  if (!loaded.value) {
    await loadDiagram();
    return;
  }
  await nextTick();
  viewer?.get("canvas")?.resized();
  resetView();
}, { immediate: true });
onBeforeUnmount(destroyViewer);
</script>

<template>
  <Teleport to="body">
    <div v-show="show" class="flow-progress-backdrop" @click.self="emit('close')">
      <section class="flow-progress-modal" role="dialog" aria-modal="true" aria-labelledby="flow-progress-title">
        <header class="flow-progress-header">
          <div>
            <h2 id="flow-progress-title" class="h5 mb-1">流程進度</h2>
            <div class="small text-secondary">狀態：{{ processStatus || '載入中' }}</div>
          </div>
          <button type="button" class="btn-close" aria-label="關閉" @click="emit('close')"></button>
        </header>
        <div class="flow-progress-toolbar">
          <div class="flow-progress-legend">
            <span><i class="legend-dot completed"></i>已完成</span>
            <span><i class="legend-dot active"></i>目前節點</span>
          </div>
          <div class="btn-group btn-group-sm" aria-label="流程圖縮放">
            <button type="button" class="btn btn-outline-secondary" @click="zoom(-0.15)">－</button>
            <button type="button" class="btn btn-outline-secondary" @click="resetView">適合畫面</button>
            <button type="button" class="btn btn-outline-secondary" @click="zoom(0.15)">＋</button>
          </div>
        </div>
        <div class="flow-progress-body">
          <div v-if="loading" class="progress-state"><span class="spinner-border spinner-border-sm me-2"></span>載入流程圖</div>
          <div v-else-if="errorMessage" class="progress-state text-danger">
            {{ errorMessage }}
            <button type="button" class="btn btn-sm btn-outline-danger ms-3" @click="loadDiagram">重試</button>
          </div>
          <div ref="canvasHost" class="flow-progress-canvas" :class="{ invisible: loading || errorMessage }"></div>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.flow-progress-backdrop { position: fixed; inset: 0; z-index: 1080; display: flex; align-items: center; justify-content: center; padding: 1.5rem; background: rgb(15 23 42 / 55%); }
.flow-progress-modal { display: flex; flex-direction: column; width: min(1400px, 96vw); height: min(850px, 92vh); overflow: hidden; background: #fff; border-radius: .75rem; box-shadow: 0 1.5rem 4rem rgb(15 23 42 / 30%); }
.flow-progress-header, .flow-progress-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1rem 1.25rem; border-bottom: 1px solid #dee2e6; }
.flow-progress-toolbar { padding-block: .65rem; }
.flow-progress-legend { display: flex; flex-wrap: wrap; gap: 1rem; font-size: .875rem; color: #495057; }
.legend-dot { display: inline-block; width: .75rem; height: .75rem; margin-right: .35rem; border-radius: 50%; vertical-align: -.05rem; }
.legend-dot.completed { background: #198754; }
.legend-dot.active { background: #0d6efd; }
.flow-progress-body { position: relative; flex: 1; min-height: 0; }
.flow-progress-canvas { width: 100%; height: 100%; background: #f8f9fa; }
.progress-state { position: absolute; inset: 0; z-index: 2; display: flex; align-items: center; justify-content: center; background: #fff; }
:deep(.flowmint-completed .djs-visual > :first-child) { stroke: #198754 !important; stroke-width: 3px !important; fill: #d1e7dd !important; }
:deep(.flowmint-active .djs-visual > :first-child) { stroke: #0d6efd !important; stroke-width: 4px !important; fill: #cfe2ff !important; filter: drop-shadow(0 0 4px rgb(13 110 253 / 55%)); }
@media (max-width: 767.98px) {
  .flow-progress-backdrop { padding: 0; }
  .flow-progress-modal { width: 100vw; height: 100dvh; border-radius: 0; }
  .flow-progress-header, .flow-progress-toolbar { padding-inline: .85rem; }
}
</style>
