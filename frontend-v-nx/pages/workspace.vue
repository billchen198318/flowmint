<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";
import { useBaseStore } from "@/store/baseStore";
import { useFormioDataActionBridge } from "@/composables/useFormioDataActionBridge";
import { useFormCustomJavascript } from "@/composables/useFormCustomJavascript";

definePageMeta({ layout: "default", middleware: ["auth"] });

const baseStore = useBaseStore();
const tenants = ref<any[]>([]);
const processes = ref<any[]>([]);
const inbox = ref<any[]>([]);
const myRequests = ref<any[]>([]);
const startData = ref<any>(null);
const selectedForm = ref<any>(null);
const tenantId = ref("");
const applicantAccount = ref(baseStore.userId);
const processDefId = ref("");
const formHost = ref<HTMLElement | null>(null);
const loading = ref(false);
const submitting = ref(false);
const result = ref<any>(null);
const idempotencyKey = ref("");
const { attach: attachDataActionBridge } = useFormioDataActionBridge();
const { attach: attachCustomJavascript } = useFormCustomJavascript();
let formInstance: any = null;
let detachDataActionBridge: (() => void) | null = null;
let detachCustomJavascript: (() => Promise<void>) | null = null;

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const runtimePost = (path: string, body: any = {}, headers: any = {}) =>
  useApi(`/fm/requests${path}`, { method: "POST", body, headers });
const tenantHeaders = () => ({ "X-FlowMint-Tenant": tenantId.value });
const showError = (response: any, fallback: string) => {
  toast.warning(response?.message || fallback);
};
const destroyForm = async () => {
  detachDataActionBridge?.();
  detachDataActionBridge = null;
  await detachCustomJavascript?.();
  detachCustomJavascript = null;
  formInstance?.destroy?.(true);
  formInstance = null;
  if (formHost.value) formHost.value.innerHTML = "";
};
const renderForm = async () => {
  await destroyForm();
  await nextTick();
  if (!formHost.value || !selectedForm.value) return;
  const { Formio } = await import("@formio/js");
  const schema = JSON.parse(selectedForm.value.schemaContent || "{}");
  formInstance = await Formio.createForm(formHost.value, schema, {
    noAlerts: true,
    noDefaultSubmitButton: true,
  });
  let uiSchema: any = { engine: "FORMIO", version: 1 };
  try {
    uiSchema = JSON.parse(selectedForm.value.uiSchemaContent || "{}");
  } catch {
    // Published schema validity is enforced by the backend.
  }
  detachDataActionBridge = attachDataActionBridge(
    formInstance,
    tenantId.value,
    uiSchema,
  );
  const script = await attachCustomJavascript({
    scriptContent: selectedForm.value.customScriptContent || "",
    form: formInstance,
    tenantId: tenantId.value,
    formId: selectedForm.value.formId,
    formCode: selectedForm.value.formCode,
    versionNo: selectedForm.value.formVersionNo,
    mode: "RUNTIME_START",
  });
  detachCustomJavascript = script.detach;
};
const loadTenants = async () => {
  const response: any = await runtimePost("/start/tenants");
  if (!ok(response)) return showError(response, "無法載入公司清單");
  tenants.value = response.value || [];
  const preferred = tenants.value.find((item) => item.defaultTenant);
  tenantId.value = preferred?.tenantId || tenants.value[0]?.tenantId || "";
};
const loadCatalog = async () => {
  processes.value = [];
  processDefId.value = "";
  startData.value = null;
  selectedForm.value = null;
  result.value = null;
  idempotencyKey.value = "";
  await destroyForm();
  if (!tenantId.value || !applicantAccount.value.trim()) return;
  loading.value = true;
  try {
    const response: any = await runtimePost(
      "/start/catalog",
      { applicantAccount: applicantAccount.value.trim() },
      tenantHeaders(),
    );
    if (!ok(response)) return showError(response, "無法載入可發起流程");
    processes.value = response.value || [];
  } finally {
    loading.value = false;
  }
};
const loadInbox = async () => {
  inbox.value = [];
  if (!tenantId.value) return;
  const response: any = await runtimePost(
    "/tasks/inbox",
    {},
    tenantHeaders(),
  );
  if (!ok(response)) return showError(response, "無法載入我的待辦");
  inbox.value = response.value || [];
};
const loadMyRequests = async () => {
  myRequests.value = [];
  if (!tenantId.value) return;
  const response: any = await runtimePost("/mine", {}, tenantHeaders());
  if (!ok(response)) return showError(response, "無法載入我的申請");
  myRequests.value = response.value || [];
};
const loadStart = async () => {
  startData.value = null;
  selectedForm.value = null;
  result.value = null;
  idempotencyKey.value = "";
  await destroyForm();
  if (!processDefId.value) return;
  loading.value = true;
  try {
    const response: any = await runtimePost(
      "/start/load",
      {
        processDefId: processDefId.value,
        applicantAccount: applicantAccount.value.trim(),
      },
      tenantHeaders(),
    );
    if (!ok(response)) return showError(response, "無法載入起單表單");
    startData.value = response.value;
    selectedForm.value = response.value?.forms?.[0] || null;
    idempotencyKey.value = crypto.randomUUID();
  } finally {
    loading.value = false;
  }
};
const submit = async () => {
  if (!formInstance || !selectedForm.value) return;
  if (!formInstance.checkValidity(null, true)) {
    toast.warning("請完成表單必填欄位");
    return;
  }
  submitting.value = true;
  try {
    const response: any = await runtimePost(
      "/submit",
      {
        processDefId: processDefId.value,
        formId: selectedForm.value.formId,
        formVersionNo: selectedForm.value.formVersionNo,
        applicantAccount: applicantAccount.value.trim(),
        formData: formInstance.submission?.data || {},
      },
      { ...tenantHeaders(), "Idempotency-Key": idempotencyKey.value },
    );
    if (!ok(response)) return showError(response, "送出失敗");
    result.value = response.value;
    toast.success("表單已送出");
  } finally {
    submitting.value = false;
  }
};

