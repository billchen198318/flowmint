# 32 外部系統 API 管理與流程拋單規劃

日期：2026-08-28  
狀態：設計規劃  
程式代碼：`FM_PROG010D0002`  
程式名稱：外部系統 API 管理

## 1. 目的與邊界

本功能讓管理員為 MES、ERP、HR 或其他受信任系統建立 Tenant-scoped API Client 與 API Key，
使外部系統可以查詢 FlowMint 組織／人員／流程／表單契約，並以受控身分發起正式簽核流程。

API Key 代表「外部系統」，不是某位使用者的登入 Token，也不能取得一般 UI Session。外部系統
提出發單人與申請人後，FlowMint 仍須驗證 Tenant membership、在職狀態、流程起單政策及外部
Client 的授權範圍；不得因持有 Key 就能任意冒用帳號發單、簽核、查看待辦或讀取歷史表單。

本規劃保留需求中的 13 組能力，全部業務端點使用 `POST`。為避免語意混淆，本文作以下調整：

- 「帳號 ID」統一使用 QIFU4 穩定登入識別 `account`，不使用資料庫 OID。
- 「部門 ID」使用 FlowMint 穩定 `orgUnitId`，另回傳 `orgUnitCode`。
- 發單人部門不是授權資料。若外部系統傳入，後端只把它當期望值並與有效任職比對；正式
  `applicantOrgUnitId` 由 FlowMint 解析或驗證後保存。
- 外部 API Client 是實際技術發起者；`initiatorAccount` 是業務發起人；`applicantAccount`
  是申請人。三者分開保存於稽核資料。
- 流程可綁定多個 Form Version，但起單只提交該發布流程的 Start Form；後續 Task Form 由
  Runtime 節點決定，外部系統不得一次替未來簽核人填寫。

## 2. UI 功能

### 2.1 主畫面

本程式與既有 AI Provider 管理共用 `FM_PROG010D` Program Family。Folder 顯示名稱
統一為「FJ. API-整合服務」，程式結構如下：

```text
FJ. API-整合服務                          FM_PROG010D（FOLDER）
├─ FJ01 - AI Provider 管理                FM_PROG010D0001Q（既有 ITEM）
└─ FJ02 - 外部系統 API 管理               FM_PROG010D0002Q（新增 ITEM）
```

選單 URL 與前端實體目錄固定為：

```text
Query URL       #/fm_prog010d0002
Create URL      #/fm_prog010d0002/create
Edit URL        #/fm_prog010d0002/edit/{clientId}
API 說明 URL    #/fm_prog010d0002/docs

C:\home\flowmint\frontend-v-nx\pages\fm_prog010d0002\
├── index.vue
├── create.vue
├── edit\
│   └── [id].vue
├── docs.vue
├── config.ts
├── QueryPageStore.ts
└── components\
    ├── ApiClientForm.vue
    ├── ApiKeyOneTimeDialog.vue
    ├── ApiKeyRotateDialog.vue
    └── ApiAccessLogDialog.vue
```

`docs.vue` 由 Query 頁的「API 說明」按鈕進入，不另外建立左側選單 Item；它仍須檢查
`FM_PROG010D0002Q`。Create／Edit 頁依 QIFU4 慣例註冊實際 Page Item，撤銷、輪替、停用、
下載 OpenAPI 等 Controller Command 只作權限，不為每個按鈕建立選單項目。

部署時新增可重複執行的 `backend/doc/FM_PROG010D0002-register.sql`。該 SQL 先安全更新既有
`FM_PROG010D` Folder 顯示名稱，再註冊 `FM_PROG010D0002` 實際 Query／Create／Edit Page；不得
重建或改號既有 `FM_PROG010D0001`，也不自動授予任何角色。權限由 QIFU4 管理配置給系統整合
管理員。

查詢條件：Tenant、Client Code、名稱、狀態、Key 狀態及最後使用日期。Grid 顯示：

- Client Code、名稱、用途及所屬系統類型。
- Tenant、狀態、Scopes、IP Allowlist。
- Key Prefix／末四碼、建立日、到期日、最後使用日及最後來源 IP。
- Rate Limit、建立人及更新人。

