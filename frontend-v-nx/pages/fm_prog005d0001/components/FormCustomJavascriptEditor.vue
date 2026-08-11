<script setup lang="ts">
import { ref } from "vue";
import { toast } from "vue3-toastify";
import { compileFormCustomJavascript } from "@/composables/useFormCustomJavascript";

const props = defineProps<{
  modelValue?: string;
  readonly?: boolean;
}>();
const emit = defineEmits<{ "update:modelValue": [value: string] }>();
const checking = ref(false);

const template = `return {
  async onFormLoad(ctx) {
    ctx.log("表單載入", ctx.formCode, ctx.versionNo);
  },

  async onFieldChange(ctx) {
    const key = ctx.changed?.component?.key;
    if (!key) return;
  },

  async beforeSubmit(ctx) {
    return true;
  },

  async afterSubmit(ctx) {
    ctx.log("送出完成", ctx.response);
  },

  async onDestroy(ctx) {
    ctx.log("表單釋放");
  },
};`;

const update = (event: Event) => {
  emit("update:modelValue", (event.target as HTMLTextAreaElement).value);
};

const insertTemplate = () => {
  if (props.readonly) return;
  if (props.modelValue?.trim() && !window.confirm("確定以生命週期範本覆蓋目前內容？")) return;
  emit("update:modelValue", template);
};

const validate = async () => {
  checking.value = true;
  try {
    await compileFormCustomJavascript(props.modelValue);
    toast.success("JavaScript 語法與生命週期格式正確");
  } catch (error) {
    toast.error(error instanceof Error ? error.message : "JavaScript 檢查失敗");
  } finally {
    checking.value = false;
  }
};
</script>

<template>
  <div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
      <div>
        <span class="fw-semibold">表單客製 JavaScript</span>
        <div class="small text-muted">
          支援 onFormLoad、onFieldChange、beforeSubmit、afterSubmit 與 onDestroy。
        </div>
      </div>
      <div class="d-flex gap-2">
        <button
          type="button"
          class="btn btn-sm btn-outline-secondary"
          :disabled="readonly"
          @click="insertTemplate"
        >
          插入範本
        </button>
        <button
          type="button"
          class="btn btn-sm btn-outline-primary"
          :disabled="checking"
          @click="validate"
        >
          {{ checking ? "檢查中…" : "檢查 JavaScript" }}
        </button>
      </div>
    </div>
    <div class="card-body">
      <textarea
        :value="modelValue || ''"
        :readonly="readonly"
        class="form-control script-editor"
        spellcheck="false"
        placeholder="return { async onFormLoad(ctx) { ... } };"
        @input="update"
      ></textarea>
      <div class="form-text">
        可使用 ctx.data、ctx.form、ctx.axios、ctx.executeDataAction()、ctx.setValue() 與
        ctx.redraw()。
      </div>
    </div>
  </div>
</template>

<style scoped>
.script-editor {
  min-height: 420px;
  font-family: Consolas, "Courier New", monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  tab-size: 2;
  white-space: pre;
}
</style>
