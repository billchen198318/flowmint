<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import TenantForm from "./components/TenantForm.vue";
import { PageConstants } from "./config";

definePageMeta({ middleware: ["auth"] });

interface TenantFormData {
  tenantId: string;
  tenantCode: string;
  tenantName: string;
  defaultLocale: string;
  defaultTimezone: string;
  status: string;
  description: string;
}

const newTenantForm = (): TenantFormData => ({
  tenantId: "",
  tenantCode: "",
  tenantName: "",
  defaultLocale: "zh-TW",
  defaultTimezone: "Asia/Taipei",
  status: "ACTIVE",
  description: "",
});

const router = useRouter();
const { showLoading, hideLoading } = useSwalLoading();
const checkFields = ref<Record<string, string>>({});
const form = ref<TenantFormData>(newTenantForm());

const btnBack = () => router.back();

const btnClear = () => {
  checkFields.value = {};
  form.value = newTenantForm();
};

const btnSave = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/save",
      form.value,
    );

    checkFields.value = response.data?.checkFields || {};
    if (!response.data) {
      toast.error("後端未回傳資料。");
      return;
    }
    if (response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data.message));
      return;
    }

    toast.success(response.data.message);
    btnClear();
  } catch (error: any) {
    toast.error(error?.message || "新增 Tenant 失敗。");
  } finally {
    hideLoading();
  }
};
</script>

<template>
  <Toolbar
    :progId="PageConstants.CreateId"
    description="建立 Tenant"
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="btnClear"
    @backMethod="btnBack"
    @saveMethod="btnSave"
  />

  <div class="card">
    <div class="card-body">
      <TenantForm v-model="form" :checkFields="checkFields" />

      <div class="row mt-4">
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="btnSave">
            <i class="bi bi-save"></i> 儲存
          </button>
          <button
            type="button"
            class="btn btn-outline-secondary"
            @click="btnClear"
          >
            <i class="bi bi-eraser"></i> 清除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
