# 25 Workspace 與申請中心重構規劃

## 1. 文件目的與狀態

本文件定義 FlowMint 一般使用者工作台、可發起流程目錄及正式填單頁面的重構方案。目標是把目前集中在 `workspace.vue` 的儀表板、流程選擇、Form.io Runtime、附件與送單責任拆開，使使用者可以一眼看懂能申請什麼，並在獨立、可返回、可恢復且適合長表單的頁面完成申請。

本文件目前是實作規劃，不代表頁面已完成。正式標記完成前必須通過第 22 節的驗收條件。

本規劃遵守：

- [07 前端程式規範](07-前端程式規範.md)
- [08 API 規格](08-API規格.md)
- [09 程式編排](09-程式編排.md)
- [15 QIFU4 前端實作強制規範](15-QIFU4前端實作規範.md)
- [19 原始碼排版與交付規範](19-原始碼排版與交付規範.md)
- [22 附件上傳與儲存規格](22-附件上傳與儲存規格.md)

## 2. 現況結論

目前 `frontend-v-nx/pages/workspace.vue` 同時負責：

1. Tenant 清單與目前公司選擇。
2. 待辦清單及統計。
3. 通知清單及已讀操作。
4. 我的申請清單及統計。
5. 可發起流程查詢。
6. 代申請人帳號輸入。
7. 流程下拉選擇。
8. 起單資料載入。
9. Form.io 建立、銷毀及 submission 管理。
10. Custom JavaScript Runtime。
11. Data Action Bridge。
12. 附件 Upload Session、上傳、刪除及限制檢查。
13. 冪等鍵建立及正式送單。
14. 送單成功結果顯示。
15. 離頁前附件未送出警告。

這個結構完成了最小垂直切片，但不適合作為正式產品資訊架構。

### 2.1 現況問題

| 問題 | 使用者影響 | 工程影響 |
|---|---|---|
| 流程藏在單一 Select | 無法一眼看見分類、流程說明與可用項目 | 後續難加入搜尋、常用、排序及推薦 |
| 表單顯示在 Workspace 下方 | 使用者容易迷失目前位置，長表單破壞工作台閱讀 | Workspace 綁死 Form.io、附件及送單生命週期 |
| Tenant、申請人、流程及表單共享同頁狀態 | 任何條件改變都可能銷毀使用者輸入 | watch 與非同步請求競態難以控制 |
| 成功後仍停留在 Workspace | 不清楚下一步應查看申請還是再發一張 | 成功結果與申請詳情形成兩套呈現 |
| 大量 `any` | API 契約與狀態轉換不透明 | 重構與測試風險高 |
| Catalog 例外被轉為空清單 | 無權限、設定錯誤及系統錯誤看起來完全相同 | 問題難以診斷與監控 |
| Workspace 一次載入多種資料 | 首屏請求多，任何 API 慢都影響整頁 | Loading、錯誤與重試無法局部分離 |

### 2.2 應保留的既有能力

重構不能丟失下列已完成能力：

- Tenant 只能來自登入者有效 membership。
- 本人申請與代申請必須分開，代申請由後端重新驗證授權。
- Catalog 只顯示實際可發起的已發布流程。
- Load 與 Submit 必須再次執行安全檢查，不能信任 Catalog。
- Form.io Schema、UI Schema、Custom JavaScript 與 Data Action Bridge 必須沿用發布版本。
- 系統欄位必須透過 `FLOWMINT_SYSTEM_FIELDS` 管理。
- `documentNumber` 等顯示欄位送出前必須移除。
- 附件仍採 Upload Session，正式送單時同交易綁定。
- Submit 必須帶 `Idempotency-Key`，避免重複送單。
- Form.io、Custom JavaScript、Data Action Bridge 在換頁或卸載時必須完整 destroy。
- 未送出的附件及有內容的表單離頁前必須警告。

## 3. 產品設計原則

