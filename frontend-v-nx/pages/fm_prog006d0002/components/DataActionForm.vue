<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import Toolbar from "@/components/Toolbar.vue";
import {
  checkInvalid,
  escapeQifuHtmlMsg,
  getAxiosInstance,
  invalidFeedback,
} from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";

interface OptionItem {
  value: string;
  label: string;
}

interface DataActionStepForm {
  oid: string;
  stepCode: string;
  stepName: string;
  executionOrder: number;
  statementType: string;
  executionMode: string;
  sqlContent: string;
  arrayPath: string;
  resultKey: string;
  resultMode: string;
  expectAffectedRows: number | null;
  continueCondition: string;
  queryTimeoutSeconds: number;
  maxRows: number;
  retryCount: number;
  retryDelayMillis: number;
  status: string;
}

interface DataActionForm {
  oid: string;
  tenantId: string;
  actionId: string;
  actionCode: string;
  actionName: string;
  poolId: string;
  actionType: string;
  requestSchema: string;
  responseMode: string;
  status: string;
  currentVersionNo: number;
  draftVersionNo: number | null;
  draftStatus: string | null;
  lockVersion: number;
  description: string;
  rateLimitPerMinute: number | null;
  steps: DataActionStepForm[];
}

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<OptionItem[]>([]);
const pools = ref<OptionItem[]>([]);
const checkFields = ref<Record<string, string>>({});
const previewRequest = ref("{}");
const previewResult = ref<unknown>(null);

const newStep = (index: number): DataActionStepForm => ({
  oid: "",
  stepCode: "STEP_" + (index + 1),
  stepName: "SQL Step " + (index + 1),
  executionOrder: (index + 1) * 10,
  statementType: "SELECT_LIST",
  executionMode: "ONCE",
  sqlContent: "SELECT 1 AS RESULT_VALUE",
  arrayPath: "",
  resultKey: "step" + (index + 1),
  resultMode: "LIST",
  expectAffectedRows: null,
  continueCondition: "",
  queryTimeoutSeconds: 30,
  maxRows: 1000,
  retryCount: 0,
  retryDelayMillis: 0,
  status: "ACTIVE",
});

const newForm = (): DataActionForm => ({
  oid: "",
  tenantId: "",
  actionId: "",
  actionCode: "",
  actionName: "",
  poolId: "",
  actionType: "QUERY",
  requestSchema: "{}",
  responseMode: "COMPOSITE",
  status: "DRAFT",
  currentVersionNo: 0,
  draftVersionNo: 1,
  draftStatus: "DRAFT",
  lockVersion: 0,
  description: "",
  rateLimitPerMinute: null,
  steps: [newStep(0)],
});

const form = ref<DataActionForm>(newForm());

const post = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

const tenantPost = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + "/FM_PROG006D0001" + path,
    body,
  );

const apply = (value: DataActionForm) => {
  form.value = {
    ...value,
    steps: (value.steps || []).map((step) => ({ ...step })),
  };
  checkFields.value = {};
  previewResult.value = null;
};

const clear = async () => {
  form.value = newForm();
  checkFields.value = {};
  previewRequest.value = "{}";
  previewResult.value = null;
  pools.value = [];
};

const loadPools = async () => {
  pools.value = [];
  form.value.poolId = props.edit ? form.value.poolId : "";
  if (!form.value.tenantId) {
    return;
  }
  const response = await post("/pool-options", {
    tenantId: form.value.tenantId,
  });
  pools.value = response.data?.value || [];
};

const load = async () => {
  if (!props.edit) {
    return;
  }
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "載入 Data Action 失敗"),
      );
      await router.push(PageConstants.frontendNamespace);
      return;
    }
    apply(response.data.value);
    await loadPools();
  } catch (error: unknown) {
    const message =
      error instanceof Error ? error.message : "載入 Data Action 失敗";
    toast.error(escapeQifuHtmlMsg(message));
    await router.push(PageConstants.frontendNamespace);
  } finally {
    hideLoading();
  }
};

const validateJson = (value: string, fieldName: string, label: string) => {
  try {
    const parsed = JSON.parse(value);
    if (parsed === null || Array.isArray(parsed) || typeof parsed !== "object") {
      checkFields.value[fieldName] = label + "必須是 JSON Object";
    }
  } catch {
    checkFields.value[fieldName] = label + "不是有效 JSON";
  }
};

