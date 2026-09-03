# FlowMint

> Open-source workflows, approvals made simple. 讓每一次簽核，都流暢有跡。

FlowMint 是一套開源的企業電子表單簽核系統平台(BPM)，整合視覺化表單設計、BPMN 流程編排、組織簽核人解析、待辦處理、通知、稽核、異常處理與營運報表。平台支援多租戶（Tenant）資料隔離，讓企業能以設定方式建立請購、驗收、費用、名片及其他內部申請流程，不必為每一種單據重新開發一套系統。

FlowMint 的定位是「可配置的簽核平台」，不是 HR、ERP、法人主檔或專案管理系統。請購單、費用申請等業務單據應透過內建的表單與流程設計器配置，而不是寫死在平台程式中。

## 使用 FlowMint

FlowMint 將一個可執行的簽核應用拆成「組織與人員」、「表單」、「簽核流程」及「執行權限」四部分。建議依下列順序初始化；前一步尚未完成時，不要先發布後面的流程：

```text
建立 Tenant 與使用者 membership
  → 建立員工、任職、部門與主管關係
  → 建立簽核群組及核決規則
  → 設計、驗證並發布 Form
  → 設計 BPMN
  → 為每個 User Task 綁定 Published Form、Task Policy 與 Assignment Rule
  → Resolver Preview 與發布檢查
  → 發布 BPMN Process
  → 到申請中心測試起單、簽核、退回及流程進度
```

### 1. 初始化一個租戶專案

FlowMint 以 Tenant 隔離資料。一個新 Tenant 至少需要完成以下設定，才具備可起單及可指派的基本條件：

1. 建立 Tenant，並將管理者與測試帳號加入該 Tenant 的 membership。
2. 建立組織版本、根部門及下層部門，設定組織層級。
3. 建立員工與任職資料；每位申請人應有一筆有效的主要任職。
4. 設定部門主管、直屬主管、職稱或部門職務。流程若使用主管 Resolver，對應關係不可缺少或形成循環。
5. 建立流程會使用的簽核群組，設定模式、成員、順序及有效期間。
6. 配置流程的起單政策，以及使用者需要的 QIFU4 Program／Role 權限。

FlowMint 不在 README 提供預設帳密。登入帳號必須同時具備有效的 QIFU4 身分、Tenant membership，以及操作對應功能的 Program／Role 權限。

### 2. 設計與發布 Form

表單設計入口為 `FM_PROG005D0001`。建立表單主檔時先決定穩定且不可任意變更的 Form Code，例如 `LEAVE_REQUEST`；系統會建立可編輯的 Draft 版本。

1. 使用 Form.io Designer 放入文字、數字、日期、選項、Email、Container、Columns、Table、Data Grid／Edit Grid、附件等元件。
2. 為每個輸入元件設定唯一的 `key`。BPMN Gateway、核決規則、Data Action 與 Custom JavaScript 都以 key 讀取欄位，不要以顯示名稱作為程式契約。
3. 設定 required、型別、長度、數值範圍、格式與明細欄位驗證。送單時後端會依同一份 Published Schema 再驗證。
4. 如需查詢選項或回填外部資料，先發布 DataSource Pool／Data Action，再於表單建立 Binding；資料庫密碼及任意 SQL 不會下放瀏覽器。
5. 如需欄位連動或送單前檢查，可加入表單級 Custom JavaScript，並在 Designer 內完成格式檢查與 Preview。
6. 使用 Preview 測試初始載入、欄位變更、明細增刪、Data Action、附件與送單驗證。
7. 驗證通過後發布 Form。Published 版本不可直接修改；後續調整應建立下一個 Draft 版本。

Custom JavaScript 支援 `onFormLoad`、`onFieldChange`、`beforeSubmit`、`afterSubmit` 等 lifecycle。它適合處理介面連動及補充驗證，但關鍵權限、金額與資料完整性仍由後端驗證，不能只依賴瀏覽器 Script。

### 3. 設計與發布 BPMN 流程

流程設計入口為 `FM_PROG004D0001`。建立流程主檔時設定穩定的 Process Key，例如 `LEAVE_APPROVAL`，再於 Draft 版本使用內嵌 bpmn-js Designer 編輯流程。

1. 建立一個 Start Event、所需的 User Task／Gateway，以及可到達的 End Event。
2. 每個 User Task 使用穩定且唯一的 Task Definition Key；版本升級時若節點業務意義相同，應保留相同 key。
3. 點選 User Task，在同一個節點屬性面板選擇一張同 Tenant、已發布的 Form Version。每個 User Task 只能綁定一張表單。
4. 設定 Task Policy，包括允許核准、駁回、退回、轉派、代理、加簽與否，意見是否必填，以及 SLA、提醒、自簽與重複簽核政策。
5. 設定 Assignment Rule，例如申請人部門主管、直屬主管、上層主管、簽核群組、組織職稱、核決權限或固定帳號。
6. 設定該節點的表單欄位權限：`HIDDEN`、`READ` 或 `EDIT`。申請內容通常唯讀，只有補件節點開放必要欄位修改。
7. Gateway 條件只引用已綁定表單中的受控欄位，並為可能無法命中的分支設定 default flow。
8. 使用 Resolver Preview 帶入實際申請人與測試表單資料，確認解析出的簽核帳號、順序與略過原因。
9. 執行發布檢查。所有 User Task 都必須具有有效 Form Binding、Task Policy 及 Assignment Rule，BPMN 結構與條件也必須通過驗證。
10. 發布流程。FlowMint 會在部署版本中為 User Task 注入 Assignment Listener，部署至 Flowable；設計器保存的原始 BPMN 不需要手工加入 listener。

發布順序一定是 **Form 先、BPMN 後**。流程綁定的是明確的 Published Form Version，不會自動跟隨表單的最新草稿。表單建立新版本後，必須建立流程新 Draft、重新選擇該 Form Version、完成 Preview 與發布檢查，再發布新的 Process Version。既有流程實例仍使用啟動當時的版本，不會中途漂移。

### 4. 起單與簽核

1. 登入後由 `/workspace` 查看待辦、最近申請與通知摘要。
2. 前往 `/requests/start` 申請中心，依分類或搜尋選擇可發起的流程。
3. 在 `/requests/start/[processDefId]` 填寫正式表單、執行表單 Script／Data Action、上傳附件並送出。
4. 簽核人在 `/tasks/[taskId]` 查看流程指定的表單版本與欄位權限，依 Task Policy 執行核准、駁回、退回、轉派、代理或加簽。
5. 申請人可在 `/requests/[processInstanceId]` 查看單據狀態、目前關卡、目前簽核人、表單快照、附件及 BPMN 流程進度。

遇到找不到簽核人、組織資料失效或解析設定錯誤時，FlowMint 會建立 Assignment Incident，不會默默略過必要簽核。營運人員可由 `/operations/incidents` 處理，並在 `/operations/processes` 與 `/operations/reports` 追蹤流程及統計。

### 5. 版本與發布原則

- Draft 可反覆編輯、驗證及 Preview；Published 內容不可直接改寫。
- Form 與 BPMN Process 各自管理版本；相同的 `v1` 只代表各自主檔的第一個正式版本。
- BPMN 的每個 User Task 固定綁定一個已發布 Form Version。
- 發布 BPMN 時才產生 Flowable deployment，並將 FlowMint Assignment Listener 注入部署內容。
- 新版本只影響之後建立的流程實例；執行中的實例繼續使用原本的 BPMN、Form 與指派快照。
- 正式上線前至少測試正常核准、條件分流、金額邊界、退回補件、駁回、附件權限、找不到簽核人與跨 Tenant 存取。

## 程式操作手冊

### 程式入口總表

實際選單名稱由 QIFU4 Program 設定決定；若帳號看不到某支程式，先檢查 Role 是否已配置該 Program，而不是直接輸入其他 Tenant 或繞過權限。

| 程式／頁面 | 用途 | 建議使用時機 |
|---|---|---|
| `FM_PROG001D0001` | Tenant 與 membership | 建立租戶、加入或停用租戶帳號 |
| `FM_PROG002D0001` | 員工與任職 | 建立員工、主要任職及直屬主管 |
| `FM_PROG002D0002` | 部門與組織樹 | 建立部門階層、檢視組織樹 |
| `FM_PROG002D0003` | 組織簽核層級 | 定義課、部、處等由低至高的核決層級 |
| `FM_PROG002D0004` | 組織職稱 | 建立職稱並連結簽核層級 |
| `FM_PROG002D0005` | 部門主管 | 指定 HEAD、DEPUTY_HEAD、ACTING_HEAD |
| `FM_PROG002D0006` | 部門職務 | 定義特定部門職務及擔任人 |
| `FM_PROG003D0001` | 簽核群組 | 建立財務、法務、資訊等可重用簽核群組 |
| `FM_PROG003D0002` | 工作代理 | 設定期間代理與代理範圍 |
| `FM_PROG004D0001` | BPMN 流程設計 | 設計、驗證、Preview 及發布流程 |
| `FM_PROG005D0001` | Form 表單設計 | 設計、試跑及發布 Form.io 表單 |
| `FM_PROG006D0001` | DataSource Pool | 建立供 Data Action 使用的外部資料庫連線 |
| `FM_PROG006D0002` | Data Action | 建立受控查詢或異動，供表單欄位使用 |
| `FM_PROG010D0001` | AI Provider | 設定簽核解說使用的 AI 供應商與模型 |
| `FM_PROG010D0002` | 外部 API Client | 建立外部系統 Client、Key、Scope、配額及 IP 白名單 |
| `FM_PROG010D0003` | 外部 API 說明 | 查看外部發單及流程查詢 API 的使用方式 |
| `/workspace` | 工作台 | 查看待辦、最近申請與通知摘要 |
| `/requests/start` | 申請中心 | 搜尋流程並開始填單 |
| `/tasks/[taskId]` | 待辦處理 | 核准、駁回、退回、轉派、代理或加簽 |
| `/requests/[processInstanceId]` | 申請詳情 | 查看表單、附件、簽核軌跡與 BPMN 進度 |
| `/operations/incidents` | 指派異常 | 處理找不到簽核人等異常 |
| `/operations/processes` | 流程監控 | 查詢執行中及已完成流程 |
| `/operations/reports` | 營運報表 | 查看流程數量、處理時間與逾期待辦 |

