<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";

import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import TenantForm from "../components/TenantForm.vue";
import { PageConstants } from "../config";

definePageMeta({ middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();

const checkFields = ref<Record<string, string>>({});
const accounts = ref<any[]>([]);
const form = ref<any>({
  oid: "",
  tenantId: "",
  tenantCode: "",
  tenantName: "",
  defaultLocale: "zh-TW",
  defaultTimezone: "Asia/Taipei",
  status: "ACTIVE",
  description: "",
});
const accountForm = ref<any>({
  tenantOid: "",
  account: "",
  createNewAccount: false,
  password: "",
  confirmPassword: "",
  isDefault: "N",
  status: "ACTIVE",
  effectiveFrom: "",
  effectiveTo: null,
});

const resetPasswordForm = ref({
  account: "",
  password: "",
  confirmPassword: "",
});
const showResetPassword = ref(false);
const applyTenant = (value: any) => {
  form.value = { ...value };
  accounts.value = value.accounts || [];
  accountForm.value.tenantOid = value.oid;
};

const resetAccountForm = () => {
  accountForm.value = {
    tenantOid: form.value.oid,
    account: "",
    createNewAccount: false,
    password: "",
    confirmPassword: "",
    isDefault: "N",
    status: "ACTIVE",
    effectiveFrom: "",
    effectiveTo: null,
  };
};

const btnBack = () => router.back();

const loadData = async () => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/load",
      { oid: route.params.id },
    );

    if (!response.data) {
      toast.error("後端未回傳資料。");
      router.push(PageConstants.frontendNamespace);
      return;
    }
    if (response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data.message));
      router.push(PageConstants.frontendNamespace);
      return;
    }

    checkFields.value = {};
    applyTenant(response.data.value);
  } catch (error: any) {
    toast.error(error?.message || "讀取 Tenant 失敗。");
  } finally {
    hideLoading();
  }
};

const btnSave = async () => {
  checkFields.value = {};
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/update",
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

    applyTenant(response.data.value);
    toast.success(response.data.message);
  } catch (error: any) {
    toast.error(error?.message || "更新 Tenant 失敗。");
  } finally {
    hideLoading();
  }
};

const validateAccount = (): boolean => {
  if (!accountForm.value.account?.trim()) {
    toast.warning("請輸入帳號。");
    return false;
  }
  if (!accountForm.value.effectiveFrom) {
    toast.warning("請輸入生效時間。");
    return false;
  }
  if (accountForm.value.createNewAccount && !accountForm.value.password) {
    toast.warning("建立新帳號時，初始密碼為必填。");
    return false;
  }
  if (
    accountForm.value.createNewAccount &&
    accountForm.value.password !== accountForm.value.confirmPassword
  ) {
    toast.warning("密碼與確認密碼不一致。");
    return false;
  }
  return true;
};

const btnAddAccount = async () => {
  if (!validateAccount()) {
    return;
  }

  const payload = {
    ...accountForm.value,
    effectiveFrom: new Date(accountForm.value.effectiveFrom).toISOString(),
    effectiveTo: accountForm.value.effectiveTo
      ? new Date(accountForm.value.effectiveTo).toISOString()
      : null,
  };

  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/account/save",
      payload,
    );

    if (!response.data) {
      toast.error("後端未回傳資料。");
      return;
    }
    if (response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data.message));
      return;
    }

    applyTenant(response.data.value);
    resetAccountForm();
    toast.success(response.data.message);
  } catch (error: any) {
    toast.error(error?.message || "加入 Tenant Account 失敗。");
  } finally {
    hideLoading();
  }
};

const openResetPassword = (account: string) => {
  resetPasswordForm.value = { account, password: "", confirmPassword: "" };
  showResetPassword.value = true;
};

const closeResetPassword = () => {
  showResetPassword.value = false;
  resetPasswordForm.value.password = "";
  resetPasswordForm.value.confirmPassword = "";
};

const btnResetPassword = async () => {
  if (!resetPasswordForm.value.password) {
    toast.warning("請輸入新密碼。");
    return;
  }
  if (
    resetPasswordForm.value.password !==
    resetPasswordForm.value.confirmPassword
  ) {
    toast.warning("密碼與確認密碼不一致。");
    return;
  }

  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/account/reset-password",
      { tenantOid: form.value.oid, ...resetPasswordForm.value },
    );
    if (!response.data) {
      toast.error("後端未回傳資料。");
      return;
    }
    if (response.data.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data.message));
      return;
    }
    applyTenant(response.data.value);
    closeResetPassword();
    toast.success("密碼重設成功。");
  } catch (error: any) {
    toast.error(error?.message || "重設密碼失敗。");
  } finally {
    hideLoading();
  }
};
const doDeactivateAccount = async (account: string) => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/account/deactivate",
      { tenantOid: form.value.oid, account },
    );
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      applyTenant(response.data.value);
      toast.success("帳號已全域停用。");
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "停用帳號失敗。"),
      );
    }
  } catch (error: any) {
    toast.error(error?.message || "停用帳號失敗。");
  } finally {
    hideLoading();
  }
};

const btnDeactivateAccount = (account: string) => {
  confirmFire(
    `確定全域停用帳號 ${account}？此帳號將無法登入，所有 Tenant 權限也會立即失效。`,
    doDeactivateAccount,
    account,
  );
};
const doActivateAccount = async (account: string) => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/account/activate",
      { tenantOid: form.value.oid, account },
    );
    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      applyTenant(response.data.value);
      toast.success("帳號已全域啟用。");
    } else {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "啟用帳號失敗。"),
      );
    }
  } catch (error: any) {
    toast.error(error?.message || "啟用帳號失敗。");
  } finally {
    hideLoading();
  }
};

