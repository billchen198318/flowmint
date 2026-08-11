<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { toast } from "vue3-toastify";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const tenants = ref<any[]>([]);
const tenantId = ref(String(route.query.tenant || ""));
const report = ref<any | null>(null);
const loading = ref(false);
const isoDate = (date: Date) => date.toISOString().slice(0, 10);
const endDate = ref(isoDate(new Date()));
const start = new Date();
start.setDate(start.getDate() - 29);
const startDate = ref(isoDate(start));
const ok = (response: any) => response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const formatDuration = (minutes: number) => {
  const value = Number(minutes || 0);
  const days = Math.floor(value / 1440);
  const hours = Math.floor((value % 1440) / 60);
  const mins = value % 60;
  return [days ? `${days} 天` : "", hours ? `${hours} 小時` : "", `${mins} 分`]
    .filter(Boolean).join(" ");
};
const loadTenants = async () => {
  const response: any = await useApi("/fm/requests/start/tenants", {
    method: "POST", body: {},
  });
  if (!ok(response)) return toast.warning(response?.message || "無法載入 Tenant");
  tenants.value = response.value || [];
  if (!tenantId.value) tenantId.value = tenants.value[0]?.tenantId || "";
};
const load = async () => {
  if (!tenantId.value) return;
  loading.value = true;
  try {
    const response: any = await useApi("/fm/operations/reports/summary", {
      method: "POST",
      body: { startDate: startDate.value, endDate: endDate.value },
      headers: { "X-FlowMint-Tenant": tenantId.value },
    });
    if (!ok(response)) {
      report.value = null;
      return toast.warning(response?.message || "無法載入營運報表");
    }
    report.value = response.value;
  } finally {
    loading.value = false;
  }
};
onMounted(async () => { await loadTenants(); await load(); });
</script>

<template>
  <main class="container-fluid py-4">
    <div class="mb-4">
      <h2 class="mb-1">流程營運報表</h2>
      <p class="text-muted mb-0">依 Tenant 與流程開始日期彙整狀態、完成耗時及目前 SLA 風險。</p>
    </div>
    <div class="card border-0 shadow-sm mb-4"><div class="card-body row g-3 align-items-end">
      <div class="col-lg-4"><label class="form-label">Tenant</label><select v-model="tenantId" class="form-select"><option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">{{ tenant.tenantCode }} - {{ tenant.tenantName }}</option></select></div>
      <div class="col-lg-3"><label class="form-label">開始日期</label><input v-model="startDate" type="date" class="form-control" /></div>
      <div class="col-lg-3"><label class="form-label">結束日期</label><input v-model="endDate" type="date" class="form-control" /></div>
      <div class="col-lg-2 d-grid"><button class="btn btn-primary" :disabled="loading" @click="load">產生報表</button></div>
    </div></div>
    <template v-if="report">
      <div class="row g-3 mb-4">
        <div v-for="card in [
          ['流程總數', report.totalProcesses, 'primary'],
          ['執行中', report.runningProcesses, 'warning'],
          ['已完成', report.completedProcesses, 'success'],
          ['逾時待辦', report.overdueTasks, 'danger'],
          ['24 小時內到期', report.dueSoonTasks, 'info'],
          ['平均完成耗時', formatDuration(report.averageCompletedMinutes), 'secondary'],
        ]" :key="String(card[0])" class="col-sm-6 col-xl-2">
          <div class="card border-0 shadow-sm h-100"><div class="card-body"><div class="small text-muted">{{ card[0] }}</div><div class="fs-4 fw-semibold" :class="`text-${card[2]}`">{{ card[1] }}</div></div></div>
        </div>
      </div>
      <div class="card border-0 shadow-sm"><div class="card-header bg-white"><strong>結案狀態分布</strong></div><div class="card-body row g-3 text-center">
        <div v-for="item in [['完成', report.completedProcesses], ['駁回', report.rejectedProcesses], ['取消', report.cancelledProcesses], ['管理員終止', report.terminatedProcesses]]" :key="String(item[0])" class="col-6 col-lg-3"><div class="fs-3 fw-semibold">{{ item[1] }}</div><div class="text-muted">{{ item[0] }}</div></div>
      </div></div>
    </template>
  </main>
</template>
