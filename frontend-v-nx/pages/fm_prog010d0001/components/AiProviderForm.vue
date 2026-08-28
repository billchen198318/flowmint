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

interface AiProviderForm {
  oid: string;
  tenantId: string;
  providerCode: string;
  providerType: string;
  displayName: string;
  baseUrl: string;
  modelId: string;
  apiKey: string;
  maskedApiKey: string;
  temperature: number;
  maxOutputTokens: number;
  timeoutSeconds: number;
  defaultFlag: string;
  status: string;
  lastTestStatus: string;
  lastTestDate: string;
  configVersion: number;
  lockVersion: number;
}

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const tenants = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const defaults: Record<string, { baseUrl: string; modelId: string }> = {
  OPENAI: { baseUrl: "https://api.openai.com/v1", modelId: "gpt-5-mini" },
  GEMINI: { baseUrl: "https://generativelanguage.googleapis.com", modelId: "gemini-2.5-flash" },
  GROQ: { baseUrl: "https://api.groq.com/openai/v1", modelId: "llama-3.3-70b-versatile" },
  OPENROUTER: { baseUrl: "https://openrouter.ai/api/v1", modelId: "openai/gpt-5-mini" },
};
const newForm = (): AiProviderForm => ({
  oid: "",
  tenantId: "",
  providerCode: "",
  providerType: "OPENAI",
  displayName: "",
  baseUrl: defaults.OPENAI.baseUrl,
  modelId: defaults.OPENAI.modelId,
  apiKey: "",
  maskedApiKey: "",
  temperature: 0.2,
  maxOutputTokens: 2000,
  timeoutSeconds: 45,
  defaultFlag: "N",
  status: "ACTIVE",
  lastTestStatus: "",
  lastTestDate: "",
  configVersion: 1,
  lockVersion: 0,
});
const form = ref<AiProviderForm>(newForm());
const post = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const apply = (value: any) => {
  form.value = { ...newForm(), ...value, apiKey: "" };
  checkFields.value = {};
};
const clear = () => {
  form.value = newForm();
  checkFields.value = {};
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "讀取 AI Provider 失敗。"));
      await router.push(PageConstants.frontendNamespace);
      return;
    }
    apply(response.data.value);
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "讀取 AI Provider 失敗。",
    ));
    await router.push(PageConstants.frontendNamespace);
  } finally {
    hideLoading();
  }
};
const save = async () => {
  showLoading();
  try {
    const response = await post(props.edit ? "/update" : "/save", form.value);
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "儲存 AI Provider 失敗。"));
      return;
    }
    toast.success(escapeQifuHtmlMsg(response.data?.message || "儲存成功"));
    if (props.edit) apply(response.data.value);
    else await router.push(PageConstants.frontendNamespace);
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "儲存 AI Provider 失敗。",
    ));
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "停用 AI Provider 失敗。"));
      return;
    }
    apply(response.data.value);
    toast.success(escapeQifuHtmlMsg(response.data?.message || "停用成功"));
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "停用 AI Provider 失敗。",
    ));
  } finally {
    hideLoading();
  }
};
const testConnection = async () => {
  showLoading();
  try {
    const response = await post("/test-connection", { oid: form.value.oid });
    if (response.data?.value) apply(response.data.value);
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "AI Provider 連線測試失敗。"),
      );
      return;
    }
    toast.success(
      escapeQifuHtmlMsg(response.data?.message || "AI Provider 連線測試成功。"),
    );
  } catch (error: unknown) {
    toast.error(
      escapeQifuHtmlMsg(
        error instanceof Error ? error.message : "AI Provider 連線測試失敗。",
      ),
    );
  } finally {
    hideLoading();
  }
};

