export interface GroovyBinding {
  nodeId: string;
  bindingId: string;
  scriptContent: string;
  inputSchema: string;
  outputSchema: string;
  mappingContent: string;
  timeoutMs: number;
}

export interface GroovyField {
  path: string;
  label: string;
  type: string;
}

export const groovyContextFields = [
  ["tenantId", "Tenant", "string"],
  ["processInstanceId", "流程實例（試跑可為空）", "string|null"],
  ["processVersionNo", "固定流程版本", "number"],
  ["nodeId", "目前 System Task", "string"],
  ["invocationId", "本次執行識別", "string"],
  ["startedAt", "本次執行固定 UTC 時間", "string"],
  ["mode", "PREVIEW／RUNTIME", "string"],
  ["businessKey", "業務識別碼（試跑可為空）", "string|null"],
  ["documentNo", "正式單號（未編號可為空）", "string|null"],
  ["applicantAccount", "申請人（試跑可為空）", "string|null"],
  ["initiatorAccount", "實際發起人（試跑可為空）", "string|null"],
  ["applicantOrgUnitId", "流程保存的申請部門（可為空）", "string|null"],
].map(([path, label, type]) => ({ path: path!, label: label!, type: type! }));

export const newGroovyBinding = (nodeId: string): GroovyBinding => ({
  nodeId,
  bindingId: `groovy_${nodeId}`,
  scriptContent: "return [:]\n",
  inputSchema: JSON.stringify({ type: "object", properties: {} }, null, 2),
  outputSchema: JSON.stringify({ type: "object", properties: {} }, null, 2),
  mappingContent: JSON.stringify(
    { mappingVersion: 1, input: {}, output: {} },
    null,
    2,
  ),
  timeoutMs: 3000,
});

export const groovyGuide = [
  {
    title: "完整版本檔",
    text: "流程頁可匯出包含 BPMN、Groovy 腳本及簽核配置的 JSON 版本檔；匯出目前草稿前，須先將 Dialog 修改套用至節點。匯入只允許同 Tenant、同流程的 DRAFT，會取代畫面內容並清除圖形復原紀錄，仍須儲存。表單、Data Action 及組織配置須已存在；版本檔不包含這些主檔，也不包含正式單據資料。",
  },
  {
    title: "從節點到儲存",
    text: "新增 Groovy System Task，開啟編輯腳本。設定輸入 Schema／Mapping，撰寫腳本，再設定輸出 Schema／Mapping。套用至節點只更新畫面；最後仍須儲存流程。檢查／試跑與正式發布須先完成服務啟用及專用權限配置；發布時後端會重新編譯並固定版本內容。",
  },
  {
    title: "表單 value 與 Grid",
    text: "選擇表單欄位並指定輸入別名，例如 FORM_DATA.totalAmount → input.amount。巢狀欄位使用 dot path；Grid 以整個陣列輸入。數字、布林與物件保留型別。Runtime 取後端已保存 revision，不含畫面未送出的修改。",
  },
  {
    title: "唯讀系統變數",
    text: "context 由後端建立。試跑時單號、申請人、流程實例等正式業務欄位固定為 null，不讀正式單據。System Task 在背景執行，沒有目前登入者或目前簽核人；正式 Runtime 使用發布時固定的腳本與契約。",
  },
  {
    title: "輸出寫回與 Gateway",
    text: "return 必須回傳符合輸出 Schema 的 object，並透過 output Mapping 指定 FORM_DATA 欄位。修改 input 不會直接寫資料庫。未列出的輸出不回寫；不可修改 Tenant、身分、單號或流程狀態。後續 Gateway 才會讀取成功保存的結果。",
  },
  {
    title: "檢查、試跑與限制",
    text: "上色與 JSON 格式檢查不等於 Groovy 編譯。檢查不要求人工 sample；試跑只使用人工 JSON，不讀正式單據、不套用輸出 Mapping、不儲存。點選診斷可定位行／欄；修改內容或關閉視窗後舊結果失效。資料庫操作應使用相鄰 Data Action；HTTP 可使用教學提供的 Java HttpClient。GString 回傳前須轉為 String。",
  },
  {
    title: "Runtime 失敗與補處理",
    text: "Runtime 失敗後，到「Incident 營運管理」頁面的「Groovy 執行異常」查詢原因及處理歷程。確認安全後，可沿用原輸入與固定腳本版本重試，或使用最新表單修訂建立重算工作；兩者都須填寫理由，並保留原失敗紀錄。標記已處理或已忽略只更新處理狀態，不會執行腳本。若 HTTP 斷線或逾時造成外部結果不明，必須先查明外部狀態或確認對方支援冪等鍵，不可盲目重試。",
  },
  {
    title: "操作與排錯",
    text: "Ctrl+F 搜尋、Ctrl+H 取代、Ctrl+Space 顯示變數提示，Tab 縮排。錯誤行號可在後端診斷接線後跳轉。取消會放棄未套用的副本；有修改時關閉會詢問。切換教學或全螢幕保留編輯狀態。已發布版本唯讀。",
  },
];

export const groovyExample = `def subtotal = 0G
for (def item : input.items) {
    subtotal += (item.quantity * item.unitPrice).setScale(
        2, java.math.RoundingMode.HALF_UP
    )
}
return [
    subtotal: subtotal,
    requiresReview: subtotal > 10000G
]
`;

