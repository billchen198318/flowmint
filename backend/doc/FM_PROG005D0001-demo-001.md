# FM_PROG005D0001 Data Action Demo 001 詳細規劃

## 1. 文件目的

本文件規劃一個可在 `FM_PROG005D0001` 實際操作的 Form.io Demo 表單，示範如何由表單事件呼叫 `FM_PROG006D0002` 配置並發布的共用 Data Action API。

Demo 的第一個目標不是一次完成完整的視覺化 Data Action Binding Designer，而是先建立一條安全、可重用且能逐步擴充的端到端路徑：

```text
FM_PROG006D0002 已發布 Data Action
  → FM_PROG005D0001 Demo 表單 Binding 設定
  → Form.io 試跑模式觸發事件
  → FlowMint 共用 Data Action Executor
  → POST /api/fm/data-actions/{actionCode}/execute
  → Response Mapping 回填 Form.io 欄位
```

完成後，使用者應能從 `FM_PROG005D0001` 找到 Demo、查看表單設計、切換試跑模式、按下按鈕，並看到目前登入者的員工資料自動回填。

---

## 2. 現況盤點

### 2.1 FM_PROG005D0001

目前 `FM_PROG005D0001` 已具備：

- 表單主檔 `fm_form_def`。
- 表單版本 `fm_form_version`。
- Form.io Builder。
- 草稿儲存。
- 建立下一版草稿。
- 發布版本。
- 已發布版本唯讀渲染。
- `SCHEMA_CONTENT` 保存 Form.io schema。
- `UI_SCHEMA_CONTENT` 保存 renderer metadata，目前主要內容為：

```json
{
  "engine": "FORMIO",
  "version": 1
}
```

目前尚未具備：

- 草稿的獨立「試跑表單」模式。
- Data Action 選擇器。
- Form.io event 與 Data Action 的 binding。
- Request Mapping。
- Response Mapping。
- 共用的表單 Data Action executor。
- Data Action 執行 loading、錯誤及成功狀態呈現。

### 2.2 FM_PROG006D0002

目前已具備：

- Data Action 主檔、版本及 SQL Step。
- 草稿、Preview、發布與版本鎖定。
- 正式共用 Execute API。
- 已發布 Action options API。
- Metadata API。
- Tenant membership 驗證。
- Server Context 參數，例如 `tenantId`、`loginAccount`、`now`。

正式 Execute API：

```http
POST /api/fm/data-actions/{actionCode}/execute
X-FlowMint-Tenant: {tenantId}
X-FlowMint-Action-Version: {versionNo}   # optional
Content-Type: application/json
```

第一版 Demo 可直接使用 SQL seed 內已發布的：

```text
Tenant：A01
Action Code：FM_GET_CURRENT_EMPLOYEE
Action Type：QUERY
Published Version：1
Request Body：{}
Result Key：employee
```

此 Action 會依後端提供的 `tenantId` 與 `loginAccount` 查詢目前登入者的員工資料，不需要由表單傳入帳號，因此適合作為第一個安全、低副作用 Demo。

---

## 3. Demo 功能定義

### 3.1 表單主檔

建議資料：

| 欄位 | 建議值 |
|---|---|
| TENANT_ID | `A01` |
| FORM_CODE | `FLOWMINT_DATA_ACTION_DEMO` |
| FORM_NAME | `Data Action Demo－目前登入者資料` |
| VERSION_NO | `1` |
| VERSION_STATUS | `DRAFT` |
| STATUS | `DRAFT` |
| DESCRIPTION | `示範 Form.io 呼叫 FlowMint Data Action 並回填欄位` |

Demo 初始建立為 `DRAFT`，讓管理者可以先在 `FM_PROG005D0001` 查看、修改及試跑；不應由 seed 直接發布，避免部署 SQL 自動改變正式可用表單版本。

### 3.2 Demo 畫面欄位

表單建議包含：

1. 說明區塊。
2. 「讀取目前登入者」按鈕。
3. 員工 ID，唯讀。
4. 員工編號，唯讀。
5. 登入帳號，唯讀。
6. 顯示名稱，唯讀。
7. Email，唯讀。
8. 語系，唯讀。
9. 時區，唯讀。
10. API 執行狀態，唯讀。
11. API 錯誤訊息，唯讀或 Alert 呈現。

