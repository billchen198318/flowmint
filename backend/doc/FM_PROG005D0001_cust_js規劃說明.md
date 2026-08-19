# FM_PROG005D0001 客製 JavaScript 規劃說明

## 1. 文件目的

本文件規劃在 `FM_PROG005D0001` 表單設計與版本功能中，提供表單設計者撰寫客製 JavaScript 的完整能力。

目標不是只開放 Form.io 個別元件原生的 `Calculated Value`、`Custom Validation`、`Conditional` 與 `Logic`，而是進一步提供整張表單共用的 JavaScript 編輯器、生命週期、執行 Context、錯誤顯示、版本保存及正式 Runtime 執行機制。

完成後，表單設計者應可在不修改 FlowMint 前端專案原始碼、不重新編譯及不另外建立 Vue 頁面的情況下，實作下列進階功能：

- 欄位連動與跨欄位計算。
- 明細列加總、重算、增刪及跨列驗證。
- 動態預設值、顯示、唯讀、停用及必填狀態。
- 表單載入後初始化。
- 欄位異動後執行複合邏輯。
- 送出前資料整理與業務檢查。
- 送出後執行畫面提示或後續動作。
- 使用 FlowMint Axios instance 呼叫內部 API 或 Data Action。
- 依回傳資料更新 Form.io submission data 及畫面元件。

---

## 2. 現況盤點

### 2.1 已具備能力

目前 `FM_PROG005D0001` 已具備：

- `fm_form_def` 表單主檔。
- `fm_form_version` 表單版本。
- Form.io Builder。
- 草稿儲存、建立新版本、發布及退役。
- `SCHEMA_CONTENT` 保存完整 Form.io Schema。
- `UI_SCHEMA_CONTENT` 保存 FlowMint Renderer metadata。
- 發布內容正規化及 SHA-256。
- 草稿設計與試跑模式。
- Data Action Binding Editor。
- Preview 使用 `Formio.createForm()` 渲染表單。
- 後端接受 Form.io Schema 中的 JavaScript 與 Logic 屬性。

Form.io 元件目前可使用其內建的進階設定，例如：

- Custom Default Value。
- Calculated Value。
- Custom Validation。
- Advanced Conditional。
- Logic／Trigger／Action。

這些設定會保存在元件 Schema 中，並隨表單版本保存。

### 2.2 尚未具備能力

目前尚缺：

- 整張表單共用的 JavaScript 編輯頁籤。
- 明確且固定的表單生命週期函式。
- Designer Preview 與正式 Runtime 共用的 Script Executor。
- 提供給客製程式使用的統一 `context`。
- 非同步生命週期及 `await` 支援。
- JavaScript 語法檢查。
- 執行錯誤的生命週期、行號與 Stack 顯示。
- Script 執行紀錄與除錯 Console。
- 發布前的 Script compile validation。
- 將整張表單 JavaScript 納入版本 SHA-256。

只依靠元件級 JavaScript，複雜邏輯容易分散在多個 Component Schema，不利於共用函式、流程追蹤及版本比較。因此需要表單級 JavaScript。

---

## 3. 功能範圍

### 3.1 第一版納入

第一版提供：

1. 表單級 JavaScript 編輯器。
2. 固定生命週期函式。
3. 同步與非同步 JavaScript。
4. 統一 Form Script Context。
5. Designer 試跑模式執行。
6. 正式 Runtime 執行。
7. 送出前可中止送出。
8. 執行錯誤及 Console 顯示。
9. 草稿保存、版本複製與發布鎖定。
10. 發布前語法檢查。

### 3.2 第一版不要求

第一版不另外實作：

- npm 套件線上安裝。
- TypeScript 編譯。
- 多檔案 JavaScript Project。
- 原始碼 Git Repository 整合。
- Server-side JavaScript 執行。
- JavaScript Debugger breakpoint。
- Source Map 上傳。

表單 Script 在瀏覽器執行；若要使用第三方函式庫，第一版應由 FlowMint 前端專案預先安裝並透過 Context 提供。

---

## 4. 使用者操作流程

### 4.1 FM_PROG005D0001 畫面

表單版本內容區調整為：

```text
設計 | 試跑 | Data Action | JavaScript | JSON
```

