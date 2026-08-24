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
const page = ref(1);
const pageSize = ref(30);
const totalCount = ref(0);
const totalPages = ref(1);
const loading = ref(false);
const detailLoading = ref(false);
const detail = ref<any | null>(null);
const reassignTarget = ref<any | null>(null);
const reassignOptions = ref<any[]>([]);
const reassignAccount = ref("");
const reassignReason = ref("");
const reassigning = ref(false);
const reassignPreview = ref<any | null>(null);
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
      body: {
        status: status.value || null,
        keyword: keyword.value.trim() || null,
        page: page.value,
        pageSize: pageSize.value,
      },
      headers: { "X-FlowMint-Tenant": tenantId.value },
    });
    if (!ok(response)) {
      processes.value = [];
      return toast.warning(response?.message || "無法載入流程實例");
    }
    processes.value = response.value?.items || [];
    totalCount.value = response.value?.totalCount || 0;
    totalPages.value = Math.max(1, response.value?.totalPages || 1);
    page.value = response.value?.page || 1;
  } finally {
    loading.value = false;
  }
};
const search = () => { page.value = 1; load(); };
const changePage = (target: number) => {
  if (target < 1 || target > totalPages.value || target === page.value) return;
  page.value = target;
  load();
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
const operationsPost = (path: string, body: any = {}) => useApi(`/fm/operations${path}`, {
  method: "POST",
  body,
  headers: { "X-FlowMint-Tenant": tenantId.value },
});
const openReassign = async (task: any, parallel = false) => {
  reassignTarget.value = { ...task, parallel };
  reassignAccount.value = "";
  reassignReason.value = "";
  reassignPreview.value = parallel ? {
    previousAssignee: task.account,
    assignmentMode: "PARALLEL_ADD_SIGN",
    warning: "只改派這一張平行加簽 Task，其他成員不變",
  } : null;
  const response: any = await operationsPost("/tasks/reassign-options");
  if (!ok(response)) {
    reassignTarget.value = null;
    return toast.warning(response?.message || "無法讀取改派人選");
  }
  reassignOptions.value = (response.value || []).filter(
    (item: any) => item.value !== task.assignee && item.value !== task.account,
  );
};
const previewReassign = async () => {
  if (!reassignTarget.value || !reassignAccount.value) {
    return toast.warning("請先選擇新簽核人");
  }
  if (reassignTarget.value.parallel) {
    reassignPreview.value = {
      previousAssignee: reassignTarget.value.account,
      targetAccount: reassignAccount.value,
      assignmentMode: "PARALLEL_ADD_SIGN",
      warning: "只改派這一張平行加簽 Task，其他成員不變",
    };
    return;
  }
  const response: any = await operationsPost("/tasks/reassign-preview", {
    taskId: reassignTarget.value.taskId,
    targetAccount: reassignAccount.value,
  });
  if (!ok(response)) return toast.warning(response?.message || "改派預覽失敗");
  reassignPreview.value = response.value;
};
const submitReassign = async () => {
  if (!reassignTarget.value || !reassignAccount.value || !reassignReason.value.trim()) {
    return toast.warning("請選擇新簽核人並填寫改派原因");
  }
  if (!reassignPreview.value
      || reassignPreview.value.targetAccount !== reassignAccount.value) {
    return toast.warning("請先執行改派預覽並確認結果");
  }
  reassigning.value = true;
  try {
    const target = reassignTarget.value;
    const response: any = target.parallel
      ? await operationsPost("/parallel-add-sign/reassign", {
          taskId: target.taskId,
          targetAccount: reassignAccount.value,
          reason: reassignReason.value.trim(),
        })
      : await operationsPost("/tasks/reassign", {
          taskId: target.taskId,
          targetAccount: reassignAccount.value,
          reason: reassignReason.value.trim(),
          requestKey: crypto.randomUUID(),
        });
    if (!ok(response)) return toast.warning(response?.message || "改派失敗");
    toast.success("改派完成");
    const processInstanceId = detail.value?.process?.processInstanceId;
    reassignTarget.value = null;
    if (processInstanceId) await loadDetail(processInstanceId);
  } finally {
    reassigning.value = false;
  }
};

watch([tenantId, status, pageSize], () => { detail.value = null; page.value = 1; load(); });
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
      <div class="col-lg-3"><label class="form-label">關鍵字</label><input v-model.trim="keyword" class="form-control" placeholder="流程編號、單號、名稱或帳號" @keyup.enter="search" /></div>
      <div class="col-lg-2 d-grid"><button class="btn btn-primary" :disabled="loading" @click="search">查詢</button></div>
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
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mt-3">
      <div class="text-muted small">共 {{ totalCount }} 筆，第 {{ page }}／{{ totalPages }} 頁</div>
      <div class="d-flex align-items-center gap-2">
        <select v-model.number="pageSize" class="form-select form-select-sm" style="width: auto">
          <option :value="10">每頁 10 筆</option><option :value="30">每頁 30 筆</option><option :value="50">每頁 50 筆</option><option :value="100">每頁 100 筆</option>
        </select>
        <button class="btn btn-sm btn-outline-secondary" :disabled="page <= 1 || loading" @click="changePage(page - 1)">上一頁</button>
        <button class="btn btn-sm btn-outline-secondary" :disabled="page >= totalPages || loading" @click="changePage(page + 1)">下一頁</button>
      </div>
    </div>
    <div v-if="detail" class="card border-0 shadow-sm mt-4">
      <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <div><strong>稽核軌跡</strong><span class="text-muted ms-2">{{ detail.process.businessKey }}</span></div>
        <button class="btn-close" aria-label="關閉" @click="detail = null"></button>
      </div>
      <div class="card-body">
        <h5>目前簽核工作</h5>
        <div v-if="!detail.activeTasks?.length" class="text-muted mb-4">目前沒有 active Task</div>
        <div v-else class="table-responsive mb-4">
          <table class="table table-sm align-middle">
            <thead><tr><th>節點</th><th>Task ID</th><th>簽核人</th><th>期限</th><th></th></tr></thead>
            <tbody><tr v-for="task in detail.activeTasks" :key="task.taskId">
              <td>{{ task.taskName }}<div class="small text-muted">{{ task.taskDefinitionKey }} / {{ task.assignmentMode }}</div></td>
              <td class="small">{{ task.taskId }}</td><td>{{ task.assignee || "候選人" }}</td>
              <td>{{ formatDate(task.dueDate) }}</td><td class="text-end">
                <button v-if="task.reassignable" class="btn btn-sm btn-outline-warning" @click="openReassign(task)">管理員改派</button>
                <span v-else class="small text-muted">{{ task.blockedReason }}</span>
              </td>
            </tr></tbody>
          </table>
        </div>
        <h5>平行加簽</h5>
        <div v-if="!detail.parallelAddSigns?.length" class="text-muted mb-4">尚無平行加簽</div>
        <div v-for="batch in detail.parallelAddSigns" :key="batch.batchOid" class="border rounded p-3 mb-3">
          <div class="d-flex justify-content-between gap-2">
            <strong>{{ batch.status }}</strong>
            <span>完成 {{ batch.completedCount }}/{{ batch.totalCount }}・同意 {{ batch.agreeCount }}・不同意 {{ batch.disagreeCount }}</span>
          </div>
          <div v-for="member in batch.members" :key="member.account" class="small mt-2">
            {{ member.displayName || member.account }}
            <span class="badge text-bg-light border ms-2">{{ member.status }}</span>
            <span v-if="member.comment" class="ms-2 text-muted">{{ member.comment }}</span>
            <button v-if="detail.canReassign && batch.status === 'WAITING' && member.status === 'PENDING'"
              class="btn btn-sm btn-link text-warning" @click="openReassign(member, true)">改派</button>
          </div>
        </div>
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
    <div v-if="reassignTarget" class="card border-warning shadow-sm mt-4">
      <div class="card-header bg-warning-subtle d-flex justify-content-between">
        <strong>管理員改派</strong><button class="btn-close" @click="reassignTarget = null"></button>
      </div>
      <div class="card-body row g-3">
        <div class="col-12 small text-muted">Task {{ reassignTarget.taskId }}，原簽核人：{{ reassignTarget.assignee || reassignTarget.account }}</div>
        <div class="col-md-5"><label class="form-label">新簽核人</label><select v-model="reassignAccount" class="form-select" @change="reassignPreview = null"><option value="">請選擇</option><option v-for="option in reassignOptions" :key="option.value" :value="option.value">{{ option.label }}</option></select></div>
        <div class="col-md-7"><label class="form-label">改派原因</label><textarea v-model="reassignReason" class="form-control" maxlength="2000" rows="3"></textarea></div>
        <div v-if="reassignPreview" class="col-12"><div class="alert alert-warning mb-0">
          <strong>{{ reassignPreview.previousAssignee || "候選人" }} → {{ reassignPreview.targetDisplayName || reassignPreview.targetAccount }}</strong>
          <div>{{ reassignPreview.assignmentMode }}：{{ reassignPreview.warning }}</div>
        </div></div>
        <div class="col-12 text-end d-flex justify-content-end gap-2">
          <button class="btn btn-outline-warning" :disabled="reassigning" @click="previewReassign">預覽改派</button>
          <button class="btn btn-warning" :disabled="reassigning || !reassignPreview" @click="submitReassign">二次確認改派</button>
        </div>
      </div>
    </div>
  </main>
</template>