1. Workspace 是摘要與導覽，不是完整填單頁。
2. 使用者不展開下拉選單，也能看見主要可發起流程。
3. 所有流程按業務類別分組；類別、流程與表單是不同層級，不得混用名稱。
4. 點擊流程卡片後換到獨立填單路由，不在 Workspace 原地展開表單。
5. URL 必須可表達目前正在發起哪一個流程，但不得把 Tenant、申請人或敏感表單資料暴露在 URL。
6. Catalog 是目錄；Load 是建立填單 Context；Submit 是交易。三者不可在前端混成單一隱含動作。
7. 本人申請應是預設且最短路徑；代申請是明確切換的進階情境。
8. 空狀態、無權限、設定錯誤、網路錯誤與載入中必須分開呈現。
9. 桌面與手機都必須能完成目錄瀏覽及填單，不能只縮小桌面版。
10. 每一頁只擁有自己需要的狀態，離頁後不得殘留 Form.io instance 或非同步回寫。

## 4. 目標資訊架構

```text
FlowMint
├─ 工作台 /workspace
│  ├─ 統計摘要
│  ├─ 我的待辦摘要
│  ├─ 我的申請摘要
│  ├─ 通知摘要
│  └─ 常用申請／查看全部申請
├─ 申請中心 /requests/start
│  ├─ 公司 Context
│  ├─ 本人／代申請 Context
│  ├─ 搜尋
│  ├─ 類別導覽
│  └─ 類別下的流程卡片
├─ 發起申請 /requests/start/[processDefId]
│  ├─ 申請 Context
│  ├─ 流程與表單說明
│  ├─ Form.io Runtime
│  ├─ 附件
│  └─ 取消／送出
├─ 送出完成 /requests/[processInstanceId]
│  ├─ 單據編號與狀態
│  ├─ 表單內容
│  └─ 流程歷程
├─ 待辦 /tasks/[taskId]
└─ 營運與設計程式
```

### 4.1 路由定義

| 路由 | 職責 | 權限來源 |
|---|---|---|
| `/workspace` | 摘要與快速導覽 | 登入即可，資料由各 Runtime API 限制 |
| `/requests/start` | 完整可申請目錄 | 登入、Tenant membership、Catalog |
| `/requests/start/[processDefId]` | 獨立正式填單 | Load Start 重新驗證 |
| `/requests/[processInstanceId]` | 申請詳情與歷程 | Owner／實際 Starter／授權營運角色 |
| `/tasks/[taskId]` | 待辦內容與動作 | Task assignee／candidate／delegate |

`/requests/start` 與 `/requests/start/[processDefId]` 是登入後 Runtime 路由，不應要求後台 Program 權限；Nuxt middleware 必須延續 `/requests/` 的 Runtime 例外，但後端仍執行全部資料權限檢查。

## 5. Workspace 目標設計

### 5.1 Workspace 應保留

- 歡迎區及目前公司 Context。
- 我的待辦、未讀通知、進行中、已完成等統計。
- 最近待辦，最多 5 筆。
- 最近申請，最多 5 筆。
- 最近通知，最多 5 筆。
- 常用申請，桌面最多 6 張、手機最多 4 張。
- 「查看全部申請」入口。

### 5.2 Workspace 應移除

- 申請人帳號文字輸入。
- 完整流程 Select。
- `/start/load` 呼叫。
- Form.io schema parse 與 render。
- Custom JavaScript Runtime。
- Data Action Bridge。
- Upload Session 與附件管理。
- Idempotency Key 與 Submit。
- 完整表單成功結果。

### 5.3 Workspace 建議版面

```text
┌────────────────────────────────────────────────────────────┐
│ 工作台                         公司：A01 ○○股份有限公司   │
├──────────┬──────────┬──────────┬──────────┐
│ 我的待辦 │ 進行中   │ 已完成   │ 未讀通知 │
├───────────────────────────┬────────────────────────────────┤
│ 我的待辦                  │ 常用申請                       │
│ 1. PR-A01-...             │ [請購申請] [請假申請]         │
│ 2. ...                    │ [費用報銷] [查看更多申請 →]   │
├───────────────────────────┼────────────────────────────────┤
│ 我的申請                  │ 通知                           │
└───────────────────────────┴────────────────────────────────┘
```

### 5.4 常用申請第一版策略

第一版尚無使用者收藏資料表時，採下列可預測規則：

1. Catalog 回傳的流程依類別排序、流程排序排列。
2. 若未提供排序 metadata，依 `category + processName` 穩定排序。
3. Workspace 顯示前 6 筆，並清楚標示為「快速申請」，不得宣稱是個人常用。
4. 第二版才新增個人收藏、最近使用與管理員推薦，不應在第一版以 localStorage 假裝正式偏好。

