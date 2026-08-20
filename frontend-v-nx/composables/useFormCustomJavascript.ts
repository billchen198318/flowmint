import { ref } from "vue";
import { toast } from "vue3-toastify";
import { getAxiosInstance } from "@/components/BaseHelper";
import { setFormDataPath } from "@/composables/useFormDataAction";
import type {
  FormCustomScriptContext,
  FormCustomScriptModule,
  FormScriptConsoleEntry,
  FormScriptLifecycle,
  FormScriptMode,
} from "@/types/formCustomJavascript";

interface FormioRuntime {
  submission?: { data?: Record<string, unknown>; [key: string]: unknown };
  on: (event: string, handler: (...args: any[]) => void) => void;
  off?: (event: string, handler: (...args: any[]) => void) => void;
  getComponent?: (key: string) => any;
  setSubmission?: (submission: Record<string, unknown>) => Promise<unknown>;
  redraw?: () => Promise<unknown> | unknown;
}

interface AttachOptions {
  scriptContent?: string;
  form: FormioRuntime;
  tenantId: string;
  formId: string;
  formCode: string;
  versionNo: number;
  mode: FormScriptMode;
  hookTimeoutMs?: number;
}

const lifecycles: FormScriptLifecycle[] = [
  "onFormLoad",
  "onFieldChange",
  "beforeSubmit",
  "afterSubmit",
  "onDataActionSuccess",
  "onDataActionError",
  "onDestroy",
];

const getPath = (source: unknown, path: string): unknown =>
  path
    .split(".")
    .filter(Boolean)
    .reduce<unknown>((value, segment) => {
      if (value === null || typeof value !== "object") return undefined;
      return (value as Record<string, unknown>)[segment];
    }, source);

const refreshSubmission = async (form: FormioRuntime) => {
  const submission = form.submission || { data: {} };
  form.submission = submission;
  if (form.setSubmission) await form.setSubmission(submission);
  else await form.redraw?.();
};

const refreshComponent = async (form: FormioRuntime, key: string) => {
  const component = form.getComponent?.(key);
  if (component?.redraw) await component.redraw();
  else await form.redraw?.();
};

export const compileFormCustomJavascript = async (
  scriptContent?: string,
): Promise<FormCustomScriptModule> => {
  if (!scriptContent?.trim()) return {};
  const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
  const factory = new AsyncFunction(`"use strict";\n${scriptContent}\n`);
  const module = await factory();
  if (module === null || typeof module !== "object" || Array.isArray(module)) {
    throw new Error("客製 JavaScript 必須 return 一個生命週期物件");
  }
  for (const lifecycle of lifecycles) {
    const handler = module[lifecycle];
    if (handler !== undefined && typeof handler !== "function") {
      throw new Error(`${lifecycle} 必須是 function`);
    }
  }
  return module as FormCustomScriptModule;
};

