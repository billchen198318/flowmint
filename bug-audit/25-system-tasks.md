## 25 System Task、Data Action Task 與 Groovy worker — 靜態／唯讀盤查完成

2026-10-05 收尾：已對照第 33／35 章、Data Action 與 Groovy Delegate、發布／Runtime 固定版本及 manifest、Claim／Commit／Retry／Recalculate／Incident、營運處理 Controller／Vue 與現有 Groovy／Data Action Task 測試。BUG-151／152／169／170 保留為既有觀察；Groovy 核心路徑本輪未見獨立根因。Mockito 與交易 fixture 不證明 MariaDB 原子性；現有執行資料為 0，未重跑 Flowable／MariaDB E2E 或故障注入，這些維持驗證限制。

2026-10-04 Groovy 人工處理續查：`FmGroovyRetryLogicServiceImpl` 與 `FmGroovyRecalculateLogicServiceImpl` 的 preview 及命令均由 `FmGroovyOperateAccess.require()` 驗證操作權限與 Tenant；命令在 Process、Execution 鎖下檢查 revision／generation、requestId 重送及當前狀態。Recalculate 還核對表單 revision、lock、內容 SHA 與腳本 binding SHA。`FmGroovyIncidentHandlingLogicServiceImpl` 的查詢與狀態更新使用 Tenant 篩選、歷史分頁、revision 與 requestId；`FmSystemTaskIncidentActionMapper.xml` 以 ACTION_NO 倒序查詢。`GroovyIncidentPanel.vue` 使用 preview 回傳值送命令，網路例外時保留同一 requestId 供重送。`pages/operations/incidents.vue` 的一般 Incident 頁面另用 `/incidents` API，不是 Groovy 處理 API。此輪未確認獨立新缺陷；尚未以實際 Groovy 執行資料測試競態與交易回滾，下一步核對相關測試、開關與查詢邊界。

2026-10-04 測試與開關續查：Groovy Retry、Recalculate、Incident、Claim、Commit 等 Logic 測試使用 Mockito 與 `GroovyTransactionFixture`；該 fixture 的註解明確指出它只測 Spring 交易邊界，不模擬資料庫原子性。沒有因此把真實 MariaDB 回滾或併發視為已驗證。`application-system-task-local.properties` 啟用 Incident Handling、Manual Retry、Recalculate；各服務的 `@Value` 預設為 false。Incident 清單 offset 0～10000、每頁 50；處理歷史 offset 0～1000000、每頁 50，Logic、Service、Mapper 參數一致。此輪未確認新的獨立缺陷；續查 Groovy 發布與 Runtime 整合邊界。

2026-10-04 發布／Runtime 固定內容續查：`FmProcessDefLogicServiceImpl.publish()` 對 Groovy 先在交易外呼叫編譯，`FmGroovyPublishCoordinatorLogicServiceImpl.commit()` 再以新交易執行 `FmGroovyPublicationLogicServiceImpl.persist()`、部署與版本發布；manifest 保存與版本更新受同一交易控制。`FmGroovyRuntimeDefinitionLogicServiceImpl.load()` 只接受 PUBLISHED／RETIRED 固定流程與表單版本，核對 Tenant、版本、manifest 與 engine profile；`FmGroovyRuntimePreparation` 以 `FmGroovyVersionManifest.verify()` 重算 XML／binding hash，並驗證表單 Mapping。第 35 章記載過去已做核心 Flowable／MariaDB E2E；本輪未重新執行，也未以目前空的執行表驗證並發時序。此段未確認獨立新缺陷；下一步回第 15 單元 Data Action 分頁／Streaming。

已檢查 `FmDataActionTaskDelegate`、`FmGroovyTaskDelegate`、`FmGroovyRuntimeCommitLogicServiceImpl` 的主要執行與寫回路徑。Data Action Published 版本使用可變主檔設定的問題記於 BUG-017。Groovy 租約回收、前端與資料庫狀態仍待盤查。

2026-10-04 續查 `backend/doc/33-SystemTask與DataActionTask規劃.md` 與 `backend/doc/35-SystemTask的Groovy腳本規劃.md`，核對 Data Action Delegate、表單 Mapper、Groovy 寫回與自動重試路徑。MariaDB 唯讀確認 `fm_process_system_task`、`fm_system_task_execution` 均為 0 筆；未執行寫入重現。

### BUG-151：Data Action Task 寫回表單後缺少對應快照

