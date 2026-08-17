<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { is } from "bpmn-js/lib/util/ModelUtil";
import { toast } from "vue3-toastify";

type FieldOption = {
  key: string;
  label: string;
  type: string;
  values: Array<{ label: string; value: unknown }>;
};
type ConditionRow = { field: string; operator: string; value: unknown };

const props = defineProps<{
  element: any;
  modeler: any;
  schemaContent?: string;
  disabled?: boolean;
}>();

const match = ref<"ALL" | "ANY">("ALL");
const conditions = ref<ConditionRow[]>([]);
const flowName = ref("");
const defaultFlow = ref(false);
const rawExpression = ref("");
const structured = ref(true);
const isConditionalGateway = (element: any) =>
  is(element, "bpmn:ExclusiveGateway") || is(element, "bpmn:InclusiveGateway");
const endpointLabel = (side: "source" | "target") => {
  const diagramElement = props.element?.[side];
  const businessObject =
    diagramElement?.businessObject ||
    props.element?.businessObject?.[`${side}Ref`];
  return (
    businessObject?.name?.trim?.() ||
    businessObject?.id ||
    diagramElement?.id ||
    (side === "source" ? "未識別起點" : "未識別終點")
  );
};

const collectFields = (
  components: any[] = [],
  result: FieldOption[] = [],
  insideGrid = false,
) => {
  for (const component of components) {
    const nestedGrid = insideGrid || component?.type === "datagrid";
    if (component?.key && component?.input !== false && !insideGrid) {
      result.push({
        key: component.key,
        label: component.label || component.key,
        type: component.type || "textfield",
        values: Array.isArray(component.data?.values)
          ? component.data.values
          : [],
      });
    }
    collectFields(component?.components, result, nestedGrid);
    for (const column of Array.isArray(component?.columns)
      ? component.columns
      : []) {
      collectFields(column?.components, result, nestedGrid);
    }
    for (const row of Array.isArray(component?.rows) ? component.rows : []) {
      for (const cell of Array.isArray(row) ? row : []) {
        collectFields(cell?.components, result, nestedGrid);
      }
    }
  }
  return result;
};
const fields = computed(() => {
  try {
    const schema = JSON.parse(props.schemaContent || "{}");
    const unique = new Map<string, FieldOption>();
    for (const field of collectFields(schema.components || [])) {
      if (!unique.has(field.key)) unique.set(field.key, field);
    }
    return [...unique.values()];
  } catch {
    return [];
  }
});
const field = (key: string) => fields.value.find((item) => item.key === key);
const operators = (key: string) => {
  const type = field(key)?.type;
  if (type === "number")
    return ["EQ", "NE", "GT", "GTE", "LT", "LTE"];
  if (type === "checkbox") return ["EQ", "NE"];
  return ["EQ", "NE"];
};
const operatorLabel = (operator: string) =>
  ({ EQ: "等於", NE: "不等於", GT: "大於", GTE: "大於等於", LT: "小於", LTE: "小於等於" })[
    operator
  ] || operator;
const symbol = (operator: string) =>
  ({ EQ: "==", NE: "!=", GT: ">", GTE: ">=", LT: "<", LTE: "<=" })[
    operator
  ] || "==";
