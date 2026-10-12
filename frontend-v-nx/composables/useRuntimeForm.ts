import { nextTick, ref, type Ref } from "vue";
import { FLOWMINT_SYSTEM_FIELDS } from "@/composables/useFlowmintSystemFields";
import { useFormioDataActionBridge } from "@/composables/useFormioDataActionBridge";
import { useFormCustomJavascript } from "@/composables/useFormCustomJavascript";
import type { FormScriptRunner } from "@/types/formCustomJavascript";

interface RuntimeFormOptions {
  tenantId: Ref<string>;
  applicantAccount: Ref<string>;
  selectedForm: Ref<any>;
  prepareAttachments: (schema: any) => void;
  refreshAttachmentFields: () => void;
  createUploadBatch: (expired: boolean) => Promise<boolean>;
  resetAttachments: () => void;
  hasAttachmentFields: () => boolean;
}

export const useRuntimeForm = (options: RuntimeFormOptions) => {
  const formHost = ref<HTMLElement | null>(null);
  const formDirty = ref(false);
  const formReady = ref(false);
  const formError = ref("");
  const { attach: attachDataActionBridge } = useFormioDataActionBridge();
  const { attach: attachCustomJavascript } = useFormCustomJavascript();
  let formInstance: any = null;
  let detachDataActionBridge: (() => void) | null = null;
  let detachCustomJavascript: (() => Promise<void>) | null = null;
  let scriptRunner: FormScriptRunner | null = null;
  let renderGeneration = 0;

  const destroyForm = async () => {
    renderGeneration += 1;
    formReady.value = false;
    formError.value = "";
    detachDataActionBridge?.();
    detachDataActionBridge = null;
    await detachCustomJavascript?.();
    detachCustomJavascript = null;
    scriptRunner = null;
    formInstance?.destroy?.(true);
    formInstance = null;
    formDirty.value = false;
    options.resetAttachments();
    if (formHost.value) formHost.value.innerHTML = "";
  };

  const renderForm = async () => {
    await destroyForm();
    const generation = renderGeneration;
    await nextTick();
    const selectedForm = options.selectedForm.value;
    if (!formHost.value || !selectedForm) return;
    const { Formio } = await import("@formio/js");
    if (generation !== renderGeneration) return;
    const schema = JSON.parse(selectedForm.schemaContent || "{}");
    options.prepareAttachments(schema);
    const createdForm = await Formio.createForm(formHost.value, schema, {
      noAlerts: true,
      noDefaultSubmitButton: true,
    });
    if (generation !== renderGeneration) {
      createdForm?.destroy?.(true);
      return;
    }
    formInstance = createdForm;
    formInstance.submission = {
      data: {
        ...(formInstance.submission?.data || {}),
        [FLOWMINT_SYSTEM_FIELDS.applicantAccount]:
          options.applicantAccount.value.trim(),
        [FLOWMINT_SYSTEM_FIELDS.documentNumber]: "",
      },
    };
    options.refreshAttachmentFields();
    formInstance.on?.("change", () => {
      formDirty.value = true;
      options.refreshAttachmentFields();
    });
    try {
      if (options.hasAttachmentFields()) {
        const created = await options.createUploadBatch(false);
        if (generation !== renderGeneration) return;
        if (!created) throw new Error("無法建立附件上傳 Session，請重試載入表單");
      }
    let uiSchema: any = { engine: "FORMIO", version: 1 };
    try {
      uiSchema = JSON.parse(selectedForm.uiSchemaContent || "{}");
    } catch {
      // Published schema validity is enforced by the backend.
    }
    const script = await attachCustomJavascript({
      scriptContent: selectedForm.customScriptContent || "",
      form: formInstance,
      tenantId: options.tenantId.value,
      formId: selectedForm.formId,
      formCode: selectedForm.formCode,
      versionNo: selectedForm.formVersionNo,
      mode: "RUNTIME_START",
    });
    if (generation !== renderGeneration) {
      await script.detach();
      return;
    }
    detachCustomJavascript = script.detach;
    scriptRunner = script.run;
    detachDataActionBridge = attachDataActionBridge(
      formInstance,
      options.tenantId.value,
      uiSchema,
      script.run,
    );
    formReady.value = true;
    } catch (error) {
      if (generation !== renderGeneration) return;
      formError.value = error instanceof Error ? error.message : "表單初始化失敗，請重試";
    }
  };

  return {
    formHost,
    formDirty,
    formReady,
    formError,
    renderForm,
    destroyForm,
    getFormInstance: () => formInstance,
    getScriptRunner: () => scriptRunner,
  };
};
