<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import Toolbar from "@/components/Toolbar.vue";
import Grid from "@/components/Grid.vue";
import GridPagination from "@/components/GridPagination.vue";
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
const router = useRouter(),
  store = useStore(),
  rows = ref<any[]>([]),
  tenants = ref<any[]>([]);
const post = (p: string, b: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + p,
    b,
  );
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
    { label: "職稱代碼", field: "titleCode" },
    { label: "職稱名稱", field: "titleName" },
    { label: "主管職稱", field: "isManagerTitle" },
    { label: "排序", field: "sortNo" },
    { label: "狀態", field: "status" },
  ],
);
const loadUnits = async () =>
  (units.value = store.queryParam.tenantId
    ? (await post("/org-unit-options", { tenantId: store.queryParam.tenantId }))
        .data?.value || []
    : []);
const query = async () => {
  const x = await post("/findPage", {
    field: {
      tenantId: store.queryParam.tenantId,
      titleCodeLike: store.queryParam.titleCode,
      titleNameLike: store.queryParam.titleName,
      status: store.queryParam.status,
    },
    pageOf: { select: store.gridConfig.page, showRow: store.gridConfig.row },
  });
  rows.value = x.data?.value || [];
  setConfigTotal(store.gridConfig, x.data?.pageOf?.countSize || 0);
};
const clear = () => {
  store.queryParam = {
    tenantId: "",
    titleCode: "",
    titleName: "",
    status: "",
  };
  rows.value = [];
};
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  await query();
});
</script>
<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="職稱與簽核 Level"
    createFlag="Y"
    refreshFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
  />
  <div class="card mb-3">
    <div class="card-body row g-2">
      <div class="col">
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">全部 Tenant</option>
          <option v-for="x in tenants" :key="x.value" :value="x.value">
            {{ x.label }}
          </option>
        </select>
      </div>
      <div class="col">
        <input
          v-model="store.queryParam.titleCode"
          class="form-control"
          placeholder="職稱代碼"
        />
      </div>
      <div class="col">
        <input
          v-model="store.queryParam.titleName"
          class="form-control"
          placeholder="職稱名稱"
        />
      </div>
      <div class="col">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option>ACTIVE</option>
          <option>INACTIVE</option>
        </select>
      </div>
      <div class="col">
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
      (p: number) => {
        setConfigPage(store.gridConfig, p);
        query();
      }
    "
    :changeGridConfigRowMethod="
      (n: number) => {
        setConfigRow(store.gridConfig, n);
        query();
      }
    "
  /><Grid
    :progId="PageConstants.QueryId"
    :dataSource="rows"
    :config="store.gridConfig"
  />
</template>
