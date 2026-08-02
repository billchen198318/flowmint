<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import Toolbar from "@/components/Toolbar.vue";
import {
  checkInvalid,
  escapeQifuHtmlMsg,
  getAxiosInstance,
  invalidFeedback,
} from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";
const props = defineProps<{ edit: boolean }>(),
  route = useRoute(),
  router = useRouter(),
  tenants = ref<any[]>([]),
  units = ref<any[]>([]),
  levels = ref<any[]>([]),
  checkFields = ref<any>({});
const now = () => {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};
const blank = () => ({
  tenantId: "",
  orgUnitId: "",
  titleCode: "",
  titleName: "",
  approvalLevelId: "",
  isManagerTitle: "N",
  sortNo: 0,
  status: "ACTIVE",
  effectiveFrom: now(),
  effectiveTo: "",
  description: "",
});
const form = ref<any>(blank());
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const post = (p: string, b: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + p,
    b,
  );
const refs = async () => {
  if (!form.value.tenantId) {
    units.value = [];
    levels.value = [];
    return;
  }
  const [u, l] = await Promise.all([
    post("/org-unit-options", { tenantId: form.value.tenantId }),
    post("/level-options", { tenantId: form.value.tenantId }),
  ]);
  units.value = u.data?.value || [];
  levels.value = l.data?.value || [];
};
const tenantChanged = async () => {
  form.value.orgUnitId = "";
  form.value.approvalLevelId = "";
  await refs();
};
const local = (v: any) => {
  const f = (x: any) =>
    x
      ? new Date(
          new Date(x).getTime() - new Date(x).getTimezoneOffset() * 60000,
        )
          .toISOString()
          .slice(0, 16)
      : "";
  form.value = {
    ...v,
    effectiveFrom: f(v.effectiveFrom),
    effectiveTo: f(v.effectiveTo),
  };
};
const load = async () => {
  if (!props.edit) return;
  const x = await post("/load", { oid: route.params.id });
  if (x.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
    local(x.data.value);
    await refs();
  } else toast.warning(escapeQifuHtmlMsg(x.data?.message));
};
const save = async () => {
  const payload = {
    ...form.value,
    effectiveFrom: form.value.effectiveFrom
      ? new Date(form.value.effectiveFrom).toISOString()
      : null,
    effectiveTo: form.value.effectiveTo
      ? new Date(form.value.effectiveTo).toISOString()
      : null,
  };
  const x = await post(props.edit ? "/update" : "/save", payload);
  checkFields.value = x.data?.checkFields || {};
  if (x.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
    toast.warning(escapeQifuHtmlMsg(x.data?.message));
    return;
  }
  toast.success(x.data.message);
  if (props.edit) local(x.data.value);
  else form.value = blank();
};
const clear = async () => {
  checkFields.value = {};
  form.value = blank();
  await refs();
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message));
      return;
    }
    toast.success(response.data.message);
    local(response.data.value);
  } finally {
    hideLoading();
  }
};
const confirmDeactivate = () => confirmFire("確定停用此部門職稱？", deactivate);
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  await load();
});
</script>
<template>
  <Toolbar
    :progId="edit ? PageConstants.EditId : PageConstants.CreateId"
    description="部門職稱與簽核 Level"
    backFlag="Y"
    saveFlag="Y"
    refreshFlag="Y"
    @backMethod="router.back()"
    @saveMethod="save"
    @refreshMethod="edit ? load() : clear()"
  />
  <div class="card">
    <div class="card-body row g-3">
      <div class="col-md-4">
        <label for="tenantId" class="form-label">Tenant</label>
        <select
          id="tenantId"
          v-model="form.tenantId"
          :disabled="edit"
          :class="[
            'form-select',
            checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
          ]"
          @change="tenantChanged"
        >
          <option value="">請選擇</option>
          <option
            v-for="option in tenants"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <div
          v-if="checkInvalid('tenantId', checkFields)"
          class="invalid-feedback"
        >
          {{ invalidFeedback("tenantId", checkFields) }}
        </div>
      </div>
      <div class="col-md-4">
        <label for="orgUnitId" class="form-label">部門</label>
        <select
          id="orgUnitId"
          v-model="form.orgUnitId"
          :disabled="edit"
          :class="[
            'form-select',
            checkInvalid('orgUnitId', checkFields) ? 'is-invalid' : '',
          ]"
        >
          <option value="">請選擇</option>
          <option
            v-for="option in units"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <div
          v-if="checkInvalid('orgUnitId', checkFields)"
          class="invalid-feedback"
        >
          {{ invalidFeedback("orgUnitId", checkFields) }}
        </div>
      </div>
      <div class="col-md-4">
        <label for="approvalLevelId" class="form-label">簽核 Level</label>
        <select
          id="approvalLevelId"
          v-model="form.approvalLevelId"
          :class="[
            'form-select',
            checkInvalid('approvalLevelId', checkFields) ? 'is-invalid' : '',
          ]"
        >
          <option value="">請選擇</option>
          <option
            v-for="option in levels"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <div
          v-if="checkInvalid('approvalLevelId', checkFields)"
          class="invalid-feedback"
        >
          {{ invalidFeedback("approvalLevelId", checkFields) }}
        </div>
      </div>
      <div class="col-md-4">
        <label for="titleCode" class="form-label">職稱代碼</label>
        <input
          id="titleCode"
          v-model="form.titleCode"
          :class="[
            'form-control',
            checkInvalid('titleCode', checkFields) ? 'is-invalid' : '',
          ]"
        />
        <div
          v-if="checkInvalid('titleCode', checkFields)"
          class="invalid-feedback"
        >
          {{ invalidFeedback("titleCode", checkFields) }}
        </div>
      </div>
      <div class="col-md-4">
        <label for="titleName" class="form-label">職稱名稱</label>
        <input
          id="titleName"
          v-model="form.titleName"
          :class="[
            'form-control',
            checkInvalid('titleName', checkFields) ? 'is-invalid' : '',
          ]"
        />
        <div
          v-if="checkInvalid('titleName', checkFields)"
          class="invalid-feedback"
        >
          {{ invalidFeedback("titleName", checkFields) }}
        </div>
      </div>
      <div class="col-md-2">
        <label for="isManagerTitle" class="form-label">主管職稱</label
        ><select
          id="isManagerTitle"
          v-model="form.isManagerTitle"
          class="form-select"
        >
          <option value="N">否</option>
          <option value="Y">是</option>
        </select>
      </div>
      <div class="col-md-2">
        <label for="sortNo" class="form-label">排序</label
        ><input
          id="sortNo"
          v-model.number="form.sortNo"
          type="number"
          min="0"
          class="form-control"
        />
      </div>
      <div class="col-md-3">
        <label for="status" class="form-label">狀態</label
        ><select id="status" v-model="form.status" class="form-select">
          <option value="ACTIVE">啟用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="col-md-3">
        <label for="effectiveFrom" class="form-label">生效時間</label>
        <input
          id="effectiveFrom"
          v-model="form.effectiveFrom"
          type="datetime-local"
          :class="[
            'form-control',
            checkInvalid('effectiveFrom', checkFields) ? 'is-invalid' : '',
          ]"
        />
        <div
          v-if="checkInvalid('effectiveFrom', checkFields)"
          class="invalid-feedback"
        >
          {{ invalidFeedback("effectiveFrom", checkFields) }}
        </div>
      </div>
      <div class="col-md-3">
        <label for="effectiveTo" class="form-label">失效時間</label
        ><input
          id="effectiveTo"
          v-model="form.effectiveTo"
          type="datetime-local"
          class="form-control"
        />
      </div>
      <div class="col-12">
        <label for="description" class="form-label">說明</label
        ><textarea
          id="description"
          v-model="form.description"
          class="form-control"
          rows="3"
        ></textarea>
      </div>
      <div class="col-12 d-flex gap-2">
        <button type="button" class="btn btn-primary" @click="save">
          <i class="bi bi-save"></i> 儲存
        </button>
        <button
          v-if="!edit"
          type="button"
          class="btn btn-outline-secondary"
          @click="clear"
        >
          <i class="bi bi-eraser"></i> 清除
        </button>
        <button
          v-if="edit"
          type="button"
          class="btn btn-outline-secondary"
          @click="load"
        >
          <i class="bi bi-repeat"></i> 重新載入
        </button>
        <button
          v-if="edit && form.status === 'ACTIVE'"
          type="button"
          class="btn btn-outline-danger"
          @click="confirmDeactivate"
        >
          <i class="bi bi-slash-circle"></i> 停用職稱
        </button>
      </div>
    </div>
  </div>
</template>
