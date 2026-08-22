import { ref, type Ref } from "vue";
import { toast } from "vue3-toastify";
import { escapeQifuHtmlMsg } from "@/components/BaseHelper";
import {
  FLOWMINT_SYSTEM_FIELDS,
  withoutFlowmintDisplayFields,
} from "@/composables/useFlowmintSystemFields";
import type { FormScriptRunner } from "@/types/formCustomJavascript";

interface RuntimeSubmitOptions {
  tenantId: Ref<string>;
  processDefId: Ref<string>;
  applicantAccount: Ref<string>;
  selectedForm: Ref<any>;
  idempotencyKey: Ref<string>;
  uploadSessionId: Ref<string>;
  attachmentFields: Ref<any[]>;
  attachmentFiles: Ref<Record<string, any[]>>;
  result: Ref<any>;
  formDirty: Ref<boolean>;
  getFormInstance: () => any;
  getScriptRunner: () => FormScriptRunner | null;
}

export const useRuntimeSubmit = (options: RuntimeSubmitOptions) => {
  const submitting = ref(false);
  let submitInFlight = false;

  const ok = (response: any) =>
    response?.success === import.meta.env.VITE_SUCCESS_FLAG;
  const tenantHeaders = () => ({
    "X-FlowMint-Tenant": options.tenantId.value,
    "Idempotency-Key": options.idempotencyKey.value,
  });

  const submitOnce = async () => {
    const formInstance = options.getFormInstance();
    const selectedForm = options.selectedForm.value;
    if (!formInstance || !selectedForm) return;
    if (!formInstance.checkValidity(null, true)) {
      toast.warning("請完成表單必填欄位");
      return;
    }
    const missingAttachment = options.attachmentFields.value.find((field: any) =>
      field.validate?.required && !options.attachmentFiles.value[field.key]?.length);
    if (missingAttachment) {
      toast.warning(`${missingAttachment.label || missingAttachment.key} 為必填附件`);
      return;
    }
    const scriptRunner = options.getScriptRunner();
    try {
      const validation = await scriptRunner?.("beforeSubmit");
      if (validation === false || (validation && validation.valid === false)) {
        toast.warning(validation?.message || "表單送出前檢核未通過");
        return;
      }
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "表單送出前檢核失敗");
      return;
    }
    if (!formInstance.checkValidity(null, true)) {
      toast.warning("送出前處理後表單檢核未通過，請確認欄位內容");
      return;
    }
    const renderedFormData = formInstance.submission?.data || {};
    const applicantAccount = String(
      renderedFormData[FLOWMINT_SYSTEM_FIELDS.applicantAccount]
        || options.applicantAccount.value,
    ).trim();
    if (!applicantAccount) {
      toast.warning("請選擇申請人");
      return;
    }
    const response: any = await useApi("/fm/requests/submit", {
      method: "POST",
      headers: tenantHeaders(),
      body: {
        processDefId: options.processDefId.value,
        formId: selectedForm.formId,
        formVersionNo: selectedForm.formVersionNo,
        applicantAccount,
        formData: withoutFlowmintDisplayFields(renderedFormData),
        uploadSessionId: options.uploadSessionId.value || null,
      },
    });
    if (!ok(response)) {
      toast.warning(response?.message || "送出失敗");
      return;
    }
    options.result.value = response.value;
    options.formDirty.value = false;
    toast.success("表單已送出");
    try {
      await scriptRunner?.("afterSubmit", { response: response.value });
    } catch (error) {
      toast.warning(`表單已送出，但送出後處理失敗：${
        error instanceof Error ? error.message : "未知錯誤"
      }`);
    }
  };

  const submit = async () => {
    if (submitInFlight || options.result.value) return;
    submitInFlight = true;
    submitting.value = true;
    try {
      await submitOnce();
    } catch (error) {
      toast.error(escapeQifuHtmlMsg(
        error instanceof Error ? error.message : "表單送出時發生未預期錯誤",
      ));
    } finally {
      submitInFlight = false;
      submitting.value = false;
    }
  };

  return { submitting, submit };
};
