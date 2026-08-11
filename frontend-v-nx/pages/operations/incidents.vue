<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const tenants = ref<any[]>([]);
const incidents = ref<any[]>([]);
const assignees = ref<any[]>([]);
const tenantId = ref(String(route.query.tenant || ""));
const status = ref("OPEN");
const selected = ref<any>(null);
const targetAccount = ref("");
const reason = ref("");
const loading = ref(false);
const acting = ref(false);
const openCount = computed(() => incidents.value.filter(
  (item: any) => item.incidentStatus === "OPEN",
).length);
const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const operationsPost = (path: string, body: any = {}) =>
  useApi(`/fm/operations${path}`, {
    method: "POST",
    body,
    headers: { "X-FlowMint-Tenant": tenantId.value },
  });
const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat("zh-TW", {
      dateStyle: "medium", timeStyle: "short",
    }).format(new Date(value))
  : "-";
const formattedContext = (value: string | null) => {
  if (!value) return "-";
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
};
const loadTenants = async () => {
  const response: any = await useApi("/fm/requests/start/tenants", {
    method: "POST", body: {},
  });
  if (!ok(response)) {
    toast.warning(response?.message || "無法載入 Tenant");
    return;
  }
  tenants.value = response.value || [];
  if (!tenantId.value) tenantId.value = tenants.value[0]?.tenantId || "";
};
const load = async () => {
  if (!tenantId.value) return;
  loading.value = true;
  selected.value = null;
  try {
    const [incidentResponse, optionResponse]: any[] = await Promise.all([
      operationsPost("/incidents", { status: status.value || null }),
      operationsPost("/incidents/reassign-options"),
    ]);
    if (!ok(incidentResponse)) {
      toast.warning(incidentResponse?.message || "無法載入 Incident");
      incidents.value = [];
      return;
    }
    incidents.value = incidentResponse.value || [];
    assignees.value = ok(optionResponse) ? optionResponse.value || [] : [];
  } finally {
    loading.value = false;
  }
};
const choose = (incident: any) => {
  selected.value = incident;
  targetAccount.value = "";
  reason.value = "";
};
const run = async (action: "retry" | "reassign" | "terminate") => {
  if (!selected.value || !reason.value.trim()) {
    toast.warning("請先選擇 Incident 並填寫理由");
    return;
  }
  if (action === "reassign" && !targetAccount.value) {
    toast.warning("請選擇改派員工");
    return;
  }
  if (action === "terminate" && !window.confirm("確定要終止整個流程？此操作無法復原。")) return;
  acting.value = true;
  try {
    const response: any = action === "retry"
      ? await operationsPost("/incidents/retry", {
          incidentId: selected.value.incidentId, reason: reason.value,
        })
      : action === "reassign"
        ? await operationsPost("/incidents/reassign", {
            incidentId: selected.value.incidentId,
            targetAccount: targetAccount.value,
            reason: reason.value,
          })
        : await operationsPost("/process-instances/terminate", {
            processInstanceId: selected.value.processInstanceId,
            reason: reason.value,
          });
    if (!ok(response)) {
      toast.warning(response?.message || "Incident 操作失敗");
      return;
    }
    toast.success(action === "terminate" ? "流程已終止" : "Incident 已處理");
    await load();
  } finally {
    acting.value = false;
  }
};

watch([tenantId, status], load);
onMounted(async () => {
  await loadTenants();
  await load();
});
</script>

<template>
  <main class="container-fluid py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
      <div>
        <h2 class="mb-1">Incident 營運管理</h2>
        <p class="text-muted mb-0">處理簽核人解析失敗及卡住的流程工作。</p>
      </div>
      <span class="badge text-bg-danger fs-6">目前清單 OPEN：{{ openCount }}</span>
    </div>

    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body row g-3 align-items-end">
        <div class="col-md-5">
          <label class="form-label">Tenant</label>
          <select v-model="tenantId" class="form-select">
            <option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">
              {{ tenant.tenantCode }} - {{ tenant.tenantName }}
            </option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="form-label">Incident 狀態</label>
          <select v-model="status" class="form-select">
            <option value="OPEN">待處理</option>
            <option value="RESOLVED">已解決</option>
            <option value="IGNORED">已忽略</option>
            <option value="">全部</option>
          </select>
        </div>
        <div class="col-md-3 d-grid">
          <button class="btn btn-outline-primary" :disabled="loading" @click="load">重新整理</button>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <div class="col-xl-7">
        <div class="card border-0 shadow-sm">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead><tr><th>狀態</th><th>節點／錯誤</th><th>建立時間</th><th></th></tr></thead>
              <tbody>
                <tr v-if="!loading && !incidents.length"><td colspan="4" class="text-center text-muted py-5">沒有符合條件的 Incident</td></tr>
                <tr v-for="item in incidents" :key="item.incidentId">
                  <td><span :class="['badge', item.incidentStatus === 'OPEN' ? 'text-bg-danger' : 'text-bg-secondary']">{{ item.incidentStatus }}</span></td>
                  <td><strong>{{ item.taskDefKey || '-' }}</strong><div class="small text-muted">{{ item.errorCode }}：{{ item.errorMessage }}</div></td>
                  <td>{{ formatDate(item.createdDate) }}</td>
                  <td><button class="btn btn-sm btn-outline-primary" @click="choose(item)">查看／處理</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-xl-5">
        <div v-if="selected" class="card border-0 shadow-sm sticky-xl-top incident-detail">
          <div class="card-header bg-white py-3"><strong>Incident 明細</strong></div>
          <div class="card-body">
            <dl class="row small">
              <dt class="col-4">Incident</dt><dd class="col-8 text-break">{{ selected.incidentId }}</dd>
              <dt class="col-4">流程</dt><dd class="col-8 text-break">{{ selected.processInstanceId }}</dd>
              <dt class="col-4">Task</dt><dd class="col-8 text-break">{{ selected.taskId || '-' }}</dd>
              <dt class="col-4">錯誤</dt><dd class="col-8">{{ selected.errorMessage }}</dd>
            </dl>
            <label class="form-label">解析 Context</label>
            <pre class="context-box">{{ formattedContext(selected.contextData) }}</pre>
            <template v-if="selected.incidentStatus === 'OPEN'">
              <div class="mb-3">
                <label class="form-label">處理理由 *</label>
                <textarea v-model="reason" class="form-control" rows="3" maxlength="1000"></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">改派員工</label>
                <select v-model="targetAccount" class="form-select">
                  <option value="">請選擇</option>
                  <option v-for="option in assignees" :key="option.value" :value="option.value">{{ option.label }}</option>
                </select>
              </div>
              <div class="d-grid gap-2">
                <button class="btn btn-primary" :disabled="acting" @click="run('retry')">重新解析 Retry</button>
                <button class="btn btn-warning" :disabled="acting" @click="run('reassign')">管理員改派</button>
                <button class="btn btn-outline-danger" :disabled="acting" @click="run('terminate')">終止整個流程</button>
              </div>
            </template>
            <div v-else class="alert alert-secondary mb-0">
              {{ selected.resolvedBy || '-' }}／{{ formatDate(selected.resolvedDate) }}<br />
              {{ selected.resolutionNote || '沒有處理註記' }}
            </div>
          </div>
        </div>
        <div v-else class="card border-0 shadow-sm"><div class="card-body text-center text-muted py-5">請從清單選擇 Incident</div></div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.incident-detail { top: 1rem; }
.context-box { max-height: 220px; overflow: auto; padding: .75rem; border-radius: .5rem; background: #f6f8fa; font-size: .78rem; white-space: pre-wrap; word-break: break-word; }
</style>