各模式職責：

| 模式 | 職責 |
|---|---|
| 設計 | 使用 Form.io Builder 拖拉元件及設定元件屬性 |
| 試跑 | 以目前記憶體中的 Schema、UI Schema 及 Script 建立可操作表單 |
| Data Action | 設定事件、Action、Request Mapping 與 Response Mapping |
| JavaScript | 編輯整張表單的生命週期與共用函式 |
| JSON | 檢視 Form.io Schema、UI Schema 及原始版本內容 |

### 4.2 JavaScript 編輯器

JavaScript 頁籤至少包含：

- 程式碼編輯區。
- 儲存狀態。
- 語法檢查按鈕。
- 格式化按鈕。
- 還原為上次儲存內容。
- 生命週期範本插入。
- Context 說明。
- 執行 Console。
- 清除 Console。

建議使用 Monaco Editor；若不增加大型依賴，第一階段可先使用一般 `textarea`，但資料格式與 Executor 契約不可因編輯器選型而不同。

### 4.3 草稿與發布版本

- `DRAFT`：JavaScript 可編輯、儲存、試跑及檢查。
- `PUBLISHED`：JavaScript 唯讀，可檢視及在 Runtime 執行。
- `RETIRED`：JavaScript 唯讀，既有流程實例仍使用原版本。
- 建立新版本時，複製前一版 Schema、UI Schema 及 Script。
- 已發布版本不可直接修改 Script。

---

## 5. JavaScript 模組格式

### 5.1 標準格式

表單 Script 使用單一物件作為模組內容：

```javascript
return {
  async onFormLoad(ctx) {
    ctx.log("表單載入", ctx.formCode, ctx.versionNo);
  },

  async onFieldChange(ctx) {
    if (ctx.changed?.component?.key === "quantity") {
      ctx.data.amount =
        Number(ctx.data.quantity || 0) *
        Number(ctx.data.unitPrice || 0);
      await ctx.redraw();
    }
  },

  async beforeSubmit(ctx) {
    if (!Array.isArray(ctx.data.items) || ctx.data.items.length === 0) {
      throw new Error("至少需要一筆明細");
    }
  },

  async afterSubmit(ctx) {
    ctx.log("送出完成", ctx.response);
  },
};
```

由 Executor 將 Script 包裝為 `AsyncFunction` 並取得回傳物件。Script 不使用 ES Module 的 `export default`，避免瀏覽器動態編譯時還需要額外 bundler 或 module URL。

### 5.2 共用函式

設計者可在回傳物件外宣告共用函式：

```javascript
function calculateTotal(items) {
  return (items || []).reduce(
    (total, item) =>
      total + Number(item.quantity || 0) * Number(item.unitPrice || 0),
    0,
  );
}

return {
  async onFormLoad(ctx) {
    ctx.data.totalAmount = calculateTotal(ctx.data.items);
  },

  async onFieldChange(ctx) {
    ctx.data.totalAmount = calculateTotal(ctx.data.items);
  },
};
```

### 5.3 空白 Script

未設定 Script 時保存空字串。Executor 遇到空字串時不建立模組、不註冊生命週期，表單仍可正常執行。

---

## 6. 生命週期

### 6.1 第一版生命週期

| 生命週期 | 執行時機 | 可非同步 | 失敗行為 |
|---|---|---:|---|
| `onFormLoad` | Form.io instance 建立完成且初始 submission 設定後 | 是 | 顯示錯誤，表單保留 |
| `onFieldChange` | Form.io `change` 事件發生後 | 是 | 顯示錯誤，保留目前輸入 |
| `beforeSubmit` | 呼叫正式送出 API 前 | 是 | 中止送出並顯示錯誤 |
| `afterSubmit` | 正式送出 API 成功後 | 是 | 顯示錯誤，但不回滾已完成送出 |
| `onDataActionSuccess` | Data Action Binding 成功後 | 是 | 顯示錯誤，保留 Action 結果 |
| `onDataActionError` | Data Action Binding 失敗後 | 是 | 顯示 Script 錯誤及原 Action 錯誤 |
| `onDestroy` | 表單切換、離開頁面或 instance destroy 前 | 是 | 記錄錯誤後繼續釋放資源 |

