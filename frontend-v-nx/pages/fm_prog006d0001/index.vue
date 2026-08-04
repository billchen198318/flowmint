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
    { label: "連線池代碼", field: "poolCode" },
    { label: "連線池名稱", field: "poolName" },
    { label: "資料庫", field: "dbType" },
    { label: "帳號", field: "username" },
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
      field: { tenantId: store.queryParam.tenantId },
      pageOf: {
        select: store.gridConfig.page,
        showRow: store.gridConfig.row,
      },
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      setConfigTotal(store.gridConfig, 0);
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "查詢資料來源失敗。"),
      );
      return;
    }
    rows.value = response.data.value || [];
    setConfigTotal(store.gridConfig, response.data.pageOf?.countSize || 0);
  } catch (error: unknown) {
    setConfigTotal(store.gridConfig, 0);
    const message =
      error instanceof Error ? error.message : "查詢資料來源失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};

const clear = () => {
  store.queryParam.tenantId = "";
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};

onMounted(async () => {
  try {
    const response = await post("/tenant-options");
    tenants.value = response.data?.value || [];
  } catch (error: unknown) {
    const message =
      error instanceof Error ? error.message : "載入 Tenant 失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  }
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="管理 FlowMint 可使用的外部資料庫連線池。密碼不會回傳畫面。"
    createFlag="Y"
    refreshFlag="Y"
    queryFieldShowSwitchFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
    @queryFieldShowSwitcMethod="show = !show"
  />
  <HiddenQueryFieldAlertInfo
    :dataSource="rows"
    :queryFieldShowFlag="show"
  />
  <div v-show="show" class="card mb-3">
    <div class="card-body row g-2">
      <div class="col-md-5">
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">請選擇 Tenant</option>
          <option
            v-for="item in tenants"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-3">
        <button
          class="btn btn-primary"
          @click="setConfigPage(store.gridConfig, 1); query()"
        >
          查詢
        </button>
        <button class="btn btn-outline-secondary ms-2" @click="clear">
          清除
        </button>
      </div>
    </div>
  </div>
  <GridPagination
    :progId="PageConstants.QueryId"
    :gridConfig="store.gridConfig"
    :changePageSelectMethod="
      (page: number) => {
        setConfigPage(store.gridConfig, page);
        query();
      }
    "
    :changeGridConfigRowMethod="
      (row: number) => {
        setConfigRow(store.gridConfig, row);
        query();
      }
    "
  />
  <Grid
    :progId="PageConstants.QueryId"
    :dataSource="rows"
    :config="store.gridConfig"
  />
</template>
