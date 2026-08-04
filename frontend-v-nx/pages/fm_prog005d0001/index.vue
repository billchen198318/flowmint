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
const show = ref(true);
const router = useRouter();
const store = useStore();
const rows = ref<any[]>([]);
const tenants = ref<any[]>([]);
const { showLoading, hideLoading } = useSwalLoading();
const post = (path: string, body: any = {}) =>
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
    { label: "表單代碼", field: "formCode" },
    { label: "表單名稱", field: "formName" },
    { label: "目前版本", field: "currentVersionNo" },
    { label: "狀態", field: "status" },
  ],
);
const query = async () => {
  showLoading();
  rows.value = [];
  try {
    const response = await post("/findPage", {
      field: { ...store.queryParam },
      pageOf: { select: store.gridConfig.page, showRow: store.gridConfig.row },
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      setConfigTotal(store.gridConfig, 0);
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "查詢表單資料失敗。"),
      );
      return;
    }
    rows.value = response.data.value || [];
    setConfigTotal(store.gridConfig, response.data.pageOf?.countSize || 0);
  } catch (error: unknown) {
    setConfigTotal(store.gridConfig, 0);
    const message =
      error instanceof Error ? error.message : "查詢表單資料失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};
const clear = () => {
  store.queryParam = { tenantId: "", formCode: "", formName: "", status: "" };
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
  setConfigPage(store.gridConfig, 1);
};
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  if (!store.queryParam.tenantId && tenants.value.length === 1)
    store.queryParam.tenantId = tenants.value[0].value;
  await query();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="建立及發布可供流程 UserTask 綁定的表單版本。發布版本不可修改；需要調整時請建立新版本。"
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
      <div class="col-md-2">
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">全部 Tenant</option>
          <option v-for="item in tenants" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.formCode"
          class="form-control"
          placeholder="表單代碼"
        />
      </div>
      <div class="col-md-3">
        <input
          v-model="store.queryParam.formName"
          class="form-control"
          placeholder="表單名稱"
        />
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">已發布</option>
          <option value="INACTIVE">已停用</option>
        </select>
      </div>
      <div class="col-md-3 d-flex gap-2">
        <button
          class="btn btn-primary"
          @click="
            setConfigPage(store.gridConfig, 1);
            query();
          "
        >
          <i class="bi bi-search"></i> 查詢
        </button>
        <button class="btn btn-outline-secondary" @click="clear">
          <i class="bi bi-eraser"></i> 清除
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