### 6.2 onFormLoad

執行順序：

1. 解析 Form.io Schema。
2. 建立 Form.io instance。
3. 設定既有 submission data。
4. 建立 Script Module。
5. 建立 Context。
6. 執行 `onFormLoad(ctx)`。
7. 執行必要 redraw。

### 6.3 onFieldChange

`onFieldChange` 的 Context 額外提供：

```javascript
ctx.changed = {
  component,
  instance,
  value,
  flags,
};
```

同一次 Script 修改資料而引發的 Form.io change，可能造成重複觸發。因此 Executor 必須提供執行旗標及事件序號，避免同一事件無限遞迴。

### 6.4 beforeSubmit

`beforeSubmit` 可用三種方式阻止送出：

```javascript
throw new Error("申請金額不可為零");
```

```javascript
return false;
```

```javascript
return {
  valid: false,
  message: "請先完成所有明細欄位",
};
```

若回傳 `undefined`、`true` 或 `{ valid: true }`，則繼續送出。

`beforeSubmit` 可直接修改 `ctx.data`。送出 API 必須使用生命週期完成後的最新 submission data。

### 6.5 afterSubmit

`afterSubmit` 的 Context 額外提供：

```javascript
ctx.response;
ctx.processInstanceId;
ctx.businessKey;
```

`afterSubmit` 發生錯誤時不得將已成功的後端交易顯示為送出失敗；畫面應清楚區分「表單已送出，但送出後 JavaScript 執行失敗」。

---

## 7. Script Context 契約

### 7.1 基本 Context

```typescript
export interface FormCustomScriptContext {
  mode: "DESIGNER_PREVIEW" | "RUNTIME_CREATE" | "RUNTIME_TASK" | "READ_ONLY";
  tenantId: string;
  formId: string;
  formCode: string;
  versionNo: number;
  processInstanceId?: string;
  taskId?: string;
  businessKey?: string;
  form: any;
  data: Record<string, any>;
  submission: Record<string, any>;
  changed?: FormioChangedEvent;
  response?: any;
  axios: AxiosInstance;
  getValue: (key: string) => any;
  setValue: (key: string, value: any) => Promise<void>;
  getComponent: (key: string) => any;
  redraw: () => Promise<void>;
  executeDataAction: (
    actionCode: string,
    body?: Record<string, any>,
    versionNo?: number,
  ) => Promise<any>;
  notify: FormScriptNotify;
  log: (...values: any[]) => void;
  warn: (...values: any[]) => void;
  error: (...values: any[]) => void;
}
```

### 7.2 取值與設值

```javascript
const quantity = ctx.getValue("quantity");
await ctx.setValue("amount", Number(quantity) * 100);
```

欄位 key 支援 dot path：

```javascript
ctx.getValue("applicant.departmentName");
await ctx.setValue("summary.totalAmount", 1000);
```

### 7.3 取得 Form.io Component

```javascript
const amountComponent = ctx.getComponent("amount");
amountComponent.disabled = true;
await ctx.redraw();
```

### 7.4 Axios

客製 Script 可透過既有 Axios instance 呼叫 FlowMint 內部 API：

```javascript
const response = await ctx.axios.post(
  "/api/FM_PROG001D0001/query",
  { keyword: ctx.data.keyword },
);

ctx.data.options = response.data?.value || [];
await ctx.redraw();
```

Executor 提供的 `ctx.axios` 必須與 FlowMint 頁面使用相同的 `getAxiosInstance()`，使登入狀態、Base URL、Token refresh 及既有 interceptor 行為一致。

### 7.5 Data Action

```javascript
const employee = await ctx.executeDataAction(
  "FM_GET_CURRENT_EMPLOYEE",
  {},
  1,
);

ctx.data.employeeId = employee.employeeId;
ctx.data.displayName = employee.displayName;
await ctx.redraw();
```

此 helper 重用既有 Data Action Executor，不要求設計者自行處理統一 response envelope。

### 7.6 訊息

```javascript
ctx.notify.success("計算完成");
ctx.notify.warning("部分欄位尚未輸入");
ctx.notify.error("讀取資料失敗");
```

