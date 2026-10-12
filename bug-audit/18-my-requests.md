## 18 我的申請、撤回與取消 — 靜態／唯讀盤查完成

2026-10-08 修復複核：BUG-024 確認為真。`README.md` 第 19 節明定「我的申請」明細只允許表單 Owner 或實際發起人；原 `load()` 卻使用允許簽核參與者的 `canViewDiagram()`。現已將 `/mine/load` 的授權收緊為 Owner／發起人；`/mine/diagram` 繼續允許流程參與者查看。第 19A 節的「我的流程紀錄」另有簽核者明細入口，不受此修正影響。BUG-024 已修正（靜態確認；無既有流程資料供多帳號 E2E 重現）。

2026-10-05 收尾：已對照 README、流程規格、`FmProcessRuntimeController` 的 `/mine`、`/mine/load`、`/mine/withdraw`、`/mine/cancel`、`/mine/diagram`，`FmRequestTrackingLogicServiceImpl` 及申請明細 Vue。明細權限差異見 BUG-024；撤回時點在兩份規格中的差異保留為契約待決，不列新缺陷。既有流程實例為 0，未執行實際撤回／取消或參與者存取重現。

2026-10-04 續查 `FmRequestTrackingLogicServiceImpl.withdraw()`／`cancel()`／`terminateRequest()` 與 Vue 申請詳情頁。後端會重查表單 Owner／實際發起人、`RUNNING` 狀態並鎖定表單資料；尚無流程實例可重現。`backend/doc/04-流程與表單規格.md` 將撤回描述為「未被他人處理前」，但 `README.md` 第 19 節寫為 Owner 可撤回 `RUNNING` 流程；兩份契約不同，本輪不據此新增缺陷，待規格統一後再核對行為。

### BUG-024：簽核參與者可透過我的申請明細 API 讀取非本人申請的完整表單

- 嚴重度：高。性質：Java 明細讀取授權；現有流程實例 0 筆，尚無資料可實際重現。
- 預期：`README.md` 第 19 節規定「只有表單 Owner 或實際發起人可查看明細」。流程圖另外允許目前及歷史簽核參與者查看；兩種權限範圍不同。
- 實際：`FmRequestTrackingLogicServiceImpl.load()` 使用 `canViewDiagram()` 授權。該方法除了 Owner 與發起人，也接受目前候選／受派簽核人，以及 `fm_task_action` 中曾有動作紀錄的帳號。通過後 `load()` 回傳完整表單內容、表單 schema 與 Custom JavaScript、所有 Action、不可變快照及平行加簽資料。簽核參與者即使不是 Owner 或發起人，也可直接呼叫 `/api/fm/requests/mine/load` 讀取這些明細；Vue 只顯示入口與否不能限制 API。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmRequestTrackingLogicServiceImpl.java` 的 `load()`、`canViewDiagram()`、`diagram()`；`backend/app/src/main/java/org/qifu/fm/controller/FmProcessRuntimeController.java` 的 `/mine/load`；`README.md` 第 19 節。MariaDB 唯讀 `SELECT` 顯示 `fm_process_instance`、`fm_form_data`、`fm_task_action`、`fm_form_snapshot` 均為 0 筆，無現成參與者資料可重現。
