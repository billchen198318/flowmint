<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import BpmnModeler from "bpmn-js/lib/Modeler";
import { is } from "bpmn-js/lib/util/ModelUtil";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import Toolbar from "@/components/Toolbar.vue";
import ApprovalAuthorityPanel from "./ApprovalAuthorityPanel.vue";
import SequenceFlowConditionPanel from "./SequenceFlowConditionPanel.vue";
import {
  checkInvalid,
  escapeQifuHtmlMsg,
  getAxiosInstance,
  invalidFeedback,
} from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";

const props = defineProps<{ edit?: boolean }>();
const route = useRoute();
const router = useRouter();
const { showLoading, hideLoading, confirmFire } = useSwalLoading();
const tenants = ref<any[]>([]);
const checkFields = ref<Record<string, string>>({});
const canvas = ref<HTMLElement | null>(null);
const selectedVersion = ref<any>(null);
const publishedForms = ref<any[]>([]);
const selectedElement = ref<any>(null);
const selectedTask = ref<any>(null);
const selectedFlow = ref<any>(null);
const previewAccount = ref("");
const previewFormData = ref("{}");
const resolverPreview = ref<any[]>([]);
const resolverAccounts = ref<any[]>([]);
const approvalGroups = ref<any[]>([]);
const approvalLevels = ref<any[]>([]);
const orgTitles = ref<any[]>([]);
const orgDuties = ref<any[]>([]);
const orgUnits = ref<any[]>([]);
const addStartPolicy = () => {
  if (selectedVersion.value?.versionStatus !== "DRAFT") return;
  selectedVersion.value.startPolicies ||= [];
  selectedVersion.value.startPolicies.push({
    policySeq: selectedVersion.value.startPolicies.length + 1,
    subjectType: "ALL",
    subjectRefId: "",
    allowStart: "Y",
  });
};
const removeStartPolicy = (index: number) => {
  if (selectedVersion.value?.versionStatus !== "DRAFT") return;
  selectedVersion.value.startPolicies.splice(index, 1);
  selectedVersion.value.startPolicies.forEach(
    (policy: any, policyIndex: number) => policy.policySeq = policyIndex + 1,
  );
};
const startPolicyOptions = (subjectType: string) => {
  if (subjectType === "ACCOUNT") return resolverAccounts.value;
  if (subjectType === "APPROVAL_GROUP") return approvalGroups.value;
  if (subjectType === "ORG_UNIT") return orgUnits.value;
  return [];
};
const changeStartPolicyType = (policy: any) => {
  policy.subjectRefId = "";
};
let modeler: any = null;
const newForm = () => ({
  oid: "",
  tenantId: "",
  processKey: "",
  processName: "",
  category: "",
  status: "DRAFT",
  description: "",
  versions: [] as any[],
});
const form = ref<any>(newForm());
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const responseOk = (response: any) =>
  response.data?.success === import.meta.env.VITE_SUCCESS_FLAG;