## 6. 申請中心設計

### 6.1 頁面目的

申請中心是「我現在可以發起哪些申請」的完整答案。使用者進頁後，不需操作下拉選單即可看到分類與每個流程。

### 6.2 桌面版配置

```text
┌────────────────────────────────────────────────────────────┐
│ 發起申請                                                   │
│ 公司 [A01 ○○股份有限公司 ▼]  申請方式 [本人申請 ▼]       │
│ 搜尋 [搜尋申請名稱或說明____________________]              │
├───────────────┬────────────────────────────────────────────┤
│ 全部          │ 採購（2）                                 │
│ 採購       2  │ ┌────────────┐ ┌────────────┐             │
│ 人事       4  │ │ 請購申請   │ │ 緊急採購   │             │
│ 財務       3  │ │ 說明...    │ │ 說明...    │             │
│ 資訊       1  │ │ 開始申請 → │ │ 開始申請 → │             │
│ 行政       2  │ └────────────┘ └────────────┘             │
└───────────────┴────────────────────────────────────────────┘
```

### 6.3 手機版配置

- 公司與申請方式置頂並採全寬控制項。
- 搜尋框固定在類別上方。
- 類別改為可水平捲動的 pills，不使用左側欄。
- 流程卡片一列一張，整張卡可點擊但保留清楚的「開始申請」文字。
- 不使用 hover 才出現的資訊。

### 6.4 類別行為

- 類別使用流程主檔 `CATEGORY` 作為穩定代碼。
- 類別顯示名稱不可直接把代碼原樣顯示給使用者。
- 第一版建立後端受控 Catalog：`PURCHASE → 採購`、`HR → 人事`、`FINANCE → 財務`、`IT → 資訊`、`ADMIN → 行政`、未識別代碼 → `其他`。
- 類別必須回傳 `code`、`label`、`icon`、`sortOrder`。
- 流程卡片至少回傳 `processDefId`、`processKey`、`processName`、`description`、`categoryCode`、`categoryLabel`、`icon`、`sortOrder`、`versionNo`。
- 類別只在至少有一個可發起流程時顯示。
- 類別數量是目前安全過濾後的流程數，不是全系統流程數。

### 6.5 搜尋行為

- 搜尋對目前已授權 Catalog 做前端即時過濾，不為每個字元重新呼叫後端。
- 比對流程名稱、流程說明、類別名稱及流程代碼。
- 搜尋字串 trim、忽略大小寫。
- 無結果時顯示「找不到符合條件的申請」，並提供清除搜尋。
- 不顯示被安全過濾掉的流程名稱或數量。

### 6.6 流程卡片內容

每張卡片包含：

- 類別圖示。
- 流程名稱。
- 最多兩行簡短說明。
- 可選的標籤，例如「常用」「需主管簽核」，但標籤必須有正式 metadata，不可由前端猜測 BPMN。
- 「開始申請」按鈕。

版本號屬技術資訊，正式使用者目錄預設不顯示；可放在 `title`、除錯資訊或營運模式，不應以「v1」干擾一般使用者。

## 7. 本人申請與代申請設計

### 7.1 本人申請

- 預設申請人為登入帳號。
- 頁面顯示「本人申請」，不要求使用者再次輸入帳號。
- Catalog 使用登入帳號查詢。

### 7.2 代申請

- 只有後端回傳至少一個可代理申請人時，才顯示「代他人申請」。
- 不使用自由文字帳號輸入。
- 使用可搜尋的受控人員選擇器，資料只能來自有效代起單授權。
- 選項顯示姓名、帳號、主要部門；不得顯示不必要的個資。
- 切換申請人後重新載入 Catalog。
- 已進入填單頁後不可靜默切換申請人；需返回申請中心重新選擇，以避免表單 Context 混用。

### 7.3 建議新增 API

```text
POST /api/fm/requests/start/applicants
Header: X-FlowMint-Tenant
```

回傳本人及登入者具有有效代起單授權的申請人：

```json
{
  "success": "Y",
  "value": [
    {
      "account": "admin",
      "displayName": "管理者",
      "primaryOrgUnitName": "資訊部",
      "self": true
    }
  ]
}
```

