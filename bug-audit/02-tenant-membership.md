## 02 Tenant 與 membership（`FM_PROG001D0001`）— 靜態／唯讀盤查完成

### BUG-002：全域啟用會將所有停用的 Tenant membership 標為 ACTIVE

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原 `activateAccount()` 確實逐筆啟用所有停用 membership。現在全域停用／啟用只改 `tb_account.ON_JOB`，保留各 Tenant membership 的狀態與有效期間；停用帳號的既有 Token 由 BUG-039 的驗證修正拒絕。`FmTenantAccountActivationTest` 通過。過去已被舊邏輯修改的資料不會自動還原，現有資料尚無跨 Tenant 情境可重現。

- 嚴重度：高。性質：Java 業務邏輯；Vue 畫面會觸發該路徑；MariaDB 現有資料不足以重現跨 Tenant 案例。
- 預期：重新啟用登入帳號時，不能讓原本刻意停用的其他 Tenant membership 自動重新生效；應區分帳號全域停用與個別 Tenant membership 停用。
- 實際：`FmTenantLogicServiceImpl.activateAccount()` 查詢該帳號所有 `INACTIVE` 的 `fm_tenant_account`，逐筆設為 `ACTIVE`。即使只在某一 Tenant 的編輯頁執行，也會改動其他 Tenant。若其他 Tenant membership 原本沒有截止時間，會立即恢復存取；有已過截止時間者仍因日期判斷無法存取。Java 雖設定 `EFFECTIVE_TO=null`，Mapper 實際會略過此欄位，詳 BUG-043。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `activateAccount()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmTenantAccountMapper.xml` 的 `update`；`frontend-v-nx/pages/fm_prog001d0001/edit/[id].vue` 的 `doActivateAccount()` 與 `btnActivateAccount()`。`README.md` 要求 membership 啟用且在有效期間內。
- 驗證：MariaDB 唯讀查詢顯示 `fm_tenant` 為 1 筆、`fm_tenant_account` 為 140 筆，現況沒有多 Tenant 帳號可用於跨 Tenant 重現；因此此項為程式碼確認的邏輯缺陷，跨 Tenant 實際影響尚未以資料重現。未修改資料。

### BUG-042：同一帳號可同時設定多個預設 Tenant，登入後選取結果不明確

- 複核與修正狀態：**程式與資料約束確認，已修正應用寫入路徑（2026-10-06）**。原新增／修改直接寫入 `IS_DEFAULT='Y'`，資料庫也沒有單帳號唯一預設約束。現在選擇新預設 Tenant 時，在同一交易中清除該帳號其他預設旗標；新增與修改交易使用 `SERIALIZABLE`，以處理並行設定。`FmTenantAccountActivationTest` 覆蓋切換預設情境並通過。現有資料只有一個 Tenant，未做雙 Tenant 實際重現；未透過應用程式寫入的資料仍須由資料庫治理。

- 嚴重度：中。性質：Java 業務邏輯、Vue 選取行為、MariaDB 約束。
- 預期：帳號的「預設 Tenant」應只有一個明確結果；設定另一個預設 Tenant 時，舊預設應被清除，或應拒絕產生多筆預設設定。
- 實際：`FmTenantLogicServiceImpl.addAccount()` 與 `updateAccount()` 直接寫入傳入的 `IS_DEFAULT='Y'`，沒有查詢或更新同帳號在其他 Tenant 的預設旗標。`fm_tenant_account` 只有 `(TENANT_ID, ACCOUNT)` 唯一鍵，無帳號預設旗標唯一約束，因此同帳號可有多筆啟用中的預設 membership。`workspace.vue` 與 `useProcessStartCatalog.ts` 都用 `.find(item => item.defaultTenant)` 取第一筆，會依清單順序選取其中一個。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `addAccount()`、`updateAccount()`；`frontend-v-nx/pages/fm_prog001d0001/edit/[id].vue` 的預設 Tenant 選項；`frontend-v-nx/pages/workspace.vue` 與 `frontend-v-nx/composables/useProcessStartCatalog.ts` 的選取程式；`backend/doc/flowmint.sql` 的 `fm_tenant_account` DDL。README 的 Tenant 管理步驟允許設定「該帳號的預設 Tenant」。
- 驗證：MariaDB CLI `SHOW INDEX FROM fm_tenant_account` 確認只有 `(TENANT_ID, ACCOUNT)` 唯一鍵；`SELECT` 查詢目前啟用且多筆預設的帳號為 0。現有資料只有一個 Tenant，故此為跨 Tenant 程式碼／資料模型推論，尚未用資料重現。未修改資料。