const showResponse = (response: any) => {
  if (!responseOk(response)) {
    toast.warning(escapeQifuHtmlMsg(response.data?.message || "操作失敗"));
    return false;
  }
  toast.success(response.data.message);
  return true;
};
const selectedTaskRule = computed(() =>
  selectedVersion.value?.taskForms?.find(
    (item: any) => item.taskDefKey === selectedTask.value?.id,
  ),
);
const selectedTaskPolicy = computed(() =>
  selectedVersion.value?.taskPolicies?.find(
    (item: any) => item.taskDefKey === selectedTask.value?.id,
  ),
);
const selectedAssignmentRule = computed(() =>
  selectedVersion.value?.assignmentRules?.find(
    (item: any) => item.taskDefKey === selectedTask.value?.id && item.ruleSeq === 1,
  ),
);
const ruleConfig = () => {
  try {
    return JSON.parse(selectedAssignmentRule.value?.resolverConfig || "{}");
  } catch {
    return {};
  }
};
const selectedFixedAccounts = computed({
  get: () => ruleConfig().accounts || [],
  set: (accounts: string[]) => {
    if (selectedAssignmentRule.value)
      selectedAssignmentRule.value.resolverConfig = JSON.stringify({ accounts });
  },
});
const selectedApprovalGroup = computed({
  get: () => ruleConfig().approvalGroupId || "",
  set: (approvalGroupId: string) => {
    if (selectedAssignmentRule.value)
      selectedAssignmentRule.value.resolverConfig = JSON.stringify({ approvalGroupId });
  },
});
const selectedApprovalLevel = computed({
  get: () => ruleConfig().approvalLevelId || "",
  set: (approvalLevelId: string) => {
    if (selectedAssignmentRule.value)
      selectedAssignmentRule.value.resolverConfig = JSON.stringify({ approvalLevelId });
  },
});
const selectedOrgTitle = computed({
  get: () => ruleConfig().titleId || "",
  set: (titleId: string) => {
    if (selectedAssignmentRule.value)
      selectedAssignmentRule.value.resolverConfig = JSON.stringify({ titleId });
  },
});
const selectedOrgDuty = computed({
  get: () => ruleConfig().dutyId || "",
  set: (dutyId: string) => {
    if (selectedAssignmentRule.value)
      selectedAssignmentRule.value.resolverConfig = JSON.stringify({ dutyId });
  },
});
const selectedApprovalAuthority = computed({
  get: () => ruleConfig().approvalAuthorityId || "",
  set: (approvalAuthorityId: string) => {
    if (selectedAssignmentRule.value)
      selectedAssignmentRule.value.resolverConfig = JSON.stringify({ approvalAuthorityId });
  },
});
const ensureSelectedAssignmentRule = () => {
  if (!selectedTask.value || selectedVersion.value?.versionStatus !== "DRAFT") return;
  selectedVersion.value.assignmentRules ||= [];
  if (!selectedAssignmentRule.value) {
    selectedVersion.value.assignmentRules.push({
      taskDefKey: selectedTask.value.id,
      ruleSeq: 1,
      resolverType: "DIRECT_MANAGER",
      resolverConfig: "{}",
      fallbackConfig: null,
      maxResults: 100,
      status: "ACTIVE",
    });
  }
};
const ensureSelectedTaskPolicy = () => {
  if (
    !selectedTask.value ||
    !selectedVersion.value ||
    selectedVersion.value.versionStatus !== "DRAFT"
  )
    return;
  selectedVersion.value.taskPolicies ||= [];
  if (selectedTaskPolicy.value) {
    selectedTaskPolicy.value.taskName =
      selectedTask.value.businessObject?.name || selectedTask.value.id;
    return;
  }
  selectedVersion.value.taskPolicies.push({
    taskDefKey: selectedTask.value.id,
    taskName: selectedTask.value.businessObject?.name || selectedTask.value.id,
    assignmentMode: "ASSIGNEE",
    selfApprovalPolicy: "SKIP_TO_NEXT",
    duplicatePolicy: "MERGE_CONSECUTIVE",
    allowReject: "Y",
    allowReturn: "N",
    allowTransfer: "N",
    allowAddSign: "N",
    commentRequired: "ON_REJECT_RETURN",
    dueHours: null,
    reminderBeforeHours: null,
  });
};
const selectedFormValue = computed(() => {
  const rule = selectedTaskRule.value;
  return rule ? `${rule.formId}::${rule.formVersionNo}` : "";
});
const selectedPublishedForm = computed(() => publishedForms.value.find(
  (item: any) => item.formId === selectedTaskRule.value?.formId
    && item.formVersionNo === selectedTaskRule.value?.formVersionNo,
));
const conditionSchemaContent = computed(() => {
  const bindings = selectedVersion.value?.taskForms || [];
  const schemas = bindings
    .map((binding: any) => publishedForms.value.find(
      (item: any) => item.formId === binding.formId
        && item.formVersionNo === binding.formVersionNo,
    )?.schemaContent)
    .filter(Boolean);
  if (!schemas.length || schemas.length !== bindings.length) return "";
  const catalogs = schemas.map((content: string) => {
    const fields = new Map<string, any>();
    const collect = (components: any[] = [], insideGrid = false) => {
      for (const component of components) {
        const nestedGrid = insideGrid || ["datagrid", "editgrid"].includes(component?.type);
        if (component?.key && component?.input !== false && !insideGrid)
          fields.set(component.key, component);
        collect(component?.components, nestedGrid);
        for (const column of Array.isArray(component?.columns) ? component.columns : [])
          collect(column?.components, nestedGrid);
        for (const row of Array.isArray(component?.rows) ? component.rows : [])
          for (const cell of Array.isArray(row) ? row : [])
            collect(cell?.components, nestedGrid);
      }
    };
    collect(JSON.parse(content).components || []);
    return fields;
  });
  const common = [...catalogs[0].entries()]
    .filter(([key]) => catalogs.every((catalog) => catalog.has(key)))
    .map(([, component]) => component);
  return JSON.stringify({ display: "form", components: common });
});
const loadPublishedForms = async () => {
  if (!form.value.tenantId) return;
  const response = await post("/published-form-options", {
    tenantId: form.value.tenantId,
  });
  publishedForms.value = response.data?.value || [];
};
const loadResolverOptions = async () => {
  if (!form.value.tenantId) return;
  const [accountResponse, groupResponse, levelResponse, titleResponse, dutyResponse,
    unitResponse] =
    await Promise.all([
    post("/resolver-account-options", { tenantId: form.value.tenantId }),
    post("/approval-group-options", { tenantId: form.value.tenantId }),
    post("/approval-level-options", { tenantId: form.value.tenantId }),
    post("/org-title-options", { tenantId: form.value.tenantId }),
    post("/org-duty-options", { tenantId: form.value.tenantId }),
    post("/org-unit-options", { tenantId: form.value.tenantId }),
  ]);
  resolverAccounts.value = accountResponse.data?.value || [];
  approvalGroups.value = groupResponse.data?.value || [];
  approvalLevels.value = levelResponse.data?.value || [];
  orgTitles.value = titleResponse.data?.value || [];
  orgDuties.value = dutyResponse.data?.value || [];
  orgUnits.value = unitResponse.data?.value || [];
};
const bindModelerEvents = () => {
  const selectElement = (element: any) => {
    element = element?.labelTarget || element;
    selectedElement.value = element || null;
    selectedTask.value = is(element, "bpmn:UserTask") ? element : null;
    selectedFlow.value = is(element, "bpmn:SequenceFlow") ? element : null;
    ensureSelectedTaskPolicy();
    ensureSelectedAssignmentRule();
  };
  modeler.get("eventBus").on("selection.changed", (event: any) => {
    selectElement(event.newSelection?.[0]);
  });
  modeler.get("eventBus").on("element.click", (event: any) => {
    selectElement(event.element);
  });
  modeler.get("eventBus").on("element.changed", (event: any) => {
    if (selectedTask.value?.id === event.element?.id)
      selectedTask.value = event.element;
    if (selectedFlow.value?.id === event.element?.id)
      selectedFlow.value = event.element;
  });
};
const changeSelectedForm = (event: Event) => {
  if (!selectedTask.value || selectedVersion.value?.versionStatus !== "DRAFT")
    return;
  const value = (event.target as HTMLSelectElement).value;
  const remaining = (selectedVersion.value.taskForms || []).filter(
    (item: any) => item.taskDefKey !== selectedTask.value.id,
  );
  if (value) {
    const [formId, formVersionNo] = value.split("::");
    remaining.push({
      taskDefKey: selectedTask.value.id,
      formId,
      formVersionNo: Number(formVersionNo),
    });
  }
  selectedVersion.value.taskForms = remaining;
};
const currentTaskForms = () => {
  if (!modeler || !selectedVersion.value) return [];
  const taskKeys = new Set(
    modeler
      .get("elementRegistry")
      .filter((item: any) => is(item, "bpmn:UserTask"))
      .map((item: any) => item.id),
  );
  return (selectedVersion.value.taskForms || []).filter((item: any) =>
    taskKeys.has(item.taskDefKey),
  );
};
const currentTaskPolicies = () => {
  if (!modeler || !selectedVersion.value) return [];
  const taskKeys = new Set(
    modeler
      .get("elementRegistry")
      .filter((item: any) => is(item, "bpmn:UserTask"))
      .map((item: any) => item.id),
  );
  return (selectedVersion.value.taskPolicies || []).filter((item: any) =>
    taskKeys.has(item.taskDefKey),
  );
};
const currentAssignmentRules = () => {
  if (!modeler || !selectedVersion.value) return [];
  const taskKeys = new Set(modeler.get("elementRegistry")
    .filter((item: any) => is(item, "bpmn:UserTask")).map((item: any) => item.id));
  return (selectedVersion.value.assignmentRules || []).filter((item: any) =>
    taskKeys.has(item.taskDefKey));
};
const conditionFieldKeys = () => {
  const keys = new Set<string>();
  const collect = (components: any[] = [], insideGrid = false) => {
    for (const component of components) {
      const nestedGrid = insideGrid || component?.type === "datagrid";
      if (component?.key && component?.input !== false && !insideGrid)
        keys.add(component.key);
      collect(component?.components, nestedGrid);
      for (const column of Array.isArray(component?.columns)
        ? component.columns
        : [])
        collect(column?.components, nestedGrid);
      for (const row of Array.isArray(component?.rows) ? component.rows : [])
        for (const cell of Array.isArray(row) ? row : [])
          collect(cell?.components, nestedGrid);
    }
  };
  try {
    collect(JSON.parse(conditionSchemaContent.value || "{}").components || []);
  } catch {
    return new Set<string>();
  }
  return keys;
};
const validateSequenceFlows = () => {
  if (!modeler) return true;
  const fieldKeys = conditionFieldKeys();
  const elements = modeler.get("elementRegistry").getAll();
  for (const gateway of elements.filter(
    (item: any) =>
      is(item, "bpmn:ExclusiveGateway") || is(item, "bpmn:InclusiveGateway"),
  )) {
    const outgoing = gateway.outgoing || [];
    if (outgoing.length <= 1) continue;
    if (outgoing.length > 1 && !gateway.businessObject?.default) {
      toast.warning(`${gateway.businessObject?.name || gateway.id} 必須設定 Default Flow`);
      return false;
    }
    for (const flow of outgoing) {
      const businessObject = flow.businessObject;
      const isDefault = gateway.businessObject?.default?.id === flow.id;
      const body = businessObject?.conditionExpression?.body?.trim() || "";
      if (isDefault && body) {
        toast.warning(`${businessObject?.name || flow.id} 是 Default Flow，不可同時設定條件`);
        return false;
      }
      if (!isDefault && !body) {
        toast.warning(`${businessObject?.name || flow.id} 尚未設定分流條件`);
        return false;
      }
      for (const fieldName of body.matchAll(/flowmintFormData\.([A-Za-z][A-Za-z0-9_]*)/g)) {
        if (!fieldKeys.has(fieldName[1])) {
          toast.warning(`${businessObject?.name || flow.id} 引用了不存在的表單欄位 ${fieldName[1]}`);
          return false;
        }
      }
    }
  }
  return true;
};
const ensureSequenceFlowLabels = () => {
  if (!modeler || selectedVersion.value?.versionStatus !== "DRAFT") return;
  const modeling = modeler.get("modeling");
  const flows = modeler
    .get("elementRegistry")
    .filter((item: any) => is(item, "bpmn:SequenceFlow"));
  for (const flow of flows) {
    if (flow.businessObject?.name) continue;
    const isDefault = flow.source?.businessObject?.default?.id === flow.id;
    const body = flow.businessObject?.conditionExpression?.body || "";
    if (!isDefault && !body) continue;
    const readable = isDefault
      ? "其他／預設"
      : body
          .replace(/^\$\{/, "")
          .replace(/\}$/, "")
          .replaceAll("flowmintFormData.", "")
          .replaceAll(" == ", " = ")
          .replaceAll(" && ", " 且 ")
          .replaceAll(" || ", " 或 ");
    modeling.updateProperties(flow, {
      name: readable.length > 80 ? `${readable.slice(0, 77)}…` : readable,
    });
  }
};
const openVersion = async (version: any) => {
  selectedVersion.value = version;
  selectedElement.value = null;
  selectedTask.value = null;
  selectedFlow.value = null;
  await nextTick();
  if (!modeler && canvas.value) {
    modeler = new BpmnModeler({ container: canvas.value });
    bindModelerEvents();
  }
  if (modeler && version?.bpmnXml) {
    await modeler.importXML(version.bpmnXml);
    ensureSequenceFlowLabels();
    modeler.get("canvas").zoom("fit-viewport");
  }
};
const apply = async (value: any) => {
  form.value = value;
  await loadPublishedForms();
  await loadResolverOptions();
  const draft = value.versions?.find(
    (item: any) => item.versionStatus === "DRAFT",
  );
  await openVersion(draft || value.versions?.[0]);
};
const load = async () => {
  if (!props.edit) return;
  showLoading();
  try {
    const response = await post("/load", { oid: route.params.id });
    if (!responseOk(response)) {
      toast.warning(
        escapeQifuHtmlMsg(response.data?.message || "載入流程失敗"),
      );
      return;
    }
    checkFields.value = {};
    await apply(response.data.value);
  } catch (error: any) {
    toast.error(error?.message || "載入流程失敗");
  } finally {
    hideLoading();
  }
};
const clear = () => {
  checkFields.value = {};
  form.value = newForm();
};
const validate = () => {
  const fields: Record<string, string> = {};
  if (!form.value.tenantId) fields.tenantId = "請選擇 Tenant";
  if (!form.value.processKey) fields.processKey = "請輸入流程代碼";
  else if (!/^[A-Za-z][A-Za-z0-9_-]*$/.test(form.value.processKey))
    fields.processKey =
      "流程代碼須以英文字母開頭，且只能包含英數字、底線或連字號";
  if (!form.value.processName?.trim()) fields.processName = "請輸入流程名稱";
  checkFields.value = fields;
  if (Object.keys(fields).length) {
    toast.warning(Object.values(fields)[0]);
    return false;
  }
  return true;
};
const save = async () => {
  if (!validate() || !validateSequenceFlows()) return;
  showLoading();
  try {
    const draftOid =
      selectedVersion.value?.versionStatus === "DRAFT"
        ? selectedVersion.value.oid
        : "";
    const draftXml =
      draftOid && modeler ? (await modeler.saveXML({ format: true })).xml : "";
    const draftTaskForms = draftOid ? currentTaskForms() : [];
    const draftTaskPolicies = draftOid ? currentTaskPolicies() : [];
    const draftAssignmentRules = draftOid ? currentAssignmentRules() : [];
    const draftStartPolicies = draftOid ? selectedVersion.value.startPolicies || [] : [];
    let response = await post(props.edit ? "/update" : "/save", form.value);
    checkFields.value = response.data?.checkFields || {};
    if (!showResponse(response)) return;
    if (!props.edit) {
      router.push(
        PageConstants.frontendNamespace + "/edit/" + response.data.value.oid,
      );
      return;
    }
    await apply(response.data.value);
    if (draftOid && draftXml) {
      response = await post("/version/save-draft", {
        oid: draftOid,
        bpmnXml: draftXml,
        taskForms: draftTaskForms,
        taskPolicies: draftTaskPolicies,
        assignmentRules: draftAssignmentRules,
        startPolicies: draftStartPolicies,
      });
      if (showResponse(response)) await apply(response.data.value);
    }
  } catch (error: any) {
    toast.error(error?.message || "儲存流程失敗");
  } finally {
    hideLoading();
  }
};
const createVersion = async () => {
  showLoading();
  try {
    const response = await post("/version/create", { oid: form.value.oid });
    if (showResponse(response)) await apply(response.data.value);
  } finally {
    hideLoading();
  }
};
const publish = async () => {
  if (!selectedVersion.value || selectedVersion.value.versionStatus !== "DRAFT")
    return;
  if (!validateSequenceFlows()) return;
  showLoading();
  try {
    const xml = (await modeler.saveXML({ format: true })).xml;
    let response = await post("/version/save-draft", {
      oid: selectedVersion.value.oid,
      bpmnXml: xml,
      taskForms: currentTaskForms(),
      taskPolicies: currentTaskPolicies(),
      assignmentRules: currentAssignmentRules(),
      startPolicies: selectedVersion.value.startPolicies || [],
    });
    if (!responseOk(response)) {
      showResponse(response);
      return;
    }
    response = await post("/version/publish", {
      oid: selectedVersion.value.oid,
    });
    if (showResponse(response)) await apply(response.data.value);
  } finally {
    hideLoading();
  }
};
const previewResolvers = async () => {
  if (!selectedVersion.value || !previewAccount.value.trim()) {
    toast.warning("請輸入測試申請人的帳號");
    return;
  }
  let formData: Record<string, unknown>;
  try {
    formData = JSON.parse(previewFormData.value || "{}");
  } catch {
    toast.warning("測試表單資料不是有效的 JSON");
    return;
  }
  showLoading();
  try {
    const response = await post("/resolver-preview", {
      versionOid: selectedVersion.value.oid,
      initiatorAccount: previewAccount.value.trim(),
      variables: { form: formData },
    });
    if (!responseOk(response)) {
      showResponse(response);
      return;
    }
    resolverPreview.value = response.data?.value || [];
  } finally {
    hideLoading();
  }
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await post("/deactivate", { oid: form.value.oid });
    if (showResponse(response)) await apply(response.data.value);
  } finally {
    hideLoading();
  }
};
onMounted(async () => {
  tenants.value = (await post("/tenant-options")).data?.value || [];
  if (!props.edit && tenants.value.length === 1)
    form.value.tenantId = tenants.value[0].value;
  if (!props.edit) await loadResolverOptions();
  await load();
});
onBeforeUnmount(() => modeler?.destroy());
</script>

