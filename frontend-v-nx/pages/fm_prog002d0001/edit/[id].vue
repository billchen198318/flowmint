<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import EmployeeAssignmentSection from "../components/EmployeeAssignmentSection.vue";
import EmployeeForm from "../components/EmployeeForm.vue";
import { PageConstants } from "../config";

definePageMeta({ middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const form = ref<any>({});
const checkFields = ref<Record<string, string>>({});
const assignmentCheckFields = ref<Record<string, string>>({});
const tenantOptions = ref<any[]>([]);
const accountOptions = ref<any[]>([]);
const assignments = ref<any[]>([]);
const orgUnitOptions = ref<any[]>([]);
const titleOptions = ref<any[]>([]);
const managerOptions = ref<any[]>([]);

const toLocalInput = (value: string | null) => {
  if (!value) return "";
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};

const newAssignmentForm = () => ({
  oid: "",
  employeeOid: String(route.params.id),
  orgUnitId: "",
  titleId: "",
  managerSource: "ORG_HEAD",
  directManagerAssignmentId: "",
  isPrimary: "N",
  status: "ACTIVE",
  effectiveFrom: toLocalInput(new Date().toISOString()),
  effectiveTo: "",
});
const assignmentForm = ref<any>(newAssignmentForm());

const apiPost = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

const loadTenantOptions = async () => {
  const response = await apiPost("/tenant-options");
  tenantOptions.value = response.data?.value || [];
};

const loadAccountOptions = async (tenantId: string) => {
  const response = await apiPost("/account-options", { tenantId });
  accountOptions.value = response.data?.value || [];
};

const loadAssignmentData = async () => {
  const employeeOid = String(route.params.id);
  const [listResponse, unitResponse, titleResponse, managerResponse] =
    await Promise.all([
      apiPost("/assignment/list", { employeeOid }),
      apiPost("/assignment/org-unit-options", { employeeOid }),
      apiPost("/assignment/title-options", { employeeOid }),
      apiPost("/assignment/manager-options", { employeeOid }),
    ]);
  assignments.value = listResponse.data?.value || [];
  orgUnitOptions.value = unitResponse.data?.value || [];
  titleOptions.value = titleResponse.data?.value || [];
  managerOptions.value = managerResponse.data?.value || [];
};

const applyEmployee = async (value: any) => {
  form.value = {
    ...value,
    effectiveFrom: toLocalInput(value.effectiveFrom),
    effectiveTo: toLocalInput(value.effectiveTo),
  };
  await loadAccountOptions(value.tenantId);
};

const loadData = async () => {
  showLoading();
  try {
    const response = await apiPost("/load", { oid: route.params.id });
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取員工失敗。"),
      );
      router.push(PageConstants.frontendNamespace);
      return;
    }
    await applyEmployee(response.data.value);
    await loadAssignmentData();
    assignmentCheckFields.value = {};
  } catch (error: any) {
    toast.error(error?.message || "讀取員工失敗。");
  } finally {
    hideLoading();
  }
};

const btnSave = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const payload = {
      ...form.value,
      effectiveFrom: form.value.effectiveFrom
        ? new Date(form.value.effectiveFrom).toISOString()
        : null,
      effectiveTo: form.value.effectiveTo
        ? new Date(form.value.effectiveTo).toISOString()
        : null,
    };
    const response = await apiPost("/update", payload);
    checkFields.value = response.data?.checkFields || {};
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "更新員工失敗。"),
      );
      return;
    }
    await applyEmployee(response.data.value);
    toast.success(response.data.message);
  } catch (error: any) {
    toast.error(error?.message || "更新員工失敗。");
  } finally {
    hideLoading();
  }
};

const resetAssignment = () => {
  assignmentForm.value = newAssignmentForm();
  assignmentCheckFields.value = {};
};

const editAssignment = (value: any) => {
  assignmentForm.value = {
    ...value,
    employeeOid: String(route.params.id),
    effectiveFrom: toLocalInput(value.effectiveFrom),
    effectiveTo: toLocalInput(value.effectiveTo),
  };
  assignmentCheckFields.value = {};
};

