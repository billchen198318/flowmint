<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import Grid from "@/components/Grid.vue";
import GridPagination from "@/components/GridPagination.vue";
import HiddenQueryFieldAlertInfo from "@/components/HiddenQueryFieldAlertInfo.vue";
import Toolbar from "@/components/Toolbar.vue";
import {
  escapeQifuHtmlMsg,
  getAxiosInstance,
} from "@/components/BaseHelper";
import {
  getGridConfig,
  setConfigPage,
  setConfigRow,
  setConfigTotal,
} from "@/components/GridHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "./config";
import { useStore } from "./QueryPageStore";

interface OptionItem {
  value: string;
  label: string;
}

interface DataActionRow {
  oid: string;
  tenantId: string;
  actionCode: string;
  actionName: string;
  actionType: string;
  status: string;
  currentVersionNo: number;
  draftVersionNo: number | null;
}

definePageMeta({ middleware: ["auth"] });

const pageProgramId = PageConstants.QueryId;
const router = useRouter();
const store = useStore();
const { showLoading, hideLoading } = useSwalLoading();
const rows = ref<DataActionRow[]>([]);
const tenants = ref<OptionItem[]>([]);
const queryFieldShow = ref(true);

const post = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

const tenantPost = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + "/FM_PROG006D0001" + path,
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
    { label: "Action Code", field: "actionCode" },
    { label: "Action 名稱", field: "actionName" },
    { label: "類型", field: "actionType" },
    { label: "已發布版本", field: "currentVersionNo" },
    { label: "草稿版本", field: "draftVersionNo" },
    { label: "狀態", field: "status" },
  ],
);

const query = async () => {
  if (!store.queryParam.tenantId) {
    rows.value = [];
    setConfigTotal(store.gridConfig, 0);
    toast.warning(escapeQifuHtmlMsg("請先選擇 Tenant。"));
    return;
  }
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
        escapeQifuHtmlMsg(response.data?.message || "查詢 Data Action 失敗。"),
      );
      return;
    }
    rows.value = response.data?.value || [];
    setConfigTotal(
      store.gridConfig,
      response.data?.pageOf?.countSize || 0,
    );
  } catch (error: unknown) {
    setConfigTotal(store.gridConfig, 0);
    const message =
      error instanceof Error ? error.message : "查詢 Data Action 失敗。";
    toast.error(escapeQifuHtmlMsg(message));
  } finally {
    hideLoading();
  }
};

const clear = () => {
  store.queryParam.tenantId = "";
  store.queryParam.actionCodeLike = "";
  store.queryParam.status = "";
  rows.value = [];
  setConfigTotal(store.gridConfig, 0);
};

onMounted(async () => {
  try {
    const response = await tenantPost("/tenant-options");
    tenants.value = response.data?.value || [];
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "載入 Tenant 失敗";
    toast.error(escapeQifuHtmlMsg(message));
  }

  if (store.queryParam.tenantId) {
    await query();
  }
});
</script>

<template>
  <Toolbar
    :progId="pageProgramId"
    description="設定可供 Form.io 與 FlowMint 共用 API 呼叫的版本化 Data Action。"
    createFlag="Y"
    refreshFlag="Y"
    queryFieldShowSwitchFlag="Y"
    @createMethod="router.push(PageConstants.frontendNamespace + '/create')"
    @refreshMethod="clear"
    @queryFieldShowSwitcMethod="queryFieldShow = !queryFieldShow"
  />

  <HiddenQueryFieldAlertInfo
    :dataSource="rows"
    :queryFieldShowFlag="queryFieldShow"
  />

  <div class="mb-3 text-end">
    <button class="btn btn-outline-primary" @click="router.push(PageConstants.frontendNamespace + '/audits')">
      執行紀錄
    </button>
  </div>

  <div v-show="queryFieldShow" class="card mb-3">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-4">
          <label for="tenantId" class="form-label">Tenant</label>
          <select
            id="tenantId"
            v-model="store.queryParam.tenantId"
            class="form-select"
          >
            <option value="">請選擇</option>
            <option
              v-for="item in tenants"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
        </div>

        <div class="col-md-3">
          <label for="actionCodeLike" class="form-label">Action Code</label>
          <input
            id="actionCodeLike"
            v-model="store.queryParam.actionCodeLike"
            class="form-control"
          />
        </div>

        <div class="col-md-2">
          <label for="status" class="form-label">狀態</label>
          <select
            id="status"
            v-model="store.queryParam.status"
            class="form-select"
          >
            <option value="">全部</option>
            <option value="DRAFT">草稿</option>
            <option value="ACTIVE">已發布</option>
            <option value="INACTIVE">停用</option>
          </select>
        </div>

        <div class="col-md-3 d-flex align-items-end gap-2">
          <button
            class="btn btn-primary"
            @click="setConfigPage(store.gridConfig, 1); query()"
          >
            查詢
          </button>
          <button class="btn btn-outline-secondary" @click="clear">
            清除
          </button>
        </div>
      </div>
    </div>
  </div>

  <GridPagination
    :progId="pageProgramId"
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
    :progId="pageProgramId"
    :dataSource="rows"
    :config="store.gridConfig"
  />
</template>