---

## 8. 資料模型

### 8.1 建議新增欄位

在 `fm_form_version` 增加：

```sql
CUSTOM_SCRIPT_CONTENT  LONGTEXT NULL
```

Oracle 對應 `CLOB`，MSSQL 對應 `nvarchar(max)`。

不建議只把完整 JavaScript 塞入 `UI_SCHEMA_CONTENT`，原因是：

- Script 是獨立版本內容。
- JavaScript 編輯器不應反覆解析及重寫 UI Schema。
- 獨立欄位更容易做版本比較。
- 錯誤訊息及 API DTO 能明確指出 Script。
- 後續可增加 Script 專用 metadata，而不污染 UI Schema。

### 8.2 Entity 與 DTO

下列物件增加 `customScriptContent`：

- `FmFormVersion`。
- `FmFormVersionCommand`。
- `FmFormVersionView`。
- `FmFormVersionMapper.xml` 的 ResultMap、Insert、Update 及 Select 欄位。

### 8.3 版本內容 Hash

目前版本 Hash 由 Schema 與 UI Schema 組合計算。調整為：

```text
normalizedSchemaContent
+ "\n"
+ normalizedUiSchemaContent
+ "\n"
+ normalizedCustomScriptContent
```

JavaScript 不做語意改寫。為避免不同換行符號產生不同 Hash，後端保存前統一：

- `CRLF` 轉為 `LF`。
- 單獨 `CR` 轉為 `LF`。
- `null` 轉為空字串。
- 不自動刪除註解。
- 不自動壓縮。

### 8.4 建立新版本

建立新草稿版本時完整複製：

- `SCHEMA_CONTENT`。
- `UI_SCHEMA_CONTENT`。
- `CUSTOM_SCRIPT_CONTENT`。

---

## 9. 後端 API 調整

### 9.1 Load

既有表單 Load Response 的 Version 增加：

```json
{
  "oid": "...",
  "versionNo": 2,
  "versionStatus": "DRAFT",
  "schemaContent": "...",
  "uiSchemaContent": "...",
  "customScriptContent": "...",
  "contentSha256": "..."
}
```

### 9.2 Save Draft

```http
POST /api/FM_PROG005D0001/version/save-draft
```

Request：

```json
{
  "oid": "form-version-oid",
  "schemaContent": "...",
  "uiSchemaContent": "...",
  "customScriptContent": "..."
}
```

### 9.3 Validate Script

建議增加草稿語法檢查 API：

```http
POST /api/FM_PROG005D0001/version/validate-script
```

Request：

```json
{
  "oid": "form-version-oid",
  "customScriptContent": "..."
}
```

Response：

```json
{
  "valid": false,
  "errors": [
    {
      "line": 18,
      "column": 7,
      "message": "Unexpected token ')'"
    }
  ]
}
```

Java 後端本身不負責執行瀏覽器 JavaScript。若後端沒有 JavaScript parser，第一版可由前端先 compile 檢查；發布 API仍要求前端送出檢查結果並由後端驗證 Script 非空內容已隨版本保存。正式階段建議後端加入純 parser，避免只靠前端檢查。

### 9.4 Publish

發布流程調整為：

1. 驗證 Schema JSON。
2. 驗證 UI Schema JSON。
3. 正規化 Script 換行。
4. 執行 Script 語法檢查。
5. 驗證生命週期模組可建立。
6. 計算包含 Script 的 SHA-256。
7. 鎖定版本並寫入發布資訊。

---

## 10. 前端元件規劃

### 10.1 建議新增檔案

```text
frontend-v-nx/pages/fm_prog005d0001/components/FormCustomJavascriptEditor.vue
frontend-v-nx/composables/useFormCustomJavascript.ts
frontend-v-nx/types/formCustomJavascript.ts
```

### 10.2 FormCustomJavascriptEditor

責任：

- 顯示及編輯 `customScriptContent`。
- 顯示 DRAFT／PUBLISHED 唯讀狀態。
- 插入生命週期範本。
- 語法檢查。
- 格式化 JavaScript。
- 顯示錯誤行號。
- 顯示 Context API 說明。
- 顯示試跑 Console。

### 10.3 useFormCustomJavascript

