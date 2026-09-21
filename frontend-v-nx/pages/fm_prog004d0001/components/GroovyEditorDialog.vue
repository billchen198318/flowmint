<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from "vue";
import {
  getAxiosInstance,
  escapeQifuHtmlMsg,
  checkInvalid,
  invalidFeedback,
} from "../../../components/BaseHelper";
import { PageConstants } from "../config";
import {
  previewGeneration,
  type PrepareGroovyPreview,
  type GroovyPreviewResult,
} from "../bpmn/groovyPreview";
import { toast } from "vue3-toastify";
import GroovyCodeEditor from "./GroovyCodeEditor.vue";
import {
  groovyContextFields,
  groovyExample,
  groovyExampleContract,
  groovyGuide,
  groovyHttpGetExample,
  groovyHttpGuidance,
  groovyHttpPostExample,
  validateGroovyBinding,
  type GroovyBinding,
  type GroovyField,
} from "../bpmn/groovyContract";

const props = defineProps<{
  fields: GroovyField[];
  readonly?: boolean;
  preparePreview: PrepareGroovyPreview;
}>();
const emit = defineEmits<{ apply: [binding: GroovyBinding] }>();
const dialog = ref<HTMLDialogElement | null>(null);
const codeEditor = ref<InstanceType<typeof GroovyCodeEditor> | null>(null);
const draft = ref<GroovyBinding | null>(null);
const tab = ref<"editor" | "help">("editor");
const fullscreen = ref(false);
const showVariables = ref(true);
const errors = ref<string[]>([]);
const search = ref("");
const sourcePath = ref("");
const inputAlias = ref("");
const opened = ref(false);
const sampleInput = ref("{}");
const previewBusy = ref(false);
const previewResult = ref<GroovyPreviewResult | null>(null);
const checkFields = ref<Record<string, string>>({});
const requests = previewGeneration();
let pending: AbortController | null = null;
const invalidatePreview = () => {
  requests.invalidate();
  pending?.abort();
  pending = null;
  previewBusy.value = false;
  previewResult.value = null;
  checkFields.value = {};
};
watch([draft, sampleInput, () => props.readonly], invalidatePreview, {
  deep: true,
  flush: "sync",
});
const runPreview = async (run: boolean) => {
  if (!draft.value || props.readonly || previewBusy.value || !opened.value)
    return;
  invalidatePreview();
  errors.value = validateGroovyBinding(draft.value);
  if (run) {
    try {
      const sample = JSON.parse(sampleInput.value);
      if (!sample || Array.isArray(sample) || typeof sample !== "object")
        throw new Error();
      if (new TextEncoder().encode(sampleInput.value).length > 256 * 1024)
        throw new Error();
    } catch {
      checkFields.value = { sampleInput: "請輸入不超過 256 KiB 的 JSON 物件" };
      errors.value.push(checkFields.value.sampleInput);
    }
  }
  if (errors.value.length) {
    toast.warning(errors.value[0]);
    return;
  }
  const ticket = requests.current();
  const controller = new AbortController();
  pending = controller;
  previewBusy.value = true;
  try {
    const command = await props.preparePreview(
      JSON.parse(JSON.stringify(draft.value)),
      run ? sampleInput.value : "",
    );
    if (!requests.accepts(ticket)) return;
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        (run ? "/groovy/preview" : "/groovy/validate"),
      command,
      { signal: controller.signal, timeout: 40000 },
    );
    if (!requests.accepts(ticket)) return;
    checkFields.value = response.data?.checkFields || {};
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      const message = escapeQifuHtmlMsg(
        response.data?.message || "檢查／試跑失敗",
      );
      errors.value = [message];
      toast.warning(message);
      return;
    }
    const result = response.data.value as GroovyPreviewResult;
    if (!result || result.expectedLockVersion !== command.expectedLockVersion)
      throw new Error("流程版本已變更，請重新載入後再試。");
    previewResult.value = result;
  } catch {
    if (!requests.accepts(ticket)) return;
    errors.value = ["檢查／試跑未完成，請確認服務啟用與權限設定後再試。"];
    toast.warning(errors.value[0]);
  } finally {
    if (requests.accepts(ticket)) {
      previewBusy.value = false;
      pending = null;
    }
  }
};
const helpSections = computed(() =>
  groovyGuide.filter((section) =>
    `${section.title} ${section.text}`
      .toLocaleLowerCase()
      .includes(search.value.trim().toLocaleLowerCase()),
  ),
);
let original = "";
let previousFocus: HTMLElement | null = null;
let previousOverflow: string | null = null;
const inputNames = computed(() => {
  try {
    return Object.keys(
      JSON.parse(draft.value?.inputSchema || "{}").properties || {},
    );
  } catch {
    return [];
  }
});