export const useFormCustomJavascript = () => {
  const consoleEntries = ref<FormScriptConsoleEntry[]>([]);

  const appendConsole = (
    level: FormScriptConsoleEntry["level"],
    values: unknown[],
    lifecycle?: FormScriptLifecycle,
  ) => {
    consoleEntries.value.push({
      occurredAt: new Date().toISOString(),
      level,
      lifecycle,
      values,
    });
    if (consoleEntries.value.length > 200) consoleEntries.value.shift();
  };

  const clearConsole = () => {
    consoleEntries.value = [];
  };

  const attach = async (options: AttachOptions) => {
    const module = await compileFormCustomJavascript(options.scriptContent);
    const axios = getAxiosInstance();
    const hookTimeoutMs = Math.max(options.hookTimeoutMs ?? 15_000, 1_000);
    let destroying = false;
    let destroyed = false;
    const generation = Symbol("form-custom-javascript");
    let activeGeneration: symbol | null = generation;
    let handlingChange = false;
    let pendingChange: any = null;
    let data =
      options.form.submission?.data || ({} as Record<string, unknown>);
    let submission = options.form.submission || { data };
    submission.data = data;
    options.form.submission = submission;

    const assertActive = () => {
      if (destroying || destroyed || activeGeneration !== generation) {
        throw new Error("表單已卸載，忽略過期的客製 JavaScript 操作");
      }
    };

    const context: FormCustomScriptContext = {
      mode: options.mode,
      tenantId: options.tenantId,
      formId: options.formId,
      formCode: options.formCode,
      versionNo: options.versionNo,
      form: options.form,
      data,
      submission,
      axios,
      getValue: (path) => getPath(data, path),
      setValue: async (path, value) => {
        assertActive();
        setFormDataPath(data, path, value);
        const topLevelKey = path.split(".").filter(Boolean)[0];
        const component = topLevelKey
          ? options.form.getComponent?.(topLevelKey)
          : null;
        if (component?.setValue) component.setValue(data[topLevelKey], {
          modified: true,
          noUpdateEvent: true,
        });
      },
      setSelectOptions: async (key, items) => {
        assertActive();
        const component = options.form.getComponent?.(key);
        if (!component) return;
        component.component.data = component.component.data || {};
        component.component.data.values = items.map((item) => ({ ...item }));
        if (component.setItems) component.setItems(items, false);
        else if (component.triggerUpdate) await component.triggerUpdate(null, true);
        else await refreshComponent(options.form, key);
        assertActive();
      },
      setComponentDisabled: async (key, disabled) => {
        assertActive();
        const component = options.form.getComponent?.(key);
        if (!component) return;
        const effectiveDisabled = options.mode === "READ_ONLY" ? true : disabled;
        component.component.disabled = effectiveDisabled;
        component.disabled = effectiveDisabled;
        await refreshComponent(options.form, key);
        assertActive();
      },
      getComponent: (key) => options.form.getComponent?.(key),
      redraw: async () => {
        assertActive();
        await refreshSubmission(options.form);
        assertActive();
      },
      executeDataAction: async (
        actionCode,
        body = {},
        versionNo,
        invokeLifecycle: boolean = true,
      ) => {
        assertActive();
        const headers: Record<string, string | number> = {
          "X-FlowMint-Tenant": options.tenantId,
        };
        if (versionNo) headers["X-FlowMint-Action-Version"] = versionNo;
        let result: Record<string, unknown>;
        try {
          if (options.mode === "READ_ONLY") {
            const metadata = await axios.post(
              `${import.meta.env.VITE_API_URL}/fm/data-actions/${encodeURIComponent(actionCode)}/metadata`,
              {},
              { headers },
            );
            assertActive();
            if (
              metadata.data?.success !== import.meta.env.VITE_SUCCESS_FLAG ||
              metadata.data?.value?.actionType !== "QUERY"
            ) {
              throw new Error("唯讀表單只允許執行 QUERY Data Action");
            }
          }
          const response = await axios.post(
            `${import.meta.env.VITE_API_URL}/fm/data-actions/${encodeURIComponent(actionCode)}/execute`,
            body,
            { headers },
          );
          assertActive();
          if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
            throw new Error(response.data?.message || `${actionCode} 執行失敗`);
          }
          result = response.data?.value?.data || {};
        } catch (error) {
          if (invokeLifecycle) {
            try {
              await run("onDataActionError", {
                actionCode,
                request: body,
                actionVersion: versionNo,
                error,
              });
            } catch {
              // run() already records the hook failure; preserve the action error.
            }
          }
          throw error;
        }
        if (invokeLifecycle) {
          try {
            await run("onDataActionSuccess", {
              actionCode,
              request: body,
              actionVersion: versionNo,
              response: result,
            });
          } catch (hookError) {
            toast.warning(
              `Data Action ${actionCode} 已執行成功，但成功後處理失敗：${
                hookError instanceof Error ? hookError.message : "未知錯誤"
              }`,
            );
          }
        }
        return result;
      },
      notify: {
        success: (message) => toast.success(message),
        warning: (message) => toast.warning(message),
        error: (message) => toast.error(message),
      },
      log: (...values) => appendConsole("LOG", values),
      warn: (...values) => appendConsole("WARN", values),
      error: (...values) => appendConsole("ERROR", values),
    };

    const run = async (
      lifecycle: FormScriptLifecycle,
      additions: Partial<FormCustomScriptContext> = {},
    ) => {
      if ((destroying || destroyed) && lifecycle !== "onDestroy") return undefined;
      const handler = module[lifecycle];
      if (!handler) return undefined;
      let expired = false;
      const assertHookActive = () => {
        if (expired) throw new Error(`${lifecycle} 已逾時，忽略過期操作`);
      };
      try {
        const lifecycleDataAction =
          lifecycle === "onDataActionSuccess" || lifecycle === "onDataActionError"
            ? (actionCode: string, body?: Record<string, unknown>, versionNo?: number) =>
                (
                  context.executeDataAction as (
                    actionCode: string,
                    body?: Record<string, unknown>,
                    versionNo?: number,
                    invokeLifecycle?: boolean,
                  ) => Promise<Record<string, unknown>>
                )(actionCode, body, versionNo, false)
            : context.executeDataAction;
        const lifecycleContext: FormCustomScriptContext = {
          ...context,
          ...additions,
          executeDataAction: async (...args) => {
            assertHookActive();
            const result = await lifecycleDataAction(...args);
            assertHookActive();
            return result;
          },
          setValue: async (...args) => {
            assertHookActive();
            return context.setValue(...args);
          },
          setSelectOptions: async (...args) => {
            assertHookActive();
            return context.setSelectOptions(...args);
          },
          setComponentDisabled: async (...args) => {
            assertHookActive();
            return context.setComponentDisabled(...args);
          },
          redraw: async () => {
            assertHookActive();
            return context.redraw();
          },
          notify: {
            success: (message) => {
              assertHookActive();
              context.notify.success(message);
            },
            warning: (message) => {
              assertHookActive();
              context.notify.warning(message);
            },
            error: (message) => {
              assertHookActive();
              context.notify.error(message);
            },
          },
          log: (...values) => appendConsole("LOG", values, lifecycle),
          warn: (...values) => appendConsole("WARN", values, lifecycle),
          error: (...values) => appendConsole("ERROR", values, lifecycle),
        };
        let timeoutId: ReturnType<typeof setTimeout> | undefined;
        try {
          return await Promise.race([
            Promise.resolve(handler(lifecycleContext)),
            new Promise<never>((_, reject) => {
              timeoutId = setTimeout(() => {
                expired = true;
                reject(new Error(`${lifecycle} 執行逾時（${hookTimeoutMs}ms）`));
              }, hookTimeoutMs);
            }),
          ]);
        } finally {
          if (timeoutId) clearTimeout(timeoutId);
        }
      } catch (error) {
        appendConsole("ERROR", [error], lifecycle);
        throw error;
      }
    };

    const processChange = async (changed: any) => {
      if (destroying || destroyed) return;
      handlingChange = true;
      try {
        data = changed?.data || options.form.submission?.data || data;
        submission = options.form.submission || { data };
        submission.data = data;
        options.form.submission = submission;
        context.data = data;
        context.submission = submission;
        await run("onFieldChange", { changed: changed?.changed || changed });
      } catch (error) {
        toast.error(error instanceof Error ? error.message : "onFieldChange 執行失敗");
      } finally {
        handlingChange = false;
        const next = pendingChange;
        pendingChange = null;
        if (next && !destroying && !destroyed) void processChange(next);
      }
    };
    const changeHandler = async (changed: any) => {
      if (destroying || destroyed) return;
      if (handlingChange) {
        pendingChange = changed;
        return;
      }
      await processChange(changed);
    };
    if (options.mode !== "READ_ONLY") {
      options.form.on("change", changeHandler);
    }

    try {
      await run("onFormLoad");
      if (destroying || destroyed) return {
        context,
        run,
        detach: async () => undefined,
      };
      await refreshSubmission(options.form);
    } catch (error) {
      if (!destroying && !destroyed) {
        toast.error(error instanceof Error ? error.message : "onFormLoad 執行失敗");
      }
    }

    return {
      context,
      run,
      detach: async () => {
        if (destroying || destroyed) return;
        destroying = true;
        activeGeneration = null;
        pendingChange = null;
        if (options.mode !== "READ_ONLY") {
          options.form.off?.("change", changeHandler);
        }
        await run("onDestroy").catch(() => undefined);
        destroyed = true;
      },
    };
  };

  return { attach, consoleEntries, clearConsole };
};
