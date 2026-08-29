<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "./config";

definePageMeta({ middleware: ["auth"] });

const router = useRouter();
const { showLoading, hideLoading } = useSwalLoading();
const specification = ref<any>(null);
const selectedPath = ref("");
const externalPrefix = "/api/fm/external/v1";

const endpoints = computed(() =>
  Object.entries(specification.value?.paths || {})
    .filter(([path, value]: [string, any]) =>
      path.startsWith(externalPrefix) && Boolean(value?.post),
    )
    .map(([path, value]: [string, any]) => ({ path, operation: value.post })),
);
const selected = computed(() =>
  endpoints.value.find((item) => item.path === selectedPath.value),
);
const schemas = computed(() => specification.value?.components?.schemas || {});
const backendBase = String(import.meta.env.VITE_API_URL || "").replace(/\/api\/?$/, "");

const filteredSpecification = () => ({
  ...specification.value,
  openapi: "3.1.0",
  info: {
    ...(specification.value?.info || {}),
    title: "FlowMint External System API",
    version: "v1",
  },
  paths: Object.fromEntries(
    endpoints.value.map((item) => [item.path, { post: item.operation }]),
  ),
});
const downloadOpenApi = () => {
  const blob = new Blob([JSON.stringify(filteredSpecification(), null, 2)], {
    type: "application/json;charset=utf-8",
  });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = "flowmint-external-api-v1.openapi.json";
  anchor.click();
  URL.revokeObjectURL(url);
};
const copyCurl = async () => {
  if (!selected.value) return;
  const command = [
    `curl -X POST '${window.location.origin}${selected.value.path}'`,
    "  -H 'Authorization: Bearer {YOUR_API_KEY}'",
    "  -H 'Content-Type: application/json'",
    "  -H 'X-Request-Id: {YOUR_REQUEST_ID}'",
    "  --data '{\"requestTime\":\"2026-08-29T12:00:00+08:00\",\"data\":{}}'",
  ].join(" \\\n");
  await navigator.clipboard.writeText(command);
  toast.success("cURL 已複製，API Key 保留為安全 placeholder。");
};
const load = async () => {
  showLoading();
  try {
    const access = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/access",
      {},
    );
    if (access.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(access.data?.message || "無 API 說明查詢權限。"));
      await router.push("/");
      return;
    }
    const response = await getAxiosInstance().get(backendBase + "/v3/api-docs");
    specification.value = response.data;
    selectedPath.value = endpoints.value[0]?.path || "";
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "載入 OpenAPI 契約失敗。",
    ));
  } finally {
    hideLoading();
  }
};

onMounted(load);
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="外部系統 API 說明與 OpenAPI 3.1 契約。"
    backFlag="Y"
    refreshFlag="Y"
    @backMethod="router.back()"
    @refreshMethod="load"
  />

  <div class="card mb-3">
    <div class="card-header d-flex justify-content-between align-items-center">
      <strong>連線規格</strong>
      <button type="button" class="btn btn-outline-primary btn-sm" @click="downloadOpenApi">
        <i class="bi bi-download"></i> 下載 OpenAPI 3.1 JSON
      </button>
    </div>
    <div class="card-body">
      <dl class="row mb-0">
        <dt class="col-md-3">Base URL／版本</dt><dd class="col-md-9"><code>/api/fm/external/v1</code>／v1</dd>
        <dt class="col-md-3">認證</dt><dd class="col-md-9"><code>Authorization: Bearer {YOUR_API_KEY}</code></dd>
        <dt class="col-md-3">共同 Header</dt><dd class="col-md-9"><code>Content-Type: application/json</code>、選用 <code>X-Request-Id</code>；發單另用 <code>Idempotency-Key</code></dd>
        <dt class="col-md-3">編碼／時間</dt><dd class="col-md-9">UTF-8；時間使用 ISO-8601 並附 offset，建議 Asia/Taipei（UTC+08:00）。</dd>
        <dt class="col-md-3">安全與重送</dt><dd class="col-md-9">API Key 不得放入 URL 或 Log。Timeout 後僅能使用相同 Idempotency-Key 重送發單；撤銷後立即失效。</dd>
        <dt class="col-md-3">配額</dt><dd class="col-md-9">依 Tenant、Client、Key 與 Endpoint 套用分鐘／每日限制；429 後應退避重試。</dd>
      </dl>
    </div>
  </div>

  <div class="row g-3">
    <div class="col-lg-4">
      <div class="card h-100">
        <div class="card-header"><strong>POST API（{{ endpoints.length }}）</strong></div>
        <div class="list-group list-group-flush endpoint-list">
          <button
            v-for="item in endpoints"
            :key="item.path"
            type="button"
            :class="['list-group-item', 'list-group-item-action', selectedPath === item.path ? 'active' : '']"
            @click="selectedPath = item.path"
          >
            <span class="badge text-bg-success me-2">POST</span>{{ item.path }}
          </button>
        </div>
      </div>
    </div>
    <div class="col-lg-8">
      <div v-if="selected" class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <strong>{{ selected.operation.summary || selected.path }}</strong>
          <button type="button" class="btn btn-outline-secondary btn-sm" @click="copyCurl">
            <i class="bi bi-clipboard"></i> 複製 cURL
          </button>
        </div>
        <div class="card-body">
          <p><code>POST {{ selected.path }}</code></p>
          <p>{{ selected.operation.description || "用途與欄位定義依下方 OpenAPI 契約。" }}</p>
          <h6>Request</h6>
          <pre>{{ JSON.stringify(selected.operation.requestBody || {}, null, 2) }}</pre>
          <h6>Response／錯誤狀態</h6>
          <pre>{{ JSON.stringify(selected.operation.responses || {}, null, 2) }}</pre>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><strong>Request／Response DTO Schemas</strong></div>
        <div class="card-body"><pre>{{ JSON.stringify(schemas, null, 2) }}</pre></div>
      </div>
    </div>
  </div>

  <div class="alert alert-secondary mt-3">
    錯誤回應共同包含 <code>success=false</code>、<code>requestId</code>、<code>error.code</code>、
    <code>error.message</code> 與 <code>fieldErrors</code>。可能狀態為 400、401、403、404、409、422、429、500。
    Form Template API 回傳的 Form.io schema 是 Submission JSON 的唯一欄位契約；不得傳送 schema 未定義或 server-managed 欄位。
  </div>
</template>

<style scoped>
.endpoint-list { max-height: 70vh; overflow: auto; }
.endpoint-list button { font-family: monospace; font-size: 0.82rem; text-align: left; }
pre { max-height: 38rem; overflow: auto; padding: 1rem; border-radius: 0.375rem; background: #f6f8fa; font-size: 0.78rem; }
</style>