操作按鈕：新增 Client、編輯、產生 Key、輪替 Key、撤銷 Key、停用 Client、檢視稽核，以及
固定顯示的「API 說明」。

權限建議：

| 權限 | 用途 |
| --- | --- |
| `FM_PROG010D0002Q` | 查詢 Client 及遮罩後 Key 資訊 |
| `FM_PROG010D0002A` | 建立 Client、產生第一把 Key |
| `FM_PROG010D0002E` | 修改名稱、Scopes、IP、期限及配額 |
| `FM_PROG010D0002D` | 撤銷 Key、停用 Client |
| `FM_PROG010D0002X` | 輪替 Key、查看高風險稽核及測試發單 |

### 2.2 API Key 產生與輪替

Key 建議格式：

```text
fmk_live_{keyId}.{base64urlRandomSecret}
```

- Secret 使用 CSPRNG 產生至少 256 bit 隨機值。
- 完整 Key 只在建立或輪替成功後顯示一次，並提供一次性複製；關閉視窗後不可查回。
- 資料庫只保存 `KEY_ID`、Prefix、末四碼及 Secret Hash，不保存明文或可逆密文。
- 高熵隨機 Secret 可使用 SHA-256/HMAC-SHA-256 與 server-side pepper 驗證；比較固定時間化。
- Key 可設定生效日、到期日、IP/CIDR Allowlist、Scopes、每分鐘與每日配額。
- 輪替時可設定最長 24 小時重疊期，舊 Key 到期後自動撤銷；立即撤銷不提供復原。
- Key 不得出現在 URL、query string、response body（首次顯示除外）、application log 或稽核明細。

### 2.3 API 說明畫面

主畫面「API 說明」進入 `/fm/api-clients/docs`，內容不是外部 Swagger 公開站，而是受 QIFU4
權限保護的內部說明頁。至少包含：

- Base URL、版本、認證 Header、共同 Header、時區及編碼。
- 每支 API 的用途、所需 Scope、Request／Response 欄位表、範例 JSON。
- 必填、長度、格式、enum、分頁與有效時間規則。
- 200／400／401／403／404／409／422／429／500 錯誤範例。
- Idempotency、重送、Timeout、Rate Limit、Key 輪替與撤銷說明。
- Form.io Template 與 Submission JSON 的對照範例。
- 可下載的 OpenAPI 3.1 JSON，但所有 path 仍定義為 `post`。
- 「複製 cURL」功能必須使用 `{YOUR_API_KEY}` placeholder，絕不把管理畫面目前 Key 自動帶入。

文件由後端 DTO/OpenAPI 契約產生或以契約測試保證同步，不能手工維護一套已過期欄位說明。

## 3. 資料模型

### 3.1 `fm_api_client`

| 欄位 | 說明 |
| --- | --- |
| `TENANT_ID` | 唯一資料隔離範圍 |
| `CLIENT_ID` | 穩定 UUID |
| `CLIENT_CODE` | Tenant 內唯一，例如 `ERP_PROD` |
| `CLIENT_NAME` | 顯示名稱 |
| `SYSTEM_TYPE` | `ERP`、`MES`、`HR`、`OTHER` |
| `DESCRIPTION` | 用途及責任人 |
| `STATUS` | `ACTIVE`、`INACTIVE` |
| `ALLOWED_SCOPES` | 受控 Scope 集合，不接受任意字串 |
| `ALLOWED_PROCESS_IDS` | 可發起流程白名單；空值不是自動代表全部 |
| `ALLOWED_INITIATOR_ACCOUNTS` | 可代表的業務發起人規則或受控白名單 |
| `IP_ALLOWLIST` | IPv4／IPv6 CIDR；正規化後保存 |
| `RATE_LIMIT_PER_MINUTE` | 每分鐘限制 |
| `DAILY_QUOTA` | 每日限制 |
| `LOCK_VERSION` | 樂觀鎖 |
| Audit 欄位 | 建立、修改帳號與時間 |

流程與帳號白名單資料量大時應正規化為關聯表，不能以超長 CSV 實作。第一版可使用受控 JSON，
但必須有 schema validation、索引策略與明確上限。

