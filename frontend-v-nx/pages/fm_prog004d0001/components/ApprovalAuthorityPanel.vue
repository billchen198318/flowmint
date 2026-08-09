<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { toast } from "vue3-toastify";
import ApprovalAuthorityConditionEditor from "@/components/fm/ApprovalAuthorityConditionEditor.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { PageConstants } from "../config";

const props = defineProps<{
  tenantId: string;
  processDefId: string;
  formId?: string;
  schemaContent?: string;
  modelValue?: string;
  disabled?: boolean;
  accounts: any[];
  groups: any[];
  levels: any[];
  titles: any[];
  duties: any[];
}>();

const emit = defineEmits<{ "update:modelValue": [value: string] }>();
const authorities = ref<any[]>([]);
const editing = ref(false);
const saving = ref(false);
const collectFields = (components: any[], values: any[]) => {
  for (const component of components || []) {
    if (component?.key && component?.input !== false) {
      values.push({
        value: `form.${component.key}`,
        label: component.label || component.key,
      });
    }
    collectFields(component?.components, values);
    for (const column of component?.columns || []) collectFields(column?.components, values);
    for (const row of component?.rows || [])
      for (const cell of row || []) collectFields(cell?.components, values);
  }
};
const fields = computed(() => {
  try {
    const schema = JSON.parse(props.schemaContent || "{}");
    const values: any[] = [];
    collectFields(schema.components || [], values);
    return values;
  } catch {
    return [];
  }
});
const emptyRule = (sequence = 1) => ({
  ruleSeq: sequence,
  conditionConfig: JSON.stringify({
    match: "ALL",
    conditions: [{ field: "", operator: "EQ", value: "" }],
  }),
  targetType: "APPROVAL_LEVEL",
  targetRefId: "",
  resolverConfig: null,
  stopAfterApproval: "N",
  status: "ACTIVE",
});
const emptyForm = () => ({
  oid: "",
  tenantId: props.tenantId,
  authorityCode: "",
  authorityName: "",
  processDefId: props.processDefId,
  formId: props.formId || "",
  status: "ACTIVE",
  effectiveFrom: new Date().toISOString().slice(0, 10),
  effectiveTo: null,
  description: "",
  rules: [emptyRule()],
});
const form = ref<any>(emptyForm());
const post = (path: string, body: any = {}) =>
  getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + path,
    body,
  );
const isOk = (response: any) =>
  response.data?.success === import.meta.env.VITE_SUCCESS_FLAG;
const selected = computed({
  get: () => props.modelValue || "",
  set: (value: string) => emit("update:modelValue", value),
});
const condition = (rule: any) => {
  try {
    return JSON.parse(rule.conditionConfig);
  } catch {
    return { match: "ALL", conditions: [] };
  }
};
const setCondition = (rule: any, value: any) => {
  rule.conditionConfig = JSON.stringify(value);
};
const targetOptions = (type: string) => ({
  APPROVAL_LEVEL: props.levels,
  ORG_TITLE: props.titles,
  ORG_DUTY: props.duties,
  APPROVAL_GROUP: props.groups,
  FIXED_ACCOUNT: props.accounts,
}[type] || []);
const load = async () => {
  if (!props.tenantId || !props.processDefId) return;
  const response = await post("/approval-authority/list", {
    tenantId: props.tenantId,
    processDefId: props.processDefId,
  });
  authorities.value = response.data?.value || [];
};
const create = () => {
  form.value = emptyForm();
  editing.value = true;
};
const edit = () => {
  const value = authorities.value.find(
    (item) => item.approvalAuthorityId === selected.value,
  );
  if (!value) return;
  form.value = JSON.parse(JSON.stringify(value));
  editing.value = true;
};
const addRule = () => form.value.rules.push(emptyRule(form.value.rules.length + 1));
const removeRule = (index: number) => {
  form.value.rules.splice(index, 1);
  form.value.rules.forEach((rule: any, ruleIndex: number) => {
    rule.ruleSeq = ruleIndex + 1;
  });
};
const save = async () => {
  saving.value = true;
  try {
    const path = form.value.oid
      ? "/approval-authority/update"
      : "/approval-authority/save";
    const response = await post(path, form.value);
    if (!isOk(response)) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message || "儲存失敗"));
      return;
    }
    selected.value = response.data.value.approvalAuthorityId;
    editing.value = false;
    await load();
    toast.success(response.data?.message || "儲存成功");
  } finally {
    saving.value = false;
  }
};

