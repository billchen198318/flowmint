import type { AxiosInstance } from "axios";

export type FormScriptMode =
  | "DESIGNER_PREVIEW"
  | "RUNTIME_START"
  | "RUNTIME_TASK"
  | "READ_ONLY";

export type FormScriptLifecycle =
  | "onFormLoad"
  | "onFieldChange"
  | "beforeSubmit"
  | "afterSubmit"
  | "onDataActionSuccess"
  | "onDataActionError"
  | "onDestroy";

export interface FormScriptConsoleEntry {
  occurredAt: string;
  level: "LOG" | "WARN" | "ERROR";
  lifecycle?: FormScriptLifecycle;
  values: unknown[];
}

export interface FormCustomScriptContext {
  mode: FormScriptMode;
  tenantId: string;
  formId: string;
  formCode: string;
  versionNo: number;
  form: any;
  data: Record<string, unknown>;
  submission: Record<string, unknown>;
  changed?: any;
  actionType?: string;
  taskId?: string;
  formData?: Record<string, unknown> | null;
  actionCode?: string;
  actionVersion?: number;
  bindingId?: string;
  request?: Record<string, unknown>;
  response?: unknown;
  error?: unknown;
  axios: AxiosInstance;
  getValue: (path: string) => unknown;
  setValue: (path: string, value: unknown) => Promise<void>;
  setSelectOptions: (
    key: string,
    items: Array<{ label: string; value: unknown; disabled?: boolean }>,
  ) => Promise<void>;
  setComponentDisabled: (key: string, disabled: boolean) => Promise<void>;
  getComponent: (key: string) => any;
  redraw: () => Promise<void>;
  executeDataAction: (
    actionCode: string,
    body?: Record<string, unknown>,
    versionNo?: number,
  ) => Promise<Record<string, unknown>>;
  notify: {
    success: (message: string) => void;
    warning: (message: string) => void;
    error: (message: string) => void;
  };
  log: (...values: unknown[]) => void;
  warn: (...values: unknown[]) => void;
  error: (...values: unknown[]) => void;
}

export type FormCustomScriptModule = Partial<
  Record<
    FormScriptLifecycle,
    (context: FormCustomScriptContext) => unknown | Promise<unknown>
  >
>;

export type FormScriptRunner = (
  lifecycle: FormScriptLifecycle,
  additions?: Partial<FormCustomScriptContext>,
) => Promise<any>;