### 3.2 `fm_api_client_key`

保存 Client、Key ID、Prefix、末四碼、Secret Hash、生效／到期／撤銷時間、撤銷原因、最後使用
時間、最後來源 IP、失敗次數及 Lock Version。一個 Client 可在輪替重疊期間有兩把有效 Key。

### 3.3 `fm_api_access_log`

append-only 記錄：Tenant、Client、Key ID、Request ID、Trace ID、Endpoint Code、Scope、來源 IP、
User-Agent 摘要、HTTP Status、Result Code、耗時、Request／Response bytes、Idempotency Key Hash、
業務流程 ID／Business Key（如有）及時間。

不得記錄完整 API Key、Form Submission、個資欄位值、附件內容或完整 response。需要調查發單時，
以正式 Form Snapshot 與流程 Action 為準。

## 4. 認證與共同契約

Base path：`/api/fm/external/v1`。

共同 Header：

| Header | 必填 | 說明 |
| --- | --- | --- |
| `Authorization: Bearer {apiKey}` | 是 | API Key 只放 Header |
| `Content-Type: application/json` | 是 | UTF-8 JSON |
| `X-Request-Id` | 建議 | 呼叫端追蹤 ID；缺少時由伺服器產生 |
| `Idempotency-Key` | 發單必填 | 8～128 字；Client 範圍內唯一 |

Tenant 不接受 body 或 `X-FlowMint-Tenant` 指定，固定從 API Client 解析，避免一把 Key 跨 Tenant。

所有 Request 使用共同 envelope：

```json
{
  "requestTime": "2026-08-28T10:30:00+08:00",
  "data": {}
}
```

成功 Response：

```json
{
  "success": true,
  "requestId": "req-uuid",
  "data": {}
}
```

錯誤 Response：

```json
{
  "success": false,
  "requestId": "req-uuid",
  "error": {
    "code": "ORG_UNIT_NOT_FOUND",
    "message": "The requested resource was not found.",
    "fieldErrors": []
  }
}
```

跨 Tenant、無權限及不存在資料對外統一回 404，避免探測其他 Tenant。認證失敗為 401、Scope／
IP 禁止為 403、欄位驗證為 400、狀態或冪等衝突為 409、Resolver／流程無法發起為 422、限流為
429 並回 `Retry-After`。錯誤不得洩漏 SQL、類別名稱、Stack Trace 或 Key 驗證細節。

## 5. Scope

| Scope | 能力 |
| --- | --- |
| `org.department.read` | 部門詳細、主管、員工、父部門及組織樹 |
| `org.employee.read` | 員工、簽核 Level、直屬主管及任職部門 |
| `design.process.read` | 已發布流程及 Form Binding |
| `design.form.read` | 已發布 Form Template JSON |
| `runtime.request.submit` | 發起白名單內的已發布流程 |

Scope 只縮小權限，不能取代 Tenant、流程白名單、發起人白名單、起單政策與資料狀態驗證。

## 6. 組織與人員 API

所有查詢以 `effectiveAt` 判斷有效版本，省略時使用伺服器現在時間；時間格式 ISO-8601。

### 6.1 部門詳細資料

`POST /org/departments/detail`，Scope：`org.department.read`

Request `data`：`orgUnitId`（必填）、`effectiveAt`（選填）。

Response `data`：`orgUnitId`、`orgUnitCode`、中英文名稱（存在才回）、`orgUnitType`、`status`、
`parentOrgUnitId`、`path`、`levelDepth`、`sortNo`、`effectiveFrom`、`effectiveTo`。不回資料庫 OID。

### 6.2 部門主要主管

`POST /org/departments/primary-head`，Scope：`org.department.read`

Request：`orgUnitId`、`effectiveAt`。Response：部門摘要及 `head`；主管包含 `account`、employeeId、
姓名、主管類型、有效期間。`PRIMARY` HEAD 不存在時 `head=null` 並回受控 warning，不以副主管冒充。

### 6.3 部門員工清單與人數

`POST /org/departments/employees`，Scope：`org.department.read`

