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
const assignments = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const assigneeCheckFields = ref<Record<string, string>>({});

const toLocal = (value: string | null) => {
  if (!value) return "";
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};

const newForm = () => ({
  tenantId: "",
  orgUnitId: "",
  dutyCode: "",
  dutyName: "",
  dutyType: "APPROVAL",
  status: "ACTIVE",
  effectiveFrom: toLocal(new Date().toISOString()),
  effectiveTo: "",
  description: "",
  assignees: [],
});
const newAssigneeForm = () => ({
  oid: "",
  dutyOid: String(route.params.id || ""),
  employeeOrgAssignmentId: "",
  isPrimary: "N",
  status: "ACTIVE",
  effectiveFrom: toLocal(new Date().toISOString()),
  effectiveTo: "",
});
const form = ref<any>(newForm());
const assigneeForm = ref<any>(newAssigneeForm());

const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

const loadUnits = async () => {
  units.value = form.value.tenantId
    ? (await post("/org-unit-options", { tenantId: form.value.tenantId })).data
        ?.value || []
    : [];
};

watch(
  () => form.value.tenantId,
  async () => {
    if (!props.edit) form.value.orgUnitId = "";
    await loadUnits();
  },
);

const loadAssignments = async () => {
  assignments.value = props.edit
    ? (await post("/assignment-options", { dutyOid: route.params.id })).data
        ?.value || []
    : [];
};

const apply = async (value: any) => {
  form.value = {
    ...value,
    effectiveFrom: toLocal(value.effectiveFrom),
    effectiveTo: toLocal(value.effectiveTo),
  };
  await loadUnits();
  await loadAssignments();
};

const clear = () => {
  checkFields.value = {};
  form.value = newForm();
  units.value = [];
};

const clearAssignee = () => {
  assigneeCheckFields.value = {};
  assigneeForm.value = newAssigneeForm();
};

const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取部門職務失敗。"),
      );
      return;
    }
    checkFields.value = {};
    await apply(response.data.value);
    clearAssignee();
  } catch (error: any) {
    toast.error(error?.message || "讀取部門職務失敗。");
  } finally {
    hideLoading();
  }
};

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
        escapeQifuHtmlMsg(response.data?.message || "儲存部門職務失敗。"),
      );
      return;
    }
    toast.success(response.data.message);
    if (props.edit) await apply(response.data.value);
    else clear();
  } catch (error: any) {
    toast.error(error?.message || "儲存部門職務失敗。");
  } finally {
    hideLoading();
  }
};

const saveAssignee = async () => {
  assigneeCheckFields.value = {};
  showLoading();
  try {
    const payload = {
      ...assigneeForm.value,
      dutyOid: String(route.params.id),
      effectiveFrom: assigneeForm.value.effectiveFrom
        ? new Date(assigneeForm.value.effectiveFrom).toISOString()
        : null,
      effectiveTo: assigneeForm.value.effectiveTo
        ? new Date(assigneeForm.value.effectiveTo).toISOString()
        : null,
    };
    const response = await post("/assignee/save", payload);
    assigneeCheckFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存擔任人失敗。"),
      );
      return;
    }
    await apply(response.data.value);
    clearAssignee();
    toast.success(response.data.message);
  } catch (error: any) {
    toast.error(error?.message || "儲存擔任人失敗。");
  } finally {
    hideLoading();
  }
};

const editAssignee = (value: any) => {
  assigneeCheckFields.value = {};
  assigneeForm.value = {
    ...value,
    dutyOid: String(route.params.id),
    effectiveFrom: toLocal(value.effectiveFrom),
    effectiveTo: toLocal(value.effectiveTo),
  };
};

const doDeactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      await apply(response.data.value);
      toast.success(response.data.message);
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用部門職務失敗。"),
      );
    }
  } finally {
    hideLoading();
  }
};

const doDeactivateAssignee = async (value: any) => {
  showLoading();
  try {
    const response = await post("/assignee/deactivate", {
      dutyOid: String(route.params.id),
      oid: value.oid,
    });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      await apply(response.data.value);
      clearAssignee();
      toast.success(response.data.message);
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用擔任人失敗。"),
      );
    }
  } finally {
    hideLoading();
  }
};

