<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import BpmnModeler from "bpmn-js/lib/Modeler";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
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
const canvas = ref<HTMLElement | null>(null);
const selectedVersion = ref<any>(null);
let modeler: any = null;
const newForm = () => ({
  oid: "",
  tenantId: "",
  processKey: "",
  processName: "",
  category: "",
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
const openVersion = async (version: any) => {
  selectedVersion.value = version;
  await nextTick();
  if (!modeler && canvas.value)
    modeler = new BpmnModeler({ container: canvas.value });
  if (modeler && version?.bpmnXml) {
    await modeler.importXML(version.bpmnXml);
    modeler.get("canvas").zoom("fit-viewport");
  }
};
const apply = async (value: any) => {
  form.value = value;
  const draft = value.versions?.find(
    (item: any) => item.versionStatus === "DRAFT",
  );
  await openVersion(draft || value.versions?.[0]);
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (!responseOk(response)) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "載入流程失敗"),
      );
      return;
    }
    checkFields.value = {};
    await apply(response.data.value);
  } catch (error: any) {
    toast.error(error?.message || "載入流程失敗");
  } finally {
    hideLoading();
  }
};
const clear = () => {
  checkFields.value = {};
  form.value = newForm();
};
const validate = () => {
  const fields: Record<string, string> = {};
  if (!form.value.tenantId) fields.tenantId = "請選擇 Tenant";
  if (!form.value.processKey) fields.processKey = "請輸入流程代碼";
  else if (!/^[A-Za-z][A-Za-z0-9_-]*$/.test(form.value.processKey))
    fields.processKey =
      "流程代碼須以英文字母開頭，且只能包含英數字、底線或連字號";
  if (!form.value.processName?.trim()) fields.processName = "請輸入流程名稱";
  checkFields.value = fields;
  if (Object.keys(fields).length) {
    toast.warning(Object.values(fields)[0]);
    return false;
  }
  return true;
};
const save = async () => {
  if (!validate()) return;
  showLoading();
  try {
    const draftOid =
      selectedVersion.value?.versionStatus === "DRAFT"
        ? selectedVersion.value.oid
        : "";
    const draftXml =
      draftOid && modeler ? (await modeler.saveXML({ format: true })).xml : "";
    let response = await post(props.edit ? "/update" : "/save", form.value);
    checkFields.value = response.data?.checkFields || {};
    if (!showResponse(response)) return;
    if (!props.edit) {
      router.push(
        PageConstants.frontendNamespace + "/edit/" + response.data.value.oid,
      );
      return;
    }
    await apply(response.data.value);
    if (draftOid && draftXml) {
      response = await post("/version/save-draft", {
        oid: draftOid,
        bpmnXml: draftXml,
      });
      if (showResponse(response)) await apply(response.data.value);
    }
  } catch (error: any) {
    toast.error(error?.message || "儲存流程失敗");
  } finally {
    hideLoading();
  }
};
const createVersion = async () => {
  showLoading();
  try {
    const response = await post("/version/create", { oid: form.value.oid });
    if (showResponse(response)) await apply(response.data.value);
  } finally {
    hideLoading();
  }
};
const publish = async () => {
  if (!selectedVersion.value || selectedVersion.value.versionStatus !== "DRAFT")
    return;
  showLoading();
  try {
    const xml = (await modeler.saveXML({ format: true })).xml;
    let response = await post("/version/save-draft", {
      oid: selectedVersion.value.oid,
      bpmnXml: xml,
    });
    if (!responseOk(response)) {
      showResponse(response);
      return;
    }
    response = await post("/version/publish", {
      oid: selectedVersion.value.oid,
    });
    if (showResponse(response)) await apply(response.data.value);
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (showResponse(response)) await apply(response.data.value);
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
onBeforeUnmount(() => modeler?.destroy());
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="
      props.edit
        ? '編輯流程基本資料與 BPMN 版本。流程代碼與 Tenant 建立後固定；只有草稿版本可以修改。發布會先驗證 BPMN 並部署至 Flowable，發布成功後該版本即鎖定，後續異動必須建立新版本。'
        : '建立流程穩定主檔。儲存後系統會自動建立第 1 版 BPMN 草稿，再進入編輯頁使用視覺化設計器調整、驗證及發布。流程代碼建立後不可修改。'
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
        草稿可反覆儲存；「發布草稿」會先保存畫布、由 Flowable 驗證
        XML，再建立正式部署。已發布版本只能檢視，請按「建立新版本」複製最新版後再修改。
      </div>
      <div class="row g-3">
        <div class="col-md-3">
          <label class="form-label">Tenant</label
          ><select
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
          <label class="form-label">流程代碼</label
          ><input
            v-model="form.processKey"
            :disabled="props.edit"
            :class="[
              'form-control',
              checkInvalid('processKey', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("processKey", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">流程名稱</label
          ><input
            v-model="form.processName"
            :class="[
              'form-control',
              checkInvalid('processName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("processName", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">分類</label
          ><input v-model="form.category" class="form-control" />
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label
          ><input :value="form.status" disabled class="form-control" />
        </div>
        <div class="col-md-10">
          <label class="form-label">說明</label
          ><input
            v-model="form.description"
            maxlength="500"
            class="form-control"
          />
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="save">
            <i class="bi bi-save"></i> 儲存</button
          ><button
            v-if="!props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="clear"
          >
            <i class="bi bi-eraser"></i> 清除</button
          ><button
            v-if="props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="load"
          >
            <i class="bi bi-repeat"></i> 重新載入</button
          ><button
            v-if="props.edit && form.status !== 'INACTIVE'"
            type="button"
            class="btn btn-outline-danger"
            @click="confirmFire('確定停用此流程？', deactivate, form.oid)"
          >
            <i class="bi bi-slash-circle"></i> 停用
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="props.edit" class="card mt-4">
    <div class="card-header d-flex justify-content-between align-items-center">
      <span>BPMN 流程版本</span
      ><button
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
          @click="openVersion(version)"
        >
          v{{ version.versionNo }}・{{ version.versionStatus }}
        </button>
      </div>
      <div class="position-relative border rounded">
        <div ref="canvas" class="bpmn-canvas"></div>
        <div
          v-if="selectedVersion?.versionStatus !== 'DRAFT'"
          class="readonly-cover"
        >
          <span class="badge text-bg-secondary">已發布版本：僅供檢視</span>
        </div>
      </div>
      <div v-if="selectedVersion" class="mt-3 d-flex gap-2 align-items-center">
        <button
          v-if="selectedVersion.versionStatus === 'DRAFT'"
          type="button"
          class="btn btn-primary"
          @click="save"
        >
          <i class="bi bi-save"></i> 儲存草稿</button
        ><button
          v-if="selectedVersion.versionStatus === 'DRAFT'"
          type="button"
          class="btn btn-success"
          @click="
            confirmFire(
              '發布後此版本不可再修改，確定發布？',
              publish,
              selectedVersion.oid,
            )
          "
        >
          <i class="bi bi-cloud-upload"></i> 發布草稿</button
        ><small class="text-muted"
          >SHA-256：{{ selectedVersion.bpmnSha256 }}</small
        >
      </div>
    </div>
  </div>
</template>
<style scoped>
.bpmn-canvas {
  height: 620px;
  background: #fff;
}
.readonly-cover {
  position: absolute;
  inset: 0;
  background: rgba(248, 249, 250, 0.08);
  pointer-events: all;
}
.readonly-cover .badge {
  position: absolute;
  right: 1rem;
  top: 1rem;
}
</style>