### BUG-043：membership 截止時間無法清除，帳號重新啟用後仍無法使用 Tenant

- 複核與修正狀態：**部分原情境已由 BUG-002 消除，剩餘缺陷已修正（2026-10-06）**。全域停用／啟用現在不再改 membership 截止時間，因此「重新啟用後截止時間殘留」不再由該路徑產生；但編輯時傳 `null` 確實仍會被 Mapper 忽略。已讓更新 SQL 明確寫入 `EFFECTIVE_TO = NULL`，並以 `FmTenantAccountMapperXmlTest` 檢查產生的 SQL。既有已過期資料不自動更動。

- 嚴重度：高。性質：Java 與 MyBatis 更新語意。
- 預期：全域停用後再啟用帳號，若畫面承諾恢復 Tenant 權限，membership 的截止時間也應恢復為有效值；編輯 membership 時指定無截止時間亦應能清除舊值。
- 實際：`deactivateAccount()` 把所有啟用 membership 的 `EFFECTIVE_TO` 設成停用時間；`activateAccount()` 雖把欄位設為 `null`，`FmTenantAccountMapper.update` 只在 `effectiveTo != null` 時才產生 `EFFECTIVE_TO = ...`，所以資料庫保留已過期的停用時間。帳號及 membership 狀態恢復為啟用，但 Runtime 的有效期間檢查仍拒絕 Tenant；`updateAccount()` 傳入 `null` 也無法清除原有截止時間。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `deactivateAccount()`、`activateAccount()`、`updateAccount()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmTenantAccountMapper.xml` 的 `<update id="update">`。
- 驗證：MariaDB CLI `SHOW CREATE TABLE fm_tenant_account` 確認 `EFFECTIVE_TO` 可為 `NULL`，目前無可用的停用／重啟資料供重現；此項由程式與 SQL 更新語句確認。未修改資料。

### BUG-044：有未來生效 membership 時，全域停用帳號可能整筆交易失敗

- 複核與修正狀態：**原程式路徑成立，已由 BUG-002 的修正消除（2026-10-06）**。原停用會把未來生效 membership 的截止時間設為現在，確實可能違反 `CK_FM_TA_DATE`；全域停用現在只改帳號 `ON_JOB`，不再更新 membership 日期。現有資料沒有未來生效案例，尚未執行資料庫交易重現。

- 嚴重度：高。性質：Java 停用邏輯與 MariaDB CHECK 約束衝突。
- 預期：帳號即使有未來生效的 Tenant membership，也能被全域停用。
- 實際：`deactivateAccount()` 對每筆 `ACTIVE` membership 把 `EFFECTIVE_TO` 設為現在；若 `EFFECTIVE_FROM` 在未來，截止時間會早於生效時間，觸發 `CK_FM_TA_DATE`（`EFFECTIVE_TO > EFFECTIVE_FROM`）。交易回滾，帳號與其他 membership 也無法停用。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `deactivateAccount()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmTenantAccountMapper.xml` 的 `update`；`backend/doc/flowmint.sql` 的 `CK_FM_TA_DATE`。
- 驗證：MariaDB CLI `SHOW CREATE TABLE fm_tenant_account` 確認約束存在；`SELECT` 查詢目前未來生效且 `ACTIVE` 的 membership 為 0 筆，因此尚未用現有資料重現。未修改資料。