建議介面：

```typescript
const scriptRuntime = useFormCustomJavascript({
  scriptContent,
  form,
  tenantId,
  formId,
  formCode,
  versionNo,
  mode,
});

await scriptRuntime.compile();
await scriptRuntime.run("onFormLoad");
await scriptRuntime.run("beforeSubmit");
await scriptRuntime.destroy();
```

責任：

1. 正規化 Script。
2. 建立 `AsyncFunction`。
3. 驗證回傳值為物件。
4. 驗證已知生命週期為函式。
5. 建立 Context。
6. 執行生命週期。
7. 捕捉並格式化錯誤。
8. 維護 Console。
9. 註冊及解除 Form.io 事件。
10. 防止已 destroy 的 instance 繼續回寫資料。

### 10.4 編譯方式

概念實作：

```typescript
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;

const factory = new AsyncFunction(
  "context",
  `
    "use strict";
    const initialContext = context;
    ${scriptContent}
  `,
);

const module = await factory(baseContext);
```

實際實作必須將編譯錯誤轉成設計者可理解的行號；因 Executor 增加了包裝行，錯誤行號需扣除 wrapper offset。

---

## 11. Designer 試跑整合

### 11.1 建立順序

試跑模式建立順序：

```text
syncSchemaFromDesigner
  → parse schema
  → Formio.createForm
  → attach Data Action Bridge
  → compile Custom JavaScript
  → attach Form Script lifecycle
  → run onFormLoad
```

### 11.2 切換及釋放

離開試跑、切換版本或元件 unmount 時：

1. 停止接受新的 Script lifecycle。
2. 執行 `onDestroy`。
3. 解除 Form.io change listener。
4. 解除 Data Action Bridge listener。
5. 清除 Script Runtime reference。
6. Destroy Form.io instance。

### 11.3 Console

試跑 Console 每筆至少記錄：

```text
時間
等級：LOG／WARN／ERROR
生命週期
訊息
錯誤行號
Stack
```

Console 只存在目前瀏覽器頁面記憶體，不寫入表單版本。

---

## 12. 正式 Runtime 整合

### 12.1 共用原則

正式 Runtime 不可另外實作第二套 Script 規則。Designer Preview 與 Runtime 必須共同使用：

- `useFormCustomJavascript`。
- 相同 Script Module 格式。
- 相同 Context 基本欄位。
- 相同 Data Action helper。
- 相同生命週期名稱。
- 相同錯誤格式。

只有 `ctx.mode` 及 Runtime 才能取得的流程資訊不同。

### 12.2 發起表單

發起畫面：

```text
載入 Published Form Version
  → 建立 Form.io instance
  → 設定 Draft Form Data 或空白 submission
  → 建立 Script Runtime
  → onFormLoad
  → 使用者編輯
  → onFieldChange
  → 使用者送出
  → beforeSubmit
  → FlowMint Submit API
  → afterSubmit
```

### 12.3 簽核任務表單

簽核畫面除了表單版本，還必須套用 `FIELD_POLICY`。順序建議：

1. 載入表單版本及資料快照。
2. 建立 Form.io instance。
3. 套用 Task Field Policy。
4. 建立 Script Context。
5. 執行 `onFormLoad`。

若 Script 動態修改 Component 的 `disabled` 或 hidden 狀態，Runtime 應明確定義 Field Policy 與 Script 的優先順序。建議固定為：

```text
Task Field Policy 的 HIDDEN／READ 不可被 Script 改成 EDIT
Task Field Policy 的 EDIT 可由 Script 再改為 READ／disabled
```

此規則是為了避免同一張表單在不同簽核節點呈現錯誤的可編輯狀態，也使 Script 行為可預測。

### 12.4 唯讀畫面

`READ_ONLY` 模式可執行 `onFormLoad` 以完成顯示格式及衍生欄位，但不執行 `beforeSubmit`、`afterSubmit`。`onFieldChange` 原則上不會發生。

---

## 13. Data Action Binding 與 Custom JavaScript 關係

兩者可以並存：

