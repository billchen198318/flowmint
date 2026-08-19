import { toast } from "vue3-toastify";
import { escapeQifuHtmlMsg } from "@/components/BaseHelper";
import {
  buildDataActionRequest,
  setFormDataPath,
  useFormDataAction,
} from "@/composables/useFormDataAction";
import type { FormDataActionUiSchema } from "@/types/formDataAction";

interface FormioRuntime {
  on: (event: string, handler: (...args: unknown[]) => void) => void;
  off?: (event: string, handler: (...args: unknown[]) => void) => void;
  submission?: { data?: Record<string, unknown>; [key: string]: unknown };
  setSubmission?: (submission: Record<string, unknown>) => Promise<unknown>;
  redraw?: () => Promise<unknown> | unknown;
}

type LifecycleRunner = (
  lifecycle: "onDataActionSuccess" | "onDataActionError",
  additions?: Record<string, unknown>,
) => Promise<unknown>;

export const useFormioDataActionBridge = () => {
  const { execute, applyResponse } = useFormDataAction();

  const attach = (
    formio: FormioRuntime,
    tenantId: string,
    uiSchema: FormDataActionUiSchema,
    runLifecycle?: LifecycleRunner,
  ) => {
    const detachCallbacks: Array<() => void> = [];
    const inFlight = new Set<string>();
    const bindings = uiSchema.dataActions || [];
    const bindingIdCounts = bindings.reduce<Map<string, number>>(
      (counts, binding) => {
        const bindingId = binding.bindingId?.trim();
        if (bindingId) counts.set(bindingId, (counts.get(bindingId) || 0) + 1);
        return counts;
      },
      new Map(),
    );

    for (const [bindingIndex, binding] of bindings.entries()) {
      if (!binding.event || !binding.actionCode) continue;
      const configuredBindingId = binding.bindingId?.trim();
      const runtimeBindingId =
        configuredBindingId && bindingIdCounts.get(configuredBindingId) === 1
          ? configuredBindingId
          : `legacy-${bindingIndex}-${binding.event}-${binding.actionCode}`;
      const handler = async () => {
        if (inFlight.has(runtimeBindingId)) return;
        inFlight.add(runtimeBindingId);
        const submissionData = formio.submission?.data || {};
        try {
          let request: Record<string, unknown>;
          try {
            request = buildDataActionRequest(
              binding.requestMapping,
              submissionData,
            );
          } catch (error) {
            toast.error(
              escapeQifuHtmlMsg(
                error instanceof Error
                  ? error.message
                  : "Data Action request mapping 設定錯誤",
              ),
            );
            return;
          }
          if (binding.statusTarget) {
            setFormDataPath(submissionData, binding.statusTarget, "RUNNING");
          }
          if (binding.errorTarget) {
            setFormDataPath(submissionData, binding.errorTarget, "");
          }
          try {
            await refreshSubmission(formio, submissionData);
          } catch {
            toast.warning("Data Action 執行中，但畫面狀態更新失敗");
          }

          let execution: Awaited<ReturnType<typeof execute>>;
          try {
            execution = await execute(binding, {
              tenantId,
              submissionData,
              requestData: request,
            });
          } catch (error: unknown) {
            const message = error instanceof Error ? error.message : "Data Action 執行失敗";
            if (binding.statusTarget) {
              setFormDataPath(submissionData, binding.statusTarget, "ERROR");
            }
            if (binding.errorTarget) {
              setFormDataPath(submissionData, binding.errorTarget, message);
            }
            try {
              await refreshSubmission(formio, submissionData);
            } catch {
              toast.warning("Data Action 失敗，且畫面狀態更新失敗");
            }
            try {
              await runLifecycle?.("onDataActionError", {
                actionCode: binding.actionCode,
                request,
                actionVersion: binding.actionVersion,
                error,
                bindingId: runtimeBindingId,
              });
            } catch {
              // Preserve the original Data Action error shown below.
            }
            toast.error(escapeQifuHtmlMsg(message));
            return;
          }

          try {
            applyResponse(binding, execution, submissionData);
            if (binding.statusTarget) {
              setFormDataPath(submissionData, binding.statusTarget, "SUCCESS");
            }
            await refreshSubmission(formio, submissionData);
          } catch (error) {
            toast.warning(
              `Data Action 已執行成功，但結果套用失敗：${
                error instanceof Error ? error.message : "未知錯誤"
              }`,
            );
          }
          try {
            await runLifecycle?.("onDataActionSuccess", {
              actionCode: binding.actionCode,
              request,
              actionVersion: binding.actionVersion,
              response: execution.data,
              bindingId: runtimeBindingId,
            });
          } catch (hookError) {
            toast.warning(
              `Data Action ${binding.actionCode} 已執行成功，但成功後處理失敗：${
                hookError instanceof Error ? hookError.message : "未知錯誤"
              }`,
            );
          }
          toast.success(`${binding.actionCode} 執行成功`);
        } finally {
          inFlight.delete(runtimeBindingId);
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