### 共通操作方式

- 清單頁先輸入查詢條件，再按「查詢」；「清除」只重設條件，不會刪除資料。
- 「新增」會進入建立頁；清單中的「編輯」會載入該筆資料及其明細。
- 建立成功後，Tenant、Form Code、Process Key、Action Code 等穩定識別通常不可修改；名稱及說明才是顯示用途。
- `ACTIVE` 代表可供 Runtime 使用；停用只阻止之後的新使用情境，不應破壞既有流程稽核資料。
- 有生效與失效時間的設定，以 Runtime 執行當下是否落在有效期間判定。
- 刪除或停用前先確認沒有流程、任職、群組或版本仍在引用；畫面若拒絕操作，應先解除相依關係。
- 表單、流程及 Data Action 採版本發布制；「儲存草稿」與「發布」是不同動作。

### Tenant 與帳號：`FM_PROG001D0001`

1. 在清單以 Tenant 代碼或名稱查詢；按「新增」建立 Tenant。
2. 填寫 Tenant ID、Tenant 代碼、名稱、預設語系、預設時區、狀態與說明後儲存。Tenant ID／代碼建立後視為穩定識別。
3. 進入編輯頁，在 membership 區輸入既有 QIFU4 帳號；若勾選建立新帳號，另填初始密碼與確認密碼。
4. 設定生效時間及是否為該帳號的預設 Tenant，再按加入。
5. membership 清單可重設密碼、停用或重新啟用。停用 membership 不等於刪除 QIFU4 帳號。
6. 使用該帳號重新登入，確認能取得正確 Tenant，且不能存取其他 Tenant 的資料。

### 員工、部門與組織

#### 員工與任職：`FM_PROG002D0001`

1. 以員工編號、帳號或姓名查詢，按「新增」。
2. 選擇 Tenant，填寫員工編號、登入帳號、顯示名稱、Email、手機、語系、時區、狀態與有效期間。
3. 儲存員工後，在任職區選擇部門、職稱及直屬主管來源。
4. 若主管來源為指定任職，選擇「指定直屬主管」；若由組織規則解析，確認部門主管資料已建立。
5. 設定主要任職、狀態與有效期間後儲存。每位申請人在同一時間應只有一筆主要任職。
6. 兼任可新增其他任職，但流程的申請人部門及主管鏈預設以主要任職解析。

#### 部門與組織樹：`FM_PROG002D0002`

1. 先建立根部門，再建立下層部門；填寫部門代碼、名稱、簡稱、父部門、類型、排序、虛擬部門、狀態與有效期間。
2. 一般部門選擇 `DEPARTMENT`；純分類或矩陣組織才使用 `VIRTUAL`，避免讓虛擬節點誤入主管鏈。
3. 使用「組織樹」選擇 Tenant 並載入樹狀結構；必要時勾選包含停用資料。
4. 調整父部門前先確認不會形成循環；儲存時帶入畫面取得的版本，避免覆蓋其他管理者的異動。

#### 簽核層級、職稱、主管與職務

1. 在 `FM_PROG002D0003` 建立層級方案，設定方案代碼、名稱、是否預設、狀態與有效期間；於層級明細由低至高加入層級及排序。
2. 在 `FM_PROG002D0004` 建立職稱，選擇其簽核 Level，設定是否為主管職稱、排序及有效期間。
3. 在 `FM_PROG002D0005` 選擇部門與主管員工，設定主管類型、優先序及有效期間。正常主管使用 `HEAD`；副主管及代理主管依實際職責使用對應類型。
4. 在 `FM_PROG002D0006` 選擇部門，建立職務代碼、名稱、用途及有效期間，再於擔任人區加入員工並指定主要擔任人。
5. 完成後用 BPMN 的 Resolver Preview 分別測試 `INITIATOR_ORG_HEAD`、`DIRECT_MANAGER`、`ORG_TITLE` 或 `ORG_DUTY`，不要只確認主檔已儲存。

### 簽核群組：`FM_PROG003D0001`

1. 建立群組代碼、名稱、處理方式、狀態與說明。
2. `CANDIDATE` 表示任一有效候選人可處理；`ALL` 表示群組內每位有效成員都必須完成；`SEQUENTIAL` 依優先序逐一處理。
3. 儲存主檔後，在成員區選擇員工、設定優先序、狀態及有效期間。
4. 編輯或刪除成員後重新執行流程 Resolver Preview，確認實際帳號與順序。
5. BPMN User Task 的派送方式必須與群組處理方式相容，否則發布檢查會拒絕。

### 工作代理：`FM_PROG003D0002`

1. 選擇 Tenant、被代理人及代理人。
2. 選擇代理範圍：全部工作、指定流程或指定簽核群組；後兩者需再選對應識別碼。
3. 設定是否允許再次轉代理、狀態、開始時間、結束時間及原因後儲存。
4. 代理必須已生效且雙方帳號有效。工作代理只影響待辦處理，不授權代理人代替委託人發起申請。
5. 代理人收到 Delegated Task 後應先處理並 Resolve 歸還 owner；不能把代理中的 Task 當成一般待辦直接核准。

### Form 完整操作：`FM_PROG005D0001`

1. 在清單按「新增」，選擇 Tenant，輸入不可重複的表單代碼、表單名稱、狀態與說明；儲存後系統自動建立 Version 1 Draft。
2. 進入編輯頁並選擇 Draft 版本。從左側工具箱拖曳元件到畫布，點選元件設定 Label、Key、預設值、驗證與顯示規則。
3. 可使用「加入單據編號」建立平台管理的唯讀 `documentNumber`，或使用「加入申請人欄位」加入 Runtime Context 欄位；不要另寫 Script 偽造這些系統資料。
4. 附件元件設定允許類型、單檔大小、檔案數量與總容量。Preview 不會真的上傳附件，正式附件需到申請頁驗證。
5. Custom JavaScript 應保持可讀格式，使用 lifecycle 與 `ctx` API，不直接存取密碼、資料庫或任意外部網址。
6. 若使用 Data Action Binding，選擇已發布 Action、事件、request mapping 及 response mapping；先在 Action Preview 驗證，再測試表單 Preview。
7. 按「儲存草稿」保存 Designer、Schema、Script 與 Binding；再以 Preview 輸入資料並按「驗證送出」。
8. 修正所有 required、型別、元件 key、Script 及 Binding 錯誤後按「發布草稿」。發布後該版本只可檢視。
9. 需要修改時按「建立新版本」，由最新版複製成 Draft；修改、Preview、發布後，再到 BPMN 新版本重新綁定。

### BPMN 完整操作：`FM_PROG004D0001`

1. 在清單按「新增」，選擇 Tenant，輸入流程代碼、名稱、分類、分類內排序、狀態與說明；儲存後系統建立 Version 1 Draft。
2. 若沒有合適分類，可在分類管理輸入分類代碼、顯示名稱、Bootstrap Icon 與排序後儲存。申請中心會使用此 metadata 顯示流程。
3. 在畫布建立單一 Process，以及可完整連通的 Start Event、User Task、Gateway、Sequence Flow 與 End Event。
4. 點選 User Task，確認節點代碼與名稱，再選擇「顯示表單」。清單只會列出同 Tenant 的 Published Form。
5. 設定派送方式、自簽政策、重複簽核政策、意見必填、處理期限、提前提醒，以及允許的退回、駁回、轉派、代理和加簽動作。
6. 設定解析方式及其必要參數：固定帳號、簽核群組、目標層級、職稱、職務或表單人員帳號欄位等。
7. 設定欄位權限。原始申請資料通常採 `READ`，簽核專用欄位採 `EDIT`，內部或不適用欄位採 `HIDDEN`。
8. 對 Exclusive／Inclusive Gateway 設定條件及 default flow；不得在 Parallel Gateway outgoing flow 上設定不會生效的條件。
9. 按「儲存草稿」。Resolver Preview 使用已儲存規則，因此修改節點後要先儲存，再輸入測試表單 JSON 執行 Preview。
10. 分別以一般申請人、兼任人員、簽核人本人及邊界金額測試解析結果。
11. 按「發布草稿」。後端會驗證 BPMN、Process Key、每個 User Task 的 Form Binding／Policy／Rule、Gateway 條件及 Resolver，成功後才部署至 Flowable。
12. 已發布版本只能檢視；修改流程時使用「建立新版本」，重新確認所有節點設定後再發布。

### DataSource Pool 與 Data Action

#### DataSource Pool：`FM_PROG006D0001`

1. 選擇 Tenant，填寫連線池代碼、名稱、資料庫類型、JDBC URL、帳號、密碼及連線池參數。
2. 驗證 SQL 應使用該資料庫安全且低成本的查詢，例如 `SELECT 1`。
3. 按「測試連線」，確認畫面回傳資料庫產品、版本及耗時後再儲存並啟用。
4. 編輯時密碼留空表示沿用現有密碼；輸入新密碼會取代原設定。
5. 正被 Published Data Action 使用的連線池不要任意停用。

#### Data Action：`FM_PROG006D0002`

1. 選擇 Tenant 與 DataSource Pool，設定 Action Code、名稱、類型、Request Schema、每分鐘呼叫上限與說明。
2. 依執行順序新增 Step，設定 Step Code、Statement、Execution Mode、SQL、Result Key、Result Mode、逾時及最大回傳筆數。
3. SQL 使用 Named Parameter；值由 Request、Server Context、目前 item 或前一步結果綁定，不以字串拼接使用者輸入。
4. 批次處理選擇 `FOR_EACH` 並設定 Array Path；有條件執行時設定 Continue Condition。
5. 先儲存草稿，再輸入測試 JSON 執行 Preview。Preview 預設 Rollback；確認每一步參數、結果與異動筆數。
6. 驗證預期異動筆數、錯誤回滾及空結果後發布版本。Form Binding 只能正式引用 Published Action。
7. 由 Execution Audits 查看執行狀態、耗時與 digest；稽核資料不應保存明文密碼或敏感內容。