const valid = () => {
  const fields: Record<string, string> = {};
  checkFields.value = fields;
  if (!form.value.tenantId) {
    fields.tenantId = "請選擇 Tenant";
  }
  if (!form.value.actionCode.trim()) {
    fields.actionCode = "請輸入 Action Code";
  }
  if (!form.value.actionName.trim()) {
    fields.actionName = "請輸入 Action 名稱";
  }
  if (!form.value.poolId) {
    fields.poolId = "請選擇 DataSource Pool";
  }
  if (!form.value.actionType) {
    fields.actionType = "請選擇 Action Type";
  }
  validateJson(form.value.requestSchema, "requestSchema", "Request Mapping");
  if (form.value.steps.length === 0) {
    fields.steps = "至少需要一個 SQL Step";
  }
  form.value.steps.forEach((step, index) => {
    if (!step.stepCode.trim() || !step.stepName.trim() || !step.sqlContent.trim()) {
      fields.steps = `第 ${index + 1} 個 SQL Step 資料不完整`;
    }
    if (form.value.actionType === "QUERY" && !step.statementType.startsWith("SELECT")) {
      fields.steps = "QUERY Action 只能使用 SELECT Step";
    }
  });
  const firstMessage = Object.values(fields)[0];
  if (firstMessage) {
    toast.warning(escapeQifuHtmlMsg(firstMessage));
    return false;
  }
  return true;
};

const save = async () => {
  if (!valid()) {
    return;
  }
  showLoading();
  try {
    const response = await post(props.edit ? "/update" : "/save", form.value);
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存 Data Action 失敗"),
      );
      return;
    }
    toast.success(escapeQifuHtmlMsg(response.data?.message || "儲存成功"));
    if (props.edit) {
      apply(response.data.value);
      await loadPools();
    } else {
      await router.push(
        PageConstants.frontendNamespace + "/edit/" + response.data.value.oid,
      );
    }
  } catch (error: unknown) {
    const message =
      error instanceof Error ? error.message : "儲存 Data Action 失敗";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};

const publish = async () => {
  showLoading();
  try {
    const response = await post("/publish", { oid: form.value.oid });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "發布 Data Action 失敗"),
      );
      return;
    }
    apply(response.data.value);
    await loadPools();
    toast.success(escapeQifuHtmlMsg(response.data?.message || "發布成功"));
  } catch (error: unknown) {
    const message =
      error instanceof Error ? error.message : "發布 Data Action 失敗";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};

const preview = async () => {
  if (!form.value.actionId || !form.value.draftVersionNo) {
    toast.warning("請先儲存草稿後再執行 Preview");
    return;
  }
  let request: Record<string, unknown>;
  try {
    request = JSON.parse(previewRequest.value);
  } catch {
    toast.warning("Preview Request 不是有效 JSON");
    return;
  }
  showLoading();
  try {
    const path =
      "/" +
      form.value.actionId +
      "/versions/" +
      form.value.draftVersionNo +
      "/preview";
    const response = await post(path, {
      tenantId: form.value.tenantId,
      request,
      commit: false,
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "Preview 執行失敗"),
      );
      return;
    }
    previewResult.value = response.data.value;
    toast.success("Preview 完成，所有異動已 Rollback");
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "Preview 執行失敗";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};

const addStep = () => {
  form.value.steps.push(newStep(form.value.steps.length));
};

const removeStep = (index: number) => {
  form.value.steps.splice(index, 1);
};

watch(
  () => form.value.tenantId,
  async (current, previous) => {
    if (!props.edit || previous) {
      form.value.poolId = "";
    }
    if (current) {
      try {
        await loadPools();
      } catch (error: unknown) {
        const message =
          error instanceof Error ? error.message : "載入 DataSource Pool 失敗";
        toast.error(escapeQifuHtmlMsg(message));
      }
    }
  },
);

