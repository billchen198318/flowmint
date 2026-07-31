<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import EmployeeForm from "./components/EmployeeForm.vue";
import { PageConstants } from "./config";

definePageMeta({ middleware: ["auth"] });

const localNow = () => {
  const date = new Date();
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
};
const newForm = () => ({
  tenantId: "", employeeNo: "", account: "", displayName: "", email: "", mobile: "",
  locale: "zh-TW", timezone: "Asia/Taipei", status: "ACTIVE", effectiveFrom: localNow(),
  effectiveTo: "", description: "",
});
const router = useRouter();
const { showLoading, hideLoading } = useSwalLoading();
const form = ref<any>(newForm());
const checkFields = ref<Record<string, string>>({});
const tenantOptions = ref<any[]>([]);
const accountOptions = ref<any[]>([]);

const loadTenantOptions = async () => {
  const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/tenant-options");
  tenantOptions.value = response.data?.value || [];
};
const loadAccountOptions = async (tenantId: string) => {
  accountOptions.value = [];
  if (!tenantId) return;
  const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/account-options", { tenantId });
  accountOptions.value = response.data?.value || [];
};
watch(() => form.value.tenantId, async (value, oldValue) => {
  if (value !== oldValue) form.value.account = "";
  await loadAccountOptions(value);
});
const btnClear = () => { checkFields.value = {}; form.value = newForm(); accountOptions.value = []; };
const btnSave = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const payload = { ...form.value, effectiveFrom: new Date(form.value.effectiveFrom).toISOString(), effectiveTo: form.value.effectiveTo ? new Date(form.value.effectiveTo).toISOString() : null };
    const response = await getAxiosInstance().post(import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/save", payload);
    checkFields.value = response.data?.checkFields || {};
    if (!response.data || response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "新增員工失敗。")); return;
    }
    toast.success(response.data.message); btnClear();
  } catch (error: any) { toast.error(error?.message || "新增員工失敗。"); }
  finally { hideLoading(); }
};
onMounted(loadTenantOptions);
</script>

<template>
  <Toolbar :progId="PageConstants.CreateId" description="建立員工" refreshFlag="Y" backFlag="Y" saveFlag="Y"
    @refreshMethod="btnClear" @backMethod="router.back()" @saveMethod="btnSave" />
  <div class="card"><div class="card-body">
    <EmployeeForm v-model="form" :checkFields="checkFields" :tenantOptions="tenantOptions" :accountOptions="accountOptions" />
    <div class="row mt-4"><div class="col-12 d-flex gap-2">
      <button type="button" class="btn btn-primary" @click="btnSave"><i class="bi bi-save"></i> 儲存</button>
      <button type="button" class="btn btn-outline-secondary" @click="btnClear"><i class="bi bi-eraser"></i> 清除</button>
    </div></div>
  </div></div>
</template>
