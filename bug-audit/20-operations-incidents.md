## 20 異常處理與營運流程管理 — 靜態／唯讀盤查完成

2026-10-08 修復複核：BUG-020 確認為真。`FLOWMINT_OPERATIONS`／`FLOWMINT_REASSIGN` 只是全域程式角色，原營運入口沒有檢查操作者對 Header Tenant 的有效 membership。已在 `FmIncidentOperationsLogicServiceImpl` 的所有營運及改派入口共用權限檢查加入非系統管理員的 Tenant membership 驗證；系統管理員權限不變。BUG-020 已修正（靜態與定向測試驗證；現有 Incident 0 筆，未以真實資料跨 Tenant 操作）。

2026-10-05 收尾：已對照營運權限規格、`FmIncidentOperationsController` 的 Incident 查詢／重試／重派、流程查詢／終止、Task 重派及平行加簽重派入口，`FmIncidentOperationsLogicServiceImpl` 與營運 Vue；報表入口歸 22。非管理員 Tenant membership 邊界見 BUG-020。既有 Incident 與流程實例沒有可用資料，未做跨 Tenant 操作或故障注入重現。

### BUG-020：非系統管理員的營運角色未受 Tenant membership 限制

- 嚴重度：高。性質：Java 跨 Tenant 權限邊界；目前 Incident 0 筆，無實際資料可重現。
- 預期：全域 `FLOWMINT_OPERATIONS` 角色表示可使用營運程式，非系統管理員仍應只存取有權限的 Tenant；`backend/doc/17-QIFU4帳號與Role邊界.md` 說明全域 Program Role 與 Tenant 資料授權分離。
- 實際：`FmIncidentOperationsLogicServiceImpl.incidents()`、`reassign()` 等入口只呼叫 `requireOperator()`；該方法只檢查 `UserUtils.isAdmin()` 或全域 `FLOWMINT_OPERATIONS`，不檢查操作人的目標 Tenant membership。`requireReassignOperator()` 對全域 `FLOWMINT_REASSIGN` 亦同。目標員工的 membership 有查，但操作人的沒有。持有營運角色的非管理員可把 Header 的 Tenant ID 指向其他 Tenant 查詢 Incident 或操作流程。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmIncidentOperationsLogicServiceImpl.java` 的 `requireOperator()`、`requireReassignOperator()` 及呼叫端；MariaDB 唯讀查詢顯示目前 `fm_assignment_incident` 為 0 筆，未進行跨 Tenant 實際驗證。