### AI Provider：`FM_PROG010D0001`

1. 選擇 Tenant，輸入 Provider 代碼、顯示名稱及 Provider 類型。
2. 填寫 Base URL、Model ID、API Key、Temperature、最大輸出 Token 與逾時秒數。
3. 同一 Tenant 可指定一個預設 Provider；啟用前先執行連線測試。
4. 編輯時 API Key 留空表示不變；輸入新值會立即取代舊 Key，且無法由畫面讀回。
5. Provider 停用或測試失敗時，Task AI 解說不可假裝成功，應查看錯誤訊息及稽核紀錄。

### 外部 API Client：`FM_PROG010D0002`、`FM_PROG010D0003`

1. 建立 Client Code、名稱、系統類型、每分鐘上限、每日配額、狀態與說明。
2. 只勾選必要 Scope，並限制允許的流程、Initiator 帳號與 IP Allowlist。
3. 產生或輪替 API Key 後立即複製並交由外部系統安全保存；明文 Key 關閉視窗後不再顯示。
4. 外部系統依 `FM_PROG010D0003` 顯示的契約傳送 Tenant、Client、API Key、Idempotency Key 與 request body。
5. 重送同一業務請求必須沿用 Idempotency Key；不要以重建 Key 或改變 business key 規避重複送單保護。
6. 上線前測試有效請求、Scope 不足、IP 不符、配額超限、重複請求及流程狀態查詢。

### 從零建立第一支流程：請假申請範例

以下範例只示範操作順序，實際欄位與核決規則仍依公司制度設定：

1. 在 `FM_PROG001D0001` 建立 Tenant，加入申請人、主管及管理者帳號。
2. 在 `FM_PROG002D0002` 建立公司根部門與申請人部門。
3. 在 `FM_PROG002D0001` 建立申請人與主管，配置有效主要任職。
4. 在 `FM_PROG002D0005` 將主管設為申請人部門的 `HEAD`。
5. 在 `FM_PROG005D0001` 建立 `LEAVE_REQUEST`，加入假別、開始日期、結束日期、天數及原因，完成 Preview 後發布 Form v1。
6. 在 `FM_PROG004D0001` 建立 `LEAVE_APPROVAL`：Start → `managerApproval` → End。
7. 選取 `managerApproval`，綁定 `LEAVE_REQUEST v1`，設定原申請欄位唯讀、簽核意見可編輯，Resolver 選擇 `INITIATOR_ORG_HEAD`。
8. 儲存流程後，以申請人帳號執行 Resolver Preview；確認解析結果是預期主管，再發布 Process v1。
9. 以申請人登入 `/requests/start` 起單；以主管登入 `/tasks/[taskId]` 核准。
10. 回到 `/requests/[processInstanceId]` 確認狀態為完成，並檢查表單快照、簽核動作及 BPMN 進度。

### 常見問題排除

| 狀況 | 優先檢查 |
|---|---|
| 選單看不到程式 | QIFU4 Program 是否註冊、Role 是否授權、帳號是否有有效 Tenant membership |
| 申請中心看不到流程 | Process 是否 Active 且 Published、分類是否有效、起單政策是否允許目前帳號 |
| BPMN 選不到表單 | Form 是否屬於同一 Tenant 且已發布；Draft 不會出現在清單 |
| 流程發布顯示 User Task 設定不完整 | 該節點是否同時具備 Form Binding、Task Policy 與至少一條 Assignment Rule |
| Resolver Preview 找不到人 | 員工、membership、主要任職、主管／群組成員及有效期間是否完整 |
| Gateway 沒有走預期分支 | 條件使用的欄位 key、型別與測試 JSON 是否一致，是否有 default flow |
| Form Preview 可過但正式送單失敗 | 後端會重新驗證 Schema、欄位權限、Script、Data Action 與附件；依畫面錯誤逐項修正 |
| Published 版本無法編輯 | 這是不可變版本設計；建立新 Draft 版本後修改 |
| Data Action Preview 失敗 | Pool 連線、Named Parameter、Request Schema、Step 順序、逾時與預期異動筆數 |
| 待辦沒有指派給任何人 | 到 `/operations/incidents` 查看解析路徑與失敗原因，修正組織或規則後再處理 Incident |
| 修改 Form 後流程仍顯示舊欄位 | 流程固定綁定明確 Form Version；建立流程新版本並重新綁定後發布 |

## 目前狀態

FlowMint Phase 1～5 平台能力、Runtime／Workspace 重構及 AI 簽核解說精靈第一版已完成程式實作，目前進入整合部署與實機驗收階段；「程式完成」不等同「瀏覽器 E2E 已驗收」。

- Phase 1～4 核心功能已完成。
- Phase 5 營運功能已完成。
- Workspace 已改為摘要與導覽，正式起單移至獨立申請中心及填單頁。
- 共用單據編號、正式附件、申請追蹤、目前簽核人與 BPMN 流程進度已完成 Java／Vue 實作與自動驗證。
- A01 的請購、採購單、驗收單與公司名片 Form／Process 目前均已發布，Process 已部署至 Flowable；仍需以多帳號完成逐級核決、條件分支、退回重驗、附件、簽核流轉與流程進度的瀏覽器 E2E。
- AI Provider 管理、OpenAI／Gemini／Groq／OpenRouter Adapter、Task AI 分析、快取與稽核已完成；尚未使用真實 API Key 完成 Provider 與瀏覽器 E2E，不能標示為正式上線。
- A01 現行的請購、採購單、驗收單與公司名片 Form／Process 均已重新整理為 `PUBLISHED v1`；所有 BPMN User Task 的 Form Binding 皆指向對應的 Published Form v1。發布與部署完成不等同真實業務 E2E 已驗收。
- 外部系統 API 管理、Client／Key／Scope／IP／配額、Request Audit、外部發單 Ledger、流程狀態查詢及獨立 API 說明頁均已實作並套用 MariaDB；仍待正式外部 Client 與多帳號端到端驗收。
- 正式業務表單屬應用配置，不計入平台本體程式。
- 尚待完成正式環境部署、Program／角色配置、完整瀏覽器 E2E 與真實資料量效能驗證。
- Email 成功與永久失敗狀態均可同步；FlowMint 以既有 notification 欄位保存重試與最終失敗，不修改 QIFU4 Mail Helper schema。

詳細紀錄請參考 [開發進度](backend/doc/18-開發進度.md) 與 [開發路線](backend/doc/12-開發路線.md)。

## 主要功能

### 多租戶與組織

- Tenant 資料邊界與帳號 membership。
- 員工、部門、職稱、職級與部門職務。
- 組織版本、部門樹與主要任職。
- HEAD、DEPUTY、ACTING 與直屬主管關係。
- 防止主管循環與跨 Tenant 資料存取。

### 簽核人解析

- 固定帳號與簽核群組。
- 申請人部門主管、上層主管與指定職級主管。
- 直屬主管與主管鏈。
- 部門職稱、部門職務與核決權限。
- Resolver Preview、解析路徑及指派快照。
- 找不到簽核人時建立 Assignment Incident，不遺失異常證據。

### 表單與資料動作

- Form.io 表單設計、版本及發布。
- Grid／Data Grid 品項明細。
- 節點欄位 Hidden／Read／Edit 政策。
- 表單自訂 JavaScript lifecycle。
- Data Action 與外部資料來源連線池。
- SQL 參數、結果 mapping、逾時與安全限制。
- 發布後版本不可變，Runtime 固定使用已發布版本。

### BPMN 與流程設計

- BPMN 流程設計、版本、發布與驗證。
- User Task Policy 與 Assignment Rule。
- 單人、候選人、全員會簽與循序簽核。
- 自我簽核及重複簽核人政策。
- 節點駁回、退回、轉派、加簽與意見必填政策。
- Task SLA 處理期限與提前提醒設定。

### 流程 Runtime

- 草稿、起單與代申請授權。
- Idempotency Key 防止重複送單。
- 我的申請、我的待辦與已辦。
- 核准、駁回、退回與補件重送。
- 申請人撤回與實際發起人取消。
- Task Transfer。
- Delegation／Resolve 與期間代理授權。
- 循序前加簽與完成加簽。
- Approval Group 多人模式。
- 不可變表單快照、指派快照及 Task Action 稽核軌跡。
- 共用正式單據編號，與內部 `businessKey`、冪等鍵分離。
- 申請詳情顯示目前關卡、等待簽核人及完整 BPMN 流程進度。

### Workspace、申請中心與附件

- Workspace 提供待辦、申請及通知摘要，起單導向獨立申請中心。
- 申請中心依 Tenant 級流程分類 metadata 顯示可發起流程，支援搜尋、分類及受控代申請人選擇。
- 獨立填單頁負責 Form.io、Custom JavaScript、Data Action、附件與冪等送出生命週期。
- 附件採 Upload Session 暫存，正式送單時同交易綁定流程；支援欄位限制、驗證下載及孤兒檔案隔離清理。
- 申請詳情與待辦共用正式附件 metadata，表單欄位和附件清單顯示相同檔名、大小及下載內容。

### 通知與 Email

- Workspace 站內通知中心。
- 單筆已讀、全部已讀與未讀統計。
- 待辦指派、流程完成、駁回、取消通知。
- 即將到期與逾時通知。
- 穩定事件 ID 與資料庫去重。
- QIFU4 `SysMailHelperServiceImpl`／Mail Helper Outbox 整合。
- QIFU4 Template Manager／FreeMarker 通知範本。
- Email 寄送成功、延遲重試與第三次失敗後永久 `FAILED` 狀態同步。

