<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import Toolbar from "@/components/Toolbar.vue";
import {
  checkInvalid,
  escapeQifuHtmlMsg,
  getAxiosInstance,
  invalidFeedback,
} from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<any[]>([]);
const units = ref<any[]>([]);
const employees = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const toLocal = (value: string | null) =>
  value
    ? new Date(
        new Date(value).getTime() - new Date(value).getTimezoneOffset() * 60000,
      )
        .toISOString()
        .slice(0, 16)
    : "";
const newForm = () => ({
  tenantId: "",
  orgUnitId: "",
  employeeId: "",
  headType: "HEAD",
  priority: 100,
  status: "ACTIVE",
  effectiveFrom: toLocal(new Date().toISOString()),
  effectiveTo: "",
  description: "",
});
const form = ref<any>(newForm());
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const loadUnits = async () =>
  (units.value = form.value.tenantId
    ? (await post("/org-unit-options", { tenantId: form.value.tenantId })).data
        ?.value || []
    : []);
const loadEmployees = async () =>
  (employees.value = form.value.orgUnitId
    ? (
        await post("/employee-options", {
          tenantId: form.value.tenantId,
          orgUnitId: form.value.orgUnitId,
        })
      ).data?.value || []
    : []);
watch(
  () => form.value.tenantId,
  async () => {
    if (!props.edit) {
      form.value.orgUnitId = "";
      form.value.employeeId = "";
      employees.value = [];
    }
    await loadUnits();
  },
);
watch(
  () => form.value.orgUnitId,
  async () => {
    if (!props.edit) form.value.employeeId = "";
    await loadEmployees();
  },
);
const apply = async (value: any) => {
  form.value = {
    ...value,
    effectiveFrom: toLocal(value.effectiveFrom),
    effectiveTo: toLocal(value.effectiveTo),
  };
  await loadUnits();
  await loadEmployees();
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取主管配置失敗。"),
      );
      return;
    }
    await apply(response.data.value);
  } catch (error: any) {
    toast.error(error?.message || "讀取主管配置失敗。");
  } finally {
    hideLoading();
  }
};
const clear = () => {
  checkFields.value = {};
  form.value = newForm();
  units.value = [];
  employees.value = [];
};
const refresh = () => (props.edit ? load() : clear());
const save = async () => {
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
    const response = await post(props.edit ? "/update" : "/save", payload);
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存主管配置失敗。"),
      );
      return;
    }
    toast.success(response.data.message);
    if (props.edit) await apply(response.data.value);
    else clear();
  } catch (error: any) {
    toast.error(error?.message || "儲存主管配置失敗。");
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      await apply(response.data.value);
      toast.success(response.data.message);
    } else
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用主管配置失敗。"),
      );
  } finally {
    hideLoading();
  }
};
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  await load();
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="
      props.edit
        ? '編輯部門主管配置。主要主管在同一部門的有效期間不可重疊；副主管可多人並依優先序處理；代理主管應設定明確代理期間。主管候選人只列出已在所選部門建立有效任職的員工。'
        : '新增部門主管配置。請先選 Tenant 與部門，再從該部門有效任職員工中選擇主管。一般正式主管選主要主管；協助主管選副主管；短期代理請選代理主管並設定起訖時間。'
    "
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="refresh"
    @backMethod="router.back()"
    @saveMethod="save"
  />
  <div class="card">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label">Tenant</label
          ><select
            v-model="form.tenantId"
            :disabled="props.edit"
            :class="[
              'form-select',
              checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇 Tenant</option>
            <option
              v-for="item in tenants"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("tenantId", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">部門</label
          ><select
            v-model="form.orgUnitId"
            :class="[
              'form-select',
              checkInvalid('orgUnitId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇部門</option>
            <option v-for="item in units" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("orgUnitId", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">主管員工</label
          ><select
            v-model="form.employeeId"
            :class="[
              'form-select',
              checkInvalid('employeeId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇主管員工</option>
            <option
              v-for="item in employees"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("employeeId", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">主管類型</label
          ><select
            v-model="form.headType"
            :class="[
              'form-select',
              checkInvalid('headType', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="HEAD">主要主管</option>
            <option value="DEPUTY_HEAD">副主管</option>
            <option value="ACTING_HEAD">代理主管</option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("headType", checkFields) }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">優先序</label
          ><input
            v-model.number="form.priority"
            type="number"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label
          ><select v-model="form.status" class="form-select">
            <option value="ACTIVE">啟用</option>
            <option value="INACTIVE">停用</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">生效時間</label
          ><input
            v-model="form.effectiveFrom"
            type="datetime-local"
            :class="[
              'form-control',
              checkInvalid('effectiveFrom', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("effectiveFrom", checkFields) }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">失效時間</label
          ><input
            v-model="form.effectiveTo"
            type="datetime-local"
            class="form-control"
          />
        </div>
        <div class="col-12">
          <label class="form-label">說明</label
          ><textarea
            v-model="form.description"
            maxlength="500"
            class="form-control"
          ></textarea>
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="save">
            <i class="bi bi-save"></i> 儲存
          </button>
          <button
            v-if="!props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="clear"
          >
            <i class="bi bi-eraser"></i> 清除
          </button>
          <button
            v-if="props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="load"
          >
            <i class="bi bi-repeat"></i> 重新載入
          </button>
          <button
            v-if="props.edit && form.status === 'ACTIVE'"
            type="button"
            class="btn btn-outline-danger"
            @click="confirmFire('確定停用此主管配置？', deactivate, form.oid)"
          >
            <i class="bi bi-x-circle"></i> 停用
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
