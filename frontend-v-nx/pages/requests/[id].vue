<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";
import { useBaseStore } from "@/store/baseStore";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const baseStore = useBaseStore();
const tenantId = String(route.query.tenant || "");
const detail = ref<any>(null);
const formHost = ref<HTMLElement | null>(null);
const loading = ref(false);
const withdrawing = ref(false);
const withdrawReason = ref("");
const cancelling = ref(false);
const cancelReason = ref("");
const selectedSnapshot = ref<any>(null);
const attachments = ref<any[]>([]);
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
    const attachmentResponse: any = await useApi(
      `/fm/attachments/processes/${route.params.id}`,
      { headers: { "X-FlowMint-Tenant": tenantId } },
    );
    attachments.value = ok(attachmentResponse) ? attachmentResponse.value || [] : [];
    selectedSnapshot.value = response.value?.snapshots?.at(-1) || null;
    await renderData(
      selectedSnapshot.value?.formData || response.value?.currentFormData || {},
    );
  } finally {
    loading.value = false;
  }
};
const downloadAttachment = async (attachment: any) => {
  try {
    const content: any = await useApi(
      `/fm/attachments/${attachment.attachmentId}/download`,
      { responseType: "blob", headers: { "X-FlowMint-Tenant": tenantId } },
    );
    const url = URL.createObjectURL(content);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = attachment.fileName;
    anchor.click();
    URL.revokeObjectURL(url);
  } catch {
    toast.warning("附件下載失敗");
  }
};
const deleteAttachment = async (attachment: any) => {
  if (!window.confirm(`確定刪除附件「${attachment.fileName}」？`)) return;
  const response: any = await useApi(
    `/fm/attachments/${attachment.attachmentId}/delete`,
    { method: "POST", headers: { "X-FlowMint-Tenant": tenantId } },
  );
  if (!ok(response)) {
    toast.warning(response?.message || "附件刪除失敗");
    return;
  }
  attachments.value = attachments.value.filter(
    (file: any) => file.attachmentId !== attachment.attachmentId,
  );
  toast.success("附件已刪除");
};
const withdraw = async () => {
  const reason = withdrawReason.value.trim();
  if (!reason) {
    toast.warning("請填寫撤回原因");
    return;
  }
  if (!window.confirm("確定要撤回這筆申請？撤回後流程將立即終止。")) return;
  withdrawing.value = true;
  try {
    const response: any = await useApi("/fm/requests/mine/withdraw", {
      method: "POST",
      body: { processInstanceId: route.params.id, reason },
      headers: { "X-FlowMint-Tenant": tenantId },
    });
    if (!ok(response)) {
      toast.warning(response?.message || "撤回申請失敗");
      return;
    }
    toast.success("申請已撤回");
    withdrawReason.value = "";
    await load();
  } finally {
    withdrawing.value = false;
  }
};
const cancel = async () => {
  const reason = cancelReason.value.trim();
  if (!reason) {
    toast.warning("請填寫取消原因");
    return;
  }
  if (!window.confirm("確定要取消這筆代申請流程？取消後流程將立即終止。")) return;
  cancelling.value = true;
  try {
    const response: any = await useApi("/fm/requests/mine/cancel", {
      method: "POST",
      body: { processInstanceId: route.params.id, reason },
      headers: { "X-FlowMint-Tenant": tenantId },
    });
    if (!ok(response)) {
      toast.warning(response?.message || "取消流程失敗");
      return;
    }
    toast.success("流程已取消");
    cancelReason.value = "";
    await load();
  } finally {
    cancelling.value = false;
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
          <div v-if="attachments.length" class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-white py-3"><strong>附件</strong></div>
            <div class="list-group list-group-flush">
              <div v-for="file in attachments" :key="file.attachmentId"
                class="list-group-item d-flex justify-content-between align-items-center gap-2">
                <button type="button" class="btn btn-link text-start text-decoration-none p-0"
                  @click="downloadAttachment(file)">
                  <i class="bi bi-paperclip me-2"></i>{{ file.fileName }}
                  <span class="d-block small text-muted ms-4">{{ file.fieldKey }} · {{ file.fileSize }} bytes</span>
                </button>
                <button
                  v-if="detail.request.instanceStatus === 'RUNNING'
                    && [detail.request.applicantAccount, detail.request.starterAccount].includes(baseStore.userId)"
                  type="button" class="btn btn-sm btn-outline-danger"
                  @click="deleteAttachment(file)">
                  刪除
                </button>
              </div>
            </div>
          </div>
          <div
            v-if="detail.request.instanceStatus === 'RUNNING' && detail.request.applicantAccount === baseStore.userId"
            class="card border-danger-subtle shadow-sm mb-4"
          >
            <div class="card-header bg-white py-3"><strong>撤回申請</strong></div>
            <div class="card-body">
              <label for="withdrawReason" class="form-label">撤回原因</label>
              <textarea
                id="withdrawReason"
                v-model="withdrawReason"
                class="form-control"
                rows="3"
                maxlength="1000"
                :disabled="withdrawing"
              ></textarea>
              <button
                type="button"
                class="btn btn-outline-danger mt-3 w-100"
                :disabled="withdrawing || !withdrawReason.trim()"
                @click="withdraw"
              >
                <span
                  v-if="withdrawing"
                  class="spinner-border spinner-border-sm me-2"
                ></span>
                撤回申請
              </button>
            </div>
          </div>
          <div
            v-if="detail.request.instanceStatus === 'RUNNING' && detail.request.initiatorAccount === baseStore.userId && detail.request.applicantAccount !== baseStore.userId"
            class="card border-warning-subtle shadow-sm mb-4"
          >
            <div class="card-header bg-white py-3"><strong>取消代申請流程</strong></div>
            <div class="card-body">
              <p class="small text-muted">你是這筆代申請的實際發起人。</p>
              <label for="cancelReason" class="form-label">取消原因</label>
              <textarea
                id="cancelReason"
                v-model="cancelReason"
                class="form-control"
                rows="3"
                maxlength="1000"
                :disabled="cancelling"
              ></textarea>
              <button
                type="button"
                class="btn btn-outline-warning mt-3 w-100"
                :disabled="cancelling || !cancelReason.trim()"
                @click="cancel"
              >
                <span
                  v-if="cancelling"
                  class="spinner-border spinner-border-sm me-2"
                ></span>
                取消流程
              </button>
            </div>
          </div>
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
