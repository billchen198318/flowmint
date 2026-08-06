import { toast } from "vue3-toastify";
import { escapeQifuHtmlMsg } from "@/components/BaseHelper";
import { setFormDataPath, useFormDataAction } from "@/composables/useFormDataAction";
import type { FormDataActionUiSchema } from "@/types/formDataAction";

interface FormioRuntime {
  on: (event: string, handler: (...args: unknown[]) => void) => void;
  off?: (event: string, handler: (...args: unknown[]) => void) => void;
  submission?: { data?: Record<string, unknown>; [key: string]: unknown };
  setSubmission?: (submission: Record<string, unknown>) => Promise<unknown>;
  redraw?: () => Promise<unknown> | unknown;
}

export const useFormioDataActionBridge = () => {
  const { execute, applyResponse } = useFormDataAction();

  const attach = (
    formio: FormioRuntime,
    tenantId: string,
    uiSchema: FormDataActionUiSchema,
  ) => {
    const detachCallbacks: Array<() => void> = [];
    const inFlight = new Set<string>();

    for (const binding of uiSchema.dataActions || []) {
      if (!binding.event || !binding.actionCode) continue;
      const handler = async () => {
        if (inFlight.has(binding.bindingId)) return;
        inFlight.add(binding.bindingId);
        const submissionData = formio.submission?.data || {};
        try {
          if (binding.statusTarget) {
            setFormDataPath(submissionData, binding.statusTarget, "RUNNING");
          }
          if (binding.errorTarget) {
            setFormDataPath(submissionData, binding.errorTarget, "");
          }
          await refreshSubmission(formio, submissionData);

          const execution = await execute(binding, { tenantId, submissionData });
          applyResponse(binding, execution, submissionData);
          if (binding.statusTarget) {
            setFormDataPath(submissionData, binding.statusTarget, "SUCCESS");
          }
          await refreshSubmission(formio, submissionData);
          toast.success(`${binding.actionCode} 執行成功`);
        } catch (error: unknown) {
          const message = error instanceof Error ? error.message : "Data Action 執行失敗";
          if (binding.statusTarget) {
            setFormDataPath(submissionData, binding.statusTarget, "ERROR");
          }
          if (binding.errorTarget) {
            setFormDataPath(submissionData, binding.errorTarget, message);
          }
          await refreshSubmission(formio, submissionData);
          toast.error(escapeQifuHtmlMsg(message));
        } finally {
          inFlight.delete(binding.bindingId);
        }
      };
      formio.on(binding.event, handler);
      detachCallbacks.push(() => formio.off?.(binding.event, handler));
    }

    return () => detachCallbacks.forEach((detach) => detach());
  };

  return { attach };
};

const refreshSubmission = async (
  formio: FormioRuntime,
  data: Record<string, unknown>,
) => {
  const submission = { ...(formio.submission || {}), data };
  formio.submission = submission;
  if (formio.setSubmission) await formio.setSubmission(submission);
  else await formio.redraw?.();
};