### BUG-045：管理員重設密碼後，帳號原有 Token 仍可使用

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原重設密碼只更新 `tb_account.PASSWORD`，既有 Access／Refresh Token 仍會通過 token store 驗證。現在密碼更新與撤銷該帳號全部 token store 紀錄在同一交易中執行；`FmTenantAccountActivationTest` 驗證重設時呼叫撤銷。未以真實 Token 執行 HTTP 重現。

- 嚴重度：高。性質：Java 身分驗證與帳號管理流程；與 BUG-038 的登出及 BUG-039 的停用帳號屬不同觸發情境。
- 預期：管理員重設登入密碼後，該帳號既有的 Access／Refresh Token 應失效，避免持有舊 Token 的使用者繼續存取或更新登入狀態。
- 實際：`FmTenantLogicServiceImpl.resetPassword()` 僅更新 `tb_account.PASSWORD`，沒有撤銷 `tb_sys_token` 中該帳號的 Token。`WebSecurityConfig.jwtDecoder()` 對既有 Access Token 只驗證簽章、時間及 Token store 未過期；`AuthController.refreshNewToken()` 對 Refresh Token 只驗證 Token store，也沒有重新驗證密碼。因此重設密碼不影響已核發 Token 的剩餘有效期。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `resetPassword()`；`backend/app/src/main/java/org/qifu/core/config/WebSecurityConfig.java` 的 `jwtDecoder()`；`backend/app/src/main/java/org/qifu/core/api/AuthController.java` 的 `refreshNewToken()`；`backend/base/src/main/java/org/qifu/base/support/TokenStoreValidateBuilder.java`。
- 驗證：MariaDB CLI `SELECT COUNT(*) FROM tb_sys_token` 顯示目前 Token store 有 3 筆；僅查筆數，未讀取 Token 內容，也未實際重設密碼。此項為程式碼確認的流程缺陷。

### BUG-046：Tenant 帳號畫面無法設定或修改 membership 失效時間

- 複核與修正狀態：**前端與 API 對照確認，已修正（2026-10-06）**。原畫面沒有失效時間輸入與 `/account/update` 呼叫；現在可新增時設定失效時間，也可編輯既有 membership 的期間、狀態與預設旗標，並可清空截止時間。前端正式建置通過；未在瀏覽器對實際資料操作。

- 嚴重度：中。性質：Vue 功能缺漏；Java 已提供更新 API，MariaDB 已有欄位及有效期間約束。
- 預期：管理員可依 README 所述設定 membership 有效時間，並在帳號加入後調整其有效期間、預設 Tenant 與狀態。
- 實際：`edit/[id].vue` 的 `accountForm` 有 `effectiveTo` 並在新增請求中傳送，但範本只有「生效時間」輸入，沒有「失效時間」輸入；`effectiveTo` 因此固定為 `null`。membership 清單只有重設密碼、全域停用及全域啟用按鈕，沒有編輯操作；專案此頁沒有呼叫既有的 `/account/update` API。透過畫面無法設定失效時間，也無法更改已加入帳號的 membership 設定。
- 證據：`frontend-v-nx/pages/fm_prog001d0001/edit/[id].vue` 的 `accountForm`、`btnAddAccount()` 及 Tenant Account 範本；`backend/app/src/main/java/org/qifu/fm/controller/FMPROG001D0001Controller.java` 的 `/account/update`；`README.md`「Tenant 與帳號」第 4 步。
- 驗證：MariaDB CLI `SHOW CREATE TABLE fm_tenant_account` 確認 `EFFECTIVE_TO` 欄位存在且可為 NULL，並有 `CK_FM_TA_DATE` 約束。此項為前端與 API 對照確認，未改動資料。

### BUG-047：Tenant 接受無效時區，之後需取單據編號的起單會失敗

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原建立／修改 Tenant 沒有檢查時區，而取號路徑使用 `ZoneId.of()`；現在兩個寫入入口都先驗證時區，無效值以業務錯誤拒絕。`FmTenantAccountActivationTest` 覆蓋無效時區，測試通過；未以實際起單重現。

