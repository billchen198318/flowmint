<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";
import { useFormioDataActionBridge } from "@/composables/useFormioDataActionBridge";
import { useFormCustomJavascript } from "@/composables/useFormCustomJavascript";

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const tenantId = String(route.query.tenant || "");
const detail = ref<any>(null);
const formHost = ref<HTMLElement | null>(null);
const loading = ref(false);
const acting = ref(false);
const actionType = ref<"APPROVE" | "RETURN" | "REJECT" | "RESUBMIT" | "TRANSFER" | "DELEGATE" | "RESOLVE" | "ADD_SIGN" | "ADD_SIGN_COMPLETE">("APPROVE");
const comment = ref("");
const reason = ref("");
const targetTaskDefKey = ref("");
const targetAccount = ref("");
const transferOptions = ref<any[]>([]);
const delegationId = ref("");
const attachments = ref<any[]>([]);
const { attach: attachDataActionBridge } = useFormioDataActionBridge();
const { attach: attachCustomJavascript } = useFormCustomJavascript();
let formInstance: any = null;
let detachDataActionBridge: (() => void) | null = null;
let detachCustomJavascript: (() => Promise<void>) | null = null;

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const post = (path: string, body: any) =>
  useApi(`/fm/requests${path}`, {
    method: "POST",
    body,
    headers: { "X-FlowMint-Tenant": tenantId },
  });
const destroyForm = async () => {
  detachDataActionBridge?.();
  detachDataActionBridge = null;
  await detachCustomJavascript?.();
  detachCustomJavascript = null;
  formInstance?.destroy?.(true);
  formInstance = null;
  if (formHost.value) formHost.value.innerHTML = "";
};
const renderForm = async () => {
  await destroyForm();
  await nextTick();
  if (!formHost.value || !detail.value) return;
  const { Formio } = await import("@formio/js");
  formInstance = await Formio.createForm(
    formHost.value,
    JSON.parse(detail.value.schemaContent || "{}"),
    { readOnly: !detail.value.correctionTask, noAlerts: true, noDefaultSubmitButton: true },
  );
  formInstance.submission = { data: detail.value.formData || {} };
  let uiSchema: any = { engine: "FORMIO", version: 1 };
  try {
    uiSchema = JSON.parse(detail.value.uiSchemaContent || "{}");
  } catch {
    // Published schema validity is enforced by the backend.
  }
  detachDataActionBridge = attachDataActionBridge(formInstance, tenantId, uiSchema);
  const script = await attachCustomJavascript({
    scriptContent: detail.value.customScriptContent || "",
    form: formInstance,
    tenantId,
    formId: detail.value.formId,
    formCode: "",
    versionNo: detail.value.formVersionNo,
    mode: "RUNTIME_TASK",
  });
  detachCustomJavascript = script.detach;
};
const load = async () => {
  if (!tenantId) {
    toast.warning("缺少 Tenant 參數");
    return;
  }
  loading.value = true;
  try {
    const response: any = await post("/tasks/load", { taskId: route.params.id });
    if (!ok(response)) {
      toast.warning(response?.message || "無法載入待辦");
      return;
    }
    detail.value = response.value;
    const attachmentResponse: any = await useApi(
      `/fm/attachments/tasks/${route.params.id}`,
      { headers: { "X-FlowMint-Tenant": tenantId } },
    );
    attachments.value = ok(attachmentResponse) ? attachmentResponse.value || [] : [];
    actionType.value = response.value?.addSignTask
      ? "ADD_SIGN_COMPLETE"
      : response.value?.delegatedTask ? "RESOLVE"
      : response.value?.correctionTask ? "RESUBMIT" : "APPROVE";
    targetTaskDefKey.value = response.value?.returnTargets?.[0]?.taskDefKey || "";
    if (response.value?.allowTransfer) {
      const options: any = await post("/tasks/transfer-options", {
        taskId: route.params.id,
      });
      if (ok(options)) transferOptions.value = options.value || [];
    }
    if (response.value?.allowAddSign && !response.value?.delegatedTask) {
      const options: any = await post("/tasks/add-sign-options", {
        taskId: route.params.id,
      });
      if (ok(options)) transferOptions.value = options.value || [];
    }
    await renderForm();
  } finally {
    loading.value = false;
  }
};
const downloadAttachment = async (attachment: any) => {
  try {
    const content: any = await useApi(
      `/fm/attachments/${attachment.attachmentId}/download`,
      { responseType: "blob", headers: { "X-FlowMint-Tenant": tenantId } },
    );
    const url = URL.createObjectURL(content);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = attachment.fileName;
    anchor.click();
    URL.revokeObjectURL(url);
  } catch {
    toast.warning("附件下載失敗");
  }
};
const submitAction = async () => {
  const commentRequired = detail.value?.commentRequired === "ALWAYS"
    || (detail.value?.commentRequired === "ON_REJECT_RETURN"
      && (actionType.value === "REJECT" || actionType.value === "RETURN"));
  if (commentRequired && !comment.value.trim()) {
    toast.warning("此簽核動作必須填寫意見");
    return;
  }
  if ((actionType.value === "REJECT" || actionType.value === "RETURN") && !reason.value.trim()) {
    toast.warning("請填寫理由");
    return;
  }
  if (actionType.value === "RETURN" && !targetTaskDefKey.value) {
    toast.warning("請選擇退回節點");
    return;
  }
  if ((actionType.value === "TRANSFER" || actionType.value === "ADD_SIGN") && !targetAccount.value) {
    toast.warning("請選擇處理對象");
    return;
  }
  if (actionType.value === "DELEGATE" && !delegationId.value) {
    toast.warning("請選擇代理授權");
    return;
  }
  acting.value = true;
  try {
    const editedFormData = actionType.value === "RESUBMIT"
      ? (await formInstance?.submit?.())?.data
      : null;
    const response: any = actionType.value === "TRANSFER"
      ? await post("/tasks/transfer", {
          taskId: route.params.id,
          targetAccount: targetAccount.value,
          comment: comment.value,
          reason: reason.value,
        })
      : actionType.value === "ADD_SIGN"
        ? await post("/tasks/add-sign", {
            taskId: route.params.id,
            targetAccount: targetAccount.value,
            comment: comment.value,
            reason: reason.value,
          })
      : actionType.value === "ADD_SIGN_COMPLETE"
        ? await post("/tasks/complete-add-sign", {
            taskId: route.params.id,
            comment: comment.value,
          })
      : actionType.value === "DELEGATE"
        ? await post("/tasks/delegate", {
            taskId: route.params.id,
            delegationId: delegationId.value,
            comment: comment.value,
            reason: reason.value,
          })
        : actionType.value === "RESOLVE"
          ? await post("/tasks/resolve", {
              taskId: route.params.id,
              comment: comment.value,
            })
          : await post("/tasks/action", {
          taskId: route.params.id,
          actionType: actionType.value,
          comment: comment.value,
          reason: reason.value,
          targetTaskDefKey: actionType.value === "RETURN" ? targetTaskDefKey.value : null,
          formData: editedFormData,
            });
    if (!ok(response)) {
      toast.warning(response?.message || "簽核處理失敗");
      return;
    }
    toast.success("簽核處理完成");
    await router.push("/workspace");
  } finally {
    acting.value = false;
  }
};

