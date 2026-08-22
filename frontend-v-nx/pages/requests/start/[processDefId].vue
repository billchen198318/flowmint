<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";
import { useBaseStore } from "@/store/baseStore";
import RuntimeAttachmentFields from "@/components/flowmint/start/RuntimeAttachmentFields.vue";
import StartActionBar from "@/components/flowmint/start/StartActionBar.vue";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const baseStore = useBaseStore();
const startData = ref<any>(null);
const selectedForm = ref<any>(null);
const tenantId = ref("");
const applicantAccount = ref(baseStore.userId);
const processDefId = ref("");
const loading = ref(false);
const loadError = ref("");
const result = ref<any>(null);
const idempotencyKey = ref("");
let runtimeFormApi: ReturnType<typeof useRuntimeForm>;

const {
  attachmentFields,
  attachmentFiles,
  uploadSessionId,
  uploadingField,
  resetAttachments,
  prepareAttachmentFields,
  createUploadBatch,
  acceptedFileTypes,
  uploadAttachment,
  deleteAttachment,
} = useRuntimeAttachments({
  tenantId,
  selectedForm,
  getFormInstance: () => runtimeFormApi?.getFormInstance(),
});
runtimeFormApi = useRuntimeForm({
  tenantId,
  applicantAccount,
  selectedForm,
  prepareAttachments: prepareAttachmentFields,
  createUploadBatch,
  resetAttachments,
  hasAttachmentFields: () => attachmentFields.value.length > 0,
});
const {
  formHost,
  formDirty,
  renderForm,
  destroyForm,
} = runtimeFormApi;
const hasUnsavedChanges = computed(() => !result.value && (
  formDirty.value || Object.values(attachmentFiles.value)
    .some((files: any[]) => files.length > 0)
));

const { submitting, submit } = useRuntimeSubmit({
  tenantId,
  processDefId,
  applicantAccount,
  selectedForm,
  idempotencyKey,
  uploadSessionId,
  attachmentFields,
  attachmentFiles,
  result,
  formDirty,
  getFormInstance: runtimeFormApi.getFormInstance,
  getScriptRunner: runtimeFormApi.getScriptRunner,
});

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const runtimePost = (path: string, body: any = {}, headers: any = {}) =>
  useApi(`/fm/requests${path}`, { method: "POST", body, headers });
const tenantHeaders = () => ({ "X-FlowMint-Tenant": tenantId.value });
const showError = (response: any, fallback: string) => {
  toast.warning(response?.message || fallback);
};
const beforeUnload = (event: BeforeUnloadEvent) => {
  if (!hasUnsavedChanges.value) return;
  event.preventDefault();
  event.returnValue = "";
};
const loadStart = async () => {
  startData.value = null;
  selectedForm.value = null;
  result.value = null;
  idempotencyKey.value = "";
  loadError.value = "";
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
    if (!ok(response)) {
      loadError.value = response?.message || "無法載入起單表單";
      return showError(response, loadError.value);
    }
    startData.value = response.value;
    selectedForm.value = response.value?.forms?.[0] || null;
    idempotencyKey.value = crypto.randomUUID();
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : "無法載入起單表單";
  } finally {
    loading.value = false;
  }
};
watch(selectedForm, renderForm);
onMounted(async () => {
  window.addEventListener("beforeunload", beforeUnload);
  tenantId.value = String(route.query.tenant || "");
  applicantAccount.value = String(route.query.applicant || baseStore.userId).trim();
  processDefId.value = String(route.params.processDefId || "");
  if (!tenantId.value || !processDefId.value) {
    toast.warning("發起資訊不完整，請重新選擇申請流程");
    await router.replace("/requests/start");
    return;
  }
  await loadStart();
});
onBeforeUnmount(() => {
  window.removeEventListener("beforeunload", beforeUnload);
  void destroyForm();
});
onBeforeRouteLeave(() => {
  if (!hasUnsavedChanges.value || typeof window === "undefined") return true;
  return window.confirm("申請內容尚未送出，確定要離開這個頁面嗎？");
});
</script>

