<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";

import Grid from "@/components/Grid.vue";
import GridPagination from "@/components/GridPagination.vue";
import HiddenQueryFieldAlertInfo from "@/components/HiddenQueryFieldAlertInfo.vue";
import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import {
  getGridConfig,
  setConfigPage,
  setConfigRow,
  setConfigTotal,
} from "@/components/GridHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "./config";
import { useStore } from "./QueryPageStore";

definePageMeta({ middleware: ["auth"] });

const router = useRouter();
const store = useStore();
const rows = ref<any[]>([]);
const tenants = ref<any[]>([]);
const show = ref(true);
const { showLoading, hideLoading } = useSwalLoading();
const post = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

store.gridConfig = getGridConfig(
  "oid",
  [
    {
      method: (oid: string) =>
        router.push(PageConstants.frontendNamespace + "/edit/" + oid),
      icon: "pen",
      type: "edit",
      memo: "編輯",
      class: "btn btn-info btn-sm",
    },
  ],
  [
    { label: "", field: "oid" },
    { label: "Tenant", field: "tenantId" },
    { label: "Provider 代碼", field: "providerCode" },
    { label: "顯示名稱", field: "displayName" },
    { label: "類型", field: "providerType" },
    { label: "Model", field: "modelId" },
    { label: "預設", field: "defaultFlag" },
    { label: "狀態", field: "status" },
  ],
);

const query = async () => {
  rows.value = [];
  if (!store.queryParam.tenantId) {
    setConfigTotal(store.gridConfig, 0);
    toast.warning(escapeQifuHtmlMsg("請先選擇 Tenant。"));
    return;
  }
  showLoading();
  try {
    const response = await post("/findPage", {
      field: {
        tenantId: store.queryParam.tenantId,
        status: store.queryParam.status,
      },
      pageOf: {
        select: store.gridConfig.page,
        showRow: store.gridConfig.row,
      },
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      setConfigTotal(store.gridConfig, 0);
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "查詢 AI Provider 失敗。"));
      return;
    }
    rows.value = response.data.value || [];
    setConfigTotal(store.gridConfig, response.data.pageOf?.countSize || 0);
  } catch (error: unknown) {
    setConfigTotal(store.gridConfig, 0);
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "查詢 AI Provider 失敗。",
    ));
  } finally {
    hideLoading();
  }
};

const clear = () => {
  store.queryParam = { tenantId: "", status: "" };
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};

onMounted(async () => {
  try {
    tenants.value = (await post("/tenant-options")).data?.value || [];
    if (store.queryParam.tenantId) await query();
  } catch (error: unknown) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "載入 Tenant 選項失敗。",
    ));
  }
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="管理各 Tenant 的 AI Provider、Model 與加密 API Key。"
    createFlag="Y"
    refreshFlag="Y"
    queryFieldShowSwitchFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
    @queryFieldShowSwitcMethod="show = !show"
  />
  <HiddenQueryFieldAlertInfo :dataSource="rows" :queryFieldShowFlag="show" />
  <div v-show="show" class="card mb-3">
    <div class="card-body row g-2">
      <div class="col-md-5">
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">請選擇 Tenant</option>
          <option v-for="item in tenants" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-3">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="ACTIVE">啟用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="col-md-4">
        <button class="btn btn-primary" @click="setConfigPage(store.gridConfig, 1); query()">
          查詢
        </button>
        <button class="btn btn-outline-secondary ms-2" @click="clear">清除</button>
      </div>
    </div>
  </div>
  <GridPagination
    :progId="PageConstants.QueryId"
    :gridConfig="store.gridConfig"
    :changePageSelectMethod="(page: number) => { setConfigPage(store.gridConfig, page); query(); }"
    :changeGridConfigRowMethod="(row: number) => { setConfigRow(store.gridConfig, row); query(); }"
  />
  <Grid :progId="PageConstants.QueryId" :dataSource="rows" :config="store.gridConfig" />
</template>
