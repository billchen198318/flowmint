<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
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
const notifications = ref<any[]>([]);
const unreadNotificationCount = ref(0);
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
const uploadSessionId = ref("");
const uploadBatchExpiresDate = ref("");
const attachmentFields = ref<any[]>([]);
const attachmentFiles = ref<Record<string, any[]>>({});
const uploadingField = ref("");
const attachmentDirty = computed(() => Object.values(attachmentFiles.value)
  .some((files: any[]) => files.length > 0) && !result.value);
const activeTenant = computed(() => tenants.value.find(
  (item: any) => item.tenantId === tenantId.value,
));
const runningRequests = computed(() => myRequests.value.filter(
  (item: any) => item.instanceStatus === "RUNNING",
));
const completedRequests = computed(() => myRequests.value.filter(
  (item: any) => item.instanceStatus === "COMPLETED",
));
const statCards = computed(() => [
  { label: "我的待辦", value: inbox.value.length, icon: "bi-inbox", tone: "primary", anchor: "inbox" },
  { label: "未讀通知", value: unreadNotificationCount.value, icon: "bi-bell", tone: "info", anchor: "notifications" },
  { label: "進行中", value: runningRequests.value.length, icon: "bi-hourglass-split", tone: "warning", anchor: "requests" },
  { label: "已完成", value: completedRequests.value.length, icon: "bi-check2-circle", tone: "success", anchor: "requests" },
  { label: "可發起流程", value: processes.value.length, icon: "bi-send-plus", tone: "info", anchor: "start" },
]);
const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat("zh-TW", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
  : "—";
const { attach: attachDataActionBridge } = useFormioDataActionBridge();
const { attach: attachCustomJavascript } = useFormCustomJavascript();
let formInstance: any = null;
let detachDataActionBridge: (() => void) | null = null;
let detachCustomJavascript: (() => Promise<void>) | null = null;
let runCustomJavascript: ((lifecycle: any, additions?: any) => Promise<any>) | null = null;

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const runtimePost = (path: string, body: any = {}, headers: any = {}) =>
  useApi(`/fm/requests${path}`, { method: "POST", body, headers });
const notificationPost = (path: string, body: any = {}) =>
  useApi(`/fm/notifications${path}`, { method: "POST", body, headers: tenantHeaders() });
const tenantHeaders = () => ({ "X-FlowMint-Tenant": tenantId.value });
const attachmentPost = (path: string, body: any) =>
  useApi(`/fm/attachments${path}`, { method: "POST", body, headers: tenantHeaders() });
const showError = (response: any, fallback: string) => {
  toast.warning(response?.message || fallback);
};
const destroyForm = async () => {
  detachDataActionBridge?.();
  detachDataActionBridge = null;
  await detachCustomJavascript?.();
  detachCustomJavascript = null;
  runCustomJavascript = null;
  formInstance?.destroy?.(true);
  formInstance = null;
  attachmentFields.value = [];
  attachmentFiles.value = {};
  uploadSessionId.value = "";
  uploadBatchExpiresDate.value = "";
  if (formHost.value) formHost.value.innerHTML = "";
};
const renderForm = async () => {
  await destroyForm();
  await nextTick();
  if (!formHost.value || !selectedForm.value) return;
  const { Formio } = await import("@formio/js");
  const schema = JSON.parse(selectedForm.value.schemaContent || "{}");
  const collectFileFields = (components: any[] = []): any[] => components.flatMap((item: any) => [
    ...(item.type === "file" ? [item] : []),
    ...collectFileFields(item.components || []),
    ...(item.columns || []).flatMap((column: any) => collectFileFields(column.components || [])),
    ...(item.rows || []).flatMap((row: any[]) => row.flatMap(
      (cell: any) => collectFileFields(cell.components || []),
    )),
  ]);
  attachmentFields.value = collectFileFields(schema.components || []);
  attachmentFiles.value = {};
  uploadSessionId.value = "";
  attachmentFields.value.forEach((field: any) => {
    field.hidden = true;
    attachmentFiles.value[field.key] = [];
  });
  formInstance = await Formio.createForm(formHost.value, schema, {
    noAlerts: true,
    noDefaultSubmitButton: true,
  });
  if (attachmentFields.value.length) await createUploadBatch(false);
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
  runCustomJavascript = script.run;
};
const createUploadBatch = async (expired: boolean) => {
  const response: any = await attachmentPost("/sessions", {
      formId: selectedForm.value.formId,
      formVersionNo: selectedForm.value.formVersionNo,
  });
  if (!ok(response)) {
    showError(response, "無法建立附件上傳批次");
    return false;
  }
  uploadSessionId.value = response.value?.uploadSessionId || "";
  uploadBatchExpiresDate.value = response.value?.expiresDate || "";
  if (expired) {
    attachmentFiles.value = Object.fromEntries(
      attachmentFields.value.map((field: any) => [field.key, []]),
    );
    attachmentFields.value.forEach((field: any) => {
      if (formInstance?.submission?.data) formInstance.submission.data[field.key] = [];
    });
    toast.warning("附件上傳批次已逾期，已建立新批次，請重新上傳附件");
  }
  return true;
};
const uploadAttachment = async (field: any, event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  if (!uploadSessionId.value
    || (uploadBatchExpiresDate.value
      && new Date(uploadBatchExpiresDate.value).getTime() <= Date.now())) {
    if (!await createUploadBatch(true)) return;
  }
  const maximum = parseFileSize(field.fileMaxSize || "8MB");
  if (file.size > maximum) {
    toast.warning(`${field.label || field.key} 單檔不可超過 ${field.fileMaxSize || "8MB"}`);
    input.value = "";
    return;
  }
  const maxFiles = Number(field.maxNumberOfFiles || (field.multiple ? 10 : 1));
  if ((attachmentFiles.value[field.key]?.length || 0) >= maxFiles) {
    toast.warning(`${field.label || field.key} 最多上傳 ${maxFiles} 個附件`);
    input.value = "";
    return;
  }
  const totalMaximum = parseFileSize(field.flowmintMaxTotalSize || "20MB");
  const currentTotal = (attachmentFiles.value[field.key] || [])
    .reduce((sum: number, item: any) => sum + Number(item.fileSize || 0), 0);
  if (currentTotal + file.size > totalMaximum) {
    toast.warning(`${field.label || field.key} 附件總容量不可超過 ${field.flowmintMaxTotalSize || "20MB"}`);
    input.value = "";
    return;
  }
  uploadingField.value = field.key;
  try {
    const body = new FormData();
    body.append("uploadSessionId", uploadSessionId.value);
    body.append("fieldKey", field.key);
    body.append("file", file);
    const response: any = await attachmentPost("/sessions/files", body);
    if (!ok(response)) {
      if (String(response?.message || "").includes("不存在或已過期")) {
        await createUploadBatch(true);
      } else showError(response, "附件上傳失敗");
      return;
    }
    attachmentFiles.value[field.key].push(response.value);
    formInstance.submission.data[field.key] = attachmentFiles.value[field.key]
      .map((item: any) => item.attachmentId);
    toast.success("附件已上傳");
  } finally {
    uploadingField.value = "";
    input.value = "";
  }
};
const parseFileSize = (value: string) => {
  const normalized = String(value).trim().toUpperCase().replaceAll(" ", "");
  if (normalized.endsWith("MB")) return Number.parseInt(normalized) * 1024 * 1024;
  if (normalized.endsWith("KB")) return Number.parseInt(normalized) * 1024;
  return Number.parseInt(normalized) || 8 * 1024 * 1024;
};
const acceptedFileTypes = (field: any) => {
  const allowed = [
    ".pdf", ".jpg", ".jpeg", ".png", ".bmp",
    ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
    ".zip", ".7z", ".rar",
  ];
  const values = (field.fileTypes || []).flatMap((item: any) =>
    String(typeof item === "string" ? item : item.value || "").split(/[, ]+/));
  const safe = values.map((value: string) => value.trim().toLowerCase())
    .filter((value: string) => allowed.includes(
      value.startsWith(".") ? value : `.${value}`,
    ));
  return safe.length ? safe.join(",") : allowed.join(",");
};
const beforeUnload = (event: BeforeUnloadEvent) => {
  if (!attachmentDirty.value) return;
  event.preventDefault();
  event.returnValue = "";
};
const deleteAttachment = async (field: any, attachment: any) => {
  const response: any = await attachmentPost("/sessions/files/delete", {
    uploadSessionId: uploadSessionId.value,
    attachmentId: attachment.attachmentId,
  });
  if (!ok(response)) return showError(response, "附件刪除失敗");
  attachmentFiles.value[field.key] = attachmentFiles.value[field.key]
    .filter((item: any) => item.attachmentId !== attachment.attachmentId);
  formInstance.submission.data[field.key] = attachmentFiles.value[field.key]
    .map((item: any) => item.attachmentId);
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
const loadNotifications = async () => {
  notifications.value = [];
  unreadNotificationCount.value = 0;
  if (!tenantId.value) return;
  const response: any = await notificationPost("/inbox");
  if (!ok(response)) return showError(response, "無法載入通知");
  notifications.value = response.value?.notifications || [];
  unreadNotificationCount.value = response.value?.unreadCount || 0;
};
const markNotificationRead = async (notification: any) => {
  if (notification.readDate) return;
  const response: any = await notificationPost("/read", {
    notificationId: notification.notificationId,
  });
  if (!ok(response)) return showError(response, "無法更新通知");
  await loadNotifications();
};
const markAllNotificationsRead = async () => {
  const response: any = await notificationPost("/read-all");
  if (!ok(response)) return showError(response, "無法更新通知");
  await loadNotifications();
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
  const missingAttachment = attachmentFields.value.find((field: any) =>
    field.validate?.required && !attachmentFiles.value[field.key]?.length);
  if (missingAttachment) {
    toast.warning(`${missingAttachment.label || missingAttachment.key} 為必填附件`);
    return;
  }
  const formData = formInstance.submission?.data || {};
  try {
    const validation = await runCustomJavascript?.("beforeSubmit");
    if (validation === false || (validation && validation.valid === false)) {
      toast.warning(validation?.message || "表單送出前檢核未通過");
      return;
    }
  } catch (error) {
    toast.error(error instanceof Error ? error.message : "表單送出前檢核失敗");
    return;
  }
  const selectedApplicantAccount = String(
    formData.applicantAccount || applicantAccount.value,
  ).trim();
  if (!selectedApplicantAccount) {
    toast.warning("請選擇申請人");
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
        applicantAccount: selectedApplicantAccount,
        formData,
        uploadSessionId: uploadSessionId.value || null,
      },
      { ...tenantHeaders(), "Idempotency-Key": idempotencyKey.value },
    );
    if (!ok(response)) return showError(response, "送出失敗");
    result.value = response.value;
    try {
      await runCustomJavascript?.("afterSubmit", { response: response.value });
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "送出後處理失敗");
    }
    toast.success("表單已送出");
  } finally {
    submitting.value = false;
  }
};

