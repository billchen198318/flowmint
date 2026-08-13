<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";
import Toolbar from "@/components/Toolbar.vue";
import {
  checkInvalid,
  escapeQifuHtmlMsg,
  getAxiosInstance,
  invalidFeedback,
} from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import {
  compileFormCustomJavascript,
  useFormCustomJavascript,
} from "@/composables/useFormCustomJavascript";
import { useFormioDataActionBridge } from "@/composables/useFormioDataActionBridge";
import type { FormDataActionUiSchema } from "@/types/formDataAction";
import { PageConstants } from "../config";
import FormCustomJavascriptEditor from "./FormCustomJavascriptEditor.vue";
import FormDataActionBindingEditor from "./FormDataActionBindingEditor.vue";

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const selectedVersion = ref<any>(null);
const designerHost = ref<HTMLElement | null>(null);
const designerLoading = ref(false);
const designerMode = ref<"design" | "preview" | "javascript">("design");
const { attach: attachDataActionBridge } = useFormioDataActionBridge();
const {
  attach: attachCustomJavascript,
  consoleEntries,
  clearConsole,
} = useFormCustomJavascript();
let designerInstance: any = null;
let detachDataActionBridge: (() => void) | null = null;
let detachCustomJavascript: (() => Promise<void>) | null = null;
let runCustomJavascript: ((lifecycle: any, additions?: any) => Promise<any>) | null = null;
const previewSubmission = ref<Record<string, unknown>>({});
let designerSequence = 0;
const newForm = () => ({
  oid: "",
  tenantId: "",
  formId: "",
  formCode: "",
  formName: "",
  currentVersionNo: 1,
  status: "DRAFT",
  description: "",
  versions: [] as any[],
});
const form = ref<any>(newForm());
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const responseOk = (response: any) =>
  response.data?.success === import.meta.env.VITE_SUCCESS_FLAG;
const showResponse = (response: any) => {
  if (!responseOk(response)) {
    toast.warning(escapeQifuHtmlMsg(response.data?.message || "操作失敗"));
    return false;
  }
  toast.success(response.data.message);
  return true;
};
const emptyFormioSchema = () => ({ display: "form", components: [] as any[] });
const legacyComponent = (key: string, definition: any, required: string[]) => {
  let type = "textfield";
  if (definition?.type === "boolean") type = "checkbox";
  else if (["number", "integer"].includes(definition?.type)) type = "number";
  else if (["date", "date-time"].includes(definition?.format))
    type = "datetime";
  else if (definition?.["x-control"] === "TEXTAREA") type = "textarea";
  return {
    type,
    key,
    label: definition?.title || key,
    input: true,
    validate: { required: required.includes(key) },
  };
};
const parseFormioSchema = (content: string) => {
  try {
    const schema = JSON.parse(content || "{}");
    if (Array.isArray(schema.components)) return schema;
    if (schema.type === "object" && schema.properties) {
      const required = Array.isArray(schema.required) ? schema.required : [];
      return {
        display: "form",
        components: Object.entries(schema.properties).map(([key, definition]) =>
          legacyComponent(key, definition, required),
        ),
      };
    }
  } catch {
    // The backend provides the detailed validation error when saving the draft.
  }
  return emptyFormioSchema();
};
const parseUiSchema = (content: string): FormDataActionUiSchema => {
  try {
    const parsed = JSON.parse(content || "{}");
    if (parsed?.engine === "FORMIO") return parsed;
  } catch {
    // Invalid content is reported by the backend when the draft is saved.
  }
  return { engine: "FORMIO", version: 1 };
};
const normalizedUiSchemaContent = () =>
  JSON.stringify(parseUiSchema(selectedVersion.value?.uiSchemaContent), null, 2);
