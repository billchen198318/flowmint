import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import vm from "node:vm";
import { randomUUID } from "node:crypto";
import { parse, compileScript } from "@vue/compiler-sfc";
import { transform } from "esbuild";
import { reactive, ref, watch, effectScope } from "vue";

const source = await readFile(
  new URL("../components/fm/GroovyIncidentPanel.vue", import.meta.url),
  "utf8",
);
const { descriptor } = parse(source);
const script = compileScript(descriptor, { id: "groovy-incidents-test" });
const { code } = await transform(script.content, {
  loader: "ts",
  format: "cjs",
  define: { "import.meta.env.VITE_SUCCESS_FLAG": '"Y"' },
});

function fixture(api) {
  const scope = effectScope();
  let unmount;
  const sandbox = {
    exports: {},
    module: { exports: {} },
    useApi: api,
    crypto: { randomUUID },
    require: () => ({
      ref,
      watch,
      defineComponent: (value) => value,
      onBeforeUnmount: (callback) => {
        unmount = callback;
      },
    }),
  };
  vm.runInNewContext(code, sandbox);
  const props = reactive({ tenantId: "T" });
  const state = scope.run(() =>
    sandbox.module.exports.default.setup(props, { expose() {} }),
  );
  return {
    state,
    props,
    close: () => {
      unmount();
      scope.stop();
    },
  };
}

test("recalculation confirms the preview revision and reuses request id after uncertain result", async () => {
  const calls = [];
  let submissions = 0;
  const app = fixture(async (path, options) => {
    calls.push({ path, options });
    if (path.endsWith("recalculate/preview"))
      return {
        success: "Y",
        value: {
          invocationId: "I",
          revision: 2,
          generation: 5,
          previousFormRevision: 3,
          formRevision: 4,
          formLock: 8,
          inputSha256: "input-hash",
          bindingSha256: "pinned-version",
        },
      };
    if (path.endsWith("/recalculate")) {
      if (++submissions === 1) throw new Error("connection lost");
      return { success: "Y", value: "successor" };
    }
    return {
      success: "Y",
      value: { status: "IGNORED", revision: 3, history: [], hasMore: false },
    };
  });
  app.state.selected.value = { invocationId: "I", executionStatus: "FAILED" };
  app.state.handling.value = { status: "OPEN", revision: 2, history: [] };
  app.state.reason.value = "new form reviewed";
  await app.state.recalculate();
  assert.equal(calls.length, 0);
  await app.state.checkRecalculate();
  app.state.reason.value = " ";
  await app.state.recalculate();
  assert.equal(submissions, 0);
  app.state.reason.value = " new form reviewed ";
  await app.state.recalculate();
  await app.state.recalculate();
  const sent = calls.filter((call) => call.path.endsWith("/recalculate"));
  assert.equal(sent[0].options.body.requestId, sent[1].options.body.requestId);
  assert.equal(sent[0].options.body.formRevision, 4);
  assert.equal(sent[0].options.body.formLock, 8);
  assert.equal(sent[0].options.body.inputSha256, "input-hash");
  assert.equal(sent[0].options.body.bindingSha256, "pinned-version");
  assert.equal(sent[0].options.headers["X-FlowMint-Tenant"], "T");
  assert.equal(app.state.successorInvocation.value, "successor");
  assert.equal(app.state.selected.value.executionStatus, "FAILED");
  assert.equal(app.state.handling.value.status, "IGNORED");
  assert.equal(app.state.recalculatePreview.value, null);
  app.close();
});

test("tenant switch discards late recalculation preview and confirmation", async () => {
  let resolve;
  const app = fixture(
    () =>
      new Promise((done) => {
        resolve = done;
      }),
  );
  app.state.selected.value = { invocationId: "I" };
  app.state.handling.value = { status: "OPEN", revision: 0, history: [] };
  const preview = app.state.checkRecalculate();
  app.props.tenantId = "U";
  resolve({ success: "Y", value: { invocationId: "I" } });
  await preview;
  assert.equal(app.state.recalculatePreview.value, null);
  app.state.selected.value = { invocationId: "U-I" };
  app.state.recalculatePreview.value = {
    invocationId: "U-I",
    revision: 0,
    generation: 1,
    formRevision: 2,
  };
  app.state.reason.value = "reviewed";
  const confirmation = app.state.recalculate();
  app.props.tenantId = "V";
  resolve({ success: "Y", value: "old-successor" });
  await confirmation;
  assert.equal(app.state.successorInvocation.value, "");
  assert.equal(app.state.recalculateMessage.value, "");
  assert.equal(app.state.selected.value, null);
  app.close();
});

