## 15 Data Action（`FM_PROG006D0002`）— 靜態／唯讀盤查完成

2026-10-05 收尾：已對照動態資料服務規格、補強項目、管理 Controller 的清單／稽核／建立／更新／載入／發布／Preview／Pool 選項，以及公開 execute／options／metadata／stream，並核對管理 Vue、Executor、參數解析、SQL 驗證與現有相關單元測試。七項既有觀察涵蓋版本綁定、Server 參數、稽核、FOR_EACH timeout、已發布設定與分頁／COUNT SQL；Pool 選取另歸第 14 單元。既有資料不足以做大表、跨資料庫或真實執行稽核重現；本單元完成限靜態與既有唯讀範圍。

### BUG-171：SELECT_LIST 分頁會把目標頁之前的全部資料載入 JVM

- 嚴重度：高。性質：查詢效能與記憶體耗用；程式碼推論，未以大量資料實測。
- 預期：分頁請求的資料庫查詢與 Java 記憶體用量應受每頁 `pageSize` 約束；`backend/doc/FM_PROG006D0002補強項目.md` 要求搜尋、分頁與最大筆數有明確上限。
- 實際：`FmDataActionParameterResolver.copyPagination()` 容許 `page` 至 1,000,000；`FmDataActionExecutor.executePagedList()` 僅限制 `pageSize <= step.maxRows` 並防 `offset + pageSize` 整數溢位，接著設 `JdbcTemplate.setMaxRows(offset + pageSize)`，以 `queryForList()` 把該頁之前所有列實體化，最後才用 `subList()` 擷取單頁。深頁請求可能使 SQL 掃描與 JVM 配置量隨頁碼增長，遠超過 Step 的 `maxRows`；例如 `page=100000`、`pageSize=1000` 時，若資料足夠，單次請求最多會讀入一億列後才回傳一千列。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/dataaction/FmDataActionParameterResolver.java` 的 `copyPagination()`；`FmDataActionExecutor.java` 的 `executePagedList()`；`backend/doc/FM_PROG006D0002補強項目.md` 第 58、392～415 行。尚未以實際大表或記憶體量測驗證。

### 2026-10-08 BUG-171 修復複核
- **BUG-171 已修正**：複核 `executePagedList()` 原以 `offset + pageSize` 設定 JDBC `maxRows`，再用 `queryForList()` 載入並截取目標頁；深頁記憶體成本隨頁碼增長，故不是誤判。
- 現在由 MariaDB／MySQL、Oracle 或 SQL Server 執行原生 OFFSET 分頁，Java 只接收該頁列；COUNT 回應格式維持既有契約。單頁上限 10,000 筆，SQL Server 分頁要求最外層 ORDER BY。`FmDataActionExecutorColumnNameTest` 目標測試通過；尚未以三種資料庫及大表量測驗收。

### BUG-172：分頁 COUNT SQL 可能誤刪雙引號字串中的 ORDER BY

- 嚴重度：中。性質：SQL 改寫錯誤；程式碼推論，未送出資料庫查詢重現。
- 預期：產生 COUNT SQL 時只移除最外層查詢真正的 `ORDER BY`，字串常值中的內容應保持完整。
- 實際：`FmDataActionExecutor.stripTopLevelOrderBy()` 只追蹤單引號，未辨識雙引號字串。對 `SELECT "x ORDER BY y" AS label FROM t ORDER BY id`，函式會在字串內第一個 `ORDER BY` 截斷為 `SELECT "x`；`executePagedList()` 再包成 `SELECT COUNT(*) FROM (SELECT "x) FM_DATA_ACTION_COUNT`，造成語法錯誤。`FmDataActionSqlValidator.validate()` 沒有排除雙引號字串；現有單元測試僅涵蓋單引號。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/dataaction/FmDataActionExecutor.java` 的 `stripTopLevelOrderBy()`、`executePagedList()`；`FmDataActionSqlValidator.java` 的 `validate()`；`backend/app/src/test/java/org/qifu/fm/domain/dataaction/FmDataActionExecutorColumnNameTest.java` 的 `removesOnlyTopLevelOrderByForCountQuery()`。是否命中取決於 Action SQL 實際使用雙引號字串及資料庫 SQL mode，尚未以現有配置確認。