### 營運與稽核

- Assignment Incident 查詢、Retry、Reassign 與 Terminate。
- 流程實例監控、狀態及關鍵字查詢。
- 後端分頁與 `%`、`_` 搜尋字元 escape。
- 目前節點、流程耗時、最近期限與逾時數量。
- 任務動作時間軸與歷次表單快照。
- Tenant-scoped 完整稽核明細。

### 營運報表

- 流程狀態統計與平均完成耗時。
- 逾時與未來 24 小時內到期 Task。
- 每日起單量、完成量及當日平均耗時。
- 依流程定義統計起單量、完成率與平均耗時。
- User Task 節點平均及最長處理時間排名。
- 日期區間預設 30 天，最大 366 天。

### AI 簽核解說精靈

- `FM_PROG010D0001` 管理 Tenant-scoped OpenAI、Gemini、Groq 與 OpenRouter Provider。
- API Key 使用 AES-GCM 加密保存，管理畫面只顯示遮罩值，並提供受控 Provider 連線測試。
- 正式待辦可由目前合法處理人主動選擇 Provider 執行 AI 解說，不點擊就不呼叫外部服務。
- Context 只包含受控表單欄位、流程資料及已保存簽核歷史，排除密碼、Token、SQL、Script 與連線設定。
- 分析結果使用 Content Hash、Generation、快取與 append-only access ledger，保存受控錯誤碼、Token 用量及耗時。
- AI 只提供摘要、風險、問題與參考建議，不修改表單、不執行核准，也不取代 Resolver 或簽核責任。
- 第一版不解析附件內容；真實 Provider Key、多帳號權限及密鑰遮罩瀏覽器 E2E 尚待完成。

### 外部系統 API 與流程拋單

- `FM_PROG010D0002` 管理 Tenant-scoped API Client、Scope、來源 IP、期限、配額與狀態。
- API Key 僅在簽發或輪替時顯示一次，資料庫保存安全雜湊；管理畫面只顯示遮罩資訊。
- 外部請求經 Key、Client、Scope、IP、期限與配額驗證，並保存 Request Audit 與受控錯誤碼。
- 外部發單使用 Idempotency Key 與 Request Ledger，重試不會重複建立流程。
- 提供外部流程狀態查詢；Tenant、Client 與可用流程範圍均由後端強制限制。
- `FM_PROG010D0003` 提供獨立 API 說明頁，依 Springdoc OpenAPI 契約顯示可用端點。

## 完整功能說明

本節依實際程式、API、資料模型及 Phase 1～5 完成紀錄整理。請購單、費用申請、出差申請等特定業務內容，是運用下列平台能力建立的「應用配置」，不是寫死於 FlowMint 核心的獨立模組。

### 1. 登入、身分與 Tenant 邊界

- 沿用 QIFU4 帳號、登入、登出、Session、Cookie、CSRF 與系統管理機制，不另建一套帳號密碼。
- 前端頁面具有登入 middleware；未登入導向登入頁，缺少 Program 權限導向無權限頁。
- 操作者一律由 Spring Security Context 取得，API 不接受前端冒用其他登入帳號。
- Runtime 以 `X-FlowMint-Tenant` 指定操作 Tenant，後端再確認登入者具有啟用且在有效期間內的 membership。
- 同一帳號可加入多個 Tenant；起單入口只列出該帳號目前可用的 Tenant。
- Tenant、登入帳號、系統時間等 server context 優先於 request body，同名參數不能由前端覆寫。
- 所有 FlowMint 業務查詢、異動、快照與營運資料都包含 `TENANT_ID` 條件；跨 Tenant 資料以禁止存取或找不到處理。
- QIFU4 系統 Role、FlowMint Tenant membership、流程業務角色三者分離；組織職稱或簽核身分不會自動取得後台 Program 權限。

### 2. Tenant 管理（`FM_PROG001D0001`）

- 建立、查詢、修改 FlowMint Tenant 基本資料。
- 維護 Tenant 代碼、名稱、狀態、有效期間與稽核欄位。
- Tenant 是員工、組織、表單、流程、Runtime、通知及營運資料的最高業務邊界。
- 相同業務編號可以存在不同 Tenant，但同一 Tenant 內仍受唯一性規則限制。
- Tenant 停用或 membership 失效後，起單目錄及 Runtime 不再開放使用。
- Program registration SQL 只註冊頁面與選單，不會自動把 Tenant 管理權限授予角色。

### 3. 員工與任職管理（`FM_PROG002D0001`）

- 維護員工編號、姓名、QIFU4 登入帳號、Email、狀態及有效期間。
- 支援新增、查詢、編輯與停用；停用不刪除已被歷史流程引用的資料。
- 一位員工可有多筆部門任職，區分主要任職 `PRIMARY` 與兼任 `CONCURRENT`。
- 任職資料包含部門、職稱、有效期間等資訊；同一有效時間點只能有一筆主要任職。
- 起單政策與組織 Resolver 以申請人的有效任職為基礎。
- 支援設定直屬主管，主管必須是同 Tenant 的有效員工。
- 提供主管預覽，在保存前檢視實際主管解析結果。
- 防止自我主管及循環主管鏈，例如 A 管 B、B 又回頭管 A。
- 起單、轉派、加簽、代理與管理員 Reassign 都會在執行當下重新驗證員工及 membership。

### 4. 部門與組織樹（`FM_PROG002D0002`）

- 支援 `ROOT`、`DEPARTMENT`、`VIRTUAL` 組織單位類型。
- 維護父子關係、排序、狀態、有效期間與版本。
- 同時提供清單頁與樹狀瀏覽／編輯頁。
- 可依 `effectiveAt` 查詢指定時間點有效的組織樹。
- 支援樹節點搬移 Preview，先驗證 root、new parent、排序、生效日及 expected version。
- 正式搬移使用 `expectedVersionNo` 防止多人同時編輯互相覆蓋。
- 防止把節點移到自己或自己的子孫下，避免部門樹循環。
- 停用及改組不抹除舊版本，歷史指派仍由快照保留當時證據。
- Resolver 可沿父部門路徑查找本部門主管、上層主管、指定層級主管與根組織主管。

### 5. 組織簽核層級（`FM_PROG002D0003`）

- 建立 Tenant 專屬簽核層級方案，配置由低至高的組織層級與排序。
- 支援建立、修改、查詢及方案驗證。
- 驗證層級順序、重複設定與必要欄位。
- 提供「下一個較高層級」「指定層級」「層級主管鏈」Resolver 的正式依據。
- 不綁定課、科、部、處等固定名稱，可適應台灣、中國大陸及不同企業組織結構。

### 6. 組織職稱（`FM_PROG002D0004`）

- 在 Tenant 內維護職稱主檔、狀態及有效期間。
- 職稱可配置到員工任職資料。
- `ORG_TITLE` Resolver 可在指定組織範圍內尋找特定職稱的有效人員。
- 核決權限規則命中後也可導向指定組織職稱。
- 職稱只代表組織資料，例如「經理」，不等於 QIFU4 系統管理 Role。

### 7. 部門主管（`FM_PROG002D0005`）

- 逐部門配置正式主管 `HEAD`、副主管 `DEPUTY_HEAD`、代理主管 `ACTING_HEAD`。
- 每筆關係包含人員、主管類型、順序與有效期間。
- 主管解析同時檢查部門、員工、任職、狀態、有效日期及 Tenant。
- 支援申請人部門主管、父部門主管、下一層級主管、指定層級主管、主管鏈及根主管解析。
- 多人符合時依明確順序產生候選，不依資料庫未定義的自然順序。

### 8. 部門職務（`FM_PROG002D0006`）

- 維護部門內的業務職務，例如採購窗口、財務審核人或資安窗口。
- 一項職務可配置一或多位有效擔任人。
- 職務與職稱分離：職稱是頭銜，職務是流程需要尋找的業務責任。
- `ORG_DUTY` Resolver 可依部門與職務取得簽核人。
- 核決權限命中後也可將結果導向指定部門職務。

### 9. 簽核群組（`FM_PROG003D0001`）

- 建立 Tenant 內可重複引用的簽核群組，維護成員、順序、狀態及有效期間。
- `CANDIDATE`：成員成為同一 Task 候選人，其中一人處理即可。
- `ALL`：建立平行 Multi-instance，每位成員都必須完成自己的簽核。
- `SEQUENTIAL`：依群組成員順序逐一簽核。
- 流程發布時驗證 Assignment Rule 與群組 `ASSIGNMENT_MODE` 完全一致。
- 群組不存在、停用、無有效成員、設定格式錯誤或派送模式不一致時禁止發布。
- 群組可用於起單政策、Task Resolver、工作代理 scope 與核決權限結果。
- Runtime 保存實際解析成員快照，日後修改群組不會改寫歷史流程。

### 10. 工作代理（`FM_PROG003D0002`）

- 維護委託人、代理人、授權範圍、狀態及生效／失效時間。
- 支援全部工作 `ALL`、指定流程 `PROCESS`、指定簽核群組 `APPROVAL_GROUP` scope。
- 過期、未生效或停用授權不可使用。
- 工作代理與代起單授權分開，不能拿簽核代理權限冒用他人申請。
- `DELEGATE` 使用 Flowable delegation 保留原 Task owner，暫交代理人處理。
- `RESOLVE` 只允許目前代理人把工作交還原 owner。
- 代理中的 Task 不可直接核准、退回、駁回或補件重送。
- 委託與歸還皆保存 Task Action、表單快照與指派快照。

### 11. 流程設計與版本（`FM_PROG004D0001`）

