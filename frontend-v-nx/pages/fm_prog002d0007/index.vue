<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { toast } from "vue3-toastify";
import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "./config";

definePageMeta({ middleware: ["auth"] });

interface TenantOption {
  tenantId: string;
  tenantCode: string;
  tenantName: string;
}

interface HealthIssue {
  section: string;
  code: string;
  severity: "ERROR" | "WARNING";
  title: string;
  subjectId: string;
  subjectLabel: string;
  description: string;
  impact: string;
  evidence: string;
  recommendation: string;
  targetProgramId: string;
  path: string[];
}

interface HealthResult {
  tenantId: string;
  checkedAt: string;
  headIssues: HealthIssue[];
  managerIssues: HealthIssue[];
  organizationIssues: HealthIssue[];
  delegationIssues: HealthIssue[];
}

interface HealthSection {
  key: keyof Pick<
    HealthResult,
    | "headIssues"
    | "managerIssues"
    | "organizationIssues"
    | "delegationIssues"
  >;
  title: string;
  description: string;
}

const tenants = ref<TenantOption[]>([]);
const tenantId = ref("");
const result = ref<HealthResult | null>(null);
const keyword = ref("");
const { showLoading, hideLoading } = useSwalLoading();
const sections: HealthSection[] = [
  {
    key: "headIssues",
    title: "部門主管問題",
    description: "檢查缺少主管、多位主管及無效主管任職。",
  },
  {
    key: "managerIssues",
    title: "員工直屬主管問題",
    description: "檢查缺少主管、重複主要任職及主管鏈循環。",
  },
  {
    key: "organizationIssues",
    title: "上級部門與組織樹問題",
    description: "檢查多根、無效父節點及組織樹循環。",
  },
  {
    key: "delegationIssues",
    title: "工作代理問題",
    description: "檢查自我代理、互相代理及代理鏈循環。",
  },
];
const targetRoutes: Record<string, string> = {
  FM_PROG002D0001: "/fm_prog002d0001",
  FM_PROG002D0002: "/fm_prog002d0002",
  FM_PROG002D0005: "/fm_prog002d0005",
  FM_PROG003D0002: "/fm_prog003d0002",
};
const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const allIssues = computed(() =>
  result.value
    ? sections.flatMap((section) => result.value?.[section.key] || [])
    : [],
);
const errorCount = computed(
  () => allIssues.value.filter((issue) => issue.severity === "ERROR").length,
);
const warningCount = computed(
  () => allIssues.value.filter((issue) => issue.severity === "WARNING").length,
);
const formatDate = (value: string | null) =>
  value
    ? new Intl.DateTimeFormat("zh-TW", {
        dateStyle: "medium",
        timeStyle: "medium",
      }).format(new Date(value))
    : "-";
const filteredIssues = (key: HealthSection["key"]) => {
  const values = result.value?.[key] || [];
  const normalized = keyword.value.trim().toLowerCase();
  if (!normalized) return values;
  return values.filter((issue) =>
    [
      issue.title,
      issue.subjectLabel,
      issue.description,
      issue.recommendation,
      issue.code,
    ].some((value) => value?.toLowerCase().includes(normalized)),
  );
};
const count = (key: HealthSection["key"], severity: string) =>
  (result.value?.[key] || []).filter((issue) => issue.severity === severity)
    .length;
