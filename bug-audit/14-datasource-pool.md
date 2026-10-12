## 14 DataSource Pool（`FM_PROG006D0001`）— 靜態／唯讀盤查完成

2026-10-05 收尾結論：已對照 `README.md` 操作說明、`backend/doc/20-動態資料服務規格.md` 第 4 節、Controller 全部七個 POST 端點（清單、建立、更新、載入、停用、測試連線、Tenant 選項）、Logic、Registry、Mapper、Vue 清單與表單。Controller 使用各自 Program 權限；Tenant 資料授權缺口見 BUG-016。建立、更新、停用及測試的狀態與錯誤路徑已核對；密碼沿用的連線目標邊界見 BUG-163，驗證 SQL 見 BUG-164，ID／狀態漏篩見 BUG-165，表單參數缺口見 BUG-168。Driver 類型及 URL 前綴經 `FmDataSourceDriverCatalog` 檢查；Registry 按版本重建，但不在管理交易提交時主動清理。`FmDataSourcePoolMapperXmlTest` 只證明 XML 可解析且主鍵查詢存在，不能證明動態條件、Tenant 權限或停用後行為。既有 MariaDB 唯讀證據僅有一筆 `A01/FLOWMINT` ACTIVE Pool、驗證 SQL `SELECT 1`、25 個 Action 引用；未讀密碼。本輪沒有新增資料庫查詢或異動。多 Pool 錯選、停用後借用、跨資料庫連線及快取生命週期仍需隔離環境驗證，屬現有條目的重現限制，不妨礙本單元靜態／唯讀盤查收尾。

2026-10-04 生命週期續查：`FmDataSourcePoolLogicServiceImpl.create()` 建立隨機 Pool ID、初始 lock version 0；`update()` 比對 command 與讀取版本並由 Mapper 的 `LOCK_VERSION` 條件更新，`deactivate()` 也走同一 Mapper 更新。`FmDataSourcePoolRegistry.get()` 以 Tenant／要求的 Pool ID／資料列 lock version 為快取鍵；下一次取用同一 Pool ID 且版本變動時會關閉舊 Hikari pool。停用後仍可能取到 Pool 的原因已記於 BUG-165 的 Mapper 漏篩，不另列快取缺陷。Registry 不會在管理交易提交當下主動清理既有快取；尚未以借用中連線、跨節點部署或真實停用操作驗證生命週期。此輪未確認獨立新缺陷，下一步回第 24 單元外部 API 或第 26 單元單據編號的剩餘路徑。

2026-10-04 續查 `backend/doc/20-動態資料服務規格.md`、`FmDataSourcePoolRegistry`、管理 Logic、Mapper 與 Vue 管理頁路徑。MariaDB 唯讀確認目前 1 筆 `A01/FLOWMINT` ACTIVE 連線池，未讀取密碼欄位。更新 SQL 以 Tenant、OID、`LOCK_VERSION` 做 compare-and-set；通用 `BaseService.update()` 對 0 筆更新會拋錯，因此本輪未列併發覆寫問題。Registry 依 lock version 重建快取，實際更新、停用及跨資料庫連線仍待隔離測試。

### BUG-016：連線池管理缺少操作人的 Tenant 資料授權檢查

