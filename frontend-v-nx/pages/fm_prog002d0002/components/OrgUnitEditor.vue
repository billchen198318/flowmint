<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";
import OrgUnitForm from "./OrgUnitForm.vue";

const props = defineProps<{ edit: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();

const tenantOptions = ref<any[]>([]);
const parentOptions = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});

const localNow = () => {
  const date = new Date();
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};

const newForm = () => ({
  oid: "",
  tenantId: "",
  unitCode: "",
  currentVersionNo: 1,
  parentOrgUnitId: "",
  unitName: "",
  shortName: "",
  unitType: "DEPARTMENT",
  sortNo: 0,
  isVirtual: "N",
  status: "ACTIVE",
  effectiveFrom: localNow(),
  effectiveTo: "",
  description: "",
});

const form = ref<any>(newForm());

const toLocalInput = (value: string | null) => {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};

const toPayload = () => ({
  ...form.value,
  effectiveFrom: new Date(form.value.effectiveFrom).toISOString(),
  effectiveTo: form.value.effectiveTo
    ? new Date(form.value.effectiveTo).toISOString()
    : null,
});

const applyValue = (value: any) => {
  form.value = {
    ...value,
    parentOrgUnitId: value.parentOrgUnitId || "",
    effectiveFrom: toLocalInput(value.effectiveFrom),
    effectiveTo: toLocalInput(value.effectiveTo),
  };
};

const loadTenantOptions = async () => {
  const response = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL +
      PageConstants.eventNamespace +
      "/tenant-options",
  );
  tenantOptions.value = response.data?.value || [];
};

const loadParentOptions = async (tenantId: string) => {
  parentOptions.value = [];
  if (!tenantId) {
    return;
  }
  const response = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/tree",
    { tenantId, includeInactive: false },
  );
  parentOptions.value = (response.data?.value || []).filter(
    (item: any) => item.orgUnitId !== form.value.orgUnitId,
  );
};

const loadData = async () => {
  if (!props.edit) {
    return;
  }
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/load",
      { oid: route.params.id },
    );
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取部門失敗。"),
      );
      router.push(PageConstants.frontendNamespace);
      return;
    }
    applyValue(response.data.value);
    await loadParentOptions(form.value.tenantId);
  } catch (error: any) {
    toast.error(error?.message || "讀取部門失敗。");
  } finally {
    hideLoading();
  }
};

const clear = async () => {
  checkFields.value = {};
  if (props.edit) {
    await loadData();
  } else {
    form.value = newForm();
    parentOptions.value = [];
  }
};

const save = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        (props.edit ? "/update" : "/save"),
      toPayload(),
    );
    checkFields.value = response.data?.checkFields || {};
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存部門失敗。"),
      );
      return;
    }
    toast.success(response.data.message);
    if (props.edit) {
      applyValue(response.data.value);
      await loadParentOptions(form.value.tenantId);
    } else {
      await clear();
    }
  } catch (error: any) {
    toast.error(error?.message || "儲存部門失敗。");
  } finally {
    hideLoading();
  }
};

const deactivate = async () => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/deactivate",
      {
        oid: form.value.oid,
        currentVersionNo: form.value.currentVersionNo,
      },
    );
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      applyValue(response.data.value);
      toast.success(response.data.message);
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用部門失敗。"),
      );
    }
  } catch (error: any) {
    toast.error(error?.message || "停用部門失敗。");
  } finally {
    hideLoading();
  }
};

const confirmDeactivate = () =>
  confirmFire("確定停用此部門？", deactivate, form.value.oid);

watch(
  () => form.value.tenantId,
  async (tenantId, oldTenantId) => {
    if (!props.edit && tenantId !== oldTenantId) {
      form.value.parentOrgUnitId = "";
      await loadParentOptions(tenantId);
    }
  },
);

onMounted(async () => {
  await loadTenantOptions();
  await loadData();
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="props.edit ? '編輯部門' : '建立部門'"
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="clear"
    @backMethod="router.back()"
    @saveMethod="save"
  />

  <div class="card">
    <div class="card-body">
      <OrgUnitForm
        v-model="form"
        :checkFields="checkFields"
        :tenantOptions="tenantOptions"
        :parentOptions="parentOptions"
        :tenantReadonly="props.edit"
        :parentReadonly="props.edit"
      />

      <div class="row mt-4">
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="save">
            <i class="bi bi-save"></i>
            儲存
          </button>
          <button
            type="button"
            class="btn btn-outline-secondary"
            @click="clear"
          >
            <i class="bi bi-repeat"></i>
            {{ props.edit ? "重新載入" : "清除" }}
          </button>
          <button
            v-if="props.edit && form.status === 'ACTIVE'"
            type="button"
            class="btn btn-outline-danger"
            @click="confirmDeactivate"
          >
            <i class="bi bi-diagram-2"></i>
            停用部門
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
