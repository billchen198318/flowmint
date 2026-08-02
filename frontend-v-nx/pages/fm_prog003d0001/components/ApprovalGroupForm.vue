<script setup lang="ts">
import { onMounted, ref } from "vue";
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
const employees = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const memberCheckFields = ref<Record<string, string>>({});

const toLocal = (value: string | null) => {
  if (!value) return "";
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};
const newForm = () => ({
  tenantId: "",
  groupCode: "",
  groupName: "",
  assignmentMode: "CANDIDATE",
  status: "ACTIVE",
  description: "",
  members: [],
});
const newMemberForm = () => ({
  oid: "",
  groupOid: String(route.params.id || ""),
  employeeId: "",
  priority: 100,
  status: "ACTIVE",
  effectiveFrom: toLocal(new Date().toISOString()),
  effectiveTo: "",
});
const form = ref<any>(newForm());
const memberForm = ref<any>(newMemberForm());
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

const loadEmployees = async () => {
  employees.value = props.edit
    ? (await post("/employee-options", { groupOid: route.params.id })).data
        ?.value || []
    : [];
};
const apply = async (value: any) => {
  form.value = { ...value };
  await loadEmployees();
};
const clear = () => {
  checkFields.value = {};
  form.value = newForm();
};
const clearMember = () => {
  memberCheckFields.value = {};
  memberForm.value = newMemberForm();
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取簽核群組失敗。"),
      );
      return;
    }
    checkFields.value = {};
    await apply(response.data.value);
    clearMember();
  } catch (error: any) {
    toast.error(error?.message || "讀取簽核群組失敗。");
  } finally {
    hideLoading();
  }
};
const save = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const response = await post(props.edit ? "/update" : "/save", form.value);
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存簽核群組失敗。"),
      );
      return;
    }
    toast.success(response.data.message);
    if (props.edit) await apply(response.data.value);
    else clear();
  } catch (error: any) {
    toast.error(error?.message || "儲存簽核群組失敗。");
  } finally {
    hideLoading();
  }
};
const saveMember = async () => {
  memberCheckFields.value = {};
  showLoading();
  try {
    const payload = {
      ...memberForm.value,
      groupOid: String(route.params.id),
      effectiveFrom: memberForm.value.effectiveFrom
        ? new Date(memberForm.value.effectiveFrom).toISOString()
        : null,
      effectiveTo: memberForm.value.effectiveTo
        ? new Date(memberForm.value.effectiveTo).toISOString()
        : null,
    };
    const response = await post("/member/save", payload);
    memberCheckFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存群組成員失敗。"),
      );
      return;
    }
    await apply(response.data.value);
    clearMember();
    toast.success(response.data.message);
  } catch (error: any) {
    toast.error(error?.message || "儲存群組成員失敗。");
  } finally {
    hideLoading();
  }
};
const editMember = (value: any) => {
  memberCheckFields.value = {};
  memberForm.value = {
    ...value,
    groupOid: String(route.params.id),
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
        escapeQifuHtmlMsg(response.data?.message || "停用簽核群組失敗。"),
      );
    }
  } finally {
    hideLoading();
  }
};
const doDeactivateMember = async (value: any) => {
  showLoading();
  try {
    const response = await post("/member/deactivate", {
      groupOid: String(route.params.id),
      oid: value.oid,
    });
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      await apply(response.data.value);
      clearMember();
      toast.success(response.data.message);
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用群組成員失敗。"),
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
        ? '編輯跨部門簽核群組與有效成員。任一成員處理代表其中一人完成即可；全員處理代表每位成員都必須處理；依優先序逐一處理會按數字由小到大交辦。'
        : '建立跨部門簽核群組。請先選擇處理方式；新增完成後，再到編輯頁加入有效員工並設定優先序與有效期間。'
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
          <label class="form-label">群組代碼</label>
          <input
            v-model="form.groupCode"
            :class="[
              'form-control',
              checkInvalid('groupCode', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("groupCode", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">群組名稱</label>
          <input
            v-model="form.groupName"
            :class="[
              'form-control',
              checkInvalid('groupName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("groupName", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">處理方式</label>
          <select v-model="form.assignmentMode" class="form-select">
            <option value="CANDIDATE">任一成員處理</option>
            <option value="ALL">全員都要處理</option>
            <option value="SEQUENTIAL">依優先序逐一處理</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label>
          <select v-model="form.status" class="form-select">
            <option value="ACTIVE">啟用</option>
            <option value="INACTIVE">停用</option>
          </select>
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
            @click="confirmFire('確定停用此簽核群組？', doDeactivate, form.oid)"
          >
            <i class="bi bi-slash-circle"></i> 停用群組
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="props.edit" class="card mt-4">
    <div class="card-header">群組成員</div>
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-5">
          <label class="form-label">員工</label>
          <select
            v-model="memberForm.employeeId"
            :class="[
              'form-select',
              checkInvalid('employeeId', memberCheckFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇 Tenant 內的有效員工</option>
            <option
              v-for="item in employees"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("employeeId", memberCheckFields) }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">優先序</label>
          <input
            v-model.number="memberForm.priority"
            type="number"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label>
          <select v-model="memberForm.status" class="form-select">
            <option value="ACTIVE">啟用</option>
            <option value="INACTIVE">停用</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">生效時間</label>
          <input
            v-model="memberForm.effectiveFrom"
            type="datetime-local"
            :class="[
              'form-control',
              checkInvalid('effectiveFrom', memberCheckFields)
                ? 'is-invalid'
                : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("effectiveFrom", memberCheckFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">失效時間</label>
          <input
            v-model="memberForm.effectiveTo"
            type="datetime-local"
            class="form-control"
          />
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="saveMember">
            <i class="bi bi-person-plus"></i>
            {{ memberForm.oid ? "更新成員" : "新增成員" }}
          </button>
          <button
            type="button"
            class="btn btn-outline-secondary"
            @click="clearMember"
          >
            <i class="bi bi-eraser"></i> 清除成員表單
          </button>
        </div>
      </div>
      <div class="table-responsive mt-4">
        <table class="table table-striped table-hover align-middle">
          <thead>
            <tr>
              <th>員工</th>
              <th>優先序</th>
              <th>狀態</th>
              <th>生效時間</th>
              <th>失效時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in form.members || []" :key="item.oid">
              <td>{{ item.employeeLabel }}</td>
              <td>{{ item.priority }}</td>
              <td>{{ item.status === "ACTIVE" ? "啟用" : "停用" }}</td>
              <td>{{ item.effectiveFrom }}</td>
              <td>{{ item.effectiveTo || "—" }}</td>
              <td>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-primary me-2"
                  @click="editMember(item)"
                >
                  編輯
                </button>
                <button
                  v-if="item.status === 'ACTIVE'"
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="
                    confirmFire(
                      '確定停用此群組成員？',
                      () => doDeactivateMember(item),
                      item.oid,
                    )
                  "
                >
                  停用
                </button>
              </td>
            </tr>
            <tr v-if="(form.members || []).length === 0">
              <td colspan="6" class="text-center text-muted py-4">
                尚未設定群組成員
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