### 2026-10-08 BUG-172 修復複核
- **BUG-172 已修正**：複核 COUNT SQL 仍使用 `stripTopLevelOrderBy()`，原方法只追蹤單引號；雙引號內的 `ORDER BY` 確實會被當作外層排序切掉，故不是誤判。
- 解析器現在跳過單引號、雙引號及方括號內容與巢狀查詢，並辨識重複引號及反斜線跳脫；只移除最外層真正的 `ORDER BY`。`FmDataActionExecutorColumnNameTest` 目標測試通過；尚未用實際雙引號 SQL 做資料庫整合驗收。

### BUG-173：編輯既有表單 Data Action Binding 會改綁目前 Action 版本

- 嚴重度：中。性質：Vue 表單設計與版本引用；靜態推論，未在瀏覽器操作重現。
- 預期：編輯既有 Binding 的事件或 Mapping 時，原本固定的 `actionVersion` 應保留，除非使用者明確選擇升級；`backend/doc/20-動態資料服務規格.md` 第 470 行要求表單固定引用 Action Version，避免 Current Version 變動造成漂移。
- 實際：`FormDataActionBindingEditor.vue` 的 `editBinding()` 載入既有 `actionCode` 與 Mapping，沒有保存既有 `actionVersion`；接著 `loadMetadata(false)` 呼叫只依 Action Code 查詢的 `/metadata`。後端 `FmDataActionLogicServiceImpl.metadata()` 一律回傳 Action 主檔的 `currentVersionNo`。按保存時 `saveBinding()` 無條件把 `metadata.value.versionNo` 寫進 Binding。若原 Binding 為 v1 而 Action 已發布 v2，僅編輯 Mapping 並保存就會改成 v2；畫面顯示目前 Metadata 版本，但沒有保留舊版或明確升級的控制。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDataActionBindingEditor.vue` 的 `editBinding()`、`loadMetadata()`、`saveBinding()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmDataActionLogicServiceImpl.java` 的 `metadata()`；`backend/doc/20-動態資料服務規格.md` 第 470、602 行。需有舊版 Binding 與新 Current Version 才會發生，尚未以現有表單驗證。

### 2026-10-08 BUG-173 修復複核
- **BUG-173 已修正**：複核編輯流程原本未保存既有 `actionVersion`，儲存時直接採用目前 Metadata 版本；Action 發布新版後僅編輯 Mapping 就會改綁，故不是誤判。
- 編輯既有 Binding 時現在保留固定版本；只有新建、改選 Action 或明確按「升級綁定版本」才採用目前版本。畫面同時顯示綁定與目前版本，提醒舊版 Binding 的欄位建議來自目前 Metadata。`npm run build` 通過；尚未用 v1 Binding／v2 Action 的瀏覽器情境驗收。

2026-10-04 續查 `backend/doc/20-動態資料服務規格.md`、`backend/doc/FM_PROG006D0002補強項目.md`、Data Action 參數解析、Executor、發布驗證、Vue 表單與 MariaDB。唯讀確認 Action 25 筆、Version 34 PUBLISHED／12 DRAFT／3 RETIRED、Execution Audit 85 筆；目前 `FOR_EACH` Step 與「`tenantId` 對 `${item...}`」Mapping 均為 0 筆，BUG-155 尚無現成資料可重現。

### BUG-155：Step Mapping 可覆蓋 Server 提供的 Tenant 與登入帳號參數

- 嚴重度：高。性質：Data Action SQL 參數的 Tenant／身分邊界；須有已發布 Action 將保留參數映射到 `${item...}` 或 `${steps...}`，目前資料庫無此配置，屬程式碼推論。
- 預期：`backend/doc/20-動態資料服務規格.md` 第 245 行與 `backend/doc/FM_PROG006D0002補強項目.md` 第 23～32 行規定 `:tenantId`、`:loginAccount` 由 Server 提供，Request 不可覆蓋。
- 實際：`FmDataActionParameterResolver.resolve()` 最後寫入 Server 的 `tenantId`、`loginAccount`；但 `resolveForStep()` 複製這份參數後，又依 Request Schema 的 `${item...}`／`${steps...}` Mapping 對同名鍵執行 `put()`，沒有再次保護保留參數。`FmDataActionLogicServiceImpl.readRequestMappings()` 與發布驗證未拒絕 Mapping 的保留鍵。若 `FOR_EACH` Action 將 `tenantId` 映射到 `${item.tenantId}`，`FmDataActionExecutor.executeForEach()` 傳入使用者提供的 item，SQL 的 `:tenantId` 即可能改用 item 值；`loginAccount` 與前一步結果引用也有同型路徑。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/dataaction/FmDataActionParameterResolver.java` 的 `resolve()`、`resolveForStep()`；`FmDataActionExecutor.java` 的 `executeSteps()`、`executeForEach()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmDataActionLogicServiceImpl.java` 的 `readRequestMappings()`、`validateForPublish()`。未以跨 Tenant SQL 實際執行驗證。