- 嚴重度：高。性質：System Task 表單稽核缺口；目前無執行資料，屬程式碼推論。
- 預期：`backend/doc/33-SystemTask與DataActionTask規劃.md` 第 162 行要求寫回 `FORM_DATA` 時同步 Flowable 變數、表單資料，並定義 Revision、Snapshot 與並發規則，避免待辦、歷程快照與 Gateway 看到不同資料；`backend/doc/04-流程與表單規格.md` 要求有效動作保留不可變快照。
- 實際：`FmDataActionTaskDelegate.execute()` 在 response mapping 非空時更新 Flowable 變數，接著 `persistFormData()` 呼叫 `updateDataContent()`。Mapper 同時增加 `REVISION_NO` 與 `LOCK_VERSION`，但這條 Delegate 路徑沒有建立 `fm_form_snapshot`。若後續 Task 或查詢依快照還原該 revision，會缺少 Data Action 產生的版本；相對地 Groovy 寫回路徑在增加 revision 後建立快照。
- 證據：`backend/app/src/main/java/org/qifu/fm/flowable/FmDataActionTaskDelegate.java` 的 `execute()`、`persistFormData()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmFormDataMapper.xml` 的 `updateDataContent`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmGroovyRuntimeCommitLogicServiceImpl.java` 的寫回與 snapshot。尚未用執行中流程驗證前端歷程呈現。

### BUG-152：Data Action Task 的 Response Mapping 可寫入未經表單 Schema 驗證的欄位

- 嚴重度：高。性質：System Task 表單資料完整性；目前無 System Task 配置及執行紀錄，屬程式碼推論。
- 預期：`backend/doc/33-SystemTask與DataActionTask規劃.md` 第 158 行要求 Response Mapping 的 `FORM_DATA` 目標為已發布表單 Schema 中存在、型別相容且允許持久化的欄位；`backend/doc/04-流程與表單規格.md` 規定 Runtime 只接受 Schema 定義的欄位並在後端重新驗證。
- 實際：`FmBpmnDesignValidator.validateMapping()` 只驗證目標為格式合法的 `FORM_DATA.<path>`；`FmDataActionTaskPublishValidator.validateMappings()` 只確認來源 root 是 Action 結果 key，未檢查目標欄位是否存在或型別相容。設計器的 `DataActionTaskPanel.vue` 接受 JSON 文字。執行時 `FmDataActionTaskDelegate.applyResponse()` 只要求目標以 `FORM_DATA.` 開頭，再用 `setPath()` 新增或覆寫欄位，`persistFormData()` 直接更新 JSON，沒有呼叫表單 Schema 驗證。設定錯誤但格式合法的欄位路徑或型別可進入正式表單資料。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidator.java` 的 `validateMapping()`；`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmDataActionTaskPublishValidator.java` 的 `validateMappings()`；`backend/app/src/main/java/org/qifu/fm/flowable/FmDataActionTaskDelegate.java` 的 `applyResponse()`、`setPath()`、`persistFormData()`；`frontend-v-nx/pages/fm_prog004d0001/components/DataActionTaskPanel.vue` 的 Response Mapping 輸入。

### BUG-169：Data Action Task 發布驗證使用不存在的 STEP_NO 欄位排序

- 嚴重度：高。性質：Java／MyBatis 發布阻斷；SQL 錯誤已以 MariaDB 唯讀查詢確認，未發布新流程重現。
- 預期：含 DATA_ACTION Service Task 的流程應能查出固定 Action Version 的 SQL Step，完成 Request／Response Mapping 驗證後發布。
- 實際：`FmDataActionTaskPublishValidator.validateMappings()` 呼叫 `stepService.selectListByParams(params, "STEP_NO", "ASC")`。`FmDataActionStepMapper.xml` 會把 `orderBy` 放入 `ORDER BY ${orderBy}`，但 `fm_data_action_step` 表與 Mapper 的 Step 排序欄位是 `EXECUTION_ORDER`，沒有 `STEP_NO`。`validateMappings()` 對每個 DATA_ACTION Task 都執行這個查詢，包含 Response Mapping 為空的情況；因此前面 Action／Version 驗證通過後，流程發布會因未知欄位而失敗。
- 證據：`FmProcessDefLogicServiceImpl.publish()` 呼叫 `FmDataActionTaskPublishValidator.validate()`／`validateDataActions()`；`FmDataActionTaskPublishValidator.validateMappings()`；`FmDataActionStepMapper.xml` 的 `selectListByParams`；`backend/doc/flowmint.sql` 的 `fm_data_action_step` DDL。MariaDB CLI 唯讀確認 Step 49 筆；`SELECT STEP_ID FROM fm_data_action_step ORDER BY STEP_NO LIMIT 1` 回 `ERROR 1054 (42S22): Unknown column 'STEP_NO' in 'ORDER BY'`。未異動流程或資料庫。

### BUG-170：Response Mapping 可引用停用 Step，Runtime 將缺失結果寫成 null