- 嚴重度：高。性質：Java 權限邊界；與 BUG-003 同類型，但可影響外部資料庫連線設定。
- 預期：全域 Program 權限之外，讀取、編輯、停用或測試某 Tenant 的連線池時，須驗證操作人有該 Tenant 的資料權限。
- 實際：`FmDataSourcePoolLogicServiceImpl` 的 `load()`、`update()`、`deactivate()` 依 OID 存取；`create()` 與 `test()` 只驗證目標 Tenant 存在且啟用。所有方法均未呼叫 `FmTenantAccessGuard` 或等價檢查，`tenantOptions()` 也列出所有啟用 Tenant。持有連線池 Program 權限的使用者可對其他 Tenant 的連線池操作。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmDataSourcePoolLogicServiceImpl.java`；MariaDB 唯讀查詢顯示目前連線池 1 筆，屬於單一 Tenant，無跨 Tenant 資料可實際重現。查詢未讀取密碼欄位。

### 2026-10-08 BUG-016 修復複核
- **BUG-016 已修正**：核對清單 Controller 原直接使用呼叫者提供的 Tenant 條件，Logic 的建立、載入、更新、停用、測試連線及 Tenant 選項原均無有效 membership 檢查；Program 權限不能取代 Tenant 資料授權，故不是誤判。
- 清單現在要求指定且可讀的 Tenant；Logic 對目標 Tenant 或既存 Pool 所屬 Tenant 執行 `FmTenantAccessGuard` 檢查，Tenant 選項僅回傳可讀範圍。`FmDataSourcePoolTenantAccessTest` 驗證跨 Tenant 載入拒絕及選項過濾，目標測試通過。尚未以多 Tenant 帳號進行 HTTP 驗收。

### BUG-163：測試既有 Pool 時可把儲存的密碼用於任意新連線目標

- 嚴重度：高。性質：Java DataSource Pool 憑證使用邊界；靜態推論，未連接外部目標或讀取密碼。
- 預期：編輯頁密碼留空表示沿用既有 Pool 的密碼時，測試連線應只對既存的受控連線目標使用該密碼；若 JDBC URL 或帳號改變，應要求提供新密碼或明確授權，避免把原憑證送往未核准的資料庫端點。
- 實際：`FmDataSourcePoolLogicServiceImpl.test()` 以傳入 command 的 `jdbcUrl`、`username`、`dbType` 建立測試連線；只要 command 有既存 `oid` 且密碼空白，就從該 Pool 解密原密碼。它僅檢查 command 與既存 Pool 的 Tenant 相同，沒有比對 JDBC URL、帳號或資料庫類型。`FmDataSourceDriverCatalog.urlMatches()` 只檢查受支援 JDBC 前綴，沒有鎖定 host。Vue 編輯頁允許修改 URL／帳號並將整張尚未儲存的表單送到 `/test-connection`。因此具 Pool 操作權限者可讓後端以儲存的憑證向另填的連線目標嘗試認證；這與 BUG-016 的跨 Tenant 存取是不同的憑證再利用問題。
- 證據：`README.md` DataSource Pool 編輯與密碼留空說明；`FmDataSourcePoolLogicServiceImpl.test()`／`dataSource()`、`FmDataSourceDriverCatalog.urlMatches()`、`DataSourcePoolForm.vue.call()` 與可編輯的 JDBC URL／帳號欄位。MariaDB 唯讀查詢顯示目前 `A01/FLOWMINT` 有 1 筆 ACTIVE Pool；查詢未讀取 `PASSWORD_CONTENT`，未執行連線測試。

### 2026-10-08 BUG-163 修復複核
- **BUG-163 已修正**：核對密碼留空時，`test()` 原只確認既存 Pool 的 Tenant，隨後解密並用請求中的 JDBC URL、帳號及資料庫類型建立連線；`update()` 也可在變更連線目標時保留舊密碼。故問題成立，且更新路徑有相同根因。
- 測試連線與更新在沿用既存密碼前，現在要求資料庫類型、JDBC URL 及帳號均與既存 Pool 相同；變更任一項須提供新密碼。`FmDataSourcePoolCredentialTargetTest` 驗證未解密舊密碼、未保存變更，目標測試通過。未對外部資料庫執行實際連線驗收。

### BUG-164：Validation Query 可執行資料異動且在借用連線時重複觸發

- 嚴重度：高。性質：Java DataSource Pool／SQL 執行邊界；依程式與 HikariCP 行為靜態推論，未送出寫入 SQL。
- 預期：用於連線健康檢查的 SQL 應限制為安全的唯讀查詢；測試與正式借用連線都不能藉此執行未經 Data Action 步驟、交易與權限檢查的資料異動。
- 實際：Vue 的「驗證 SQL」是任意文字輸入。`FmDataSourcePoolLogicServiceImpl.validate()` 未驗證 SQL 種類，`apply()` 原樣保存；`test()` 與 `FmDataSourcePoolRegistry.config()` 均將其交給 Hikari 的 `setConnectionTestQuery()`。Hikari 會在連線驗證時執行該語句；預設 `autoCommit=true`，且程式未設定唯讀連線。因此若 Pool 帳號有寫入權限，管理者填入有效的資料異動 SQL 後，測試連線或正式 Data Action 借用連線時可執行異動，且可能多次觸發。
- 證據：`frontend-v-nx/pages/fm_prog006d0001/components/DataSourcePoolForm.vue` 的 `validationQuery` 輸入；`FmDataSourcePoolLogicServiceImpl.validate()`、`apply()`、`dataSource()`；`FmDataSourcePoolRegistry.config()`；`README.md` DataSource Pool 操作說明建議安全且低成本的 `SELECT 1`。HikariCP 官方 [README](https://github.com/brettwooldridge/HikariCP#frequently-used) 說明 `connectionTestQuery` 用於驗證連線，`autoCommit` 預設為 `true`；[PoolBase 原始碼](https://github.com/brettwooldridge/HikariCP/blob/dev/src/main/java/com/zaxxer/hikari/pool/PoolBase.java) 以 `Statement.execute()` 執行測試語句。MariaDB 唯讀確認現有 Pool 的驗證 SQL 是 `SELECT 1`；未以寫入語句或正式連線重現。

### 2026-10-08 BUG-164 修復複核
- **BUG-164 已修正**：管理端原樣保存 Validation Query，測試連線與正式 Registry 均交給 Hikari 作連線檢查，原先沒有 SQL 種類限制；故資料異動可在連線驗證時執行的風險不是誤判。
- 建立、更新、測試連線及既有 Pool 的 Runtime 建立現在共用驗證，只接受常數查詢 `SELECT 1` 或 `SELECT 1 FROM DUAL`；空值仍由 JDBC 驅動驗證。其他 SQL，包括資料異動、多語句及函式呼叫，均在設定 Hikari 前拒絕。`FmDataSourceValidationQueryTest` 及相關 Pool 目標測試通過；尚未對外部資料庫執行實際連線驗收。

### BUG-165：Pool Mapper 忽略 Pool ID 與狀態，Data Action 可連到錯誤或停用的資料庫

- 嚴重度：高。性質：Java／MyBatis Pool 選取邊界；多 Pool 與停用情境由程式碼推論，未異動現有 Pool。
- 預期：Data Action 建立／發布時只接受同 Tenant 的 ACTIVE 且 ID 相符的 Pool；Runtime 應以該 `POOL_ID` 建立連線，停用後不得再借用。
- 實際：`FmDataSourcePoolMapper.xml` 的 `selectListByParams` 與 `baseConditions` 只有 `TENANT_ID` 條件，忽略呼叫端提供的 `poolId`、`status`。`FmDataSourcePoolRegistry.loadActivePool()` 傳入這兩個條件後，實際取得該 Tenant 查詢結果的第一筆 Pool，不論要求哪個 ID 或它是否停用；`get()` 仍以要求的 ID 建立快取鍵。`FmDataActionLogicServiceImpl.validatePool()` 只判斷查詢結果非空，因此只要 Tenant 有任一 Pool，即可通過錯誤或停用 Pool ID 的驗證。`poolOptions()` 同樣會顯示停用 Pool。若停用的 Pool 是查詢結果第一筆，Runtime 仍可為它建立連線；若有多個 Pool，Action 可能在另一個資料庫執行。原先 BUG-165 對 `loadActivePool()` 會先拒絕停用 Pool 的推論已由此 Mapper 核對推翻，本條以實際根因取代。
- 證據：`backend/app/src/main/resources/org/qifu/fm/mapper/FmDataSourcePoolMapper.xml` 的 `baseConditions`／`selectListByParams`；`FmDataSourcePoolRegistry.loadActivePool()`／`get()`；`FmDataActionLogicServiceImpl.validatePool()`／`poolOptions()`；`FmDataActionExecutor.execute()` 取得 Registry Pool。MariaDB 唯讀確認目前只有 `A01/FLOWMINT` 1 筆 ACTIVE Pool，被 25 個 Action 引用；缺少第二個 Pool 或停用 Pool，未實測錯誤連線及停用後執行。

### 2026-10-08 BUG-165 修復複核
- **BUG-165 已修正**：複核 `baseConditions` 原本只有 `TENANT_ID`；Registry、Data Action 驗證及選項雖傳入 `poolId`／`status`，SQL 均忽略。這會使第一筆錯誤或停用 Pool 被選到，故不是誤判。
- Pool Mapper 現在依傳入的 `POOL_ID` 與 `STATUS` 篩選；Registry 建立連線前再次確認 Tenant、Pool ID 與 ACTIVE 狀態，Data Action 發布檢查及選項也做相同防護。Mapper SQL 與 Registry 錯誤結果的目標測試通過；尚未以兩筆 Pool 或停用 Pool 進行 MariaDB／HTTP 整合驗收。

### BUG-168：管理表單無法設定 Idle Timeout 與 Max Lifetime

- 嚴重度：低。性質：Vue 管理功能缺口；已由表單模板與後端欄位對照確認。
- 預期：DataSource Pool 管理頁可維護後端支援的主要連線池參數，包含 `idleTimeoutMs` 與 `maxLifetimeMs`；`backend/doc/20-動態資料服務規格.md` 第 4.1 節將 Timeout 與 Lifetime 列為可配置項目。
- 實際：`DataSourcePoolForm.vue` 的 TypeScript 型別與 `newForm()` 具有 `idleTimeoutMs`、`maxLifetimeMs`，儲存時整份 `form.value` 送出，但模板只提供最大連線數、最小閒置數、連線逾時與驗證 SQL 的輸入元件，沒有前述兩個欄位。後端 `FmDataSourcePoolLogicServiceImpl.apply()` 會接受並保存兩個值。使用者只能得到建立時的預設值或維持載入值，無法在畫面上調整。
- 證據：`frontend-v-nx/pages/fm_prog006d0001/components/DataSourcePoolForm.vue` 的 `DataSourcePoolForm`、`newForm()`、`call()` 與完整模板；`FmDataSourcePoolLogicServiceImpl.apply()`；`FmDataSourcePoolRegistry.config()` 將兩值交給 Hikari。未以瀏覽器操作重現。

### 2026-10-08 BUG-168 修復複核
- **BUG-168 已修正**：核對管理頁已有 `idleTimeoutMs`、`maxLifetimeMs` 的型別、預設值與送出資料，後端亦保存並套用，但模板沒有輸入欄位；故不是誤判。
- 管理頁現在可編輯閒置逾時與連線最長存活時間，前後端均拒絕不支援的過小數值，允許 0 表示停用該計時。`npm run build`、後端編譯通過；尚未以瀏覽器及實際 Pool 更新驗收。

2026-10-04 續查 Pool 建立／更新／停用／測試與 Vue 編輯表單：一般更新以 Mapper 的 `LOCK_VERSION` 條件更新，前一輪已確認；本輪確認密碼留空時的測試路徑。Validation Query 直接進入測試與正式 Registry 的 Hikari 設定記為 BUG-164。續查 Controller 權限、清單／表單 Vue、Driver 白名單、密碼加密與 Mapper，發現 `selectListByParams` 忽略 Pool ID／狀態，將 BUG-165 改記為錯誤／停用 Pool 選取根因；原先「停用後會被 ACTIVE 篩選拒絕」的推論不成立。管理表單缺少兩個連線生命週期輸入欄位記為 BUG-168。MariaDB 唯讀確認唯一 `A01/FLOWMINT` Pool 為 ACTIVE、`MINIMUM_IDLE=1`、驗證 SQL `SELECT 1`，並被 25 個 Action 引用，25 個均有 Published 版本；未讀取密碼。BUG-164 的異動 SQL 情境目前沒有現成配置。跨資料庫連線、實際更新／停用及快取清理仍待隔離驗證，第 14 單元維持部分盤查。
