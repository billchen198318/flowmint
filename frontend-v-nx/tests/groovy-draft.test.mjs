import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import { transform } from "esbuild";
import BpmnModdle from "bpmn-moddle";

async function loadTypescript(path) {
  const source = await readFile(new URL(path, import.meta.url), "utf8");
  const { code } = await transform(source, { loader: "ts", format: "esm" });
  return import(
    `data:text/javascript;base64,${Buffer.from(code).toString("base64")}`
  );
}
const contract = await loadTypescript(
  "../pages/fm_prog004d0001/bpmn/groovyContract.ts",
);
const { default: BindingCommand } = await loadTypescript(
  "../pages/fm_prog004d0001/bpmn/GroovyBindingCommand.ts",
);
const descriptor = JSON.parse(
  await readFile(
    new URL(
      "../pages/fm_prog004d0001/bpmn/flowmint-moddle.json",
      import.meta.url,
    ),
  ),
);

test("Groovy stays a ServiceTask and script is kept out of XML", async () => {
  const moddle = new BpmnModdle({ flowmint: descriptor });
  const task = moddle.create("bpmn:ServiceTask", {
    id: "calculate",
    taskType: "GROOVY",
    bindingId: "groovy_calculate",
  });
  task.$groovyBinding = { scriptContent: "SENSITIVE SCRIPT CONTENT" };
  const process = moddle.create("bpmn:Process", {
    id: "P1",
    flowElements: [task],
  });
  const definitions = moddle.create("bpmn:Definitions", {
    targetNamespace: "test",
    rootElements: [process],
  });
  const { xml } = await moddle.toXML(definitions);
  assert.match(xml, /serviceTask/);
  assert.match(xml, /flowmint:bindingId="groovy_calculate"/);
  assert.doesNotMatch(xml, /SENSITIVE|scriptContent|groovyBinding/);
  const loaded = await moddle.fromXML(xml);
  assert.equal(
    loaded.rootElement.rootElements[0].flowElements[0].bindingId,
    "groovy_calculate",
  );
});

test("undo and redo restore the entire binding without aliasing editor state", () => {
  let Handler;
  new BindingCommand({
    registerHandler: (name, handler) => {
      assert.equal(name, "flowmint.updateGroovyBinding");
      Handler = handler;
    },
  });
  const previous = contract.newGroovyBinding("calculate");
  const changed = {
    ...previous,
    scriptContent: "return [amount: 7]",
    timeoutMs: 900,
  };
  const element = { businessObject: { $groovyBinding: previous } };
  const context = { element, binding: changed };
  const handler = new Handler();
  handler.execute(context);
  changed.timeoutMs = 1000;
  assert.equal(element.businessObject.$groovyBinding.timeoutMs, 900);
  handler.revert(context);
  assert.equal(element.businessObject.$groovyBinding, previous);
  handler.execute(context);
  assert.equal(
    element.businessObject.$groovyBinding.scriptContent,
    "return [amount: 7]",
  );
});

test("field catalog keeps container paths and passes Grid as a whole array", () => {
  const fields = contract.formGroovyFields(
    JSON.stringify({
      components: [
        {
          type: "container",
          key: "department",
          label: "部門",
          input: true,
          components: [{ type: "textfield", key: "code", input: true }],
        },
        {
          type: "datagrid",
          key: "items",
          label: "明細",
          input: true,
          components: [{ type: "number", key: "amount", input: true }],
        },
        { type: "file", key: "attachment", input: true },
      ],
    }),
  );
  assert.ok(fields.some((field) => field.path === "department.code"));
  assert.ok(
    fields.some((field) => field.path === "items" && field.type === "array"),
  );
  assert.ok(
    !fields.some(
      (field) => field.path === "items.amount" || field.path === "attachment",
    ),
  );
});

test("draft validates JSON contracts without claiming Groovy execution", () => {
  const binding = contract.newGroovyBinding("calculate");
  assert.deepEqual(contract.validateGroovyBinding(binding), []);
  assert.ok(
    contract.validateGroovyBinding({ ...binding, inputSchema: "[]" }).length,
  );
  assert.ok(
    contract.validateGroovyBinding({
      ...binding,
      scriptContent: "中文".repeat(22000),
    }).length,
  );
  assert.ok(
    contract.validateGroovyBinding({ ...binding, timeoutMs: 0 }).length,
  );
});

test("process designer sends Groovy drafts through the publish endpoint", async () => {
  const source = await readFile(
    new URL(
      "../pages/fm_prog004d0001/components/ProcessDesignerForm.vue",
      import.meta.url,
    ),
    "utf8",
  );
  const publishSource = source.slice(
    source.indexOf("const publish = async"),
    source.indexOf("const previewResolvers = async"),
  );
  assert.match(publishSource, /groovyBindings: currentGroovyBindings\(\)/);
  assert.match(publishSource, /post\("\/version\/publish"/);
  assert.doesNotMatch(publishSource, /正式發布尚未開放/);
});