const open = async (binding: GroovyBinding) => {
  if (!dialog.value || dialog.value.open) return;
  draft.value = JSON.parse(JSON.stringify(binding));
  sampleInput.value = "{}";
  original = JSON.stringify(draft.value);
  tab.value = "editor";
  errors.value = [];
  search.value = "";
  sourcePath.value = "";
  inputAlias.value = "";
  previousFocus =
    document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;
  previousOverflow = document.body.style.overflow;
  opened.value = true;
  dialog.value.showModal();
  document.body.style.overflow = "hidden";
  await nextTick();
  codeEditor.value?.measure();
};
const restore = () => {
  invalidatePreview();
  opened.value = false;
  if (previousOverflow !== null)
    document.body.style.overflow = previousOverflow;
  previousOverflow = null;
  previousFocus?.isConnected && previousFocus.focus();
  previousFocus = null;
};
const close = () => {
  if (
    !props.readonly &&
    JSON.stringify(draft.value) !== original &&
    !window.confirm("有尚未套用的修改，確定放棄？")
  )
    return;
  invalidatePreview();
  dialog.value?.close();
};
const apply = () => {
  if (!draft.value || props.readonly) return;
  errors.value = validateGroovyBinding(draft.value);
  if (errors.value.length) return;
  emit("apply", JSON.parse(JSON.stringify(draft.value)));
  invalidatePreview();
  dialog.value?.close();
};
const addInput = () => {
  if (!draft.value || props.readonly) return;
  const field = props.fields.find((item) => item.path === sourcePath.value);
  const alias = inputAlias.value.trim();
  if (field?.type === "unknown") {
    errors.value = [
      "此欄位的 value 型別需確認，請依實際表單契約手動設定輸入 Schema 與 Mapping。",
    ];
    return;
  }
  if (
    !field ||
    !/^[A-Za-z][A-Za-z0-9_]{0,99}$/.test(alias) ||
    ["class", "metaClass", "constructor", "prototype"].includes(alias)
  ) {
    errors.value = ["請選擇欄位並輸入合法的輸入別名"];
    return;
  }
  try {
    const schema = JSON.parse(draft.value.inputSchema);
    const mapping = JSON.parse(draft.value.mappingContent);
    schema.properties ||= {};
    mapping.input ||= {};
    if (
      Object.hasOwn(schema.properties, alias) ||
      Object.hasOwn(mapping.input, alias)
    ) {
      errors.value = ["輸入別名已存在，請改用其他名稱"];
      return;
    }
    schema.properties[alias] =
      field.type === "array"
        ? { type: ["array", "null"], items: { type: "object", properties: {} } }
        : { type: [field.type, "null"] };
    mapping.input[alias] = { source: "FORM_DATA", path: field.path };
    draft.value.inputSchema = JSON.stringify(schema, null, 2);
    draft.value.mappingContent = JSON.stringify(mapping, null, 2);
    errors.value = [];
    codeEditor.value?.insert(`input.${alias}`);
    if (["array", "object"].includes(field.type))
      toast.info("請在輸入 Schema 補齊物件／Grid 子欄位型別。");
  } catch {
    errors.value = ["請先修正輸入 Schema 與 Mapping 的 JSON 格式"];
  }
};
const copy = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text);
    toast.success("已複製");
  } catch {
    toast.warning("無法使用剪貼簿，請選取範例後複製");
  }
};
const toggleFullscreen = async () => {
  fullscreen.value = !fullscreen.value;
  await nextTick();
  codeEditor.value?.measure();
};
const switchTab = (event: KeyboardEvent) => {
  if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) return;
  event.preventDefault();
  tab.value =
    event.key === "Home"
      ? "editor"
      : event.key === "End"
        ? "help"
        : tab.value === "editor"
          ? "help"
          : "editor";
  dialog.value
    ?.querySelector<HTMLButtonElement>(`#groovy-tab-${tab.value}`)
    ?.focus();
};
onBeforeUnmount(restore);
defineExpose({ open });
</script>