test("rejected recalculation invalidates preview and never displays server diagnostics", async () => {
  const app = fixture(async () => ({
    success: "N",
    message: "SECRET",
    value: "SECRET",
  }));
  app.state.selected.value = { invocationId: "I" };
  app.state.recalculatePreview.value = {
    invocationId: "I",
    revision: 0,
    generation: 1,
    formRevision: 2,
  };
  app.state.reason.value = "reviewed";
  await app.state.recalculate();
  assert.equal(app.state.recalculatePreview.value, null);
  assert.equal(app.state.successorInvocation.value, "");
  assert.ok(!app.state.recalculateMessage.value.includes("SECRET"));
  app.close();
});

test("retry requires preview and reason and preserves request identity after lost response", async () => {
  const calls = [];
  let attempts = 0;
  const app = fixture(async (path, options) => {
    calls.push({ path, options });
    if (path.endsWith("retry/preview"))
      return {
        success: "Y",
        value: {
          invocationId: "I",
          revision: 4,
          generation: 7,
          inputRevision: 3,
          remainingAttempts: 1,
        },
      };
    if (path.endsWith("/retry")) {
      if (++attempts === 1) throw new Error("lost response");
      return { success: "Y", value: true };
    }
    return {
      success: "Y",
      value: { status: "OPEN", revision: 5, history: [], hasMore: false },
    };
  });
  app.state.selected.value = { invocationId: "I", executionStatus: "FAILED" };
  app.state.handling.value = { status: "OPEN", revision: 4, history: [] };
  app.state.reason.value = "reviewed";
  await app.state.retry();
  assert.equal(calls.length, 0);
  await app.state.checkRetry();
  app.state.reason.value = " ";
  await app.state.retry();
  assert.equal(attempts, 0);
  app.state.reason.value = " reviewed ";
  await app.state.retry();
  await app.state.retry();
  const submitted = calls.filter((call) => call.path.endsWith("/retry"));
  assert.equal(submitted.length, 2);
  assert.equal(
    submitted[0].options.body.requestId,
    submitted[1].options.body.requestId,
  );
  assert.equal(submitted[0].options.body.expectedGeneration, 7);
  assert.equal(submitted[0].options.body.expectedRevision, 4);
  assert.equal(submitted[0].options.body.reason, "reviewed");
  assert.equal(submitted[0].options.headers["X-FlowMint-Tenant"], "T");
  assert.equal(app.state.retryPreview.value, null);
  assert.equal(app.state.selected.value.executionStatus, "RETRY_ACCEPTED");
  app.close();
});

test("retry preview and submission ignore late responses from another tenant", async () => {
  let resolve;
  const app = fixture(
    () =>
      new Promise((done) => {
        resolve = done;
      }),
  );
  app.state.selected.value = { invocationId: "I" };
  app.state.handling.value = { status: "OPEN", revision: 0, history: [] };
  const preview = app.state.checkRetry();
  app.props.tenantId = "U";
  resolve({
    success: "Y",
    value: { invocationId: "I", revision: 0, generation: 1 },
  });
  await preview;
  assert.equal(app.state.retryPreview.value, null);
  app.state.selected.value = { invocationId: "U-I" };
  app.state.retryPreview.value = {
    invocationId: "U-I",
    revision: 0,
    generation: 1,
  };
  app.state.reason.value = "reviewed";
  const retry = app.state.retry();
  app.props.tenantId = "V";
  resolve({ success: "Y", value: true });
  await retry;
  assert.equal(app.state.selected.value, null);
  assert.equal(app.state.retryMessage.value, "");
  app.close();
});

test("denied retry preview clears confirmation and hides raw diagnostics", async () => {
  const app = fixture(async () => ({
    success: "N",
    message: "SECRET",
    value: { input: "SECRET" },
  }));
  app.state.selected.value = { invocationId: "I" };
  app.state.handling.value = { status: "OPEN", revision: 0, history: [] };
  app.state.retryPreview.value = { invocationId: "old" };
  await app.state.checkRetry();
  assert.equal(app.state.retryPreview.value, null);
  assert.ok(!app.state.retryMessage.value.includes("SECRET"));
  app.close();
});

