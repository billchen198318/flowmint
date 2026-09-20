import { validateGroovyBinding, type GroovyBinding } from "./groovyContract";

export const MAX_PROCESS_BUNDLE_BYTES = 16 * 1024 * 1024;
const MAX_XML_BYTES = 2 * 1024 * 1024;
const encoder = new TextEncoder();
type Rule = Record<string, string | number | null>;

export interface ProcessVersionBundle {
  kind: "flowmint-process-version";
  bundleVersion: 1;
  tenantId: string;
  processKey: string;
  sourceVersionNo: number;
  bpmnXml: string;
  taskForms: Rule[];
  taskPolicies: Rule[];
  assignmentRules: Rule[];
  startPolicies: Rule[];
  groovyBindings: GroovyBinding[];
}

const ruleFields = {
  taskForms: ["taskDefKey", "formId", "formVersionNo", "fieldPolicy"],
  taskPolicies: [
    "taskDefKey",
    "taskName",
    "assignmentMode",
    "selfApprovalPolicy",
    "duplicatePolicy",
    "allowReject",
    "allowReturn",
    "allowTransfer",
    "allowAddSign",
    "allowParallelAddSign",
    "parallelAddSignMaxMembers",
    "parallelAddSignCommentRequired",
    "commentRequired",
    "dueHours",
    "reminderBeforeHours",
  ],
  assignmentRules: [
    "taskDefKey",
    "ruleSeq",
    "resolverType",
    "resolverConfig",
    "fallbackConfig",
    "maxResults",
    "status",
  ],
  startPolicies: ["policySeq", "subjectType", "subjectRefId", "allowStart"],
} as const;
const bindingFields = [
  "nodeId",
  "bindingId",
  "scriptContent",
  "inputSchema",
  "outputSchema",
  "mappingContent",
  "timeoutMs",
];
const envelopeFields = [
  "kind",
  "bundleVersion",
  "tenantId",
  "processKey",
  "sourceVersionNo",
  "bpmnXml",
  ...Object.keys(ruleFields),
  "groovyBindings",
];
const object = (value: unknown): value is Record<string, any> =>
  !!value && typeof value === "object" && !Array.isArray(value);
const onlyFields = (value: Record<string, any>, fields: readonly string[]) =>
  Object.keys(value).every((key) => fields.includes(key));

/** Explicit projection excludes database IDs, deployment IDs, hashes and lock versions. */
export function createProcessVersionBundle(
  tenantId: string,
  processKey: string,
  version: Record<string, any>,
): ProcessVersionBundle {
  const result: Record<string, any> = {
    kind: "flowmint-process-version",
    bundleVersion: 1,
    tenantId,
    processKey,
    sourceVersionNo: version.versionNo,
    bpmnXml: version.bpmnXml,
    groovyBindings: (version.groovyBindings || []).map(
      (binding: GroovyBinding) =>
        Object.fromEntries(
          bindingFields.map((key) => [
            key,
            binding[key as keyof GroovyBinding],
          ]),
        ),
    ),
  };
  for (const [key, fields] of Object.entries(ruleFields)) {
    result[key] = (version[key] || []).map((rule: Rule) =>
      Object.fromEntries(fields.map((field) => [field, rule[field] ?? null])),
    );
  }
  return parseProcessVersionBundle(
    JSON.stringify(result),
    tenantId,
    processKey,
  );
}

export function serializeProcessVersionBundle(
  bundle: ProcessVersionBundle,
): string {
  const content = JSON.stringify(bundle, null, 2) + "\n";
  if (encoder.encode(content).length > MAX_PROCESS_BUNDLE_BYTES)
    throw new Error("流程版本檔最多 16 MiB");
  return content;
}