watch(() => form.value.providerType, (type, oldType) => {
  if (!props.edit && oldType && defaults[type]) {
    form.value.baseUrl = defaults[type].baseUrl;
    form.value.modelId = defaults[type].modelId;
  }
});
onMounted(async () => {
  try {
    tenants.value = (await post("/tenant-options")).data?.value || [];
    await load();
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "載入 AI Provider 表單失敗。",
    ));
  }
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    description="API Key 使用加密方式保存，載入頁面時只顯示遮罩。"
    backFlag="Y"
    refreshFlag="Y"
    saveFlag="Y"
    @backMethod="router.back()"
    @refreshMethod="props.edit ? load() : clear()"
    @saveMethod="save"
  />
  <div class="card">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-4">
          <label for="tenantId" class="form-label">Tenant *</label>
          <select id="tenantId" v-model="form.tenantId" :disabled="props.edit"
            :class="['form-select', checkInvalid('tenantId', checkFields) ? 'is-invalid' : '']">
            <option value="">請選擇</option>
            <option v-for="item in tenants" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <div class="invalid-feedback">{{ invalidFeedback("tenantId", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="providerCode" class="form-label">Provider 代碼 *</label>
          <input id="providerCode" v-model="form.providerCode" :readonly="props.edit" maxlength="50"
            :class="['form-control', checkInvalid('providerCode', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("providerCode", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="displayName" class="form-label">顯示名稱 *</label>
          <input id="displayName" v-model="form.displayName" maxlength="100"
            :class="['form-control', checkInvalid('displayName', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("displayName", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="providerType" class="form-label">Provider 類型 *</label>
          <select id="providerType" v-model="form.providerType" class="form-select">
            <option value="OPENAI">OpenAI</option>
            <option value="GEMINI">Gemini</option>
            <option value="GROQ">Groq</option>
            <option value="OPENROUTER">OpenRouter</option>
          </select>
        </div>
        <div class="col-md-8">
          <label for="baseUrl" class="form-label">Base URL *</label>
          <input id="baseUrl" v-model="form.baseUrl"
            :class="['form-control', checkInvalid('baseUrl', checkFields) ? 'is-invalid' : '']" />
          <div class="form-text">只允許後端白名單中的官方 HTTPS Host。</div>
          <div class="invalid-feedback">{{ invalidFeedback("baseUrl", checkFields) }}</div>
        </div>
        <div class="col-md-6">
          <label for="modelId" class="form-label">Model ID *</label>
          <input id="modelId" v-model="form.modelId" maxlength="100"
            :class="['form-control', checkInvalid('modelId', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("modelId", checkFields) }}</div>
        </div>
        <div class="col-md-6">
          <label for="apiKey" class="form-label">API Key {{ props.edit ? "（留空表示不變）" : "*" }}</label>
          <input id="apiKey" v-model="form.apiKey" type="password" autocomplete="new-password"
            :placeholder="props.edit ? form.maskedApiKey : ''"
            :class="['form-control', checkInvalid('apiKey', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("apiKey", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="temperature" class="form-label">Temperature</label>
          <input id="temperature" v-model.number="form.temperature" type="number" min="0" max="2" step="0.01"
            :class="['form-control', checkInvalid('temperature', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("temperature", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="maxOutputTokens" class="form-label">最大輸出 Token</label>
          <input id="maxOutputTokens" v-model.number="form.maxOutputTokens" type="number" min="256" max="32000"
            :class="['form-control', checkInvalid('maxOutputTokens', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("maxOutputTokens", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="timeoutSeconds" class="form-label">逾時秒數</label>
          <input id="timeoutSeconds" v-model.number="form.timeoutSeconds" type="number" min="10" max="120"
            :class="['form-control', checkInvalid('timeoutSeconds', checkFields) ? 'is-invalid' : '']" />
          <div class="invalid-feedback">{{ invalidFeedback("timeoutSeconds", checkFields) }}</div>
        </div>
        <div class="col-md-4">
          <label for="defaultFlag" class="form-label">Tenant 預設</label>
          <select id="defaultFlag" v-model="form.defaultFlag" class="form-select">
            <option value="N">否</option><option value="Y">是</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="status" class="form-label">狀態</label>
          <select id="status" v-model="form.status" class="form-select">
            <option value="ACTIVE">啟用</option><option value="INACTIVE">停用</option>
          </select>
        </div>
        <div v-if="props.edit" class="col-md-4">
          <label class="form-label">設定版本</label>
          <input :value="form.configVersion" class="form-control" readonly />
        </div>
        <div v-if="props.edit" class="col-md-4">
          <label class="form-label">最近連線測試</label>
          <input
            :value="form.lastTestStatus || '尚未測試'"
            class="form-control"
            readonly
          />
          <div v-if="form.lastTestDate" class="form-text">
            {{ new Date(form.lastTestDate).toLocaleString() }}
          </div>
        </div>
        <div class="col-12 d-flex gap-2">
          <button class="btn btn-primary" @click="save"><i class="bi bi-save"></i> 儲存</button>
          <button v-if="!props.edit" class="btn btn-outline-secondary" @click="clear">清除</button>
          <button v-if="props.edit" class="btn btn-outline-secondary" @click="load">重新載入</button>
          <button
            v-if="props.edit"
            class="btn btn-outline-primary"
            @click="confirmFire('連線測試會連至此 Provider 的官方 API，確定繼續？', testConnection, form.oid)"
          >
            測試連線
          </button>
          <button v-if="props.edit && form.status === 'ACTIVE'" class="btn btn-outline-danger"
            @click="confirmFire('確定停用此 AI Provider？', deactivate, form.oid)">停用</button>
        </div>
      </div>
    </div>
  </div>
</template>
