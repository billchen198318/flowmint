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
const showSystemApiHelp = ref(false);
const editorHost = ref<HTMLElement | null>(null);
let editor: EditorView | null = null;
let readonlyCompartment: import("@codemirror/state").Compartment | null = null;
let applyingExternalValue = false;
let originalBodyOverflow = "";

const openSystemApiHelp = () => {
  showSystemApiHelp.value = true;
};

const closeSystemApiHelp = () => {
  showSystemApiHelp.value = false;
};

const handleWindowKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape" && showSystemApiHelp.value) closeSystemApiHelp();
};

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

watch(showSystemApiHelp, (visible) => {
  if (typeof document === "undefined") return;
  if (visible) {
    originalBodyOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = originalBodyOverflow;
  }
});

onMounted(async () => {
  window.addEventListener("keydown", handleWindowKeydown);
  await nextTick();
  await createEditor();
});

onBeforeUnmount(() => {
  window.removeEventListener("keydown", handleWindowKeydown);
  if (typeof document !== "undefined") {
    document.body.style.overflow = originalBodyOverflow;
  }
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
          支援 7 個生命週期與 FlowMint 系統 Context API。
        </div>
      </div>
      <div class="d-flex gap-2">
        <button
          type="button"
          class="btn btn-sm btn-outline-info"
          @click="openSystemApiHelp"
        >
          系統 API 說明
        </button>
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

  <Teleport to="body">
    <div
      v-if="showSystemApiHelp"
      class="system-api-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="system-api-help-title"
      @click.self="closeSystemApiHelp"
    >
      <div class="system-api-dialog">
        <div class="system-api-header">
          <div>
            <h2 id="system-api-help-title" class="h5 mb-1">Custom JavaScript 系統 API 說明</h2>
            <div class="small text-muted">以目前 FlowMint Runtime 實作為準</div>
          </div>
          <button type="button" class="btn-close" aria-label="關閉" @click="closeSystemApiHelp"></button>
        </div>

        <div class="system-api-body">
          <section>
            <h3 class="h6">基本格式</h3>
            <p>Script 必須回傳一個生命週期物件，每個 handler 都可以使用 <code>async/await</code>。</p>
            <pre><code>return {
  async onFormLoad(ctx) {
    ctx.log("表單載入", ctx.formCode, ctx.versionNo);
  },
  async beforeSubmit(ctx) {
    return true;
  },
};</code></pre>
          </section>

          <section>
            <h3 class="h6">生命週期 methods</h3>
            <div class="table-responsive">
              <table class="table table-sm table-bordered align-middle">
                <thead><tr><th>Method</th><th>執行時機</th><th>重要行為</th></tr></thead>
                <tbody>
                  <tr><td><code>onFormLoad(ctx)</code></td><td>表單建立與 submission 設定後</td><td>適合初始化與資料查詢</td></tr>
                  <tr><td><code>onFieldChange(ctx)</code></td><td>Form.io change 事件</td><td>異動資訊在 <code>ctx.changed</code></td></tr>
                  <tr><td><code>beforeSubmit(ctx)</code></td><td>正式送出前</td><td>可回傳 <code>false</code>、<code>{ valid: false, message }</code> 或 throw 阻止送出</td></tr>
                  <tr><td><code>afterSubmit(ctx)</code></td><td>後端送出成功後</td><td><code>ctx.response</code> 含 API 回應；失敗不會回滾已完成交易</td></tr>
                  <tr><td><code>onDataActionSuccess(ctx)</code></td><td>Data Action 成功後</td><td>提供 action、request 與 response 資訊</td></tr>
                  <tr><td><code>onDataActionError(ctx)</code></td><td>Data Action 失敗後</td><td>錯誤在 <code>ctx.error</code></td></tr>
                  <tr><td><code>onDestroy(ctx)</code></td><td>表單切換或銷毀前</td><td>適合釋放 Script 自行建立的資源</td></tr>
                </tbody>
              </table>
            </div>
          </section>

          <section>
            <h3 class="h6">系統 variables</h3>
            <div class="table-responsive">
              <table class="table table-sm table-bordered align-middle">
                <thead><tr><th>Variable</th><th>說明</th></tr></thead>
                <tbody>
                  <tr><td><code>ctx.mode</code></td><td><code>DESIGNER_PREVIEW</code>、<code>RUNTIME_START</code>、<code>RUNTIME_TASK</code> 或 <code>READ_ONLY</code></td></tr>
                  <tr><td><code>ctx.tenantId</code></td><td>目前 Tenant ID</td></tr>
                  <tr><td><code>ctx.formId</code> / <code>ctx.formCode</code> / <code>ctx.versionNo</code></td><td>表單與版本資訊；部分 Runtime 的 formCode 可能為空字串</td></tr>
                  <tr><td><code>ctx.form</code></td><td>Form.io form instance</td></tr>
                  <tr><td><code>ctx.data</code></td><td>目前 submission data，可讀寫</td></tr>
                  <tr><td><code>ctx.submission</code></td><td>完整 Form.io submission 物件</td></tr>
                  <tr><td><code>ctx.changed</code></td><td>當次異動的 component、instance、value 與 flags</td></tr>
                  <tr><td><code>ctx.actionType</code> / <code>ctx.taskId</code> / <code>ctx.formData</code></td><td>Task Action 的額外情境，只在相關 lifecycle 提供</td></tr>
                  <tr><td><code>ctx.actionCode</code> / <code>ctx.actionVersion</code> / <code>ctx.bindingId</code></td><td>Data Action 情境，僅在相關 hook 可用</td></tr>
                  <tr><td><code>ctx.request</code> / <code>ctx.response</code> / <code>ctx.error</code></td><td>請求、成功回應或錯誤，依 lifecycle 提供</td></tr>
                  <tr><td><code>ctx.axios</code></td><td>FlowMint 共用 Axios instance，含登入狀態與 interceptors</td></tr>
                </tbody>
              </table>
            </div>
            <div class="alert alert-warning py-2 mb-0">
              <code>ctx.processInstanceId</code>、<code>ctx.businessKey</code> 與 <code>ctx.utils.*</code> 目前不是可用 API。
            </div>
          </section>

          <section>
            <h3 class="h6">系統 helper methods</h3>
            <div class="table-responsive">
              <table class="table table-sm table-bordered align-middle">
                <thead><tr><th>Method</th><th>說明</th></tr></thead>
                <tbody>
                  <tr><td><code>ctx.getValue(path)</code></td><td>以 key 或 dot path 取值</td></tr>
                  <tr><td><code>await ctx.setValue(path, value)</code></td><td>設值並同步頂層 Form.io component</td></tr>
                  <tr><td><code>await ctx.setSelectOptions(key, items)</code></td><td>更新 Select 選項；item 格式為 <code>{ label, value, disabled? }</code></td></tr>
                  <tr><td><code>await ctx.setComponentDisabled(key, disabled)</code></td><td>啟用或停用元件；READ_ONLY 不允許重新啟用</td></tr>
                  <tr><td><code>ctx.getComponent(key)</code></td><td>取得 Form.io component instance</td></tr>
                  <tr><td><code>await ctx.redraw()</code></td><td>重新同步 submission 與畫面</td></tr>
                  <tr><td><code>await ctx.executeDataAction(code, body?, versionNo?)</code></td><td>執行 FlowMint Data Action；READ_ONLY 僅允許 QUERY</td></tr>
                  <tr><td><code>ctx.notify.success/warning/error(message)</code></td><td>顯示畫面通知</td></tr>
                  <tr><td><code>ctx.log/warn/error(...values)</code></td><td>寫入本次 Script Console</td></tr>
                </tbody>
              </table>
            </div>
          </section>

          <section>
            <h3 class="h6">Examples</h3>
            <h4 class="small fw-semibold">計算欄位</h4>
            <pre><code>async onFieldChange(ctx) {
  const key = ctx.changed?.component?.key;
  if (!["quantity", "unitPrice"].includes(key)) return;
  const amount = Number(ctx.getValue("quantity") || 0)
    * Number(ctx.getValue("unitPrice") || 0);
  await ctx.setValue("amount", amount);
}</code></pre>

            <h4 class="small fw-semibold">Data Action 回填選項</h4>
            <pre><code>async onFormLoad(ctx) {
  const result = await ctx.executeDataAction("FM_EMPLOYEE_OPTIONS", {});
  await ctx.setSelectOptions("employeeId", result.items || []);
}</code></pre>

            <h4 class="small fw-semibold">送出前檢查</h4>
            <pre><code>async beforeSubmit(ctx) {
  if (Number(ctx.data.totalAmount || 0) &lt;= 0) {
    return { valid: false, message: "總金額必須大於 0" };
  }
  return true;
}</code></pre>
          </section>

          <div class="alert alert-info mb-0">
            每個 lifecycle 預設最長執行 15 秒；表單銷毀或執行逾時後，過期的設值、Data Action 結果與通知不會再回寫。
          </div>
        </div>

        <div class="system-api-footer">
          <button type="button" class="btn btn-secondary" @click="closeSystemApiHelp">關閉</button>
        </div>
      </div>
    </div>
  </Teleport>
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

.system-api-modal {
  position: fixed;
  z-index: 1080;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.55);
}

.system-api-dialog {
  display: flex;
  flex-direction: column;
  width: min(1100px, 100%);
  max-height: calc(100vh - 2rem);
  overflow: hidden;
  background: var(--bs-body-bg);
  border-radius: var(--bs-border-radius-lg);
  box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.35);
}

.system-api-header,
.system-api-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--bs-border-color);
}

.system-api-footer {
  justify-content: flex-end;
  border-top: 1px solid var(--bs-border-color);
  border-bottom: 0;
}

.system-api-body {
  padding: 1.25rem;
  overflow-y: auto;
}

.system-api-body section + section {
  margin-top: 1.5rem;
}

.system-api-body pre {
  padding: 0.875rem;
  overflow-x: auto;
  color: #e9ecef;
  background: #212529;
  border-radius: var(--bs-border-radius);
}

.system-api-body code {
  font-family: Consolas, "Courier New", monospace;
}

@media (max-width: 575.98px) {
  .system-api-modal {
    padding: 0;
  }

  .system-api-dialog {
    width: 100%;
    max-height: 100vh;
    min-height: 100vh;
    border-radius: 0;
  }
}
</style>
