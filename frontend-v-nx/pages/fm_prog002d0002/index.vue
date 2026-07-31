<script setup lang="ts">
import { onMounted, ref } from "vue";
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
import { useFmProg002d0002Store } from "./QueryPageStore";

definePageMeta({ middleware: ["auth"] });

const router = useRouter();
const store = useFmProg002d0002Store();
const rows = ref<any[]>([]);
const tenantOptions = ref<any[]>([]);
const show = ref(true);
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
    { label: "Tenant", field: "tenantId" },
    { label: "部門代碼", field: "unitCode" },
    { label: "部門名稱", field: "unitName" },
    { label: "類型", field: "unitType" },
    { label: "深度", field: "treeDepth" },
    { label: "版本", field: "currentVersionNo" },
    { label: "狀態", field: "status" },
  ],
);

const loadTenantOptions = async () => {
  const response = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL +
      PageConstants.eventNamespace +
      "/tenant-options",
  );
  tenantOptions.value = response.data?.value || [];
};

const query = async () => {
  showLoading();
  rows.value = [];
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/findPage",
      {
        field: {
          tenantId: store.queryParam.tenantId,
          unitCodeLike: store.queryParam.unitCode,
          unitNameLike: store.queryParam.unitName,
          status: store.queryParam.status,
        },
        pageOf: {
          select: store.gridConfig.page,
          showRow: store.gridConfig.row,
        },
      },
    );
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      setConfigTotal(store.gridConfig, 0);
      toast.warning(response.data?.message || "查詢部門失敗。");
      return;
    }
    rows.value = response.data.value || [];
    setConfigTotal(store.gridConfig, response.data.pageOf?.countSize || 0);
  } catch (error: any) {
    setConfigTotal(store.gridConfig, 0);
    toast.error(error?.message || "查詢部門失敗。");
  } finally {
    hideLoading();
  }
};

const btnQuery = () => {
  setConfigPage(store.gridConfig, 1);
  query();
};

const clear = () => {
  store.queryParam = {
    tenantId: "",
    unitCode: "",
    unitName: "",
    status: "",
  };
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};

const page = (value: number) => {
  setConfigPage(store.gridConfig, value);
  query();
};

const size = (value: number) => {
  setConfigRow(store.gridConfig, value);
  setConfigPage(store.gridConfig, 1);
  query();
};

onMounted(async () => {
  await loadTenantOptions();
  await query();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="部門資料與組織樹"
    refreshFlag="Y"
    createFlag="Y"
    queryFieldShowSwitchFlag="Y"
    @refreshMethod="clear"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @queryFieldShowSwitcMethod="show = !show"
  />

  <div class="d-flex justify-content-end mb-3">
    <button
      type="button"
      class="btn btn-outline-primary"
      @click="router.push(PageConstants.frontendNamespace + '/tree')"
    >
      <i class="bi bi-diagram-3"></i>
      組織樹
    </button>
  </div>

  <HiddenQueryFieldAlertInfo :dataSource="rows" :queryFieldShowFlag="show" />

  <div v-show="show" class="card mb-3">
    <div class="card-body row g-3">
      <div class="col-md-3">
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">全部 Tenant</option>
          <option
            v-for="item in tenantOptions"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.unitCode"
          class="form-control"
          placeholder="部門代碼"
        />
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.unitName"
          class="form-control"
          placeholder="部門名稱"
        />
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="ACTIVE">啟用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="col-md-3">
        <button class="btn btn-primary me-2" @click="btnQuery">查詢</button>
        <button class="btn btn-outline-secondary" @click="clear">清除</button>
      </div>
    </div>
  </div>

  <GridPagination
    :progId="PageConstants.QueryId"
    :gridConfig="store.gridConfig"
    :changePageSelectMethod="page"
    :changeGridConfigRowMethod="size"
  />
  <Grid
    :progId="PageConstants.QueryId"
    :dataSource="rows"
    :config="store.gridConfig"
  />
</template>
