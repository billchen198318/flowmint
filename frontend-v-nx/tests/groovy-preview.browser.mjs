// Runs a fixed browser fixture with no application server, credentials or database writes.
import { build } from "esbuild";
import { parse, compileScript } from "@vue/compiler-sfc";
import { readFile } from "node:fs/promises";
import { spawn } from "node:child_process";
import { once } from "node:events";
import { mkdtemp, rm } from "node:fs/promises";
import { createServer } from "node:http";
import { tmpdir } from "node:os";
import { basename, dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const browserPath =
  process.env.FLOWMINT_TEST_BROWSER ||
  "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";
const compiled = await build({
  entryPoints: [
    fileURLToPath(new URL("./groovy-preview.browser.ts", import.meta.url)),
  ],
  bundle: true,
  write: false,
  format: "iife",
  platform: "browser",
  target: "es2022",
  define: {
    "import.meta.env.VITE_API_URL": '"/api"',
    "import.meta.env.VITE_SUCCESS_FLAG": '"Y"',
    "process.env.NODE_ENV": '"production"',
  },
  plugins: [
    {
      name: "vue-fixture",
      setup(plugin) {
        plugin.onResolve({ filter: /BaseHelper$|^vue3-toastify$/ }, () => ({
          path: "fixture-helpers",
          namespace: "fixture",
        }));
        plugin.onLoad({ filter: /.*/, namespace: "fixture" }, () => ({
          contents: `
      export const getAxiosInstance = () => ({ post: (...args) => window.groovyPost(...args) });
      export const escapeQifuHtmlMsg = value => value;
      export const checkInvalid = (field, fields) => !!fields[field];
      export const invalidFeedback = (field, fields) => fields[field] || "";
      export const toast = { warning() {}, success() {}, info() {} };
    `,
          loader: "js",
        }));
        plugin.onLoad({ filter: /\.vue$/ }, async ({ path }) => {
          const { descriptor } = parse(await readFile(path, "utf8"), {
            filename: path,
          });
          return {
            contents: compileScript(descriptor, {
              id: path,
              inlineTemplate: true,
            }).content,
            loader: "ts",
            resolveDir: dirname(path),
          };
        });
      },
    },
  ],
});
let finish;
const completed = new Promise((resolveResult) => {
  finish = resolveResult;
});
const server = createServer(async (request, response) => {
  if (request.method === "POST" && request.url === "/result") {
    const chunks = [];
    let length = 0;
    for await (const chunk of request) {
      length += chunk.length;
      if (length > 65536) {
        request.destroy();
        finish({ success: false, error: "Result exceeds limit" });
        return;
      }
      chunks.push(chunk);
    }
    try {
      finish(JSON.parse(Buffer.concat(chunks).toString("utf8")));
    } catch {
      finish({ success: false, error: "Invalid browser result" });
    }
    response.end("ok");
  } else if (request.url === "/fixture.js") {
    response.setHeader("Content-Type", "text/javascript; charset=utf-8");
    response.end(compiled.outputFiles[0].text);
  } else {
    response.setHeader("Content-Type", "text/html; charset=utf-8");
    response.end(
      '<!doctype html><meta charset="utf-8"><div id="canvas" style="width:1000px;height:600px"></div><script src="/fixture.js"></script>',
    );
  }
});
server.listen(0, "127.0.0.1");
await once(server, "listening");
const temporaryRoot = resolve(tmpdir());
const profile = await mkdtemp(join(temporaryRoot, "flowmint-groovy-browser-"));
const browser = spawn(
  browserPath,
  [
    "--headless=new",
    "--disable-gpu",
    "--no-first-run",
    "--no-default-browser-check",
    `--user-data-dir=${profile}`,
    `http://127.0.0.1:${server.address().port}/`,
  ],
  { windowsHide: true, stdio: "ignore" },
);
browser.on("error", (error) =>
  finish({ success: false, error: error.message }),
);
const timeout = setTimeout(
  () => finish({ success: false, error: "Browser fixture timed out" }),
  45000,
);
try {
  const result = await completed;
  console.log(JSON.stringify(result, null, 2));
  if (!result.success) process.exitCode = 1;
} finally {
  clearTimeout(timeout);
  if (browser.pid) {
    const stop = spawn("taskkill", ["/PID", String(browser.pid), "/T", "/F"], {
      windowsHide: true,
      stdio: "ignore",
    });
    await once(stop, "exit");
  }
  // Edge may relaunch under a different PID. Match only this unique profile.
  const profileLiteral = profile.replaceAll("'", "''");
  const cleanup = spawn(
    "powershell.exe",
    [
      "-NoProfile",
      "-Command",
      `$fixtureProfile='${profileLiteral}'; Get-CimInstance Win32_Process -Filter "name='msedge.exe'" -ErrorAction Stop | Where-Object { $_.CommandLine -and $_.CommandLine.Contains($fixtureProfile) } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }`,
    ],
    { windowsHide: true, stdio: "inherit" },
  );
  const [cleanupExit] = await once(cleanup, "exit");
  if (cleanupExit !== 0)
    throw new Error("Cannot verify fixture browser cleanup");
  server.closeAllConnections();
  server.close();
  // Delete only the unique temporary profile created by this invocation.
  if (
    dirname(resolve(profile)) !== temporaryRoot ||
    !basename(profile).startsWith("flowmint-groovy-browser-")
  )
    throw new Error("Unexpected browser profile path");
  await rm(profile, {
    recursive: true,
    force: true,
    maxRetries: 10,
    retryDelay: 200,
  });
}