const refresh = () => (props.edit ? load() : clear());

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
        ? '選配功能：維護特定部門職務及擔任人。只有流程必須找到部門內固定專責人員時才需設定；一般主管簽核不需要使用此功能。'
        : '選配功能：建立部門專責職務，例如資訊部資安會簽人。一般主管簽核請使用部門主管配置，跨部門固定成員請使用簽核群組。'
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
      <div class="alert alert-info">
        此為非必要設定。大多數流程只需使用部門主管或簽核群組；只有需要依部門尋找固定專責人員時才建立部門職務。
      </div>
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label">Tenant</label>
          <select
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
          <label class="form-label">部門</label>
          <select
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
        <div class="col-md-2">
          <label class="form-label">職務代碼</label>
          <input
            v-model="form.dutyCode"
            :readonly="props.edit"
            :class="[
              'form-control',
              checkInvalid('dutyCode', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("dutyCode", checkFields) }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">職務名稱</label>
          <input
            v-model="form.dutyName"
            :class="[
              'form-control',
              checkInvalid('dutyName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("dutyName", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">用途</label>
          <select v-model="form.dutyType" class="form-select">
            <option value="APPROVAL">簽核</option>
            <option value="REVIEW">會簽／審查</option>
            <option value="NOTIFY">通知</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label>
          <select v-model="form.status" class="form-select">
            <option value="ACTIVE">啟用</option>
            <option value="INACTIVE">停用</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">生效時間</label>
          <input
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
        <div class="col-md-3">
          <label class="form-label">失效時間</label>
          <input
            v-model="form.effectiveTo"
            type="datetime-local"
            class="form-control"
          />
        </div>
        <div class="col-12">
          <label class="form-label">說明</label>
          <textarea
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
            @click="confirmFire('確定停用此部門職務？', doDeactivate, form.oid)"
          >
            <i class="bi bi-slash-circle"></i> 停用職務
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="props.edit" class="card mt-4">
    <div class="card-header">職務擔任人</div>
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-5">
          <label class="form-label">擔任人</label>
          <select
            v-model="assigneeForm.employeeOrgAssignmentId"
            :class="[
              'form-select',
              checkInvalid('employeeOrgAssignmentId', assigneeCheckFields)
                ? 'is-invalid'
                : '',
            ]"
          >
            <option value="">請選擇所屬部門的有效任職員工</option>
            <option
              v-for="item in assignments"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{
              invalidFeedback("employeeOrgAssignmentId", assigneeCheckFields)
            }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">主要擔任人</label>
          <select v-model="assigneeForm.isPrimary" class="form-select">
            <option value="Y">是</option>
            <option value="N">否</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label>
          <select v-model="assigneeForm.status" class="form-select">
            <option value="ACTIVE">啟用</option>
            <option value="INACTIVE">停用</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">生效時間</label>
          <input
            v-model="assigneeForm.effectiveFrom"
            type="datetime-local"
            :class="[
              'form-control',
              checkInvalid('effectiveFrom', assigneeCheckFields)
                ? 'is-invalid'
                : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("effectiveFrom", assigneeCheckFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">失效時間</label>
          <input
            v-model="assigneeForm.effectiveTo"
            type="datetime-local"
            class="form-control"
          />
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="saveAssignee">
            <i class="bi bi-person-plus"></i>
            {{ assigneeForm.oid ? "更新擔任人" : "新增擔任人" }}
          </button>
          <button
            type="button"
            class="btn btn-outline-secondary"
            @click="clearAssignee"
          >
            <i class="bi bi-eraser"></i> 清除擔任人表單
          </button>
        </div>
      </div>

      <div class="table-responsive mt-4">
        <table class="table table-striped table-hover align-middle">
          <thead>
            <tr>
              <th>擔任人</th>
              <th>主要</th>
              <th>狀態</th>
              <th>生效時間</th>
              <th>失效時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in form.assignees || []" :key="item.oid">
              <td>{{ item.employeeLabel }}</td>
              <td>{{ item.isPrimary === "Y" ? "是" : "否" }}</td>
              <td>{{ item.status === "ACTIVE" ? "啟用" : "停用" }}</td>
              <td>{{ item.effectiveFrom }}</td>
              <td>{{ item.effectiveTo || "—" }}</td>
              <td>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-primary me-2"
                  @click="editAssignee(item)"
                >
                  編輯
                </button>
                <button
                  v-if="item.status === 'ACTIVE'"
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="
                    confirmFire(
                      '確定停用此擔任人？',
                      () => doDeactivateAssignee(item),
                      item.oid,
                    )
                  "
                >
                  停用
                </button>
              </td>
            </tr>
            <tr v-if="(form.assignees || []).length === 0">
              <td colspan="6" class="text-center text-muted py-4">
                尚未設定擔任人
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