建議的 Form.io component key：

| 顯示欄位 | Component Key |
|---|---|
| 員工 ID | `employeeId` |
| 員工編號 | `employeeNo` |
| 登入帳號 | `account` |
| 顯示名稱 | `displayName` |
| Email | `email` |
| 語系 | `locale` |
| 時區 | `timezone` |
| 執行狀態 | `dataActionStatus` |
| 錯誤訊息 | `dataActionError` |

### 3.3 Form.io 按鈕事件

按鈕不直接保存任意 JavaScript 或裸 `fetch()`。按鈕只發出一個受控事件名稱，例如：

```text
load-current-employee
```

Form.io Button 建議設定：

```json
{
  "type": "button",
  "key": "loadCurrentEmployee",
  "label": "讀取目前登入者",
  "action": "event",
  "event": "load-current-employee",
  "theme": "primary",
  "block": false,
  "input": true
}
```

事件名稱由 `UI_SCHEMA_CONTENT.dataActions` 對應到允許呼叫的 Data Action，表單 schema 本身不保存 Token、CSRF、API Base URL 或 SQL。

---

## 4. Binding 契約

### 4.1 UI_SCHEMA_CONTENT 擴充

第一版建議沿用 `UI_SCHEMA_CONTENT`，新增 `dataActions`：

```json
{
  "engine": "FORMIO",
  "version": 1,
  "dataActions": [
    {
      "bindingId": "load-current-employee",
      "event": "load-current-employee",
      "actionCode": "FM_GET_CURRENT_EMPLOYEE",
      "actionVersion": 1,
      "requestMapping": {},
      "responseMapping": {
        "employee.employeeId": "employeeId",
        "employee.employeeNo": "employeeNo",
        "employee.account": "account",
        "employee.displayName": "displayName",
        "employee.email": "email",
        "employee.locale": "locale",
        "employee.timezone": "timezone"
      },
      "statusTarget": "dataActionStatus",
      "errorTarget": "dataActionError"
    }
  ]
}
```

### 4.2 Path 基準

後端成功回應的 `response.data.value` 是 `FmDataActionExecutionView`，其業務結果位於：

```text
response.data.value.data
```

因此 `responseMapping` 的來源 path 建議以 execution view 的 `data` 內容為 root：

```text
employee.employeeNo
```

而不是寫成：

```text
response.data.value.data.employee.employeeNo
```

這能讓 Binding JSON 與 Axios response envelope 解耦。

### 4.3 Request Mapping

雖然第一版 Action 的 Request Body 為 `{}`，executor 仍應支援未來擴充：

```json
{
  "requestMapping": {
    "keyword": "submission.keyword",
    "orgUnitId": "submission.orgUnitId"
  }
}
```

規則：

- mapping key 是 API Request Body 欄位。
- mapping value 是 Form.io submission data path。
- 不允許表單覆寫 Server Context 的 `tenantId`、`loginAccount`、`now`。
- 找不到來源 path 時，第一版可傳 `null`，但應在 console/debug 資訊標示；後續可增加 required mapping。

### 4.4 Response Mapping

規則：

- mapping key 是 Data Action `data` root 下的 path。
- mapping value 是 Form.io submission data 的 component key/path。
- 寫入後必須通知 Form.io 重繪及更新 submission。
- 不應直接操作 DOM input value，避免 Form.io internal state 與畫面不同步。

---

## 5. 前端實作規劃

### 5.1 共用型別

建議新增：

```text
frontend-v-nx/types/formDataAction.ts
```

包含：

- `FormDataActionBinding`
- `FormDataActionUiSchema`
- `DataActionExecutionView`
- `DataActionExecutionContext`

最低限度型別：

```ts
export interface FormDataActionBinding {
  bindingId: string;
  event: string;
  actionCode: string;
  actionVersion?: number;
  requestMapping: Record<string, string>;
  responseMapping: Record<string, string>;
  statusTarget?: string;
  errorTarget?: string;
}
```

### 5.2 共用 executor

建議新增：

