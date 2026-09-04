# 33 System Task 與 Data Action Task 規劃

日期：2026-09-04  
狀態：Phase 1～2 第一版已實作，Runtime MVP 已接線；完整營運能力仍在開發中  
適用程式：`FM_PROG004D0001` BPMN 流程設計與版本

## 1. 目的

FlowMint 目前的 BPMN Designer 使用 `bpmn-js`，後端發布驗證只允許 Start Event、End Event、User Task、Gateway 與 Sequence Flow，並明確禁止 BPMN Script Task、任意 Java／Groovy、未登錄 Delegate Expression、直接 SQL 與任意 HTTP。

本規劃要在不開放任意後端程式碼的前提下，讓流程設計者可在 BPMN 中加入受控的自動化節點，例如：

- 查詢員工、供應商或 ERP 資料。
- 建立或更新外部系統單據。
- 執行受控資料庫交易。
- 將執行結果寫回流程表單資料，供後續 Gateway 分流或 User Task 使用。

## 2. 產品決策

### 2.1 不開放原生 Script Task

不將 `bpmn:scriptTask` 加入允許清單，也不提供 JavaScript、Groovy、Java class 或 JUEL 任意執行內容。原因包含：

- 無法從 Tenant 邊界與登入者權限推導 Script 可存取的資源。
- 難以保證 timeout、重試、冪等、交易與補償語意。
- 容易繞過 Data Action 的 SQL 白名單、參數綁定、版本與 Audit。
- 已發布流程的行為難以穩定重現。

### 2.2 新增受控 System Task

Designer 對使用者顯示「System Task」，第一種 subtype 為「Data Action Task」。底層使用標準 BPMN `bpmn:serviceTask`，但只允許 FlowMint 自己定義且可驗證的屬性。

`System Task` 是產品名稱，`Data Action Task` 是具體的執行類型，不是兩種不同 BPMN element。後續若增加通知或其他受控執行器，仍沿用 System Task 模型，不新增任意 Script 入口。

## 3. BPMN XML 契約

### 3.1 設計端 XML

Designer 儲存的 Draft XML 建議格式：

```xml
<definitions
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:flowmint="https://flowmint.qifu.org/schema/bpmn">
  <process id="FM_PURCHASE_APPROVAL" isExecutable="true">
    <serviceTask
      id="loadEmployee"
      name="讀取員工資料"
      flowmint:taskType="DATA_ACTION"
      flowmint:actionCode="FM_GET_EMPLOYEE"
      flowmint:actionVersion="1"
      flowmint:bindingId="binding-load-employee" />
  </process>
</definitions>
```

Request／Response Mapping、timeout、retry 及錯誤處理若結構較大，不建議全部壓成 XML attribute。應放在受控 `flowmint:extensionElements` 或流程版本專用設定表，並以 `bindingId` 關聯。實作前必須先確定單一儲存來源，不可同時維護 XML 與資料庫兩份可漂移設定。

### 3.2 禁止屬性

Data Action Task 不得出現下列 Flowable 執行屬性：

- `flowable:class`
- `flowable:delegateExpression`
- `flowable:expression`
- `flowable:type` 中任何未列入允許清單的類型
- Script、SQL、URL、HTTP header 或 credential 原始內容

### 3.3 發布用 Runtime XML

資料庫保存設計端 BPMN；發布時仿照現有 User Task Listener 與 Multi-instance 轉換，產生另一份 Runtime BPMN。受控 Service Task 統一轉為平台內部 Delegate：

```xml
<serviceTask
  id="loadEmployee"
  name="讀取員工資料"
  flowable:delegateExpression="${fmDataActionTaskDelegate}" />
```

`delegateExpression` 只能由後端發布轉換器產生，Designer、API command 與儲存的 Draft XML 均不得自行指定。

## 4. bpmn-js Designer 擴充

### 4.1 Moddle descriptor

新增 FlowMint BPMN moddle descriptor，定義 namespace、prefix 及可儲存屬性。`BpmnModeler` 建立時透過 `moddleExtensions` 註冊，確保 import、edit 與 export XML 不會遺失屬性。

### 4.2 Palette 與 Context Pad

- Palette 新增「Data Action Task」。
- 建立底層 `bpmn:ServiceTask`，同時設定 `flowmint:taskType=DATA_ACTION`。
- Context Pad 可將一般 Task 轉成 Data Action Task，但不可將已有 Task Policy／Assignment Rule 的 User Task 直接轉換而留下孤兒設定。
- 自訂 Renderer 可顯示齒輪／資料交換圖示與 Action Code badge，但 XML 語意仍是 Service Task。
- Published／Retired 版本只能檢視，不得編輯屬性。