Request：`orgUnitId`、`effectiveAt`、`includeSubtree=false`、`primaryOnly=true`、`page=1`、
`pageSize=50`（最大 200）、`status=ACTIVE`。

Response：部門摘要、`totalElements`、`totalPages`、`page`、`pageSize`、`items`。每位員工只回
必要欄位：account、employeeId、姓名、職稱、簽核 Level、是否主要任職及有效期間。預設不展開
子部門，避免「本部門員工數」語意不清。

### 6.4 人員詳細資料

`POST /org/employees/detail`，Scope：`org.employee.read`

Request：`account`、`effectiveAt`。Response：account、employeeId、顯示姓名、在職狀態、有效期間、
公司 Email（若資料政策允許）、主要任職摘要。預設不回私人電話、地址、證件或薪資資料。

### 6.5 人員簽核 Level

`POST /org/employees/approval-level`，Scope：`org.employee.read`

Request：`account`、`effectiveAt`、選填 `orgUnitId`。Response：account、orgUnitId、schemeId、
`approvalLevelId`、levelCode、levelName、levelOrder、來源任職。沒有 Level 時回 `level=null`，不猜測。

若員工有多筆有效任職且未指定 orgUnitId，固定使用 Primary Assignment；沒有唯一 Primary 時回
409 `PRIMARY_ASSIGNMENT_AMBIGUOUS`。

### 6.6 人員直屬主管

`POST /org/employees/direct-manager`，Scope：`org.employee.read`

Request：`account`、`effectiveAt`、選填 `orgUnitId`。Response：員工任職摘要及 `manager`。此 API
優先回 `fm_employee_org_assignment.DIRECT_MANAGER_ACCOUNT` 的有效直屬主管，不等同部門 HEAD；
未設定時是否 fallback 部門主管由 `fallbackToOrgHead` 明確控制，預設 `false`。

### 6.7 人員部門

`POST /org/employees/departments`，Scope：`org.employee.read`

Request：`account`、`effectiveAt`、`primaryOnly=false`。Response：所有有效任職清單，包含 orgUnitId、
名稱、職稱、Level、primaryFlag 及有效期間；呼叫端不可假設員工永遠只有一個部門。

### 6.8 人員主要部門主管

`POST /org/employees/primary-department-head`，Scope：`org.employee.read`

Request：`account`、`effectiveAt`。後端先解析唯一 Primary Assignment，再查該部門 PRIMARY HEAD。
Response 同時回員工、主要部門及主管摘要；不得把 `direct-manager` 的結果混用。

### 6.9 人員主要部門的上一層部門

`POST /org/employees/parent-department`，Scope：`org.employee.read`

Request：`account`、`effectiveAt`。Response：主要部門及 `parentDepartment`。根部門的 parent 為 null；
若要取得完整祖先鏈，使用選填 `includeAncestors=true`，按目前部門父層至根節點排序。

### 6.10 全部組織部門

`POST /org/departments/tree`，Scope：`org.department.read`

Request：`effectiveAt`、`rootOrgUnitId`（選填）、`includeInactive=false`、`format=TREE|FLAT`、分頁參數
（FLAT 時使用）。Response 只包含 API Client Tenant 內的有效組織。TREE 模式應設定最大節點數；
超過時要求使用 FLAT 分頁，不提供無上限全表輸出。

## 7. 流程與表單 API

### 7.1 流程的 Form Binding

`POST /design/processes/forms`，Scope：`design.process.read`

Request：

```json
{
  "data": {
    "processDefId": "FM_PURCHASE_APPROVAL",
    "versionNo": null
  }
}
```

省略 versionNo 時只取目前 `PUBLISHED` 版本；不得讀 DRAFT。Response 回 processDefId、processName、
versionNo、deployment 狀態，以及去重後 Form 清單：formId、formName、formVersionNo、schemaHash、
bindingUsage（`START`／`USER_TASK`）、taskDefinitionKeys、fieldPolicy 摘要。只有 `START` Form 可用於發單。

### 7.2 Form Template JSON

`POST /design/forms/template`，Scope：`design.form.read`

