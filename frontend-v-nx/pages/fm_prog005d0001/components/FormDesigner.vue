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
import { PageConstants } from "../config";

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const selectedVersion = ref<any>(null);
const designerHost = ref<HTMLElement | null>(null);
const designerLoading = ref(false);
let designerInstance: any = null;
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
const destroyDesigner = () => {
  designerSequence += 1;
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
  designerInstance?.destroy?.(true);
  designerHost.value.innerHTML = "";
  try {
    const { Formio } = await import("@formio/js");
    if (sequence !== designerSequence || !designerHost.value) return;
    const schema = parseFormioSchema(selectedVersion.value.schemaContent);
    if (selectedVersion.value.versionStatus === "DRAFT") {
      designerInstance = await Formio.builder(designerHost.value, schema, {
        noDefaultSubmitButton: true,
        alwaysConfirmComponentRemoval: true,
        builder: {
          basic: { title: "基本欄位", weight: 0, default: true },
          advanced: false,
          data: false,
          premium: false,
          layout: { title: "版面配置", weight: 10 },
        },
      });
      designerInstance.on("change", (changedSchema: any) => {
        if (selectedVersion.value?.versionStatus !== "DRAFT") return;
        selectedVersion.value.schemaContent = JSON.stringify(
          changedSchema,
          null,
          2,
        );
        selectedVersion.value.uiSchemaContent = JSON.stringify(
          { engine: "FORMIO", version: 1 },
          null,
          2,
        );
      });
    } else {
      designerInstance = await Formio.createForm(designerHost.value, schema, {
        readOnly: true,
        noAlerts: true,
      });
    }
  } catch (error: any) {
    toast.error(error?.message || "載入 Form.io 設計器失敗");
  } finally {
    if (sequence === designerSequence) designerLoading.value = false;
  }
};
const selectVersion = (version: any) => {
  selectedVersion.value = version;
  void renderDesigner();
};
const syncSchemaFromDesigner = () => {
  if (selectedVersion.value?.versionStatus !== "DRAFT") return true;
  const schema =
    designerInstance?.form ||
    parseFormioSchema(selectedVersion.value.schemaContent);
  selectedVersion.value.schemaContent = JSON.stringify(schema, null, 2);
  selectedVersion.value.uiSchemaContent = JSON.stringify(
    { engine: "FORMIO", version: 1 },
    null,
    2,
  );
  return true;
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
  showLoading();
  try {
    let response = await post("/version/save-draft", {
      oid: selectedVersion.value.oid,
      schemaContent: selectedVersion.value.schemaContent,
      uiSchemaContent: selectedVersion.value.uiSchemaContent,
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
          <div v-if="designerLoading" class="designer-loading text-muted">
            <span class="spinner-border spinner-border-sm me-2"></span>
            載入表單設計器…
          </div>
          <div ref="designerHost" class="formio-designer"></div>
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