- 建立流程主檔，維護流程代碼、名稱、狀態與目前版本。
- 建立及修改草稿版本；已發布版本不可直接改寫，變更必須建立新版本。
- 內嵌 BPMN Designer，支援 Start／End Event、User Task、Sequence Flow、Exclusive Gateway 與 Multi-instance。
- 保存 BPMN XML、版本號、內容 digest、發布者、發布時間及 Flowable deployment 關聯。
- 發布前驗證 BPMN 結構、表單綁定、Task Policy、Assignment Rule 與簽核群組設定。
- 發布時部署至 Flowable；Runtime 固定啟動明確的已發布版本。
- 設計器保存原始 BPMN，部署時才替 User Task 注入 FlowMint Assignment Listener。
- 已執行流程不會因 current version 改變而漂移至新流程定義。

#### 11.1 User Task Policy

- 逐節點設定派送方式：`ASSIGNEE`、`CANDIDATE`、`ALL`、`SEQUENTIAL`。
- 設定是否允許退回、駁回、轉派及加簽。
- 可分別規定各動作的簽核意見是否必填。
- 自我簽核政策：`ALLOW`、`SKIP_TO_NEXT`、`REQUIRE_ALTERNATE`、`INCIDENT`。
- 重複簽核人政策：`KEEP_EACH_LEVEL`、`MERGE_CONSECUTIVE`、`SKIP_ALREADY_APPROVED`。
- 設定節點表單欄位為 Hidden、Read 或 Edit。
- 設定 Task SLA 處理期限 1～8760 小時。
- 設定提前提醒小時數；必須大於等於 0 且小於處理期限。
- 未設定期限的節點不產生 due date，也不進入 SLA 通知掃描。

#### 11.2 Assignment Rule 與 Resolver

- 每個 User Task 可設定多條有順序的 Assignment Rule。
- `FIXED_ACCOUNT`：固定帳號。
- `APPROVAL_GROUP`：簽核群組。
- `INITIATOR_ORG_HEAD`：申請人主要部門主管。
- `PARENT_ORG_HEAD`：申請人父部門主管。
- `NEXT_HIGHER_LEVEL_HEAD`：下一個更高簽核層級主管。
- `TARGET_LEVEL_HEAD`：指定簽核層級主管。
- `LEVEL_HEAD_CHAIN`：依簽核層級展開主管鏈。
- `ROOT_ORG_HEAD`：根組織主管。
- `DIRECT_MANAGER`：員工直屬主管。
- `MANAGER_CHAIN`：沿直屬主管關係展開主管鏈。
- `ORG_TITLE`：指定組織範圍與職稱的人員。
- `ORG_DUTY`：指定部門職務擔任人。
- `APPROVAL_AUTHORITY`：依表單內容匹配核決權限。
- Resolver 只回傳同 Tenant、員工與 membership 有效的登入帳號。
- Preview 使用與 Runtime 相同的 Resolver 核心，可輸入申請人、節點與測試表單 JSON。
- Preview 顯示實際候選帳號與解析路徑，供發布前檢查。
- Runtime 保存規則、Resolver 類型、候選帳號、部門、職稱、層級與解析時間快照。

#### 11.3 核決權限

- 直接內嵌於流程設計，用表單金額、分類或其他欄位決定簽核路徑與人員。
- 規則依設定順序執行，可組合多項欄位條件。
- 欄位目錄由已發布 Form.io Schema 遞迴取得，包含 Container、Columns、Table 等巢狀欄位。
- 命中後可導向簽核層級、職稱、職務、簽核群組或固定帳號。
- 支援候選去重、`MAX_RESULTS` 與 `STOP_AFTER_APPROVAL`。
- Preview 可帶入真實表單變數驗證金額或分類條件。

#### 11.4 起單政策與代起單

- 支援 `ALL`、`ACCOUNT`、`ORG_UNIT`、`APPROVAL_GROUP` 起單政策。
- 無政策、無命中允許規則或命中任一拒絕規則時，一律拒絕起單。
- 拒絕規則優先，避免寬鬆 `ALL` 規則蓋過特定禁止條件。
- 本人申請仍須通過 Tenant、員工、主要任職與起單政策。
- 實際登入發起人與申請人不同時，必須命中有效 `fm_process_start_proxy` 授權。
- 代起單授權可套用全部流程或指定流程，並檢查狀態及有效期間。
- 流程保存實際發起人，表單保存申請人；主管鏈永遠依申請人組織解析。

### 12. 表單設計與版本（`FM_PROG005D0001`）

- 建立表單主檔及多個版本，支援草稿、驗證、發布與建立下一版。
- 內嵌 Form.io Designer，支援文字、數字、日期、選項、Email、Container、Columns、Table、Data Grid／Edit Grid 等元件。
- 保存 Form.io Schema、UI Schema、客製 JavaScript、Data Action Binding、內容 digest 與發布資訊。
- 發布後版本不可直接改寫；Runtime 固定使用流程正式綁定的表單 ID 與版本號。
- 流程可把表單綁定在起單或指定 User Task。
- 節點欄位政策支援 Hidden、Read、Edit，後端也會重新檢查可寫欄位。
- Form.io 輸入元件 key 必須在同一表單版本內唯一，包含 Data Grid／Edit Grid 內的子欄位。
- 客製 JavaScript 的 `onFieldChange` 由單一 `ctx` 取得事件，欄位 key 位於 `ctx.changed?.component?.key`。
- 純日期欄位使用 Form.io `input` 搭配 `inputType = date`，產生瀏覽器原生 `<input type="date">`。

#### 12.1 Submission 驗證

- 送單前由後端依已發布 Form.io Schema 再驗證，不只依賴瀏覽器。
- 驗證 required、型別、長度、數值範圍、pattern、Email、multiple。
- 遞迴驗證 Container 及 DataGrid／EditGrid 每筆明細。
- Schema 或資料錯誤會在建立 form data 與啟動 Flowable 前拒絕。
- 表單必須確實綁定於該流程版本，不能拿任意已發布表單啟動其他流程。

#### 12.2 客製 JavaScript

- 表單版本可保存模組化 JavaScript。
- 支援 `onFormLoad`、`onFieldChange`、`beforeSubmit`、`afterSubmit` lifecycle。
- Context 提供表單取值／設值、Form.io component、訊息及受控 Data Action 呼叫。
- `beforeSubmit` 可執行業務驗證並阻止送單。
- Designer 提供 Script 編輯、驗證、Preview 與執行訊息。
- Runtime 載入發布版本 Script，不會跟隨草稿變動。

#### 12.3 Data Action Binding

- 視覺化設定表單欄位與已發布 Data Action 的 request／response mapping。
- 支援 `FORM_LOAD`、`FIELD_CHANGE`、`BUTTON_CLICK`、`BEFORE_SUBMIT`、`AFTER_SUBMIT`、`SELECT_OPTIONS` 情境。
- Request Mapping 從表單資料組合參數；Response Mapping 把結果回填表單欄位。
- Binding Editor 提供 Action 選擇、欄位 path、測試資料與 Preview。
- `BEFORE_SUBMIT` Action 失敗時可阻止正式送單。

### 13. DataSource Pool（`FM_PROG006D0001`）

- 依 Tenant 管理外部 JDBC 連線池。
- 維護 Pool ID、名稱、JDBC URL、Driver、帳號、加密密碼、狀態與連線參數。
- 支援建立、查詢、編輯、停用與連線測試。
- Data Action 只引用 Pool ID，瀏覽器及表單不取得連線密碼。
- Runtime 受控建立／重用資料來源，避免無限制開啟外部連線。
- 同一交易 Action 的所有 Step 必須使用同一 Pool；目前不宣稱支援跨資料庫原子交易。

### 14. Data Action（`FM_PROG006D0002`）

- 建立可由不同表單重複使用的參數化資料操作。
- `QUERY` 只允許查詢；`COMMAND` 執行異動；`TRANSACTION` 在同一 Pool 交易內執行多 Step，任一步失敗即回滾。
- Action 採草稿／發布版本，表單正式引用固定版本。
- SQL Step 依 execution order 執行，支援 `SELECT_ONE`、`SELECT_LIST`、`INSERT`、`UPDATE`、`DELETE`。
- 支援單次 `ONCE` 與陣列批次 `FOR_EACH` 執行。
- SQL 使用 Named Parameter，JSON 值只作 JDBC value binding，不用字串串接 SQL。
- 參數可來自 request body、server context、目前 item 與前置 Step 結果。
- 保留 `tenantId`、`loginAccount`、`businessKey`、`processInstanceId`、`now`，且不能由前端覆寫。
- 後續 Step 可引用前一步 rows、affected rows 或 generated key。
- Result Mode 支援 `OBJECT`、`LIST`、`AFFECTED_ROWS`、`GENERATED_KEY`、`NONE`。
- 單筆無資料回傳 `null`，清單無資料回傳空陣列。
- 支援預期異動筆數檢查，不符時可使整體交易回滾。
- Preview 顯示解析參數、逐 Step 結果、異動筆數、耗時與錯誤位置。
- Transaction Preview 可 rollback，避免試跑污染外部資料。
- 發布前驗證 SQL、Named Parameter、Step、Pool 與 mapping。
- 公用入口為 `POST /api/fm/data-actions/{actionCode}/execute`。
- Query SELECT Step 支援受控 transient retry；單一 `ONCE / SELECT_LIST` Query 另提供 NDJSON Streaming 端點。
- 執行稽核可記錄 Action／版本、表單、流程、事件、操作者、時間、筆數、狀態與 request／response digest。

### 15. Workspace（`/workspace`）

- 作為登入後摘要與導覽，整合我的待辦、最近申請、通知及統計，不在頁面內直接 render 正式表單。
- 顯示真實待辦、申請及未讀通知統計，不使用展示假資料。
- 提供「前往申請中心」入口；起單目錄及填單職責由 `/requests/start` 路由承擔。
- Tenant 來自登入者 membership，不允許手寫其他 Tenant。
- 支援重新整理各區及由清單進入申請中心、Task、申請明細。
- 通知支援單筆已讀及全部已讀。

### 16. 正式起單

