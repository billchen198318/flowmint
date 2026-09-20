<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import type { EditorView } from "@codemirror/view";
import type { CompletionContext } from "@codemirror/autocomplete";
import { groovyContextFields } from "../bpmn/groovyContract";

const props = defineProps<{
  modelValue: string;
  readonly?: boolean;
  inputNames: string[];
  visible: boolean;
}>();
const emit = defineEmits<{ "update:modelValue": [value: string] }>();
const host = ref<HTMLElement | null>(null);
const error = ref("");
let editor: EditorView | null = null;
let readonlyCompartment: import("@codemirror/state").Compartment | null = null;
let destroyed = false;

const complete = (context: CompletionContext) => {
  const match = context.matchBefore(/(?:input|context)\.[A-Za-z0-9_]*/);
  if (!match) return null;
  const system = match.text.startsWith("context.");
  return {
    from: match.from + match.text.indexOf(".") + 1,
    options: system
      ? groovyContextFields.map((field) => ({
          label: field.path,
          type: "property",
          detail: field.label,
        }))
      : props.inputNames.map((name) => ({ label: name, type: "variable" })),
  };
};

const createEditor = async () => {
  try {
    const [state, view, language, mode, theme, commands, search, completion] =
      await Promise.all([
        import("@codemirror/state"),
        import("@codemirror/view"),
        import("@codemirror/language"),
        import("@codemirror/legacy-modes/mode/groovy"),
        import("@codemirror/theme-one-dark"),
        import("@codemirror/commands"),
        import("@codemirror/search"),
        import("@codemirror/autocomplete"),
      ]);
    if (destroyed || !host.value || editor) return;
    readonlyCompartment = new state.Compartment();
    editor = new view.EditorView({
      parent: host.value,
      state: state.EditorState.create({
        doc: props.modelValue,
        extensions: [
          view.lineNumbers(),
          view.highlightActiveLineGutter(),
          view.highlightActiveLine(),
          language.StreamLanguage.define(mode.groovy),
          language.bracketMatching(),
          language.indentOnInput(),
          language.foldService.of((editorState, start, end) => {
            const line = editorState.doc.lineAt(start);
            if (!/[{[]\s*$/.test(line.text)) return null;
            const indentation = (text: string) =>
              (text.match(/^\s*/)?.[0] || "").replaceAll(
                "\t",
                " ".repeat(editorState.tabSize),
              ).length;
            const base = indentation(line.text);
            let last = end;
            for (
              let number = line.number + 1;
              number <= editorState.doc.lines;
              number++
            ) {
              const next = editorState.doc.line(number);
              if (next.text.trim() && indentation(next.text) <= base) break;
              last = next.to;
            }
            return last > end ? { from: end, to: last } : null;
          }),
          language.foldGutter(),
          commands.history(),
          completion.closeBrackets(),
          completion.autocompletion({ override: [complete] }),
          search.search({ top: true }),
          theme.oneDark,
          view.keymap.of([
            ...completion.closeBracketsKeymap,
            ...commands.defaultKeymap,
            ...search.searchKeymap,
            ...commands.historyKeymap,
            ...language.foldKeymap,
            ...completion.completionKeymap,
            commands.indentWithTab,
          ]),
          readonlyCompartment.of([
            state.EditorState.readOnly.of(!!props.readonly),
            view.EditorView.editable.of(!props.readonly),
          ]),
          view.EditorView.updateListener.of((update) => {
            if (update.docChanged)
              emit("update:modelValue", update.state.doc.toString());
          }),
          view.EditorView.theme({
            "&": { height: "100%" },
            ".cm-scroller": { overflow: "auto" },
          }),
        ],
      }),
    });
  } catch {
    error.value = "Groovy 編輯器載入失敗，請重新開啟視窗。";
  }
};

const insert = (text: string) => {
  if (!editor || props.readonly) return;
  const selection = editor.state.selection.main;
  editor.dispatch({
    changes: { from: selection.from, to: selection.to, insert: text },
    selection: { anchor: selection.from + text.length },
    scrollIntoView: true,
  });
  editor.focus();
};
const measure = () => editor?.requestMeasure();
const locate = (line: number, column: number) => {
  if (
    !editor ||
    !Number.isInteger(line) ||
    line < 1 ||
    line > editor.state.doc.lines
  )
    return;
  const target = editor.state.doc.line(line);
  const anchor = Math.min(target.to, target.from + Math.max(0, column - 1));
  editor.dispatch({ selection: { anchor }, scrollIntoView: true });
  editor.focus();
};
watch(
  () => props.modelValue,
  (value) => {
    if (editor && value !== editor.state.doc.toString())
      editor.dispatch({
        changes: { from: 0, to: editor.state.doc.length, insert: value },
      });
  },
);
watch(
  () => props.readonly,
  async (readonly) => {
    const [{ EditorState }, { EditorView }] = await Promise.all([
      import("@codemirror/state"),
      import("@codemirror/view"),
    ]);
    if (editor && readonlyCompartment)
      editor.dispatch({
        effects: readonlyCompartment.reconfigure([
          EditorState.readOnly.of(!!readonly),
          EditorView.editable.of(!readonly),
        ]),
      });
  },
);
watch(
  () => props.visible,
  async () => {
    await nextTick();
    measure();
  },
);
onMounted(createEditor);
onBeforeUnmount(() => {
  destroyed = true;
  editor?.destroy();
  editor = null;
});
defineExpose({ insert, measure, locate });
</script>

<template>
  <div class="groovy-code-host">
    <div v-if="error" class="alert alert-danger" role="alert">{{ error }}</div>
    <div ref="host" class="editor-host" aria-label="Groovy 腳本編輯器"></div>
  </div>
</template>

<style scoped>
.groovy-code-host,
.editor-host {
  height: 100%;
  min-height: 280px;
}
</style>