export const groovyExampleContract = JSON.stringify(
  {
    inputSchema: {
      type: "object",
      required: ["items"],
      properties: {
        items: {
          type: "array",
          items: {
            type: "object",
            required: ["quantity", "unitPrice"],
            properties: {
              quantity: { type: "number" },
              unitPrice: { type: "number" },
            },
          },
        },
      },
    },
    outputSchema: {
      type: "object",
      required: ["subtotal", "requiresReview"],
      properties: {
        subtotal: { type: "number" },
        requiresReview: { type: "boolean" },
      },
    },
    mapping: {
      mappingVersion: 1,
      input: { items: { source: "FORM_DATA", path: "items" } },
      output: {
        subtotal: { target: "FORM_DATA", path: "subtotal" },
        requiresReview: { target: "FORM_DATA", path: "requiresReview" },
      },
    },
    sampleInput: { items: [{ quantity: 3, unitPrice: 12.345 }] },
    expectedResult: { subtotal: 37.04, requiresReview: false },
  },
  null,
  2,
);

export const groovyHttpGetExample = `import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import groovy.json.JsonSlurper

def client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build()
def request = HttpRequest.newBuilder(URI.create('https://example.com/api/orders/123'))
    .timeout(Duration.ofSeconds(15))
    .header('Accept', 'application/json')
    // 如需認證，從核准的秘密來源取得，不要把正式密碼寫在腳本：
    // .header('Authorization', 'Bearer ' + tokenFromApprovedSecretSource)
    .GET()
    .build()
def response = client.send(request, HttpResponse.BodyHandlers.ofString())
if (response.statusCode() < 200 || response.statusCode() >= 300) {
    throw new IllegalStateException("HTTP status \${response.statusCode()}")
}
if (!response.body()) return [externalStatus: null, externalId: null]
Map json = (Map) new JsonSlurper().parseText(response.body())
return [externalStatus: json.status, externalId: json.id]
`;

export const groovyHttpPostExample = `import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build()
Map payload = [orderNo: input.orderNo, amount: input.amount]
String requestJson = JsonOutput.toJson(payload)
String idempotencyKey = context.invocationId
def request = HttpRequest.newBuilder(URI.create('https://example.com/api/orders'))
    .timeout(Duration.ofSeconds(15))
    .header('Content-Type', 'application/json')
    .header('Accept', 'application/json')
    .header('Idempotency-Key', idempotencyKey)
    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
    .build()
def response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
if (response.statusCode() < 200 || response.statusCode() >= 300) {
    throw new IllegalStateException("HTTP status \${response.statusCode()}")
}
Map result = response.body() ? (Map) new JsonSlurper().parseText(response.body()) : [:]
return [externalId: result.id]
`;

export const groovyHttpGuidance = [
  "Groovy 是受信任 IT 管理腳本，會在 FlowMint 主 JVM 執行；只在測試環境驗證完成後發布。",
  "必須同時設定 connect timeout 與 request timeout，並檢查 HTTP 2xx；不得把正式密碼、Bearer Token 或 API Key 寫死在腳本。",
  "查詢參數請用 URLEncoder；JSON object 對應 Map，JSON array 對應 List，空回應須先判斷再解析。",
  "POST／PATCH 等異動請求應傳送對方認可的冪等鍵。斷線或逾時可能代表結果不確定，不可盲目重送。",
];

export function validateGroovyBinding(binding: GroovyBinding): string[] {
  const errors: string[] = [];
  if (!binding.scriptContent.trim()) errors.push("腳本不可空白");
  if (new TextEncoder().encode(binding.scriptContent).length > 65536)
    errors.push("腳本最多 64 KiB");
  if (
    !Number.isInteger(binding.timeoutMs) ||
    binding.timeoutMs < 100 ||
    binding.timeoutMs > 10000
  )
    errors.push("逾時須介於 100～10000 ms");
  for (const key of [
    "inputSchema",
    "outputSchema",
    "mappingContent",
  ] as const) {
    try {
      const value = JSON.parse(binding[key]);
      if (!value || Array.isArray(value) || typeof value !== "object")
        throw new Error();
      if (key !== "mappingContent" && value.type !== "object")
        throw new Error();
      if (
        key === "mappingContent" &&
        (value.mappingVersion !== 1 || !value.input || !value.output)
      )
        throw new Error();
    } catch {
      errors.push(
        `${key} 必須是合法的 ${key === "mappingContent" ? "Mapping V1" : "object Schema"} JSON`,
      );
    }
  }
  return errors;
}

export function formGroovyFields(schemaContent: string): GroovyField[] {
  const fields: GroovyField[] = [];
  const visit = (components: any[], prefix = "") => {
    for (const component of components || []) {
      const path =
        prefix && component.key ? `${prefix}.${component.key}` : component.key;
      if (
        component.input &&
        path &&
        !["button", "file"].includes(component.type)
      ) {
        const type = ["datagrid", "editgrid"].includes(component.type)
          ? "array"
          : component.type === "number"
            ? "number"
            : component.type === "checkbox"
              ? "boolean"
              : component.type === "container"
                ? "object"
                : [
                      "textfield",
                      "textarea",
                      "email",
                      "url",
                      "phoneNumber",
                    ].includes(component.type)
                  ? "string"
                  : "unknown";
        fields.push({ path, label: component.label || path, type });
      }
      if (["datagrid", "editgrid"].includes(component.type)) continue;
      const nestedPrefix = component.type === "container" ? path : prefix;
      visit(component.components, nestedPrefix);
      for (const column of component.columns || [])
        visit(column.components, prefix);
      for (const row of component.rows || [])
        for (const cell of row) visit(cell.components, prefix);
    }
  };
  try {
    visit(JSON.parse(schemaContent).components);
  } catch {
    return [];
  }
  return fields;
}
