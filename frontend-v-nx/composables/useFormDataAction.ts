import { getAxiosInstance } from "@/components/BaseHelper";
import type {
  DataActionExecutionView,
  FormDataActionBinding,
  FormDataActionExecutionContext,
} from "@/types/formDataAction";

const SUBMISSION_PATH_PATTERN =
  /^(?:submission\.)?[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)*$/;
const RESPONSE_PATH_PATTERN =
  /^[A-Za-z][A-Za-z0-9_]*(?:\.[A-Za-z][A-Za-z0-9_]*)*$/;

const getPath = (source: unknown, path: string): unknown => {
  if (!path) return source;
  return path.split(".").reduce<unknown>((value, segment) => {
    if (value === null || typeof value !== "object") return undefined;
    return (value as Record<string, unknown>)[segment];
  }, source);
};

export const setFormDataPath = (
  target: Record<string, unknown>,
  path: string,
  value: unknown,
) => {
  const segments = path.split(".").filter(Boolean);
  if (!segments.length) return;
  let cursor = target;
  segments.slice(0, -1).forEach((segment) => {
    const child = cursor[segment];
    if (child === null || typeof child !== "object" || Array.isArray(child)) {
      cursor[segment] = {};
    }
    cursor = cursor[segment] as Record<string, unknown>;
  });
  cursor[segments.at(-1)!] = value;
};

export const buildDataActionRequest = (
  mapping: Record<string, string> | null | undefined,
  submissionData: Record<string, unknown>,
) => {
  if (mapping == null) return {};
  if (typeof mapping !== "object" || Array.isArray(mapping)) {
    throw new Error("Data Action requestMapping 必須是物件");
  }
  return Object.fromEntries(
    Object.entries(mapping).map(([requestField, submissionPath]) => {
      if (!requestField.trim() || typeof submissionPath !== "string" || !submissionPath.trim()) {
        throw new Error("Data Action requestMapping 必須使用非空白字串欄位與路徑");
      }
      if (!SUBMISSION_PATH_PATTERN.test(submissionPath)) {
        throw new Error(`Data Action requestMapping 路徑格式不合法：${submissionPath}`);
      }
      return [
        requestField,
        getPath(submissionData, submissionPath.replace(/^submission\./, "")),
      ];
    }),
  );
};

export const useFormDataAction = () => {
  const execute = async (
    binding: FormDataActionBinding,
    context: FormDataActionExecutionContext,
  ): Promise<DataActionExecutionView> => {
    if (!context.tenantId) throw new Error("Data Action 缺少 Tenant context");
    if (!binding.actionCode) throw new Error("Data Action binding 缺少 actionCode");

    const headers: Record<string, string | number> = {
      "X-FlowMint-Tenant": context.tenantId,
    };
    if (binding.actionVersion) {
      headers["X-FlowMint-Action-Version"] = binding.actionVersion;
    }

    const response = await getAxiosInstance().post(
      `${import.meta.env.VITE_API_URL}/fm/data-actions/${encodeURIComponent(binding.actionCode)}/execute`,
      context.requestData ??
        buildDataActionRequest(binding.requestMapping, context.submissionData),
      { headers },
    );
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      throw new Error(response.data?.message || `Data Action ${binding.actionCode} 執行失敗`);
    }
    if (!response.data?.value) {
      throw new Error(`Data Action ${binding.actionCode} 未回傳執行結果`);
    }
    return response.data.value as DataActionExecutionView;
  };

  const applyResponse = (
    binding: FormDataActionBinding,
    execution: DataActionExecutionView,
    submissionData: Record<string, unknown>,
  ) => {
    const mapping = binding.responseMapping;
    if (mapping == null) return;
    if (typeof mapping !== "object" || Array.isArray(mapping)) {
      throw new Error("Data Action responseMapping 必須是物件");
    }
    const entries = Object.entries(mapping);
    const targets = new Set<string>();
    for (const [responsePath, submissionPath] of entries) {
      if (!RESPONSE_PATH_PATTERN.test(responsePath)) {
        throw new Error(`Data Action responseMapping 來源路徑格式不合法：${responsePath}`);
      }
      if (typeof submissionPath !== "string" || !submissionPath.trim()) {
        throw new Error("Data Action responseMapping 目標路徑必須是非空白字串");
      }
      const normalizedTarget = submissionPath.replace(/^submission\./, "");
      if (!targets.add(normalizedTarget)) {
        throw new Error(`Data Action responseMapping 目標欄位重複：${normalizedTarget}`);
      }
    }
    entries.forEach(([responsePath, submissionPath]) => {
        setFormDataPath(
          submissionData,
          submissionPath.replace(/^submission\./, ""),
          getPath(execution.data, responsePath),
        );
      });
  };

  return { execute, applyResponse };
};