後端仍必須在 Catalog、Load、Submit 各階段重新驗證，這個選項 API 只改善 UX，不是授權依據。

## 8. 獨立填單頁設計

### 8.1 路由

```text
/requests/start/[processDefId]
```

Tenant 與申請人以 query string 作為頁面導覽狀態：

```text
/requests/start/{processDefId}?tenantId={tenantId}&applicantAccount={account}
```

`tenantId`、`applicantAccount` 與 `processDefId` 不是授權憑證，使用者可修改 URL，但後端必須在 Catalog、Load 與 Submit 每一階段依 HttpOnly Cookie 內的 JWT 重新驗證 Tenant 成員資格、本人／代理申請權限、流程發布狀態、啟動規則與表單綁定。無權時直接拒絕，不依賴前端隱藏或 URL 不可猜測性。

> 產品決策（2026-08-21）：本階段不建立 Start Context API，也不把 Start Context 列為上線條件。若未來有不在 URL 呈現導覽資訊的隱私需求，再獨立評估；不得以它取代後端授權重驗。

### 8.2 頁首

- 麵包屑：工作台／申請中心／請購申請。
- 流程名稱及說明。
- 公司、申請人、實際發起人與主要申請部門摘要。
- 申請 Context 為唯讀；若需變更，使用「重新選擇」返回申請中心。
- 不顯示可編輯的流程版本號或技術 ID。

### 8.3 表單區

- Form.io 置於獨立卡片或內容區，不再受 Workspace 雙欄寬度限制。
- 桌面最大內容寬度建議 1200～1400px；長表單保持欄位可讀性。
- Schema parse 失敗顯示完整頁錯誤，不建立半成品表單。
- Custom JavaScript、Data Action Bridge 與 Form.io Runtime 採單一共用 composable 管理生命週期。
- 表單切換只在流程確實綁定多個「起單用表單」且業務定義明確時出現；一般流程不顯示表單 Select。

### 8.4 操作列

桌面版使用頁尾 sticky action bar，手機版使用固定底部操作列：

- 左側：返回申請中心／取消。
- 右側：儲存草稿（第二階段）、送出申請。
- 送出時整組操作鎖定，顯示進度文字。
- 送出按鈕防 double click，沿用 `submitInFlight` 與冪等鍵雙重保護。
- 表單驗證錯誤時捲動並聚焦第一個錯誤欄位。

### 8.5 取消與離頁

下列任一條件成立即視為 Dirty：

- Form.io submission 與初始資料不同。
- 已上傳至少一個未綁定附件。
- Custom JavaScript 已改變可持久化欄位。

Dirty 時：

- 瀏覽器重新整理或關閉使用 `beforeunload`。
- Nuxt 站內導頁使用 route leave guard 與確認對話框。
- 確認離開後，前端可呼叫 Upload Session abandon；即使未呼叫，後端過期清理仍是最後保護。
- 不只檢查附件，必須同時檢查一般表單內容。

### 8.6 送出成功

成功後不在原頁追加大型成功 Alert。應立即：

1. 執行 `afterSubmit`，失敗只提示「單據已送出，但後處理失敗」。
2. 清除 Dirty 與 Upload Session state。
3. 以 `replace` 導向 `/requests/{processInstanceId}`，避免返回鍵重送表單。
4. 申請詳情頁顯示成功提示、單據編號、目前節點與歷程。

## 9. 共用前端元件與 Composable 拆分

### 9.1 建議目錄

```text
frontend-v-nx/
├─ pages/
│  ├─ workspace.vue
│  └─ requests/
│     ├─ start/
│     │  ├─ index.vue
│     │  └─ [processDefId].vue
│     └─ [id].vue
├─ components/flowmint/start/
│  ├─ RuntimeAttachmentFields.vue
│  └─ StartActionBar.vue
├─ composables/
│  ├─ useProcessStartCatalog.ts
│  ├─ useRuntimeForm.ts
│  ├─ useRuntimeAttachments.ts
│  └─ useRuntimeSubmit.ts
└─ types/
   └─ processStart.ts
```

只有 Workspace 與申請中心共用的元件才放在全域 `components/flowmint/start`；若元件只被填單頁使用，也可放在該頁目錄的 `components`。

### 9.2 `useProcessStartCatalog`

責任：

