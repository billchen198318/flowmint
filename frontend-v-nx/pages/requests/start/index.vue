<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import "vue3-toastify/dist/index.css";
import { useBaseStore } from "@/store/baseStore";
import type { ProcessStartCatalogItem, ProcessStartCategory } from "@/types/processStart";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const baseStore = useBaseStore();
const {
  tenants,
  tenantId,
  applicants,
  applicantAccount,
  processes,
  loading,
  errorMessage,
  initialize,
  loadCatalog,
} = useProcessStartCatalog(baseStore.userId);
const keyword = ref("");
const selectedCategory = ref("ALL");

const categories = computed<ProcessStartCategory[]>(() => {
  const query = keyword.value.trim().toLocaleLowerCase("zh-TW");
  const grouped = new Map<string, ProcessStartCatalogItem[]>();
  processes.value.filter((process) => !query
    || process.processName.toLocaleLowerCase("zh-TW").includes(query)
    || (process.description || "").toLocaleLowerCase("zh-TW").includes(query))
    .forEach((process) => {
      const code = process.categoryCode;
      grouped.set(code, [...(grouped.get(code) || []), process]);
    });
  return [...grouped.entries()].map(([code, items]) => ({
    code,
    label: items[0]?.categoryLabel || code,
    icon: `bi-${(items[0]?.categoryIcon || "grid").replace(/^bi-/, "")}`,
    processes: items.sort((a, b) => a.processSortOrder - b.processSortOrder
      || a.processName.localeCompare(b.processName, "zh-TW")),
  })).sort((a, b) => (a.processes[0]?.categorySortOrder || 999)
    - (b.processes[0]?.categorySortOrder || 999));
});
const visibleCategories = computed(() => selectedCategory.value === "ALL"
  ? categories.value
  : categories.value.filter((category) => category.code === selectedCategory.value));
const processLink = (processDefId: string) => ({
  path: `/requests/start/${encodeURIComponent(processDefId)}`,
  query: { tenant: tenantId.value, applicant: applicantAccount.value.trim() },
});
const initializeCatalog = () => initialize(
  String(route.query.tenant || tenantId.value),
  String(route.query.applicant || applicantAccount.value || baseStore.userId),
);

watch(processes, () => {
  selectedCategory.value = "ALL";
});
onMounted(initializeCatalog);
</script>

<template>
  <div class="container-fluid start-center-page">
    <nav aria-label="麵包屑" class="mb-3"><NuxtLink to="/workspace" class="text-decoration-none">工作台</NuxtLink><i class="bi bi-chevron-right mx-2 small"></i><span>申請中心</span></nav>
    <header class="start-center-header mb-4">
      <div><div class="eyebrow">APPLICATION CENTER</div><h1 class="h2 mb-2">我要申請</h1><p class="mb-0 text-secondary">選擇申請類別與表單，開始新的流程。</p></div>
      <div class="context-panel">
        <label class="form-label small" for="start-tenant">公司</label>
        <select id="start-tenant" v-model="tenantId" class="form-select"><option v-for="tenant in tenants" :key="tenant.tenantId" :value="tenant.tenantId">{{ tenant.tenantName }}（{{ tenant.tenantCode }}）</option></select>
        <label class="form-label small mt-3" for="start-applicant">申請人</label>
        <select id="start-applicant" v-model="applicantAccount" class="form-select" @change="loadCatalog">
          <option v-for="applicant in applicants" :key="applicant.account" :value="applicant.account">
            {{ applicant.displayName }}（{{ applicant.account }}）{{ applicant.primaryOrgUnitName ? ` · ${applicant.primaryOrgUnitName}` : '' }}{{ applicant.self ? ' · 本人' : ' · 代理' }}
          </option>
        </select>
      </div>
    </header>
    <div class="search-row mb-3"><div class="input-group search-box"><span class="input-group-text bg-white"><i class="bi bi-search"></i></span><input v-model="keyword" class="form-control border-start-0" placeholder="搜尋申請名稱或說明"></div></div>
    <div class="category-tabs mb-4" role="tablist" aria-label="申請類別">
      <button type="button" :class="['category-tab', { active: selectedCategory === 'ALL' }]" @click="selectedCategory = 'ALL'">全部 <span>{{ processes.length }}</span></button>
      <button v-for="category in categories" :key="category.code" type="button" :class="['category-tab', { active: selectedCategory === category.code }]" @click="selectedCategory = category.code"><i :class="['bi', category.icon]"></i>{{ category.label }} <span>{{ category.processes.length }}</span></button>
    </div>
    <div v-if="loading" class="text-center py-5 text-secondary"><span class="spinner-border spinner-border-sm me-2"></span>載入可發起流程</div>
    <div v-else-if="errorMessage" class="error-state">
      <i class="bi bi-exclamation-triangle"></i>
      <h2 class="h5">無法載入申請中心</h2>
      <p class="text-secondary">{{ errorMessage }}</p>
      <button type="button" class="btn btn-outline-primary" @click="initializeCatalog">重新載入</button>
    </div>
    <div v-else-if="visibleCategories.length">
      <section v-for="category in visibleCategories" :key="category.code" class="mb-5">
        <div class="category-heading"><span><i :class="['bi', category.icon]"></i></span><div><h2 class="h5 mb-0">{{ category.label }}</h2><small class="text-secondary">{{ category.processes.length }} 個可發起流程</small></div></div>
        <div class="row g-3 mt-1"><div v-for="process in category.processes" :key="process.processDefId" class="col-12 col-md-6 col-xl-4"><NuxtLink :to="processLink(process.processDefId)" class="process-card"><span class="process-icon"><i :class="['bi', category.icon]"></i></span><span class="flex-grow-1 min-width-0"><strong class="d-block">{{ process.processName }}</strong><small class="description">{{ process.description || '點選後填寫申請內容' }}</small><span class="version">v{{ process.versionNo }}</span></span><i class="bi bi-arrow-right process-arrow"></i></NuxtLink></div></div>
      </section>
    </div>
    <div v-else class="empty-state"><i class="bi bi-grid"></i><h2 class="h5">目前沒有符合的申請流程</h2><p class="text-secondary">請確認公司、申請人或搜尋條件。</p></div>
  </div>