watch(tenantId, async () => {
  await Promise.all([loadCatalog(), loadInbox(), loadMyRequests()]);
});
watch(selectedForm, renderForm);
onMounted(async () => {
  await loadTenants();
});
onBeforeUnmount(() => void destroyForm());
</script>

<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="mb-1">工作台</h2>
        <div class="text-muted">選擇您可發起的流程並填寫正式表單。</div>
      </div>
      <span class="badge text-bg-light border">發起人：{{ baseStore.userId }}</span>
    </div>

    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <div>
          <strong>我的待辦</strong>
          <span class="badge rounded-pill text-bg-primary ms-2">{{ inbox.length }}</span>
        </div>
        <button type="button" class="btn btn-sm btn-outline-secondary" @click="loadInbox">
          <i class="bi bi-arrow-clockwise"></i> 重新整理
        </button>
      </div>
      <div v-if="inbox.length" class="list-group list-group-flush">
        <NuxtLink
          v-for="task in inbox"
          :key="task.taskId"
          :to="{ path: `/tasks/${task.taskId}`, query: { tenant: tenantId } }"
          class="list-group-item list-group-item-action py-3"
        >
          <div class="d-flex justify-content-between gap-3">
            <div>
              <div class="fw-semibold">{{ task.processName }}・{{ task.taskName }}</div>
              <div class="small text-muted mt-1">
                申請人：{{ task.applicantAccount }}　流程編號：{{ task.businessKey }}
              </div>
            </div>
            <i class="bi bi-chevron-right text-muted align-self-center"></i>
          </div>
        </NuxtLink>
      </div>
      <div v-else class="card-body text-center text-muted py-4">
        <i class="bi bi-check2-circle fs-3 d-block mb-2"></i>
        目前沒有待處理事項
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <div>
          <strong>我的申請</strong>
          <span class="badge rounded-pill text-bg-secondary ms-2">{{ myRequests.length }}</span>
        </div>
        <button type="button" class="btn btn-sm btn-outline-secondary" @click="loadMyRequests">
          <i class="bi bi-arrow-clockwise"></i> 重新整理
        </button>
      </div>
      <div v-if="myRequests.length" class="list-group list-group-flush">
        <NuxtLink
          v-for="request in myRequests.slice(0, 10)"
          :key="request.processInstanceId"
          :to="{ path: `/requests/${request.processInstanceId}`, query: { tenant: tenantId } }"
          class="list-group-item list-group-item-action py-3"
        >
          <div class="d-flex justify-content-between align-items-center gap-3">
            <div>
              <div class="fw-semibold">{{ request.processName }}・{{ request.formName }}</div>
              <div class="small text-muted mt-1">
                申請人：{{ request.applicantAccount }}　目前節點：{{ request.currentTaskNames?.join('、') || '—' }}
              </div>
            </div>
            <span :class="['badge', request.instanceStatus === 'RUNNING' ? 'text-bg-primary' : request.instanceStatus === 'COMPLETED' ? 'text-bg-success' : 'text-bg-secondary']">
              {{ request.instanceStatus }}
            </span>
          </div>
        </NuxtLink>
      </div>
      <div v-else class="card-body text-center text-muted py-4">
        尚無申請紀錄
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-lg-4">
            <label class="form-label">公司</label>
            <select v-model="tenantId" class="form-select">
              <option value="">請選擇公司</option>
              <option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">
                {{ tenant.tenantName }}（{{ tenant.tenantCode }}）
              </option>
            </select>
          </div>
          <div class="col-lg-4">
            <label class="form-label">申請人帳號</label>
            <div class="input-group">
              <input v-model.trim="applicantAccount" class="form-control" @keyup.enter="loadCatalog" />
              <button type="button" class="btn btn-outline-secondary" @click="loadCatalog">套用</button>
            </div>
            <div class="form-text">替他人申請時，系統會檢查有效的代申請授權。</div>
          </div>
          <div class="col-lg-4">
            <label class="form-label">可發起流程</label>
            <select v-model="processDefId" class="form-select" @change="loadStart">
              <option value="">請選擇流程</option>
              <option v-for="process in processes" :key="process.processDefId" :value="process.processDefId">
                {{ process.processName }}（v{{ process.versionNo }}）
              </option>
            </select>
          </div>
        </div>
        <div v-if="!loading && tenantId && applicantAccount && !processes.length" class="alert alert-secondary mt-3 mb-0">
          此申請人目前沒有可發起的流程。
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5 text-muted">
      <span class="spinner-border spinner-border-sm me-2"></span>載入中…
    </div>

    <div v-if="startData" class="card">
      <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <strong>{{ startData.processName }}</strong>
        <select v-if="startData.forms?.length > 1" v-model="selectedForm" class="form-select form-select-sm form-selector">
          <option v-for="item in startData.forms" :key="`${item.formId}:${item.formVersionNo}`" :value="item">
            {{ item.formName }}（v{{ item.formVersionNo }}）
          </option>
        </select>
        <span v-else class="text-muted small">{{ selectedForm?.formName }}</span>
      </div>
      <div class="card-body">
        <div ref="formHost" class="runtime-form"></div>
        <div class="d-flex justify-content-end mt-4">
          <button type="button" class="btn btn-primary" :disabled="submitting || !!result" @click="submit">
            <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
            送出申請
          </button>
        </div>
        <div v-if="result" class="alert alert-success mt-4 mb-0">
          <strong>送出成功</strong>
          <div>流程編號：{{ result.processInstanceId }}</div>
          <div>表單資料編號：{{ result.formDataId }}</div>
          <div>狀態：{{ result.instanceStatus }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.runtime-form { min-height: 160px; }
.form-selector { width: min(100%, 360px); }
</style>