### 2026-10-08 BUG-155 修復複核
- **BUG-155 已修正**：複核初次 `resolve()` 雖以 Server 值覆蓋 `tenantId`、`loginAccount`，但後續 `resolveForStep()` 的 `${item...}`／`${steps...}` 與無 Schema 時的 item Map 都可再次覆蓋；故不是誤判。
- Step 解析現在保護 Tenant、登入帳號、時間、流程識別及已驗證的分頁參數，動態 Mapping 與原始 item 都不能改寫；發布時拒絕新的保留鍵動態 Mapping。`FmDataActionParameterResolverTest` 驗證兩條覆蓋路徑，目標測試通過；尚未以跨 Tenant SQL 做整合驗收。

### BUG-166：Data Action 在參數、Step 或 Pool 前置檢查失敗時不產生執行稽核

- 嚴重度：中。性質：Java 執行稽核；靜態推論，未送出失敗請求。
- 預期：已定位到 Action／版本的執行嘗試，若因 Request 參數、Step 驗證或 Pool 不可用而失敗，也應在 Execution Audit 中留下失敗狀態，便於追查呼叫及錯誤。`backend/doc/FM_PROG006D0002補強項目.md` 第 14.7 節要求記錄執行成功或失敗。
- 實際：`FmDataActionExecutor.execute()` 在產生 `executionId` 及進入 `try/finally` 前，先呼叫 `parameterResolver.resolve()`、`validateSteps()`、`poolRegistry.get()`。其中任一步驟拋錯，`auditRecorder.record()` 均不會執行。`stream()` 也在 `try/finally` 前完成參數解析、Step 驗證及 Streaming Step 要求檢查，因此這些失敗同樣漏記。正式 `execute()` 的 `poolRegistry.get()` 也在稽核保護範圍外；例如同 Tenant 完全沒有 Pool 時會在此處拋錯。停用 Pool 的檢查目前另受 BUG-165 所述 Mapper 缺漏影響，不能以停用 Pool 作為此錯誤路徑的既定重現條件。
- 證據：`FmDataActionExecutor.execute()`／`stream()` 的呼叫順序與 `finally`；`FmDataSourcePoolRegistry.loadActivePool()` 在查無資料時拋錯；`FmDataActionExecutionAuditRecorder.record()` 是 Execution Audit 寫入入口。MariaDB 先前唯讀確認 Execution Audit 85 筆；未製造前置失敗或比對新紀錄。

### 2026-10-08 BUG-166 修復複核
- **BUG-166 已修正**：複核 `execute()` 與 `stream()` 原在產生 Execution ID、進入 `try/finally` 前解析參數及驗證 Step；`execute()` 更在之前取得 Pool，故這些失敗不會寫稽核，並非誤判。
- 已定位 Action／版本後即建立 Execution ID，前置檢查與實際執行共用失敗記錄；參數尚未解析時記 0 個參數。`FmDataActionPreflightAuditTest` 驗證參數失敗、Pool 不可用與 Streaming 失敗均記錄 FAILED，目標測試通過；尚未用實際資料庫查驗稽核列。

### BUG-167：FOR_EACH SQL 未套用所設定的 Step Query Timeout