const saveAssignment = async () => {
  assignmentCheckFields.value = {};
  showLoading();
  try {
    const payload = {
      ...assignmentForm.value,
      employeeOid: String(route.params.id),
      directManagerAssignmentId:
        assignmentForm.value.managerSource === "EXPLICIT"
          ? assignmentForm.value.directManagerAssignmentId
          : null,
      effectiveFrom: assignmentForm.value.effectiveFrom
        ? new Date(assignmentForm.value.effectiveFrom).toISOString()
        : null,
      effectiveTo: assignmentForm.value.effectiveTo
        ? new Date(assignmentForm.value.effectiveTo).toISOString()
        : null,
    };
    const response = await apiPost("/assignment/save", payload);
    assignmentCheckFields.value = response.data?.checkFields || {};
    if (
      !response.data ||
      response.data.success !== import.meta.env.VITE_SUCCESS_FLAG
    ) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存任職失敗。"),
      );
      return;
    }
    assignments.value = response.data.value || [];
    await loadAssignmentData();
    resetAssignment();
    toast.success(response.data.message || "任職資料已儲存。");
  } catch (error: any) {
    toast.error(error?.message || "儲存任職失敗。");
  } finally {
    hideLoading();
  }
};

const doDeactivateAssignment = async (value: any) => {
  showLoading();
  try {
    const response = await apiPost("/assignment/deactivate", {
      employeeOid: String(route.params.id),
      oid: value.oid,
    });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      assignments.value = response.data.value || [];
      resetAssignment();
      toast.success(response.data.message || "任職資料已停用。");
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用任職失敗。"),
      );
    }
  } catch (error: any) {
    toast.error(error?.message || "停用任職失敗。");
  } finally {
    hideLoading();
  }
};

const deactivateAssignment = (value: any) =>
  confirmFire(
    `確定停用「${value.orgUnitLabel}／${value.titleLabel}」任職？`,
    () => doDeactivateAssignment(value),
    value.oid,
  );

const doDeactivate = async () => {
  showLoading();
  try {
    const response = await apiPost("/deactivate", { oid: form.value.oid });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      await applyEmployee(response.data.value);
      toast.success(response.data.message);
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用員工失敗。"),
      );
    }
  } catch (error: any) {
    toast.error(error?.message || "停用員工失敗。");
  } finally {
    hideLoading();
  }
};

const btnDeactivate = () =>
  confirmFire("確定停用此員工？", doDeactivate, form.value.oid);

onMounted(async () => {
  await loadTenantOptions();
  await loadData();
});
</script>

<template>
  <Toolbar
    :progId="PageConstants.EditId"
    description="編輯員工"
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="loadData"
    @backMethod="router.back()"
    @saveMethod="btnSave"
  />

  <div class="card">
    <div class="card-body">
      <EmployeeForm
        v-model="form"
        :checkFields="checkFields"
        :tenantOptions="tenantOptions"
        :accountOptions="accountOptions"
        tenantReadonly
        employeeNoReadonly
      />
      <div class="row mt-4">
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="btnSave">
            <i class="bi bi-save"></i> 儲存
          </button>
          <button
            type="button"
            class="btn btn-outline-secondary"
            @click="loadData"
          >
            <i class="bi bi-repeat"></i> 重新載入
          </button>
          <button
            v-if="form.status === 'ACTIVE'"
            type="button"
            class="btn btn-outline-danger"
            @click="btnDeactivate"
          >
            <i class="bi bi-person-x"></i> 停用員工
          </button>
        </div>
      </div>
    </div>
  </div>

  <EmployeeAssignmentSection
    :assignments="assignments"
    :assignmentForm="assignmentForm"
    :checkFields="assignmentCheckFields"
    :orgUnitOptions="orgUnitOptions"
    :titleOptions="titleOptions"
    :managerOptions="managerOptions"
    @save="saveAssignment"
    @reset="resetAssignment"
    @edit="editAssignment"
    @deactivate="deactivateAssignment"
  />
</template>