<template>
  <dialog
    ref="dialog"
    class="groovy-dialog border rounded shadow"
    :class="{ fullscreen }"
    aria-labelledby="groovy-dialog-title"
    @close="restore"
    @cancel.prevent="close"
    @keydown.stop
  >
    <div v-if="draft && opened" class="dialog-layout">
      <header
        class="p-3 border-bottom d-flex justify-content-between align-items-center gap-3"
      >
        <div>
          <h2 id="groovy-dialog-title" class="h5 mb-1">
            Groovy 腳本：{{ draft.nodeId }}
          </h2>
          <div class="small text-secondary">
            {{ readonly ? "唯讀版本" : "套用至節點後，仍須儲存流程草稿" }}
          </div>
        </div>
        <div class="d-flex gap-2">
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary"
            @click="toggleFullscreen"
          >
            {{ fullscreen ? "還原視窗" : "全螢幕" }}
          </button>
          <button
            type="button"
            class="btn-close"
            aria-label="關閉編輯器"
            @click="close"
          ></button>
        </div>
      </header>
      <div
        class="nav nav-tabs px-3"
        role="tablist"
        aria-label="Groovy 編輯與教學"
        @keydown="switchTab"
      >
        <button
          id="groovy-tab-editor"
          type="button"
          class="nav-link"
          :class="{ active: tab === 'editor' }"
          role="tab"
          :aria-selected="tab === 'editor'"
          aria-controls="groovy-panel-editor"
          :tabindex="tab === 'editor' ? 0 : -1"
          @click="tab = 'editor'"
        >
          腳本編輯
        </button>
        <button
          id="groovy-tab-help"
          type="button"
          class="nav-link"
          :class="{ active: tab === 'help' }"
          role="tab"
          :aria-selected="tab === 'help'"
          aria-controls="groovy-panel-help"
          :tabindex="tab === 'help' ? 0 : -1"
          @click="tab = 'help'"
        >
          教學說明
        </button>
      </div>
      <div
        v-show="tab === 'editor'"
        id="groovy-panel-editor"
        class="editor-panel"
        role="tabpanel"
        aria-labelledby="groovy-tab-editor"
      >
        <div
          class="p-2 d-flex gap-2 align-items-center flex-wrap border-bottom"
        >
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary"
            :aria-expanded="showVariables"
            @click="showVariables = !showVariables"
          >
            輸入／輸出與可用變數
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline-info"
            @click="tab = 'help'"
          >
            查看教學
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline-primary"
            :disabled="readonly || previewBusy"
            @click="runPreview(false)"
          >
            檢查腳本
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline-primary"
            :disabled="readonly || previewBusy"
            @click="runPreview(true)"
          >
            試跑
          </button>
          <span class="small text-secondary">{{
            previewBusy
              ? "處理中…"
              : "檢查／試跑不會套用或儲存；服務須先啟用並配置權限。"
          }}</span>
        </div>
        <div class="editor-body" :class="{ 'hide-variables': !showVariables }">
          <GroovyCodeEditor
            ref="codeEditor"
            v-model="draft.scriptContent"
            :readonly="readonly"
            :input-names="inputNames"
            :visible="tab === 'editor'"
          />
          <aside
            v-show="showVariables"
            class="variables p-3 border-start"
            aria-label="Groovy 契約設定"
          >
            <label for="groovy-sample" class="form-label"
              >人工測試 JSON（試跑輸入）</label
            >
            <textarea
              id="groovy-sample"
              v-model="sampleInput"
              :readonly="readonly"
              :class="[
                'form-control font-monospace',
                checkInvalid('sampleInput', checkFields) ? 'is-invalid' : '',
              ]"
              rows="6"
            ></textarea>
            <div class="invalid-feedback">
              {{ invalidFeedback("sampleInput", checkFields) }}
            </div>
            <p class="small text-secondary">
              只使用此 JSON，不讀取正式表單，也不套用輸出 Mapping。
            </p>
            <label for="groovy-field" class="form-label"
              >表單欄位（不讀正式 value）</label
            >
            <select
              id="groovy-field"
              v-model="sourcePath"
              class="form-select mb-2"
              :disabled="readonly"
            >
              <option value="">
                {{
                  fields.length
                    ? "請選擇欄位"
                    : "尚無唯一表單 Schema，可先手動設定 Mapping"
                }}
              </option>
              <option
                v-for="field in fields"
                :key="field.path"
                :value="field.path"
              >
                {{ field.label }} · {{ field.path }} · {{ field.type }}
              </option>
            </select>
            <label for="groovy-alias" class="form-label">輸入別名</label>
            <input
              id="groovy-alias"
              v-model="inputAlias"
              class="form-control mb-2"
              :disabled="readonly"
              placeholder="例如 amount"
            />
            <button
              type="button"
              class="btn btn-sm btn-outline-primary mb-3"
              :disabled="readonly || !sourcePath"
              @click="addInput"
            >
              加入 Mapping 並插入變數
            </button>
            <details class="mb-3">
              <summary>系統變數（Runtime 待接線）</summary>
              <div
                v-for="field in groovyContextFields"
                :key="field.path"
                class="my-2"
              >
                <button
                  type="button"
                  class="btn btn-sm btn-outline-secondary"
                  :disabled="readonly"
                  @click="codeEditor?.insert(`context.${field.path}`)"
                >
                  context.{{ field.path }}
                </button>
                <div class="small text-secondary">
                  {{ field.label }} · {{ field.type }}
                </div>
              </div>
            </details>
            <div
              v-for="key in [
                'inputSchema',
                'outputSchema',
                'mappingContent',
              ] as const"
              :key="key"
              class="mb-3"
            >
              <label :for="`groovy-${key}`" class="form-label">{{ key }}</label>
              <textarea
                :id="`groovy-${key}`"
                v-model="draft[key]"
                :readonly="readonly"
                class="form-control font-monospace"
                rows="8"
              ></textarea>
            </div>
            <label for="groovy-timeout" class="form-label"
              >執行逾時（ms，Runtime 待接線）</label
            >
            <input
              id="groovy-timeout"
              v-model.number="draft.timeoutMs"
              type="number"
              min="100"
              max="10000"
              :readonly="readonly"
              class="form-control"
            />
          </aside>
        </div>
        <div
          v-if="previewResult"
          class="preview-result p-2 border-top"
          role="status"
          aria-live="polite"
        >
          <strong>{{ previewResult.status }}</strong> ·
          {{ previewResult.elapsedMs }} ms
          <div v-if="previewResult.errorCode">
            {{ previewResult.errorCode }}
          </div>
          <div
            v-for="(diagnostic, index) in previewResult.diagnostics"
            :key="index"
          >
            <button
              type="button"
              class="btn btn-sm btn-link"
              :disabled="diagnostic.line < 1"
              @click="codeEditor?.locate(diagnostic.line, diagnostic.column)"
            >
              {{ diagnostic.code }} · 行 {{ diagnostic.line }}／欄
              {{ diagnostic.column }}
            </button>
          </div>
          <pre v-if="previewResult.result != null">{{
            JSON.stringify(previewResult.result, null, 2)
          }}</pre>
          <pre v-if="previewResult.logs?.length">{{
            previewResult.logs.join("\n")
          }}</pre>
          <div v-if="previewResult.logsTruncated">日誌已截斷。</div>
        </div>
        <div v-if="errors.length" class="alert alert-warning m-2" role="alert">
          <ul class="mb-0">
            <li v-for="error in errors" :key="error">{{ error }}</li>
            <li v-for="(message, field) in checkFields" :key="field">
              {{ escapeQifuHtmlMsg(message) }}
            </li>
          </ul>
        </div>
      </div>
      <div
        v-show="tab === 'help'"
        id="groovy-panel-help"
        class="help-panel p-3"
        role="tabpanel"
        aria-labelledby="groovy-tab-help"
      >
        <label for="groovy-help-search" class="form-label">搜尋教學</label>
        <input
          id="groovy-help-search"
          v-model="search"
          type="search"
          class="form-control mb-3"
          placeholder="例如 Grid、Mapping、儲存"
        />
        <p v-if="!helpSections.length" role="status">沒有符合的教學主題。</p>
        <section
          v-for="section in helpSections"
          :key="section.title"
          class="mb-4"
        >
          <h3 class="h6">{{ section.title }}</h3>
          <p>{{ section.text }}</p>
        </section>
        <h3 class="h6">明細金額計算範例</h3>
        <button
          type="button"
          class="btn btn-sm btn-outline-secondary mb-2"
          @click="copy(groovyExample)"
        >
          複製程式碼
        </button>
        <pre>{{ groovyExample }}</pre>
        <details>
          <summary>範例契約與人工測試 JSON</summary>
          <pre>{{ groovyExampleContract }}</pre>
        </details>
        <h3 class="h6 mt-4">HTTP GET 與 JSON 轉 Map</h3>
        <button
          type="button"
          class="btn btn-sm btn-outline-secondary mb-2"
          @click="copy(groovyHttpGetExample)"
        >
          複製 GET 範例
        </button>
        <pre>{{ groovyHttpGetExample }}</pre>
        <h3 class="h6 mt-4">HTTP POST、Map 轉 JSON 與冪等鍵</h3>
        <button
          type="button"
          class="btn btn-sm btn-outline-secondary mb-2"
          @click="copy(groovyHttpPostExample)"
        >
          複製 POST 範例
        </button>
        <pre>{{ groovyHttpPostExample }}</pre>
        <h3 class="h6 mt-4">HTTP 安全與錯誤處理</h3>
        <ul>
          <li v-for="item in groovyHttpGuidance" :key="item">{{ item }}</li>
        </ul>
      </div>
      <footer class="p-3 border-top d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-secondary" @click="close">
          {{ readonly ? "關閉" : "取消" }}
        </button>
        <button
          v-if="!readonly"
          type="button"
          class="btn btn-primary"
          @click="apply"
        >
          套用至節點
        </button>
      </footer>
    </div>
  </dialog>