```text
frontend-v-nx/composables/useFormDataAction.ts
```

責任：

1. 驗證 binding 基本欄位。
2. 從 Form.io submission data 組裝 Request Body。
3. 使用 `getAxiosInstance()` 呼叫共用 API。
4. 帶入 `X-FlowMint-Tenant`。
5. 指定版本時帶入 `X-FlowMint-Action-Version`。
6. 檢查 FlowMint 統一 success flag。
7. 解析 `response.data.value.data`。
8. 執行 Response Mapping。
9. 更新狀態與錯誤欄位。
10. 回傳 execution id、action code、version、rolledBack 及 data，供未來 audit/debug 使用。

Executor 必須使用既有 Axios instance，原因包括：

- 沿用登入狀態。
- 沿用 CSRF header。
- 沿用 refresh token queue。
- 沿用 API base URL。
- 沿用既有錯誤處理慣例。

不建議在表單 JSON 使用：

```js
fetch('/api/fm/data-actions/...')
```

因為這會把認證、CSRF、錯誤處理與 API URL 問題分散到每張表單。

### 5.3 Form.io event bridge

建議新增共用 bridge：

```text
frontend-v-nx/composables/useFormioDataActionBridge.ts
```

輸入：

- Form.io instance。
- Tenant ID。
- parsed UI schema。

行為：

1. 解析 `uiSchema.dataActions`。
2. 對每個 binding 註冊 Form.io event listener。
3. Event 發生時鎖定同一 binding，避免連點重複執行。
4. 將狀態設為 `RUNNING`。
5. 呼叫 `useFormDataAction`。
6. 成功時套用 response mapping 並設為 `SUCCESS`。
7. 失敗時設為 `ERROR`，保留可讀錯誤訊息。
8. Form.io instance destroy 或切換版本時解除 listener。

第一版可採每個 binding 一次只允許一個 in-flight request。

### 5.4 FM_PROG005D0001 試跑模式

修改：

```text
frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue
```

在版本內容區加入模式切換：

```text
設計 | 試跑 | JSON
```

建議行為：

- `設計`：草稿使用 `Formio.builder()`。
- `試跑`：使用 `Formio.createForm()` 渲染目前記憶體中的 schema，不需要先發布。
- `JSON`：保留現有 schema/ui schema 檢視或編輯能力。
- 進入試跑前先執行 `syncSchemaFromDesigner()`。
- 試跑不自動儲存草稿。
- 離開試跑或切換版本時 destroy Form.io instance 並解除 Data Action event。
- 已發布版本可直接使用試跑／唯讀渲染，但是否允許觸發 API 應明確標示；第一版建議允許 QUERY 類型 Demo。

### 5.5 Loading 與錯誤呈現

不建議每次 Data Action 都使用全畫面 Swal loading，否則表單內多個事件會造成操作阻塞。

第一版建議：

- 按鈕執行期間 disabled。
- `dataActionStatus` 顯示 `READY`、`RUNNING`、`SUCCESS`、`ERROR`。
- 錯誤同時顯示 toast。
- `dataActionError` 保存可讀訊息。
- 不在畫面顯示 SQL、Stack Trace 或內部連線資訊。

---

## 6. Seed SQL 規劃

建議新增：

```text
示範 Seed 已合併至 `backend/doc/flowmint.sql`
```

### 6.1 Seed 原則

- 不直接修改既有 `flowmint.sql` 的 dump 區塊作為唯一來源。
- Seed 應可獨立執行。
- Seed 應可重複執行，避免重複建立相同 `FORM_CODE`。
- 使用明確、固定 UUID，方便測試與文件說明。
- 執行前檢查 Tenant `A01` 是否存在。
- 執行前檢查 `FM_GET_CURRENT_EMPLOYEE` 是否存在、狀態為 `ACTIVE`，且具有 `PUBLISHED` version。
- 若 Demo 已存在，建議停止並提示，而不是覆寫使用者後續修改的 schema。
- 不自動發布表單。

### 6.2 建議前置檢查