- 載入 Tenant。
- 載入本人／可代申請人。
- 載入 Catalog。
- 建立分類 View Model。
- 搜尋與類別過濾。
- 區分 loading、empty、forbidden 與 error。

不得包含 Form.io、附件或 Submit。

### 9.3 `useRuntimeForm`

責任：

- Parse Schema 與 UI Schema。
- 建立及銷毀 Form.io。
- 注入起單 Context 系統欄位。
- 接上 Custom JavaScript 與 Data Action Bridge。
- 追蹤 dirty、validity 及第一個錯誤欄位。
- 卸載時使用 generation token 防止舊 Promise 回寫。

### 9.4 `useRuntimeAttachments`

責任：

- 從 Schema 收集 File 元件。
- 建立及更新 Upload Session。
- 檔案格式、單檔、數量、總容量的前端提示。
- 上傳、刪除、逾期重建。
- 將 attachment ID 寫回 submission。
- expose `dirty`、`uploading`、`uploadSessionId`。

後端仍是所有限制與權限的最終判斷者。

### 9.5 `useRuntimeSubmit`

責任：

- 建立並保留一次申請生命週期的 Idempotency Key。
- Form.io、附件及 Custom JavaScript 驗證順序。
- 移除顯示用系統欄位。
- 防止 double click。
- Submit 與成功導頁。
- 區分送單失敗及 `afterSubmit` 失敗。

## 10. TypeScript 型別

禁止新頁面沿用 Workspace 大量 `any`。至少建立：

```ts
export interface RuntimeTenant {
  tenantId: string;
  tenantCode: string;
  tenantName: string;
  defaultTenant: boolean;
}

export interface ProcessStartCatalogItem {
  processDefId: string;
  processKey: string;
  processName: string;
  description: string | null;
  categoryCode: string;
  categoryLabel: string;
  categoryIcon: string;
  categorySortOrder: number;
  processSortOrder: number;
  versionNo: number;
}

export interface ProcessStartCategory {
  code: string;
  label: string;
  icon: string;
  sortOrder: number;
  processes: ProcessStartCatalogItem[];
}

export interface ProcessStartForm {
  formId: string;
  formVersionNo: number;
  formCode: string;
  formName: string;
  schemaContent: string;
  uiSchemaContent: string;
  customScriptContent: string;
  taskDefKeys: string[];
}
```

API 共通 response 亦應建立 generic type，不再用 `response: any` 判斷。

## 11. 後端 API 規劃

### 11.1 保留 API

```text
POST /api/fm/requests/start/tenants
POST /api/fm/requests/start/catalog
POST /api/fm/requests/start/load
POST /api/fm/requests/submit
```

### 11.2 Catalog 契約補強

目前 `FmProcessStartCatalogView` 只有原始 `category`。建議改為：

```json
{
  "processDefId": "...",
  "processKey": "FM_PURCHASE_APPROVAL",
  "processName": "請購簽核流程",
  "description": "請購申請、專業審查與金額核決",
  "categoryCode": "PURCHASE",
  "categoryLabel": "採購",
  "categoryIcon": "cart",
  "categorySortOrder": 30,
  "processSortOrder": 10,
  "versionNo": 1
}
```

類別 label 與 icon 必須由後端受控 catalog 或正式資料表提供，不讓不同前端各自翻譯。

### 11.3 錯誤可觀測性

Catalog 可安全忽略「該使用者不可發起」的流程，但不得無差別吞掉所有 `ServiceException`。

應區分：

- `NOT_AUTHORIZED`：正常過濾，不回傳流程。
- `NOT_EFFECTIVE`：正常過濾。
- `PUBLISH_CONFIGURATION_INVALID`：記錄 error／incident，不向一般使用者暴露敏感細節。
- `FORM_BINDING_INVALID`：記錄 error／incident。
- `SYSTEM_ERROR`：整個 Catalog 回傳失敗，不能假裝為空清單。

一般使用者空清單只表示真的沒有可申請流程；設定錯誤必須能由營運監控看見。

### 11.4 導覽參數與授權邊界

- 導覽參數只負責恢復使用者選擇，以支援 F5、返回與多分頁。
- HttpOnly JWT 識別實際操作者；後端依請求當下資料執行授權。
- 不建立 Start Context API，不增加短效 Token 的儲存、過期、清理與多分頁狀態複雜度。
- Load 與 Submit 仍必須各自完整驗證，不可信任前端傳入值。

