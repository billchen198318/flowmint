<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
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
import { apiScopes, PageConstants } from "../config";

interface ApiClientForm {
  oid: string;
  tenantId: string;
  clientId: string;
  clientCode: string;
  clientName: string;
  systemType: string;
  description: string;
  allowedScopes: string[];
  allowedProcessIds: string[];
  allowedInitiatorAccounts: string[];
  ipAllowlist: string[];
  rateLimitPerMinute: number;
  dailyQuota: number;
  status: string;
  lockVersion: number;
}
interface ApiKeyView {
  oid: string;
  keyId: string;
  maskedKey: string;
  effectiveFrom: string;
  expiresAt: string | null;
  revokedAt: string | null;
  lastUsedAt: string | null;
  lastSourceIp: string | null;
  status: string;
}
const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const tenants = ref<any[]>([]);
const keys = ref<ApiKeyView[]>([]);
const checkFields = ref<Record<string, string>>({});
const processIdsText = ref("");
const initiatorsText = ref("");
const ipAllowlistText = ref("");
const expiresAt = ref("");
const issuedApiKey = ref("");
const issuedNotice = ref("");
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const newForm = (): ApiClientForm => ({
  oid: "",
  tenantId: "",
  clientId: "",
  clientCode: "",
  clientName: "",
  systemType: "ERP",
  description: "",
  allowedScopes: [],
  allowedProcessIds: [],
  allowedInitiatorAccounts: [],
  ipAllowlist: [],
  rateLimitPerMinute: 60,
  dailyQuota: 10000,
  status: "ACTIVE",
  lockVersion: 0,
});
const form = ref<ApiClientForm>(newForm());
const post = (path: string, body: unknown = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const lines = (value: string) =>
  value
    .split(/[\n,]/)
    .map((item) => item.trim())
    .filter(Boolean);
const keyCount = computed(
  () => keys.value.filter((item) => item.status === "ACTIVE").length,
);
const apply = (value: any) => {
  form.value = { ...newForm(), ...value };
  processIdsText.value = (form.value.allowedProcessIds || []).join("\n");
  initiatorsText.value = (form.value.allowedInitiatorAccounts || []).join("\n");
  ipAllowlistText.value = (form.value.ipAllowlist || []).join("\n");
  checkFields.value = {};
};
const payload = () => ({
  ...form.value,
  allowedProcessIds: lines(processIdsText.value),
  allowedInitiatorAccounts: lines(initiatorsText.value),
  ipAllowlist: lines(ipAllowlistText.value),
});
const validate = () => {
  const errors: Record<string, string> = {};
  if (!form.value.tenantId.trim()) errors.tenantId = "請選擇 Tenant";
  if (!form.value.clientCode.trim()) errors.clientCode = "請輸入 Client Code";
  if (!form.value.clientName.trim()) errors.clientName = "請輸入 Client 名稱";
  if (!form.value.systemType) errors.systemType = "請選擇系統類型";
  if (
    !Number.isInteger(form.value.rateLimitPerMinute) ||
    form.value.rateLimitPerMinute < 1 ||
    form.value.rateLimitPerMinute > 100000
  ) {
    errors.rateLimitPerMinute = "每分鐘上限必須介於 1 到 100000";
  }
  if (!Number.isInteger(form.value.dailyQuota) || form.value.dailyQuota < 1) {
    errors.dailyQuota = "每日配額必須大於或等於 1";
  }
  if (!form.value.allowedScopes.length)
    errors.allowedScopes = "請至少選擇一個 API Scope";
  checkFields.value = errors;
  if (Object.keys(errors).length) {
    toast.warning(escapeQifuHtmlMsg("請修正欄位錯誤後再儲存。"));
    return false;
  }
  return true;
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "載入 API Client 失敗。"),
      );
      await router.push(PageConstants.frontendNamespace);
      return;
    }
    apply(response.data.value);
    await loadKeys();
  } catch (error: unknown) {
    toast.error(
      escapeQifuHtmlMsg(error instanceof Error ? error.message : "載入失敗。"),
    );
  } finally {
    hideLoading();
  }
};
const save = async () => {
  if (!validate()) return;
  showLoading();
  try {
    const response = await post(props.edit ? "/update" : "/save", payload());
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "儲存 API Client 失敗。"),
      );
      return;
    }
    toast.success(escapeQifuHtmlMsg(response.data?.message || "儲存成功。"));
    if (props.edit) apply(response.data.value);
    else await router.push(PageConstants.frontendNamespace);
  } catch (error: unknown) {
    toast.error(
      escapeQifuHtmlMsg(error instanceof Error ? error.message : "儲存失敗。"),
    );
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "停用失敗。"));
      return;
    }
    apply(response.data.value);
    await loadKeys();
    toast.success("Client 已停用。");
  } finally {
    hideLoading();
  }
};
const loadKeys = async () => {
  if (!form.value.oid) return;
  const response = await post("/keys", { clientOid: form.value.oid });
  keys.value = response.data?.value || [];
};
const issue = async (rotate: boolean) => {
  showLoading();
  try {
    const response = await post(rotate ? "/keys/rotate" : "/keys/issue", {
      clientOid: form.value.oid,
      expiresAt: expiresAt.value
        ? new Date(expiresAt.value).toISOString()
        : null,
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "簽發 Key 失敗。"),
      );
      return;
    }
    issuedApiKey.value = response.data.value?.apiKey || "";
    issuedNotice.value = response.data.value?.notice || "此 Key 只會顯示一次。";
    await loadKeys();
  } finally {
    hideLoading();
  }
};
const confirmDeactivate = () =>
  confirmFire(
    "停用 Client 會同步撤銷所有有效 Key，確定繼續？",
    deactivate,
    form.value.oid,
  );
