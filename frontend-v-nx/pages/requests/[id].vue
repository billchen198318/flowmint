<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const tenantId = String(route.query.tenant || "");
const detail = ref<any>(null);
const formHost = ref<HTMLElement | null>(null);
const loading = ref(false);
const selectedSnapshot = ref<any>(null);
let formInstance: any = null;

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const destroyForm = () => {
  formInstance?.destroy?.(true);
  formInstance = null;
  if (formHost.value) formHost.value.innerHTML = "";
};
const renderData = async (data: any) => {
  destroyForm();
  await nextTick();
  if (!formHost.value || !detail.value) return;
  const { Formio } = await import("@formio/js");
  formInstance = await Formio.createForm(
    formHost.value,
    JSON.parse(detail.value.schemaContent || "{}"),
    { readOnly: true, noAlerts: true, noDefaultSubmitButton: true },
  );
  formInstance.submission = { data: data || {} };
};
const selectSnapshot = async (snapshot: any) => {
  selectedSnapshot.value = snapshot;
  await renderData(snapshot?.formData || detail.value?.currentFormData || {});
};
const load = async () => {
  if (!tenantId) {
    toast.warning("缺少 Tenant 參數");
    return;
  }
  loading.value = true;
  try {
    const response: any = await useApi("/fm/requests/mine/load", {
      method: "POST",
      body: { processInstanceId: route.params.id },
      headers: { "X-FlowMint-Tenant": tenantId },
    });
    if (!ok(response)) {
      toast.warning(response?.message || "無法載入申請進度");
      return;
    }
    detail.value = response.value;
    selectedSnapshot.value = response.value?.snapshots?.at(-1) || null;
    await renderData(
      selectedSnapshot.value?.formData || response.value?.currentFormData || {},
    );
  } finally {
    loading.value = false;
  }
};

onMounted(load);
onBeforeUnmount(destroyForm);
</script>

<template>
  <div class="container-fluid request-page">
    <button type="button" class="btn btn-link px-0 mb-3 text-decoration-none" @click="router.back()">
      <i class="bi bi-arrow-left"></i> 返回工作台
    </button>
    <div v-if="loading" class="text-center py-5 text-muted">
      <span class="spinner-border spinner-border-sm me-2"></span>載入中…
    </div>
    <template v-if="detail">
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body p-4">
          <div class="d-flex flex-wrap justify-content-between gap-3">
            <div>
              <div class="text-primary small fw-semibold mb-1">我的申請</div>
              <h2 class="h4 mb-2">{{ detail.request.processName }}</h2>
              <div class="text-muted">{{ detail.request.formName }}・申請人 {{ detail.request.applicantAccount }}</div>
            </div>
            <div class="text-end">
              <span :class="['badge fs-6', detail.request.instanceStatus === 'RUNNING' ? 'text-bg-primary' : detail.request.instanceStatus === 'COMPLETED' ? 'text-bg-success' : 'text-bg-secondary']">
                {{ detail.request.instanceStatus }}
              </span>
              <div class="small text-muted mt-2">目前節點：{{ detail.request.currentTaskNames?.join('、') || '—' }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-xl-8">
          <div class="card border-0 shadow-sm">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <strong>表單內容</strong>
              <span class="small text-muted">{{ selectedSnapshot ? `${selectedSnapshot.actionType} 快照` : '目前資料' }}</span>
            </div>
            <div class="card-body p-4"><div ref="formHost" class="runtime-form"></div></div>
          </div>
        </div>
        <div class="col-xl-4">
          <div class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-white py-3"><strong>表單快照</strong></div>
            <div class="list-group list-group-flush snapshot-list">
              <button
                v-for="snapshot in detail.snapshots"
                :key="snapshot.formSnapshotId"
                type="button"
                :class="['list-group-item list-group-item-action', selectedSnapshot?.formSnapshotId === snapshot.formSnapshotId ? 'active' : '']"
                @click="selectSnapshot(snapshot)"
              >
                <div class="fw-semibold">{{ snapshot.actionType }}・Revision {{ snapshot.revisionNo }}</div>
                <div class="small text-truncate mt-1">SHA-256：{{ snapshot.contentSha256 }}</div>
              </button>
            </div>
          </div>
          <div class="card border-0 shadow-sm">
            <div class="card-header bg-white py-3"><strong>流程紀錄</strong></div>
            <div class="card-body">
              <div v-for="(action, index) in detail.actions" :key="index" class="timeline-item">
                <div class="fw-semibold">{{ action.actionType }}・{{ action.actorAccount }}</div>
                <div class="small text-muted">{{ action.comment || action.reason || action.outcome }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.request-page { max-width: 1440px; }
.runtime-form { min-height: 220px; }
.snapshot-list { max-height: 360px; overflow-y: auto; }
.timeline-item { padding: 0.75rem 0; border-bottom: 1px solid var(--bs-border-color); }
.timeline-item:last-child { border-bottom: 0; }
</style>
