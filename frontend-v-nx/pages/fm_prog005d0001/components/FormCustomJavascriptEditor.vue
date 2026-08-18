<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import type { EditorView } from "@codemirror/view";
import { toast } from "vue3-toastify";
import { compileFormCustomJavascript } from "@/composables/useFormCustomJavascript";

const props = defineProps<{
  modelValue?: string;
  readonly?: boolean;
}>();
const emit = defineEmits<{ "update:modelValue": [value: string] }>();
const checking = ref(false);
const editorHost = ref<HTMLElement | null>(null);
let editor: EditorView | null = null;
let readonlyCompartment: import("@codemirror/state").Compartment | null = null;
let applyingExternalValue = false;

const readonlyExtensions = async (readonly = false) => {
  const [{ EditorState }, { EditorView }] = await Promise.all([
    import("@codemirror/state"),
    import("@codemirror/view"),
  ]);
  return [EditorState.readOnly.of(readonly), EditorView.editable.of(!readonly)];
};

const createEditor = async () => {
  if (!editorHost.value || editor) return;
  const [{ Compartment, EditorState }, view, javascript, theme] =
    await Promise.all([
      import("@codemirror/state"),
      import("@codemirror/view"),
      import("@codemirror/lang-javascript"),
      import("@codemirror/theme-one-dark"),
    ]);
  if (!editorHost.value || editor) return;
  readonlyCompartment = new Compartment();
  editor = new view.EditorView({
    parent: editorHost.value,
    state: EditorState.create({
      doc: props.modelValue || "",
      extensions: [
        view.lineNumbers(),
        view.highlightActiveLineGutter(),
        view.highlightActiveLine(),
        view.EditorView.lineWrapping,
        javascript.javascript(),
        theme.oneDark,
        readonlyCompartment.of(await readonlyExtensions(props.readonly)),
        view.EditorView.updateListener.of((update) => {
          if (!update.docChanged || applyingExternalValue) return;
          emit("update:modelValue", update.state.doc.toString());
        }),
      ],
    }),
  });
};

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

watch(
  () => props.modelValue || "",
  (value) => {
    if (!editor || editor.state.doc.toString() === value) return;
    applyingExternalValue = true;
    editor.dispatch({
      changes: { from: 0, to: editor.state.doc.length, insert: value },
    });
    applyingExternalValue = false;
  },
);

watch(
  () => props.readonly,
  async (value) => {
    if (!editor || !readonlyCompartment) return;
    editor.dispatch({
      effects: readonlyCompartment.reconfigure(await readonlyExtensions(value)),
    });
  },
);

onMounted(async () => {
  await nextTick();
  await createEditor();
});

onBeforeUnmount(() => {
  editor?.destroy();
  editor = null;
});
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
      <div ref="editorHost" class="script-editor"></div>
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
  overflow: hidden;
  border: 1px solid var(--bs-border-color);
  border-radius: var(--bs-border-radius);
}

.script-editor :deep(.cm-editor) {
  min-height: 420px;
  font-size: 0.875rem;
}

.script-editor :deep(.cm-scroller) {
  min-height: 420px;
  font-family: Consolas, "Courier New", monospace;
  line-height: 1.5;
}

.script-editor :deep(.cm-content) {
  min-height: 420px;
}
</style>
