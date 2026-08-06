export interface FormDataActionBinding {
  bindingId: string;
  event: string;
  actionCode: string;
  actionVersion?: number;
  requestMapping?: Record<string, string>;
  responseMapping?: Record<string, string>;
  statusTarget?: string;
  errorTarget?: string;
}

export interface FormDataActionUiSchema {
  engine: "FORMIO";
  version: number;
  dataActions?: FormDataActionBinding[];
}

export interface DataActionExecutionView {
  executionId: string;
  actionCode: string;
  versionNo: number;
  rolledBack: boolean;
  data: Record<string, unknown>;
}

export interface FormDataActionExecutionContext {
  tenantId: string;
  submissionData: Record<string, unknown>;
}
