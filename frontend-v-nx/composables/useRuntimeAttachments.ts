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
  let schemaComponents: any[] = [];
  let hasFileComponents = false;
  const uploadSessionId = ref("");
  const uploadBatchExpiresDate = ref("");
  const uploadingField = ref("");
  let attachmentGeneration = 0;

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
    attachmentGeneration += 1;
    schemaComponents = [];
    hasFileComponents = false;
    attachmentFields.value = [];
    attachmentFiles.value = {};
    uploadSessionId.value = "";
    uploadBatchExpiresDate.value = "";
    uploadingField.value = "";
  };

  const fileComponents = (components: any[] = []): any[] =>
    components.flatMap((item: any) => [
      ...(item.type === "file" ? [item] : []),
      ...fileComponents(item.components || []),
      ...(item.columns || []).flatMap((column: any) =>
        fileComponents(column.components || [])),
      ...(item.rows || []).flatMap((row: any[]) =>
        row.flatMap((cell: any) => fileComponents(cell.components || []))),
    ]);

  const fieldId = (path: (string | number)[]) => path.map(String).join(".");
  const dataAt = (data: any, path: (string | number)[]) =>
    path.reduce((value: any, key) => value?.[key], data);
  const setAttachmentIds = (field: any, ids: string[]) => {
    const data = options.getFormInstance()?.submission?.data;
    if (!data) return false;
    let target: any = data;
    for (const segment of field.path.slice(0, -1)) {
      if (target?.[segment] == null && typeof segment === "string") {
        target[segment] = {};
      }
      target = target?.[segment];
      if (target == null) return false;
    }
    target[field.path[field.path.length - 1]] = ids;
    return true;
  };

  const expandFileFields = (components: any[], data: any,
    parent: (string | number)[] = []): any[] =>
    components.flatMap((item: any) => {
      const path = [...parent, item.key];
      if (item.type === "file") {
        return [{ ...item, path, stateKey: fieldId(path) }];
      }
      if (item.type === "container") {
        return expandFileFields(item.components || [], data?.[item.key] || {}, path);
      }
      if (item.type === "datagrid" || item.type === "editgrid") {
        const rows = Array.isArray(data?.[item.key]) ? data[item.key] : [];
        return rows.flatMap((row: any, index: number) =>
          expandFileFields(item.components || [], row, [...path, index]));
      }
      return [
        ...expandFileFields(item.components || [], data, parent),
        ...(item.columns || []).flatMap((column: any) =>
          expandFileFields(column.components || [], data, parent)),
        ...(item.rows || []).flatMap((row: any[]) =>
          row.flatMap((cell: any) =>
            expandFileFields(cell.components || [], data, parent))),
      ];
    });

  const refreshAttachmentFields = () => {
    const data = options.getFormInstance()?.submission?.data || {};
    const fields = expandFileFields(schemaComponents, data);
    const previousFields = attachmentFields.value;
    const previousFiles = attachmentFiles.value;
    const knownFiles = new Map(Object.values(attachmentFiles.value)
      .flat().map((file: any) => [file.attachmentId, file]));
    const nextFiles: Record<string, any[]> = {};
    fields.forEach((field: any) => {
      const ids = dataAt(data, field.path);
      nextFiles[field.stateKey] = Array.isArray(ids)
        ? ids.map((id: string) => knownFiles.get(id)).filter(Boolean)
        : [];
    });
    const activeIds = new Set(Object.values(nextFiles)
      .flat().map((file: any) => file.attachmentId));
    previousFields.forEach((field: any) => {
      (previousFiles[field.stateKey] || []).forEach((file: any) => {
        if (activeIds.has(file.attachmentId)) return;
        const orphanKey = `orphan:${file.attachmentId}`;
        fields.push({ ...field, stateKey: orphanKey, orphaned: true,
          label: `${field.label || field.key}（表單列已移除）` });
        nextFiles[orphanKey] = [file];
      });
    });
    attachmentFields.value = fields;
    attachmentFiles.value = nextFiles;
  };

  const prepareAttachmentFields = (schema: any) => {
    resetAttachments();
    schemaComponents = schema.components || [];
    const components = fileComponents(schemaComponents);
    hasFileComponents = components.length > 0;
    components.forEach((field: any) => {
      field.hidden = true;
    });
    attachmentFields.value = expandFileFields(schemaComponents, {});
    attachmentFiles.value = Object.fromEntries(
      attachmentFields.value.map((field: any) => [field.stateKey, []]));
  };

  const createUploadBatch = async (expired: boolean) => {
    const selectedForm = options.selectedForm.value;
    if (!selectedForm) return false;
    const generation = attachmentGeneration;
    const response: any = await attachmentPost("/sessions", {
      formId: selectedForm.formId,
      formVersionNo: selectedForm.formVersionNo,
    });
    if (generation !== attachmentGeneration || options.selectedForm.value !== selectedForm) {
      return false;
    }
    if (!ok(response)) {
      showError(response, "無法建立附件上傳批次");
      return false;
    }
    uploadSessionId.value = response.value?.uploadSessionId || "";
    uploadBatchExpiresDate.value = response.value?.expiresDate || "";
    if (expired) {
      attachmentFiles.value = Object.fromEntries(
        attachmentFields.value.map((field: any) => [field.stateKey, []]),
      );
      const formInstance = options.getFormInstance();
      attachmentFields.value.forEach((field: any) => {
        if (formInstance?.submission?.data && !field.orphaned) {
          setAttachmentIds(field, []);
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
    return safe.length ? safe.join(",")
      : (field.fileTypes || []).length ? ".flowmint-invalid" : ALLOWED_FILE_TYPES.join(",");
  };

  const uploadAttachment = async (field: any, event: Event) => {
    if (field.orphaned) return;
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const generation = attachmentGeneration;
    const formInstance = options.getFormInstance();
    const rowAtUpload = dataAt(options.getFormInstance()?.submission?.data,
      field.path.slice(0, -1));
    if (!uploadSessionId.value || (uploadBatchExpiresDate.value
      && new Date(uploadBatchExpiresDate.value).getTime() <= Date.now())) {
      try {
        if (!await createUploadBatch(true)) {
          input.value = "";
          return;
        }
      } catch {
        if (generation === attachmentGeneration) {
          toast.warning("無法建立附件上傳批次，請檢查連線後重試");
        }
        input.value = "";
        return;
      }
    }
    if (generation !== attachmentGeneration || formInstance !== options.getFormInstance()) {
      input.value = "";
      return;
    }
    const maximum = parseFileSize(field.fileMaxSize || "8MB");
    if (file.size > maximum) {
      toast.warning(`${field.label || field.key} 單檔不可超過 ${field.fileMaxSize || "8MB"}`);
      input.value = "";
      return;
    }
    const maxFiles = field.multiple ? Number(field.maxNumberOfFiles || 10) : 1;
    if ((attachmentFiles.value[field.stateKey]?.length || 0) >= maxFiles) {
      toast.warning(`${field.label || field.key} 最多上傳 ${maxFiles} 個附件`);
      input.value = "";
      return;
    }
    const totalMaximum = parseFileSize(field.flowmintMaxTotalSize || "20MB");
    const currentTotal = (attachmentFiles.value[field.stateKey] || [])
      .reduce((sum: number, item: any) => sum + Number(item.fileSize || 0), 0);
    if (currentTotal + file.size > totalMaximum) {
      toast.warning(`${field.label || field.key} 附件總容量不可超過 ${field.flowmintMaxTotalSize || "20MB"}`);
      input.value = "";
      return;
    }
    uploadingField.value = field.stateKey;
    const sessionId = uploadSessionId.value;
    try {
      const body = new FormData();
      body.append("uploadSessionId", sessionId);
      body.append("fieldKey", field.key);
      body.append("file", file);
      const response: any = await attachmentPost("/sessions/files", body);
      if (generation !== attachmentGeneration || sessionId !== uploadSessionId.value
        || formInstance !== options.getFormInstance()) return;
      if (!ok(response)) {
        if (String(response?.message || "").includes("不存在或已過期")) {
          await createUploadBatch(true);
        } else {
          showError(response, "附件上傳失敗");
        }
        return;
      }
      const ids = dataAt(options.getFormInstance()?.submission?.data, field.path);
      if (!attachmentFields.value.some((item: any) => item.stateKey === field.stateKey)
        || rowAtUpload !== dataAt(options.getFormInstance()?.submission?.data,
          field.path.slice(0, -1))) {
        await attachmentPost("/sessions/files/delete", {
          uploadSessionId: uploadSessionId.value,
          attachmentId: response.value.attachmentId,
        });
        toast.warning("附件所在的表單列已移除，請重新上傳");
        return;
      }
      attachmentFiles.value[field.stateKey] ||= [];
      attachmentFiles.value[field.stateKey].push(response.value);
      setAttachmentIds(field, [
        ...(Array.isArray(ids) ? ids : []), response.value.attachmentId,
      ]);
      toast.success("附件已上傳");
    } catch {
      if (generation === attachmentGeneration && sessionId === uploadSessionId.value) {
        toast.warning("附件上傳失敗，請檢查連線後重試");
      }
    } finally {
      if (generation === attachmentGeneration) uploadingField.value = "";
      input.value = "";
    }
  };

  const deleteAttachment = async (field: any, attachment: any) => {
    const generation = attachmentGeneration;
    const sessionId = uploadSessionId.value;
    const response: any = await attachmentPost("/sessions/files/delete", {
      uploadSessionId: sessionId,
      attachmentId: attachment.attachmentId,
    });
    if (generation !== attachmentGeneration || sessionId !== uploadSessionId.value) return;
    if (!ok(response)) return showError(response, "附件刪除失敗");
    attachmentFiles.value[field.stateKey] = attachmentFiles.value[field.stateKey]
      .filter((item: any) => item.attachmentId !== attachment.attachmentId);
    if (field.orphaned) {
      attachmentFields.value = attachmentFields.value
        .filter((item: any) => item.stateKey !== field.stateKey);
      delete attachmentFiles.value[field.stateKey];
    } else {
      setAttachmentIds(field,
        attachmentFiles.value[field.stateKey].map((item: any) => item.attachmentId));
    }
  };

  return {
    attachmentFields,
    attachmentFiles,
    uploadSessionId,
    uploadBatchExpiresDate,
    uploadingField,
    resetAttachments,
    prepareAttachmentFields,
    refreshAttachmentFields,
    hasAttachmentFields: () => hasFileComponents,
    createUploadBatch,
    acceptedFileTypes,
    uploadAttachment,
    deleteAttachment,
  };
};