- 嚴重度：高。性質：Java 建立／編輯驗證、Vue 自由輸入與 Runtime 連動。
- 預期：建立或修改 Tenant 時應驗證預設時區為可解析的時區，避免儲存會阻斷 Runtime 的設定。
- 實際：Tenant 表單的「預設時區」是任意文字輸入；Controller 的 `validateTenant()` 只檢查 ID／代碼／名稱，`FmTenantLogicServiceImpl` 直接儲存傳入的時區，資料庫也沒有時區有效性約束。若填入無效值，已設定 `DOCUMENT_TYPE` 的流程起單時，`FmProcessRuntimeLogicServiceImpl.nextDocumentNumber()` 把該值傳入 `FmDocumentNumberService.zoneId()`，後者呼叫 `ZoneId.of()` 並拋出「Tenant 時區無效」，使送單失敗。
- 證據：`frontend-v-nx/pages/fm_prog001d0001/components/TenantForm.vue`；`backend/app/src/main/java/org/qifu/fm/controller/FMPROG001D0001Controller.java` 的 `validateTenant()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `create()`／`update()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `nextDocumentNumber()`；`backend/app/src/main/java/org/qifu/fm/domain/runtime/FmDocumentNumberService.java` 的 `zoneId()`。
- 驗證：MariaDB CLI `SHOW CREATE TABLE fm_tenant` 確認 `DEFAULT_TIMEZONE` 只有長度與非空約束，`SELECT` 顯示目前 Tenant 時區為有效的 `Asia/Taipei`；`fm_process_def` 有 4 筆非空 `DOCUMENT_TYPE`。未寫入無效時區，實際失敗路徑依程式碼確認。

### 資料盤查紀錄

使用 `backend/doc/21-MariaDB操作規範.md` 指定的 MariaDB CLI，對 `flowmint` 執行 `SELECT`／`SHOW`：Tenant 1 筆、membership 140 筆、跨 Tenant 帳號 0 筆、有效期間倒置 0 筆、單帳號多筆啟用預設 membership 0 筆、未來生效且 ACTIVE 的 membership 0 筆、Token store 3 筆。`SHOW CREATE TABLE` 確認截止時間可為 NULL，且有效期間有 CHECK 約束。現行 Tenant 管理 Controller 權限的 `tb_role_permission` 只有 `BPM_ADMIN` 角色 5 筆。這些結果只描述當前資料狀態，不代表程式邏輯沒有其他缺陷。

### BUG-048：membership 更新缺少有效期間檢查，錯誤輸入落到資料庫約束

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原更新入口確實未呼叫既有 `validateAccountCommand()`；現在更新前先檢查起始與截止時間，倒置期間會在資料庫更新前以 `ServiceException` 拒絕。`FmTenantAccountActivationTest` 覆蓋倒置時間並通過；未用實際 API 送入非法值。