<template>
  <Toolbar
    :progId="props.edit ? PageConstants.EditId : PageConstants.CreateId"
    :description="
      props.edit
        ? '編輯流程基本資料與 BPMN 版本。流程代碼與 Tenant 建立後固定；只有草稿版本可以修改。發布會先驗證 BPMN 並部署至 Flowable，發布成功後該版本即鎖定，後續異動必須建立新版本。'
        : '建立流程穩定主檔。儲存後系統會自動建立第 1 版 BPMN 草稿，再進入編輯頁使用視覺化設計器調整、驗證及發布。流程代碼建立後不可修改。'
    "
    refreshFlag="Y"
    backFlag="Y"
    saveFlag="Y"
    @refreshMethod="props.edit ? load() : clear()"
    @backMethod="router.back()"
    @saveMethod="save"
  />
  <div class="card">
    <div class="card-body">
      <div class="alert alert-info">
        草稿可反覆儲存；「發布草稿」會先保存畫布、由 Flowable 驗證
        XML，再建立正式部署。請點選 UserTask 設定顯示表單及 Task
        Policy；點選 Gateway 出線可設定表單欄位分流條件與 Default Flow。一個 UserTask 只能選一張同 Tenant
        已發布的表單。已發布版本只能檢視，請按「建立新版本」複製最新版後再修改。
      </div>
      <div class="row g-3">
        <div class="col-md-3">
          <label class="form-label">Tenant</label
          ><select
            v-model="form.tenantId"
            :disabled="props.edit"
            :class="[
              'form-select',
              checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇 Tenant</option>
            <option
              v-for="item in tenants"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>
          <div class="invalid-feedback">
            {{ invalidFeedback("tenantId", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">流程代碼</label
          ><input
            v-model="form.processKey"
            :disabled="props.edit"
            :class="[
              'form-control',
              checkInvalid('processKey', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("processKey", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">流程名稱</label
          ><input
            v-model="form.processName"
            :class="[
              'form-control',
              checkInvalid('processName', checkFields) ? 'is-invalid' : '',
            ]"
          />
          <div class="invalid-feedback">
            {{ invalidFeedback("processName", checkFields) }}
          </div>
        </div>
        <div class="col-md-3">
          <label class="form-label">分類</label
          ><input v-model="form.category" class="form-control" />
        </div>
        <div class="col-md-2">
          <label class="form-label">狀態</label
          ><input :value="form.status" disabled class="form-control" />
        </div>
        <div class="col-md-10">
          <label class="form-label">說明</label
          ><input
            v-model="form.description"
            maxlength="500"
            class="form-control"
          />
        </div>
        <div class="col-12 d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="save">
            <i class="bi bi-save"></i> 儲存</button
          ><button
            v-if="!props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="clear"
          >
            <i class="bi bi-eraser"></i> 清除</button
          ><button
            v-if="props.edit"
            type="button"
            class="btn btn-outline-secondary"
            @click="load"
          >
            <i class="bi bi-repeat"></i> 重新載入</button
          ><button
            v-if="props.edit && form.status !== 'INACTIVE'"
            type="button"
            class="btn btn-outline-danger"
            @click="confirmFire('確定停用此流程？', deactivate, form.oid)"
          >
            <i class="bi bi-slash-circle"></i> 停用
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="props.edit" class="card mt-4">
    <div class="card-header d-flex justify-content-between align-items-center">
      <span>BPMN 流程版本</span
      ><button
        v-if="
          !form.versions?.some((item: any) => item.versionStatus === 'DRAFT')
        "
        type="button"
        class="btn btn-sm btn-outline-primary"
        @click="createVersion"
      >
        <i class="bi bi-plus-circle"></i> 建立新版本
      </button>
    </div>
    <div class="card-body">
      <div class="d-flex flex-wrap gap-2 mb-3">
        <button
          v-for="version in form.versions"
          :key="version.oid"
          type="button"
          :class="[
            'btn btn-sm',
            selectedVersion?.oid === version.oid
              ? 'btn-primary'
              : 'btn-outline-secondary',
          ]"
          @click="openVersion(version)"
        >
          v{{ version.versionNo }}・{{ version.versionStatus }}
        </button>
      </div>
      <div class="row g-3">
        <div class="col-lg-9">
          <div class="position-relative border rounded">
            <div ref="canvas" class="bpmn-canvas"></div>
            <div
              v-if="selectedVersion?.versionStatus !== 'DRAFT'"
              class="readonly-cover"
            >
              <span class="badge text-bg-secondary">已發布版本：僅供檢視</span>
            </div>
          </div>
        </div>
        <div class="col-lg-3">
          <div class="card h-100 task-property-panel">
            <div class="card-header">
              {{ selectedFlow ? "流程條件" : "UserTask 節點屬性" }}
            </div>
            <div class="card-body">
              <SequenceFlowConditionPanel
                v-if="selectedFlow"
                :key="selectedFlow.id"
                :element="selectedFlow"
                :modeler="modeler"
                :schema-content="conditionSchemaContent"
                :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
              />
              <div v-else-if="!selectedTask" class="text-muted">
                <template v-if="selectedElement">
                  目前選取「{{
                    selectedElement.businessObject?.$type ||
                    selectedElement.type ||
                    "未知節點"
                  }}」。請選取 UserTask 設定簽核人與表單，或選取
                  Sequence Flow 設定分流條件。
                </template>
                <template v-else>
                  請在左側流程圖點選 UserTask 或 Sequence Flow。
                </template>
              </div>
              <template v-else>
                <div class="mb-3">
                  <label class="form-label">節點代碼</label>
                  <input
                    :value="selectedTask.id"
                    disabled
                    class="form-control"
                  />
                </div>
                <div class="mb-3">
                  <label class="form-label">節點名稱</label>
                  <input
                    :value="selectedTask.businessObject?.name || '未命名節點'"
                    disabled
                    class="form-control"
                  />
                </div>
                <div class="mb-2">
                  <label class="form-label">顯示表單</label>
                  <select
                    :value="selectedFormValue"
                    :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                    class="form-select"
                    @change="changeSelectedForm"
                  >
                    <option value="">請選擇已發布表單</option>
                    <option
                      v-for="item in publishedForms"
                      :key="`${item.formId}-${item.formVersionNo}`"
                      :value="`${item.formId}::${item.formVersionNo}`"
                    >
                      {{ item.label }}
                    </option>
                  </select>
                </div>
                <div class="form-text">
                  此表單會在使用者處理本節點時顯示。改選會取代原表單，不會新增第二張表單。
                </div>
                <template v-if="selectedTaskPolicy">
                  <hr />
                  <div class="mb-3">
                    <label class="form-label">派送方式</label>
                    <select
                      v-model="selectedTaskPolicy.assignmentMode"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select"
                    >
                      <option value="ASSIGNEE">單一簽核人</option>
                      <option value="CANDIDATE">候選人承接</option>
                      <option value="ALL">全員會簽</option>
                      <option value="SEQUENTIAL">依序簽核</option>
                      <option value="APPLICANT_CORRECTION">申請人補件</option>
                    </select>
                  </div>
                  <div class="mb-3">
                    <label class="form-label">自簽政策</label>
                    <select
                      v-model="selectedTaskPolicy.selfApprovalPolicy"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select"
                    >
                      <option value="ALLOW">允許自簽</option>
                      <option value="SKIP_TO_NEXT">跳至下一位</option>
                      <option value="REQUIRE_ALTERNATE">必須找到替代人</option>
                      <option value="INCIDENT">建立指派異常</option>
                    </select>
                  </div>
                  <div class="mb-3">
                    <label class="form-label">重複簽核政策</label>
                    <select
                      v-model="selectedTaskPolicy.duplicatePolicy"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select"
                    >
                      <option value="KEEP_EACH_LEVEL">每一層都保留</option>
                      <option value="MERGE_CONSECUTIVE">合併連續重複</option>
                      <option value="SKIP_ALREADY_APPROVED">
                        略過已簽核人
                      </option>
                    </select>
                  </div>
                  <div class="mb-3">
                    <label class="form-label">意見必填</label>
                    <select
                      v-model="selectedTaskPolicy.commentRequired"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select"
                    >
                      <option value="NEVER">不要求</option>
                      <option value="ALWAYS">每次操作必填</option>
                      <option value="ON_REJECT_RETURN">駁回／退回時必填</option>
                    </select>
                  </div>
                  <div class="row g-2 mb-3">
                    <div class="col-6">
                      <label class="form-label">處理期限（小時）</label>
                      <input
                        v-model.number="selectedTaskPolicy.dueHours"
                        :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                        type="number"
                        min="1"
                        max="8760"
                        class="form-control"
                        placeholder="不設定期限"
                      />
                    </div>
                    <div class="col-6">
                      <label class="form-label">提前提醒（小時）</label>
                      <input
                        v-model.number="selectedTaskPolicy.reminderBeforeHours"
                        :disabled="selectedVersion?.versionStatus !== 'DRAFT' || !selectedTaskPolicy.dueHours"
                        type="number"
                        min="0"
                        :max="Math.max(0, Number(selectedTaskPolicy.dueHours || 1) - 1)"
                        class="form-control"
                        placeholder="只在逾時時提醒"
                      />
                    </div>
                    <div class="form-text">期限依 Task 建立時間以曆時小時計算；未設定期限時不執行提醒。</div>
                  </div>
                  <div class="row g-2">
                    <div
                      v-for="action in [
                        ['allowReject', '允許駁回'],
                        ['allowReturn', '允許退回'],
                        ['allowTransfer', '允許轉派'],
                        ['allowAddSign', '允許加簽'],
                      ]"
                      :key="action[0]"
                      class="col-6 form-check"
                    >
                      <input
                        :id="`${selectedTask.id}-${action[0]}`"
                        v-model="selectedTaskPolicy[action[0]]"
                        :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                        true-value="Y"
                        false-value="N"
                        type="checkbox"
                        class="form-check-input"
                      />
                      <label
                        :for="`${selectedTask.id}-${action[0]}`"
                        class="form-check-label"
                      >
                        {{ action[1] }}
                      </label>
                    </div>
                  </div>
                </template>
                <template v-if="selectedAssignmentRule && selectedTaskPolicy?.assignmentMode !== 'APPLICANT_CORRECTION'">
                  <hr />
                  <h6>簽核人規則</h6>
                  <div class="mb-3">
                    <label class="form-label">解析方式</label>
                    <select v-model="selectedAssignmentRule.resolverType"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'" class="form-select">
                      <option value="DIRECT_MANAGER">申請人的直屬主管</option>
                      <option value="INITIATOR_ORG_HEAD">申請人所屬單位主管</option>
                      <option value="PARENT_ORG_HEAD">上一層單位主管</option>
                      <option value="NEXT_HIGHER_LEVEL_HEAD">下一個較高層級主管</option>
                      <option value="ROOT_ORG_HEAD">最高層單位主管</option>
                      <option value="MANAGER_CHAIN">逐級直屬主管</option>
                      <option value="LEVEL_HEAD_CHAIN">逐級單位主管</option>
                      <option value="FIXED_ACCOUNT">指定帳號</option>
                      <option value="APPROVAL_GROUP">簽核群組</option>
                      <option value="TARGET_LEVEL_HEAD">指定層級主管</option>
                      <option value="ORG_TITLE">組織職稱</option>
                      <option value="ORG_DUTY">組織職務</option>
                      <option value="APPROVAL_AUTHORITY">簽核權限</option>
                    </select>
                  </div>
                  <div v-if="selectedAssignmentRule.resolverType === 'FIXED_ACCOUNT'"
                    class="mb-3">
                    <label class="form-label">指定帳號</label>
                    <select v-model="selectedFixedAccounts" multiple
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select" size="6">
                      <option v-for="item in resolverAccounts" :key="item.value"
                        :value="item.value">{{ item.label }}</option>
                    </select>
                    <div class="form-text">按住 Ctrl 可選擇多個帳號。</div>
                  </div>
                  <div v-else-if="selectedAssignmentRule.resolverType === 'APPROVAL_GROUP'"
                    class="mb-3">
                    <label class="form-label">簽核群組</label>
                    <select v-model="selectedApprovalGroup"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select">
                      <option value="">請選擇簽核群組</option>
                      <option v-for="item in approvalGroups" :key="item.value"
                        :value="item.value">{{ item.label }}</option>
                    </select>
                  </div>
                  <div v-else-if="selectedAssignmentRule.resolverType === 'TARGET_LEVEL_HEAD'"
                    class="mb-3">
                    <label class="form-label">目標簽核層級</label>
                    <select v-model="selectedApprovalLevel"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select">
                      <option value="">請選擇簽核層級</option>
                      <option v-for="item in approvalLevels" :key="item.value"
                        :value="item.value">{{ item.label }}</option>
                    </select>
                  </div>
                  <div v-else-if="selectedAssignmentRule.resolverType === 'ORG_TITLE'"
                    class="mb-3">
                    <label class="form-label">組織職稱</label>
                    <select v-model="selectedOrgTitle"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select">
                      <option value="">請選擇職稱</option>
                      <option v-for="item in orgTitles" :key="item.value"
                        :value="item.value">{{ item.label }}</option>
                    </select>
                    <div class="form-text">解析申請人主要部門中具有此職稱的有效任職者。</div>
                  </div>
                  <div v-else-if="selectedAssignmentRule.resolverType === 'ORG_DUTY'"
                    class="mb-3">
                    <label class="form-label">組織職務</label>
                    <select v-model="selectedOrgDuty"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      class="form-select">
                      <option value="">請選擇職務</option>
                      <option v-for="item in orgDuties" :key="item.value"
                        :value="item.value">{{ item.label }}</option>
                    </select>
                    <div v-if="!orgDuties.length" class="form-text text-warning">
                      此 Tenant 尚未建立組織職務資料。
                    </div>
                  </div>
                  <div v-else-if="selectedAssignmentRule.resolverType === 'APPROVAL_AUTHORITY'"
                    class="mb-3">
                    <ApprovalAuthorityPanel v-model="selectedApprovalAuthority"
                      :tenant-id="form.tenantId" :process-def-id="form.processDefId"
                      :form-id="selectedTaskRule?.formId"
                      :schema-content="selectedPublishedForm?.schemaContent"
                      :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                      :accounts="resolverAccounts" :groups="approvalGroups"
                      :levels="approvalLevels" :titles="orgTitles" :duties="orgDuties" />
                  </div>
                  <div class="row g-2">
                    <div class="col-6">
                      <label class="form-label">最多結果數</label>
                      <input v-model.number="selectedAssignmentRule.maxResults" type="number"
                        min="1" max="1000" :disabled="selectedVersion?.versionStatus !== 'DRAFT'"
                        class="form-control" />
                    </div>
                    <div class="col-6">
                      <label class="form-label">狀態</label>
                      <select v-model="selectedAssignmentRule.status"
                        :disabled="selectedVersion?.versionStatus !== 'DRAFT'" class="form-select">
                        <option value="ACTIVE">啟用</option><option value="INACTIVE">停用</option>
                      </select>
                    </div>
                  </div>
                </template>
                <div
                  v-if="!publishedForms.length"
                  class="alert alert-warning mt-3 mb-0"
                >
                  此 Tenant 尚無已發布表單，請先至 FM_PROG005D0001 建立並發布
                  Form。
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div v-if="selectedVersion" class="card mt-3">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>流程啟動規則</span>
          <button v-if="selectedVersion.versionStatus === 'DRAFT'" type="button"
            class="btn btn-sm btn-outline-primary" @click="addStartPolicy">
            <i class="bi bi-plus-circle"></i> 新增規則
          </button>
        </div>
        <div class="card-body">
          <div class="form-text mb-2">
            至少需要一筆「允許」規則；拒絕規則優先於允許規則。
          </div>
          <div v-if="!selectedVersion.startPolicies?.length" class="alert alert-warning mb-0">
            尚未設定啟動規則，此版本無法發佈。
          </div>
          <div v-else class="table-responsive">
            <table class="table table-sm align-middle mb-0">
              <thead><tr><th>#</th><th>對象類型</th><th>對象</th><th>權限</th><th></th></tr></thead>
              <tbody>
                <tr v-for="(policy, index) in selectedVersion.startPolicies"
                  :key="`${policy.policySeq}-${index}`">
                  <td>{{ index + 1 }}</td>
                  <td>
                    <select v-model="policy.subjectType" class="form-select form-select-sm"
                      :disabled="selectedVersion.versionStatus !== 'DRAFT'"
                      @change="changeStartPolicyType(policy)">
                      <option value="ALL">全部使用者</option>
                      <option value="ACCOUNT">指定帳號</option>
                      <option value="APPROVAL_GROUP">簽核群組</option>
                      <option value="ORG_UNIT">組織單位 ID</option>
                    </select>
                  </td>
                  <td>
                    <span v-if="policy.subjectType === 'ALL'" class="text-muted">不需指定</span>
                    <select v-else-if="startPolicyOptions(policy.subjectType).length"
                      v-model="policy.subjectRefId" class="form-select form-select-sm"
                      :disabled="selectedVersion.versionStatus !== 'DRAFT'">
                      <option value="">請選擇</option>
                      <option v-for="option in startPolicyOptions(policy.subjectType)"
                        :key="option.value" :value="option.value">{{ option.label }}</option>
                    </select>
                    <input v-else v-model.trim="policy.subjectRefId"
                      class="form-control form-control-sm" placeholder="請輸入組織單位 ID"
                      :disabled="selectedVersion.versionStatus !== 'DRAFT'" />
                  </td>
                  <td>
                    <select v-model="policy.allowStart" class="form-select form-select-sm"
                      :disabled="selectedVersion.versionStatus !== 'DRAFT'">
                      <option value="Y">允許</option>
                      <option value="N">拒絕</option>
                    </select>
                  </td>
                  <td class="text-end">
                    <button v-if="selectedVersion.versionStatus === 'DRAFT'" type="button"
                      class="btn btn-sm btn-outline-danger" @click="removeStartPolicy(index)">
                      刪除
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div v-if="selectedVersion" class="mt-3 d-flex gap-2 align-items-center">
        <button
          v-if="selectedVersion.versionStatus === 'DRAFT'"
          type="button"
          class="btn btn-primary"
          @click="save"
        >
          <i class="bi bi-save"></i> 儲存草稿</button
        ><button
          v-if="selectedVersion.versionStatus === 'DRAFT'"
          type="button"
          class="btn btn-success"
          @click="
            confirmFire(
              '發布後此版本不可再修改，確定發布？',
              publish,
              selectedVersion.oid,
            )
          "
        >
          <i class="bi bi-cloud-upload"></i> 發布草稿</button
        ><small class="text-muted"
          >SHA-256：{{ selectedVersion.bpmnSha256 }}</small
        >
      </div>
      <div v-if="selectedVersion" class="card mt-3">
        <div class="card-header">簽核人解析預覽</div>
        <div class="card-body">
          <div class="input-group mb-3">
            <span class="input-group-text">測試申請人帳號</span>
            <input v-model="previewAccount" class="form-control"
              placeholder="例如 tester" @keyup.enter="previewResolvers" />
            <button type="button" class="btn btn-outline-primary"
              @click="previewResolvers">開始預覽</button>
          </div>
          <div class="mb-3">
            <label class="form-label">測試表單資料</label>
            <textarea v-model="previewFormData" rows="3" class="form-control"
              placeholder='例如 {"totalAmount": 100000, "category": "資訊設備"}'></textarea>
          </div>
          <div class="form-text mb-3">預覽使用已儲存的簽核人規則；修改規則後請先儲存草稿。</div>
          <div v-if="resolverPreview.length" class="table-responsive">
            <table class="table table-sm align-middle mb-0">
              <thead><tr><th>節點</th><th>Resolver</th><th>結果</th><th>簽核人</th><th>說明</th></tr></thead>
              <tbody>
                <tr v-for="item in resolverPreview"
                  :key="`${item.taskDefKey}-${item.ruleSeq}`">
                  <td>{{ item.taskDefKey }}</td>
                  <td>{{ item.resolverType }}</td>
                  <td><span class="badge"
                    :class="item.resultStatus === 'RESOLVED' ? 'text-bg-success' : 'text-bg-warning'">
                    {{ item.resultStatus }}</span></td>
                  <td>{{ (item.candidates || []).map((candidate: any) =>
                    `${candidate.displayName} (${candidate.account})`).join('、') || '—' }}</td>
                  <td>{{ item.message }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.bpmn-canvas {
  height: 620px;
  background: #fff;
}
.readonly-cover {
  position: absolute;
  inset: 0;
  background: rgba(248, 249, 250, 0.08);
  pointer-events: all;
}
.readonly-cover .badge {
  position: absolute;
  right: 1rem;
  top: 1rem;
}
.task-property-panel {
  min-height: 620px;
}
</style>
