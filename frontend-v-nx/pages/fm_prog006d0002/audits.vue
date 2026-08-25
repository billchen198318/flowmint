<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";

interface OptionItem { value: string; label: string }
interface AuditRow {
  executionId: string; tenantId: string; actionCode: string; versionNo: number;
  loginAccount: string; executionStatus: string; rollbackOnly: string;
  stepCount: number; requestParameterCount: number; startTime: string;
  endTime: string; durationMs: number; errorMessage: string;
}

definePageMeta({ middleware: ["auth"] });
const router = useRouter();
const { showLoading, hideLoading } = useSwalLoading();
const tenants = ref<OptionItem[]>([]);
const rows = ref<AuditRow[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(30);
const filters = ref({ tenantId: "", executionId: "", actionCode: "", executionStatus: "", loginAccount: "" });
const post = (path: string, body: unknown) => getAxiosInstance().post(import.meta.env.VITE_API_URL + path, body);

const query = async () => {
  if (!filters.value.tenantId) { toast.warning("請先選擇 Tenant。"); return; }
  showLoading();
  try {
    const response = await post("/FM_PROG006D0002/execution-audits/findPage", {
      field: filters.value, pageOf: { select: String(page.value), showRow: String(pageSize.value) },
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) throw new Error(response.data?.message || "查詢失敗");
    rows.value = response.data?.value || [];
    total.value = Number(response.data?.pageOf?.countSize || 0);
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(error instanceof Error ? error.message : "查詢失敗"));
  } finally { hideLoading(); }
};

onMounted(async () => {
  const response = await post("/FM_PROG006D0001/tenant-options", {});
  tenants.value = response.data?.value || [];
});
</script>

<template>
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="mb-0">Data Action 執行紀錄</h3>
    <button class="btn btn-outline-secondary" @click="router.push('/fm_prog006d0002')">返回</button>
  </div>
  <div class="card mb-3"><div class="card-body"><div class="row g-3">
    <div class="col-md-3"><label class="form-label">Tenant *</label><select v-model="filters.tenantId" class="form-select"><option value="">請選擇</option><option v-for="item in tenants" :key="item.value" :value="item.value">{{ item.label }}</option></select></div>
    <div class="col-md-3"><label class="form-label">Execution ID</label><input v-model="filters.executionId" class="form-control" /></div>
    <div class="col-md-2"><label class="form-label">Action Code</label><input v-model="filters.actionCode" class="form-control" /></div>
    <div class="col-md-2"><label class="form-label">帳號</label><input v-model="filters.loginAccount" class="form-control" /></div>
    <div class="col-md-2"><label class="form-label">狀態</label><select v-model="filters.executionStatus" class="form-select"><option value="">全部</option><option value="SUCCESS">SUCCESS</option><option value="FAILED">FAILED</option></select></div>
    <div class="col-12"><button class="btn btn-primary" @click="page=1; query()">查詢</button></div>
  </div></div></div>
  <div class="table-responsive"><table class="table table-striped table-sm"><thead><tr><th>開始時間</th><th>Execution ID</th><th>Action</th><th>版本</th><th>帳號</th><th>狀態</th><th>Preview</th><th>耗時(ms)</th><th>錯誤</th></tr></thead><tbody>
    <tr v-for="row in rows" :key="row.executionId"><td>{{ row.startTime }}</td><td class="font-monospace">{{ row.executionId }}</td><td>{{ row.actionCode }}</td><td>{{ row.versionNo }}</td><td>{{ row.loginAccount }}</td><td>{{ row.executionStatus }}</td><td>{{ row.rollbackOnly }}</td><td>{{ row.durationMs }}</td><td>{{ row.errorMessage }}</td></tr>
    <tr v-if="rows.length === 0"><td colspan="9" class="text-center text-muted">尚無資料</td></tr>
  </tbody></table></div>
  <div class="d-flex justify-content-between"><span>共 {{ total }} 筆</span><div><button class="btn btn-sm btn-outline-secondary me-2" :disabled="page <= 1" @click="page--; query()">上一頁</button><button class="btn btn-sm btn-outline-secondary" :disabled="page * pageSize >= total" @click="page++; query()">下一頁</button></div></div>
</template>