### 4.3 屬性面板

選取 Data Action Task 時顯示：

- Node ID 與顯示名稱。
- Task Type：第一版固定 `DATA_ACTION`。
- Data Action：僅列出同 Tenant、啟用且已發布版本。
- Action Version：必須固定版號，不得使用「最新版」。
- Request Mapping：從受控來源組裝 Action 參數。
- Response Mapping：將 Action result 寫入允許的目標。
- Timeout：平台設定上、下限，不允許無限等待。
- Retry Policy：重試次數、backoff 與可重試錯誤類型。
- Failure Policy：第一版建議固定建立 Incident 並停留流程，不提供靜默忽略。
- Idempotency Key：異動類 Action 必填。

Data Action catalog 與 metadata 可沿用現有 Form Data Action Binding Editor 的 API，但 BPMN 節點必須有自己的編輯組件，不可將 Form.io event binding 資料結構直接當成 BPMN Runtime 契約。

## 5. Mapping 契約

### 5.1 Request 允許來源

- `FORM_DATA`：從 `flowmintFormData` 使用明確 dot path 取值。
- `PROCESS_CONTEXT`：受控系統值，例如 Tenant ID、Process ID、Process Version、Applicant Account 與 Business Key。
- `CONSTANT`：JSON scalar，不允許 expression。
- `PREVIOUS_RESULT`：僅能參照同一 System Task 內的既有 step result，實際能力以 Data Action metadata 為準。

不允許使用 JavaScript、SpEL、任意 JUEL、SQL fragment 或任意 HTTP 取值。

### 5.2 Response 允許目標

- `FORM_DATA`：寫入已發布 Form Schema 存在、型別相容且允許持久化的欄位。
- `PROCESS_VARIABLE`：只能寫入受控 prefix 或 catalog 內的變數，不得覆寫 FlowMint 保留 Runtime 變數。
- `DISCARD`：明確不保存該 result key。

寫回 `FORM_DATA` 時必須同步 Flowable variable 與 FlowMint 的表單資料，並定義 Revision、Snapshot 與並發規則；不可只修改 Flowable 記憶體變數，造成待辦畫面、歷程快照與 Gateway 看到不同資料。

## 6. 發布驗證

流程發布前必須額外驗證：

1. `serviceTask` 必須有 `flowmint:taskType=DATA_ACTION`，其他 Service Task 全部拒絕。
2. 不得帶入任意 class、delegate expression、expression 或 Script。
3. Action 必須屬於同 Tenant、已啟用且指定版本已發布。
4. Action type 必須在 BPMN System Task 允許清單內。
5. Request mapping key 必須存在於 Action request metadata，必填參數不可遺漏。
6. Response source 必須存在於 Action result metadata。
7. Form Data source／target path 必須存在於該 Process Version 綁定的已發布 Form Version。
8. target 不得衝突，也不得覆寫 Tenant、Process ID、Business Key 等保留值。
9. timeout、retry、backoff 及 failure policy 必須在系統上限內。
10. 異動類 Action 必須定義穩定的 idempotency key。
11. Node ID 與 binding ID 在同一 Process Version 內唯一。
12. Runtime XML 轉換後必須再由 Flowable parser 驗證，且只能出現平台固定 Delegate。

發布必須固定 Process Version 對 Action Version 的引用，後續 Action 發布新版不得改變已發布或執行中流程的行為。

## 7. Runtime 執行模型

### 7.1 固定 Delegate

`fmDataActionTaskDelegate` 只負責：

1. 從當前 Flowable execution 取得 Tenant、FlowMint Process ID、Process Version、Node ID 與 Business Key。
2. 依已發布 Process Version 讀取不可變的 System Task 設定，不相信 XML 中由使用者傳入的執行 class。
3. 再次驗證 Tenant、Action 啟用狀態、版本與 Action type。
4. 套用 Request Mapping。
5. 呼叫現有 Data Action 執行核心，不從內部繞回 HTTP Controller。
6. 驗證完整 Response Mapping 後再一次套用，避免只寫入一半。
7. 同步可持久化表單資料、Flowable variable、Revision 與 Snapshot。
8. 寫入 Execution Audit，讓流程繼續。

### 7.2 交易邊界

- 同 MariaDB local transaction 內的 FlowMint 資料與 Flowable 狀態應同成功、同失敗。
- 外部系統或跨 DataSource 無法假設具有分散式原子交易，必須使用 idempotency、retry、Incident 與必要的補償策略。
- 不得因 HTTP request timeout 盲目重跳已可能成功的異動。

