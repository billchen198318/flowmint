import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import test from "node:test";
import { build } from "esbuild";
import BpmnModdle from "bpmn-moddle";

const compiled = await build({
  entryPoints: [
    fileURLToPath(
      new URL(
        "../pages/fm_prog004d0001/bpmn/processVersionBundle.ts",
        import.meta.url,
      ),
    ),
  ],
  bundle: true,
  write: false,
  format: "esm",
  platform: "node",
});
const contract = await import(
  `data:text/javascript;base64,${Buffer.from(compiled.outputFiles[0].text).toString("base64")}`
);
const descriptor = JSON.parse(
  await readFile(
    new URL(
      "../pages/fm_prog004d0001/bpmn/flowmint-moddle.json",
      import.meta.url,
    ),
    "utf8",
  ),
);
const moddle = () => new BpmnModdle({ flowmint: descriptor });
const xml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:flowmint="https://flowmint.qifu.org/schema/bpmn" targetNamespace="test">
  <process id="P1" isExecutable="true">
    <startEvent id="start" />
    <serviceTask id="calculate" name="計算" flowmint:taskType="GROOVY" flowmint:bindingId="calculation" />
    <serviceTask id="lookup" flowmint:taskType="DATA_ACTION" flowmint:actionCode="LOOKUP"
      flowmint:actionVersion="1" flowmint:requestMapping="{}" flowmint:responseMapping="{}" />
    <userTask id="approve" />
    <endEvent id="end" />
  </process>