onMounted(load);
onBeforeUnmount(() => void destroyForm());
</script>

<template>
  <div class="container-fluid task-page">
    <button type="button" class="btn btn-link px-0 mb-3 text-decoration-none" @click="router.back()">
      <i class="bi bi-arrow-left"></i> 返回工作台
    </button>
    <div v-if="loading" class="text-center py-5 text-muted">
      <span class="spinner-border spinner-border-sm me-2"></span>載入中…
    </div>
    <template v-if="detail">
      <div class="card mb-4 border-0 shadow-sm">
        <div class="card-body p-4">
          <div class="d-flex flex-wrap justify-content-between gap-3">
            <div>
              <div class="text-primary small fw-semibold mb-1">{{ detail.task.processName }}</div>
              <h2 class="h4 mb-2">{{ detail.task.taskName }}</h2>
              <div class="text-muted">申請人：{{ detail.task.applicantAccount }}</div>
            </div>
            <div class="text-end small text-muted">
              <div>流程編號</div>
              <div class="font-monospace text-body">{{ detail.task.businessKey }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-xl-8">
          <div class="card border-0 shadow-sm">
            <div class="card-header bg-white py-3"><strong>{{ detail.formName }}</strong></div>
            <div class="card-body p-4"><div ref="formHost" class="runtime-form"></div></div>
          </div>
          <div class="card border-0 shadow-sm mt-4">
            <div class="card-header bg-white py-3"><strong>簽核紀錄</strong></div>
            <div class="card-body">
              <div v-if="!detail.actions?.length" class="text-muted">尚無簽核紀錄</div>
              <div v-for="(item, index) in detail.actions" :key="index" class="history-item">
                <span class="badge text-bg-light border me-2">{{ item.actionType }}</span>
                <strong>{{ item.actorAccount }}</strong>
                <span class="text-muted ms-2">{{ item.comment || item.reason || item.outcome }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="col-xl-4">
          <div v-if="attachments.length" class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-white py-3"><strong>附件</strong></div>
            <div class="list-group list-group-flush">
              <button v-for="file in attachments" :key="file.attachmentId" type="button"
                class="list-group-item list-group-item-action text-start"
                @click="downloadAttachment(file)">
                <i class="bi bi-paperclip me-2"></i>{{ file.fileName }}
                <span class="d-block small text-muted ms-4">{{ file.fieldKey }} · {{ file.fileSize }} bytes</span>
              </button>
            </div>
          </div>
          <div class="card border-0 shadow-sm action-card">
            <div class="card-header bg-white py-3"><strong>簽核處理</strong></div>
            <div class="card-body">
              <div class="d-grid gap-2 mb-3">
                <button v-if="detail.correctionTask" type="button" class="btn btn-primary" @click="actionType = 'RESUBMIT'">重新送出</button>
                <template v-else>
                <template v-if="!detail.delegatedTask">
                <button type="button" :class="['btn', actionType === 'APPROVE' ? 'btn-success' : 'btn-outline-success']" @click="actionType = 'APPROVE'">核准</button>
                <button v-if="detail.allowReturn" type="button" :class="['btn', actionType === 'RETURN' ? 'btn-warning' : 'btn-outline-warning']" @click="actionType = 'RETURN'">退回</button>
                <button v-if="detail.allowReject" type="button" :class="['btn', actionType === 'REJECT' ? 'btn-danger' : 'btn-outline-danger']" @click="actionType = 'REJECT'">駁回</button>
                <button
                  v-if="detail.allowTransfer"
                  type="button"
                  :class="['btn', actionType === 'TRANSFER' ? 'btn-info' : 'btn-outline-info']"
                  @click="actionType = 'TRANSFER'"
                >
                  轉派
                </button>
                <button
                  v-if="detail.allowAddSign"
                  type="button"
                  :class="['btn', actionType === 'ADD_SIGN' ? 'btn-info' : 'btn-outline-info']"
                  @click="actionType = 'ADD_SIGN'"
                >
                  加簽
                </button>
                </template>
                <button
                  v-if="detail.delegationOptions?.length"
                  type="button"
                  :class="['btn', actionType === 'DELEGATE' ? 'btn-secondary' : 'btn-outline-secondary']"
                  @click="actionType = 'DELEGATE'"
                >
                  委託代理
                </button>
                <button
                  v-if="detail.delegatedTask && !detail.addSignTask"
                  type="button"
                  :class="['btn', actionType === 'RESOLVE' ? 'btn-primary' : 'btn-outline-primary']"
                  @click="actionType = 'RESOLVE'"
                >
                  回覆委託人
                </button>
                <button
                  v-if="detail.addSignTask"
                  type="button"
                  :class="['btn', actionType === 'ADD_SIGN_COMPLETE' ? 'btn-primary' : 'btn-outline-primary']"
                  @click="actionType = 'ADD_SIGN_COMPLETE'"
                >
                  完成加簽
                </button>
                </template>
              </div>
              <div v-if="actionType === 'RETURN'" class="mb-3">
                <label class="form-label">退回節點</label>
                <select v-model="targetTaskDefKey" class="form-select">
                  <option value="">請選擇</option>
                  <option v-for="target in detail.returnTargets" :key="target.taskDefKey" :value="target.taskDefKey">{{ target.taskName }}</option>
                </select>
              </div>
              <div v-if="actionType === 'TRANSFER' || actionType === 'ADD_SIGN'" class="mb-3">
                <label class="form-label">{{ actionType === 'ADD_SIGN' ? '加簽人' : '轉派對象' }}</label>
                <select v-model="targetAccount" class="form-select">
                  <option value="">請選擇</option>
                  <option
                    v-for="option in transferOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div v-if="actionType === 'DELEGATE'" class="mb-3">
                <label class="form-label">代理授權</label>
                <select v-model="delegationId" class="form-select">
                  <option value="">請選擇</option>
                  <option
                    v-for="option in detail.delegationOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">簽核意見</label>
                <textarea v-model="comment" rows="4" class="form-control"></textarea>
              </div>
              <div v-if="!['APPROVE', 'RESUBMIT', 'RESOLVE', 'ADD_SIGN_COMPLETE'].includes(actionType)" class="mb-3">
                <label class="form-label">理由 *</label>
                <textarea v-model="reason" rows="3" class="form-control"></textarea>
              </div>
              <button type="button" class="btn btn-primary w-100" :disabled="acting" @click="submitAction">
                <span v-if="acting" class="spinner-border spinner-border-sm me-2"></span>確認送出
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.task-page { max-width: 1440px; }
.runtime-form { min-height: 220px; }
.action-card { position: sticky; top: 1rem; }
.history-item { padding: 0.75rem 0; border-bottom: 1px solid var(--bs-border-color); }
.history-item:last-child { border-bottom: 0; }
</style>