## 12. 類別 Metadata 決策

### 12.1 資料驅動分類主檔

類別不得寫死在 Java 或 Vue。以 Tenant 級 `fm_process_category` 儲存：

- `CATEGORY_CODE`：穩定分類代碼。
- `CATEGORY_LABEL`：使用者顯示名稱。
- `ICON_CODE`：Bootstrap Icon 代碼。
- `SORT_ORDER`：申請中心的分類排序。
- `STATUS`：`ACTIVE`／`INACTIVE`。

`fm_process_def.CATEGORY` 只保存對應的 `CATEGORY_CODE`；`PROCESS_SORT_ORDER` 保存同分類內的流程排序。Runtime Catalog 由這兩張表組合回傳 metadata。分類不存在或未啟用時視為發布設定錯誤，不 fallback 成任何程式內建分類。

### 12.2 管理邊界

流程設計器以受控下拉選擇分類，並提供分類主檔的新增、編輯與停用。仍被未停用流程引用的分類不得停用。新增分類不需修改或重新編譯 Java／Vue。

## 13. 狀態管理與生命週期

### 13.1 Catalog State

可保存在 Pinia：

- Tenant 清單及目前 Tenant。
- 目前申請人 Context。
- Catalog 與分類。
- 搜尋字串及目前類別。

不可保存在 localStorage：

- 表單 submission。
- 申請人敏感資料。
- Upload Session。
- Access Token 以外的安全 Context。

### 13.2 Form State

Form.io instance、submission、附件與 Idempotency Key 只屬於填單頁生命週期。

離開填單頁時必須：

1. 阻止新的非同步 callback。
2. detach Data Action Bridge。
3. await Custom JavaScript detach。
4. destroy Form.io instance。
5. 清空 DOM host。
6. 清除附件暫存 state。
7. 清除 Idempotency Key。

### 13.3 請求競態

- Tenant 或 Applicant 改變時取消或忽略舊 Catalog 回應。
- 填單 Load 使用 request generation token；舊回應不得覆蓋新路由。
- 上傳完成時若頁面已卸載，不得回寫 Form.io。
- Submit 開始後禁止切換表單、Tenant 或 Applicant。

## 14. Loading、Empty 與 Error State

| State | 呈現 |
|---|---|
| Tenant loading | 公司欄 skeleton，Catalog 不顯示假資料 |
| Catalog loading | 類別與卡片 skeleton |
| 真正無可發起流程 | 說明目前沒有可申請項目，提供返回 Workspace |
| 搜尋無結果 | 顯示清除搜尋，不等同無權限 |
| Catalog 系統錯誤 | Error panel、重試按鈕、追蹤碼 |
| Load 失敗 | 不建立 Form.io，顯示返回申請中心 |
| Schema 錯誤 | 顯示表單設定異常，記錄營運事件 |
| Submit 驗證失敗 | 聚焦第一個錯誤欄位，不清除輸入 |
| Submit 409／重複 | 依 Idempotency 回傳既有結果或提示內容衝突 |
| Task 已變更 | 與起單無關，不應污染申請中心狀態 |

## 15. 安全與隱私

- Tenant 仍由 `X-FlowMint-Tenant` header 傳入，後端驗證 membership。
- Starter 只從 Security Context 取得。
- Applicant 只能是本人或有效代起單授權對象。
- Catalog、Load、Submit 都重驗，不因前一步成功而略過。
- Process Def ID、Form ID、Version No 都視為不可信輸入。
- Schema 及 Custom JavaScript 只能取已發布版本。
- 不在 URL、console、analytics、localStorage 保存表單內容。
- 錯誤訊息不得暴露其他使用者、未授權流程或 SQL。
- 前端不得依 `admin` 身分繞過 Runtime 規則；管理員若要測試，仍須有有效 Employee 與 Tenant membership。
- Custom JavaScript 是受信任擴充模型，但仍受 timeout、destroy generation 與後端最終驗證限制。

## 16. Accessibility 與操作品質

