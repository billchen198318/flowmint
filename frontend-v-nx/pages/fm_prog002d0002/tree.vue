<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import OrgTreeNode from "./components/OrgTreeNode.vue";
import { PageConstants } from "./config";

definePageMeta({ middleware: ["auth"] });

const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenantOptions = ref<any[]>([]);
const tenantId = ref("");
const includeInactive = ref(false);
const rows = ref<any[]>([]);

const treeNodes = computed(() => {
  const nodeMap = new Map<string, any>();
  rows.value.forEach((item) => {
    nodeMap.set(item.orgUnitId, { ...item, children: [] });
  });

  const roots: any[] = [];
  nodeMap.forEach((node) => {
    const parent = nodeMap.get(node.parentOrgUnitId);
    if (parent) {
      parent.children.push(node);
    } else {
      roots.push(node);
    }
  });

  const sortNodes = (nodes: any[]) => {
    nodes.sort(
      (left, right) =>
        (left.sortNo || 0) - (right.sortNo || 0) ||
        left.unitCode.localeCompare(right.unitCode),
    );
    nodes.forEach((node) => sortNodes(node.children));
  };
  sortNodes(roots);
  return roots;
});

const loadTenantOptions = async () => {
  const response = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL +
      PageConstants.eventNamespace +
      "/tenant-options",
  );
  tenantOptions.value = response.data?.value || [];
  if (!tenantId.value && tenantOptions.value.length) {
    tenantId.value = tenantOptions.value[0].value;
  }
};

const loadTree = async () => {
  rows.value = [];
  if (!tenantId.value) {
    return;
  }
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/tree",
      {
        tenantId: tenantId.value,
        includeInactive: includeInactive.value,
      },
    );
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取組織樹失敗。"),
      );
      return;
    }
    rows.value = response.data.value || [];
  } catch (error: any) {
    toast.error(error?.message || "讀取組織樹失敗。");
  } finally {
    hideLoading();
  }
};

const executeMove = async (command: any) => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/move",
      command,
    );
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "移動部門失敗。"),
      );
      return;
    }
    rows.value = response.data.value || [];
    toast.success(response.data.message || "部門已移動。");
  } catch (error: any) {
    toast.error(error?.message || "移動部門失敗。");
  } finally {
    hideLoading();
  }
};

const previewMove = async (
  sourceOrgUnitId: string,
  targetOrgUnitId: string,
) => {
  const source = rows.value.find((item) => item.orgUnitId === sourceOrgUnitId);
  const target = rows.value.find((item) => item.orgUnitId === targetOrgUnitId);
  if (!source || !target) {
    toast.warning("找不到拖拉來源或目標部門。");
    return;
  }

  const command = {
    tenantId: tenantId.value,
    orgUnitId: source.orgUnitId,
    newParentOrgUnitId: target.orgUnitId,
    currentVersionNo: source.currentVersionNo,
    sortNo: source.sortNo,
  };

  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/move-preview",
      command,
    );
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "移動預覽失敗。"),
      );
      return;
    }
    const preview = response.data.value;
    const title =
      `將「${source.unitName}」移到「${target.unitName}」下？` +
      ` 深度 ${preview.oldTreeDepth} → ${preview.newTreeDepth}，` +
      `影響 ${preview.affectedNodeCount} 個節點。`;
    await confirmFire(title, executeMove, command);
  } catch (error: any) {
    toast.error(error?.message || "移動預覽失敗。");
  } finally {
    hideLoading();
  }
};

onMounted(async () => {
  await loadTenantOptions();
  await loadTree();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.TreeId"
    description="部門組織樹"
    backFlag="Y"
    refreshFlag="Y"
    @backMethod="router.push(PageConstants.frontendNamespace)"
    @refreshMethod="loadTree"
  />

  <div class="card mb-3">
    <div class="card-body row g-3 align-items-center">
      <div class="col-md-5">
        <label for="treeTenantId" class="form-label">Tenant</label>
        <select
          id="treeTenantId"
          v-model="tenantId"
          class="form-select"
          @change="loadTree"
        >
          <option value="">請選擇 Tenant</option>
          <option
            v-for="item in tenantOptions"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="col-md-4 pt-md-4">
        <div class="form-check">
          <input
            id="includeInactive"
            v-model="includeInactive"
            type="checkbox"
            class="form-check-input"
            @change="loadTree"
          />
          <label for="includeInactive" class="form-check-label">
            顯示停用部門
          </label>
        </div>
      </div>
      <div class="col-md-3 pt-md-4 text-md-end">
        <button
          type="button"
          class="btn btn-primary"
          @click="router.push(PageConstants.frontendNamespace + '/create')"
        >
          <i class="bi bi-plus-circle"></i>
          新增部門
        </button>
      </div>
    </div>
  </div>

  <div class="alert alert-info">
    拖曳部門到新的父部門上，系統會先顯示移動預覽；確認後才更新整個子樹。
  </div>

  <div class="card">
    <div class="card-body org-tree-container">
      <div v-if="!treeNodes.length" class="text-muted text-center py-5">
        尚無部門資料。
      </div>
      <ul v-else class="org-tree-root">
        <OrgTreeNode
          v-for="node in treeNodes"
          :key="node.orgUnitId"
          :node="node"
          @move="previewMove"
          @edit="
            router.push(PageConstants.frontendNamespace + '/edit/' + $event)
          "
        />
      </ul>
    </div>
  </div>
</template>

<style scoped>
.org-tree-container {
  min-height: 18rem;
  overflow-x: auto;
}

.org-tree-root {
  min-width: 45rem;
  margin: 0;
  padding: 0;
}
</style>
