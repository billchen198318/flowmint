<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { toast } from "vue3-toastify";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const tenants = ref<any[]>([]);
const processes = ref<any[]>([]);
const tenantId = ref(String(route.query.tenant || ""));
const status = ref("RUNNING");
const keyword = ref("");
const loading = ref(false);
const detailLoading = ref(false);
const detail = ref<any | null>(null);
const counts = computed(() => ({
  total: processes.value.length,
  running: processes.value.filter((item) => item.instanceStatus === "RUNNING").length,
  overdue: processes.value.filter((item) => item.overdueTaskCount > 0).length,
}));
const ok = (response: any) => response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat("zh-TW", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
  : "-";
const formatDuration = (minutes: number | null) => {
  const value = Number(minutes || 0);
  const days = Math.floor(value / 1440);
  const hours = Math.floor((value % 1440) / 60);
  const mins = value % 60;
  return [days ? `${days} 天` : "", hours ? `${hours} 小時` : "", `${mins} 分`]
    .filter(Boolean).join(" ");
};
const loadTenants = async () => {
  const response: any = await useApi("/fm/requests/start/tenants", { method: "POST", body: {} });
  if (!ok(response)) return toast.warning(response?.message || "無法載入 Tenant");
  tenants.value = response.value || [];
  if (!tenantId.value) tenantId.value = tenants.value[0]?.tenantId || "";
};
const load = async () => {
  if (!tenantId.value) return;
  loading.value = true;
  try {
    const response: any = await useApi("/fm/operations/process-instances", {
      method: "POST",
      body: { status: status.value || null, keyword: keyword.value.trim() || null },
      headers: { "X-FlowMint-Tenant": tenantId.value },
    });
    if (!ok(response)) {
      processes.value = [];
      return toast.warning(response?.message || "無法載入流程實例");
    }
    processes.value = response.value || [];
  } finally {
    loading.value = false;
  }
};
const loadDetail = async (processInstanceId: string) => {
  detailLoading.value = true;
  detail.value = null;
  try {
    const response: any = await useApi("/fm/operations/process-instances/load", {
      method: "POST",
      body: { processInstanceId },
      headers: { "X-FlowMint-Tenant": tenantId.value },
    });
    if (!ok(response)) return toast.warning(response?.message || "無法載入流程稽核明細");
    detail.value = response.value;
  } finally {
    detailLoading.value = false;
  }
};

watch([tenantId, status], () => { detail.value = null; load(); });
onMounted(async () => { await loadTenants(); await load(); });
</script>

<template>
  <main class="container-fluid py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
      <div><h2 class="mb-1">流程實例監控</h2><p class="text-muted mb-0">查看 Tenant 內流程狀態與目前執行節點。</p></div>
      <div class="d-flex gap-2"><span class="badge text-bg-primary fs-6">清單 {{ counts.total }}</span><span class="badge text-bg-warning fs-6">執行中 {{ counts.running }}</span><span class="badge text-bg-danger fs-6">逾時 {{ counts.overdue }}</span></div>
    </div>
    <div class="card border-0 shadow-sm mb-4"><div class="card-body row g-3 align-items-end">
      <div class="col-lg-4"><label class="form-label">Tenant</label><select v-model="tenantId" class="form-select"><option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">{{ tenant.tenantCode }} - {{ tenant.tenantName }}</option></select></div>
      <div class="col-lg-3"><label class="form-label">流程狀態</label><select v-model="status" class="form-select"><option value="RUNNING">執行中</option><option value="COMPLETED">已完成</option><option value="REJECTED">已駁回</option><option value="CANCELLED">已取消</option><option value="TERMINATED">已終止</option><option value="">全部</option></select></div>
      <div class="col-lg-3"><label class="form-label">關鍵字</label><input v-model.trim="keyword" class="form-control" placeholder="流程編號、單號、名稱或帳號" @keyup.enter="load" /></div>
      <div class="col-lg-2 d-grid"><button class="btn btn-primary" :disabled="loading" @click="load">查詢</button></div>
    </div></div>
    <div class="card border-0 shadow-sm"><div class="table-responsive"><table class="table table-hover align-middle mb-0">
      <thead><tr><th>狀態</th><th>流程／單號</th><th>申請人／發起人</th><th>目前節點／期限</th><th>耗時</th><th>開始／結束</th><th></th></tr></thead>
      <tbody>
        <tr v-if="!loading && !processes.length"><td colspan="7" class="text-center text-muted py-5">沒有符合條件的流程實例</td></tr>
        <tr v-for="item in processes" :key="item.processInstanceId">
          <td><span class="badge text-bg-secondary">{{ item.instanceStatus }}</span></td>
          <td><strong>{{ item.processName }} v{{ item.processVersionNo }}</strong><div class="small text-muted">{{ item.businessKey }}<br />{{ item.processInstanceId }}</div></td>
          <td>{{ item.ownerAccount }}<div class="small text-muted">發起：{{ item.initiatorAccount }}</div></td>
          <td>{{ item.currentTaskNames?.join("、") || "-" }}<div v-if="item.nearestDueDate" class="small" :class="item.overdueTaskCount ? 'text-danger fw-semibold' : 'text-muted'">期限：{{ formatDate(item.nearestDueDate) }}<span v-if="item.overdueTaskCount">（逾時 {{ item.overdueTaskCount }}）</span></div></td>
          <td>{{ formatDuration(item.elapsedMinutes) }}</td>
          <td>{{ formatDate(item.startDate) }}<div class="small text-muted">{{ formatDate(item.endDate) }}</div></td>
          <td><button class="btn btn-sm btn-outline-primary" :disabled="detailLoading" @click="loadDetail(item.processInstanceId)">查看稽核</button></td>
        </tr>
      </tbody>
    </table></div></div>
    <div v-if="detail" class="card border-0 shadow-sm mt-4">
      <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <div><strong>稽核軌跡</strong><span class="text-muted ms-2">{{ detail.process.businessKey }}</span></div>
        <button class="btn-close" aria-label="關閉" @click="detail = null"></button>
      </div>
      <div class="card-body">
        <h5>任務動作</h5>
        <div v-if="!detail.actions?.length" class="text-muted mb-4">尚無任務動作</div>
        <div v-for="(action, index) in detail.actions" :key="`${action.actionDate}-${index}`" class="border-start border-primary ps-3 pb-3">
          <strong>{{ action.actionType }}<span v-if="action.outcome"> · {{ action.outcome }}</span></strong>
          <div class="small text-muted">{{ action.actorAccount || "系統" }} · {{ formatDate(action.actionDate) }}</div>
          <div v-if="action.comment || action.reason" class="mt-1">{{ action.comment || action.reason }}</div>
        </div>
        <h5 class="mt-3">表單快照</h5>
        <div v-if="!detail.snapshots?.length" class="text-muted">尚無表單快照</div>
        <details v-for="snapshot in detail.snapshots" :key="snapshot.formSnapshotId" class="border rounded p-3 mb-2">
          <summary><strong>{{ snapshot.actionType }}</strong> · revision {{ snapshot.revisionNo }} · {{ formatDate(snapshot.snapshotDate) }}</summary>
          <div class="small text-muted mt-2">SHA-256: {{ snapshot.contentSha256 }}</div>
          <pre class="bg-light rounded p-3 mt-2 mb-0 overflow-auto">{{ JSON.stringify(snapshot.formData, null, 2) }}</pre>
        </details>
      </div>
    </div>
  </main>
</template>
