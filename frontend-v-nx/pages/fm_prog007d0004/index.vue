<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import { useRoute } from "vue-router";
import { PageConstants } from "./config";

definePageMeta({ middleware: ["auth"] });

const route = useRoute();
const tenants = ref<any[]>([]);
const tenantId = ref(String(route.query.tenant || ""));
const relation = ref("ALL");
const status = ref("");
const outcome = ref("");
const keyword = ref("");
const startDate = ref("");
const endDate = ref("");
const actionStartDate = ref("");
const actionEndDate = ref("");
const page = ref(1);
const pageSize = ref(20);
const totalCount = ref(0);
const totalPages = ref(1);
const items = ref<any[]>([]);
const loading = ref(false);

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const formatDate = (value: string | null) =>
  value
    ? new Intl.DateTimeFormat("zh-TW", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(new Date(value))
    : "-";
const relationLabel: Record<string, string> = {
  APPLICANT: "申請人",
  STARTER: "發起人",
  APPROVER: "簽核人",
};
const statusLabel: Record<string, string> = {
  RUNNING: "進行中",
  COMPLETED: "已完成",
  REJECTED: "已駁回",
  CANCELLED: "已取消",
  TERMINATED: "已終止",
};
const statusClass: Record<string, string> = {
  RUNNING: "text-bg-primary",
  COMPLETED: "text-bg-success",
  REJECTED: "text-bg-danger",
  CANCELLED: "text-bg-secondary",
  TERMINATED: "text-bg-dark",
};
const summary = computed(() => ({
  total: totalCount.value,
  started: items.value.filter((item) =>
    item.relations?.some((value: string) => value === "APPLICANT" || value === "STARTER"),
  ).length,
  signed: items.value.filter((item) => item.relations?.includes("APPROVER")).length,
}));

const loadTenants = async () => {
  const response: any = await useApi("/fm/requests/start/tenants", {
    method: "POST",
    body: {},
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
  try {
    const response: any = await useApi(
      `${PageConstants.eventNamespace}/findPage`,
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenantId.value },
        body: {
          relation: relation.value,
          status: status.value || null,
          outcome: outcome.value || null,
          keyword: keyword.value.trim() || null,
          startDate: startDate.value || null,
          endDate: endDate.value || null,
          actionStartDate: actionStartDate.value || null,
          actionEndDate: actionEndDate.value || null,
          page: page.value,
          pageSize: pageSize.value,
        },
      },
    );
    if (!ok(response)) {
      items.value = [];
      totalCount.value = 0;
      toast.warning(response?.message || "查詢流程紀錄失敗");
      return;
    }
    items.value = response.value?.items || [];
    totalCount.value = Number(response.value?.totalCount || 0);
    totalPages.value = Math.max(1, Number(response.value?.totalPages || 1));
    page.value = Number(response.value?.page || 1);
  } finally {
    loading.value = false;
  }
};

const search = () => {
  page.value = 1;
  void load();
};
const clear = () => {
  status.value = "";
  outcome.value = "";
  keyword.value = "";
  startDate.value = "";
  endDate.value = "";
  actionStartDate.value = "";
  actionEndDate.value = "";
  search();
};
const changePage = (target: number) => {
  if (target < 1 || target > totalPages.value || target === page.value) return;
  page.value = target;
  void load();
};
watch(tenantId, () => search());
watch(pageSize, () => search());
onMounted(async () => {
  await loadTenants();
  await load();
});
</script>

<template>
  <main class="container-fluid py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
      <div>
        <h2 class="mb-1">我的流程紀錄</h2>
        <p class="text-muted mb-0">查詢自己申請、發起或實際簽核處理過的流程。</p>
      </div>
      <div class="d-flex gap-2 flex-wrap">
        <span class="badge text-bg-primary fs-6">共 {{ summary.total }} 筆</span>
        <span class="badge text-bg-light border fs-6">本頁發起／申請 {{ summary.started }}</span>
        <span class="badge text-bg-light border fs-6">本頁簽核 {{ summary.signed }}</span>
      </div>
    </div>

    <ul class="nav nav-pills gap-2 mb-4">
      <li v-for="tab in [
        { value: 'ALL', label: '全部相關' },
        { value: 'STARTED', label: '我申請／發起的' },
        { value: 'SIGNED', label: '我簽核的' },
      ]" :key="tab.value" class="nav-item">
        <button class="nav-link" :class="{ active: relation === tab.value }"
          @click="relation = tab.value; search()">{{ tab.label }}</button>
      </li>
    </ul>

    <section class="card border-0 shadow-sm mb-4">
      <div class="card-body row g-3 align-items-end">
        <div class="col-md-6 col-xl-3">
          <label class="form-label">Tenant</label>
          <select v-model="tenantId" class="form-select">
            <option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">
              {{ tenant.tenantCode }} - {{ tenant.tenantName }}
            </option>
          </select>
        </div>
        <div class="col-md-6 col-xl-3">
          <label class="form-label">關鍵字</label>
          <input v-model="keyword" class="form-control" placeholder="單號、流程名稱或申請人"
            @keyup.enter="search" />
        </div>
        <div class="col-md-6 col-xl-2">
          <label class="form-label">流程狀態</label>
          <select v-model="status" class="form-select">
            <option value="">全部</option>
            <option v-for="(label, value) in statusLabel" :key="value" :value="value">{{ label }}</option>
          </select>
        </div>
        <div class="col-md-6 col-xl-2">
          <label class="form-label">我的處理結果</label>
          <select v-model="outcome" class="form-select">
            <option value="">全部</option>
            <option value="APPROVE">核准</option>
            <option value="REJECT">駁回</option>
            <option value="RETURN">退回</option>
            <option value="RESUBMIT">重新送出</option>
          </select>
        </div>
        <div class="col-6 col-xl-2"><label class="form-label">發起日起</label><input v-model="startDate" type="date" class="form-control" /></div>
        <div class="col-6 col-xl-2"><label class="form-label">發起日迄</label><input v-model="endDate" type="date" class="form-control" /></div>
        <div class="col-6 col-xl-2"><label class="form-label">處理日起</label><input v-model="actionStartDate" type="date" class="form-control" /></div>
        <div class="col-6 col-xl-2"><label class="form-label">處理日迄</label><input v-model="actionEndDate" type="date" class="form-control" /></div>
        <div class="col-xl-4 d-flex gap-2 justify-content-xl-end">
          <button class="btn btn-outline-secondary" :disabled="loading" @click="clear">清除</button>
          <button class="btn btn-primary px-4" :disabled="loading" @click="search">查詢</button>
        </div>
      </div>
    </section>

    <section class="card border-0 shadow-sm desktop-results">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead><tr><th>單號／流程</th><th>申請人</th><th>我的關係</th><th>我的最後處理</th><th>狀態／目前節點</th><th>發起時間</th><th></th></tr></thead>
          <tbody>
            <tr v-if="!loading && !items.length"><td colspan="7" class="text-center text-muted py-5">查無符合條件的流程紀錄</td></tr>
            <tr v-for="item in items" :key="item.processInstanceId">
              <td><strong>{{ item.documentNumber || item.businessKey }}</strong><div>{{ item.processName }}</div><small class="text-muted">{{ item.formName }}</small></td>
              <td>{{ item.applicantAccount }}<small v-if="item.starterAccount !== item.applicantAccount" class="d-block text-muted">發起：{{ item.starterAccount }}</small></td>
              <td><span v-for="value in item.relations" :key="value" class="badge text-bg-light border me-1">{{ relationLabel[value] || value }}</span></td>
              <td>{{ item.lastOutcome || item.lastActionType || '-' }}<small class="d-block text-muted">{{ formatDate(item.lastActionDate) }}</small></td>
              <td><span class="badge" :class="statusClass[item.instanceStatus] || 'text-bg-secondary'">{{ statusLabel[item.instanceStatus] || item.instanceStatus }}</span><small class="d-block mt-1 text-muted">{{ item.currentTaskNames?.join('、') || '-' }}</small></td>
              <td>{{ formatDate(item.startDate) }}</td>
              <td><NuxtLink class="btn btn-sm btn-outline-primary" :to="{ path: `/requests/${item.processInstanceId}`, query: { tenant: tenantId } }">查看</NuxtLink></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="mobile-results d-none">
      <div v-if="!loading && !items.length" class="card border-0 shadow-sm p-5 text-center text-muted">查無符合條件的流程紀錄</div>
      <article v-for="item in items" :key="item.processInstanceId" class="card border-0 shadow-sm mb-3">
        <div class="card-body">
          <div class="d-flex justify-content-between gap-2 mb-2"><strong>{{ item.documentNumber || item.businessKey }}</strong><span class="badge" :class="statusClass[item.instanceStatus] || 'text-bg-secondary'">{{ statusLabel[item.instanceStatus] }}</span></div>
          <div>{{ item.processName }}</div><div class="small text-muted mb-3">申請人 {{ item.applicantAccount }}・{{ formatDate(item.startDate) }}</div>
          <div class="mb-3"><span v-for="value in item.relations" :key="value" class="badge text-bg-light border me-1">{{ relationLabel[value] }}</span></div>
          <NuxtLink class="btn btn-outline-primary w-100" :to="{ path: `/requests/${item.processInstanceId}`, query: { tenant: tenantId } }">查看流程紀錄</NuxtLink>
        </div>
      </article>
    </section>

    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mt-3">
      <div class="text-muted small">共 {{ totalCount }} 筆，第 {{ page }}／{{ totalPages }} 頁</div>
      <div class="d-flex gap-2 align-items-center">
        <select v-model.number="pageSize" class="form-select form-select-sm" style="width: auto"><option :value="10">每頁 10 筆</option><option :value="20">每頁 20 筆</option><option :value="30">每頁 30 筆</option><option :value="50">每頁 50 筆</option></select>
        <button class="btn btn-sm btn-outline-secondary" :disabled="page <= 1 || loading" @click="changePage(page - 1)">上一頁</button>
        <button class="btn btn-sm btn-outline-secondary" :disabled="page >= totalPages || loading" @click="changePage(page + 1)">下一頁</button>
      </div>
    </div>
  </main>
</template>

<style scoped>
@media (max-width: 767.98px) {
  .desktop-results { display: none; }
  .mobile-results { display: block !important; }
}
</style>