### 7.3 重試與冪等

- Query 可依受控政策自動重試。
- Mutation 只在 Action 支援 idempotency key 時允許自動重試。
- Idempotency key 應至少包含 Tenant、Process Instance、Node ID 與穩定的 execution attempt 識別。
- Retry 後仍失敗時建立 Incident，不可靜默跳過節點。

## 8. 錯誤、Incident 與維運

第一版建議行為：

- Data Action 執行失敗時保留 Flowable job failure，並建立／關聯 FlowMint Incident。
- Incident 記錄 Tenant、Process Instance、Process Version、Node ID、Action Code／Version、attempt、錯誤分類與 correlation ID。
- 維運畫面可重試或終止，但重試前必須重新檢查流程狀態與冪等記錄。
- 不在錯誤訊息、Audit 或 Incident 儲存 API Key、credential、完整 SQL 或未遮罩敏感資料。
- 後續若支援 BPMN Boundary Error Event，必須使用受控 error code catalog，不接受任意 exception expression。

## 9. Audit 契約

每次 System Task attempt 至少記錄：

- Tenant ID、Process ID／Version、Flowable Process Instance ID、Node ID。
- Action Code／Version 與 Action type。
- Attempt number、開始／結束時間、duration、狀態。
- Request／Response 的受控摘要或 hash，不儲存密碼與敏感原文。
- 寫回的 Form Data path 與變更前後摘要。
- Correlation ID、idempotency key hash、Incident ID 與錯誤分類。

Audit 必須和已發布版本綁定，不可只記錄當前 Action 主檔的最新狀態。

## 10. 權限與 Tenant 邊界

- 只有具有流程設計與發布權限的帳號可設定 System Task。
- Designer catalog、發布驗證與 Runtime 必須各自重新強制 Tenant 邊界。
- Runtime 不接受來自表單資料的 Tenant ID、Action Version、DataSource ID 或 credential。
- Data Action 能否在 BPMN 執行應有獨立 capability，不應因「可由 Form 呼叫」就自動允許「可由 System Task 呼叫」。
- 已發布流程只能使用發布當時通過驗證的固定 Action Version。

## 11. 資料模型待決策

實作前應先比較下列兩種方案：

### 方案 A：設定全部存於 BPMN extension elements

優點是版本內容單一且易於匯出；缺點是查詢、交叉驗證與結構變更較複雜。

### 方案 B：新增 Process Version System Task 設定表

優點是可使用正式 DTO、constraint、Mapper 與 catalog 交叉驗證；缺點是必須保證 BPMN node 與設定表同步，匯入／匯出也要包含兩者。

建議採方案 B：BPMN XML 只保留 `taskType` 與 `bindingId`，其餘設定以 Process Version 子資料維護。發布時將 BPMN XML、System Task 設定、Task Policy、Assignment Rule 與 Form Rule 作為同一個不可變版本單位。

## 12. API 規劃

建議擴充 `FM_PROG004D0001` Draft command，一次儲存 BPMN 與 System Task bindings，避免分開儲存造成半套設定。至少需要：

- Data Action catalog／metadata 查詢：可沿用現有受權實 API。
- Draft save：BPMN XML 與 System Task binding commands 同一交易儲存。
- Load：回傳 BPMN 與該版本的 bindings。
- Validate／Publish：後端依最新資料重做完整交叉驗證，不接受前端「已驗證」標記代替。
- Clone version：連同 System Task bindings 複製，並產生新的版本層 OID；node ID 及 binding ID 是否保留應一致且可驗證。

## 13. 測試與驗收

### 13.1 前端

- Palette 可建立 Data Action Task，XML export／import 後屬性不遺失。
- 屬性面板只顯示同 Tenant 可用 Action Version。
- Request／Response Mapping 可使用 metadata 選擇，錯誤時可定位到 node 與欄位。
- Draft 可編輯；Published／Retired 唯讀。
- 複製、刪除、undo／redo 不會留下孤兒 binding。

### 13.2 後端單元／契約測試

- 拒絕 Script Task、任意 Service Task、class、expression 與 delegate expression。
- 拒絕跨 Tenant、停用、未發布或版本不存在的 Action。
- 拒絕 request／response key、Form path、型別、target 衝突與保留變數覆寫。
- Runtime XML 只產生固定 `fmDataActionTaskDelegate`。
- Clone／publish 完整保留固定版本引用。

### 13.3 Flowable 整合測試