- 類別 pills、流程卡片及操作按鈕可用鍵盤操作。
- 卡片不得只有顏色表示類別。
- icon 必須搭配文字或 `aria-label`。
- Focus order 依公司、申請人、搜尋、類別、流程卡片排列。
- 載入完成及錯誤使用適當 `aria-live`。
- 表單驗證後移動 focus 到第一個錯誤欄位。
- Sticky action bar 不可遮住最後一個表單欄位。
- 手機鍵盤彈出時，固定操作列不得覆蓋輸入欄。
- 文字對比及 focus ring 沿用 QIFU4／Bootstrap 基準。

## 17. 效能規劃

- Workspace 不載入完整 Schema、UI Schema 或 Custom Script。
- Catalog 只回傳 metadata，不回傳表單內容。
- Form Schema 只在進入填單頁後由 Load 取得。
- Catalog 第一版流程數不大，可前端分類與搜尋；超過 200 筆再評估後端搜尋與分頁。
- Form.io 與 CSS 可在填單頁動態 import，不進 Workspace 首屏必要 bundle。
- Tenant、Applicant 及 Catalog 可在同一 Session 短暫 cache，但發布後應有合理失效策略。
- 不 cache Submit、Upload Session 或表單敏感內容。

## 18. 分階段實作

### Phase A：先拆頁，不改核心 API

1. 建立 `types/processStart.ts`。
2. 建立 Catalog、Runtime Form、附件與 Submit composable。
3. 新增 `/requests/start` 申請中心。
4. 新增 `/requests/start/[processDefId]` 填單頁。
5. Workspace 移除流程 Select 及 Form.io，改顯示快速申請卡片。
6. Submit 成功導向既有申請詳情。
7. 沿用目前 `/tenants`、`/catalog`、`/load`、`/submit`。

完成定義：既有功能完成頁面拆分，請購可由申請中心進入獨立頁送出。

### Phase B：類別與申請人 UX 補強

1. 新增後端類別 Catalog。
2. 擴充 `FmProcessStartCatalogView`。
3. 新增受控 Applicant options API。
4. 移除自由輸入申請人帳號。
5. 加入搜尋、分類數量、類別排序與空狀態。

完成定義：使用者可一眼看到分類下的全部可申請流程，代申請不需手寫帳號。

### Phase C：F5 與多分頁恢復

1. 填單路由以明確 query 參數支援 F5 reload。
2. 每個分頁依自身 route state 載入，不共用可變的全域申請 Context。
3. 路由參數缺少、失效或無權時顯示可恢復的錯誤狀態。

完成定義：可安全重新整理填單頁，且任何 route state 都不被當作授權來源。

### Phase D：草稿與個人化

1. 正式表單草稿資料模型與 API。
2. 儲存草稿、繼續填寫及草稿清單。
3. 個人收藏與最近使用。
4. 管理員推薦流程。

草稿不得使用 localStorage 代替正式後端資料模型。

## 19. 預計修改檔案

### 19.1 前端新增

```text
frontend-v-nx/pages/requests/start/index.vue
frontend-v-nx/pages/requests/start/[processDefId].vue
frontend-v-nx/types/processStart.ts
frontend-v-nx/composables/useProcessStartCatalog.ts
frontend-v-nx/composables/useRuntimeForm.ts
frontend-v-nx/composables/useRuntimeAttachments.ts
frontend-v-nx/composables/useRuntimeSubmit.ts
frontend-v-nx/components/flowmint/start/*.vue
```

### 19.2 前端修改

```text
frontend-v-nx/pages/workspace.vue
```

### 19.3 後端第一批修改

```text
backend/app/src/main/java/org/qifu/fm/dto/view/FmProcessStartCatalogView.java
backend/app/src/main/java/org/qifu/fm/dto/view/FmProcessStartApplicantView.java
backend/app/src/main/java/org/qifu/fm/logic/IFmProcessRuntimeLogicService.java
backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java
backend/app/src/main/java/org/qifu/fm/controller/FmProcessRuntimeController.java
backend/app/src/main/java/org/qifu/fm/entity/FmProcessCategory.java
backend/app/src/main/java/org/qifu/fm/service/IFmProcessCategoryService.java
backend/app/src/main/resources/org/qifu/fm/mapper/FmProcessCategoryMapper.xml
```

實際檔名可以依現有 package 微調，但責任邊界不得重新集中回單一 Workspace。

## 20. 測試計畫

### 20.1 後端單元測試