const parseValue = (value: string) => {
  const text = value.trim();
  if (text === "true") return true;
  if (text === "false") return false;
  if (/^-?\d+(?:\.\d+)?$/.test(text)) return Number(text);
  if (text.startsWith('"') && text.endsWith('"')) {
    try {
      return JSON.parse(text);
    } catch {
      return text.slice(1, -1);
    }
  }
  return text;
};
const parseExpression = (expression: string) => {
  const content = expression.trim().replace(/^\$\{/, "").replace(/\}$/, "");
  const hasAnd = content.includes(" && ");
  const hasOr = content.includes(" || ");
  if (hasAnd && hasOr) return false;
  const separator = hasOr ? " || " : " && ";
  const parts = content.split(separator);
  const parsed: ConditionRow[] = [];
  for (const part of parts) {
    const found = part
      .trim()
      .match(/^flowmintFormData\.([A-Za-z][A-Za-z0-9_]*)\s*(==|!=|>=|<=|>|<)\s*(.+)$/);
    if (!found) return false;
    const operator =
      ({ "==": "EQ", "!=": "NE", ">": "GT", ">=": "GTE", "<": "LT", "<=": "LTE" })[
        found[2]
      ] || "EQ";
    parsed.push({ field: found[1], operator, value: parseValue(found[3]) });
  }
  match.value = hasOr ? "ANY" : "ALL";
  conditions.value = parsed;
  return true;
};
const load = () => {
  const businessObject = props.element?.businessObject;
  flowName.value = businessObject?.name || "";
  defaultFlow.value = businessObject?.sourceRef?.default?.id === businessObject?.id;
  rawExpression.value = businessObject?.conditionExpression?.body || "";
  structured.value = !rawExpression.value || parseExpression(rawExpression.value);
  if (!conditions.value.length)
    conditions.value = [{ field: "", operator: "EQ", value: "" }];
};
watch(() => props.element?.id, load, { immediate: true });

const addCondition = () =>
  conditions.value.push({ field: "", operator: "EQ", value: "" });
const removeCondition = (index: number) => conditions.value.splice(index, 1);
const resetOperator = (row: ConditionRow) => {
  row.operator = "EQ";
  row.value = field(row.field)?.type === "checkbox" ? true : "";
};
const valueExpression = (row: ConditionRow) => {
  const type = field(row.field)?.type;
  if (type === "number") return String(Number(row.value));
  if (type === "checkbox") return String(row.value === true || row.value === "true");
  return JSON.stringify(row.value ?? "");
};
const expression = computed(() => {
  if (defaultFlow.value) return "";
  const separator = match.value === "ALL" ? " && " : " || ";
  return `\${${conditions.value
    .map(
      (row) =>
        `flowmintFormData.${row.field} ${symbol(row.operator)} ${valueExpression(row)}`,
    )
    .join(separator)}}`;
});
const conditionLabel = (row: ConditionRow) => {
  const option = field(row.field);
  const valueLabel = option?.values.find(
    (item) => String(item.value) === String(row.value),
  )?.label;
  const value = valueLabel || (typeof row.value === "boolean" ? (row.value ? "是" : "否") : row.value);
  return `${option?.label || row.field} ${operatorLabel(row.operator)} ${value}`;
};
const suggestedName = computed(() => {
  if (defaultFlow.value) return "其他／預設";
  const separator = match.value === "ALL" ? " 且 " : " 或 ";
  return conditions.value.map(conditionLabel).join(separator);
});
const apply = () => {
  if (props.disabled || !props.element || !props.modeler) return;
  const source = props.element.source;
  if (!isConditionalGateway(source)) {
    toast.warning("條件只能設定在 Gateway 的出線");
    return;
  }
  if (!defaultFlow.value) {
    if (!conditions.value.length || conditions.value.some((row) => !field(row.field))) {
      toast.warning("請完整選擇條件欄位");
      return;
    }
    if (
      conditions.value.some(
        (row) => field(row.field)?.type === "number" && !Number.isFinite(Number(row.value)),
      )
    ) {
      toast.warning("數字欄位的比較值必須是有效數字");
      return;
    }
  }
  const modeling = props.modeler.get("modeling");
  const moddle = props.modeler.get("moddle");
  const previousDefault = source.businessObject?.default;
  if (defaultFlow.value) {
    modeling.updateProperties(source, { default: props.element.businessObject });
  } else if (previousDefault?.id === props.element.id) {
    modeling.updateProperties(source, { default: undefined });
  }
  const name = flowName.value.trim() || suggestedName.value;
  modeling.updateProperties(props.element, {
    name,
    conditionExpression: defaultFlow.value
      ? undefined
      : moddle.create("bpmn:FormalExpression", { body: expression.value }),
  });
  flowName.value = name;
  rawExpression.value = expression.value;
  structured.value = true;
  toast.success("流程條件已套用，請儲存草稿");
};
</script>