- Start → Data Action Task → User Task／End 正常流轉。
- Query 與 Mutation Action 的 input／output mapping 正確。
- Form Data、Flowable variable、Revision、Snapshot 與 Audit 一致。
- timeout、可重試錯誤、不可重試錯誤與重試用盡。
- 重複 job execution 不會重複建立外部單據。
- Incident 重試與終止對 FlowMint／Flowable 狀態一致。
- 舊 Process Version 在 Action 新版發布後仍執行原固定版本。

### 13.4 安全測試

- XML namespace、XXE、未允許 extension element 與屬性注入。
- 偽造 Tenant／Action Version／DataSource／credential。
- 透過 mapping 試圖覆寫保留流程變數。
- 敏感 request／response／exception 不會完整進入 log、Audit 或 Incident。

## 14. 分階段交付

### Phase 0：契約與資料模型

- 確定 XML namespace、binding schema、資料表／DTO、保留變數與 Action capability。
- 完成發布驗證與 Runtime 轉換的測試先行規格。

### Phase 1：Designer Draft

- Moddle descriptor、Palette、Context Pad、Renderer 與屬性面板。
- Draft save／load／clone 與 orphan binding 檢查。
- 本階段仍不允許發布含 System Task 的流程。

### Phase 2：Publish

- BPMN 白名單、Action／Form metadata 交叉驗證。
- 固定 Delegate Runtime XML 轉換與 Flowable parse。
- 通過所有反向安全測試後才允許發布。

### Phase 3：Runtime Query

- 先開放無副作用 QUERY Data Action。
- 完成 mapping、Form Data 同步、Audit、timeout 與 Incident。

### Phase 4：Runtime Mutation

- 僅對支援 idempotency 契約的異動 Action 開放。
- 完成 retry、外部系統不確定結果處理與補償策略。

### Phase 5：維運與 E2E

- Incident 詳情、受控重試、終止與 correlation 查詢。
- MariaDB、Flowable、瀏覽器及真實外部系統 E2E。

## 15. 驗收完成條件

下列條件全部滿足前，不得將 System Task 標記為正式完成：

1. Designer 可完整建立、編輯、複製、刪除、匯入與匯出 Data Action Task。
2. 後端對任意 Script／class／expression／跨 Tenant 輸入有獲立防線。
3. Process Version 固定 Action Version，舊 instance 行為不漂移。
4. Runtime 的 mapping、交易、Snapshot、Audit、timeout、retry、idempotency 與 Incident 通過整合測試。
5. Query 與 Mutation 各至少一個真實業務流程通過瀏覽器／Flowable／MariaDB E2E。
6. 維運人員可從 Process Instance 追到 System Task attempt、Data Action Audit 與 Incident。

## 16. 現況結論

截至 2026-09-04，第一版已依「受控 `bpmn:serviceTask` + 固定後端 Delegate + 固定 Data Action Version」完成下列能力：

- Designer 已註冊 FlowMint moddle descriptor、Data Action Task Palette 與專用屬性面板。
- 屬性面板只能選擇目前 Tenant 的已發布 Data Action，並固定 Action Version；可設定 request／response mapping。節點 timeout 在具備可強制執行的 Runtime 契約前不提供設定。
- 設計 XML 使用 `flowmint:*` 屬性保存設定；發布產生 Runtime XML 時轉為 Flowable 原生 `fieldExtension`，避免 Flowable converter 丟失未知 namespace attribute。
- 後端只接受 `taskType=DATA_ACTION` 的受控 Service Task，拒絕 Script Task、任意 class／expression／delegate expression、未知 FlowMint 屬性及其他 namespace 擴充。
- 發布前會依 Tenant 驗證 Action 與固定版本存在、啟用且為 `PUBLISHED`。
- Runtime 一律改寫為固定 `${fmDataActionTaskDelegate}` 並以 async job 執行；Delegate 透過既有 Data Action 執行服務完成 request mapping、執行與 response mapping，並以 lock version compare-and-set 更新目前 Form Data。

本版允許 `QUERY`、`COMMAND` 與 `TRANSACTION`。QUERY 沿用 Flowable job retry；異動型 Action 的 Runtime XML 固定使用 `R0/PT1M`，不進行自動重試，失敗 job 留待管理員確認外部結果後處理，避免不明確失敗造成重複異動。本版仍是 MVP：尚未建立 System Task 專屬 attempt／Incident、Form Snapshot，也尚未完成真實 MariaDB／Flowable／瀏覽器 E2E。Context Pad 轉換、複製／匯入回歸及 Data Action 的 BPMN 專用 capability 亦待後續補齊。原生 BPMN Script Task 維持禁止。

本次驗證結果：後端針對性測試 12 項通過，Nuxt production build 通過，`git diff --check` 通過；未異動 MariaDB 資料或結構。