```sql
SELECT TENANT_ID, STATUS
  FROM fm_tenant
 WHERE TENANT_ID = 'A01';

SELECT a.ACTION_CODE, a.STATUS, a.CURRENT_VERSION_NO, v.VERSION_STATUS
  FROM fm_data_action a
  JOIN fm_data_action_version v
    ON v.TENANT_ID = a.TENANT_ID
   AND v.ACTION_ID = a.ACTION_ID
   AND v.VERSION_NO = a.CURRENT_VERSION_NO
 WHERE a.TENANT_ID = 'A01'
   AND a.ACTION_CODE = 'FM_GET_CURRENT_EMPLOYEE';
```

### 6.3 寫入資料

Seed 寫入：

- 一筆 `fm_form_def`。
- 一筆 `fm_form_version`。
- `SCHEMA_CONTENT` 為完整 Form.io JSON。
- `UI_SCHEMA_CONTENT` 包含 `dataActions` binding。
- `CONTENT_SHA256` 必須依後端既有算法計算，不能任意留錯誤 hash。

需確認後端 `FmFormDefLogicServiceImpl.sha256()` 對 combined content 的精確串接方式後，再產生 seed hash。若無法保證演算法一致，可讓 seed 建立後由 `FM_PROG005D0001` 儲存一次草稿以重新計算，但正式交付建議直接產生正確 hash。

---

## 7. 後端影響評估

第一版原則上不需修改 Data Action backend，因為以下 API 已存在：

```text
POST /api/fm/data-actions/options
POST /api/fm/data-actions/{actionCode}/metadata
POST /api/fm/data-actions/{actionCode}/execute
```

可能需要的後端補強僅限：

1. 若 UI schema publish validation 未驗證 `dataActions`，可加入基本 JSON 結構驗證。
2. 發布表單時可驗證引用的 Action：
   - 屬於同一 Tenant。
   - Action 狀態為 `ACTIVE`。
   - 指定版本為 `PUBLISHED`。
3. 若不指定版本，需在規格上接受 Current Published Version 可能改變；正式流程建議發布表單時固定 Action Version。

Demo 第一版可以先由前端執行，但發布驗證是後續正式化前的重要安全檢查。

---

## 8. 安全邊界

### 8.1 必須遵守

- Tenant ID 由目前表單主檔／執行 context 提供，不從一般 submission 欄位取得。
- API 仍由後端驗證目前登入者的 Tenant membership。
- Data Action 必須已發布。
- 不允許表單 JSON 保存 SQL。
- 不允許表單 JSON 保存 access token、refresh token 或 CSRF token。
- 不允許 Request Mapping 覆寫 `tenantId`、`loginAccount`、`now`。
- Error message 必須使用既有 escape helper 顯示。
- 表單內不執行任意遠端 URL；endpoint 固定由 FlowMint executor 組裝。

### 8.2 QUERY 與 COMMAND

第一版 Demo 僅使用 `QUERY` Action。

後續若允許 `COMMAND` 或 `TRANSACTION`：

- Binding 應保存 action type metadata。
- 畫面需有二次確認能力。
- 防止重複點擊。
- 應考慮 idempotency key。
- 正式 Runtime 應留下 execution audit。
- Designer 試跑異動 Action 時，應使用 Preview API 或清楚提示正式 Execute 會 commit。

在上述規則完成前，試跑模式建議只允許 QUERY。

---

## 9. 驗證計畫

### 9.1 Seed 驗證

- Tenant `A01` 不存在時，seed 不應產生孤兒表單。
- Demo 不存在時可成功建立。
- Demo 已存在時不覆寫。
- `fm_form_def.CURRENT_VERSION_NO = 1`。
- `fm_form_version.VERSION_STATUS = DRAFT`。
- schema 與 ui schema 都是合法 JSON。

### 9.2 Designer 驗證

- Query 頁可找到 `FLOWMINT_DATA_ACTION_DEMO`。
- Edit 頁可正常載入 Builder。
- 表單欄位與按鈕顯示正確。
- 切換設計／試跑不遺失未儲存 schema。
- 切換版本不殘留舊 event listeners。
- JSON 模式可以看到 Data Action binding。

### 9.3 API 成功案例

前置條件：

- 使用 Tenant `A01` 的有效登入帳號。
- 該帳號在 `fm_employee` 有 ACTIVE 資料。

