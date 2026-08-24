<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import "@formio/js/dist/formio.full.min.css";
import { escapeQifuHtmlMsg } from "@/components/BaseHelper";
import { useFormioDataActionBridge } from "@/composables/useFormioDataActionBridge";
import {
  withFlowmintSystemFields,
  withoutFlowmintDisplayFields,
} from "@/composables/useFlowmintSystemFields";
import { useFormCustomJavascript } from "@/composables/useFormCustomJavascript";
import { applyTaskFieldPolicy } from "@/composables/useTaskFieldPolicy";
import type { FormScriptRunner } from "@/types/formCustomJavascript";

const ProcessProgressModal = defineAsyncComponent(() =>
  import("@/components/flowmint/request/ProcessProgressModal.vue"),
);

definePageMeta({ layout: "default", middleware: ["auth"] });

const route = useRoute();
const router = useRouter();
const tenantId = String(route.query.tenant || "");
const detail = ref<any>(null);
const formHost = ref<HTMLElement | null>(null);
const loading = ref(false);
const acting = ref(false);
let taskActionInFlight = false;
const actionType = ref<"APPROVE" | "RETURN" | "REJECT" | "RESUBMIT" | "TRANSFER" | "DELEGATE" | "RESOLVE" | "ADD_SIGN" | "ADD_SIGN_COMPLETE" | "PARALLEL_ADD_SIGN" | "PARALLEL_AGREE" | "PARALLEL_DISAGREE">("APPROVE");
const comment = ref("");
const reason = ref("");
const targetTaskDefKey = ref("");
const targetAccount = ref("");
const transferOptions = ref<any[]>([]);
const parallelOptions = ref<any[]>([]);
const parallelMembers = ref<string[]>([]);
const parallelSearch = ref("");
const parallelRequestKey = ref("");
const filteredParallelOptions = computed(() => {
  const keyword = parallelSearch.value.trim().toLocaleLowerCase();
  if (!keyword) return parallelOptions.value;
  return parallelOptions.value.filter((option: any) =>
    `${option.label || ""} ${option.value || ""}`.toLocaleLowerCase().includes(keyword));
});
const delegationId = ref("");
const attachments = ref<any[]>([]);
const showProcessProgress = ref(false);
const processProgressMounted = ref(false);
const { attach: attachDataActionBridge } = useFormioDataActionBridge();
const { attach: attachCustomJavascript } = useFormCustomJavascript();
let formInstance: any = null;
let detachDataActionBridge: (() => void) | null = null;
let detachCustomJavascript: (() => Promise<void>) | null = null;
let runCustomJavascript: FormScriptRunner | null = null;

const ok = (response: any) =>
  response?.success === import.meta.env.VITE_SUCCESS_FLAG;