- `/requests/start` 依 Tenant 級 `fm_process_category` metadata 分類、排序並搜尋可發起流程；分類不是 Java／Vue 寫死常數。
- `/requests/start/[processDefId]` 是獨立填單頁，受控選擇本人或有效代理申請人，並管理 Form.io、Script、Data Action 與附件生命週期。
- Load 與 Submit 都重新檢查 Tenant membership、申請人在職、主要任職、起單政策及代起單。
- 只載入流程正式綁定的發布版 Form.io Schema、UI Schema、Script 與 Data Action Binding。
- 前端執行 Form.io、custom lifecycle 與必要 Data Action，後端仍會獨立重驗所有安全與資料規則。
- `submit` 必須提供 `Idempotency-Key`，防止連點及網路重送建立重複流程。
- 同 key、同內容重送回傳原結果；同 key、不同內容則拒絕。
- Tenant 內部 `businessKey` 不可重複；使用者可讀的 `documentNumber` 由 Tenant 單據規則產生，兩者分欄保存。
- 單據規則支援 Tenant 時區、年／月重設、受控格式 token 與交易內併發安全流水號；未配置單據類型的流程維持 UUID 識別。
- 同一交易建立 `SUBMITTED` 表單資料、啟動指定 Flowable definition、建立 `RUNNING` 流程索引、指派首關並寫入快照與 `SUBMIT` Action。
- 任一步失敗時 FlowMint 與 Flowable 一起回滾，不留下半完成流程。
- 不提供一般流程刪除 API；撤回、取消、駁回及終止都必須保留稽核。

### 17. 我的待辦與 Task 明細

- Inbox 只回傳登入者為 assignee 或 candidate 的有效 Task。
- Tenant 與登入帳號由 server context 決定，body 無法代查他人待辦。
- Task load 每次重新檢查 Task 狀態及操作者權限。
- 顯示 Form.io 表單、送單資料、節點政策、合法退回目標及歷次動作。
- 依欄位政策呈現 Hidden、Read、Edit。
- 顯示流程、申請人、節點、建立時間、due date、即將到期與逾時狀態。
- 顯示正式附件並提供具 Tenant／Task 權限檢查的下載；附件顯示物件不會被誤判為關卡欄位異動。
- 可開啟該實例的完整 BPMN 流程圖，已完成節點與目前節點分色標示，對話框可重複開關。
- 僅知道 Task ID 不足以操作，所有 action 都再次檢查 assignee／candidate／delegate 狀態。

### 18. 完整簽核動作

- `APPROVE`：檢查目前處理權與意見政策，保存快照和 Action 後完成 Task；末關完成時流程與表單轉 `COMPLETED`。
- `RETURN`：只在政策允許時使用；目標必須是同流程已完成的前置 User Task，表單轉 `RETURNED` 並重新建立合法節點指派。
- `RESUBMIT`：只允許申請人補件 Task；更新可編輯欄位、重跑 Schema 驗證、建立新 revision／快照後繼續原流程。
- `REJECT`：依政策檢查原因，保存證據後終止 Flowable；流程與表單轉 `REJECTED`。
- `TRANSFER`：政策必須允許；目標限同 Tenant 有效員工，清除原候選、指定新 assignee 並保存新指派快照。
- `DELEGATE`：必須有有效工作代理授權，Flowable 保留原 owner。
- `RESOLVE`：只允許目前代理人完成代理並把 Task 還給 owner。
- `ADD_SIGN`：政策必須允許；原處理人選擇同 Tenant 有效員工執行循序前加簽。
- `complete-add-sign`：加簽人只能留意見並歸還原處理人，不能直接核決流程。
- 所有動作都檢查目前狀態、Tenant、操作者與節點政策，並建立不可變表單快照及 Task Action。

### 19. 我的申請、撤回與取消

- 清單包含登入者本人申請及由登入者代他人發起的流程。
- 顯示業務單號、流程、申請人、實際發起人、狀態與目前節點。
- 只有表單 Owner 或實際發起人可查看明細。
- 明細提供完整 Action 時間軸、表單 revisions、快照內容、正式附件及下載。
- 預設摘要顯示目前等待關卡、指派類型與簽核人帳號／姓名；按「查看流程進度」才載入完整 BPMN 圖。
- 流程圖固定使用該實例實際發布版本，依 Flowable 歷程標示已完成及進行中節點；申請人、實際發起人、目前受派／候選人及已有動作紀錄的流程參與者可查看。
- `WITHDRAW`：只有表單 Owner 可撤回 `RUNNING` 流程，原因必填；流程與表單轉 `CANCELLED`。
- `CANCEL`：只有實際發起人可取消 `RUNNING` 流程，服務代申請情境；與申請人撤回分開稽核。
- 完成、駁回、撤回與取消會通知表單 Owner 及實際發起人，相同帳號自動去重。

### 20. 不可變稽核與快照

- Task Action 類型包含 `SUBMIT`、`APPROVE`、`REJECT`、`RETURN`、`RESUBMIT`、`WITHDRAW`、`CANCEL`、`TRANSFER`、`DELEGATE`、`RESOLVE`、`ADD_SIGN`、`COMMENT`、`ADMIN_REASSIGN`、`TERMINATE`。
- Action 保存 Tenant、流程、Task、節點、操作者、申請人、意見、時間及快照關聯。
- `fm_form_snapshot` 保存當時表單版本、revision、JSON 與 SHA-256 digest。
- `fm_task_assignment_snapshot` 保存 Resolver 類型、規則路徑、候選帳號、組織、職稱、層級與解析時間。
- 退回後重新進入同一 BPMN 節點會建立新的 resolution sequence，不覆蓋舊指派。
- 快照及 Action 不提供一般 update／delete API，防止事後竄改。
- Task、申請明細與營運監控共用同一稽核來源。

### 21. 指派異常處理（`/operations/incidents`）

- Resolver 找不到簽核人時保留未指派 Task 並建立 `OPEN` Incident，不讓異常證據隨回滾消失。
- Incident 保存 Tenant、流程、Task、Task Definition Key、錯誤碼、訊息與解析 context；Task category 保存 Incident ID。
- 可依 `OPEN`、`RESOLVED`、`IGNORED` 篩選並查看處理人、時間、理由與 context。
- 僅系統管理員或 `FLOWMINT_OPERATIONS` 可使用。
- Retry：依原流程版本、原節點和啟用規則重新解析；成功後恢復指派、保存快照並關閉 Incident，失敗則整筆回滾且保持 `OPEN`。
- Reassign：選項只列同 Tenant 有效員工；要求理由、精確 Incident／Task 關聯及執行中流程，成功後建立 `ADMIN_REASSIGN` 稽核。
- Terminate：只處理 `RUNNING` 流程且理由必填；先建立 `TERMINATE` 快照／Action，再終止 Flowable，流程轉 `TERMINATED`、表單轉 `CANCELLED`，其餘 `OPEN` Incident 轉 `IGNORED`。
- 使用條件式狀態更新，防止兩位管理者重複處理同一異常。

### 22. 站內通知中心

- 每個 Tenant 與帳號有獨立 `IN_APP` 收件匣、最近通知及未讀數。
- 支援重新整理、單筆已讀與全部已讀；更新同時限制 Tenant、登入帳號、通知 ID 與 channel。
- `TASK_ASSIGNED`：新待辦指派。
- `PROCESS_COMPLETED`：流程完成。
- `PROCESS_REJECTED`：流程駁回。
- `PROCESS_CANCELLED`：撤回或取消。
- `TASK_DUE_SOON`：即將到期。
- `TASK_OVERDUE`：已逾時。
- Task 通知送給目前 assignee／candidate；流程狀態通知送給 Owner 與 Starter。
- 以 Tenant、事件類型、reference、收件帳號產生穩定事件 ID，再以資料庫 `NOT EXISTS` 去重。
- Listener 重入、Incident Retry 或排程重掃不會重複建立相同通知。

### 23. Email 與通知範本

- 不另寫 SMTP Client；透過 QIFU4 `SysMailHelperServiceImpl` 建立 `TbSysMailHelper` Outbox，由 `SendMailHelperJob` 寄送及重試。
- 收件地址取同 Tenant、啟用且有效的員工 Email；沒有 Email 時保留站內通知且不阻斷流程。
- 每封信建立 `EMAIL/PENDING` notification delivery record。
- `PROVIDER_MESSAGE_ID` 精確保存 QIFU4 `MAIL_ID`。
- `FmNotificationDeliverySyncJob` 每三分鐘把 QIFU4 `SUCCESS_FLAG=Y` 的信件同步為 `SENT` 並保存成功時間。
- FlowMint 提供 app 層 `SendMailHelperJob` 相容替代版本；寄送失敗會更新 notification 的重試次數、下次重試時間與受控錯誤，第三次失敗後轉為 `FAILED` 並停止重送。
- 使用 QIFU4 Template Manager／FreeMarker 範本：`FMTASKASG`、`FMPROCMP`、`FMPROREJ`、`FMPROCAN`、`FMTASKDUE`、`FMTASKOVD`。
- 站內與 Email 共用相同範本語意；範本缺失或 render 失敗時使用內建安全 fallback，不讓文案錯誤阻斷流程。

### 24. Task SLA 排程

- Task 建立時依發布版 Policy 設定 Flowable due date。
- `FmTaskDeadlineNotificationJob` 每五分鐘分頁掃描有效且有 due date 的 Task。
- 進入提醒區間產生 `TASK_DUE_SOON`，超過期限產生 `TASK_OVERDUE`。
- 收件人以掃描當下 assignee／candidate 為準，轉派後通知目前處理人。
- 沿用站內通知、Email Outbox、範本及穩定 ID；相同 Task／事件／收件人只發一次。
- 目前採 elapsed hours，不扣除週末、假日或非工作時間。

### 25. 流程監控（`/operations/processes`）