const btnActivateAccount = (account: string) => {
  confirmFire(
    `確定全域啟用帳號 ${account}？此帳號的所有 Tenant 權限都會恢復為有效。`,
    doActivateAccount,
    account,
  );
};
const doDeactivate = async () => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/deactivate",
      { oid: form.value.oid },
    );

    if (response.data?.success === import.meta.env.VITE_SUCCESS_FLAG) {
      applyTenant(response.data.value);
      toast.success(response.data.message);
    } else {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "停用失敗。"));
    }
  } catch (error: any) {
    toast.error(error?.message || "停用 Tenant 失敗。");
  } finally {
    hideLoading();
  }
};

const btnDeactivate = () => {
  confirmFire("確定停用此 Tenant？", doDeactivate, form.value.oid);
};

onMounted(loadData);
</script>

<template>
  <Toolbar
    :progId="PageConstants.EditId"
    description="編輯 Tenant 與帳號範圍"
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="loadData"
    @backMethod="btnBack"
    @saveMethod="btnSave"
  />

  <div class="card mb-3">
    <div class="card-body">
      <TenantForm v-model="form" :checkFields="checkFields" tenantIdReadonly />

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
            <i class="bi bi-slash-circle"></i> 停用 Tenant
          </button>
        </div>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header">Tenant Account</div>
    <div class="card-body">
      <div class="row g-3 mb-3">
        <div class="col-md-3">
          <label for="account" class="form-label">帳號</label>
          <input
            id="account"
            v-model="accountForm.account"
            class="form-control"
            placeholder="請輸入帳號"
          />
        </div>
        <div class="col-md-2 d-flex align-items-end pb-2">
          <div class="form-check">
            <input
              id="createNewAccount"
              v-model="accountForm.createNewAccount"
              type="checkbox"
              class="form-check-input"
            />
            <label for="createNewAccount" class="form-check-label"
              >建立新帳號</label
            >
          </div>
        </div>
        <div v-if="accountForm.createNewAccount" class="col-md-2">
          <label for="password" class="form-label">初始密碼</label>
          <input
            id="password"
            v-model="accountForm.password"
            type="password"
            class="form-control"
            autocomplete="new-password"
          />
        </div>
        <div v-if="accountForm.createNewAccount" class="col-md-2">
          <label for="confirmPassword" class="form-label">確認密碼</label>
          <input
            id="confirmPassword"
            v-model="accountForm.confirmPassword"
            type="password"
            class="form-control"
            autocomplete="new-password"
          />
        </div>
        <div class="col-md-3">
          <label for="effectiveFrom" class="form-label">生效時間</label>
          <input
            id="effectiveFrom"
            v-model="accountForm.effectiveFrom"
            type="datetime-local"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <label for="isDefault" class="form-label">Tenant 類型</label>
          <select
            id="isDefault"
            v-model="accountForm.isDefault"
            class="form-select"
          >
            <option value="N">一般 Tenant</option>
            <option value="Y">預設 Tenant</option>
          </select>
        </div>
        <div class="col-md-2 d-flex align-items-end">
          <button type="button" class="btn btn-primary" @click="btnAddAccount">
            <i class="bi bi-person-plus"></i> 加入帳號
          </button>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-sm">
          <thead>
            <tr>
              <th>帳號</th>
              <th>預設</th>
              <th>狀態</th>
              <th>生效時間</th>
              <th>失效時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in accounts" :key="item.oid">
              <td>{{ item.account }}</td>
              <td>{{ item.isDefault }}</td>
              <td>{{ item.status }}</td>
              <td>{{ item.effectiveFrom }}</td>
              <td>{{ item.effectiveTo || "-" }}</td>
              <td>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-warning"
                  @click="openResetPassword(item.account)"
                >
                  <i class="bi bi-key"></i> 重設密碼
                </button>                <button
                  v-if="item.status === 'ACTIVE'"
                  type="button"
                  class="btn btn-sm btn-outline-danger ms-1"
                  @click="btnDeactivateAccount(item.account)"
                >
                  <i class="bi bi-person-x"></i> 全域停用
                </button>                <button
                  v-if="item.status === 'INACTIVE'"
                  type="button"
                  class="btn btn-sm btn-outline-success ms-1"
                  @click="btnActivateAccount(item.account)"
                >
                  <i class="bi bi-person-check"></i> 全域啟用
                </button>
              </td>
            </tr>
            <tr v-if="accounts.length === 0">
              <td colspan="6" class="text-muted text-center">尚無帳號</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  <div
    v-if="showResetPassword"
    class="modal d-block"
    tabindex="-1"
    role="dialog"
    aria-modal="true"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">重設密碼：{{ resetPasswordForm.account }}</h5>
          <button
            type="button"
            class="btn-close"
            aria-label="關閉"
            @click="closeResetPassword"
          ></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label for="resetPassword" class="form-label">新密碼</label>
            <input
              id="resetPassword"
              v-model="resetPasswordForm.password"
              type="password"
              class="form-control"
              autocomplete="new-password"
            />
          </div>
          <div>
            <label for="resetConfirmPassword" class="form-label">確認新密碼</label>
            <input
              id="resetConfirmPassword"
              v-model="resetPasswordForm.confirmPassword"
              type="password"
              class="form-control"
              autocomplete="new-password"
              @keyup.enter="btnResetPassword"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-secondary"
            @click="closeResetPassword"
          >
            取消
          </button>
          <button
            type="button"
            class="btn btn-warning"
            @click="btnResetPassword"
          >
            <i class="bi bi-key"></i> 確認重設
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="showResetPassword" class="modal-backdrop show"></div>
</template>
