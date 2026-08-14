<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import type {
  FormDataActionBinding,
  FormDataActionUiSchema,
} from "@/types/formDataAction";

interface OptionItem {
  value: string;
  label: string;
}

interface DataActionMetadata {
  actionCode: string;
  actionName: string;
  actionType: string;
  versionNo: number;
  requestFields: string[];
  responseKeys: string[];
}

interface MappingRow {
  source: string;
  target: string;
}

const props = defineProps<{
  tenantId: string;
  schemaContent: string;
  uiSchemaContent: string;
  readonly?: boolean;
}>();
const emit = defineEmits<{
  (event: "update:uiSchemaContent", value: string): void;
}>();

const actionOptions = ref<OptionItem[]>([]);
const metadata = ref<DataActionMetadata | null>(null);
const loadingOptions = ref(false);
const loadingMetadata = ref(false);
const editingIndex = ref<number | null>(null);
const eventName = ref("");
const actionCode = ref("");
const requestMapping = ref<MappingRow[]>([]);
const responseMapping = ref<MappingRow[]>([]);
const statusTarget = ref("dataActionStatus");
const errorTarget = ref("dataActionError");

const parseUiSchema = (): FormDataActionUiSchema => {
  try {
    const value = JSON.parse(props.uiSchemaContent || "{}");
    if (value?.engine === "FORMIO") return value;
  } catch {
    // The parent page and backend show validation errors when saving.
  }
  return { engine: "FORMIO", version: 1, dataActions: [] };
};

const bindings = computed(() => parseUiSchema().dataActions || []);

const buttonEvents = computed(() => {
  try {
    const schema = JSON.parse(props.schemaContent || "{}");
    const events = new Set<string>();
    collectButtonEvents(schema.components || [], events);
    return [...events].sort();
  } catch {
    return [];
  }
});

const collectButtonEvents = (components: any[], events: Set<string>) => {
  for (const component of components || []) {
    if (
      component?.type === "button" &&
      component?.action === "event" &&
      component?.event
    ) {
      events.add(component.event);
    }
    collectButtonEvents(component?.components || [], events);
    for (const column of Array.isArray(component?.columns)
      ? component.columns
      : []) {
      collectButtonEvents(column?.components || [], events);
    }
    for (const row of Array.isArray(component?.rows) ? component.rows : []) {
      for (const cell of Array.isArray(row) ? row : []) {
        collectButtonEvents(cell?.components || [], events);
      }
    }
  }
};

const apiPost = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(`${import.meta.env.VITE_API_URL}${path}`, body, {
    headers: { "X-FlowMint-Tenant": props.tenantId },
  });

const responseOk = (response: any) =>
  response.data?.success === import.meta.env.VITE_SUCCESS_FLAG;

const loadOptions = async () => {
  actionOptions.value = [];
  metadata.value = null;
  if (!props.tenantId) return;
  loadingOptions.value = true;
  try {
    const response = await apiPost("/fm/data-actions/options");
    if (!responseOk(response)) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取 Data Action 清單失敗"),
      );
      return;
    }
    actionOptions.value = response.data?.value || [];
  } catch (error: unknown) {
    toast.error(error instanceof Error ? error.message : "讀取 Data Action 清單失敗");
  } finally {
    loadingOptions.value = false;
  }
};

const loadMetadata = async (resetMappings = false) => {
  metadata.value = null;
  if (!props.tenantId || !actionCode.value) return;
  loadingMetadata.value = true;
  try {
    const response = await apiPost(
      `/fm/data-actions/${encodeURIComponent(actionCode.value)}/metadata`,
    );
    if (!responseOk(response)) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取 Data Action Metadata 失敗"),
      );
      return;
    }
    metadata.value = response.data.value as DataActionMetadata;
    if (resetMappings) {
      requestMapping.value = (metadata.value.requestFields || []).map((field) => ({
        source: `submission.${field}`,
        target: field,
      }));
      responseMapping.value = (metadata.value.responseKeys || []).map((key) => ({
        source: key,
        target: key,
      }));
    }
  } catch (error: unknown) {
    toast.error(
      error instanceof Error ? error.message : "讀取 Data Action Metadata 失敗",
    );
  } finally {
    loadingMetadata.value = false;
  }
};

const resetEditor = () => {
  editingIndex.value = null;
  eventName.value = buttonEvents.value[0] || "";
  actionCode.value = "";
  metadata.value = null;
  requestMapping.value = [];
  responseMapping.value = [];
  statusTarget.value = "dataActionStatus";
  errorTarget.value = "dataActionError";
};