- 嚴重度：中。性質：Java Data Action 執行限制；靜態推論，未以慢查詢重現。
- 預期：每個 SQL Step 的 `queryTimeoutSeconds` 應在 `ONCE`、`FOR_EACH` Batch 及 Generated Key 路徑生效，避免單一步驟長時間佔用連線。`backend/doc/20-動態資料服務規格.md` 的執行與稽核原則要求每個 Step 設定 Timeout。
- 實際：`FmDataActionExecutor.executeStep()` 會在 ONCE 路徑先呼叫 `jdbcTemplate.getJdbcTemplate().setQueryTimeout(...)`。`executeForEach()` 的 Batch 分支直接呼叫 `jdbcTemplate.batchUpdate()`，Generated Key 分支直接呼叫 `executeMutation()`，兩者都不經 `executeStep()` 且沒有自行設定 Query Timeout。第一個 Step 若為 FOR_EACH 就採用 JDBC Template 預設逾時；若前面執行過其他 Step，則可能沿用前一個 Step 的逾時值，而非目前 Step 的設定。交易包裝亦未指定整體 Timeout。
- 證據：`FmDataActionExecutor.executeSteps()`、`executeForEach()`、`executeStep()`、`executeInTransaction()`；`FmDataActionLogicServiceImpl.toStep()` 保存 `queryTimeoutSeconds`；Vue `DataActionForm.vue` 提供 Step Query Timeout 欄位。MariaDB 先前唯讀確認目前 FOR_EACH Step 0 筆；未執行慢查詢或 Batch 重現。

### 2026-10-08 BUG-167 修復複核
- **BUG-167 已修正**：核對執行分支後確認 Batch 與 Generated Key 均繞過原本設定 Timeout 的 `executeStep()`，並非誤判。現在每個通過條件的 Step 在分流前設定自己的 Query Timeout，ONCE 與兩種 FOR_EACH 路徑均適用。
- `FmDataActionForEachTimeoutTest` 驗證 Batch 與 Generated Key Step 各自設定的 Timeout；目標測試通過。尚未以慢查詢驗證資料庫實際中斷行為。

### BUG-017：編輯草稿會立即改變已發布 Data Action 的共用設定

- 嚴重度：高。性質：Java 版本隔離與 MariaDB 資料模型。
- 預期：已發布版本在新草稿編輯期間保持原執行行為；`backend/doc/20-動態資料服務規格.md` 第 5.2 節要求版本化避免已發布表單因 Action 修改而變動。
- 實際：`FmDataActionLogicServiceImpl.update()` 建立／更新草稿步驟時，同時呼叫 `applyAction()` 修改 `fm_data_action` 主檔的 `POOL_ID`、`ACTION_TYPE`、`REQUEST_SCHEMA`、`RESPONSE_MODE`、`STATUS` 與 rate limit。`execute()` 即使指定舊的 Published 版本，也把這個目前可變的 Action 主檔傳入 executor；`fm_data_action_version` 沒有這些設定的快照欄位。因此儲存新草稿後、尚未發布前，既有表單綁定的舊 Published 版本可能改用新連線池或新參數契約。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmDataActionLogicServiceImpl.java` 的 `update()`、`applyAction()`、`execute()`；`backend/app/src/main/java/org/qifu/fm/entity/FmDataActionVersion.java`。MariaDB 唯讀查詢顯示 12 個已發布 Action 同時有 Draft，屬於此路徑可能發生的現況；因版本表沒有設定快照，未以唯讀查詢判定它們是否已發生漂移。

### 2026-10-08 BUG-017 修復複核
- **BUG-017 已修正（部署須先執行資料庫移轉）**：複核確認更新草稿會覆寫執行時使用的主檔設定，並非誤判。草稿設定現在存於版本 `ACTION_CONFIG`，更新草稿只推進主檔 Lock Version；發布時才更新主檔，執行、Streaming 與 Metadata 依指定版本讀取設定快照。
- 新建資料庫的 `flowmint.sql` 與既有資料庫移轉檔均加入欄位和基準資料回填。舊版本原始設定未留存，回填只能使用目前主檔值，無法還原既已發生的漂移。`FmDataActionVersionIsolationTest` 驗證草稿更新不改已發布主檔、指定版本執行使用快照，目標測試通過。移轉尚未在既有 MariaDB 執行。

2026-10-04 續查 `FmDataActionExecutor` 的 Streaming、分頁、`ONCE`／`FOR_EACH`、Generated Key、Retry 與執行稽核，對照 `FmDataActionSqlValidator`、`FmDataActionParameterResolver`、Logic 發布驗證、`FmDataActionController` 及 Vue 的 Step 設定。確認 Streaming 僅接受單一 ACTIVE 的 `ONCE / SELECT_LIST` 且不重試；分頁參數由 Resolver 限制正整數，Executor 按 Step `maxRows` 限制 `pageSize`。本輪新增 BUG-166／167；分頁大量偏移的記憶體成本與 Streaming 中途失敗的 HTTP 表示仍待實測或進一步規格核對，未先列缺陷。第 15 單元仍為部分盤查。