const destroyDesigner = () => {
  designerSequence += 1;
  detachDataActionBridge?.();
  detachDataActionBridge = null;
  void detachCustomJavascript?.();
  detachCustomJavascript = null;
  runCustomJavascript = null;
  designerInstance?.destroy?.(true);
  designerInstance = null;
  if (designerHost.value) designerHost.value.innerHTML = "";
};
const renderDesigner = async () => {
  const sequence = ++designerSequence;
  designerLoading.value = true;
  await nextTick();
  if (!designerHost.value || !selectedVersion.value) {
    designerLoading.value = false;
    return;
  }
  detachDataActionBridge?.();
  detachDataActionBridge = null;
  await detachCustomJavascript?.();
  detachCustomJavascript = null;
  runCustomJavascript = null;
  if (designerMode.value === "preview" && designerInstance?.submission?.data) {
    previewSubmission.value = structuredClone(designerInstance.submission.data);
  }
  designerInstance?.destroy?.(true);
  designerInstance = null;
  designerHost.value.innerHTML = "";
  if (designerMode.value === "javascript") {
    designerLoading.value = false;
    return;
  }
  try {
    const { Formio } = await import("@formio/js");
    if (sequence !== designerSequence || !designerHost.value) return;
    const schema = parseFormioSchema(selectedVersion.value.schemaContent);
    if (
      selectedVersion.value.versionStatus === "DRAFT" &&
      designerMode.value === "design"
    ) {
      designerInstance = await Formio.builder(designerHost.value, schema, {
        noDefaultSubmitButton: true,
        alwaysConfirmComponentRemoval: true,
        builder: {
          basic: { title: "基本元件", weight: 0, default: true },
          advanced: { title: "進階元件", weight: 10 },
          data: { title: "資料與 API", weight: 20 },
          resource: false,
          premium: false,
          layout: { title: "版面配置", weight: 30 },
        },
      });
      designerInstance.on("change", (changedSchema: any) => {
        if (selectedVersion.value?.versionStatus !== "DRAFT") return;
        selectedVersion.value.schemaContent = JSON.stringify(
          changedSchema,
          null,
          2,
        );
        selectedVersion.value.uiSchemaContent = normalizedUiSchemaContent();
      });
    } else {
      designerInstance = await Formio.createForm(designerHost.value, schema, {
        readOnly: selectedVersion.value.versionStatus !== "DRAFT",
        noAlerts: true,
        noDefaultSubmitButton: true,
      });
      if (designerMode.value === "preview" && Object.keys(previewSubmission.value).length) {
        designerInstance.submission = { data: structuredClone(previewSubmission.value) };
      }
      detachDataActionBridge = attachDataActionBridge(
        designerInstance,
        form.value.tenantId,
        parseUiSchema(selectedVersion.value.uiSchemaContent),
      );
      const scriptRuntime = await attachCustomJavascript({
        scriptContent: selectedVersion.value.customScriptContent,
        form: designerInstance,
        tenantId: form.value.tenantId,
        formId: form.value.formId,
        formCode: form.value.formCode,
        versionNo: selectedVersion.value.versionNo,
        mode: "DESIGNER_PREVIEW",
      });
      detachCustomJavascript = scriptRuntime.detach;
      runCustomJavascript = scriptRuntime.run;
    }
  } catch (error: any) {
    toast.error(error?.message || "載入 Form.io 設計器失敗");
  } finally {
    if (sequence === designerSequence) designerLoading.value = false;
  }
};
const resetPreview = async () => {
  previewSubmission.value = {};
  clearConsole();
  await renderDesigner();
};
const validatePreview = async () => {
  if (!designerInstance) return;
  if (!designerInstance.checkValidity(null, true)) {
    toast.warning("請完成表單必填欄位");
    return;
  }
  try {
    const result = await runCustomJavascript?.("beforeSubmit");
    if (result === false || (result && result.valid === false)) {
      toast.warning(result?.message || "表單送出前檢核未通過");
      return;
    }
    previewSubmission.value = structuredClone(
      designerInstance.submission?.data || {},
    );
    toast.success("試跑驗證通過（未送出資料）");
  } catch (error) {
    toast.error(error instanceof Error ? error.message : "試跑驗證失敗");
  }
};
const syncSchemaFromDesigner = () => {
  if (
    selectedVersion.value?.versionStatus !== "DRAFT" ||
    designerMode.value !== "design"
  ) return true;
  const schema =
    designerInstance?.form ||
    parseFormioSchema(selectedVersion.value.schemaContent);
  selectedVersion.value.schemaContent = JSON.stringify(schema, null, 2);
  selectedVersion.value.uiSchemaContent = normalizedUiSchemaContent();
  return true;
};
const setDesignerMode = (mode: "design" | "preview" | "javascript") => {
  if (mode === designerMode.value) return;
  if (designerMode.value === "design") syncSchemaFromDesigner();
  if (designerMode.value === "preview" && designerInstance?.submission?.data) {
    previewSubmission.value = structuredClone(designerInstance.submission.data);
  }
  designerMode.value = mode;
  void renderDesigner();
};
const selectVersion = (version: any) => {
  selectedVersion.value = version;
  designerMode.value = version?.versionStatus === "DRAFT" ? "design" : "preview";
  void renderDesigner();
};
const apply = (value: any) => {
  form.value = value;
  selectVersion(
    value.versions?.find((item: any) => item.versionStatus === "DRAFT") ||
      value.versions?.[0] ||
      null,
  );
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (!responseOk(response)) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "載入表單失敗"),
      );
      return;
    }
    checkFields.value = {};
    apply(response.data.value);
  } catch (error: any) {
    toast.error(error?.message || "載入表單失敗");
  } finally {
    hideLoading();
  }
};
const clear = () => {
  checkFields.value = {};
  form.value = newForm();
  if (tenants.value.length === 1) form.value.tenantId = tenants.value[0].value;
};
const validateMaster = () => {
  const fields: Record<string, string> = {};
  if (!form.value.tenantId) fields.tenantId = "請選擇 Tenant";
  if (!form.value.formCode) fields.formCode = "請輸入表單代碼";
  else if (!/^[A-Za-z][A-Za-z0-9_-]*$/.test(form.value.formCode))
    fields.formCode =
      "表單代碼須以英文字母開頭，且只能包含英數字、底線或連字號";
  if (!form.value.formName?.trim()) fields.formName = "請輸入表單名稱";
  checkFields.value = fields;
  if (Object.keys(fields).length) {
    toast.warning(Object.values(fields)[0]);
    return false;
  }
  return true;
};
const save = async () => {
  if (!validateMaster()) return;
  if (!syncSchemaFromDesigner()) return;
  showLoading();
  try {
    const draft =
      selectedVersion.value?.versionStatus === "DRAFT"
        ? {
            oid: selectedVersion.value.oid,
            schemaContent: selectedVersion.value.schemaContent,
            uiSchemaContent: selectedVersion.value.uiSchemaContent,
            customScriptContent:
              selectedVersion.value.customScriptContent || "",
          }
        : null;
    let response = await post(props.edit ? "/update" : "/save", form.value);
    checkFields.value = response.data?.checkFields || {};
    if (!showResponse(response)) return;
    if (!props.edit) {
      router.push(
        PageConstants.frontendNamespace + "/edit/" + response.data.value.oid,
      );
      return;
    }
    apply(response.data.value);
    if (draft) {
      response = await post("/version/save-draft", draft);
      if (showResponse(response)) apply(response.data.value);
    }
  } catch (error: any) {
    toast.error(error?.message || "儲存表單失敗");
  } finally {
    hideLoading();
  }
};
const createVersion = async () => {
  showLoading();
  try {
    const response = await post("/version/create", { oid: form.value.oid });
    if (showResponse(response)) apply(response.data.value);
  } finally {
    hideLoading();
  }
};
const publish = async () => {
  if (selectedVersion.value?.versionStatus !== "DRAFT") return;
  if (!syncSchemaFromDesigner()) return;
  try {
    await compileFormCustomJavascript(
      selectedVersion.value.customScriptContent,
    );
  } catch (error) {
    toast.error(
      error instanceof Error ? error.message : "客製 JavaScript 檢查失敗",
    );
    return;
  }
  showLoading();
  try {
    let response = await post("/version/save-draft", {
      oid: selectedVersion.value.oid,
      schemaContent: selectedVersion.value.schemaContent,
      uiSchemaContent: selectedVersion.value.uiSchemaContent,
      customScriptContent: selectedVersion.value.customScriptContent || "",
    });
    if (!responseOk(response)) {
      showResponse(response);
      return;
    }
    response = await post("/version/publish", {
      oid: selectedVersion.value.oid,
    });
    if (showResponse(response)) apply(response.data.value);
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (showResponse(response)) apply(response.data.value);
  } finally {
    hideLoading();
  }
};
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  if (!props.edit && tenants.value.length === 1)
    form.value.tenantId = tenants.value[0].value;
  await load();
});
onBeforeUnmount(destroyDesigner);
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="
      props.edit
        ? '使用 Form.io 拖拉設計器維護表單；草稿可編輯，發布後若要調整必須建立新版本。'
        : '建立表單穩定主檔。儲存後會自動建立第 1 版草稿；表單代碼與 Tenant 建立後不可修改。'
    "
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="props.edit ? load() : clear()"
    @backMethod="router.back()"
    @saveMethod="save"
  />
  <div class="card">
    <div class="card-body">
      <div class="alert alert-info">
        請從 Form.io 工具箱拖拉欄位並設定屬性，不需要自行撰寫
        JSON。發布時後端會重新驗證內容並鎖定版本。
      </div>
      <div class="row g-3">
        <div class="col-md-3">
          <label class="form-label">Tenant</label>
          <select
            v-model="form.tenantId"
            :disabled="props.edit"
            :class="[
              'form-select',
              checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇 Tenant</option>
            <option
              v-for="item in tenants"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("tenantId", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">表單代碼</label>
          <input
            v-model="form.formCode"
            :disabled="props.edit"
            :class="[
              'form-control',
              checkInvalid('formCode', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("formCode", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">表單名稱</label>
          <input
            v-model="form.formName"
            :class="[
              'form-control',
              checkInvalid('formName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("formName", checkFields) }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label>
          <input :value="form.status" disabled class="form-control" />
        </div>
        <div class="col-12">
          <label class="form-label">說明</label>
          <input
            v-model="form.description"
            maxlength="500"
            class="form-control"
          />
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="save">
            <i class="bi bi-save"></i> 儲存
          </button>
          <button
            v-if="!props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="clear"
          >
            <i class="bi bi-eraser"></i> 清除
          </button>
          <button
            v-if="props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="load"
          >
            <i class="bi bi-repeat"></i> 重新載入
          </button>
          <button
            v-if="props.edit && form.status !== 'INACTIVE'"
            type="button"
            class="btn btn-outline-danger"
            @click="confirmFire('確定停用此表單？', deactivate, form.oid)"
          >
            <i class="bi bi-slash-circle"></i> 停用
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="props.edit" class="card mt-4">
    <div class="card-header d-flex justify-content-between align-items-center">
      <span>表單版本</span>
      <button
        v-if="
          !form.versions?.some((item: any) => item.versionStatus === 'DRAFT')
        "
        type="button"
        class="btn btn-sm btn-outline-primary"
        @click="createVersion"
      >
        <i class="bi bi-plus-circle"></i> 建立新版本
      </button>
    </div>
    <div class="card-body">
      <div class="d-flex flex-wrap gap-2 mb-3">
        <button
          v-for="version in form.versions"
          :key="version.oid"
          type="button"
          :class="[
            'btn btn-sm',
            selectedVersion?.oid === version.oid
              ? 'btn-primary'
              : 'btn-outline-secondary',
          ]"
          @click="selectVersion(version)"
        >
          v{{ version.versionNo }}・{{ version.versionStatus }}
        </button>
      </div>
      <div v-if="selectedVersion" class="row g-3">
        <div class="col-12">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div>
              <h5 class="mb-1">Form.io 拖拉式表單設計</h5>
              <div class="text-muted small">
                從左側工具箱將欄位拖到畫布，點選欄位即可設定標題、代碼與驗證規則。
              </div>
            </div>
            <span class="badge text-bg-secondary">Form.io</span>
          </div>
          <div class="btn-group btn-group-sm mb-3" role="group" aria-label="表單模式">
            <button
              v-if="selectedVersion.versionStatus === 'DRAFT'"
              type="button"
              :class="[
                'btn',
                designerMode === 'design' ? 'btn-primary' : 'btn-outline-primary',
              ]"
              @click="setDesignerMode('design')"
            >
              <i class="bi bi-pencil-square"></i> 設計
            </button>
            <button
              type="button"
              :class="[
                'btn',
                designerMode === 'preview' ? 'btn-primary' : 'btn-outline-primary',
              ]"
              @click="setDesignerMode('preview')"
            >
              <i class="bi bi-play-circle"></i> 試跑
            </button>
            <button
              type="button"
              :class="[
                'btn',
                designerMode === 'javascript'
                  ? 'btn-primary'
                  : 'btn-outline-primary',
              ]"
              @click="setDesignerMode('javascript')"
            >
              <i class="bi bi-code-slash"></i> JavaScript
            </button>
          </div>
          <div v-if="designerLoading" class="designer-loading text-muted">
            <span class="spinner-border spinner-border-sm me-2"></span>
            載入表單設計器…
          </div>
          <div
            v-show="designerMode !== 'javascript'"
            ref="designerHost"
            class="formio-designer"
          ></div>
          <div v-if="designerMode === 'preview'" class="d-flex flex-wrap gap-2 mt-3">
            <button type="button" class="btn btn-primary" @click="validatePreview">
              <i class="bi bi-check2-circle"></i> 驗證送出
            </button>
            <button type="button" class="btn btn-outline-secondary" @click="resetPreview">
              <i class="bi bi-arrow-counterclockwise"></i> 重設試跑
            </button>
          </div>
          <FormCustomJavascriptEditor
            v-if="designerMode === 'javascript'"
            :model-value="selectedVersion.customScriptContent || ''"
            :readonly="selectedVersion.versionStatus !== 'DRAFT'"
            @update:model-value="
              selectedVersion.customScriptContent = $event
            "
          />
          <div
            v-if="designerMode !== 'design' && consoleEntries.length"
            class="mt-3 border rounded p-3 bg-dark text-light script-console"
          >
            <div class="d-flex justify-content-between mb-2">
              <strong>試跑 Console</strong>
              <button
                type="button"
                class="btn btn-sm btn-outline-light"
                @click="clearConsole"
              >
                清除
              </button>
            </div>
            <div
              v-for="(entry, index) in consoleEntries"
              :key="index"
              class="small font-monospace"
            >
              {{ entry.occurredAt }} [{{ entry.level }}]
              {{ entry.lifecycle || '' }} {{ entry.values }}
            </div>
          </div>
        </div>
        <div class="col-12">
          <FormDataActionBindingEditor
            :tenant-id="form.tenantId"
            :schema-content="selectedVersion.schemaContent"
            :ui-schema-content="selectedVersion.uiSchemaContent"
            :readonly="
              selectedVersion.versionStatus !== 'DRAFT' ||
              designerMode !== 'design'
            "
            @update:ui-schema-content="
              selectedVersion.uiSchemaContent = $event
            "
          />
        </div>
        <div class="col-12">
          <details class="border rounded p-3 bg-light">
            <summary class="fw-semibold">進階檢視：Form.io 系統資料</summary>
            <div class="row g-3 mt-1">
              <div class="col-lg-6">
                <label class="form-label">Form.io Schema（唯讀）</label>
                <textarea
                  :value="selectedVersion.schemaContent"
                  readonly
                  class="form-control code-preview"
                  spellcheck="false"
                ></textarea>
              </div>
              <div class="col-lg-6">
                <label class="form-label">設計器識別資料（唯讀）</label>
                <textarea
                  :value="selectedVersion.uiSchemaContent"
                  readonly
                  class="form-control code-preview"
                  spellcheck="false"
                ></textarea>
              </div>
            </div>
          </details>
        </div>
        <div class="col-12 d-flex gap-2 align-items-center">
          <button
            v-if="selectedVersion.versionStatus === 'DRAFT'"
            type="button"
            class="btn btn-primary"
            @click="save"
          >
            <i class="bi bi-save"></i> 儲存草稿
          </button>
          <button
            v-if="selectedVersion.versionStatus === 'DRAFT'"
            type="button"
            class="btn btn-success"
            @click="
              confirmFire(
                '發布後此版本不可修改，確定發布？',
                publish,
                selectedVersion.oid,
              )
            "
          >
            <i class="bi bi-cloud-upload"></i> 發布草稿
          </button>
          <small class="text-muted"
            >SHA-256：{{ selectedVersion.contentSha256 }}</small
          >
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.code-preview {
  min-height: 260px;
  font-family: Consolas, "Courier New", monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  white-space: pre;
}

.designer-loading {
  padding: 2rem;
  text-align: center;
}

.formio-designer {
  min-height: 420px;
}

.script-console {
  max-height: 240px;
  overflow: auto;
}

:global(.formio-dialog.component-settings .nav-tabs) {
  height: auto;
  margin-top: 0;
  margin-left: 0;
  overflow: visible;
  flex-wrap: wrap;
}

:global(.formio-dialog.component-settings .nav-tabs .nav-link.active) {
  background-color: var(--bs-body-bg);
  background-image: none;
}

:global(.formio-dialog.component-settings .tab-content) {
  height: auto;
  margin-right: 0;
  margin-left: 0;
  padding: 0;
  overflow: visible;
  background: transparent;
}
</style>
