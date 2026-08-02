<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
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
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "./config";
import { useStore } from "./QueryPageStore";
definePageMeta({ middleware: ["auth"] });
const router = useRouter(),
  store = useStore(),
  rows = ref<any[]>([]),
  tenants = ref<any[]>([]);
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
    { label: "方案代碼", field: "schemeCode" },
    { label: "方案名稱", field: "schemeName" },
    { label: "預設", field: "isDefault" },
    { label: "狀態", field: "status" },
  ],
);
const query = async () => {
  showLoading();
  try {
    const x = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/findPage",
      {
        field: {
          tenantId: store.queryParam.tenantId,
          schemeCodeLike: store.queryParam.schemeCode,
          schemeNameLike: store.queryParam.schemeName,
          status: store.queryParam.status,
        },
        pageOf: {
          select: store.gridConfig.page,
          showRow: store.gridConfig.row,
        },
      },
    );
    if (x.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(x.data?.message);
      return;
    }
    rows.value = x.data.value || [];
    setConfigTotal(store.gridConfig, x.data.pageOf?.countSize || 0);
  } finally {
    hideLoading();
  }
};
const clear = () => {
  store.queryParam = {
    tenantId: "",
    schemeCode: "",
    schemeName: "",
    status: "",
  };
  rows.value = [];
};
onMounted(async () => {
  const x = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL +
      PageConstants.eventNamespace +
      "/tenant-options",
  );
  tenants.value = x.data?.value || [];
  await query();
});
</script>
<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="組織簽核層級"
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
          <option v-for="x in tenants" :value="x.value">{{ x.label }}</option>
        </select>
      </div>
      <div class="col">
        <input
          v-model="store.queryParam.schemeCode"
          class="form-control"
          placeholder="方案代碼"
        />
      </div>
      <div class="col">
        <input
          v-model="store.queryParam.schemeName"
          class="form-control"
          placeholder="方案名稱"
        />
      </div>
      <div class="col">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部</option>
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
