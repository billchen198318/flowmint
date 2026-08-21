<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import { useBaseStore } from "@/store/baseStore";

definePageMeta({ layout: "default", middleware: ["auth"] });

interface RuntimeTenant {
  tenantId: string;
  tenantCode: string;
  tenantName: string;
  defaultTenant?: boolean;
}

const baseStore = useBaseStore();
const tenants = ref<RuntimeTenant[]>([]);
const tenantId = ref("");
const inbox = ref<any[]>([]);
const notifications = ref<any[]>([]);
const unreadNotificationCount = ref(0);
const myRequests = ref<any[]>([]);
const processCount = ref(0);
const loading = ref(false);

const runningRequests = computed(() => myRequests.value.filter((item) => item.instanceStatus === "RUNNING"));
const completedRequests = computed(() => myRequests.value.filter((item) => item.instanceStatus === "COMPLETED"));
const tenantHeaders = () => ({ "X-FlowMint-Tenant": tenantId.value });
const ok = (response: any) => response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const runtimePost = (path: string, body: any = {}, headers: any = {}) =>
  useApi(`/fm/requests${path}`, { method: "POST", body, headers });
const notificationPost = (path: string, body: any = {}) =>
  useApi(`/fm/notifications${path}`, { method: "POST", body, headers: tenantHeaders() });