const confirmRotate = () =>
  confirmFire(
    "輪替會簽發新 Key，舊 Key 仍需另行撤銷。確定繼續？",
    () => issue(true),
    form.value.oid,
  );
const revoke = async (key: ApiKeyView) => {
  const reason = window.prompt("請輸入撤銷原因（必填）：");
  if (!reason?.trim()) return;
  showLoading();
  try {
    const response = await post("/keys/revoke", {
      keyOid: key.oid,
      reason: reason.trim(),
    });
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "撤銷 Key 失敗。"),
      );
      return;
    }
    await loadKeys();
    toast.success("Key 已撤銷。");
  } finally {
    hideLoading();
  }
};
const copyKey = async () => {
  await navigator.clipboard.writeText(issuedApiKey.value);
  toast.success("已複製 Key。");
};
const closeKey = () => {
  issuedApiKey.value = "";
  issuedNotice.value = "";
};
onBeforeUnmount(closeKey);
onMounted(async () => {
  try {
    tenants.value = (await post("/tenant-options")).data?.value || [];
    await load();
  } catch (error: unknown) {
    toast.error(
      escapeQifuHtmlMsg(error instanceof Error ? error.message : "載入失敗。"),
    );
  }
});
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    description="API Key 明文只顯示一次；Scope、流程、Initiator 與 IP 採最小權限配置。"
    backFlag="Y"
    refreshFlag="Y"
    saveFlag="Y"
    @backMethod="router.back()"
    @refreshMethod="props.edit ? load() : apply(newForm())"
    @saveMethod="save"
  />
  <div v-if="issuedApiKey" class="alert alert-warning" role="alert">
    <h5>請立即保存 API Key</h5>
    <p>{{ issuedNotice }}</p>
    <div class="input-group">
      <input
        :value="issuedApiKey"
        class="form-control font-monospace"
        readonly
      />
      <button class="btn btn-outline-dark" @click="copyKey">複製</button>
      <button class="btn btn-dark" @click="closeKey">我已保存並關閉</button>
    </div>
  </div>
  <div class="card mb-3">
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label">Tenant *</label
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
          <label class="form-label">Client Code *</label
          ><input
            v-model="form.clientCode"
            :readonly="props.edit"
            maxlength="50"
            :class="[
              'form-control',
              checkInvalid('clientCode', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("clientCode", checkFields) }}
          </div>
        </div>
        <div class="col-md-4">
          <label class="form-label">Client 名稱 *</label
          ><input
            v-model="form.clientName"
            maxlength="100"
            :class="[
              'form-control',
              checkInvalid('clientName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("clientName", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">系統類型 *</label
          ><select
            v-model="form.systemType"
            :class="[
              'form-select',
              checkInvalid('systemType', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="ERP">ERP</option>
            <option value="MES">MES</option>
            <option value="HR">HR</option>
            <option value="OTHER">OTHER</option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("systemType", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">每分鐘上限 *</label
          ><input
            v-model.number="form.rateLimitPerMinute"
            type="number"
            min="1"
            max="100000"
            :class="[
              'form-control',
              checkInvalid('rateLimitPerMinute', checkFields)
                ? 'is-invalid'
                : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("rateLimitPerMinute", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">每日配額 *</label
          ><input
            v-model.number="form.dailyQuota"
            type="number"
            min="1"
            :class="[
              'form-control',
              checkInvalid('dailyQuota', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("dailyQuota", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">狀態</label
          ><select
            v-model="form.status"
            class="form-select"
            :disabled="props.edit && form.status === 'INACTIVE'"
          >
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </div>
        <div class="col-12">
          <label class="form-label">說明</label
          ><textarea
            v-model="form.description"
            rows="2"
            maxlength="1000"
            class="form-control"
          ></textarea>
        </div>
      </div>
    </div>
  </div>
  <div class="card mb-3">
    <div class="card-header">API Scopes *</div>
    <div class="card-body row g-2">
      <div v-for="scope in apiScopes" :key="scope" class="col-md-4 form-check">
        <input
          :id="scope"
          v-model="form.allowedScopes"
          class="form-check-input"
          type="checkbox"
          :value="scope"
        />
        <label :for="scope" class="form-check-label font-monospace">{{
          scope
        }}</label>
      </div>
      <div
        v-if="checkInvalid('allowedScopes', checkFields)"
        class="col-12 text-danger small"
      >
        {{ invalidFeedback("allowedScopes", checkFields) }}
      </div>
    </div>
  </div>
  <div class="card mb-3">
    <div class="card-header">存取限制</div>
    <div class="card-body row g-3">
      <div class="col-md-4">
        <label class="form-label">允許流程 ID</label
        ><textarea
          v-model="processIdsText"
          rows="6"
          class="form-control font-monospace"
        ></textarea>
        <div class="form-text">每行一筆；空白代表不限制。</div>
      </div>
      <div class="col-md-4">
        <label class="form-label">允許 Initiator 帳號</label
        ><textarea
          v-model="initiatorsText"
          rows="6"
          class="form-control font-monospace"
        ></textarea>
        <div class="form-text">每行一筆；空白代表不限制。</div>
      </div>
      <div class="col-md-4">
        <label class="form-label">IP Allowlist</label
        ><textarea
          v-model="ipAllowlistText"
          rows="6"
          class="form-control font-monospace"
        ></textarea>
        <div class="form-text">支援精確 IP 或 CIDR；空白代表不限制。</div>
      </div>
    </div>
  </div>
  <div v-if="props.edit" class="card mb-3">
    <div class="card-header d-flex justify-content-between">
      <span>API Keys</span><span>有效 Key：{{ keyCount }}</span>
    </div>
    <div class="card-body">
      <div class="row g-2 mb-3">
        <div class="col-md-4">
          <input
            v-model="expiresAt"
            type="datetime-local"
            class="form-control"
          />
        </div>
        <div class="col-md-8">
          <button
            class="btn btn-primary me-2"
            :disabled="form.status !== 'ACTIVE'"
            @click="issue(false)"
          >
            簽發 Key
          </button>
          <button
            class="btn btn-outline-primary"
            :disabled="form.status !== 'ACTIVE'"
            @click="confirmRotate"
          >
            輪替 Key
          </button>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table table-sm align-middle">
          <thead>
            <tr>
              <th>Key</th>
              <th>狀態</th>
              <th>有效起日</th>
              <th>到期日</th>
              <th>最後使用</th>
              <th>來源 IP</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="key in keys" :key="key.oid">
              <td class="font-monospace">{{ key.maskedKey }}</td>
              <td>{{ key.status }}</td>
              <td>
                {{
                  key.effectiveFrom
                    ? new Date(key.effectiveFrom).toLocaleString()
                    : "-"
                }}
              </td>
              <td>
                {{
                  key.expiresAt
                    ? new Date(key.expiresAt).toLocaleString()
                    : "永不"
                }}
              </td>
              <td>
                {{
                  key.lastUsedAt
                    ? new Date(key.lastUsedAt).toLocaleString()
                    : "尚未使用"
                }}
              </td>
              <td>{{ key.lastSourceIp || "-" }}</td>
              <td>
                <button
                  v-if="key.status === 'ACTIVE'"
                  class="btn btn-outline-danger btn-sm"
                  @click="revoke(key)"
                >
                  撤銷
                </button>
              </td>
            </tr>
            <tr v-if="!keys.length">
              <td colspan="7" class="text-center text-muted">尚未簽發 Key</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  <div class="d-flex gap-2">
    <button class="btn btn-primary" @click="save">儲存</button>
    <button
      v-if="props.edit && form.status === 'ACTIVE'"
      class="btn btn-outline-danger"
      @click="confirmDeactivate"
    >
      停用 Client
    </button>
  </div>
</template>