<template>
  <div class="container-fluid request-start-page">
    <nav aria-label="麵包屑" class="mb-3">
      <NuxtLink to="/workspace" class="text-decoration-none">工作台</NuxtLink>
      <i class="bi bi-chevron-right mx-2 small"></i>
      <NuxtLink :to="{ path: '/requests/start', query: { tenant: tenantId } }" class="text-decoration-none">申請中心</NuxtLink>
      <i class="bi bi-chevron-right mx-2 small"></i><span>填寫申請</span>
    </nav>

    <header class="request-header mb-4">
      <div>
        <div class="eyebrow">NEW REQUEST</div>
        <h1 class="h3 mb-2">{{ startData?.processName || '載入申請表單' }}</h1>
        <p class="mb-0 text-secondary">公司：{{ tenantId }} · 申請人：{{ applicantAccount }}</p>
      </div>
      <NuxtLink :to="{ path: '/requests/start', query: { tenant: tenantId } }" class="btn btn-outline-secondary">
        <i class="bi bi-arrow-left me-1"></i>重新選擇
      </NuxtLink>
    </header>

    <div v-if="loading" class="card request-card text-center py-5 text-secondary">
      <div><span class="spinner-border spinner-border-sm me-2"></span>載入申請表單</div>
    </div>
    <div v-else-if="startData" class="card request-card">
      <div class="card-header request-card-header">
        <div><strong>{{ selectedForm?.formName }}</strong><div class="small text-secondary mt-1">請填寫下列欄位後送出申請</div></div>
        <select v-if="startData.forms?.length > 1" v-model="selectedForm" class="form-select form-select-sm form-selector"><option v-for="item in startData.forms" :key="`${item.formId}:${item.formVersionNo}`" :value="item">{{ item.formName }}（v{{ item.formVersionNo }}）</option></select>
      </div>
      <div class="card-body request-body">
        <div ref="formHost" class="runtime-form"></div>
        <RuntimeAttachmentFields
          :fields="attachmentFields"
          :files="attachmentFiles"
          :upload-session-id="uploadSessionId"
          :uploading-field="uploadingField"
          :disabled="!!result"
          :accepted-file-types="acceptedFileTypes"
          @upload="uploadAttachment"
          @delete="deleteAttachment"
        />
        <div v-if="result" class="alert alert-success mt-4 mb-0"><strong>申請已送出</strong><div class="mt-2">{{ result.documentNumber || result.processInstanceId }}</div><NuxtLink :to="{ path: `/requests/${result.processInstanceId}`, query: { tenant: tenantId } }" class="btn btn-sm btn-success mt-3">查看申請內容</NuxtLink></div>
      </div>
      <StartActionBar v-if="!result" :cancel-to="{ path: '/requests/start', query: { tenant: tenantId } }" :submitting="submitting" @submit="submit" />
    </div>
    <div v-else class="error-state">
      <i class="bi bi-exclamation-triangle"></i>
      <h2 class="h5">無法載入申請表單</h2>
      <p class="text-secondary">{{ loadError || "流程可能已停用、你沒有發起權限，或表單配置不完整。" }}</p>
      <button type="button" class="btn btn-outline-primary" @click="loadStart">重新載入</button>
    </div>
  </div>

</template>

<style scoped>
.request-start-page{max-width:1200px;padding-bottom:5rem;color:#172033}.request-header{display:flex;justify-content:space-between;align-items:center;gap:1rem;padding:1.4rem 1.6rem;border:1px solid #e3e9f2;border-radius:1.1rem;background:linear-gradient(135deg,#fff 0%,#f4f8ff 100%)}.request-card{overflow:hidden;border:1px solid #e1e7ef;border-radius:1.1rem;box-shadow:0 10px 30px rgba(35,49,72,.07)}.request-card-header{display:flex;justify-content:space-between;align-items:center;min-height:72px;padding:1rem 1.4rem;background:#fff;border-bottom:1px solid #edf0f5}.request-body{padding:1.5rem 1.75rem}.eyebrow{margin-bottom:.4rem;color:#4263eb;font-size:.72rem;font-weight:800;letter-spacing:.14em}.error-state{text-align:center;padding:4rem 1rem;border:1px solid #f0d7a6;border-radius:1rem;background:#fffaf0}.error-state>i{display:block;margin-bottom:1rem;color:#b7791f;font-size:2.4rem}@media(max-width:767.98px){.request-header{align-items:flex-start;flex-direction:column}.request-body{padding:1.1rem}}
</style>