watch(tenantId, async () => {
  await Promise.all([loadCatalog(), loadInbox(), loadMyRequests(), loadNotifications()]);
});
watch(selectedForm, renderForm);
onMounted(async () => {
  window.addEventListener("beforeunload", beforeUnload);
  await loadTenants();
});
onBeforeUnmount(() => {
  window.removeEventListener("beforeunload", beforeUnload);
  void destroyForm();
});
</script>

<template>
  <div class="container-fluid workspace-page">
    <section class="workspace-hero mb-4">
      <div>
        <div class="eyebrow">FLOWMINT WORKSPACE</div>
        <h1 class="h2 mb-2">我的工作台</h1>
        <p class="mb-0 text-secondary">處理待辦、追蹤申請，或發起新的流程。</p>
      </div>
      <div class="hero-context">
        <span class="context-label">目前身分</span>
        <strong>{{ baseStore.userId }}</strong>
        <span v-if="activeTenant" class="context-tenant">{{ activeTenant.tenantName }}</span>
      </div>
    </section>

    <section class="row g-3 mb-4" aria-label="工作統計">
      <div v-for="card in statCards" :key="card.label" class="col-6 col-xl-3">
        <a :href="`#${card.anchor}`" class="stat-card text-decoration-none">
          <span :class="['stat-icon', `tone-${card.tone}`]"><i :class="['bi', card.icon]"></i></span>
          <span><small>{{ card.label }}</small><strong>{{ card.value }}</strong></span>
        </a>
      </div>
    </section>

    <div class="row g-4">
      <div class="col-12 col-xl-7">
        <div id="inbox" class="card workspace-card mb-4">
          <div class="card-header workspace-card-header">
            <div><strong>我的待辦</strong><span class="count-pill ms-2">{{ inbox.length }}</span></div>
            <button type="button" class="btn btn-sm btn-light" aria-label="重新整理待辦" @click="loadInbox"><i class="bi bi-arrow-clockwise"></i></button>
          </div>
          <div v-if="inbox.length" class="list-group list-group-flush">
            <NuxtLink v-for="task in inbox.slice(0, 8)" :key="task.taskId"
              :to="{ path: `/tasks/${task.taskId}`, query: { tenant: tenantId } }"
              class="list-group-item list-group-item-action workspace-list-item">
              <span class="list-icon tone-primary"><i class="bi bi-person-check"></i></span>
              <span class="flex-grow-1 min-width-0">
                <strong class="d-block text-truncate">{{ task.processName }} · {{ task.taskName }}</strong>
                <small class="text-secondary">申請人 {{ task.applicantAccount }} · {{ task.businessKey }}</small>
              </span>
              <span class="text-end d-none d-md-block"><small class="text-secondary">{{ formatDate(task.createdDate) }}</small><i class="bi bi-chevron-right ms-3"></i></span>
            </NuxtLink>
          </div>
          <div v-else class="empty-state"><i class="bi bi-check2-circle"></i><strong>待辦已清空</strong><span>目前沒有需要你處理的工作。</span></div>
        </div>

        <div id="notifications" class="card workspace-card mb-4">
          <div class="card-header workspace-card-header">
            <div><strong>通知中心</strong><span class="count-pill ms-2">{{ unreadNotificationCount }} 未讀</span></div>
            <div class="d-flex gap-2">
              <button v-if="unreadNotificationCount" type="button" class="btn btn-sm btn-outline-secondary" @click="markAllNotificationsRead">全部已讀</button>
              <button type="button" class="btn btn-sm btn-light" aria-label="重新整理通知" @click="loadNotifications"><i class="bi bi-arrow-clockwise"></i></button>
            </div>
          </div>
          <div v-if="notifications.length" class="list-group list-group-flush">
            <button v-for="notification in notifications.slice(0, 10)" :key="notification.notificationId"
              type="button" class="list-group-item list-group-item-action workspace-list-item text-start"
              @click="markNotificationRead(notification)">
              <span :class="['list-icon', notification.readDate ? 'tone-muted' : 'tone-info']"><i class="bi bi-bell"></i></span>
              <span class="flex-grow-1 min-width-0">
                <strong class="d-block text-truncate">{{ notification.subject }}</strong>
                <small class="text-secondary">{{ notification.contentText }}</small>
              </span>
              <span class="text-end d-none d-md-block"><small class="text-secondary">{{ formatDate(notification.createdDate) }}</small><span v-if="!notification.readDate" class="unread-dot ms-3"></span></span>
            </button>
          </div>
          <div v-else class="empty-state"><i class="bi bi-bell-slash"></i><strong>目前沒有通知</strong><span>流程指派與狀態通知會顯示在這裡。</span></div>
        </div>

        <div id="requests" class="card workspace-card mb-4">
          <div class="card-header workspace-card-header">
            <div><strong>我的申請</strong><span class="count-pill ms-2">{{ myRequests.length }}</span></div>
            <button type="button" class="btn btn-sm btn-light" aria-label="重新整理申請" @click="loadMyRequests"><i class="bi bi-arrow-clockwise"></i></button>
          </div>
          <div v-if="myRequests.length" class="list-group list-group-flush">
            <NuxtLink v-for="request in myRequests.slice(0, 10)" :key="request.processInstanceId"
              :to="{ path: `/requests/${request.processInstanceId}`, query: { tenant: tenantId } }"
              class="list-group-item list-group-item-action workspace-list-item">
              <span class="list-icon tone-warning"><i class="bi bi-file-earmark-text"></i></span>
              <span class="flex-grow-1 min-width-0">
                <strong class="d-block text-truncate">{{ request.processName }} · {{ request.formName }}</strong>
                <small class="text-secondary">{{ request.businessKey }} · {{ request.currentTaskNames?.join('、') || '流程已結束' }}</small>
              </span>
              <span :class="['status-pill', `status-${request.instanceStatus?.toLowerCase()}`]">{{ request.instanceStatus }}</span>
            </NuxtLink>
          </div>
          <div v-else class="empty-state"><i class="bi bi-journal-text"></i><strong>尚無申請紀錄</strong><span>從右側選擇流程開始第一筆申請。</span></div>
        </div>
      </div>

      <div class="col-12 col-xl-5">
        <div id="start" class="card workspace-card start-card">
          <div class="card-header workspace-card-header"><div><strong>發起新申請</strong><div class="small text-secondary mt-1">系統只顯示你實際有權限發起的流程</div></div></div>
          <div class="card-body p-4">
            <div class="mb-3"><label class="form-label">公司</label><select v-model="tenantId" class="form-select"><option value="">請選擇公司</option><option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">{{ tenant.tenantName }}（{{ tenant.tenantCode }}）</option></select></div>
            <div class="mb-3"><label class="form-label">申請人帳號</label><div class="input-group"><input v-model.trim="applicantAccount" class="form-control" @keyup.enter="loadCatalog" /><button type="button" class="btn btn-outline-primary" @click="loadCatalog">套用</button></div><div class="form-text">代申請時，系統會重新驗證有效授權。</div></div>
            <div><label class="form-label">流程</label><select v-model="processDefId" class="form-select" @change="loadStart"><option value="">請選擇流程</option><option v-for="process in processes" :key="process.processDefId" :value="process.processDefId">{{ process.processName }}（v{{ process.versionNo }}）</option></select></div>
            <div v-if="!loading && tenantId && applicantAccount && !processes.length" class="empty-inline mt-3"><i class="bi bi-info-circle"></i>目前沒有可發起的流程。</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5 text-secondary"><span class="spinner-border spinner-border-sm me-2"></span>載入中</div>

    <div v-if="startData" class="card workspace-card mt-4">
      <div class="card-header workspace-card-header"><strong>{{ startData.processName }}</strong><select v-if="startData.forms?.length > 1" v-model="selectedForm" class="form-select form-select-sm form-selector"><option v-for="item in startData.forms" :key="`${item.formId}:${item.formVersionNo}`" :value="item">{{ item.formName }}（v{{ item.formVersionNo }}）</option></select><span v-else class="text-secondary small">{{ selectedForm?.formName }}</span></div>
      <div class="card-body p-4"><div ref="formHost" class="runtime-form"></div>
        <div v-if="attachmentFields.length" class="mt-4">
          <div v-for="field in attachmentFields" :key="field.key" class="border rounded p-3 mb-3">
            <label class="form-label fw-semibold">
              {{ field.label || field.key }}
              <span v-if="field.validate?.required" class="text-danger">*</span>
            </label>
            <input class="form-control" type="file" :accept="acceptedFileTypes(field)"
              :disabled="!uploadSessionId || uploadingField === field.key || !!result"
              @change="uploadAttachment(field, $event)">
            <div class="form-text">
              格式 {{ acceptedFileTypes(field) }}；單檔上限 {{ field.fileMaxSize || '8MB' }}；
              最多 {{ field.maxNumberOfFiles || (field.multiple ? 10 : 1) }} 個。
              總容量上限 {{ field.flowmintMaxTotalSize || '20MB' }}。
            </div>
            <ul v-if="attachmentFiles[field.key]?.length" class="list-group mt-2">
              <li v-for="file in attachmentFiles[field.key]" :key="file.attachmentId"
                class="list-group-item d-flex justify-content-between align-items-center">
                <span>{{ file.fileName }}（{{ file.fileSize }} bytes）</span>
                <button type="button" class="btn btn-sm btn-outline-danger"
                  :disabled="!!result" @click="deleteAttachment(field, file)">刪除</button>
              </li>
            </ul>
          </div>
        </div>
        <div class="d-flex justify-content-end mt-4"><button type="button" class="btn btn-primary px-4" :disabled="submitting || !!result" @click="submit"><span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>送出申請</button></div><div v-if="result" class="alert alert-success mt-4 mb-0"><strong>申請已送出</strong><div class="mt-2">流程編號：{{ result.processInstanceId }}</div><div>表單資料編號：{{ result.formDataId }}</div><div>狀態：{{ result.instanceStatus }}</div></div></div>
    </div>
  </div>