</template>

<style scoped>
.groovy-dialog {
  width: 90vw;
  max-width: none;
  height: 90dvh;
  max-height: 100dvh;
  padding: 0;
}
.groovy-dialog::backdrop {
  background: rgb(0 0 0 / 45%);
}
.groovy-dialog.fullscreen {
  width: 100vw;
  height: 100dvh;
  margin: 0;
  border-radius: 0 !important;
}
.dialog-layout {
  height: 100%;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr) auto;
}
.editor-panel {
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.preview-result {
  max-height: 30vh;
  overflow: auto;
}
.editor-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 32%);
  flex: 1;
  min-height: 0;
}
.editor-body.hide-variables {
  grid-template-columns: minmax(0, 1fr);
}
.variables,
.help-panel {
  overflow: auto;
  min-height: 0;
}
pre {
  background: #212529;
  color: #e9ecef;
  padding: 1rem;
  overflow: auto;
}
@media (max-width: 767.98px) {
  .groovy-dialog {
    width: 100vw;
    height: 100dvh;
    margin: 0;
  }
  .editor-body {
    display: flex;
    flex-direction: column;
    overflow: auto;
  }
  .editor-body > :first-child {
    min-height: 45dvh;
  }
  .variables {
    min-height: 260px;
    overflow: visible;
  }
}
</style>