- 一般資料查詢及回填可使用視覺化 Binding。
- 複雜 Request 組裝或 Response 處理可在 Script 使用 `ctx.executeDataAction()`。
- Binding 成功後可呼叫 `onDataActionSuccess`。
- Binding 失敗後可呼叫 `onDataActionError`。

事件順序：

```text
Form.io event
  → Binding Executor
  → Response Mapping
  → onDataActionSuccess
  → redraw
```

若沒有對應 Binding，`onFieldChange` 仍獨立執行。

為避免兩邊同時改寫相同欄位造成不可預期結果，固定順序為：

```text
onFieldChange
  → Data Action Binding
  → Response Mapping
  → onDataActionSuccess
```

---

## 14. 錯誤處理

### 14.1 錯誤格式

```typescript
export interface FormCustomScriptError {
  phase: "COMPILE" | "EXECUTE";
  lifecycle?: FormScriptLifecycle;
  message: string;
  line?: number;
  column?: number;
  stack?: string;
  occurredAt: string;
}
```

### 14.2 Designer

- Compile Error：阻止進入試跑並定位至 JavaScript 行號。
- Execute Error：保留表單，顯示 toast 並寫入 Console。
- `beforeSubmit` Error：阻止試跑送出。
- Error 不得造成整個 `FM_PROG005D0001` Vue 頁面中止渲染。

### 14.3 Runtime

- `onFormLoad` Error：顯示表單載入失敗區塊及錯誤識別碼。
- `onFieldChange` Error：顯示訊息並保留目前輸入。
- `beforeSubmit` Error：中止送出。
- `afterSubmit` Error：顯示表單已成功送出，但後續 Script 執行失敗。
- Runtime 錯誤可傳至前端 logging／monitoring API，保留 Form ID、Version、Lifecycle 及帳號等診斷資訊。

---

## 15. 效能與執行控制

即使不限制 JavaScript 可使用的瀏覽器功能，Executor 仍需要處理正常的畫面穩定性：

- `onFieldChange` 對同一事件序號不可重入。
- 非同步執行完成前，切換版本後不得回寫舊 Form instance。
- 可設定 debounce helper，處理搜尋欄位。
- 同一 Data Action 可選擇取消前一次 request。
- Runtime destroy 後忽略尚未完成的 Promise 結果。
- Console 對單次 Session 設定最大筆數，避免瀏覽器記憶體持續增加。

建議 Context 提供：

```javascript
ctx.utils.debounce(key, milliseconds, callback);
ctx.utils.delay(milliseconds);
ctx.utils.cancelRequest(key);
```

---

## 16. 典型使用案例

### 16.1 金額計算

```javascript
function calculateAmount(data) {
  data.amount =
    Number(data.quantity || 0) *
    Number(data.unitPrice || 0);
}

return {
  async onFormLoad(ctx) {
    calculateAmount(ctx.data);
  },

  async onFieldChange(ctx) {
    const key = ctx.changed?.component?.key;
    if (key === "quantity" || key === "unitPrice") {
      calculateAmount(ctx.data);
      await ctx.redraw();
    }
  },
};
```

### 16.2 明細加總

```javascript
return {
  async onFieldChange(ctx) {
    ctx.data.totalAmount = (ctx.data.items || []).reduce(
      (sum, item) => sum + Number(item.amount || 0),
      0,
    );
    await ctx.redraw();
  },
};
```

### 16.3 依員工資料初始化

```javascript
return {
  async onFormLoad(ctx) {
    const employee = await ctx.executeDataAction(
      "FM_GET_CURRENT_EMPLOYEE",
      {},
    );

    ctx.data.employeeId = employee.employeeId;
    ctx.data.employeeName = employee.displayName;
    ctx.data.departmentName = employee.departmentName;
    await ctx.redraw();
  },
};
```

### 16.4 送出前檢查

```javascript
return {
  async beforeSubmit(ctx) {
    const total = Number(ctx.data.totalAmount || 0);
    const detailTotal = (ctx.data.items || []).reduce(
      (sum, item) => sum + Number(item.amount || 0),
      0,
    );

    if (total !== detailTotal) {
      return {
        valid: false,
        message: "表頭金額與明細合計不一致",
      };
    }

    return true;
  },
};
```

### 16.5 呼叫內部 API