- 只允許系統管理員或 `FLOWMINT_OPERATIONS`，並固定限制 header Tenant。
- 支援 `RUNNING`、`COMPLETED`、`REJECTED`、`CANCELLED`、`TERMINATED` 狀態。
- 可搜尋流程編號、business key、流程名稱、申請人及實際發起人。
- `%`、`_` wildcard 會 escape，避免改變查詢語意。
- 採 10／30／50／100 筆後端分頁，count／page 條件下推 MyBatis。
- 清單顯示流程版本、Owner、Starter、目前 User Task、起訖時間、總耗時、最近期限與逾時數。
- 明細顯示時間排序的 Task Action、表單 revision、內容 digest 與當時 JSON。
- 流程、Action、Snapshot 都以 Tenant 加 Process Instance ID 雙重限制。
- 終止仍走有理由、有狀態檢查及稽核的受控 API。

### 26. 營運報表（`/operations/reports`）

- 僅系統管理員或 `FLOWMINT_OPERATIONS` 可用；日期格式 `yyyy-MM-dd`，預設 30 天，最多 366 天。
- 摘要顯示流程總數、各狀態數、完成平均耗時、逾時 Task、未來 24 小時到期 Task。
- 每日趨勢顯示起單量、完成量、當日完成平均耗時；無資料日期補零。
- 流程定義 Top 20 顯示起單量、完成量、完成率及平均耗時。
- 完成率以區間起單 cohort 的目前完成狀態計算，執行中流程自然反映在比率。
- 節點效能 Top 20 顯示完成樣本、平均與最長處理時間。
- 節點時間由最早 Assignment Snapshot 到 `APPROVE`、`REJECT`、`RETURN` 或 `RESUBMIT` 完成 Action 計算。
- `TRANSFER`、`DELEGATE`、`ADD_SIGN`、`COMMENT` 不被誤算為 Task 完成。
- 指標以聚合 SQL／Flowable count 計算，不先把大量明細載入 JVM。

### 27. 狀態、併發與安全

- 表單狀態：`DRAFT`、`SUBMITTED`、`RETURNED`、`COMPLETED`、`REJECTED`、`CANCELLED`。
- 流程狀態：`RUNNING`、`COMPLETED`、`REJECTED`、`CANCELLED`、`TERMINATED`、`SUSPENDED`。
- 主檔狀態：`DRAFT`、`PUBLISHED`、`INACTIVE`；版本狀態：`DRAFT`、`PUBLISHED`、`RETIRED`。
- Resolver 狀態：`RESOLVED`、`INCIDENT`、`SUPERSEDED`；Incident 狀態：`OPEN`、`RESOLVED`、`IGNORED`。
- 狀態轉換由後端 Logic／Domain 控制，前端不能直接提交任意終態。
- 高風險管理操作要求 reason，並以目前狀態條件更新避免重複處理。
- 起單使用 idempotency key；重要主檔與組織搬移使用 lock／expected version。
- 動態 SQL 的值使用 JDBC parameter，不允許把表單值拼成 SQL 結構。
- DataSource 密碼及不必要敏感欄位不回傳給表單或一般前端頁。
- 完成、駁回、取消、終止後不可再執行一般 Task action。

### 28. 可配置的業務應用

不修改 FlowMint 核心程式，即可利用表單、BPMN、Resolver、核決條件與 Data Action 配置：

- 請購、採購及付款簽核。
- 費用報銷、預支與預算申請。
- 請假、加班、出差與人事異動。
- 合約、用印、法務及資訊安全審查。
- 帳號權限、設備領用與系統變更申請。
- 客戶、供應商或其他主檔資料變更流程。
- 其他可由 Form.io 表單及 BPMN 簽核路徑表達的企業流程。

每個正式業務應用通常需要配置表單版本、流程版本、節點政策、Assignment Rule、起單政策、表單綁定、通知範本及 Program／角色權限。這些屬於導入設定，不代表 FlowMint 平台本體功能尚未完成。

### 29. AI Provider 與簽核解說（`FM_PROG010D0001`）

- 管理 Tenant-scoped OpenAI、Gemini、Groq 與 OpenRouter Provider，API Key 以 AES-GCM 加密保存。
- 正式待辦只在合法處理人主動操作時送出受控 Context，不會自動呼叫外部 AI。
- 分析結果包含摘要、風險、問題與參考建議，不修改表單或代替使用者核准。
- Content Hash、Generation、快取、Token 用量、耗時與 append-only access ledger 提供重現與稽核依據。
- 第一版不解析附件；真實 Provider Key、正式網路與多帳號 E2E 尚待完成。

### 30. 外部系統 API（`FM_PROG010D0002`／`FM_PROG010D0003`）

- FJ02 提供 API Client 查詢、新增、編輯、停用、Key 簽發／輪替／撤銷及高風險操作稽核。
- Key 認證同時檢查 Tenant、Client 狀態、Scope、IP、有效期限與配額，不接受外部呼叫者覆寫安全 Context。
- 外部發單以 Idempotency Key、Request Ledger 與既有 Runtime 起單核心避免重複流程及繞過表單／起單政策。
- 提供流程狀態查詢與受控 Form／Process／組織人員唯讀 API。
- FJ03 使用獨立 Program 權限顯示 Springdoc OpenAPI 說明，不從 Client 管理頁洩漏未授權契約。
- 程式、MariaDB Schema、Program 與管理 UI 已完成；正式外部 Client、流量與跨 Tenant E2E 尚待驗收。

## 技術架構

| 層級 | 技術 |
|---|---|
| Backend Runtime | Java 21、Spring Boot 4.1 |
| Workflow Engine | Flowable 8.0 |
| Persistence | MyBatis、HikariCP |
| Database | MariaDB |
| Security | Spring Security、JWT、HttpOnly Cookie、CSRF |
| Cache／Token infrastructure | Redis |
| Mail | Spring Mail、QIFU4 Mail Helper Outbox |
| API Documentation | SpringDoc OpenAPI |
| Frontend | Nuxt 3、Vue 3、TypeScript、Pinia |
| UI | Bootstrap 5、Bootstrap Icons |
| Form／BPMN | Form.io、bpmn-js |
| Reporting UI | ECharts／Vue ECharts（依頁面需要） |

## Repository 結構

```text
flowmint/
├─ backend/
│  ├─ base/                    # QIFU4 基礎模型、Mapper、Service 與共用元件
│  ├─ core/                    # QIFU4 帳號、角色、Program、Mail 等標準核心
│  ├─ app/                     # FlowMint API、Logic、Runtime、Flowable 與排程
│  └─ doc/                     # 規格、進度、Schema 與部署 SQL
├─ frontend-v-nx/
│  ├─ pages/                   # Nuxt 頁面
│  ├─ components/              # 共用 UI 元件
│  ├─ composables/             # API、Form Runtime 等 composables
│  ├─ middleware/              # 登入與路由保護
│  ├─ store/                   # Pinia store
│  └─ public/                  # 靜態資源
├─ k3s-init/                   # K3s 初始化資源
├─ k3s-project/                # K3s 專案部署資源
├─ README-backend.md           # QIFU4 後端補充說明
├─ README-frontend.md          # QIFU4 前端補充說明
└─ README.md                   # 本文件
```

## 環境需求

開發環境建議準備：

- JDK 21
- Maven 3.9+
- Node.js 20+ 與 npm
- MariaDB 10.11+（或與目前 Schema 相容版本）
- Redis
- 可選：SMTP Server（若要驗證 Email 實際寄送）

預設服務位址：

| 服務 | 預設位址 |
|---|---|
| Frontend | `http://127.0.0.1:8077` |
| Backend | `http://127.0.0.1:8088` |
| API Base URL | `http://127.0.0.1:8088/api` |
| MariaDB | `localhost:3306/flowmint` |
| Redis | `127.0.0.1:6379` |

## 快速開始

### 1. 建立資料庫

這一節只說明新環境初始化。若要連線查詢、備份、還原或異動既有 MariaDB，請先完整閱讀 [MariaDB 操作規範](backend/doc/21-MariaDB操作規範.md)；正式資料不得直接以 README 範例取代備份、唯讀確認及變更核准程序。

建立 UTF-8 MariaDB database：

```sql
CREATE DATABASE flowmint
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

將完整 Schema 匯入空資料庫。Windows 本機依專案 MariaDB 操作規範使用 CLI 絕對路徑並加入
`--skip-ssl`：

```powershell
cmd /c '""C:\Program Files\MariaDB 12.3\bin\mariadb.exe" --host=127.0.0.1 --port=3306 --user=root --password --skip-ssl flowmint < "C:\home\flowmint\backend\doc\flowmint.sql""'
```

`flowmint.sql` 包含 QIFU4、Flowable 與 FlowMint 所需資料結構。請先備份既有資料庫；不要對已有正式資料的環境直接重建。

### 2. Program 與角色配置

Program 基準資料已統一包含在 `backend/doc/flowmint.sql`，repository 不再保留個別
`FM_PROG*-register.sql`。完整匯入後會建立 FlowMint 主檔、設計器、營運、AI Provider、
外部 API Client 管理及 API 說明頁所需 Program。

Program 註冊不等於角色授權。匯入後仍須透過 QIFU4 權限管理配置 Query／Create／Edit／
Deactivate／高風險操作權限，不要在資料庫種子中寫死正式環境角色。

### 3. 設定後端

主要設定檔：

- `backend/app/src/main/resources/application.properties`
- `backend/app/src/main/resources/db1-config.properties`

至少確認：

```properties
server.port=8088
db1.datasource.jdbcUrl=jdbc:mariadb://localhost/flowmint
db1.datasource.username=your_user
db1.datasource.password=your_password_or_ENC_value
spring.redis.host=127.0.0.1
spring.redis.port=6379
```

正式環境請使用環境變數提供加密金鑰：

```powershell
$env:JASYPT_ENCRYPTOR_PASSWORD = "replace-with-strong-secret"
$env:FM_DATASOURCE_ENCRYPTION_KEY = "replace-with-valid-base64-key"
```

請勿沿用 repository 中的開發預設密碼、Mail Server 或加密金鑰。

### 4. 編譯與啟動後端

```powershell
cd C:\home\flowmint\backend
mvn -pl app -am clean install -DskipTests
mvn -pl app spring-boot:run
```

也可以從 IDE 執行：

```text
org.qifu.Application
```

後端預設啟動於 `http://127.0.0.1:8088`。