Request：`formId`、選填 `versionNo`。省略 versionNo 時取目前 PUBLISHED 版本。Response：formId、
formName、versionNo、schemaType=`FORM_IO`、schemaHash、`templateJson`、submissionContract、systemFields、
附件欄位限制及 publishedAt。

`templateJson` 是已發布 Form.io Schema，不含 Custom JavaScript 原始碼、Data Action SQL、API Key、
資料庫設定或未發布版本。`submissionContract` 列出可提交 field key、型別、required、enum、array／
object 結構及 server-managed 欄位，供外部系統產生合法 JSON。Schema Hash 可用於防止以舊模板發單。

## 8. 發單 API

### 8.1 Endpoint

`POST /runtime/requests/submit`，Scope：`runtime.request.submit`

必填 `Idempotency-Key` Header。Request：

```json
{
  "requestTime": "2026-08-28T10:30:00+08:00",
  "data": {
    "processDefId": "FM_BUSINESS_CARD_APPROVAL",
    "processVersionNo": 1,
    "formId": "FM_BUSINESS_CARD_REQUEST",
    "formVersionNo": 1,
    "formSchemaHash": "sha256-hex",
    "submission": {
      "requestType": "NEW"
    },
    "initiatorAccount": "erp-service-owner",
    "applicantAccount": "fm00123",
    "applicantOrgUnitId": "ORG-SALES",
    "externalReference": {
      "sourceSystem": "ERP",
      "sourceDocumentType": "EMPLOYEE_REQUEST",
      "sourceDocumentNo": "ERP-2026-000123"
    },
    "remark": "Created by ERP integration"
  }
}
```

### 8.2 欄位語意

- `processDefId` 必須在 Client 流程白名單內且目前可發起。
- 建議傳 `processVersionNo`、`formVersionNo` 與 `formSchemaHash`，避免外部系統用舊契約送到新版；
  不傳時使用目前 Published，但 Response 必須明確回實際版本。
- `submission` 只包含 Start Form 的業務欄位；Tenant、單據編號、流程狀態、系統欄位及 Actor
  不接受外部覆寫。
- `initiatorAccount` 是外部系統代表的業務發起人，必須是 Client 允許帳號、有效員工及有效
  Tenant membership。若企業不需要此代理語意，可為每個 Client 設定固定 Service Initiator。
- `applicantAccount` 省略時等於 initiator；不同時必須同時通過 Client impersonation policy、
  流程起單政策及既有 `fm_process_start_proxy`，或建立明確的 `EXTERNAL_SYSTEM` 起單授權模型。
  不得只因 Client 有 submit scope 就跳過代起單規則。
- `applicantOrgUnitId` 建議必填以消除多重任職歧義，但後端必須驗證是申請人當下有效任職；
  省略時只允許存在唯一 Primary Assignment。
- `externalReference` 建議必填，`sourceSystem + sourceDocumentType + sourceDocumentNo` 在 Tenant／Client
  範圍建立唯一索引或查重，作為跨系統對帳鍵；不能取代 Idempotency-Key。
- 附件不接受 Base64。若需外部附件，另規劃「建立 Upload Session → 串流上傳 → 發單綁定」API，
  或先不開放附件型流程；不得接受伺服器任意 URL 抓檔以免 SSRF。

### 8.3 後端驗證順序

1. 驗證 Key 狀態、期限、固定時間 Hash、Tenant、IP、Scope、Rate Limit 及每日配額。
2. 驗證 Idempotency-Key；相同 Key＋相同 payload 回原結果，相同 Key＋不同 payload 回 409。
3. 驗證 Client 的流程、initiator、applicant 與外部來源白名單。
4. 驗證流程及 Start Form 都已發布，版本、Form Binding 與 Schema Hash 一致。
5. 驗證 initiator／applicant 的 QIFU4 Account、Employee、Tenant membership、有效期間及起單政策。
6. 驗證 applicantOrgUnitId 是有效任職；正式部門資料從 FlowMint 取得，不照抄外部名稱。
7. 執行 Form.io Submission、未宣告欄位、型別、required、附件及 Custom JavaScript server contract
   驗證；外部 API 不執行瀏覽器 JavaScript。
