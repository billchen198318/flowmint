<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import EmployeeForm from "../components/EmployeeForm.vue";
import { PageConstants } from "../config";

definePageMeta({ middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const form = ref<any>({});
const checkFields = ref<Record<string, string>>({});
const tenantOptions = ref<any[]>([]);
const accountOptions = ref<any[]>([]);

const toLocalInput = (value: string | null) => {
  if (!value) return "";
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
};
const loadTenantOptions = async () => {
  const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/tenant-options");
  tenantOptions.value = response.data?.value || [];
};
const loadAccountOptions = async (tenantId: string) => {
  const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/account-options", { tenantId });
  accountOptions.value = response.data?.value || [];
};
const applyEmployee = async (value: any) => {
  form.value = { ...value, effectiveFrom: toLocalInput(value.effectiveFrom), effectiveTo: toLocalInput(value.effectiveTo) };
  await loadAccountOptions(value.tenantId);
};
const loadData = async () => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/load", { oid: route.params.id });
    if (!response.data || response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "讀取員工失敗。")); router.push(PageConstants.frontendNamespace); return;
    }
    await applyEmployee(response.data.value);
  } catch (error: any) { toast.error(error?.message || "讀取員工失敗。"); }
  finally { hideLoading(); }
};
const btnSave = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const payload = { ...form.value, effectiveFrom: new Date(form.value.effectiveFrom).toISOString(), effectiveTo: form.value.effectiveTo ? new Date(form.value.effectiveTo).toISOString() : null };
    const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/update", payload);
    checkFields.value = response.data?.checkFields || {};
    if (!response.data || response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "更新員工失敗。")); return;
    }
    await applyEmployee(response.data.value); toast.success(response.data.message);
  } catch (error: any) { toast.error(error?.message || "更新員工失敗。"); }
  finally { hideLoading(); }
};
const doDeactivate = async () => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/deactivate", { oid: form.value.oid });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      await applyEmployee(response.data.value); toast.success(response.data.message);
    } else toast.warning(escapeQifuHtmlMsg(response.data?.message || "停用員工失敗。"));
  } catch (error: any) { toast.error(error?.message || "停用員工失敗。"); }
  finally { hideLoading(); }
};
const btnDeactivate = () => confirmFire("確定停用此員工？", doDeactivate, form.value.oid);
onMounted(async () => { await loadTenantOptions(); await loadData(); });
</script>

<template>
  <Toolbar :progId="PageConstants.EditId" description="編輯員工" refreshFlag="Y" backFlag="Y" saveFlag="Y"
    @refreshMethod="loadData" @backMethod="router.back()" @saveMethod="btnSave" />
  <div class="card"><div class="card-body">
    <EmployeeForm v-model="form" :checkFields="checkFields" :tenantOptions="tenantOptions"
      :accountOptions="accountOptions" tenantReadonly />
    <div class="row mt-4"><div class="col-12 d-flex gap-2">
      <button type="button" class="btn btn-primary" @click="btnSave"><i class="bi bi-save"></i> 儲存</button>
      <button type="button" class="btn btn-outline-secondary" @click="loadData"><i class="bi bi-repeat"></i> 重新載入</button>
      <button v-if="form.status === 'ACTIVE'" type="button" class="btn btn-outline-danger" @click="btnDeactivate">
        <i class="bi bi-person-x"></i> 停用員工
      </button>
    </div></div>
  </div></div>
</template>