步驟：

1. 進入試跑模式。
2. 按「讀取目前登入者」。
3. 確認按鈕暫時 disabled。
4. 確認 status 依序變成 `RUNNING`、`SUCCESS`。
5. 確認 employee fields 正確回填。
6. 確認沒有 reload 整頁。
7. 再次執行仍可正常更新。

### 9.4 API 失敗案例

- 登入帳號不屬於 A01。
- A01 membership 已停用。
- 員工資料不存在。
- Action 已停用。
- Action 指定版本未發布。
- 網路錯誤。
- Response path 不存在。
- Binding JSON 格式錯誤。

預期：

- status 為 `ERROR`。
- 顯示可讀錯誤。
- 不清除原有表單資料。
- 不顯示 SQL 或 Stack Trace。
- 按鈕解除 disabled，可再次嘗試。

### 9.5 Build 與品質檢查

```text
git diff --check
npm run build
```

若專案有前端測試框架，再加入：

- path getter/setter unit tests。
- request mapping unit tests。
- response mapping unit tests。
- binding validation unit tests。
- concurrent click guard unit tests。

---

## 10. 分階段交付

### Phase 1：可操作 Demo

交付：

- Demo seed SQL。
- Form.io Demo schema。
- UI schema Data Action binding。
- 共用 executor。
- Form.io event bridge。
- FM_PROG005D0001 試跑模式。
- 成功及錯誤狀態顯示。
- Build 驗證。

驗收結果：管理者可在 `FM_PROG005D0001` 實際按按鈕，透過正式共用 API 取得目前登入員工資料並回填表單。

### Phase 2：視覺化 Binding Editor

交付：

- 已發布 Data Action 下拉選單。
- Metadata 自動載入。
- Request Mapping UI。
- Response Mapping UI。
- Event 選擇。
- Mapping validation。
- 表單發布前引用驗證。

### Phase 3：正式 Runtime 整合

交付：

- Workflow task form renderer 共用 bridge。
- 表單載入事件。
- 欄位變更事件 debounce。
- COMMAND／TRANSACTION confirmation。
- Audit、idempotency 及權限政策。
- 固定 Published Action Version。

---

## 11. 預計修改檔案

### 新增

```text
示範 Seed 已合併至 `backend/doc/flowmint.sql`
frontend-v-nx/types/formDataAction.ts
frontend-v-nx/composables/useFormDataAction.ts
frontend-v-nx/composables/useFormioDataActionBridge.ts
```

### 修改

```text
frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue
```

### 視驗證結果決定是否修改

```text
backend/app/src/main/java/org/qifu/fm/logic/impl/FmFormDefLogicServiceImpl.java
```

只有在加入 UI schema binding validation 或發布時 Data Action reference validation 時才修改後端。

---

## 12. 完成定義

Demo 001 完成必須同時符合：

1. 可由 seed 建立且不覆寫既有 Demo。
2. 可在 `FM_PROG005D0001` 看見並編輯。
3. 草稿具有試跑模式。
4. 表單 JSON 不含 Token、CSRF、SQL 或任意 API URL。
5. 按鈕可呼叫 `FM_GET_CURRENT_EMPLOYEE`。
6. Request 使用 Tenant `A01` context。
7. 回應可正確寫回 Form.io submission data。
8. 成功、失敗與重複點擊都有合理行為。
9. 表單可儲存草稿與發布。
10. `git diff --check` 與 `npm run build` 通過。

---

## 13. 建議決策

建議採用本文件的「描述式 Binding + 共用 Executor」方案，不採用 Form.io Custom JavaScript 直接 `fetch()`。

原因：

- 認證與 CSRF 集中管理。
- API URL 不散落於表單 JSON。
- Tenant context 不由使用者輸入控制。
- Mapping 可以驗證。
- 後續能建立視覺化 Editor。
- 同一套 bridge 能在 Designer、Preview 與 Workflow Runtime 重用。
- 未來可對 COMMAND／TRANSACTION 增加一致的確認、audit 與 idempotency 政策。

此方案能讓 Demo 先落地，同時不把一次性示範變成後續難以維護的技術債。