</definitions>`;

function bundle() {
  return contract.createProcessVersionBundle("T1", "P1", {
    oid: "database-version-id",
    versionNo: 2,
    lockVersion: 7,
    bpmnSha256: "not-exported",
    bpmnXml: xml,
    groovyBindings: [
      {
        oid: "binding-database-id",
        nodeId: "calculate",
        bindingId: "calculation",
        scriptContent: "// 中文\nreturn [total: input.amount * 2G]\n",
        inputSchema:
          '{"type":"object","properties":{"amount":{"type":"number"}}}',
        outputSchema:
          '{"type":"object","properties":{"total":{"type":"number"}}}',
        mappingContent:
          '{"mappingVersion":1,"input":{"amount":{"source":"FORM_DATA","path":"amount"}},"output":{"total":{"target":"FORM_DATA","path":"total"}}}',
        timeoutMs: 3000,
      },
    ],
    taskForms: [
      {
        oid: "form-rule-id",
        taskDefKey: "approve",
        formId: "F1",
        formVersionNo: 1,
        fieldPolicy: '{"default":"READ"}',
      },
    ],
    taskPolicies: [
      { taskDefKey: "approve", taskName: "核准", assignmentMode: "ASSIGNEE" },
    ],
    assignmentRules: [
      {
        taskDefKey: "approve",
        ruleSeq: 1,
        resolverType: "DIRECT_MANAGER",
        resolverConfig: "{}",
        maxResults: 100,
        status: "ACTIVE",
      },
    ],
    startPolicies: [
      { policySeq: 1, subjectType: "ALL", subjectRefId: null, allowStart: "Y" },
    ],
  });
}

test("full round trip preserves scripts, typed mapping, Data Action XML and all rule groups", async () => {
  const original = bundle();
  const encoded = contract.serializeProcessVersionBundle(original);
  const decoded = contract.parseProcessVersionBundle(encoded, "T1", "P1");
  await contract.validateProcessBundleDiagram(decoded, moddle(), true);
  assert.deepEqual(decoded, original);
  assert.match(decoded.groovyBindings[0].scriptContent, /中文/);
  assert.match(decoded.bpmnXml, /DATA_ACTION/);
  assert.doesNotMatch(
    encoded,
    /database-version-id|binding-database-id|form-rule-id|lockVersion|not-exported/,
  );
  assert.equal(decoded.sourceVersionNo, 2);
  assert.equal(decoded.taskForms[0].formVersionNo, 1);
});

test("rejects XML-only imports and cross-Tenant or cross-process restores", () => {
  assert.throws(
    () => contract.parseProcessVersionBundle(xml, "T1", "P1"),
    /JSON/,
  );
  const content = contract.serializeProcessVersionBundle(bundle());
  assert.throws(
    () => contract.parseProcessVersionBundle(content, "T2", "P1"),
    /Tenant/,
  );
  assert.throws(
    () => contract.parseProcessVersionBundle(content, "T1", "P2"),
    /流程代碼/,
  );
});

test("rejects missing scripts, orphan bindings and duplicate binding identities", async () => {
  for (const mutate of [
    (value) => {
      value.groovyBindings = [];
    },
    (value) => {
      value.groovyBindings[0].nodeId = "missing";
    },
    (value) => {
      value.groovyBindings[0].bindingId = "wrong-binding";
    },
    (value) => {
      value.groovyBindings.push(structuredClone(value.groovyBindings[0]));
    },
  ]) {
    const value = bundle();
    mutate(value);
    await assert.rejects(
      contract.validateProcessBundleDiagram(value, moddle(), true),
      /Groovy/,
    );
  }
});

test("disabled Groovy rejects imports while Data Action-only bundles still work", async () => {
  const value = bundle();
  await assert.rejects(
    contract.validateProcessBundleDiagram(value, moddle(), false),
    /尚未啟用/,
  );
  value.groovyBindings = [];
  value.bpmnXml = xml.replace(/\s*<serviceTask id="calculate"[^>]+\/>/, "");
  await contract.validateProcessBundleDiagram(value, moddle(), false);
});

test("rejects unknown bundle versions and injected persistence or deployment fields", () => {
  for (const changes of [
    { bundleVersion: 2 },
    { lockVersion: 0 },
    { oid: "overwrite" },
    { flowableDeploymentId: "deploy" },
  ]) {
    assert.throws(() =>
      contract.parseProcessVersionBundle(
        JSON.stringify({ ...bundle(), ...changes }),
        "T1",
        "P1",
      ),
    );
  }
  const value = bundle();
  value.taskForms[0].tenantId = "T2";
  assert.throws(() =>
    contract.parseProcessVersionBundle(JSON.stringify(value), "T1", "P1"),
  );
});

test("enforces file, XML and individual script byte limits", () => {
  assert.throws(() =>
    contract.parseProcessVersionBundle(
      " ".repeat(contract.MAX_PROCESS_BUNDLE_BYTES + 1),
      "T1",
      "P1",
    ),
  );
  const value = bundle();
  value.groovyBindings[0].scriptContent = "中".repeat(22000);
  assert.throws(
    () => contract.parseProcessVersionBundle(JSON.stringify(value), "T1", "P1"),
    /64 KiB/,
  );
  value.groovyBindings = [];
  value.bpmnXml = "x".repeat(2 * 1024 * 1024 + 1);
  assert.throws(
    () => contract.parseProcessVersionBundle(JSON.stringify(value), "T1", "P1"),
    /2 MiB/,
  );
});

test("rejects malformed XML, wrong process IDs and unknown attributes without changing source data", async () => {
  for (const content of [
    "<definitions>",
    xml.replace('id="P1"', 'id="P2"'),
    xml.replace('id="calculate"', 'id="calculate" flowmint:unexpected="value"'),
  ]) {
    const value = bundle();
    value.bpmnXml = content;
    const previous = structuredClone(value);
    await assert.rejects(
      contract.validateProcessBundleDiagram(value, moddle(), true),
    );
    assert.deepEqual(value, previous);
  }
});

test("rejects DTD and orphan or duplicated User Task configuration", async () => {
  const value = bundle();
  value.bpmnXml = '<!DOCTYPE definitions [<!ENTITY sample "value">]>' + xml;
  assert.throws(
    () => contract.parseProcessVersionBundle(JSON.stringify(value), "T1", "P1"),
    /DTD/,
  );
  for (const key of ["taskForms", "taskPolicies", "assignmentRules"]) {
    const orphan = bundle();
    orphan[key][0].taskDefKey = "missing";
    await assert.rejects(
      contract.validateProcessBundleDiagram(orphan, moddle(), true),
    );
    const duplicated = bundle();
    duplicated[key].push(structuredClone(duplicated[key][0]));
    await assert.rejects(
      contract.validateProcessBundleDiagram(duplicated, moddle(), true),
    );
  }
});