const objectToRows = (
  value: Record<string, string> | undefined,
  request = false,
): MappingRow[] =>
  Object.entries(value || {}).map(([key, mappedValue]) =>
    request
      ? { source: mappedValue, target: key }
      : { source: key, target: mappedValue },
  );

const editBinding = async (index: number) => {
  const binding = bindings.value[index];
  editingIndex.value = index;
  eventName.value = binding.event;
  actionCode.value = binding.actionCode;
  requestMapping.value = objectToRows(binding.requestMapping, true);
  responseMapping.value = objectToRows(binding.responseMapping);
  statusTarget.value = binding.statusTarget || "";
  errorTarget.value = binding.errorTarget || "";
  await loadMetadata(false);
};

const rowsToRequestMapping = () =>
  Object.fromEntries(
    requestMapping.value
      .filter((row) => row.target.trim() && row.source.trim())
      .map((row) => [row.target.trim(), row.source.trim()]),
  );

const rowsToResponseMapping = () =>
  Object.fromEntries(
    responseMapping.value
      .filter((row) => row.source.trim() && row.target.trim())
      .map((row) => [row.source.trim(), row.target.trim()]),
  );

const saveBinding = () => {
  if (!eventName.value) {
    toast.warning("請先在表單加入 action=event 的 Button，並選擇觸發事件");
    return;
  }
  if (!actionCode.value || !metadata.value) {
    toast.warning("請選擇可用的 Data Action");
    return;
  }
  const uiSchema = parseUiSchema();
  const nextBindings = [...(uiSchema.dataActions || [])];
  const binding: FormDataActionBinding = {
    bindingId:
      editingIndex.value === null
        ? globalThis.crypto?.randomUUID?.() || `binding-${Date.now()}`
        : nextBindings[editingIndex.value].bindingId,
    event: eventName.value,
    actionCode: actionCode.value,
    actionVersion: metadata.value.versionNo,
    requestMapping: rowsToRequestMapping(),
    responseMapping: rowsToResponseMapping(),
    statusTarget: statusTarget.value.trim() || undefined,
    errorTarget: errorTarget.value.trim() || undefined,
  };
  if (editingIndex.value === null) nextBindings.push(binding);
  else nextBindings.splice(editingIndex.value, 1, binding);
  uiSchema.dataActions = nextBindings;
  emit("update:uiSchemaContent", JSON.stringify(uiSchema, null, 2));
  toast.success(editingIndex.value === null ? "Data Action Binding 已新增" : "Data Action Binding 已更新");
  resetEditor();
};

const removeBinding = (index: number) => {
  const uiSchema = parseUiSchema();
  const nextBindings = [...(uiSchema.dataActions || [])];
  nextBindings.splice(index, 1);
  uiSchema.dataActions = nextBindings;
  emit("update:uiSchemaContent", JSON.stringify(uiSchema, null, 2));
  resetEditor();
};

watch(() => props.tenantId, loadOptions, { immediate: true });
watch(buttonEvents, (events) => {
  if (!eventName.value && events.length) eventName.value = events[0];
}, { immediate: true });
</script>

