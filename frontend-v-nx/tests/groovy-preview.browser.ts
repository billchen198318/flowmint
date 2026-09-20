import { createApp, h, nextTick, ref } from "vue";
import GroovyEditorDialog from "../pages/fm_prog004d0001/components/GroovyEditorDialog.vue";

const state = window as any;
const calls: any[] = [];
let pending: ((value: any) => void) | null = null;
state.groovyPost = async (url: string, body: any) => {
  calls.push({ url, body });
  return new Promise((resolve) => {
    pending = resolve;
  });
};
const binding = {
  nodeId: "calculate",
  bindingId: "groovy_calculate",
  scriptContent: "return [total: 2]",
  inputSchema: '{"type":"object","properties":{}}',
  outputSchema: '{"type":"object","properties":{"total":{"type":"number"}}}',
  mappingContent: '{"mappingVersion":1,"input":{},"output":{}}',
  timeoutMs: 3000,
};
const editor = ref<any>(null);
const readonly = ref(false);
let applied = 0;
const app = createApp({
  setup: () => () =>
    h(GroovyEditorDialog, {
      ref: editor,
      fields: [],
      readonly: readonly.value,
      preparePreview: async (draft: any, sampleInput: string) => ({
        oid: "draft",
        expectedLockVersion: 2,
        bpmnXml: "fixture",
        groovyBindings: [draft],
        nodeId: draft.nodeId,
        sampleInput,
      }),
      onApply: () => {
        applied++;
      },
    }),
});
app.mount("#canvas");
const assert = (condition: unknown, message: string) => {
  if (!condition) throw new Error(message);
};
const settle = async () => {
  await new Promise((resolve) => setTimeout(resolve, 30));
  await nextTick();
};
const button = (label: string) =>
  Array.from(document.querySelectorAll("button")).find(
    (item) => item.textContent?.trim() === label,
  )!;
const sample = (value: string) => {
  const field = document.querySelector<HTMLTextAreaElement>("#groovy-sample")!;
  field.value = value;
  field.dispatchEvent(new Event("input", { bubbles: true }));
};
const respond = () => {
  assert(pending, "request was not sent");
  pending!({
    data: {
      success: "Y",
      value: {
        status: "SUCCESS",
        result: { text: '<img src=x onerror="window.injected=true">' },
        logs: ["test log"],
        logsTruncated: true,
        diagnostics: [{ code: "TEST", line: 1, column: 1 }],
        elapsedMs: 1,
        expectedLockVersion: 2,
      },
    },
  });
  pending = null;
};
async function verify() {
  try {
    await nextTick();
    await editor.value.open(binding);
    sample("invalid");
    button("檢查腳本").click();
    await settle();
    assert(
      calls.length === 1 && calls[0].url.endsWith("/groovy/validate"),
      "CHECK endpoint",
    );
    assert(
      calls[0].body.sampleInput === "",
      "CHECK must ignore malformed sample",
    );
    respond();
    await settle();
    assert(
      document
        .querySelector(".preview-result")
        ?.textContent?.includes("SUCCESS"),
      "result display",
    );
    assert(
      !document.querySelector(".preview-result img") && !state.injected,
      "result must stay text",
    );
    button("試跑").click();
    await settle();
    assert(calls.length === 1, "invalid sample must not call RUN");
    assert(
      document
        .querySelector("#groovy-sample")
        ?.classList.contains("is-invalid"),
      "sample field error",
    );
    sample('{"amount":3}');
    button("試跑").click();
    await settle();
    assert(
      calls.length === 2 && calls[1].url.endsWith("/groovy/preview"),
      "RUN endpoint",
    );
    assert(calls[1].body.sampleInput === '{"amount":3}', "manual sample");
    sample('{"amount":4}');
    respond();
    await settle();
    assert(
      !document.querySelector(".preview-result"),
      "edited input must reject stale response",
    );
    button("試跑").click();
    await settle();
    button("取消").click();
    await settle();
    respond();
    await settle();
    await editor.value.open(binding);
    assert(
      !document.querySelector(".preview-result"),
      "closed dialog must reject stale response",
    );
    readonly.value = true;
    await settle();
    assert(
      button("檢查腳本").disabled && button("試跑").disabled,
      "readonly execution disabled",
    );
    assert(
      applied === 0 && binding.scriptContent === "return [total: 2]",
      "preview cannot apply or save",
    );
    app.unmount();
    await fetch("/result", {
      method: "POST",
      body: JSON.stringify({ success: true, checks: 11 }),
    });
  } catch (error: any) {
    await fetch("/result", {
      method: "POST",
      body: JSON.stringify({ success: false, error: error.message }),
    });
  }
}
void verify();