const loadTenants = async () => {
  const response: any = await useApi("/fm/requests/start/tenants", {
    method: "POST",
    body: {},
  });
  if (!ok(response)) {
    toast.warning(escapeQifuHtmlMsg(response?.message || "無法載入 Tenant。"));
    return;
  }
  tenants.value = response.value || [];
  tenantId.value = tenants.value[0]?.tenantId || "";
};
const inspect = async () => {
  if (!tenantId.value) {
    result.value = null;
    return;
  }
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/inspect",
      {},
      { headers: { "X-FlowMint-Tenant": tenantId.value } },
    );
    if (!ok(response.data)) {
      result.value = null;
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "組織人員資料檢查失敗。"),
      );
      return;
    }
    result.value = response.data.value;
  } catch (error: unknown) {
    result.value = null;
    const message =
      error instanceof Error ? error.message : "組織人員資料檢查失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};
const openAdjustment = (programId: string) => {
  const route = targetRoutes[programId];
  if (route) window.open(route, "_blank", "noopener,noreferrer");
};

onMounted(async () => {
  await loadTenants();
  await inspect();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="檢查組織、人員主管與工作代理資料。此頁只提供問題說明及調整建議，不會自動修改正式資料。"
    refreshFlag="Y"
    @refreshMethod="inspect"
  />

  <div class="container-fluid py-3">
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body row g-3 align-items-end">
        <div class="col-lg-5">
          <label class="form-label">Tenant</label>
          <select v-model="tenantId" class="form-select" @change="inspect">
            <option
              v-for="tenant in tenants"
              :key="tenant.tenantId"
              :value="tenant.tenantId"
            >
              {{ tenant.tenantCode }} - {{ tenant.tenantName }}
            </option>
          </select>
        </div>
        <div class="col-lg-4">
          <label class="form-label">區塊內篩選</label>
          <input
            v-model="keyword"
            class="form-control"
            placeholder="部門、員工、問題或代碼"
          />
        </div>
        <div class="col-lg-3 d-grid">
          <button class="btn btn-primary" @click="inspect">重新檢查</button>
        </div>
      </div>
    </div>

    <div class="d-flex flex-wrap gap-2 align-items-center mb-3">
      <span class="badge text-bg-danger fs-6">ERROR {{ errorCount }}</span>
      <span class="badge text-bg-warning fs-6"
        >WARNING {{ warningCount }}</span
      >
      <span class="text-muted ms-auto small">
        檢查時間：{{ formatDate(result?.checkedAt || null) }}
      </span>
    </div>

    <section
      v-for="section in sections"
      :key="section.key"
      class="card border-0 shadow-sm mb-4"
    >
      <div class="card-header bg-white py-3">
        <div class="d-flex flex-wrap justify-content-between gap-2">
          <div>
            <h5 class="mb-1">{{ section.title }}</h5>
            <div class="small text-muted">{{ section.description }}</div>
          </div>
          <div class="d-flex gap-2 align-items-center">
            <span class="badge text-bg-danger"
              >ERROR {{ count(section.key, "ERROR") }}</span
            >
            <span class="badge text-bg-warning"
              >WARNING {{ count(section.key, "WARNING") }}</span
            >
          </div>
        </div>
      </div>
      <div class="card-body">
        <div
          v-if="!filteredIssues(section.key).length"
          class="alert alert-success mb-0"
        >
          檢查正常
        </div>
        <article
          v-for="issue in filteredIssues(section.key)"
          :key="issue.code + ':' + issue.subjectId"
          class="issue border rounded p-3 mb-3"
        >
          <div class="d-flex flex-wrap justify-content-between gap-2 mb-2">
            <div>
              <span
                :class="[
                  'badge me-2',
                  issue.severity === 'ERROR'
                    ? 'text-bg-danger'
                    : 'text-bg-warning',
                ]"
              >
                {{ issue.severity }}
              </span>
              <strong>{{ issue.title }}</strong>
              <span class="text-muted ms-2">{{ issue.subjectLabel }}</span>
            </div>
            <code>{{ issue.code }}</code>
          </div>
          <dl class="row small mb-3">
            <dt class="col-md-2">問題說明</dt>
            <dd class="col-md-10">{{ issue.description }}</dd>
            <dt class="col-md-2">可能影響</dt>
            <dd class="col-md-10">{{ issue.impact }}</dd>
            <dt class="col-md-2">判定依據</dt>
            <dd class="col-md-10">{{ issue.evidence }}</dd>
            <dt class="col-md-2">建議處理</dt>
            <dd class="col-md-10 mb-0">{{ issue.recommendation }}</dd>
          </dl>
          <div v-if="issue.path?.length" class="path-box mb-3">
            {{ issue.path.join(" → ") }}
          </div>
          <button
            class="btn btn-sm btn-outline-primary"
            @click="openAdjustment(issue.targetProgramId)"
          >
            前往調整
          </button>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.issue:last-child {
  margin-bottom: 0 !important;
}
.path-box {
  padding: 0.65rem 0.75rem;
  border-radius: 0.4rem;
  background: #f6f8fa;
  font-family: monospace;
  font-size: 0.85rem;
  word-break: break-word;
}
</style>
