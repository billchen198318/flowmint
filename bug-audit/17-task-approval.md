## 17 我的待辦、簽核動作、退回、加簽與改派 — 靜態／唯讀盤查完成

2026-10-05 收尾：已對照既有規格、`FmProcessRuntimeController` 的待辦／動作／轉派／代理／加簽入口、`FmTaskRuntimeLogicServiceImpl`、`pages/tasks/[id].vue` 與相關 Task Runtime／欄位政策測試。操作人資格及 REJECT／RETURN 帶表單資料的差異分別留在 BUG-019／150；多人簽核與營運重派的跨單元細節歸 12／20。既有唯讀資料中流程實例及 Task Action 為 0，未做 HTTP 或 Flowable 重現；此處完成指靜態與既有唯讀範圍。

2026-10-04 對照 `README.md`、`backend/doc/04-流程與表單規格.md`、`backend/doc/05-安全權限與稽核.md`、`backend/doc/27-正式會簽配置與實作指南.md`、`backend/doc/28-流程簽核改派規劃.md`，核對 Java `FmTaskRuntimeLogicServiceImpl` 的 action／transfer／delegate／resolve／addSign 路徑，以及 Vue `pages/tasks/[id].vue` 的送出資料。MariaDB 唯讀確認 `fm_process_version` 4 筆、`fm_form_version` 4 筆，`fm_process_instance` 與 `fm_task_action` 均為 0 筆；本輪未進行寫入重現。

### BUG-019：已停用員工若 membership 仍有效，可能繼續處理原待辦

- 嚴重度：高。性質：Java Runtime 授權；目前流程實例與簽核動作皆為 0 筆，無現成資料可重現。
- 預期：`README.md`「員工與任職管理」要求起單、轉派、加簽、代理與管理員 Reassign 執行時重新驗證員工及 membership；待辦核決也應拒絕已停用或逾期的員工。
- 實際：`FmTaskRuntimeLogicServiceImpl.action()` 以 `currentAccount()` 取得操作人；該方法只查 `fm_tenant_account` 的啟用狀態與有效期間，不查操作人的 `fm_employee.STATUS` 或有效期間。`authorizedTask()` 核對 Flowable 指派關係，但若待辦是在員工停用前指派，已停用員工仍可能完成核准、駁回等動作。相同服務的 `transfer()` 對轉派**目標**呼叫 `activeEmployee()`，可見目標端有員工驗證，而操作人端沒有。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTaskRuntimeLogicServiceImpl.java` 的 `action()`、`currentAccount()`、`activeEmployee()`。MariaDB 唯讀查詢顯示目前 `fm_process_instance` 與 `fm_task_action` 都為 0 筆，未做寫入重現。

### 2026-10-08 BUG-019 修復複核
- **BUG-019 已修正**：核對 `currentAccount()` 原本只查 Tenant 與 membership，已停用但仍有有效 membership 的操作人可進入原待辦，並非誤判。現在所有經此入口的待辦操作共用 `activeEmployee()`，先要求員工 ACTIVE 且在生效期間，再確認 Tenant membership。
- `FmTaskRuntimeCompletionTest` 驗證員工查詢限制 ACTIVE 且缺少有效員工時拒絕；目標測試通過。尚未以停用員工的 Flowable 待辦執行 HTTP 驗收。

### BUG-150：REJECT／RETURN 可夾帶 formData 改寫表單內容

- 嚴重度：高。性質：Java Task Action API 的動作與表單更新契約不一致；目前沒有流程實例，屬程式碼推論，未用實際 API 重現。
- 預期：`README.md` 規定表單 `beforeSubmit` 用於正式起單、`APPROVE`、`RESUBMIT`，不在 `REJECT`／`RETURN` 執行；任務畫面也只在 `APPROVE`／`RESUBMIT` 送出表單資料。駁回或退回應只記錄動作及理由，不接受同一請求改寫申請內容。
- 實際：`FmTaskRuntimeLogicServiceImpl.action()` 只判斷 `request.formData() != null`，就執行 `BEFORE_FORM_UPDATE` Hook、`updateTaskForm()` 與 Flowable 表單變數寫入，之後才依動作執行 `REJECT` 或 `RETURN`。`validatePolicy()` 沒有拒絕這兩種動作夾帶 `formData`。具 Task 處理權的人可直接呼叫 API，附上目前 revision 和符合欄位政策的表單資料，使駁回或退回動作同時改變內容與 revision，與畫面及 lifecycle 契約不同。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTaskRuntimeLogicServiceImpl.java` 的 `action()`、`validatePolicy()`、`updateTaskForm()`、`executeAction()`；`frontend-v-nx/pages/tasks/[id].vue` 的 `submitActionOnce()` 僅對 `APPROVE`／`RESUBMIT` 產生 `editedFormData`。資料庫目前 `fm_process_instance`、`fm_task_action` 皆為 0 筆，尚無現成情境可驗證結果。

### 2026-10-08 BUG-150 修復複核
- **BUG-150 已修正**：確認原 `action()` 只要收到 `formData` 就更新表單，`REJECT`／`RETURN` 可夾帶內容改寫並增加 revision，並非誤判。`validatePolicy()` 現在於任何 Hook 或表單寫入前拒絕這兩種動作夾帶 `formData`。
- `FmTaskRuntimeCompletionTest` 驗證兩種動作均拒絕夾帶資料，目標測試通過。尚未執行真實待辦 HTTP／Flowable 情境。