- 嚴重度：中。性質：Data Action Task 發布驗證／表單寫回不一致；BUG-169 修復後才可沿正式發布路徑重現，目前屬靜態推論。
- 預期：Response Mapping 的來源應是固定 Action Version 中實際會執行、且會產生結果的 ACTIVE Step；停用 Step 的 Result Key 應在流程發布時被拒絕。
- 實際：`FmDataActionTaskPublishValidator.validateMappings()` 從該版本所有 Step 蒐集 Result Key，只排除 `ResultMode=NONE`，沒有檢查 `STATUS=ACTIVE`。`FmDataActionExecutor.executeSteps()` 遇到非 ACTIVE Step 會跳過，回傳資料不含其 Result Key。`FmDataActionTaskDelegate.applyResponse()` 讀取缺失來源路徑得到 `null`，`setPath()` 仍把 `null` 寫入 `FORM_DATA`，並持久化。設定為 INACTIVE 的 Step 因此可在發布驗證通過後清空目標欄位。
- 證據：`FmDataActionTaskPublishValidator.validateMappings()`；`FmDataActionExecutor.executeSteps()`；`FmDataActionTaskDelegate.applyResponse()`／`path()`／`setPath()`／`persistFormData()`。MariaDB 唯讀確認目前 Published 版本共有 34 筆 Step，全部 ACTIVE；沒有現成停用 Step 與 Response Mapping 可重現，且 BUG-169 現階段會先阻斷發布。

2026-10-04 續查 DATA_ACTION 發布路徑，核對 Action／Version／Step Mapper 篩選、流程發布入口、Step 排序欄位與 MariaDB 表結構。Action、Version、Step 的 `selectListByParams` 均有相應動態篩選；第 14 單元 Pool Mapper 漏篩的問題未在這三張表重現。發布驗證的錯誤排序欄位記為 BUG-169；停用 Step 被算入 Response Mapping 可用來源記為 BUG-170。Groovy worker 與本單元其他生命週期仍待盤查。

2026-10-04 Groovy 續查：依 `backend/doc/35-SystemTask的Groovy腳本規劃.md` 第 1～14 節及後續主 JVM 決策，核對 `FmGroovyLeaseReaper`／`FmGroovyRecoveryLogicServiceImpl`、READY Reaper／Recovery、Automatic Retry Scheduler／Logic／Policy，以及 Execution Mapper 的到期、READY 與重試查詢。Lease 到期會以獨立交易把 RUNNING attempt 標為 ABANDONED、execution 標為 FAILED，不將未知結果直接自動重送；Automatic Retry 只接受 `GROOVY_RUNTIME_BUSY`／`GROOVY_RUNTIME_START_UNAVAILABLE` 並以 FAILED attempt 核對，與較新文件一致，未新增條目。本機 `application-system-task-local.properties` 啟用 Runtime、Lease／READY recovery，Automatic Retry 仍為預設關閉，不能把功能可用與目前已開啟混同。MariaDB 唯讀確認目前 `fm_system_task_execution`、`fm_system_task_attempt`、`fm_process_groovy_manifest`、`fm_process_system_task` 均為 0 筆；沒有現成故障／恢復資料可觀察。下一步查 Groovy Claim、Evaluation、Commit 與取消／人工處理的交易邊界，以及前端營運頁；第 25 單元仍為部分盤查。

同輪續查 `FmGroovyTaskDelegate`、`FmGroovyRuntimeClaimLogicServiceImpl`、`FmGroovyRuntimeEvaluationLogicServiceImpl`：Delegate 在 Flowable 交易外啟動 claim／evaluation，失敗記錄走獨立交易；Claim 固定 generation 與 60 秒 lease，Evaluation 在執行前後核對 RUNNING／lease／profile 與固定輸入。`FmGroovyDraftValidator` 將腳本 Timeout 限於 100～10000 ms，故未把「腳本設定逾時長於 60 秒 lease」列為缺陷。尚需繼續核對 Commit、交易回滾時序、取消與維運操作，不能僅憑此標完成。

2026-10-04 續查 Groovy Commit 與取消：`FmGroovyRuntimeCommitLogicServiceImpl.apply()` 要求 Flowable 交易，核對 occurrence、RUNNING generation／lease、唯一 Attempt、profile 與固定輸入，對需要寫回的結果執行 Form CAS、建立 `SYSTEM_TASK_APPLY` Snapshot，再更新 Attempt／Execution 成功紀錄及 Flowable 變數；Mapper 的 `complete` 再檢查 lease。`FmGroovyTaskDelegate.afterExecution()` 返回後由同一 Flowable 交易推進，失敗時回滾寫回並嘗試以獨立交易記錄失敗。`FmGroovyCancellationLogicServiceImpl` 在流程／execution 取消時鎖流程與活躍 ledger，取消 RUNNING Attempt 並增加 Execution generation；`FmGroovyExecutionDeletionListener` 同步接線。此段未發現可獨立列案的缺陷；目前資料庫無 System Task 執行資料，尚未以真實取消競態驗證。下一步查人工 Retry／Recalculate、Incident 查詢與前端營運頁，並回頭核對交易競態測試覆蓋；第 25 單元維持部分盤查。