8. 執行 Data Action `BEFORE_SUBMIT`、單據編號、Flowable 啟動、Resolver、Snapshot、Action 與通知。
9. 保存 API Client、Key ID、initiator、applicant、外部參考及 Request ID 稽核。

### 8.4 成功 Response

```json
{
  "success": true,
  "requestId": "req-uuid",
  "data": {
    "processInstanceId": "flowmint-process-uuid",
    "flowableProcessInstanceId": "flowable-id",
    "businessKey": "business-uuid",
    "documentNumber": "BC-A01-202608-000001",
    "formDataId": "form-data-uuid",
    "processDefId": "FM_BUSINESS_CARD_APPROVAL",
    "processVersionNo": 1,
    "formId": "FM_BUSINESS_CARD_REQUEST",
    "formVersionNo": 1,
    "status": "RUNNING",
    "submittedAt": "2026-08-28T10:30:01+08:00",
    "idempotentReplay": false
  }
}
```

Resolver 找不到人時沿用平台 Incident 契約，保留流程與異常證據並回 422 或明確 `INCIDENT` 狀態；
不能回成功卻讓外部系統誤認已正常派送。

### 8.5 已發單流程狀態查詢 API

`POST /runtime/requests/status`，Scope：`runtime.request.read`

此 API 供 ERP、MES、HR 在發單成功後進行狀態對帳。預設只能查詢「同一 API Client 發起」的
流程；若確實需要跨 Client 查詢，必須另授予高權限 `runtime.request.read.all`，並限制流程白名單，
不能因知道 Business Key、單據編號或 Process Instance ID 就取得資料。

每次 Request 必須且只能提供一組查詢條件：

```json
{
  "requestTime": "2026-08-28T11:00:00+08:00",
  "data": {
    "processInstanceId": null,
    "businessKey": null,
    "documentNumber": null,
    "externalReference": {
      "sourceSystem": "ERP",
      "sourceDocumentType": "EMPLOYEE_REQUEST",
      "sourceDocumentNo": "ERP-2026-000123"
    },
    "includeTimeline": false
  }
}
```

查詢識別規則：

- `processInstanceId`：FlowMint 對外流程實例 ID；不建議外部系統依賴 Flowable ID。
- `businessKey`：FlowMint 不可變 Business Key。
- `documentNumber`：已設定 Document Type 的流程可使用；仍限制 Tenant、Client 及流程白名單。
- `externalReference`：最建議的跨系統對帳方式，三個欄位必須完整且精確相符。
- 四種條件不得同時提供；全部空白或同時提供多組時回 400 `STATUS_LOOKUP_KEY_INVALID`。
- 找不到、跨 Tenant、非此 Client 建立或不在流程白名單內，一律回 404。

成功 Response：

```json
{
  "success": true,
  "requestId": "req-uuid",
  "data": {
    "processInstanceId": "flowmint-process-uuid",
    "businessKey": "business-uuid",
    "documentNumber": "BC-A01-202608-000001",
    "externalReference": {
      "sourceSystem": "ERP",
      "sourceDocumentType": "EMPLOYEE_REQUEST",
      "sourceDocumentNo": "ERP-2026-000123"
    },
    "processDefId": "FM_BUSINESS_CARD_APPROVAL",
    "processName": "公司名片申請流程",
    "processVersionNo": 1,
    "formId": "FM_BUSINESS_CARD_REQUEST",
    "formVersionNo": 1,
    "initiatorAccount": "fm00001",
    "applicantAccount": "fm00123",
    "applicantOrgUnitId": "ORG-SALES",
    "status": "RUNNING",
    "statusLabel": "簽核中",
    "submittedAt": "2026-08-28T10:30:01+08:00",
    "lastChangedAt": "2026-08-28T10:31:12+08:00",
    "completedAt": null,
    "activeTasks": [
      {
        "taskDefinitionKey": "departmentManagerApproval",
        "taskName": "部門主管審核",
        "assignmentState": "ASSIGNED",
        "startedAt": "2026-08-28T10:30:02+08:00",
        "dueAt": "2026-08-30T10:30:02+08:00"
      }
    ],
    "incident": null,
    "timeline": null
  }
}
```

