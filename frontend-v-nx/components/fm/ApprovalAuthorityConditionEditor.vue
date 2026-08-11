<script setup lang="ts">
import { computed } from "vue";

interface Option {
  value: string;
  label: string;
}

interface Condition {
  field: string;
  operator: string;
  value: unknown;
}

interface ConditionConfig {
  match: "ALL" | "ANY";
  conditions: Condition[];
}

const props = defineProps<{
  modelValue: ConditionConfig;
  fields: Option[];
  disabled?: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: ConditionConfig];
}>();

const operators: Option[] = [
  { value: "EQ", label: "等於" },
  { value: "NE", label: "不等於" },
  { value: "GT", label: "大於" },
  { value: "GTE", label: "大於或等於" },
  { value: "LT", label: "小於" },
  { value: "LTE", label: "小於或等於" },
  { value: "IN", label: "包含於清單" },
  { value: "NOT_IN", label: "不包含於清單" },
];

const config = computed(() => props.modelValue || { match: "ALL", conditions: [] });

const update = (value: ConditionConfig) => emit("update:modelValue", value);

const changeMatch = (event: Event) => {
  update({
    ...config.value,
    match: (event.target as HTMLSelectElement).value as "ALL" | "ANY",
  });
};

const updateCondition = (index: number, patch: Partial<Condition>) => {
  const conditions = config.value.conditions.map((condition, conditionIndex) =>
    conditionIndex === index ? { ...condition, ...patch } : condition,
  );
  update({ ...config.value, conditions });
};

const addCondition = () => {
  update({
    ...config.value,
    conditions: [
      ...config.value.conditions,
      { field: props.fields[0]?.value || "", operator: "EQ", value: "" },
    ],
  });
};

const removeCondition = (index: number) => {
  update({
    ...config.value,
    conditions: config.value.conditions.filter((_, conditionIndex) =>
      conditionIndex !== index),
  });
};

const parseValue = (condition: Condition, rawValue: string) => {
  if (["IN", "NOT_IN"].includes(condition.operator)) {
    return rawValue.split(",").map((value) => value.trim()).filter(Boolean);
  }
  if (rawValue !== "" && !Number.isNaN(Number(rawValue))) return Number(rawValue);
  if (rawValue === "true" || rawValue === "false") return rawValue === "true";
  return rawValue;
};

const displayValue = (condition: Condition) =>
  Array.isArray(condition.value) ? condition.value.join(", ") : String(condition.value ?? "");

const changeConditionField = (index: number, event: Event) =>
  updateCondition(index, { field: (event.target as HTMLSelectElement).value });

const changeConditionOperator = (index: number, event: Event) =>
  updateCondition(index, { operator: (event.target as HTMLSelectElement).value });

const changeConditionValue = (index: number, condition: Condition, event: Event) =>
  updateCondition(index, {
    value: parseValue(condition, (event.target as HTMLInputElement).value),
  });
</script>

<template>
  <div class="authority-condition-editor">
    <div class="d-flex align-items-center gap-2 mb-3">
      <label class="form-label mb-0">條件符合方式</label>
      <select :value="config.match" :disabled="disabled" class="form-select form-select-sm w-auto"
        @change="changeMatch">
        <option value="ALL">全部條件都符合</option>
        <option value="ANY">任一條件符合</option>
      </select>
    </div>

    <div v-for="(condition, index) in config.conditions" :key="index"
      class="row g-2 align-items-start mb-2">
      <div class="col-md-4">
        <select :value="condition.field" :disabled="disabled" class="form-select"
          @change="changeConditionField(index, $event)">
          <option value="">請選擇表單欄位</option>
          <option v-for="field in fields" :key="field.value" :value="field.value">
            {{ field.label }}
          </option>
        </select>
      </div>
      <div class="col-md-3">
        <select :value="condition.operator" :disabled="disabled" class="form-select"
          @change="changeConditionOperator(index, $event)">
          <option v-for="operator in operators" :key="operator.value" :value="operator.value">
            {{ operator.label }}
          </option>
        </select>
      </div>
      <div class="col-md-4">
        <input :value="displayValue(condition)" :disabled="disabled" class="form-control"
          :placeholder="['IN', 'NOT_IN'].includes(condition.operator) ? '多個值以逗號分隔' : '比較值'"
          @input="changeConditionValue(index, condition, $event)" />
      </div>
      <div class="col-md-1">
        <button type="button" :disabled="disabled" class="btn btn-outline-danger"
          title="移除條件" @click="removeCondition(index)">
          <i class="bi bi-trash"></i>
        </button>
      </div>
    </div>

    <button type="button" :disabled="disabled || !fields.length"
      class="btn btn-outline-primary btn-sm" @click="addCondition">
      <i class="bi bi-plus-lg"></i> 新增條件
    </button>
  </div>
</template>