- 類別主檔的 Tenant 隔離、排序、啟停用與重複代碼驗證。
- Catalog 僅回傳 `PUBLISHED` 流程與表單。
- 本人申請。
- 有效代申請。
- 無效、停用、過期及跨 Tenant 代申請。
- `ALL`、帳號、部門、群組及拒絕優先的起單政策。
- 設定錯誤不得被誤報為正常空 Catalog。

### 20.2 前端 component 測試

- 類別與卡片分組。
- 搜尋、清除搜尋與無結果。
- Tenant／Applicant 切換忽略舊回應。
- 流程卡點擊導向正確路由。
- Dirty 離頁確認。
- Submit double click 只送一次。
- 成功使用 replace 導向申請詳情。

### 20.3 瀏覽器 E2E

至少包含：

1. `admin` 登入，A01 本人請購送單。
2. 一般員工本人送單。
3. 有授權的代申請。
4. 無授權者看不到代理申請人且直接呼叫 API 被拒絕。
5. 類別切換與搜尋。
6. F5、返回與 Dirty 警告。
7. 附件上傳、刪除、逾期與送單綁定。
8. Custom JavaScript 與 Data Action 自動帶入。
9. 送出後導向申請詳情並顯示請購單號。
10. 手機尺寸完成最小請購送單。

## 21. 遷移與相容策略

- 第一階段保留既有 Runtime API 路徑，降低後端風險。
- 新申請中心完成並驗證前，Workspace 舊起單區可暫時由 feature flag 控制，不同時對一般使用者顯示兩套入口。
- 新路徑驗收通過後，完整移除 Workspace Form.io 相關程式，不保留不可達死碼。
- 既有 `/requests/[id]` 與 `/tasks/[id]` 不更改 URL。
- 通知、待辦及我的申請資料契約不因本次重構擴張。
- 不直接修改正式 `tb_sys_prog`；Runtime 路由不需要新增後台 Program Item。

## 22. Release Gate 與完成定義

以下全部成立，才能標記重構完成：

- [x] Workspace 不再包含流程 Select、Form.io、附件或 Submit。
- [x] Workspace 顯示清楚的快速申請卡與「查看全部」。
- [x] `/requests/start` 按類別呈現全部可發起流程。
- [x] 類別具有受控 label、icon 與排序。
- [x] 流程卡片顯示名稱、說明及開始申請。
- [x] 本人申請不需再次輸入帳號。
- [x] 代申請使用受控選人，不使用自由文字。
- [x] 點擊流程後進入獨立填單頁。
- [x] 填單頁完整支援 Form.io、Custom JavaScript、Data Action 及附件。
- [x] Tenant、Starter、Applicant、流程與表單均由後端重新驗證。
- [x] Dirty 表單與未送附件離頁會警告。
- [x] Submit 防 double click 且保留 Idempotency Key。
- [x] 成功後導向申請詳情，不停留在 Workspace。
- [ ] Loading、Empty、Forbidden、Configuration Error 與 System Error 可區分。
- [x] TypeScript 核心 DTO 不使用 `any`。
- [x] 後端單元／契約測試通過（11 項目標測試）。
- [x] Nuxt production build 通過。
- [x] `git diff --check` 與人工完整 diff 檢查通過。
- [ ] 真實後端與 MariaDB 完成本人、代申請、附件及請購 E2E。
- [ ] 桌面與手機瀏覽器驗收通過。

## 23. 建議開發順序

建議下一步依序執行：

1. 先建立型別與共用 Runtime composable，從 Workspace 搬移而不改變送單語意。
2. 建立獨立填單頁並以 `FM_PURCHASE_APPROVAL` 完成第一條 E2E。
3. 建立申請中心分類卡片頁。
4. 將 Workspace 改為摘要與快速入口。
5. 補 Catalog 類別 metadata 與受控代理申請人 API。
6. 完成錯誤分類與可觀測性。
7. 補 component 與瀏覽器 E2E。
8. 驗收後刪除 Workspace 舊起單程式。
9. 再評估正式草稿及個人收藏；Start Context 依產品決策不實作。

最重要的第一條使用者路徑固定為：

```text
登入
→ Workspace 看見「請購申請」
→ 申請中心確認採購類別與流程
→ 獨立請購填單頁
→ 送出
→ 請購申請詳情與單據編號
```