</template>

<style scoped>
.start-center-page{max-width:1500px;padding-bottom:4rem;color:#172033}.start-center-header{display:flex;justify-content:space-between;align-items:flex-start;gap:2rem;padding:1.75rem;border:1px solid #e3e9f2;border-radius:1.25rem;background:linear-gradient(135deg,#fff 0%,#f4f8ff 60%,#eef8f5 100%)}.eyebrow{margin-bottom:.45rem;color:#4263eb;font-size:.72rem;font-weight:800;letter-spacing:.14em}.context-panel{width:min(100%,360px);padding:1rem;border-radius:1rem;background:rgba(255,255,255,.88);box-shadow:0 8px 24px rgba(31,45,61,.07)}.search-box{max-width:620px}.category-tabs{display:flex;gap:.6rem;overflow-x:auto;padding-bottom:.25rem}.category-tab{display:flex;align-items:center;gap:.4rem;white-space:nowrap;padding:.55rem .85rem;border:1px solid #dfe5ee;border-radius:999px;background:#fff;color:#566278}.category-tab span{font-size:.72rem}.category-tab.active{border-color:#4263eb;background:#4263eb;color:#fff}.category-heading{display:flex;align-items:center;gap:.75rem}.category-heading>span{display:grid;place-items:center;width:42px;height:42px;border-radius:.8rem;background:#eaf0ff;color:#3451b2}.process-card{display:flex;align-items:center;gap:1rem;height:100%;padding:1.15rem;border:1px solid #e1e7ef;border-radius:1rem;background:#fff;color:inherit;text-decoration:none;box-shadow:0 7px 22px rgba(35,49,72,.05);transition:.18s ease}.process-card:hover{border-color:#aebef2;transform:translateY(-2px);box-shadow:0 12px 28px rgba(35,49,72,.1)}.process-icon{display:grid;place-items:center;flex:0 0 auto;width:48px;height:48px;border-radius:.85rem;background:#f0f4ff;color:#4263eb;font-size:1.2rem}.description{display:block;margin-top:.2rem;color:#778296}.version{display:inline-block;margin-top:.5rem;padding:.1rem .4rem;border-radius:999px;background:#f0f2f6;color:#667287;font-size:.68rem}.process-arrow{color:#8b96a8}.min-width-0{min-width:0}.empty-state,.error-state{text-align:center;padding:5rem 1rem}.empty-state>i,.error-state>i{display:block;margin-bottom:1rem;color:#9aa5b5;font-size:2.5rem}.error-state{border:1px solid #f0d7a6;border-radius:1rem;background:#fffaf0}.error-state>i{color:#b7791f}@media(max-width:767.98px){.start-center-header{flex-direction:column}.context-panel{width:100%}}
</style>