<template>
  <div>
    <div class="mb-3">
      <label class="form-label">連線</label>
      <input
        :value="`${endpointLabel('source')} → ${endpointLabel('target')}`"
        disabled
        class="form-control"
      />
    </div>
    <div class="mb-3">
      <label class="form-label">線條顯示名稱</label>
      <input v-model="flowName" :disabled="disabled" class="form-control" />
      <div class="form-text">留空時依條件自動產生中文標籤。</div>
    </div>
    <div class="form-check mb-3">
      <input
        :id="`${element?.id}-default`"
        v-model="defaultFlow"
        :disabled="disabled"
        type="checkbox"
        class="form-check-input"
      />
      <label :for="`${element?.id}-default`" class="form-check-label">設為 Default Flow</label>
    </div>
    <div v-if="!isConditionalGateway(element?.source)" class="alert alert-warning">
      只有 Exclusive Gateway 或 Inclusive Gateway 的出線可以設定判斷條件。
    </div>
    <div v-else-if="!structured && !defaultFlow" class="alert alert-warning">
      既有條件不是受支援的結構化格式：
      <code class="d-block text-break mt-1">{{ rawExpression }}</code>
      <button type="button" class="btn btn-sm btn-outline-warning mt-2" @click="structured = true">
        改用結構化條件
      </button>
    </div>
    <template v-else-if="!defaultFlow">
      <div class="mb-3">
        <label class="form-label">條件組合</label>
        <select v-model="match" :disabled="disabled" class="form-select">
          <option value="ALL">全部符合（AND）</option>
          <option value="ANY">任一符合（OR）</option>
        </select>
      </div>
      <div v-for="(row, index) in conditions" :key="index" class="border rounded p-2 mb-2">
        <select
          v-model="row.field"
          :disabled="disabled"
          class="form-select mb-2"
          @change="resetOperator(row)"
        >
          <option value="">請選擇表單欄位</option>
          <option v-for="item in fields" :key="item.key" :value="item.key">
            {{ item.label }}（{{ item.key }}）
          </option>
        </select>
        <select v-model="row.operator" :disabled="disabled || !row.field" class="form-select mb-2">
          <option v-for="item in operators(row.field)" :key="item" :value="item">
            {{ operatorLabel(item) }}
          </option>
        </select>
        <select
          v-if="field(row.field)?.values.length"
          v-model="row.value"
          :disabled="disabled"
          class="form-select"
        >
          <option v-for="item in field(row.field)?.values" :key="String(item.value)" :value="item.value">
            {{ item.label }}
          </option>
        </select>
        <select
          v-else-if="field(row.field)?.type === 'checkbox'"
          v-model="row.value"
          :disabled="disabled"
          class="form-select"
        >
          <option :value="true">是</option>
          <option :value="false">否</option>
        </select>
        <input
          v-else
          v-model="row.value"
          :type="field(row.field)?.type === 'number' ? 'number' : 'text'"
          :disabled="disabled"
          class="form-control"
        />
        <button
          v-if="conditions.length > 1"
          type="button"
          :disabled="disabled"
          class="btn btn-sm btn-outline-danger mt-2"
          @click="removeCondition(index)"
        >
          移除此條件
        </button>
      </div>
      <button type="button" :disabled="disabled" class="btn btn-sm btn-outline-primary" @click="addCondition">
        新增條件
      </button>
      <div class="form-text text-break mt-2">{{ expression }}</div>
    </template>
    <button
      type="button"
      :disabled="disabled || !isConditionalGateway(element?.source)"
      class="btn btn-primary w-100 mt-3"
      @click="apply"
    >
      套用流程條件
    </button>
  </div>
</template>
