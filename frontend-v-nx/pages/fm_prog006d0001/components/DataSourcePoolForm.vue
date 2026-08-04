<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<any[]>([]);
const result = ref<any>(null);
const newForm = () => ({ oid: "", tenantId: "", poolCode: "", poolName: "",
  dbType: "MARIADB", jdbcUrl: "jdbc:mariadb://127.0.0.1:3306/database",
  username: "", password: "", maximumPoolSize: 10, minimumIdle: 1,
  connectionTimeoutMs: 10000, idleTimeoutMs: 600000, maxLifetimeMs: 1800000,
  validationQuery: "SELECT 1", status: "ACTIVE", lockVersion: 0, description: "" });
const form = ref<any>(newForm());
const post = (path: string, body: any = {}) => getAxiosInstance().post(
  import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path, body);
const apply = (value: any) => { form.value = { ...value, password: "" }; };
const load = async () => {
  if (!props.edit) return;
  const response = await post("/load", { oid: route.params.id });
  if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
    toast.warning(escapeQifuHtmlMsg(response.data?.message)); router.push(PageConstants.frontendNamespace); return;
  }
  apply(response.data.value);
};
const valid = () => {
  if (!form.value.tenantId || !form.value.poolCode || !form.value.poolName ||
      !form.value.jdbcUrl || !form.value.username || (!props.edit && !form.value.password)) {
    toast.warning("請完整填寫必要欄位"); return false;
  }
  return true;
};
const call = async (path: string) => {
  if (!valid()) return;
  showLoading();
  try {
    const response = await post(path, form.value);
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message)); return;
    }
    if (path === "/test-connection") result.value = response.data.value;
    else if (props.edit) apply(response.data.value);
    else form.value = newForm();
    toast.success(response.data.message || "執行成功");
  } finally { hideLoading(); }
};
const deactivate = async () => { await post("/deactivate", { oid: form.value.oid }); await load(); };
onMounted(async () => { tenants.value = (await post("/tenant-options")).data?.value || []; await load(); });
</script>

<template>
  <Toolbar :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    description="設定 MariaDB/MySQL、Oracle 或 MSSQL 連線池。Driver 由後端白名單決定，密碼只寫入不回傳。"
    backFlag="Y" refreshFlag="Y" saveFlag="Y" @backMethod="router.back()"
    @refreshMethod="props.edit ? load() : (form = newForm())"
    @saveMethod="call(props.edit ? '/update' : '/save')" />
  <div class="card"><div class="card-body"><div class="row g-3">
    <div class="col-md-4"><label class="form-label">Tenant *</label>
      <select v-model="form.tenantId" :disabled="props.edit" class="form-select"><option value="">請選擇</option>
        <option v-for="item in tenants" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
    <div class="col-md-4"><label class="form-label">連線池代碼 *</label>
      <input v-model="form.poolCode" :readonly="props.edit" class="form-control" maxlength="50" /></div>
    <div class="col-md-4"><label class="form-label">連線池名稱 *</label>
      <input v-model="form.poolName" class="form-control" maxlength="100" /></div>
    <div class="col-md-3"><label class="form-label">資料庫類型 *</label>
      <select v-model="form.dbType" class="form-select"><option value="MARIADB">MariaDB / MySQL</option>
        <option value="ORACLE">Oracle</option><option value="MSSQL">MSSQL</option></select></div>
    <div class="col-md-9"><label class="form-label">JDBC URL *</label>
      <input v-model="form.jdbcUrl" class="form-control" /></div>
    <div class="col-md-4"><label class="form-label">帳號 *</label>
      <input v-model="form.username" class="form-control" autocomplete="off" /></div>
    <div class="col-md-4"><label class="form-label">密碼 {{ props.edit ? '（留空表示不變）' : '*' }}</label>
      <input v-model="form.password" type="password" class="form-control" autocomplete="new-password" /></div>
    <div class="col-md-4"><label class="form-label">狀態</label>
      <select v-model="form.status" class="form-select"><option value="ACTIVE">啟用</option><option value="INACTIVE">停用</option></select></div>
    <div class="col-md-3"><label class="form-label">最大連線數</label><input v-model.number="form.maximumPoolSize" type="number" min="1" max="100" class="form-control" /></div>
    <div class="col-md-3"><label class="form-label">最小閒置數</label><input v-model.number="form.minimumIdle" type="number" min="0" class="form-control" /></div>
    <div class="col-md-3"><label class="form-label">連線逾時(ms)</label><input v-model.number="form.connectionTimeoutMs" type="number" class="form-control" /></div>
    <div class="col-md-3"><label class="form-label">驗證 SQL</label><input v-model="form.validationQuery" class="form-control" /></div>
    <div class="col-12"><label class="form-label">說明</label><textarea v-model="form.description" class="form-control" maxlength="500"></textarea></div>
    <div v-if="result" class="col-12"><div class="alert alert-success">連線成功：{{ result.databaseProduct }} {{ result.databaseVersion }}，耗時 {{ result.elapsedMs }} ms</div></div>
    <div class="col-12 d-flex gap-2"><button class="btn btn-primary" @click="call(props.edit ? '/update' : '/save')">儲存</button>
      <button class="btn btn-outline-success" @click="call('/test-connection')">測試連線</button>
      <button v-if="props.edit && form.status === 'ACTIVE'" class="btn btn-outline-danger"
        @click="confirmFire('確定停用此連線池？', deactivate, form.oid)">停用</button></div>
  </div></div></div>
</template>