狀態代碼固定使用平台正式狀態，不以中文文字作程式判斷：

| 狀態 | 意義 | 終態 |
| --- | --- | --- |
| `RUNNING` | 流程執行中，可能有一或多個 active Task | 否 |
| `COMPLETED` | 全部流程正常完成 | 是 |
| `REJECTED` | 簽核駁回並終止 | 是 |
| `CANCELLED` | 申請人撤回或實際發起人取消 | 是 |
| `TERMINATED` | 營運管理員終止 | 是 |
| `INCIDENT` | 流程仍存在，但指派或 Runtime 異常待處理 | 否 |

若現有資料模型使用 `RUNNING` 搭配 OPEN Assignment Incident，API 層可計算回傳 `INCIDENT`，但必須
同時保留 `runtimeStatus=RUNNING` 與受控 Incident 摘要，不能直接改寫資料庫正式狀態。Incident 摘要
只回 `incidentId`、errorCode、taskDefinitionKey、openedAt 及 operationsStatus，不回 Context JSON、
Stack Trace 或內部錯誤訊息。

`activeTasks` 可以有多筆，以支援 ALL 會簽及平行加簽。預設不回 assignee 帳號、候選群組或簽核
意見，避免外部系統用流程狀態 API 取得不必要的人員資料；若業務確實需要，另設
`runtime.request.task-participant.read` Scope，不可隨 `runtime.request.read` 自動開放。

當 `includeTimeline=true` 時，Response 可回受控狀態時間線：SUBMIT、TASK_CREATED、APPROVE、
RETURN、RESUBMIT、REJECT、WITHDRAW、CANCEL、TERMINATE、COMPLETE 及 INCIDENT_OPENED／RESOLVED。
時間線只回 actionType、taskName、occurredAt、result，不回 Form Snapshot、簽核意見、附件或完整
Actor 個資。Timeline 最多 200 筆，超過時回 `timelineTruncated=true`；完整稽核不是此外部 API 的用途。

狀態查詢必須直接依 FlowMint Runtime Index、active Flowable Task 與 Assignment Incident 組合，不得
只相信外部系統前次保存的狀態。查詢不改變流程、不 claim Task、不產生業務 Action，只寫入
`fm_api_access_log`。Response 設定 `Cache-Control: no-store`。

### 8.6 狀態查詢的批次與輪詢規則

第一版先提供單筆查詢。ERP／MES／HR 不得高頻逐秒輪詢；API 說明頁應建議執行中流程最短間隔
30 秒，終態停止輪詢，遇 429 依 `Retry-After` 退避。

第二階段可增加 `POST /runtime/requests/status/batch`，單次最多 100 筆 External Reference，逐筆回
成功或錯誤結果，且整批仍受 Client／Endpoint Rate Limit。批次 Request 不接受 Business Key、單據
編號與 External Reference 混用，避免不明確配對。

後續若需要即時同步，可另規劃簽章 Webhook；Webhook 不能取代狀態查詢，接收端仍應以本 API
進行最終對帳。Webhook URL 必須做 HTTPS、DNS/IP 重綁與 SSRF 防護，並有事件 ID、簽章、重試、
死信及停用機制，不在第一階段範圍。

## 9. 建議補充 API

以下不是原始 13 項的必要前置，可列入第二階段；仍全部使用 POST：

| Endpoint | Scope | 用途 |
| --- | --- | --- |
| `/runtime/requests/validate` | `runtime.request.submit` | 不建立流程，只驗證版本、表單與起單資格 |
| `/design/processes/catalog` | `design.process.read` | 列出 Client 白名單內可發起的 Published 流程 |
| `/auth/key/introspect` | `auth.self.read` | 回目前 Client、Scopes、期限與配額，不回 Key |
| `/files/upload-sessions/create` | `runtime.attachment.write` | 建立受控附件上傳批次 |
| `/files/upload-sessions/complete` | `runtime.attachment.write` | 完成附件上傳並取得 attachment IDs |