watch(() => [props.tenantId, props.processDefId], load);
onMounted(load);
</script>

<template>
  <div>
    <label class="form-label">核決權限</label>
    <div class="input-group">
      <select v-model="selected" :disabled="disabled" class="form-select">
        <option value="">請選擇核決權限</option>
        <option v-for="item in authorities" :key="item.approvalAuthorityId"
          :value="item.approvalAuthorityId">
          {{ item.authorityCode }}－{{ item.authorityName }}
        </option>
      </select>
      <button type="button" :disabled="disabled" class="btn btn-outline-primary"
        @click="create">新增</button>
      <button type="button" :disabled="disabled || !selected"
        class="btn btn-outline-secondary" @click="edit">編輯</button>
    </div>

    <div v-if="editing" class="card mt-3">
      <div class="card-body">
        <div class="row g-2 mb-3">
          <div class="col-md-4">
            <label class="form-label">權限代碼</label>
            <input v-model.trim="form.authorityCode" class="form-control" />
          </div>
          <div class="col-md-5">
            <label class="form-label">權限名稱</label>
            <input v-model.trim="form.authorityName" class="form-control" />
          </div>
          <div class="col-md-3">
            <label class="form-label">狀態</label>
            <select v-model="form.status" class="form-select">
              <option value="ACTIVE">啟用</option>
              <option value="INACTIVE">停用</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label">生效日</label>
            <input v-model="form.effectiveFrom" type="date" class="form-control" />
          </div>
          <div class="col-md-4">
            <label class="form-label">失效日</label>
            <input v-model="form.effectiveTo" type="date" class="form-control" />
          </div>
          <div class="col-md-4">
            <label class="form-label">適用表單</label>
            <input v-model="form.formId" class="form-control" readonly />
          </div>
        </div>

        <div v-for="(rule, index) in form.rules" :key="index"
          class="border rounded p-3 mb-3">
          <div class="d-flex justify-content-between mb-2">
            <strong>規則 {{ index + 1 }}</strong>
            <button type="button" :disabled="form.rules.length === 1"
              class="btn btn-outline-danger btn-sm" @click="removeRule(index)">移除</button>
          </div>
          <ApprovalAuthorityConditionEditor :model-value="condition(rule)" :fields="fields"
            @update:model-value="setCondition(rule, $event)" />
          <div class="row g-2 mt-2">
            <div class="col-md-4">
              <label class="form-label">簽核目標類型</label>
              <select v-model="rule.targetType" class="form-select"
                @change="rule.targetRefId = ''">
                <option value="APPROVAL_LEVEL">簽核層級</option>
                <option value="ORG_TITLE">組織職稱</option>
                <option value="ORG_DUTY">組織職務</option>
                <option value="APPROVAL_GROUP">簽核群組</option>
                <option value="FIXED_ACCOUNT">指定帳號</option>
              </select>
            </div>
            <div class="col-md-5">
              <label class="form-label">簽核目標</label>
              <select v-model="rule.targetRefId" class="form-select">
                <option value="">請選擇</option>
                <option v-for="item in targetOptions(rule.targetType)" :key="item.value"
                  :value="item.value">{{ item.label }}</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label">符合後停止</label>
              <select v-model="rule.stopAfterApproval" class="form-select">
                <option value="N">否</option>
                <option value="Y">是</option>
              </select>
            </div>
          </div>
        </div>
        <button type="button" class="btn btn-outline-primary btn-sm" @click="addRule">
          新增規則
        </button>
        <div class="d-flex justify-content-end gap-2 mt-3">
          <button type="button" class="btn btn-outline-secondary"
            @click="editing = false">取消</button>
          <button type="button" :disabled="saving" class="btn btn-primary" @click="save">
            儲存核決權限
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