test("tenant changes and unmount discard late results", async () => {
  let resolve;
  const app = fixture(
    () =>
      new Promise((done) => {
        resolve = done;
      }),
  );
  const pending = app.state.request(0);
  app.props.tenantId = "U";
  resolve({
    success: "Y",
    value: { items: [{ incidentId: "old-tenant" }], hasMore: true },
  });
  await pending;
  assert.equal(app.state.items.value.length, 0);
  assert.equal(app.state.hasMore.value, false);
  const second = app.state.request(0);
  app.close();
  resolve({
    success: "Y",
    value: { items: [{ incidentId: "late" }], hasMore: true },
  });
  await second;
  assert.equal(app.state.items.value.length, 0);
});

test("paging and detail use scoped API without assignment operations", async () => {
  const calls = [];
  const app = fixture(async (path, options) => {
    calls.push({ path, options });
    return {
      success: "Y",
      value: path.endsWith("detail")
        ? { incidentId: "incident" }
        : { items: [{ invocationId: "invocation" }], hasMore: true },
    };
  });
  await app.state.request(50);
  assert.equal(app.state.offset.value, 50);
  assert.equal(calls[0].options.headers["X-FlowMint-Tenant"], "T");
  await app.state.request(50, "invocation");
  assert.equal(calls[1].path, "/fm/operations/system-tasks/detail");
  assert.equal(calls[1].options.body.invocationId, "invocation");
  assert.equal(app.state.selected.value.incidentId, "incident");
  app.close();
});

test("denied responses clear records and never display raw server diagnostics", async () => {
  const app = fixture(async () => ({
    success: "N",
    message: "SECRET_SERVER_DIAGNOSTICS",
  }));
  app.state.items.value = [{ incidentId: "previous" }];
  await app.state.request(0);
  assert.equal(app.state.items.value.length, 0);
  assert.equal(app.state.message.value.includes("SECRET"), false);
  app.close();
});

test("handling sends server revision and reuses request id after uncertain transport failure", async () => {
  const calls = [];
  let fail = true;
  const app = fixture(async (path, options) => {
    calls.push({ path, options });
    if (path.endsWith("/handle") && fail) throw new Error("connection lost");
    return {
      success: "Y",
      value: { status: "IGNORED", revision: 1, history: [], hasMore: false },
    };
  });
  app.state.selected.value = { invocationId: "I" };
  app.state.handling.value = {
    status: "OPEN",
    revision: 0,
    history: [],
    hasMore: false,
  };
  app.state.reason.value = " reviewed ";
  await app.state.handle();
  fail = false;
  await app.state.handle();
  assert.equal(
    calls[0].options.body.requestId,
    calls[1].options.body.requestId,
  );
  assert.equal(calls[0].options.body.reason, "reviewed");
  assert.equal(calls[0].options.body.expectedRevision, 0);
  assert.equal(app.state.handling.value.status, "IGNORED");
  assert.equal(app.state.targetStatus.value, "OPEN");
  app.close();
});

test("late handling result cannot change another tenant's state", async () => {
  let resolve;
  const app = fixture(
    () =>
      new Promise((done) => {
        resolve = done;
      }),
  );
  app.state.selected.value = { invocationId: "I" };
  app.state.handling.value = {
    status: "OPEN",
    revision: 0,
    history: [],
    hasMore: false,
  };
  app.state.reason.value = "reviewed";
  const pending = app.state.handle();
  app.props.tenantId = "OTHER";
  resolve({ success: "Y", value: { status: "IGNORED", revision: 1 } });
  await pending;
  assert.equal(app.state.selected.value, null);
  assert.equal(app.state.handling.value, null);
  assert.equal(app.state.reason.value, "");
  app.close();
});

test("blank reasons do not submit and unavailable history hides action controls", async () => {
  let calls = 0;
  const app = fixture(async () => {
    calls++;
    return { success: "N", message: "SECRET" };
  });
  app.state.selected.value = { invocationId: "I" };
  app.state.handling.value = {
    status: "OPEN",
    revision: 0,
    history: [],
    hasMore: false,
  };
  app.state.reason.value = " ";
  await app.state.handle();
  assert.equal(calls, 0);
  await app.state.loadHandling();
  assert.equal(app.state.handling.value, null);
  assert.equal(app.state.handlingMessage.value.includes("SECRET"), false);
  app.close();
});
