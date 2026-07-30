<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
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
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "./config";
import { useFmProg001d0001Store } from "./QueryPageStore";
definePageMeta({ middleware: ["auth"] });
const router = useRouter(),
  store = useFmProg001d0001Store(),
  rows = ref<any[]>([]),
  show = ref(true);
const { showLoading, hideLoading } = useSwalLoading();
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
    { label: "Tenant ID", field: "tenantId" },
    { label: "代碼", field: "tenantCode" },
    { label: "名稱", field: "tenantName" },
    { label: "語系", field: "defaultLocale" },
    { label: "時區", field: "defaultTimezone" },
    { label: "狀態", field: "status" },
  ],
);
const query = async () => {
  showLoading();
  try {
    const r = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/findPage",
      {
        field: {
          tenantCodeLike: store.queryParam.tenantCode,
          tenantNameLike: store.queryParam.tenantName,
          status: store.queryParam.status,
        },
        pageOf: {
          select: store.gridConfig.page,
          showRow: store.gridConfig.row,
        },
      },
    );
    if (r.data.success === import.meta.env.VITE_SUCCESS_FLAG) {
      rows.value = r.data.value;
      setConfigTotal(store.gridConfig, r.data.pageOf.countSize);
    } else toast.warning(r.data.message);
  } finally {
    hideLoading();
  }
};
const clear = () => {
  store.queryParam = { tenantCode: "", tenantName: "", status: "" };
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};
const page = (p: number) => {
  setConfigPage(store.gridConfig, p);
  query();
};
const size = (n: number) => {
  setConfigRow(store.gridConfig, n);
  setConfigPage(store.gridConfig, 1);
  query();
};
onMounted(query);
</script>
<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="Tenant 與帳號範圍"
    refreshFlag="Y"
    createFlag="Y"
    queryFieldShowSwitchFlag="Y"
    @refreshMethod="clear"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @queryFieldShowSwitcMethod="show = !show"
  /><HiddenQueryFieldAlertInfo :dataSource="rows" :queryFieldShowFlag="show" />
  <div v-show="show" class="card mb-3">
    <div class="card-body row g-3">
      <div class="col-md-4">
        <input
          v-model="store.queryParam.tenantCode"
          class="form-control"
          placeholder="Tenant 代碼"
        />
      </div>
      <div class="col-md-4">
        <input
          v-model="store.queryParam.tenantName"
          class="form-control"
          placeholder="Tenant 名稱"
        />
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="ACTIVE">啟用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="col-md-2">
        <button class="btn btn-primary me-2" @click="query">查詢</button
        ><button class="btn btn-outline-secondary" @click="clear">清除</button>
      </div>
    </div>
  </div>
  <GridPagination
    :progId="PageConstants.QueryId"
    :gridConfig="store.gridConfig"
    :changePageSelectMethod="page"
    :changeGridConfigRowMethod="size"
  /><Grid
    :progId="PageConstants.QueryId"
    :dataSource="rows"
    :config="store.gridConfig"
  />
</template>
