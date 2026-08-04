<script setup lang="ts">
import { onMounted, ref } from "vue";
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

interface ConnectionTestResult {
  databaseProduct: string;
  databaseVersion: string;
  driverName: string;
  elapsedMs: number;
}

interface DataSourcePoolForm {
  oid: string;
  tenantId: string;
  poolCode: string;
  poolName: string;
  dbType: string;
  jdbcUrl: string;
  username: string;
  password: string;
  maximumPoolSize: number;
  minimumIdle: number;
  connectionTimeoutMs: number;
  idleTimeoutMs: number;
  maxLifetimeMs: number;
  validationQuery: string;
  status: string;
  lockVersion: number;
  description: string;
}

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<OptionItem[]>([]);
const result = ref<ConnectionTestResult | null>(null);
const checkFields = ref<Record<string, string>>({});
const newForm = (): DataSourcePoolForm => ({
  oid: "",
  tenantId: "",
  poolCode: "",
  poolName: "",
  dbType: "MARIADB",
  jdbcUrl: "jdbc:mariadb://127.0.0.1:3306/database",
  username: "",
  password: "",
  maximumPoolSize: 10,
  minimumIdle: 1,
  connectionTimeoutMs: 10000,
  idleTimeoutMs: 600000,
  maxLifetimeMs: 1800000,
  validationQuery: "SELECT 1",
  status: "ACTIVE",
  lockVersion: 0,
  description: "",
});
const form = ref<DataSourcePoolForm>(newForm());
const post = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const apply = (value: DataSourcePoolForm) => {
  form.value = { ...value, password: "" };
  checkFields.value = {};
  result.value = null;
};
const clear = () => {
  form.value = newForm();
  checkFields.value = {};
  result.value = null;
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取連線池失敗。"),
      );
      await router.push(PageConstants.frontendNamespace);
      return;
    }
    apply(response.data.value);
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "讀取連線池失敗。";
    toast.error(escapeQifuHtmlMsg(message));
    await router.push(PageConstants.frontendNamespace);
  } finally {
    hideLoading();
  }
};
const valid = () => {
  const fields: Record<string, string> = {};
  if (!form.value.tenantId) fields.tenantId = "請選擇 Tenant";
  if (!form.value.poolCode?.trim()) fields.poolCode = "請輸入連線池代碼";
  if (!form.value.poolName?.trim()) fields.poolName = "請輸入連線池名稱";
  if (!form.value.dbType) fields.dbType = "請選擇資料庫類型";
  if (!form.value.jdbcUrl?.trim()) fields.jdbcUrl = "請輸入 JDBC URL";
  if (!form.value.username?.trim()) fields.username = "請輸入資料庫帳號";
  if (!props.edit && !form.value.password) fields.password = "請輸入資料庫密碼";
  if (form.value.maximumPoolSize < 1 || form.value.maximumPoolSize > 100) {
    fields.maximumPoolSize = "最大連線數必須介於 1 到 100";
  }
  if (form.value.minimumIdle < 0 || form.value.minimumIdle > form.value.maximumPoolSize) {
    fields.minimumIdle = "最小閒置數不可小於 0 或超過最大連線數";
  }
  if (form.value.connectionTimeoutMs < 250 || form.value.connectionTimeoutMs > 120000) {
    fields.connectionTimeoutMs = "連線逾時必須介於 250 到 120000 毫秒";
  }
  checkFields.value = fields;
  const firstMessage = Object.values(fields)[0];
  if (firstMessage) {
    toast.warning(escapeQifuHtmlMsg(firstMessage));
    return false;
  }
  return true;
};
const call = async (path: string) => {
  if (!valid()) return;
  showLoading();
  try {
    const response = await post(path, form.value);
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "操作連線池失敗。"),
      );
      return;
    }
    if (path === "/test-connection") result.value = response.data.value;
    else if (props.edit) apply(response.data.value);
    else clear();
    toast.success(escapeQifuHtmlMsg(response.data.message || "執行成功"));
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "操作連線池失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用連線池失敗。"),
      );
      return;
    }
    apply(response.data.value);
    toast.success(escapeQifuHtmlMsg(response.data.message));
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "停用連線池失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};
onMounted(async () => {
  try {
    tenants.value = (await post("/tenant-options")).data?.value || [];
    await load();
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "載入 Tenant 選項失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  }
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    description="設定 MariaDB/MySQL、Oracle 或 MSSQL 連線池。Driver 由後端白名單決定，密碼只寫入不回傳。"
    backFlag="Y"
    refreshFlag="Y"
    saveFlag="Y"
    @backMethod="router.back()"
    @refreshMethod="props.edit ? load() : clear()"
    @saveMethod="call(props.edit ? '/update' : '/save')"
  />
  <div class="card">
    <div class="card-body">
      <div class="row g-3">
    <div class="col-md-4">
      <label for="tenantId" class="form-label">Tenant *</label>
      <select
        id="tenantId"
        v-model="form.tenantId"
        :disabled="props.edit"
        :class="['form-select', checkInvalid('tenantId', checkFields) ? 'is-invalid' : '']"
      >
        <option value="">請選擇</option>
        <option v-for="item in tenants" :key="item.value" :value="item.value">
          {{ item.label }}
        </option>
      </select>
      <div class="invalid-feedback">{{ invalidFeedback("tenantId", checkFields) }}</div>
    </div>
    <div class="col-md-4">
      <label for="poolCode" class="form-label">連線池代碼 *</label>
      <input id="poolCode" v-model="form.poolCode" :readonly="props.edit" maxlength="50"
        :class="['form-control', checkInvalid('poolCode', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("poolCode", checkFields) }}</div>
    </div>
    <div class="col-md-4">
      <label for="poolName" class="form-label">連線池名稱 *</label>
      <input id="poolName" v-model="form.poolName" maxlength="100"
        :class="['form-control', checkInvalid('poolName', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("poolName", checkFields) }}</div>
    </div>
    <div class="col-md-3">
      <label for="dbType" class="form-label">資料庫類型 *</label>
      <select id="dbType" v-model="form.dbType"
        :class="['form-select', checkInvalid('dbType', checkFields) ? 'is-invalid' : '']">
        <option value="MARIADB">MariaDB / MySQL</option>
        <option value="ORACLE">Oracle</option>
        <option value="MSSQL">MSSQL</option>
      </select>
      <div class="invalid-feedback">{{ invalidFeedback("dbType", checkFields) }}</div>
    </div>
    <div class="col-md-9">
      <label for="jdbcUrl" class="form-label">JDBC URL *</label>
      <input id="jdbcUrl" v-model="form.jdbcUrl"
        :class="['form-control', checkInvalid('jdbcUrl', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("jdbcUrl", checkFields) }}</div>
    </div>
    <div class="col-md-4">
      <label for="username" class="form-label">帳號 *</label>
      <input id="username" v-model="form.username" autocomplete="off"
        :class="['form-control', checkInvalid('username', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("username", checkFields) }}</div>
    </div>
    <div class="col-md-4">
      <label for="password" class="form-label">
        密碼 {{ props.edit ? "（留空表示不變）" : "*" }}
      </label>
      <input id="password" v-model="form.password" type="password" autocomplete="new-password"
        :class="['form-control', checkInvalid('password', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("password", checkFields) }}</div>
    </div>
    <div class="col-md-4">
      <label for="status" class="form-label">狀態</label>
      <select id="status" v-model="form.status" class="form-select">
        <option value="ACTIVE">啟用</option>
        <option value="INACTIVE">停用</option>
      </select>
    </div>
    <div class="col-md-3">
      <label for="maximumPoolSize" class="form-label">最大連線數</label>
      <input id="maximumPoolSize" v-model.number="form.maximumPoolSize" type="number" min="1" max="100"
        :class="['form-control', checkInvalid('maximumPoolSize', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("maximumPoolSize", checkFields) }}</div>
    </div>
    <div class="col-md-3">
      <label for="minimumIdle" class="form-label">最小閒置數</label>
      <input id="minimumIdle" v-model.number="form.minimumIdle" type="number" min="0"
        :class="['form-control', checkInvalid('minimumIdle', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("minimumIdle", checkFields) }}</div>
    </div>
    <div class="col-md-3">
      <label for="connectionTimeoutMs" class="form-label">連線逾時(ms)</label>
      <input id="connectionTimeoutMs" v-model.number="form.connectionTimeoutMs" type="number"
        :class="['form-control', checkInvalid('connectionTimeoutMs', checkFields) ? 'is-invalid' : '']" />
      <div class="invalid-feedback">{{ invalidFeedback("connectionTimeoutMs", checkFields) }}</div>
    </div>
    <div class="col-md-3">
      <label for="validationQuery" class="form-label">驗證 SQL</label>
      <input id="validationQuery" v-model="form.validationQuery" class="form-control" />
    </div>
    <div class="col-12">
      <label for="description" class="form-label">說明</label>
      <textarea id="description" v-model="form.description" class="form-control" maxlength="500"></textarea>
    </div>
    <div v-if="result" class="col-12">
      <div class="alert alert-success">
        連線成功：{{ result.databaseProduct }} {{ result.databaseVersion }}，耗時
        {{ result.elapsedMs }} ms
      </div>
    </div>
    <div class="col-12 d-flex gap-2">
      <button class="btn btn-primary" @click="call(props.edit ? '/update' : '/save')">
        <i class="bi bi-save"></i> 儲存
      </button>
      <button v-if="!props.edit" class="btn btn-outline-secondary" @click="clear">
        <i class="bi bi-eraser"></i> 清除
      </button>
      <button v-if="props.edit" class="btn btn-outline-secondary" @click="load">
        <i class="bi bi-repeat"></i> 重新載入
      </button>
      <button class="btn btn-outline-success" @click="call('/test-connection')">
        <i class="bi bi-plug"></i> 測試連線
      </button>
      <button v-if="props.edit && form.status === 'ACTIVE'" class="btn btn-outline-danger"
        @click="confirmFire('確定停用此連線池？', deactivate, form.oid)">
        <i class="bi bi-slash-circle"></i> 停用
      </button>
    </div>
      </div>
    </div>
  </div>
</template>