const openProcessProgress = () => {
  processProgressMounted.value = true;
  showProcessProgress.value = true;
};
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
  runCustomJavascript = null;
  formInstance?.destroy?.(true);
  formInstance = null;
  if (formHost.value) formHost.value.innerHTML = "";
};
const hydrateAttachmentFields = (data: any) => {
  const hydrated = { ...(data || {}) };
  const attachmentsByField = attachments.value.reduce(
    (groups: Record<string, any[]>, attachment: any) => {
      (groups[attachment.fieldKey] ||= []).push(attachment);
      return groups;
    },
    {},
  );
  for (const [fieldKey, fieldAttachments] of Object.entries(attachmentsByField)) {
    const storedIds = Array.isArray(hydrated[fieldKey])
      ? hydrated[fieldKey].map((item: any) =>
        typeof item === "string" ? item : item?.attachmentId,
      )
      : [];
    hydrated[fieldKey] = (fieldAttachments || [])
      .filter((attachment: any) => storedIds.includes(attachment.attachmentId))
      .map((attachment: any) => ({
        attachmentId: attachment.attachmentId,
        storage: "url",
        name: attachment.fileName,
        originalName: attachment.fileName,
        size: Number(attachment.fileSize || 0),
        type: attachment.contentType || "application/octet-stream",
        url: `#flowmint-attachment-${attachment.attachmentId}`,
      }));
  }
  return hydrated;
};
const serializeAttachmentFields = (data: Record<string, unknown>) => {
  const serialized = { ...data };
  for (const fieldKey of new Set(
    attachments.value.map((attachment: any) => attachment.fieldKey),
  )) {
    serialized[fieldKey] = attachments.value
      .filter((attachment: any) => attachment.fieldKey === fieldKey)
      .map((attachment: any) => attachment.attachmentId);
  }
  return serialized;
};
const handleFormAttachmentClick = (event: MouseEvent) => {
  const anchor = (event.target as HTMLElement | null)?.closest<HTMLAnchorElement>(
    'a[href^="#flowmint-attachment-"]',
  );
  if (!anchor) return;
  event.preventDefault();
  const attachmentId = anchor.getAttribute("href")
    ?.replace("#flowmint-attachment-", "");
  const attachment = attachments.value.find(
    (item: any) => item.attachmentId === attachmentId,
  );
  if (attachment) void downloadAttachment(attachment);
};
const renderForm = async () => {
  await destroyForm();
  await nextTick();
  if (!formHost.value || !detail.value) return;
  const { Formio } = await import("@formio/js");
  const schema = applyTaskFieldPolicy(
    JSON.parse(detail.value.schemaContent || "{}"),
    detail.value.fieldPolicy,
  );
  formInstance = await Formio.createForm(
    formHost.value,
    schema,
    {
      readOnly: Boolean(detail.value.parallelAddSignTask),
      noAlerts: true,
      noDefaultSubmitButton: true,
    },
  );
  const submission = {
    data: withFlowmintSystemFields(
      hydrateAttachmentFields(detail.value.formData),
      detail.value.task?.documentNumber,
    ),
  };
  formInstance.submission = submission;
  await formInstance.setSubmission?.(submission);
  let uiSchema: any = { engine: "FORMIO", version: 1 };
  try {
    uiSchema = JSON.parse(detail.value.uiSchemaContent || "{}");
  } catch {
    // Published schema validity is enforced by the backend.
  }
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
  runCustomJavascript = script.run;
  detachDataActionBridge = attachDataActionBridge(
    formInstance,
    tenantId,
    uiSchema,
    script.run,
  );
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
    actionType.value = response.value?.parallelAddSignTask
      ? "PARALLEL_AGREE"
      : response.value?.addSignTask
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
    if (response.value?.allowParallelAddSign
        && response.value?.parallelAddSignDetail?.status !== "WAITING") {
      const options: any = await post("/tasks/parallel-add-sign-options", {
        taskId: route.params.id,
      });
      if (ok(options)) parallelOptions.value = options.value || [];
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
const submitActionOnce = async () => {
  if (detail.value?.parallelAddSignTask && !comment.value.trim()) {
    toast.warning("平行加簽意見必填");
    return;
  }
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
  if (actionType.value === "PARALLEL_ADD_SIGN" && !parallelMembers.value.length) {
    toast.warning("請至少選擇一位平行加簽人員");
    return;
  }
  if (actionType.value === "PARALLEL_ADD_SIGN"
      && parallelMembers.value.length > Number(detail.value?.parallelAddSignMaxMembers || 10)) {
    toast.warning(`平行加簽人數不可超過 ${detail.value?.parallelAddSignMaxMembers || 10} 人`);
    return;
  }
  if (actionType.value === "PARALLEL_ADD_SIGN" && !reason.value.trim()) {
    toast.warning("請填寫平行加簽原因");
    return;
  }
  if (actionType.value === "PARALLEL_ADD_SIGN"
      && !window.confirm("所有平行加簽人回覆前，本關卡將暫停核決。確定發起嗎？")) {
    return;
  }
  acting.value = true;
  try {
    const submitsForm = ["APPROVE", "REJECT", "RETURN", "RESUBMIT"].includes(
      actionType.value,
    );
    let editedFormData: Record<string, unknown> | null = null;
    if (submitsForm) {
      try {
        editedFormData = serializeAttachmentFields(withoutFlowmintDisplayFields(
          (await formInstance?.submit?.())?.data,
        ));
      } catch {
        toast.warning("請完成表單必填欄位並確認格式");
        return;
      }
      try {
        const validation = await runCustomJavascript?.("beforeSubmit", {
          actionType: actionType.value,
          taskId: String(route.params.id),
          formData: editedFormData,
        });
        if (validation === false || (validation && validation.valid === false)) {
          toast.warning(validation?.message || "表單送出前檢核未通過");
          return;
        }
      } catch (error) {
        toast.error(
          error instanceof Error ? error.message : "表單送出前處理失敗",
        );
        return;
      }
      try {
        editedFormData = withoutFlowmintDisplayFields(
          (await formInstance?.submit?.())?.data,
        );
      } catch {
        toast.warning("送出前處理後表單檢核未通過，請確認欄位內容");
        return;
      }
    }
    const response: any = actionType.value === "PARALLEL_ADD_SIGN"
      ? await post("/tasks/start-parallel-add-sign", {
          taskId: route.params.id,
          memberAccounts: parallelMembers.value,
          reason: reason.value,
          requestKey: parallelRequestKey.value ||= crypto.randomUUID(),
        })
      : ["PARALLEL_AGREE", "PARALLEL_DISAGREE"].includes(actionType.value)
      ? await post("/tasks/complete-parallel-add-sign", {
          taskId: route.params.id,
          result: actionType.value === "PARALLEL_AGREE" ? "AGREE" : "DISAGREE",
          comment: comment.value,
        })
      : actionType.value === "TRANSFER"
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
    if (submitsForm) {
      try {
        await runCustomJavascript?.("afterSubmit", {
          actionType: actionType.value,
          taskId: String(route.params.id),
          formData: editedFormData,
          response: response.value,
        });
      } catch (error) {
        toast.warning(
          `簽核已完成，但完成後處理失敗：${
            error instanceof Error ? error.message : "未知錯誤"
          }`,
        );
      }
    }
    try {
      await router.push("/workspace");
    } catch (error) {
      toast.warning(
        `簽核已完成，但無法返回工作區：${
          error instanceof Error ? error.message : "未知錯誤"
        }`,
      );
    }
  } catch (error) {
    toast.error(
      escapeQifuHtmlMsg(
        error instanceof Error ? error.message : "簽核處理時發生未預期錯誤",
      ),
    );
  } finally {
    acting.value = false;
  }
};

const cancelParallelAddSign = async () => {
  if (taskActionInFlight) return;
  if (!reason.value.trim()) {
    toast.warning("請填寫取消平行加簽的原因");
    return;
  }
  taskActionInFlight = true;
  acting.value = true;
  try {
    const response: any = await post("/tasks/cancel-parallel-add-sign", {
      taskId: route.params.id,
      reason: reason.value,
    });
    if (!ok(response)) {
      toast.warning(response?.message || "取消平行加簽失敗");
      return;
    }
    toast.success("已取消平行加簽");
    parallelRequestKey.value = "";
    parallelMembers.value = [];
    parallelSearch.value = "";
    await load();
  } catch (error) {
    toast.error(escapeQifuHtmlMsg(
      error instanceof Error ? error.message : "取消平行加簽失敗",
    ));
  } finally {
    acting.value = false;
    taskActionInFlight = false;
  }
};

const submitAction = async () => {
  if (taskActionInFlight) return;
  taskActionInFlight = true;
  acting.value = true;
  try {
    await submitActionOnce();
  } finally {
    taskActionInFlight = false;
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
              <div>{{ detail.task.documentNumber ? '單據編號' : '流程識別碼' }}</div>
              <div class="font-monospace text-body">{{ detail.task.documentNumber || detail.task.businessKey }}</div>
              <button type="button" class="btn btn-sm btn-outline-primary mt-3" @click="openProcessProgress">
                <i class="bi bi-diagram-3 me-1"></i>查看流程進度
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-xl-8">
          <div class="card border-0 shadow-sm">
            <div class="card-header bg-white py-3"><strong>{{ detail.formName }}</strong></div>
            <div class="card-body p-4"><div ref="formHost" class="runtime-form" @click.capture="handleFormAttachmentClick"></div></div>
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
              <div
                v-if="detail.parallelAddSignDetail && detail.parallelAddSignDetail.status !== 'WAITING'"
                class="alert alert-light border small"
              >
                <div class="fw-semibold mb-1">
                  最近一批平行加簽：{{ detail.parallelAddSignDetail.status }}
                </div>
                <div>
                  完成 {{ detail.parallelAddSignDetail.completedCount }} / {{ detail.parallelAddSignDetail.totalCount }}，
                  同意 {{ detail.parallelAddSignDetail.agreeCount }}，
                  不同意 {{ detail.parallelAddSignDetail.disagreeCount }}
                </div>
                <div
                  v-for="member in detail.parallelAddSignDetail.members"
                  :key="member.account"
                  class="mt-2"
                >
                  <strong>{{ member.displayName || member.account }}</strong>
                  <span class="badge text-bg-light border ms-2">{{ member.status }}</span>
                  <span v-if="member.comment" class="text-muted ms-2">{{ member.comment }}</span>
                </div>
              </div>
              <div class="d-grid gap-2 mb-3">
                <button v-if="detail.correctionTask" type="button" class="btn btn-primary" @click="actionType = 'RESUBMIT'">重新送出</button>
                <template v-else-if="detail.parallelAddSignTask">
                  <div class="alert alert-info small">
                    此回覆不會直接核准或駁回整張申請，最終決策仍由原簽核人負責。
                  </div>
                  <button type="button" :class="['btn', actionType === 'PARALLEL_AGREE' ? 'btn-success' : 'btn-outline-success']" @click="actionType = 'PARALLEL_AGREE'">同意</button>
                  <button type="button" :class="['btn', actionType === 'PARALLEL_DISAGREE' ? 'btn-danger' : 'btn-outline-danger']" @click="actionType = 'PARALLEL_DISAGREE'">不同意</button>
                </template>
                <template v-else-if="detail.parallelAddSignDetail?.status === 'WAITING'">
                  <div class="alert alert-info small mb-2">
                    平行加簽進行中：完成
                    {{ detail.parallelAddSignDetail.completedCount }} / {{ detail.parallelAddSignDetail.totalCount }}，
                    同意 {{ detail.parallelAddSignDetail.agreeCount }}，
                    不同意 {{ detail.parallelAddSignDetail.disagreeCount }}，
                    未回覆 {{ detail.parallelAddSignDetail.totalCount - detail.parallelAddSignDetail.completedCount }}。
                    <span v-if="detail.task.dueDate">期限：{{ detail.task.dueDate }}</span>
                  </div>
                  <div
                    v-for="member in detail.parallelAddSignDetail.members"
                    :key="member.account"
                    class="border rounded p-2 text-start small"
                  >
                    <div class="d-flex justify-content-between gap-2">
                      <strong>{{ member.displayName || member.account }}</strong>
                      <span class="badge text-bg-light border">{{ member.status }}</span>
                    </div>
                    <div v-if="member.comment" class="text-muted mt-1">
                      {{ member.comment }}
                    </div>
                  </div>
                </template>
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
                <button
                  v-if="detail.allowParallelAddSign"
                  type="button"
                  :class="['btn', actionType === 'PARALLEL_ADD_SIGN' ? 'btn-info' : 'btn-outline-info']"
                  @click="actionType = 'PARALLEL_ADD_SIGN'"
                >
                  平行加簽
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
              <div v-if="actionType === 'PARALLEL_ADD_SIGN' && detail.parallelAddSignDetail?.status !== 'WAITING'" class="mb-3">
                <label class="form-label">平行加簽人員</label>
                <input v-model="parallelSearch" type="search" class="form-control mb-2" placeholder="搜尋姓名或帳號" />
                <select v-model="parallelMembers" class="form-select" multiple size="6">
                  <option
                    v-for="option in filteredParallelOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
                <div v-if="parallelMembers.length" class="d-flex flex-wrap gap-1 mt-2">
                  <button
                    v-for="account in parallelMembers"
                    :key="account"
                    type="button"
                    class="badge rounded-pill text-bg-primary border-0"
                    @click="parallelMembers = parallelMembers.filter(item => item !== account)"
                  >
                    {{ parallelOptions.find(option => option.value === account)?.label || account }} ×
                  </button>
                </div>
                <div class="form-text">
                  已選 {{ parallelMembers.length }} / {{ detail.parallelAddSignMaxMembers || 10 }} 人；
                  可按 Ctrl（macOS：Command）選取多人。
                </div>
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
              <div v-if="!detail.parallelAddSignTask && !['APPROVE', 'RESUBMIT', 'RESOLVE', 'ADD_SIGN_COMPLETE'].includes(actionType)" class="mb-3">
                <label class="form-label">理由 *</label>
                <textarea v-model="reason" rows="3" class="form-control"></textarea>
              </div>
              <div v-if="detail.parallelAddSignDetail?.cancellable" class="mb-3">
                <label class="form-label">取消原因 *</label>
                <textarea v-model="reason" rows="3" class="form-control"></textarea>
              </div>
              <button
                v-if="detail.parallelAddSignDetail?.cancellable"
                type="button"
                class="btn btn-outline-danger w-100"
                :disabled="acting"
                @click="cancelParallelAddSign"
              >
                取消平行加簽
              </button>
              <button v-else-if="detail.parallelAddSignDetail?.status !== 'WAITING'" type="button" class="btn btn-primary w-100" :disabled="acting" @click="submitAction">
                <span v-if="acting" class="spinner-border spinner-border-sm me-2"></span>確認送出
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
  <ProcessProgressModal
    v-if="detail && processProgressMounted"
    :show="showProcessProgress"
    :tenant-id="tenantId"
    :process-instance-id="detail.task.processInstanceId"
    @close="showProcessProgress = false"
  />
</template>

<style scoped>
.task-page { max-width: 1440px; }
.runtime-form { min-height: 220px; }
.action-card { position: sticky; top: 1rem; }
.history-item { padding: 0.75rem 0; border-bottom: 1px solid var(--bs-border-color); }
.history-item:last-child { border-bottom: 0; }
</style>
