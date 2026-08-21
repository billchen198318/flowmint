import { ref, type Ref } from "vue";
import { toast } from "vue3-toastify";

interface RuntimeAttachmentOptions {
  tenantId: Ref<string>;
  selectedForm: Ref<any>;
  getFormInstance: () => any;
}

const ALLOWED_FILE_TYPES = [
  ".pdf", ".jpg", ".jpeg", ".png", ".bmp",
  ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
  ".zip", ".7z", ".rar",
];

export const useRuntimeAttachments = (options: RuntimeAttachmentOptions) => {
  const attachmentFields = ref<any[]>([]);
  const attachmentFiles = ref<Record<string, any[]>>({});
  const uploadSessionId = ref("");
  const uploadBatchExpiresDate = ref("");
  const uploadingField = ref("");

  const ok = (response: any) =>
    response?.success === import.meta.env.VITE_SUCCESS_FLAG;
  const attachmentPost = (path: string, body: any) =>
    useApi(`/fm/attachments${path}`, {
      method: "POST",
      body,
      headers: { "X-FlowMint-Tenant": options.tenantId.value },
    });
  const showError = (response: any, fallback: string) =>
    toast.warning(response?.message || fallback);

  const resetAttachments = () => {
    attachmentFields.value = [];
    attachmentFiles.value = {};
    uploadSessionId.value = "";
    uploadBatchExpiresDate.value = "";
    uploadingField.value = "";
  };

  const collectFileFields = (components: any[] = []): any[] =>
    components.flatMap((item: any) => [
      ...(item.type === "file" ? [item] : []),
      ...collectFileFields(item.components || []),
      ...(Array.isArray(item.columns) ? item.columns : []).flatMap(
        (column: any) => collectFileFields(column.components || []),
      ),
      ...(Array.isArray(item.rows) ? item.rows : []).flatMap((row: any[]) =>
        (Array.isArray(row) ? row : []).flatMap(
          (cell: any) => collectFileFields(cell.components || []),
        )),
    ]);

  const prepareAttachmentFields = (schema: any) => {
    resetAttachments();
    attachmentFields.value = collectFileFields(schema.components || []);
    attachmentFields.value.forEach((field: any) => {
      field.hidden = true;
      attachmentFiles.value[field.key] = [];
    });
  };

  const createUploadBatch = async (expired: boolean) => {
    const selectedForm = options.selectedForm.value;
    if (!selectedForm) return false;
    const response: any = await attachmentPost("/sessions", {
      formId: selectedForm.formId,
      formVersionNo: selectedForm.formVersionNo,
    });
    if (!ok(response)) {
      showError(response, "無法建立附件上傳批次");
      return false;
    }
    uploadSessionId.value = response.value?.uploadSessionId || "";
    uploadBatchExpiresDate.value = response.value?.expiresDate || "";
    if (expired) {
      attachmentFiles.value = Object.fromEntries(
        attachmentFields.value.map((field: any) => [field.key, []]),
      );
      const formInstance = options.getFormInstance();
      attachmentFields.value.forEach((field: any) => {
        if (formInstance?.submission?.data) {
          formInstance.submission.data[field.key] = [];
        }
      });
      toast.warning("附件上傳批次已逾期，已建立新批次，請重新上傳附件");
    }
    return true;
  };

  const parseFileSize = (value: string) => {
    const normalized = String(value).trim().toUpperCase().replaceAll(" ", "");
    if (normalized.endsWith("MB")) {
      return Number.parseInt(normalized) * 1024 * 1024;
    }
    if (normalized.endsWith("KB")) return Number.parseInt(normalized) * 1024;
    return Number.parseInt(normalized) || 8 * 1024 * 1024;
  };

  const acceptedFileTypes = (field: any) => {
    const values = (field.fileTypes || []).flatMap((item: any) =>
      String(typeof item === "string" ? item : item.value || "").split(/[, ]+/));
    const safe = values.map((value: string) => value.trim().toLowerCase())
      .filter((value: string) => ALLOWED_FILE_TYPES.includes(
        value.startsWith(".") ? value : `.${value}`,
      ));
    return safe.length ? safe.join(",") : ALLOWED_FILE_TYPES.join(",");
  };

  const uploadAttachment = async (field: any, event: Event) => {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!uploadSessionId.value || (uploadBatchExpiresDate.value
      && new Date(uploadBatchExpiresDate.value).getTime() <= Date.now())) {
      if (!await createUploadBatch(true)) return;
    }
    const maximum = parseFileSize(field.fileMaxSize || "8MB");
    if (file.size > maximum) {
      toast.warning(`${field.label || field.key} 單檔不可超過 ${field.fileMaxSize || "8MB"}`);
      input.value = "";
      return;
    }
    const maxFiles = Number(field.maxNumberOfFiles || (field.multiple ? 10 : 1));
    if ((attachmentFiles.value[field.key]?.length || 0) >= maxFiles) {
      toast.warning(`${field.label || field.key} 最多上傳 ${maxFiles} 個附件`);
      input.value = "";
      return;
    }
    const totalMaximum = parseFileSize(field.flowmintMaxTotalSize || "20MB");
    const currentTotal = (attachmentFiles.value[field.key] || [])
      .reduce((sum: number, item: any) => sum + Number(item.fileSize || 0), 0);
    if (currentTotal + file.size > totalMaximum) {
      toast.warning(`${field.label || field.key} 附件總容量不可超過 ${field.flowmintMaxTotalSize || "20MB"}`);
      input.value = "";
      return;
    }
    uploadingField.value = field.key;
    try {
      const body = new FormData();
      body.append("uploadSessionId", uploadSessionId.value);
      body.append("fieldKey", field.key);
      body.append("file", file);
      const response: any = await attachmentPost("/sessions/files", body);
      if (!ok(response)) {
        if (String(response?.message || "").includes("不存在或已過期")) {
          await createUploadBatch(true);
        } else {
          showError(response, "附件上傳失敗");
        }
        return;
      }
      attachmentFiles.value[field.key].push(response.value);
      options.getFormInstance().submission.data[field.key] =
        attachmentFiles.value[field.key].map((item: any) => item.attachmentId);
      toast.success("附件已上傳");
    } finally {
      uploadingField.value = "";
      input.value = "";
    }
  };

  const deleteAttachment = async (field: any, attachment: any) => {
    const response: any = await attachmentPost("/sessions/files/delete", {
      uploadSessionId: uploadSessionId.value,
      attachmentId: attachment.attachmentId,
    });
    if (!ok(response)) return showError(response, "附件刪除失敗");
    attachmentFiles.value[field.key] = attachmentFiles.value[field.key]
      .filter((item: any) => item.attachmentId !== attachment.attachmentId);
    options.getFormInstance().submission.data[field.key] =
      attachmentFiles.value[field.key].map((item: any) => item.attachmentId);
  };

  return {
    attachmentFields,
    attachmentFiles,
    uploadSessionId,
    uploadBatchExpiresDate,
    uploadingField,
    resetAttachments,
    prepareAttachmentFields,
    createUploadBatch,
    acceptedFileTypes,
    uploadAttachment,
    deleteAttachment,
  };
};
