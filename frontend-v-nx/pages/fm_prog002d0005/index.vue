<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import Toolbar from "@/components/Toolbar.vue";
import Grid from "@/components/Grid.vue";
import GridPagination from "@/components/GridPagination.vue";
import HiddenQueryFieldAlertInfo from "@/components/HiddenQueryFieldAlertInfo.vue";
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
const show = ref(true);
const router = useRouter();
const store = useStore();
const rows = ref<any[]>([]);
const tenants = ref<any[]>([]);
const units = ref<any[]>([]);
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const typeLabels: Record<string, string> = {
  HEAD: "主要主管",
  DEPUTY_HEAD: "副主管",
  ACTING_HEAD: "代理主管",
};

store.gridConfig = getGridConfig(
  "oid",
  [
    {
      method: (id: any) =>
        router.push(PageConstants.frontendNamespace + "/edit/" + id),
      icon: "pen",
      type: "edit",
      memo: "編輯",
      class: "btn btn-info btn-sm",
    },
  ],
  [
    { label: "", field: "oid" },
    { label: "Tenant", field: "tenantId" },
    { label: "部門", field: "orgUnitLabel" },
    { label: "主管員工", field: "employeeLabel" },
    { label: "主管類型", field: "headTypeLabel" },
    { label: "優先序", field: "priority" },
    { label: "狀態", field: "status" },
  ],
);

const loadUnits = async () => {
  units.value = store.queryParam.tenantId
    ? (await post("/org-unit-options", { tenantId: store.queryParam.tenantId }))
        .data?.value || []
    : [];
};
watch(
  () => store.queryParam.tenantId,
  async () => {
    store.queryParam.orgUnitId = "";
    await loadUnits();
  },
);
const query = async () => {
  const response = await post("/findPage", {
    field: { ...store.queryParam },
    pageOf: { select: store.gridConfig.page, showRow: store.gridConfig.row },
  });
  rows.value = (response.data?.value || []).map((value: any) => ({
    ...value,
    headTypeLabel: typeLabels[value.headType] || value.headType,
  }));
  setConfigTotal(store.gridConfig, response.data?.pageOf?.countSize || 0);
};
const clear = async () => {
  store.queryParam = { tenantId: "", orgUnitId: "", headType: "", status: "" };
  units.value = [];
  rows.value = [];
};
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  await loadUnits();
  await query();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="設定各部門的主要主管、副主管與代理主管。員工送出申請時，系統會自動找到其所屬部門的主要主管進行簽核；主管請假或暫時無法處理時，可另外設定代理主管。可選人員必須先在員工資料管理中建立該部門的有效任職。"
    createFlag="Y"
    refreshFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
    queryFieldShowSwitchFlag="Y"
    @queryFieldShowSwitcMethod="show = !show"
  />
  <HiddenQueryFieldAlertInfo :dataSource="rows" :queryFieldShowFlag="show" />
  <div v-show="show" class="card mb-3">
    <div class="card-body row g-2">
      <div class="col-md-3">
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">全部 Tenant</option>
          <option v-for="item in tenants" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-3">
        <select v-model="store.queryParam.orgUnitId" class="form-select">
          <option value="">全部部門</option>
          <option v-for="item in units" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.headType" class="form-select">
          <option value="">全部主管類型</option>
          <option value="HEAD">主要主管</option>
          <option value="DEPUTY_HEAD">副主管</option>
          <option value="ACTING_HEAD">代理主管</option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="ACTIVE">啟用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="col-md-2">
        <button
          class="btn btn-primary"
          @click="
            setConfigPage(store.gridConfig, 1);
            query();
          "
        >
          查詢
        </button>
        <button
          type="button"
          class="btn btn-outline-secondary ms-2"
          @click="clear"
        >
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
