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
const accounts = ref<any[]>([]);
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
    { label: "被代理人", field: "principalLabel" },
    { label: "代理人", field: "delegateLabel" },
    { label: "代理範圍", field: "scopeLabel" },
    { label: "開始時間", field: "effectiveFrom" },
    { label: "結束時間", field: "effectiveTo" },
    { label: "狀態", field: "status" },
  ],
);

const loadAccounts = async () => {
  accounts.value = store.queryParam.tenantId
    ? (await post("/account-options", { tenantId: store.queryParam.tenantId }))
        .data?.value || []
    : [];
};
const query = async () => {
  showLoading();
  rows.value = [];
  try {
    const response = await post("/findPage", {
      field: { ...store.queryParam },
      pageOf: {
        select: store.gridConfig.page,
        showRow: store.gridConfig.row,
      },
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      setConfigTotal(store.gridConfig, 0);
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "查詢代理設定失敗。"),
      );
      return;
    }
    rows.value = response.data.value || [];
    setConfigTotal(store.gridConfig, response.data.pageOf?.countSize || 0);
  } catch (error: unknown) {
    setConfigTotal(store.gridConfig, 0);
    const message =
      error instanceof Error ? error.message : "查詢代理設定失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};
const clear = () => {
  store.queryParam = {
    tenantId: "",
    principalAccount: "",
    delegateAccount: "",
    scopeType: "",
    status: "",
  };
  accounts.value = [];
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};

onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  if (!store.queryParam.tenantId && tenants.value.length === 1) {
    store.queryParam.tenantId = tenants.value[0].value;
  }
  await loadAccounts();
  await query();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="設定員工請假或暫時無法處理簽核時的代理人。可限定全部流程、特定流程或特定簽核群組，並設定明確的開始與結束時間；代理設定只影響有效期間內新解析或依流程規則允許改派的工作。"
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
      <div class="col-md-2">
        <select
          v-model="store.queryParam.tenantId"
          class="form-select"
          @change="loadAccounts"
        >
          <option value="">全部 Tenant</option>
          <option v-for="item in tenants" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.principalAccount" class="form-select">
          <option value="">全部被代理人</option>
          <option
            v-for="item in accounts"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.delegateAccount" class="form-select">
          <option value="">全部代理人</option>
          <option
            v-for="item in accounts"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.scopeType" class="form-select">
          <option value="">全部代理範圍</option>
          <option value="ALL">全部流程</option>
          <option value="PROCESS">指定流程</option>
          <option value="APPROVAL_GROUP">指定簽核群組</option>
        </select>
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.status" class="form-select">
          <option value="">全部狀態</option>
          <option value="ACTIVE">啟用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="col-md-2 d-flex gap-2">
        <button
          class="btn btn-primary"
          @click="
            setConfigPage(store.gridConfig, 1);
            query();
          "
        >
          查詢
        </button>
        <button class="btn btn-outline-secondary" @click="clear">清除</button>
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