export function parseProcessVersionBundle(
  content: string,
  tenantId: string,
  processKey: string,
): ProcessVersionBundle {
  if (encoder.encode(content).length > MAX_PROCESS_BUNDLE_BYTES)
    throw new Error("流程版本檔最多 16 MiB");
  let value: any;
  try {
    value = JSON.parse(content);
  } catch {
    throw new Error("請選擇完整流程版本 JSON 檔，不能只匯入 BPMN XML");
  }
  if (
    !object(value) ||
    !onlyFields(value, envelopeFields) ||
    value.kind !== "flowmint-process-version" ||
    value.bundleVersion !== 1
  )
    throw new Error("不支援的流程版本檔格式");
  if (value.tenantId !== tenantId || value.processKey !== processKey)
    throw new Error("只能匯入同一 Tenant、同一流程代碼的版本檔");
  if (
    !Number.isSafeInteger(value.sourceVersionNo) ||
    value.sourceVersionNo < 1 ||
    typeof value.bpmnXml !== "string" ||
    !value.bpmnXml.trim() ||
    encoder.encode(value.bpmnXml).length > MAX_XML_BYTES ||
    /<!DOCTYPE|<!ENTITY/i.test(value.bpmnXml)
  )
    throw new Error("流程版本號或 BPMN XML 無效（XML 最多 2 MiB，不接受 DTD）");
  for (const [key, fields] of Object.entries(ruleFields)) {
    if (
      !Array.isArray(value[key]) ||
      value[key].length > 2000 ||
      value[key].some(
        (rule: unknown) =>
          !object(rule) ||
          !onlyFields(rule, fields) ||
          Object.values(rule).some(
            (field) =>
              field !== null &&
              typeof field !== "string" &&
              !(typeof field === "number" && Number.isFinite(field)),
          ),
      )
    )
      throw new Error(`${key} 配置格式無效或超過上限`);
  }
  if (!Array.isArray(value.groovyBindings) || value.groovyBindings.length > 100)
    throw new Error("Groovy 腳本配置最多 100 筆");
  for (const binding of value.groovyBindings) {
    if (
      !object(binding) ||
      !onlyFields(binding, bindingFields) ||
      bindingFields.some(
        (field) => field !== "timeoutMs" && typeof binding[field] !== "string",
      ) ||
      !/^[A-Za-z][A-Za-z0-9_-]{0,99}$/.test(binding.nodeId) ||
      !/^[A-Za-z][A-Za-z0-9_-]{0,99}$/.test(binding.bindingId) ||
      ["inputSchema", "outputSchema", "mappingContent"].some(
        (field) => encoder.encode(binding[field]).length > 256 * 1024,
      )
    )
      throw new Error("Groovy 腳本配置格式或大小無效");
    const errors = validateGroovyBinding(binding as GroovyBinding);
    if (errors.length) throw new Error(errors.join("；"));
  }
  return value as ProcessVersionBundle;
}

/** Fully prepare an independent canvas before the caller replaces the active modeler. */
export async function stageProcessBundleModeler(
  bundle: ProcessVersionBundle,
  createModeler: () => any,
): Promise<any> {
  const staged = createModeler();
  try {
    const imported = await staged.importXML(bundle.bpmnXml);
    if (imported.warnings?.length)
      throw new Error("BPMN 圖形不完整，請修正後再匯入");
    for (const binding of bundle.groovyBindings) {
      const element = staged.get("elementRegistry").get(binding.nodeId);
      if (!element || element.businessObject?.taskType !== "GROOVY"
          || element.businessObject.bindingId !== binding.bindingId)
        throw new Error("Groovy 節點缺少可編輯圖形或腳本引用不符");
      element.businessObject.$groovyBinding = structuredClone(binding);
    }
    return staged;
  } catch (error) {
    staged.destroy();
    throw error;
  }
}

/** Parse off-screen first: malformed files must never clear the currently edited diagram. */
export async function validateProcessBundleDiagram(
  bundle: ProcessVersionBundle,
  moddle: any,
  groovyEnabled: boolean,
): Promise<void> {
  if (bundle.groovyBindings.length && !groovyEnabled)
    throw new Error("Groovy 草稿功能尚未啟用");
  let parsed: any;
  try {
    parsed = await moddle.fromXML(bundle.bpmnXml);
  } catch {
    throw new Error("BPMN XML 無法解析");
  }
  if (parsed.warnings?.length)
    throw new Error("BPMN XML 含未知屬性或無效引用，請修正後再匯入");
  const roots = parsed.rootElement?.rootElements || [];
  if (
    roots.length !== 1 ||
    roots[0].$type !== "bpmn:Process" ||
    roots[0].id !== bundle.processKey
  )
    throw new Error("BPMN 必須包含與目前流程代碼相同的單一 Process");
  const userTasks = new Set<string>();
  const groovyNodes = new Map<string, string>();
  const visit = (elements: any[]) => {
    for (const element of elements || []) {
      if (element.$type === "bpmn:UserTask") userTasks.add(element.id);
      if (element.$type === "bpmn:ServiceTask" && element.taskType === "GROOVY")
        groovyNodes.set(element.id, element.bindingId);
      visit(element.flowElements);
    }
  };
  visit(roots[0].flowElements);
  const nodes = new Set<string>();
  const bindings = new Set<string>();
  for (const binding of bundle.groovyBindings) {
    if (
      groovyNodes.get(binding.nodeId) !== binding.bindingId ||
      nodes.has(binding.nodeId) ||
      bindings.has(binding.bindingId)
    )
      throw new Error("Groovy 節點與腳本 binding 不一致或重複");
    nodes.add(binding.nodeId);
    bindings.add(binding.bindingId);
  }
  if (nodes.size !== groovyNodes.size)
    throw new Error("流程版本檔缺少 Groovy 腳本，無法還原");
  for (const key of ["taskForms", "taskPolicies", "assignmentRules"] as const) {
    const seen = new Set<string>();
    for (const rule of bundle[key]) {
      const identity =
        key === "assignmentRules"
          ? `${rule.taskDefKey}:${rule.ruleSeq}`
          : String(rule.taskDefKey);
      if (
        typeof rule.taskDefKey !== "string" ||
        !userTasks.has(rule.taskDefKey) ||
        seen.has(identity)
      )
        throw new Error(`${key} 引用不存在的 User Task 或重複配置`);
      seen.add(identity);
    }
  }
}
