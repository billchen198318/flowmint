import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import { transform } from "esbuild";

const source = await readFile(
  new URL("../pages/fm_prog004d0001/bpmn/groovyPreview.ts", import.meta.url),
  "utf8",
);
const { code } = await transform(source, { loader: "ts", format: "esm" });
const { previewBindings, previewGeneration } = await import(
  `data:text/javascript;base64,${Buffer.from(code).toString("base64")}`
);

test("preview uses the unsaved dialog copy without mutating any node", () => {
  const nodes = [
    { nodeId: "one", bindingId: "a", scriptContent: "old" },
    { nodeId: "two", bindingId: "b", scriptContent: "other" },
  ];
  const draft = { ...nodes[0], scriptContent: "unsaved" };
  const request = previewBindings(nodes, draft);
  assert.equal(request[0].scriptContent, "unsaved");
  assert.equal(request[1].scriptContent, "other");
  request[0].scriptContent = "changed";
  request[1].scriptContent = "changed";
  assert.equal(nodes[0].scriptContent, "old");
  assert.equal(nodes[1].scriptContent, "other");
  assert.equal(draft.scriptContent, "unsaved");
});

test("deleted, rebound and duplicated nodes cannot run the old dialog", () => {
  const draft = { nodeId: "one", bindingId: "a" };
  for (const nodes of [[], [{ ...draft, bindingId: "b" }], [draft, draft]])
    assert.throws(() => previewBindings(nodes, draft));
});

test("edit, undo, close and a later request invalidate earlier responses", async () => {
  const requests = previewGeneration();
  const old = requests.current();
  let resolve;
  const response = new Promise((done) => {
    resolve = done;
  });
  const accepted = response.then(() => requests.accepts(old));
  requests.invalidate();
  requests.invalidate();
  const latest = requests.current();
  resolve();
  assert.equal(await accepted, false);
  assert.equal(requests.accepts(latest), true);
  requests.invalidate();
  assert.equal(requests.accepts(latest), false);
});