<template>
  <section class="binding-editor border rounded p-3 bg-body-tertiary">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
      <div>
        <h6 class="mb-1"><i class="bi bi-lightning-charge"></i> Data Action Binding</h6>
        <div class="text-muted small">
          選擇目前 Tenant 已發布的 Action，並將 Form.io Event 與 Request／Response 欄位連接。
        </div>
      </div>
      <span class="badge text-bg-secondary">{{ bindings.length }} 個 Binding</span>
    </div>

    <div v-if="bindings.length" class="binding-list mb-3">
      <div
        v-for="(binding, index) in bindings"
        :key="binding.bindingId"
        class="binding-item d-flex flex-wrap justify-content-between align-items-center gap-2"
      >
        <div>
          <strong>{{ binding.actionCode }}</strong>
          <span class="text-muted ms-2">{{ binding.event }}</span>
          <span v-if="binding.actionVersion" class="badge text-bg-light ms-2">
            v{{ binding.actionVersion }}
          </span>
        </div>
        <div v-if="!readonly" class="btn-group btn-group-sm">
          <button type="button" class="btn btn-outline-primary" @click="editBinding(index)">
            <i class="bi bi-pencil"></i> 編輯
          </button>
          <button type="button" class="btn btn-outline-danger" @click="removeBinding(index)">
            <i class="bi bi-trash"></i> 移除
          </button>
        </div>
      </div>
    </div>

    <div v-if="!readonly" class="card border-0 shadow-sm">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-lg-5">
            <label class="form-label">Form.io 觸發事件</label>
            <select v-model="eventName" class="form-select">
              <option value="">請選擇 Button Event</option>
              <option v-for="event in buttonEvents" :key="event" :value="event">
                {{ event }}
              </option>
            </select>
            <div v-if="!buttonEvents.length" class="form-text text-warning">
              請先在設計器加入 Button，將 Action 設為 Event 並填入 Event 名稱。
            </div>
          </div>
          <div class="col-lg-7">
            <label class="form-label">已發布 Data Action</label>
            <select
              v-model="actionCode"
              class="form-select"
              :disabled="loadingOptions"
              @change="loadMetadata(true)"
            >
              <option value="">{{ loadingOptions ? "讀取中…" : "請選擇 Action" }}</option>
              <option v-for="option in actionOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div v-if="loadingMetadata" class="text-muted py-3">
          <span class="spinner-border spinner-border-sm me-2"></span>讀取 Metadata…
        </div>

        <template v-if="metadata">
          <div class="metadata-strip mt-3">
            <span><strong>名稱：</strong>{{ metadata.actionName }}</span>
            <span><strong>類型：</strong>{{ metadata.actionType }}</span>
            <span><strong>版本：</strong>v{{ metadata.versionNo }}</span>
          </div>

          <div class="row g-3 mt-1">
            <div class="col-xl-6">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label mb-0">Request Mapping</label>
                <button type="button" class="btn btn-sm btn-outline-secondary" @click="requestMapping.push({ source: '', target: '' })">
                  <i class="bi bi-plus"></i> 新增
                </button>
              </div>
              <div v-if="!requestMapping.length" class="mapping-empty">此 Action 不需要 Request 欄位</div>
              <div v-for="(row, index) in requestMapping" :key="index" class="mapping-row">
                <input v-model="row.target" class="form-control" placeholder="API 欄位" />
                <i class="bi bi-arrow-left-right"></i>
                <input v-model="row.source" class="form-control" placeholder="submission.field" />
                <button type="button" class="btn btn-outline-danger" @click="requestMapping.splice(index, 1)">
                  <i class="bi bi-x"></i>
                </button>
              </div>
            </div>

            <div class="col-xl-6">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label mb-0">Response Mapping</label>
                <button type="button" class="btn btn-sm btn-outline-secondary" @click="responseMapping.push({ source: '', target: '' })">
                  <i class="bi bi-plus"></i> 新增
                </button>
              </div>
              <div v-if="!responseMapping.length" class="mapping-empty">尚未設定回填欄位</div>
              <div v-for="(row, index) in responseMapping" :key="index" class="mapping-row">
                <input v-model="row.source" class="form-control" placeholder="result.path" />
                <i class="bi bi-arrow-right"></i>
                <input v-model="row.target" class="form-control" placeholder="formField" />
                <button type="button" class="btn btn-outline-danger" @click="responseMapping.splice(index, 1)">
                  <i class="bi bi-x"></i>
                </button>
              </div>
              <div v-if="metadata.responseKeys?.length" class="form-text">
                Result Keys：{{ metadata.responseKeys.join(", ") }}
              </div>
            </div>
          </div>

          <div class="row g-3 mt-1">
            <div class="col-md-6">
              <label class="form-label">狀態回填欄位</label>
              <input v-model="statusTarget" class="form-control" placeholder="dataActionStatus" />
            </div>
            <div class="col-md-6">
              <label class="form-label">錯誤回填欄位</label>
              <input v-model="errorTarget" class="form-control" placeholder="dataActionError" />
            </div>
          </div>
        </template>

        <div class="d-flex gap-2 mt-3">
          <button type="button" class="btn btn-primary" :disabled="!metadata" @click="saveBinding">
            <i class="bi bi-check-circle"></i>
            {{ editingIndex === null ? "新增 Binding" : "更新 Binding" }}
          </button>
          <button v-if="editingIndex !== null" type="button" class="btn btn-outline-secondary" @click="resetEditor">
            取消編輯
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.binding-list {
  display: grid;
  gap: 0.5rem;
}

.binding-item {
  padding: 0.7rem 0.85rem;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.55rem;
  background: var(--bs-body-bg);
}

.metadata-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1.5rem;
  padding: 0.75rem 1rem;
  border-radius: 0.55rem;
  color: var(--bs-primary-text-emphasis);
  background: var(--bs-primary-bg-subtle);
  font-size: 0.9rem;
}

.mapping-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.5rem;
}

.mapping-empty {
  padding: 0.75rem;
  border: 1px dashed var(--bs-border-color);
  border-radius: 0.45rem;
  color: var(--bs-secondary-color);
  text-align: center;
}

@media (max-width: 575.98px) {
  .mapping-row {
    grid-template-columns: 1fr auto;
  }

  .mapping-row > i {
    display: none;
  }
}
</style>