- 嚴重度：中。性質：Java API 驗證與錯誤回應；目前前端沒有 membership 編輯入口（另見 BUG-046），可由 `/account/update` API 觸發。
- 預期：更新 membership 時，`EFFECTIVE_FROM` 必填，且非空 `EFFECTIVE_TO` 必須晚於起始時間；不合格請求應由業務驗證回傳可理解的欄位錯誤，與新增操作一致。
- 實際：`FMPROG001D0001Controller.validateAccount()` 只檢查起始時間是否為 `null`，`FmTenantLogicServiceImpl.updateAccount()` 未呼叫已有的 `validateAccountCommand()`，直接更新兩個時間。倒置時間由資料庫 `CK_FM_TA_DATE` 擋下，例外不屬 Controller 明列捕捉的 `ServiceException`／`ControllerException`；目前也未找到全域 `ControllerAdvice` 處理資料庫約束例外，因此無法取得正常的欄位驗證回應。新增路徑則會先執行 `validateAccountCommand()`。
- 證據：`backend/app/src/main/java/org/qifu/fm/controller/FMPROG001D0001Controller.java` 的 `/account/update` 與 `validateAccount()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `addAccount()`、`updateAccount()`、`validateAccountCommand()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmTenantAccountMapper.xml` 的 `update`。
- 驗證：2026-10-02 唯讀 `SHOW CREATE TABLE fm_tenant_account` 確認 `CK_FM_TA_DATE`；140 筆現有 membership 的倒置期間為 0。未送出異動請求，HTTP 實際狀態碼尚未重現。

### BUG-049：停用 Tenant 後，既有流程仍可由直接 Runtime API 操作

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原 Task Runtime、申請追蹤、通知、附件上傳／下載、平行加簽及 Data Action 的直接入口只查 membership，未查 Tenant 主檔狀態；起單路徑亦同（BUG-018）。現在這些入口會先拒絕非 `ACTIVE` Tenant，再檢查既有 membership 與資料權限。`FmTenantStatusGuardTest` 與 `FmNotificationTenantStatusTest` 驗證停用 Tenant 被拒絕，相關定向測試通過；尚未以實際停用 Tenant 執行 HTTP 情境。擴大測試時 `FmProcessRuntimeControllerContractTest.taskActionSupportsValidatedFormResubmission` 因既有 DTO 欄位與測試預期不一致而失敗，兩檔均未在本次修改。

- 嚴重度：高。性質：Tenant 狀態與既有流程 Runtime 授權。與 BUG-018 的直接 API 起單屬同一停用邊界，但影響不同的操作入口。
- 預期：依 `README.md`「Tenant 管理」規格，Tenant 停用後 Runtime 不再開放；待辦清單、明細、簽核及改派等操作應拒絕停用 Tenant。
- 實際：`FmTenantLogicServiceImpl.deactivate()` 只把 `fm_tenant.STATUS` 設為 `INACTIVE`，不修改 membership。`FmProcessRuntimeLogicServiceImpl.tenants()` 因過濾 ACTIVE Tenant 而隱藏該 Tenant；但 `FmTaskRuntimeLogicServiceImpl.inbox()`、`load()`、`action()`、`transfer()`、`delegate()` 等入口共用的 `currentAccount()` 只確認 membership 為 ACTIVE 且在有效期間，沒有讀取 Tenant 狀態。`FmRequestTrackingLogicServiceImpl` 的我的申請清單、明細、進度與撤回／取消，`FmNotificationLogicServiceImpl` 的收件匣與已讀操作，以及 `FmAttachmentUploadLogicServiceImpl` 的 Upload Session／附件操作，也只查 membership。`FmDataActionController` 的 execute／stream 等直接 API 同樣只驗證 membership。平行加簽的 `FmParallelAddSignRuntimeLogicServiceImpl.currentAccount()` 也只查 membership；AI 簽核解說透過 `FmTaskRuntimeLogicServiceImpl.load()` 繼承同一缺口。只要呼叫者仍有原 membership 與既有資料，直接傳入停用 Tenant ID 仍可走到這些 Runtime 路徑；Data Action 執行還可能產生業務資料異動。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTenantLogicServiceImpl.java` 的 `deactivate()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `tenants()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTaskRuntimeLogicServiceImpl.java`、`FmRequestTrackingLogicServiceImpl.java`、`FmNotificationLogicServiceImpl.java`、`FmAttachmentUploadLogicServiceImpl.java`、`FmParallelAddSignRuntimeLogicServiceImpl.java`、`FmAiTaskRuntimeLogicServiceImpl.java` 的入口；`backend/app/src/main/java/org/qifu/fm/controller/FmDataActionController.java` 的 `validateTenantMembership()`；`README.md`「Tenant 管理」的停用規格。正式附件讀取缺少 membership 另見 BUG-015。
- 驗證限制：現有 `fm_tenant` 僅 1 筆且為啟用狀態；未變更資料或送出簽核請求。直接 API 行為依程式路徑推論，尚待隔離環境實際重現。