</template>

<style scoped>
.workspace-page { max-width: 1500px; padding-bottom: 3rem; color: #172033; }
.workspace-hero { display: flex; justify-content: space-between; align-items: center; gap: 2rem; padding: 1.5rem 1.75rem; border: 1px solid #e3e9f2; border-radius: 1.25rem; background: linear-gradient(135deg, #fff 0%, #f4f8ff 58%, #edf7f5 100%); }
.eyebrow { margin-bottom: .45rem; color: #4263eb; font-size: .72rem; font-weight: 800; letter-spacing: .14em; }
.hero-context { display: grid; min-width: 210px; padding: .85rem 1rem; border-radius: .9rem; background: rgba(255,255,255,.82); box-shadow: 0 8px 24px rgba(31,45,61,.07); }
.context-label { color: #788397; font-size: .72rem; }.context-tenant { color: #526076; font-size: .82rem; }
.stat-card { display: flex; align-items: center; gap: 1rem; height: 100%; padding: 1.15rem; border: 1px solid #e5eaf1; border-radius: 1rem; background: #fff; color: inherit; box-shadow: 0 8px 26px rgba(35,49,72,.055); transition: transform .18s ease, box-shadow .18s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(35,49,72,.1); }.stat-card small,.stat-card strong { display:block; }.stat-card small { color:#778296; }.stat-card strong { font-size:1.7rem; line-height:1.15; }
.stat-icon,.list-icon { display:grid; place-items:center; flex:0 0 auto; border-radius:.8rem; }.stat-icon { width:46px; height:46px; font-size:1.2rem; }.list-icon { width:38px; height:38px; }
.tone-primary { color:#3451b2; background:#eaf0ff; }.tone-warning { color:#9a6700; background:#fff3d6; }.tone-success { color:#18794e; background:#e9f9ef; }.tone-info { color:#087e8b; background:#e4f7f8; }
.tone-muted { color:#778296; background:#eef1f5; }.unread-dot { display:inline-block; width:8px; height:8px; border-radius:50%; background:#087e8b; }
.workspace-card { overflow:hidden; border:1px solid #e5eaf1; border-radius:1rem; box-shadow:0 8px 28px rgba(35,49,72,.055); }.workspace-card-header { display:flex; justify-content:space-between; align-items:center; min-height:64px; padding:.9rem 1.15rem; border-bottom:1px solid #edf0f5; background:#fff; }.count-pill { padding:.15rem .5rem; border-radius:999px; background:#eef2f8; color:#536178; font-size:.75rem; }
.workspace-list-item { display:flex; align-items:center; gap:.9rem; padding:1rem 1.15rem; border-color:#edf0f5; }.min-width-0 { min-width:0; }.status-pill { flex:0 0 auto; padding:.28rem .55rem; border-radius:999px; font-size:.72rem; font-weight:700; background:#eef1f5; color:#5c6677; }.status-running { background:#eaf0ff; color:#3451b2; }.status-completed { background:#e9f9ef; color:#18794e; }.status-rejected { background:#fff0f0; color:#c52f2f; }
.empty-state { display:flex; flex-direction:column; align-items:center; padding:2.5rem 1rem; color:#7a8699; }.empty-state i { margin-bottom:.5rem; font-size:2rem; }.empty-state strong { color:#4e5b70; }.empty-state span { margin-top:.25rem; font-size:.85rem; }.empty-inline { padding:.8rem; border-radius:.7rem; background:#f5f7fa; color:#68758a; font-size:.88rem; }.start-card { position:sticky; top:1rem; }.runtime-form { min-height:160px; }.form-selector { width:min(100%,360px); }
@media (max-width: 767.98px) { .workspace-hero { align-items:flex-start; flex-direction:column; }.hero-context { width:100%; }.stat-card { padding:.9rem; }.stat-card strong { font-size:1.4rem; } }
</style>