```javascript
return {
  async onFieldChange(ctx) {
    if (ctx.changed?.component?.key !== "customerNo") return;

    const response = await ctx.axios.post(
      "/api/customer/profile",
      { customerNo: ctx.data.customerNo },
    );

    ctx.data.customerName = response.data.value.customerName;
    ctx.data.creditLimit = response.data.value.creditLimit;
    await ctx.redraw();
  },
};
```

---

## 17. 測試計畫

### 17.1 Script 編譯

- 空白 Script。
- 合法同步 Script。
- 合法 async／await Script。
- 語法錯誤顯示正確行號。
- Script 未回傳物件。
- 生命週期欄位不是函式。
- 共用函式可被多個生命週期呼叫。

### 17.2 生命週期

- `onFormLoad` 只執行一次。
- `onFieldChange` 收到正確 changed component。
- Script 改值後 Form.io 正確 redraw。
- Script 改值不造成無限 change loop。
- `beforeSubmit` 回傳 false 時中止。
- `beforeSubmit` throw Error 時中止。
- `beforeSubmit` 修改的資料送至後端。
- `afterSubmit` 收到成功 response。
- `afterSubmit` 失敗不改變已送出結果。
- Destroy 後 Promise 完成不再回寫。

### 17.3 Context

- `getValue`／`setValue` 一般欄位。
- Dot path 欄位。
- Data Grid／Edit Grid 明細。
- `getComponent`。
- Axios instance 呼叫。
- Data Action helper。
- Toast／Console。
- Designer Preview 與 Runtime context 差異。

### 17.4 版本

- 草稿保存 Script。
- Reload 後內容一致。
- 建立新版本完整複製 Script。
- 發布後 Script 唯讀。
- Script 修改會改變 SHA-256。
- 已發布版本 Runtime 使用固定 Script。
- Retired 版本既有流程仍可載入。

### 17.5 Runtime

- 發起表單執行完整生命週期。
- 簽核任務表單載入 Script。
- Field Policy 與 Script 狀態優先順序正確。
- Read-only 表單只執行允許的生命週期。
- Runtime Error 顯示 Form ID、Version 及 Lifecycle。

---

## 18. 分階段實作

### Phase 1：資料保存與編輯器

- `fm_form_version.CUSTOM_SCRIPT_CONTENT`。
- Entity、Mapper、DTO、Load 及 Save Draft。
- JavaScript 頁籤。
- 基本編輯、範本、唯讀及語法檢查。
- 版本 Hash 納入 Script。

完成條件：Script 可隨草稿保存、Reload、建立新版本及發布鎖定。

### Phase 2：Designer Preview Executor

- `useFormCustomJavascript`。
- Context 基本欄位。
- `onFormLoad`、`onFieldChange`、`onDestroy`。
- Preview Console。
- 編譯及執行錯誤定位。

完成條件：設計者能在試跑模式執行欄位連動、計算及非同步 API。

### Phase 3：送出生命週期及 Data Action

- `beforeSubmit`。
- `afterSubmit`。
- `executeDataAction`。
- `onDataActionSuccess`。
- `onDataActionError`。
- Data Action Binding 與 Script 固定執行順序。

完成條件：試跑與實際送出可執行相同的送出前後邏輯。

### Phase 4：正式 Runtime

- 發起表單 Renderer 接入。
- Task Form Renderer 接入。
- Read-only Renderer 接入。
- 流程及 Task Context。
- Runtime Error logging。
- Field Policy 優先順序。

完成條件：已發布 Form Version 的 Script 在正式簽核生命週期中執行，且行為與 Designer Preview 一致。

### Phase 5：編輯體驗補強

- Monaco Editor。
- 自動完成 Context API。
- 格式化。
- Script 範例庫。
- 版本差異比較。
- Console filter 及匯出。

---

## 19. 驗收條件

功能完成至少符合：

