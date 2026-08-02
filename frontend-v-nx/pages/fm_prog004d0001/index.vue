<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import Grid from "@/components/Grid.vue";
import GridPagination from "@/components/GridPagination.vue";
import HiddenQueryFieldAlertInfo from "@/components/HiddenQueryFieldAlertInfo.vue";
import Toolbar from "@/components/Toolbar.vue";
import { getAxiosInstance } from "@/components/BaseHelper";
import {
  getGridConfig,
  setConfigPage,
  setConfigRow,
  setConfigTotal,
} from "@/components/GridHelper";
import { PageConstants } from "./config";
import { useStore } from "./QueryPageStore";

definePageMeta({ middleware: ["auth"] });
const router = useRouter();
const store = useStore();
const rows = ref<any[]>([]);
const tenants = ref<any[]>([]);
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
    { label: "流程代碼", field: "processKey" },
    { label: "流程名稱", field: "processName" },
    { label: "分類", field: "category" },
    { label: "目前版本", field: "currentVersionNo" },
    { label: "狀態", field: "status" },
  ],
);
const query = async () => {
  const response = await post("/findPage", {
    field: { ...store.queryParam },
    pageOf: { select: store.gridConfig.page, showRow: store.gridConfig.row },
  });
  rows.value = response.data?.value || [];
  setConfigTotal(store.gridConfig, response.data?.pageOf?.countSize || 0);
};
const clear = () => {
  store.queryParam = {
    tenantId: "",
    processKey: "",
    processName: "",
    status: "",
  };
  rows.value = [];
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
    description="管理流程穩定主檔與版本。流程代碼建立後不可修改；設計內容先儲存為草稿，通過 BPMN 驗證後才可發布。已發布版本保持不可變，後續調整必須建立新版本，避免執行中的流程被覆寫。"
    createFlag="Y"
    refreshFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
  />
  <HiddenQueryFieldAlertInfo />
  <div class="card mb-3">
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
          v-model="store.queryParam.processKey"
          class="form-control"
          placeholder="流程代碼"
        />
      </div>
      <div class="col-md-3">
        <input
          v-model="store.queryParam.processName"
          class="form-control"
          placeholder="流程名稱"
        />
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">已發布</option>
          <option value="INACTIVE">停用</option>
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
          <i class="bi bi-search"></i> 查詢</button
        ><button class="btn btn-outline-secondary" @click="clear">
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
