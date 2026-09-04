<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";

const props = defineProps<{
  element: any;
  modeler: any;
  tenantId: string;
  disabled?: boolean;
}>();

const actionOptions = ref<any[]>([]);
const loadingActions = ref(false);
const loadingMetadata = ref(false);
const metadata = ref<any>(null);

const businessObject = computed(() => props.element?.businessObject);
const actionCode = computed(() => businessObject.value?.actionCode || "");
const actionVersion = computed(() => Number(businessObject.value?.actionVersion || 0));
const requestMapping = computed(() => businessObject.value?.requestMapping || "{}");
const responseMapping = computed(() => businessObject.value?.responseMapping || "{}");

const responseOk = (response: any) =>
  response.data?.success === import.meta.env.VITE_SUCCESS_FLAG;

const updateProperties = (properties: Record<string, unknown>) => {
  if (props.disabled || !props.modeler || !props.element) return;
  props.modeler.get("modeling").updateProperties(props.element, properties);
};

const loadActions = async () => {
  actionOptions.value = [];
  if (!props.tenantId) return;
  loadingActions.value = true;
  try {
    const response = await getAxiosInstance().post(
      `${import.meta.env.VITE_API_URL}/fm/data-actions/options`,
      {},
      { headers: { "X-FlowMint-Tenant": props.tenantId } },
    );
    if (!responseOk(response)) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "Data Action 選項載入失敗"));
      return;
    }
    actionOptions.value = response.data?.value || [];
  } finally {
    loadingActions.value = false;
  }
};

const loadMetadata = async (selectedActionCode = actionCode.value) => {
  metadata.value = null;
  if (!props.tenantId || !selectedActionCode) return;
  loadingMetadata.value = true;
  try {
    const response = await getAxiosInstance().post(
      `${import.meta.env.VITE_API_URL}/fm/data-actions/${encodeURIComponent(selectedActionCode)}/metadata`,
      {},
      { headers: { "X-FlowMint-Tenant": props.tenantId } },
    );
    if (!responseOk(response)) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "Data Action metadata 載入失敗"));
      return;
    }
    metadata.value = response.data?.value || null;
    if (metadata.value?.versionNo && !actionVersion.value) {
      updateProperties({ actionVersion: Number(metadata.value.versionNo) });
    }
  } finally {
    loadingMetadata.value = false;
  }
};

const changeAction = (event: Event) => {
  const value = (event.target as HTMLSelectElement).value;
  updateProperties({ actionCode: value, actionVersion: undefined });
  void loadMetadata(value);
};

const changeText = (property: string, event: Event) => {
  updateProperties({ [property]: (event.target as HTMLTextAreaElement).value });
};

const validateJson = (label: string, value: string) => {
  try {
    const parsed = JSON.parse(value || "{}");
    if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") throw new Error();
  } catch {
    toast.warning(`${label}必須是 JSON object`);
  }
};

watch(() => props.tenantId, loadActions, { immediate: true });
watch(() => props.element?.id, loadMetadata, { immediate: true });
</script>

<template>
  <div>
    <div class="alert alert-info py-2 small">
      此節點是受控 System Task，只能執行同 Tenant 的已發布 Data Action，不允許 Script、class 或任意 expression。
    </div>
    <div class="mb-3">
      <label class="form-label">節點代碼</label>
      <input :value="element.id" disabled class="form-control" />
    </div>
    <div class="mb-3">
      <label class="form-label">節點名稱</label>
      <input
        :value="businessObject?.name || ''"
        :disabled="disabled"
        class="form-control"
        @input="updateProperties({ name: ($event.target as HTMLInputElement).value })"
      />
    </div>
    <div class="mb-3">
      <label class="form-label">Data Action</label>
      <select :value="actionCode" :disabled="disabled || loadingActions" class="form-select" @change="changeAction">
        <option value="">{{ loadingActions ? "載入中…" : "請選擇已發布 Data Action" }}</option>
        <option v-for="item in actionOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
    </div>
    <div class="row g-2 mb-3">
      <div class="col-12">
        <label class="form-label">固定版本</label>
        <input :value="actionVersion || ''" disabled class="form-control" />
      </div>
    </div>
    <div v-if="metadata" class="small text-muted mb-3">
      類型：<code>{{ metadata.actionType }}</code><br />
      Request keys：{{ (metadata.requestFields || []).join("、") || "無" }}<br />
      Response keys：{{ (metadata.responseKeys || []).join("、") || "無" }}
      <div v-if="metadata.actionType !== 'QUERY'" class="text-danger mt-1">
        此異動類型不會自動重試；失敗後必須由管理員確認外部結果，再決定是否人工重試。
      </div>
    </div>
    <div v-else-if="loadingMetadata" class="small text-muted mb-3">Metadata 載入中…</div>
    <div class="mb-3">
      <label class="form-label">Request Mapping JSON</label>
      <textarea
        :value="requestMapping"
        rows="5"
        :disabled="disabled"
        class="form-control font-monospace"
        placeholder='{"employeeId":"FORM_DATA.applicantId"}'
        @change="changeText('requestMapping', $event); validateJson('Request Mapping', ($event.target as HTMLTextAreaElement).value)"
      ></textarea>
      <div class="form-text">第一版格式：Action request key 對應 <code>FORM_DATA.path</code>、<code>PROCESS_CONTEXT.name</code> 或 <code>CONSTANT:value</code>。</div>
    </div>
    <div class="mb-3">
      <label class="form-label">Response Mapping JSON</label>
      <textarea
        :value="responseMapping"
        rows="5"
        :disabled="disabled"
        class="form-control font-monospace"
        placeholder='{"employee.name":"FORM_DATA.employeeName"}'
        @change="changeText('responseMapping', $event); validateJson('Response Mapping', ($event.target as HTMLTextAreaElement).value)"
      ></textarea>
      <div class="form-text">第一版只允許寫入 <code>FORM_DATA.path</code>。</div>
    </div>
  </div>
</template>
