<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
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
const schemaFields = computed(() => {
  try {
    const schema = JSON.parse(selectedVersion.value?.schemaContent || "{}");
    return Object.entries(schema.properties || {}).map(
      ([name, definition]: [string, any]) => ({
        name,
        type: definition?.type || "未指定",
        title: definition?.title || "",
        required: (schema.required || []).includes(name),
      }),
    );
  } catch {
    return [];
  }
});
const apply = (value: any) => {
  form.value = value;
  selectedVersion.value =
    value.versions?.find((item: any) => item.versionStatus === "DRAFT") ||
    value.versions?.[0] ||
    null;
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
const formatJson = (field: "schemaContent" | "uiSchemaContent") => {
  if (!selectedVersion.value) return;
  try {
    selectedVersion.value[field] = JSON.stringify(
      JSON.parse(selectedVersion.value[field]),
      null,
      2,
    );
    toast.success("JSON 格式正確並已重新排版");
  } catch (error: any) {
    toast.warning("JSON 格式錯誤：" + error.message);
  }
};
const save = async () => {
  if (!validateMaster()) return;
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
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="
      props.edit
        ? '維護表單主檔與版本。只有草稿版本可以修改 JSON Schema 與 UI Schema；發布後必須建立新版本。'
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
        JSON Schema 定義資料欄位、型別與必填；UI Schema
        定義畫面排列。發布時後端會重新解析、正規化並計算 SHA-256。
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
          @click="selectedVersion = version"
        >
          v{{ version.versionNo }}・{{ version.versionStatus }}
        </button>
      </div>
      <div v-if="selectedVersion" class="row g-3">
        <div class="col-lg-6">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <label class="form-label mb-0">JSON Schema</label>
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary"
              :disabled="selectedVersion.versionStatus !== 'DRAFT'"
              @click="formatJson('schemaContent')"
            >
              格式化／檢查
            </button>
          </div>
          <textarea
            v-model="selectedVersion.schemaContent"
            :readonly="selectedVersion.versionStatus !== 'DRAFT'"
            class="form-control code-editor"
            spellcheck="false"
          ></textarea>
        </div>
        <div class="col-lg-6">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <label class="form-label mb-0">UI Schema</label>
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary"
              :disabled="selectedVersion.versionStatus !== 'DRAFT'"
              @click="formatJson('uiSchemaContent')"
            >
              格式化／檢查
            </button>
          </div>
          <textarea
            v-model="selectedVersion.uiSchemaContent"
            :readonly="selectedVersion.versionStatus !== 'DRAFT'"
            class="form-control code-editor"
            spellcheck="false"
          ></textarea>
        </div>
        <div class="col-12">
          <div class="card bg-light">
            <div class="card-header">Schema 欄位預覽</div>
            <div class="card-body p-0">
              <table class="table table-sm mb-0">
                <thead>
                  <tr>
                    <th>欄位</th>
                    <th>標題</th>
                    <th>型別</th>
                    <th>必填</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="field in schemaFields" :key="field.name">
                    <td>{{ field.name }}</td>
                    <td>{{ field.title }}</td>
                    <td>{{ field.type }}</td>
                    <td>{{ field.required ? "是" : "否" }}</td>
                  </tr>
                  <tr v-if="!schemaFields.length">
                    <td colspan="4" class="text-muted">
                      尚無欄位或 JSON 格式尚未完成
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
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
.code-editor {
  min-height: 430px;
  font-family: Consolas, "Courier New", monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  white-space: pre;
}
</style>