`requests/status` 已提升為第一階段必要 API，正式契約見 8.5；本節其餘項目仍可依整合需求排程。

## 10. 非功能與安全要求

- 全程 HTTPS；反向代理不得把 Authorization Header 寫入 access log。
- Key 驗證失敗、IP 禁止、Scope 禁止、限流、撤銷與發單都寫安全稽核，但不記 Secret。
- 每支清單 API 強制 pageSize 上限、SQL timeout、response size limit 及穩定排序。
- `requestTime` 可設定最大時差，例如 5 分鐘，降低截取重放；正式防重仍以 Idempotency-Key 為主。
- API Client、Key、Scopes、流程白名單與 IP 變更均需管理 audit；高風險動作要求原因及二次確認。
- Key 建立／輪替畫面禁止瀏覽器快取，離頁後清除明文狀態。
- 對外 DTO 不直接序列化 Entity；不回 OID、Lock Version、密碼、私人個資、Script、SQL 或內部路徑。
- Rate Limit 至少同時依 Tenant＋Client＋Key＋Endpoint；多節點部署使用共享儲存或原子資料庫策略。
- Secret Hash、Idempotency 與外部參考需有唯一鍵／併發測試，不能只靠程式先查再寫。
- Production、UAT Key 使用不同環境與 Prefix，禁止 Production Key 用於測試環境。

## 11. 實作分期

### Phase 1：管理與認證基礎

- 三張資料表、Entity／Mapper／Service／API Client 管理 Controller。
- Client 新增／修改／停用、Key 產生／輪替／撤銷。
- Key Filter、Tenant Context、Scope、IP、有效期、Rate Limit 與 access log。
- API 說明畫面與 OpenAPI 3.1 契約。

### Phase 2：唯讀 API

- 完成 10 支組織／人員 API。
- 完成流程 Form Binding 與 Form Template API。
- 加入 effectiveAt、分頁、資料遮罩、跨 Tenant 與有效期間測試。

### Phase 3：發單閉環

- 外部發起授權、流程白名單、Schema Hash、External Reference 與 Idempotency。
- 重用既有正式 Runtime Submit Service，不建立第二套 Flowable 起單邏輯。
- 補齊 API Client 技術身分及 initiator／applicant 稽核欄位。
- 完成單筆 status API，支援流程 ID、Business Key、單據編號及 External Reference 對帳；附件另依確認範圍排程。

### Phase 4：整合驗收

- ERP、MES、HR 各建立一個最小 Scope 測試 Client。
- Key 輪替重疊、撤銷、過期、錯誤 IP、錯誤 Scope、限流及重放測試。
- 組織多任職、無主管、根部門、失效員工及大量分頁測試。
- 表單版本變更、Schema Hash 衝突、重複發單、Resolver Incident、資料庫 rollback 測試。
- 實際 MariaDB／Flowable 多帳號 E2E 與外部來源對帳。

## 12. 完成定義

只有下列條件全部成立，才能標示外部系統 API 已完成：

- API Key 明文只顯示一次，Hash、輪替、撤銷、期限、IP、Scope 及限流皆通過測試。
- 13 組 API 均有 POST 契約、欄位驗證、Tenant 隔離、稽核及 API 說明。
- 組織與人員 API 正確區分直屬主管、部門主管、主要任職、多重任職及簽核 Level。
- 流程／Form API 只提供已發布且 Client 有權存取的版本，不洩漏 Script 或 SQL。
- 發單重用正式 Runtime，正確保存 API Client、initiator、applicant、部門、版本、快照及外部參考。
- Idempotency、External Reference、單據編號與併發競爭不產生重複流程。
- 狀態查詢正確回傳 active Tasks、終態及受控 Incident 摘要，且不洩漏表單內容或簽核意見。
- API 說明頁的 Request／Response 與實際 Controller DTO 經契約測試保持一致。
- ERP／MES／HR 測試 Client 與多帳號、跨 Tenant、Incident、撤銷及重放 E2E 全部通過。

本文件目前只代表規劃完成，尚未建立資料表、Java、Vue、Key 或正式外部連線，不得標示為已實作
或上線。