### 5. 設定前端

在 `frontend-v-nx/.env` 設定：

```dotenv
PORT=8077
VITE_API_URL="http://127.0.0.1:8088/api"
VITE_CK_HEAD_NAME="QIFU4VNX"
VITE_FETCH_TIMEOUT=14000
VITE_DEFAULT_ROW=10
VITE_SUCCESS_FLAG="Y"
```

Cookie prefix 必須與後端 QIFU4 設定一致。

### 6. 安裝與啟動前端

```powershell
cd C:\home\flowmint\frontend-v-nx
npm install
npm run dev
```

瀏覽器開啟 `http://127.0.0.1:8077/login`。

系統不在 README 提供預設帳密；請使用資料庫中已建立並已配置 Tenant membership／Role 的帳號。

## Production Build

### Backend

```powershell
cd C:\home\flowmint\backend
mvn -pl app -am clean package
```

### Frontend

```powershell
cd C:\home\flowmint\frontend-v-nx
npm ci
npm run build
```

Nuxt 採 SPA 模式。Production build 輸出由 Nuxt／Nitro 建立於 `.output`，可使用以下指令預覽：

```powershell
node .output/server/index.mjs
```

## 權限與 Tenant 邊界

FlowMint 同時使用 QIFU4 Program 權限與 FlowMint Runtime 權限：

- UI Program 由 `tb_sys_prog`、Role／Permission 控制選單與頁面入口。
- Runtime API 仍會在後端重新驗證登入者、Tenant、Task assignee／candidate 與操作政策。
- Tenant 從 `X-FlowMint-Tenant` Header 取得，敏感 API 的 body 不接受 `tenantId` 覆寫。
- Actor 從 Spring Security Context 取得，body 不接受操作者帳號。
- 系統管理員或 `FLOWMINT_OPERATIONS` 可使用 Incident、流程監控及營運報表。
- 流程設計、表單設計與主檔維護應分別配置對應 Program 權限。

Program 註冊不等於授權。匯入註冊 SQL 後仍必須在正式權限管理流程中配置角色。

## 通知、Email 與 SLA

FlowMint 不自行建立另一套 SMTP client。Email 流程如下：

```text
FlowMint event
  → fm_notification (IN_APP / SENT)
  → fm_notification (EMAIL / PENDING)
  → QIFU4 TbSysMailHelper
  → SendMailHelperJob 寄送與重試
  → FmNotificationDeliverySyncJob 回寫 EMAIL / SENT
```

注意事項：

- 寄件者由 QIFU4 System Setting 取得，不在 FlowMint 寫死。
- 收件地址來自同 Tenant、啟用且在有效期間內的 `fm_employee.EMAIL`。
- 員工沒有 Email 時只保留站內通知，不阻斷流程。
- Notification Template 位於 QIFU4 `tb_sys_template`／`tb_sys_template_param`。
- `FmTaskDeadlineNotificationJob` 每五分鐘檢查期限。
- SLA 目前以曆時小時計算；工作日曆不在目前核心範圍。
- FlowMint app 層寄信 Job 以 `fm_notification` 保存跨排程重試狀態，第三次失敗後轉為 `FAILED`；非 FlowMint 的 QIFU4 郵件維持既有行為。

## 重要頁面

| 路徑 | 功能 |
|---|---|
| `/login` | 登入 |
| `/workspace` | 工作台、申請、待辦與通知中心 |
| `/requests/start` | 依分類瀏覽、搜尋可發起流程及選擇本人／代申請情境 |
| `/requests/start/[processDefId]` | 獨立正式填單、附件上傳與送出 |
| `/requests/[processInstanceId]` | 申請詳情、目前簽核人、附件、歷程與 BPMN 進度 |
| `/tasks/[taskId]` | 待辦表單、附件、流程進度與簽核動作 |
| `/operations/incidents` | 指派異常處理 |
| `/operations/processes` | 流程實例監控與稽核明細 |
| `/operations/reports` | 流程營運報表 |
| `#/fm_prog010d0001` | AI Provider 管理 |
| `#/fm_prog010d0002` | 外部系統 API Client、Key、Scope 與配額管理 |
| `#/fm_prog010d0003` | 外部系統 API 說明 |

其他主檔與設計器頁面由 QIFU4 Program 選單提供，不建議依賴手動輸入 URL。

## 測試與品質檢查

### Backend compile

```powershell
cd C:\home\flowmint\backend
mvn -pl app -am -DskipTests compile
```

### FlowMint 相關測試

```powershell
cd C:\home\flowmint\backend
mvn -pl app "-Dtest=Fm*Test" test
```

若要執行特定測試：

```powershell
mvn -pl app "-Dtest=FmNotification*Test,FmProcessInstanceMapperXmlTest" test
```

### Frontend production build

```powershell
cd C:\home\flowmint\frontend-v-nx
npm run build
```

### Diff 格式檢查

```powershell
cd C:\home\flowmint
git diff --check
```

測試環境目前可能輸出 Log4j `appLog` appender、Mockito dynamic agent 或 Nuxt chunk-size 警告；應分辨警告與真正的 compile／test failure。

## 建議驗收流程

1. 驗證登入、登出、Token refresh 與瀏覽器重新整理還原。
2. 驗證 Tenant membership 與跨 Tenant 拒絕。
3. 建立並發布表單及 BPMN 流程版本。
4. 驗證申請中心分類／搜尋、一般起單、合法代申請、單據編號與重複送出冪等。
5. 驗證有附件及無附件表單、表單內／側邊附件一致、權限下載，以及待辦核准不誤判附件異動。
6. 驗證待辦、核准、退回、補件、駁回、撤回與完成。
7. 驗證申請摘要的目前關卡／簽核人，以及 BPMN 流程圖首次與重複開啟、節點標色和參與者權限。
8. 驗證轉派、代理、加簽及 Approval Group 多人模式。
9. 驗證表單、指派與操作快照不可變。
10. 驗證站內通知、Email Outbox、期限提醒及逾時通知。
11. 製造 Resolver 失敗，驗證 Incident Retry／Reassign／Terminate。
12. 驗證流程監控、稽核明細、分頁與營運報表。
13. 使用台灣／中國大陸組織與時區情境執行完整 E2E。
14. 使用接近正式量級的資料執行 SQL `EXPLAIN` 與效能測試。

## 已知限制與非核心範圍

目前不列入 FlowMint 核心：

- 集團、法人與據點主檔。
- Job、Grade、Position。
- Project Organization。
- HR／ERP 同步平台。
- SLA 工作日曆。
- 外部低代碼 Script 平台。

已知技術限制：

- 完整交付仍需正式資料 E2E 與實際量級效能驗證。
- 最近完成的附件、申請追蹤、目前簽核人及 BPMN 進度仍需重新啟動完整環境後，以申請人和實際簽核人執行瀏覽器回歸。
- AI 解說尚未以真實 Provider Key、正式網路與多帳號完成 E2E；第一版不解析附件。
- Data Action 仍需跨資料庫整合與實際資料量驗收。
- 業務表單及流程須由導入人員依企業規則配置。

## 文件索引

核心文件位於 `backend/doc`：

- [新系統總綱](backend/doc/00-新系統總綱.md)
- [核心資料模型](backend/doc/01-核心資料模型.md)
- [組織與簽核人規格](backend/doc/03-組織與簽核人規格.md)
- [流程與表單規格](backend/doc/04-流程與表單規格.md)
- [安全權限與稽核](backend/doc/05-安全權限與稽核.md)
- [後端程式規範](backend/doc/06-後端程式規範.md)
- [API 規格](backend/doc/08-API規格.md)
- [程式編排](backend/doc/09-程式編排.md)
- [開發路線](backend/doc/12-開發路線.md)
- [資料庫規範](backend/doc/13-資料庫規範.md)
- [QIFU4 前端實作規範](backend/doc/15-QIFU4前端實作規範.md)
- [QIFU4 後端持久層規範](backend/doc/16-QIFU4後端持久層規範.md)
- [開發進度](backend/doc/18-開發進度.md)
- [原始碼排版與交付規範](backend/doc/19-原始碼排版與交付規範.md)
- [動態資料服務規格](backend/doc/20-動態資料服務規格.md)
- [MariaDB 操作規範](backend/doc/21-MariaDB操作規範.md)
- [附件上傳與儲存規格](backend/doc/22-附件上傳與儲存規格.md)
- [請購單流程與表單配置規格](backend/doc/23-請購單流程與表單配置規格.md)
- [共用單據編號規格](backend/doc/24-共用單據編號規格.md)
- [Workspace 與申請中心重構規劃](backend/doc/25-Workspace與申請中心重構規劃.md)
- [平行加簽規劃](backend/doc/26-平行加簽規劃.md)
- [正式會簽配置與實作指南](backend/doc/27-正式會簽配置與實作指南.md)
- [流程簽核改派規劃](backend/doc/28-流程簽核改派規劃.md)
- [AI 簽核解說精靈規劃](backend/doc/29-AI簽核解說精靈規劃.md)
- [採購單與驗收單表單流程配置規劃](backend/doc/30-採購單與驗收單表單流程配置規劃.md)
- [公司名片申請單表單流程配置規劃](backend/doc/31-公司名片申請單表單流程配置規劃.md)
- [外部系統 API 管理與流程拋單規劃](backend/doc/32-外部系統API管理與流程拋單規劃.md)
- [表單 Custom JavaScript 規劃說明](backend/doc/FM_PROG005D0001_cust_js規劃說明.md)
- [Data Action 使用說明](backend/doc/使用FM_PROG006D0002說明.md)

## License

本專案依 repository 內 [LICENSE](LICENSE) 授權；使用、修改與散布前請閱讀完整授權條款。
