<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";

import Grid from "@/components/Grid.vue";
import GridPagination from "@/components/GridPagination.vue";
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
const units = ref<any[]>([]);
const dutyTypeLabels: Record<string, string> = {
  APPROVAL: "簽核",
  REVIEW: "會簽／審查",
  NOTIFY: "通知",
};

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
    { label: "部門", field: "orgUnitLabel" },
    { label: "職務代碼", field: "dutyCode" },
    { label: "職務名稱", field: "dutyName" },
    { label: "用途", field: "dutyTypeLabel" },
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
    field: {
      tenantId: store.queryParam.tenantId,
      orgUnitId: store.queryParam.orgUnitId,
      dutyCodeLike: store.queryParam.dutyCode,
      dutyNameLike: store.queryParam.dutyName,
      dutyType: store.queryParam.dutyType,
      status: store.queryParam.status,
    },
    pageOf: {
      select: store.gridConfig.page,
      showRow: store.gridConfig.row,
    },
  });
  rows.value = (response.data?.value || []).map((value: any) => ({
    ...value,
    dutyTypeLabel: dutyTypeLabels[value.dutyType] || value.dutyType,
  }));
  setConfigTotal(store.gridConfig, response.data?.pageOf?.countSize || 0);
};

const clear = () => {
  store.queryParam = {
    tenantId: "",
    orgUnitId: "",
    dutyCode: "",
    dutyName: "",
    dutyType: "",
    status: "",
  };
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
    description="選配功能：只有流程需要指定某部門的固定專責人員時才需設定，例如資訊部資安會簽人或財務部費用審核人。一般主管簽核請使用部門主管配置；跨部門固定成員請使用簽核群組。"
    createFlag="Y"
    refreshFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
  />

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
        <select v-model="store.queryParam.orgUnitId" class="form-select">
          <option value="">全部部門</option>
          <option v-for="item in units" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.dutyCode"
          class="form-control"
          placeholder="職務代碼"
        />
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.dutyName"
          class="form-control"
          placeholder="職務名稱"
        />
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.dutyType" class="form-select">
          <option value="">全部用途</option>
          <option value="APPROVAL">簽核</option>
          <option value="REVIEW">會簽／審查</option>
          <option value="NOTIFY">通知</option>
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
