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
const modeLabels: Record<string, string> = {
  CANDIDATE: "任一成員處理",
  ALL: "全員都要處理",
  SEQUENTIAL: "依優先序逐一處理",
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
    { label: "群組代碼", field: "groupCode" },
    { label: "群組名稱", field: "groupName" },
    { label: "處理方式", field: "assignmentModeLabel" },
    { label: "狀態", field: "status" },
  ],
);

const query = async () => {
  showLoading();
  rows.value = [];
  try {
    const response = await post("/findPage", {
      field: {
        tenantId: store.queryParam.tenantId,
        groupCodeLike: store.queryParam.groupCode,
        groupNameLike: store.queryParam.groupName,
        assignmentMode: store.queryParam.assignmentMode,
        status: store.queryParam.status,
      },
      pageOf: {
        select: store.gridConfig.page,
        showRow: store.gridConfig.row,
      },
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      setConfigTotal(store.gridConfig, 0);
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "查詢簽核群組失敗。"),
      );
      return;
    }
    rows.value = (response.data.value || []).map((value: any) => ({
      ...value,
      assignmentModeLabel:
        modeLabels[value.assignmentMode] || value.assignmentMode,
    }));
    setConfigTotal(store.gridConfig, response.data.pageOf?.countSize || 0);
  } catch (error: unknown) {
    setConfigTotal(store.gridConfig, 0);
    const message =
      error instanceof Error ? error.message : "查詢簽核群組失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};

const clear = () => {
  store.queryParam = {
    tenantId: "",
    groupCode: "",
    groupName: "",
    assignmentMode: "",
    status: "",
  };
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};

onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  await query();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.QueryId"
    description="建立可跨部門的固定簽核成員群組，例如資訊安全委員會、採購審議小組或高額費用審核群組。可設定任一成員處理、全員處理，或依優先序逐一處理。"
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
        <select v-model="store.queryParam.tenantId" class="form-select">
          <option value="">全部 Tenant</option>
          <option v-for="item in tenants" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.groupCode"
          class="form-control"
          placeholder="群組代碼"
        />
      </div>
      <div class="col-md-2">
        <input
          v-model="store.queryParam.groupName"
          class="form-control"
          placeholder="群組名稱"
        />
      </div>
      <div class="col-md-2">
        <select v-model="store.queryParam.assignmentMode" class="form-select">
          <option value="">全部處理方式</option>
          <option value="CANDIDATE">任一成員處理</option>
          <option value="ALL">全員都要處理</option>
          <option value="SEQUENTIAL">依優先序逐一處理</option>
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