onMounted(async () => {
  try {
    const response = await tenantPost("/tenant-options");
    tenants.value = response.data?.value || [];
    await load();
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "載入選項失敗";
    toast.error(escapeQifuHtmlMsg(message));
  }
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    description="設計版本化 SQL Action；正式 API 只能執行已發布版本。"
    backFlag="Y"
    refreshFlag="Y"
    saveFlag="Y"
    @backMethod="router.back()"
    @refreshMethod="props.edit ? load() : clear()"
    @saveMethod="save"
  />

  <div class="card mb-3">
    <div class="card-header">Data Action</div>
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-4">
          <label for="tenantId" class="form-label">Tenant *</label>
          <select
            id="tenantId"
            v-model="form.tenantId"
            :disabled="props.edit"
            :class="[
              'form-select',
              checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇</option>
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

        <div class="col-md-4">
          <label for="actionCode" class="form-label">Action Code *</label>
          <input
            id="actionCode"
            v-model="form.actionCode"
            :readonly="props.edit"
            maxlength="50"
            :class="[
              'form-control',
              checkInvalid('actionCode', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("actionCode", checkFields) }}
          </div>
        </div>

        <div class="col-md-4">
          <label for="actionName" class="form-label">Action 名稱 *</label>
          <input
            id="actionName"
            v-model="form.actionName"
            maxlength="100"
            :class="[
              'form-control',
              checkInvalid('actionName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("actionName", checkFields) }}
          </div>
        </div>

        <div class="col-md-4">
          <label for="poolId" class="form-label">DataSource Pool *</label>
          <select
            id="poolId"
            v-model="form.poolId"
            :class="[
              'form-select',
              checkInvalid('poolId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇</option>
            <option
              v-for="item in pools"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("poolId", checkFields) }}
          </div>
        </div>

        <div class="col-md-3">
          <label for="actionType" class="form-label">Action Type *</label>
          <select
            id="actionType"
            v-model="form.actionType"
            class="form-select"
          >
            <option value="QUERY">QUERY</option>
            <option value="COMMAND">COMMAND</option>
            <option value="TRANSACTION">TRANSACTION</option>
          </select>
        </div>

        <div class="col-md-2">
          <label class="form-label">已發布版本</label>
          <input
            :value="form.currentVersionNo || '-'"
            class="form-control"
            readonly
          />
        </div>

        <div class="col-md-3">
          <label class="form-label">目前草稿</label>
          <input
            :value="form.draftVersionNo || '尚無草稿'"
            class="form-control"
            readonly
          />
        </div>

        <div class="col-12">
          <label for="requestSchema" class="form-label">
            Request JSON Path Mapping
          </label>
          <textarea
            id="requestSchema"
            v-model="form.requestSchema"
            rows="5"
            spellcheck="false"
            :class="[
              'form-control font-monospace',
              checkInvalid('requestSchema', checkFields) ? 'is-invalid' : '',
            ]"
          ></textarea>
          <div class="form-text">
            Request 範例：{ "amount": "$.formData.amount" }；進階 Step 參數可使用
            { "headerId": "${steps.header.generatedKey}", "itemCode": "${item.itemCode}" }。
          </div>
          <div class="invalid-feedback">
            {{ invalidFeedback("requestSchema", checkFields) }}
          </div>
        </div>

        <div class="col-md-4">
          <label for="rateLimitPerMinute" class="form-label">每分鐘呼叫上限</label>
          <input id="rateLimitPerMinute" v-model.number="form.rateLimitPerMinute"
            type="number" min="1" max="100000" class="form-control" />
          <div class="form-text">留空沿用系統全域門檻。</div>
        </div>

        <div class="col-12">
          <label for="description" class="form-label">說明</label>
          <textarea
            id="description"
            v-model="form.description"
            class="form-control"
            maxlength="500"
          ></textarea>
        </div>
      </div>
    </div>
  </div>

  <div class="card mb-3">
    <div class="card-header d-flex justify-content-between align-items-center">
      <span>SQL Steps</span>
      <button class="btn btn-outline-primary btn-sm" @click="addStep">
        <i class="bi bi-plus-lg"></i>
        新增 Step
      </button>
    </div>
    <div class="card-body">
      <div v-if="checkFields.steps" class="alert alert-danger">
        {{ checkFields.steps }}
      </div>

      <div
        v-for="(step, index) in form.steps"
        :key="step.oid || index"
        class="border rounded p-3 mb-3"
      >
        <div class="d-flex justify-content-between mb-3">
          <strong>Step {{ index + 1 }}</strong>
          <button
            class="btn btn-outline-danger btn-sm"
            @click="removeStep(index)"
          >
            移除
          </button>
        </div>

        <div class="row g-3">
          <div class="col-md-2">
            <label class="form-label">順序 *</label>
            <input
              v-model.number="step.executionOrder"
              type="number"
              class="form-control"
            />
          </div>

          <div class="col-md-3">
            <label class="form-label">Step Code *</label>
            <input
              v-model="step.stepCode"
              maxlength="50"
              class="form-control"
            />
          </div>

          <div class="col-md-3">
            <label class="form-label">Step 名稱 *</label>
            <input
              v-model="step.stepName"
              maxlength="100"
              class="form-control"
            />
          </div>

          <div class="col-md-2">
            <label class="form-label">Statement *</label>
            <select v-model="step.statementType" class="form-select">
              <option value="SELECT_ONE">SELECT_ONE</option>
              <option value="SELECT_LIST">SELECT_LIST</option>
              <option value="INSERT">INSERT</option>
              <option value="UPDATE">UPDATE</option>
              <option value="DELETE">DELETE</option>
            </select>
          </div>

          <div class="col-md-2">
            <label class="form-label">Execution Mode</label>
            <select v-model="step.executionMode" class="form-select">
              <option value="ONCE">ONCE</option>
              <option value="FOR_EACH">FOR_EACH</option>
            </select>
          </div>

          <div v-if="step.executionMode === 'FOR_EACH'" class="col-md-4">
            <label class="form-label">Array Path *</label>
            <input
              v-model="step.arrayPath"
              class="form-control font-monospace"
              placeholder="$.items"
            />
            <div class="form-text">陣列筆數上限使用「最大回傳筆數」設定。</div>
          </div>

          <div class="col-12">
            <label class="form-label">SQL *</label>
            <textarea
              v-model="step.sqlContent"
              rows="8"
              spellcheck="false"
              class="form-control font-monospace"
            ></textarea>
            <div class="form-text">
              僅允許單一 Named Parameter SQL；禁止分號、註解及 ${} 字串替換。
            </div>
          </div>

          <div class="col-md-3">
            <label class="form-label">Result Key *</label>
            <input v-model="step.resultKey" class="form-control" />
          </div>

          <div class="col-md-3">
            <label class="form-label">Result Mode *</label>
            <select v-model="step.resultMode" class="form-select">
              <option value="OBJECT">OBJECT</option>
              <option value="LIST">LIST</option>
              <option value="AFFECTED_ROWS">AFFECTED_ROWS</option>
              <option
                v-if="step.statementType === 'INSERT'"
                value="GENERATED_KEY"
              >GENERATED_KEY</option>
              <option value="NONE">NONE</option>
            </select>
          </div>

          <div class="col-md-5">
            <label class="form-label">Continue Condition</label>
            <input
              v-model="step.continueCondition"
              class="form-control font-monospace"
              placeholder="${steps.validation.valid} == true && ${request.amount} >= 1000"
            />
            <div class="form-text">
              留空代表執行；支援數字／日期／字串／布林／null、==、!=、&gt;、&gt;=、&lt;、&lt;=、&amp;&amp;、||。
            </div>
          </div>

          <div class="col-md-2">
            <label class="form-label">預期異動筆數</label>
            <input
              v-model.number="step.expectAffectedRows"
              type="number"
              class="form-control"
            />
          </div>

          <div class="col-md-2">
            <label class="form-label">Timeout 秒數</label>
            <input
              v-model.number="step.queryTimeoutSeconds"
              type="number"
              min="1"
              max="300"
              class="form-control"
            />
          </div>

          <div class="col-md-2">
            <label class="form-label">最大回傳筆數</label>
            <input
              v-model.number="step.maxRows"
              type="number"
              min="1"
              max="10000"
              class="form-control"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label">Transient 重試次數</label>
            <input v-model.number="step.retryCount" type="number" min="0" max="5"
              class="form-control" />
          </div>
          <div v-if="step.retryCount > 0" class="col-md-2">
            <label class="form-label">重試間隔（ms）</label>
            <input v-model.number="step.retryDelayMillis" type="number" min="0" max="5000"
              class="form-control" />
            <div class="form-text">僅 QUERY 的 SELECT 與暫時性資料庫錯誤可重試。</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-if="props.edit" class="card mb-3">
    <div class="card-header">Preview（預設 Rollback）</div>
    <div class="card-body">
      <label for="previewRequest" class="form-label">測試 JSON Body</label>
      <textarea
        id="previewRequest"
        v-model="previewRequest"
        rows="6"
        spellcheck="false"
        class="form-control font-monospace mb-3"
      ></textarea>
      <button
        class="btn btn-outline-success"
        :disabled="!form.draftVersionNo"
        @click="preview"
      >
        <i class="bi bi-play"></i>
        執行 Preview
      </button>
      <pre v-if="previewResult" class="bg-light border rounded p-3 mt-3">{{
        JSON.stringify(previewResult, null, 2)
      }}</pre>
    </div>
  </div>

  <div class="d-flex gap-2 mb-4">
    <button class="btn btn-primary" @click="save">
      <i class="bi bi-save"></i>
      儲存草稿
    </button>
    <button
      v-if="props.edit && form.draftVersionNo"
      class="btn btn-success"
      @click="
        confirmFire(
          '發布後此版本的 SQL 將鎖定，確定發布？',
          publish,
          form.oid,
        )
      "
    >
      <i class="bi bi-cloud-upload"></i>
      發布版本
    </button>
    <button
      v-if="!props.edit"
      class="btn btn-outline-secondary"
      @click="clear"
    >
      清除
    </button>
    <button
      v-if="props.edit"
      class="btn btn-outline-secondary"
      @click="load"
    >
      重新載入
    </button>
  </div>
</template>