const showError = (response: any, fallback: string) => toast.warning(response?.message || fallback);
const formatDate = (value: string | null) => value
  ? new Intl.DateTimeFormat("zh-TW", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
  : "—";

const loadTenants = async () => {
  const response: any = await runtimePost("/start/tenants");
  if (!ok(response)) return showError(response, "無法載入公司清單");
  tenants.value = response.value || [];
  const preferred = tenants.value.find((item) => item.defaultTenant);
  tenantId.value = preferred?.tenantId || tenants.value[0]?.tenantId || "";
};
const loadWorkspace = async () => {
  if (!tenantId.value) return;
  loading.value = true;
  try {
    const [inboxResponse, requestResponse, notificationResponse, catalogResponse]: any[] = await Promise.all([
      runtimePost("/tasks/inbox", {}, tenantHeaders()),
      runtimePost("/mine", {}, tenantHeaders()),
      notificationPost("/inbox"),
      runtimePost("/start/catalog", { applicantAccount: baseStore.userId }, tenantHeaders()),
    ]);
    if (ok(inboxResponse)) inbox.value = inboxResponse.value || [];
    if (ok(requestResponse)) myRequests.value = requestResponse.value || [];
    if (ok(notificationResponse)) {
      notifications.value = notificationResponse.value?.notifications || [];
      unreadNotificationCount.value = notificationResponse.value?.unreadCount || 0;
    }
    processCount.value = ok(catalogResponse) ? (catalogResponse.value || []).length : 0;
  } finally {
    loading.value = false;
  }
};
const markNotificationRead = async (notification: any) => {
  if (notification.readDate) return;
  const response: any = await notificationPost("/read", { notificationId: notification.notificationId });
  if (!ok(response)) return showError(response, "無法更新通知");
  await loadWorkspace();
};

watch(tenantId, loadWorkspace);
onMounted(loadTenants);
</script>

<template>
  <div class="container-fluid workspace-page">
    <section class="workspace-hero mb-4">
      <div>
        <div class="eyebrow">FLOWMINT WORKSPACE</div>
        <h1 class="h2 mb-2">我的工作台</h1>
        <p class="mb-0 text-secondary">處理待辦、追蹤申請，快速進入需要完成的工作。</p>
      </div>
      <div class="hero-context">
        <label class="context-label" for="workspace-tenant">目前公司</label>
        <select id="workspace-tenant" v-model="tenantId" class="form-select form-select-sm">
          <option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">
            {{ tenant.tenantName }}（{{ tenant.tenantCode }}）
          </option>
        </select>
        <span>{{ baseStore.userId }}</span>
      </div>
    </section>

    <section class="row g-3 mb-4" aria-label="工作統計">
      <div class="col-6 col-xl-3"><div class="stat-card"><span class="stat-icon tone-primary"><i class="bi bi-inbox"></i></span><span><small>我的待辦</small><strong>{{ inbox.length }}</strong></span></div></div>
      <div class="col-6 col-xl-3"><div class="stat-card"><span class="stat-icon tone-info"><i class="bi bi-bell"></i></span><span><small>未讀通知</small><strong>{{ unreadNotificationCount }}</strong></span></div></div>
      <div class="col-6 col-xl-3"><div class="stat-card"><span class="stat-icon tone-warning"><i class="bi bi-hourglass-split"></i></span><span><small>進行中</small><strong>{{ runningRequests.length }}</strong></span></div></div>
      <div class="col-6 col-xl-3"><div class="stat-card"><span class="stat-icon tone-success"><i class="bi bi-check2-circle"></i></span><span><small>已完成</small><strong>{{ completedRequests.length }}</strong></span></div></div>
    </section>

    <section class="start-banner mb-4">
      <span class="start-banner-icon"><i class="bi bi-grid"></i></span>
      <div class="flex-grow-1">
        <h2 class="h5 mb-1">發起新申請</h2>
        <p class="mb-0 text-secondary">依類別瀏覽你目前可以發起的 {{ processCount }} 個流程。</p>
      </div>
      <NuxtLink :to="{ path: '/requests/start', query: { tenant: tenantId } }" class="btn btn-primary">
        前往申請中心 <i class="bi bi-arrow-right ms-1"></i>
      </NuxtLink>
    </section>

    <div v-if="loading" class="text-center py-3 text-secondary"><span class="spinner-border spinner-border-sm me-2"></span>載入工作台</div>
    <div class="row g-4">
      <div class="col-12 col-xl-7">
        <div class="card workspace-card">
          <div class="card-header workspace-card-header"><strong>我的待辦</strong><span class="count-pill">{{ inbox.length }}</span></div>
          <div v-if="inbox.length" class="list-group list-group-flush">
            <NuxtLink v-for="task in inbox.slice(0, 8)" :key="task.taskId" :to="{ path: `/tasks/${task.taskId}`, query: { tenant: tenantId } }" class="list-group-item list-group-item-action workspace-list-item">
              <span class="list-icon tone-primary"><i class="bi bi-person-check"></i></span><span class="flex-grow-1 min-width-0"><strong class="d-block text-truncate">{{ task.processName }} · {{ task.taskName }}</strong><small class="text-secondary">{{ task.documentNumber || task.businessKey }} · {{ task.applicantAccount }}</small></span><small class="text-secondary d-none d-md-block">{{ formatDate(task.createdDate) }}</small>
            </NuxtLink>
          </div>
          <div v-else class="empty-state"><i class="bi bi-check2-circle"></i><strong>待辦已清空</strong><span>目前沒有需要你處理的工作。</span></div>
        </div>
      </div>
      <div class="col-12 col-xl-5">
        <div class="card workspace-card mb-4">
          <div class="card-header workspace-card-header"><strong>最近申請</strong><span class="count-pill">{{ myRequests.length }}</span></div>
          <div v-if="myRequests.length" class="list-group list-group-flush">
            <NuxtLink v-for="request in myRequests.slice(0, 5)" :key="request.processInstanceId" :to="{ path: `/requests/${request.processInstanceId}`, query: { tenant: tenantId } }" class="list-group-item list-group-item-action workspace-list-item"><span class="flex-grow-1 min-width-0"><strong class="d-block text-truncate">{{ request.processName }}</strong><small class="text-secondary">{{ request.documentNumber || request.businessKey }}</small></span><span class="status-pill">{{ request.instanceStatus }}</span></NuxtLink>
          </div>
          <div v-else class="empty-state"><i class="bi bi-journal-text"></i><strong>尚無申請紀錄</strong></div>
        </div>
        <div class="card workspace-card">
          <div class="card-header workspace-card-header"><strong>最新通知</strong><span class="count-pill">{{ unreadNotificationCount }} 未讀</span></div>
          <div v-if="notifications.length" class="list-group list-group-flush"><button v-for="notification in notifications.slice(0, 5)" :key="notification.notificationId" type="button" class="list-group-item list-group-item-action workspace-list-item text-start" @click="markNotificationRead(notification)"><span :class="['list-icon', notification.readDate ? 'tone-muted' : 'tone-info']"><i class="bi bi-bell"></i></span><span class="min-width-0"><strong class="d-block text-truncate">{{ notification.subject }}</strong><small class="text-secondary">{{ notification.contentText }}</small></span></button></div>
          <div v-else class="empty-state"><i class="bi bi-bell-slash"></i><strong>目前沒有通知</strong></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.workspace-page{max-width:1500px;padding-bottom:3rem;color:#172033}.workspace-hero{display:flex;justify-content:space-between;align-items:center;gap:2rem;padding:1.5rem 1.75rem;border:1px solid #e3e9f2;border-radius:1.25rem;background:linear-gradient(135deg,#fff 0%,#f4f8ff 58%,#edf7f5 100%)}.eyebrow{margin-bottom:.45rem;color:#4263eb;font-size:.72rem;font-weight:800;letter-spacing:.14em}.hero-context{display:grid;min-width:260px;gap:.35rem;padding:.85rem 1rem;border-radius:.9rem;background:rgba(255,255,255,.82);box-shadow:0 8px 24px rgba(31,45,61,.07)}.context-label{color:#788397;font-size:.72rem}.stat-card{display:flex;align-items:center;gap:1rem;height:100%;padding:1.15rem;border:1px solid #e5eaf1;border-radius:1rem;background:#fff;box-shadow:0 8px 26px rgba(35,49,72,.055)}.stat-card small,.stat-card strong{display:block}.stat-card small{color:#778296}.stat-card strong{font-size:1.7rem}.stat-icon,.list-icon{display:grid;place-items:center;flex:0 0 auto;border-radius:.8rem}.stat-icon{width:46px;height:46px;font-size:1.2rem}.list-icon{width:38px;height:38px}.tone-primary{color:#3451b2;background:#eaf0ff}.tone-warning{color:#9a6700;background:#fff3d6}.tone-success{color:#18794e;background:#e9f9ef}.tone-info{color:#087e8b;background:#e4f7f8}.tone-muted{color:#778296;background:#eef1f5}.start-banner{display:flex;align-items:center;gap:1rem;padding:1.25rem 1.4rem;border:1px solid #dce5ff;border-radius:1rem;background:#f7f9ff}.start-banner-icon{display:grid;place-items:center;width:48px;height:48px;border-radius:.85rem;color:#3451b2;background:#e4ebff;font-size:1.25rem}.workspace-card{overflow:hidden;border:1px solid #e5eaf1;border-radius:1rem;box-shadow:0 8px 28px rgba(35,49,72,.055)}.workspace-card-header{display:flex;justify-content:space-between;align-items:center;min-height:64px;padding:.9rem 1.15rem;background:#fff}.count-pill,.status-pill{padding:.2rem .55rem;border-radius:999px;background:#eef2f8;color:#536178;font-size:.75rem}.workspace-list-item{display:flex;align-items:center;gap:.9rem;padding:1rem 1.15rem;border-color:#edf0f5}.min-width-0{min-width:0}.empty-state{display:flex;flex-direction:column;align-items:center;padding:2.5rem 1rem;color:#7a8699}.empty-state i{margin-bottom:.5rem;font-size:2rem}.empty-state strong{color:#4e5b70}.empty-state span{margin-top:.25rem;font-size:.85rem}@media(max-width:767.98px){.workspace-hero,.start-banner{align-items:flex-start;flex-direction:column}.hero-context,.start-banner .btn{width:100%}}
</style>
