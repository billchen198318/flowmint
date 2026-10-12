## 19 流程進度、表單快照與不可變稽核 — 靜態／唯讀盤查完成

2026-10-08 修復複核：BUG-025 確認為真。規格要求快照只 Insert，但兩個稽核 Service 繼承通用 update/delete，Mapper 也有對應 SQL，資料庫無防改 trigger。已在 `FmFormSnapshotServiceImpl`、`FmTaskActionServiceImpl` 拒絕 update/delete，並新增 `backend/doc/migrations/2026-10-08-immutable-workflow-audit.sql`，以四個 MariaDB trigger 阻止兩表 UPDATE／DELETE。BUG-025 已修正；部署前必須執行移轉。既有資料表空白，尚未在真實 MariaDB 執行 trigger 竄改驗證。

2026-10-05 收尾：已核對流程圖／Action／快照的查詢與 Vue 顯示、`FmRuntimeAuditLogicServiceImpl` 寫入、兩張稽核表 Mapper 與既有 MariaDB 約束／trigger 唯讀證據；XML 測試僅證明 Mapper 可解析。明細存取範圍歸 BUG-024，不可變持久層缺口歸 BUG-025。兩表目前無資料，未執行竄改或端到端快照測試。

已檢查 `FmRequestTrackingLogicServiceImpl.diagram()`、`actions()`、`snapshots()`、`ProcessProgressModal.vue` 的載入與顯示，以及 `FmRuntimeAuditLogicServiceImpl` 的寫入。明細授權共用流程圖權限所造成的資料暴露已記於 BUG-024。

### BUG-025：稽核快照與 Action 的「不可變」未在持久層強制執行

- 嚴重度：中。性質：Java／MariaDB 稽核完整性；未找到目前對外 API 呼叫更新或刪除，因此屬持久層保護缺口，尚非已發生的資料竄改。
- 預期：`README.md` 第 20 節及 `backend/doc/04-流程與表單規格.md` 第 94 行要求表單快照與 Task Action 不可變。寫入後不應透過一般服務或資料庫帳號修改內容或刪除紀錄。
- 實際：`FmRuntimeAuditLogicServiceImpl` 以 insert 寫入；但 `FmFormSnapshotServiceImpl`、`FmTaskActionServiceImpl` 繼承通用 `BaseService`，兩個 Mapper 都保留會修改內容、雜湊、動作與時間的 `update` SQL，以及 `delete` SQL。MariaDB 的 `fm_form_snapshot`、`fm_task_action` 沒有阻止 UPDATE／DELETE 的 trigger；約束也只含主鍵、唯一鍵及 Action 類型檢查。因此這兩類稽核紀錄的不可變性靠呼叫慣例維持，持久層本身未保護。
- 證據：`backend/app/src/main/resources/org/qifu/fm/mapper/FmFormSnapshotMapper.xml`、`FmTaskActionMapper.xml` 的 `<update>`／`<delete>`；`backend/app/src/main/java/org/qifu/fm/service/impl/FmFormSnapshotServiceImpl.java`、`FmTaskActionServiceImpl.java`；MariaDB 唯讀查詢 `information_schema.TABLE_CONSTRAINTS` 與 `information_schema.TRIGGERS`。目前兩表皆 0 筆，未修改資料測試。
