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
const accounts = ref<any[]>([]);
const groups = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});

const toLocal = (value: string | null) => {
  if (!value) return "";
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};
const newForm = () => ({
  oid: "",
  tenantId: "",
  principalAccount: "",
  delegateAccount: "",
  scopeType: "ALL",
  scopeRefId: "",
  allowRedelegate: "N",
  status: "ACTIVE",
  effectiveFrom: toLocal(new Date().toISOString()),
  effectiveTo: "",
  reason: "",
});
const form = ref<any>(newForm());
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );

const loadOptions = async () => {
  if (!form.value.tenantId) {
    accounts.value = [];
    groups.value = [];
    return;
  }
  const [accountResponse, groupResponse] = await Promise.all([
    post("/account-options", { tenantId: form.value.tenantId }),
    post("/group-options", { tenantId: form.value.tenantId }),
  ]);
  accounts.value = accountResponse.data?.value || [];
  groups.value = groupResponse.data?.value || [];
};
const changeTenant = async () => {
  form.value.principalAccount = "";
  form.value.delegateAccount = "";
  form.value.scopeRefId = "";
  await loadOptions();
};
const changeScope = () => {
  form.value.scopeRefId = "";
};
const apply = async (value: any) => {
  form.value = {
    ...value,
    effectiveFrom: toLocal(value.effectiveFrom),
    effectiveTo: toLocal(value.effectiveTo),
  };
  await loadOptions();
};
const clear = () => {
  checkFields.value = {};
  form.value = newForm();
  accounts.value = [];
  groups.value = [];
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "讀取工作代理失敗。"),
      );
      router.push(PageConstants.frontendNamespace);
      return;
    }
    await apply(response.data.value);
  } catch (error: any) {
    toast.error(error?.message || "讀取工作代理失敗。");
    router.push(PageConstants.frontendNamespace);
  } finally {
    hideLoading();
  }
};
const validate = () => {
  const fields: Record<string, string> = {};
  if (!form.value.tenantId) fields.tenantId = "請選擇 Tenant";
  if (!form.value.principalAccount) fields.principalAccount = "請選擇被代理人";
  if (!form.value.delegateAccount) fields.delegateAccount = "請選擇代理人";
  if (!form.value.scopeType) fields.scopeType = "請選擇代理範圍";
  if (form.value.scopeType !== "ALL" && !form.value.scopeRefId) {
    fields.scopeRefId = "請指定代理範圍";
  }
  if (!form.value.effectiveFrom) fields.effectiveFrom = "請輸入開始時間";
  if (!form.value.effectiveTo) fields.effectiveTo = "請輸入結束時間";
  if (
    form.value.effectiveFrom &&
    form.value.effectiveTo &&
    new Date(form.value.effectiveTo) <= new Date(form.value.effectiveFrom)
  ) {
    fields.effectiveTo = "結束時間必須晚於開始時間";
  }
  if (!form.value.reason?.trim()) fields.reason = "請輸入代理原因";
  checkFields.value = fields;
  if (Object.keys(fields).length) {
    toast.warning(Object.values(fields)[0]);
    return false;
  }
  return true;
};
const save = async () => {
  if (!validate()) return;
  showLoading();
  try {
    const payload = {
      ...form.value,
      effectiveFrom: new Date(form.value.effectiveFrom).toISOString(),
      effectiveTo: new Date(form.value.effectiveTo).toISOString(),
    };
    const response = await post(props.edit ? "/update" : "/save", payload);
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存工作代理失敗。"),
      );
      return;
    }
    toast.success(response.data.message);
    if (props.edit) await apply(response.data.value);
    else clear();
  } catch (error: any) {
    toast.error(error?.message || "儲存工作代理失敗。");
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
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用工作代理失敗。"),
      );
    }
  } finally {
    hideLoading();
  }
};

onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  if (!props.edit && !form.value.tenantId && tenants.value.length === 1) {
    form.value.tenantId = tenants.value[0].value;
    await loadOptions();
  }
  await load();
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="
      props.edit
        ? '維護既有工作代理的代理人、適用範圍與有效期間。被代理人建立後不可更換；如需改由另一位員工提出代理，請停用舊設定後新增。'
        : '建立員工工作代理。被代理人是暫時無法處理簽核的人；代理人會在指定期間及範圍內代為處理。請確認兩人皆為同一 Tenant 的有效帳號。'
    "
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="props.edit ? load() : clear()"
    @backMethod="router.back()"
    @saveMethod="save"
  />
  <div class="card">
    <div class="card-body">
      <div class="alert alert-info">
        全部流程適用於期間內所有簽核；指定流程只代理輸入的流程識別碼；指定簽核群組只代理該群組相關工作。允許再次轉代理可能延長代理鏈，非必要請維持「否」。
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
            @change="changeTenant"
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
          <label class="form-label">被代理人</label>
          <select
            v-model="form.principalAccount"
            :disabled="props.edit"
            :class="[
              'form-select',
              checkInvalid('principalAccount', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇被代理人</option>
            <option
              v-for="item in accounts"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("principalAccount", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">代理人</label>
          <select
            v-model="form.delegateAccount"
            :class="[
              'form-select',
              checkInvalid('delegateAccount', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇代理人</option>
            <option
              v-for="item in accounts"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("delegateAccount", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">代理範圍</label>
          <select
            v-model="form.scopeType"
            class="form-select"
            @change="changeScope"
          >
            <option value="ALL">全部流程</option>
            <option value="PROCESS">指定流程</option>
            <option value="APPROVAL_GROUP">指定簽核群組</option>
          </select>
        </div>
        <div v-if="form.scopeType === 'PROCESS'" class="col-md-5">
          <label class="form-label">流程識別碼</label>
          <input
            v-model="form.scopeRefId"
            :class="[
              'form-control',
              checkInvalid('scopeRefId', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("scopeRefId", checkFields) }}
          </div>
        </div>
        <div v-if="form.scopeType === 'APPROVAL_GROUP'" class="col-md-5">
          <label class="form-label">簽核群組</label>
          <select
            v-model="form.scopeRefId"
            :class="[
              'form-select',
              checkInvalid('scopeRefId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇簽核群組</option>
            <option
              v-for="item in groups"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("scopeRefId", checkFields) }}
          </div>
        </div>
        <div class="col-md-2">
          <label class="form-label">允許再次轉代理</label>
          <select v-model="form.allowRedelegate" class="form-select">
            <option value="N">否</option>
            <option value="Y">是</option>
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
          <label class="form-label">開始時間</label>
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
          <label class="form-label">結束時間</label>
          <input
            v-model="form.effectiveTo"
            type="datetime-local"
            :class="[
              'form-control',
              checkInvalid('effectiveTo', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("effectiveTo", checkFields) }}
          </div>
        </div>
        <div class="col-12">
          <label class="form-label">代理原因</label>
          <textarea
            v-model="form.reason"
            maxlength="500"
            :class="[
              'form-control',
              checkInvalid('reason', checkFields) ? 'is-invalid' : '',
            ]"
          ></textarea>
          <div class="invalid-feedback">
            {{ invalidFeedback("reason", checkFields) }}
          </div>
        </div>
        <div class="col-12 d-flex gap-2">
          <button class="btn btn-primary" @click="save">
            <i class="bi bi-save"></i> 儲存
          </button>
          <button
            v-if="!props.edit"
            class="btn btn-outline-secondary"
            @click="clear"
          >
            <i class="bi bi-eraser"></i> 清除
          </button>
          <button
            v-if="props.edit"
            class="btn btn-outline-secondary"
            @click="load"
          >
            <i class="bi bi-repeat"></i> 重新載入
          </button>
          <button
            v-if="props.edit && form.status === 'ACTIVE'"
            class="btn btn-outline-danger"
            @click="confirmFire('確定停用此工作代理？', deactivate, form.oid)"
          >
            <i class="bi bi-slash-circle"></i> 停用
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