1. `FM_PROG005D0001` 有獨立 JavaScript 頁籤。
2. 草稿可編輯並保存完整 Script。
3. 已發布及退役版本 Script 唯讀。
4. 建立新版本會複製 Script。
5. Script 納入發布 SHA-256。
6. 支援 `onFormLoad`、`onFieldChange`、`beforeSubmit`、`afterSubmit` 及 `onDestroy`。
7. 支援 async／await。
8. Context 可讀寫 Form.io submission data。
9. Context 可取得 Form.io component 及執行 redraw。
10. Context 可使用 FlowMint Axios instance。
11. Context 可執行已發布 Data Action。
12. 試跑模式顯示 compile 及 runtime error。
13. `beforeSubmit` 可中止送出並顯示自訂訊息。
14. Designer Preview 與正式 Runtime 使用同一 Executor。
15. 正式 Runtime 固定載入流程所引用的 Form Version Script。
16. 前端 production build 通過。
17. 後端 compile、Mapper XML parse 及相關測試通過。
18. `git diff --check` 通過。

---

## 20. 建議決策

`FM_PROG005D0001` 應同時保留兩種 JavaScript 能力：

1. Form.io 元件級 JavaScript：適合單一欄位的預設值、計算、條件及驗證。
2. FlowMint 表單級 JavaScript：適合跨欄位、跨明細、生命週期、非同步 API 及共用函式。

表單級 JavaScript 採「版本欄位 + 單一 Script Module + 固定生命週期 + 共用 Executor」方案。Script 必須與 Schema、UI Schema 一起隨 Form Version 保存，並由 Designer Preview、發起表單、簽核任務及唯讀畫面使用同一套 Runtime。

此方案可讓表單設計者完成進階功能，又不需要每張表單另外建立 Vue 程式或重新部署 FlowMint 前端。

## 21. 第一版實作狀態

截至 2026-08-19，已完成 Phase 1、Phase 2、發起表單 Runtime，以及簽核任務 Renderer 的核心接線：

- 新增 migration：FM_PROG005D0001-custom-script-schema.sql。
- 新增 fm_form_version.CUSTOM_SCRIPT_CONTENT。
- 完成 Entity、Mapper、Command／View DTO。
- 完成草稿保存、版本複製、發布鎖定及 Script Hash。
- 完成 JavaScript 編輯頁籤、範本與 compile validation。
- 完成共用 Script Executor、Axios、Data Action helper 及試跑 Console。
- Designer Preview 已接入 onFormLoad、onFieldChange 與 onDestroy。
- Executor 已提供 beforeSubmit、afterSubmit、onDataActionSuccess 與 onDataActionError 執行介面。
- Workspace 發起表單已重用 `useFormCustomJavascript`，接入 `RUNTIME_START`、onFormLoad、onFieldChange、beforeSubmit、afterSubmit 與 onDestroy。
- Task 表單已重用同一 Executor，接入 `RUNTIME_TASK`、onFormLoad、onFieldChange 與 onDestroy，並與 Data Action Bridge 共存。

尚待補強的 Runtime 範圍：

- Read-only 申請追蹤已接入 `READ_ONLY` onFormLoad，script helper 在此模式只允許 QUERY Data Action。
- Workspace 正式送出與 Task Action 已接入 beforeSubmit／afterSubmit；beforeSubmit 修改會進入最終 payload，後端成功後的 afterSubmit 錯誤只警告，不回滾或誤報交易失敗。
- Data Action Binding 與直接 helper 已接入 onDataActionSuccess／onDataActionError；success hook 失敗不推翻成功 Action。
- Runtime context 已補 actionType、taskId、formData 等欄位，FIELD_POLICY 亦已完成前端套用與後端防竄改。
- 尚待處理非同步 hook 卸載競態、`onFieldChange` 事件遺失、lifecycle timeout 與相關自動化測試；詳見 [25-2026-08-19 待處理問題與明日接續](25-2026-08-19待處理問題與明日接續.md)。

Custom JavaScript 未採 iframe、Web Worker 或獨立 realm 做真正執行隔離，依 2026-08-19 產品決策屬受信任管理者的客製擴充模型，不列為 bug。`READ_ONLY` 代表表單模式，不代表 JavaScript sandbox；後端權限、tenant 邊界、Data Action 類型限制與 audit 仍須獨立強制執行。

正式 Renderer 已重用 `useFormCustomJavascript`，不得另建第二套 Script Executor。後續補強 Read-only、Task Action 與 Runtime 專屬 Context 時，也必須延伸現有共用介面。
