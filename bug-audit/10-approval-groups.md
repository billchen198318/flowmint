## 10 簽核群組（`FM_PROG003D0001`）— 靜態／唯讀盤查完成

2026-10-02 接續核對 README／組織規格、`FMPROG003D0001Controller` 全端點、`FmApprovalGroupLogicServiceImpl`、兩張 Mapper、群組清單／編輯 Vue、`FmAssignmentResolverService.approvalGroup()` 與流程發布時的 `FmApprovalGroupModeValidator`。MariaDB 唯讀確認群組 12 筆、成員 30 筆，均為 `ACTIVE`；沒有未來生效或非空截止時間的成員。現有啟用 `APPROVAL_GROUP` 規則 29 筆，但未變更群組或流程資料。

**完成範圍與限制：**群組與成員建立、編輯、停用、期間與去重、派送方式發布驗證及 Runtime 取用已靜態核對；兩張資料表唯一鍵與 CHECK 已唯讀確認。現有資料缺少停用、未來生效與多段期間案例，無寫入或瀏覽器重現。

### BUG-010：同一員工無法設定不重疊的多段群組成員期間

- 嚴重度：中。性質：Java 期間驗證。
- 預期：群組成員可設定生效起迄日；同一員工的兩段不重疊有效期間應可分別保存，用於預先安排輪值或恢復加入。
- 實際：`FmApprovalGroupLogicServiceImpl.validateDuplicateMember()` 只要同一群組、同一員工存在任一 `STATUS='ACTIVE'` 記錄，就拒絕另一筆 `ACTIVE` 記錄；沒有比較兩段 `EFFECTIVE_FROM`、`EFFECTIVE_TO` 是否重疊。例如原期間為 1 月、第二段為 3 月，仍會被拒絕。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmApprovalGroupLogicServiceImpl.java` 的 `validateDuplicateMember()`。MariaDB `SHOW INDEX FROM fm_approval_group_member` 顯示唯一鍵包含 `EFFECTIVE_FROM`，資料結構允許不同起始日；目前有群組 12 筆、成員 30 筆，未異動資料做重現。

### BUG-078：發布後修改群組派送方式不會重新檢查既有流程政策

- 嚴重度：高。性質：群組設定與流程政策一致性。
- 預期：已發布流程引用群組時，變更群組 `ASSIGNMENT_MODE` 應避免讓後續工作與已發布 Task Policy 的派送方式不一致，或要求重新發布／明確處理版本。
- 實際：流程發布時 `FmApprovalGroupModeValidator` 比對群組方式與 Task Policy；但 `FmApprovalGroupLogicServiceImpl.update()` 直接寫入新的 `assignmentMode`，沒有查已發布流程引用。`FmAssignmentResolverService.approvalGroup()` 只讀群組狀態及成員，Runtime 的派送方式取 Task Policy，並不再比對群組方式，因此修改群組後新建立的工作仍按舊政策派送，與群組目前設定可能相反。
- 證據：`FmProcessDefLogicServiceImpl.validateApprovalGroupModesForPublish()`、`FmApprovalGroupModeValidator`、`FmApprovalGroupLogicServiceImpl.update()`、`FmAssignmentResolverService.approvalGroup()`、`FmTaskAssignmentListener` 的政策派送分支。
- 驗證：現有 29 筆啟用群組型 Assignment Rule；未修改群組派送方式重現。

### BUG-079：群組成員候選與儲存忽略員工有效期間

- 嚴重度：中。性質：Java 參照驗證。
- 預期：新增或更新群組成員時，員工應在其有效期間內，不能只依 `STATUS` 判定可用。
- 實際：`employeeMap()` 只按 Tenant 與 `STATUS='ACTIVE'` 查員工；`employeeOptions()` 及 `requiredEmployee()` 直接使用該清單，沒有檢查員工 `EFFECTIVE_FROM`／`EFFECTIVE_TO`。尚未生效或已到期但狀態仍啟用的員工可被保存為成員，直到 Runtime 解析時才可能失敗。
- 證據：`FmApprovalGroupLogicServiceImpl.employeeMap()`／`employeeOptions()`／`requiredEmployee()`；`FmAssignmentResolverService.approvalGroup()` 呼叫有效員工候選解析。
- 驗證：目前沒有未來或到期但仍啟用的員工，未做異動重現。

### BUG-080：停用未來生效成員會違反日期約束

- 嚴重度：中。性質：Java 停用時間與 MariaDB CHECK。
- 預期：管理員可在成員生效前撤銷配置，停用應成功或給出明確限制。
- 實際：`deactivateMember()` 對截止時間為空或在現在之後的成員設 `EFFECTIVE_TO=now`。若原 `EFFECTIVE_FROM` 在未來，違反 `CK_FM_AGM_DATE`，交易回滾。
- 證據：`FmApprovalGroupLogicServiceImpl.deactivateMember()`；MariaDB `SHOW CREATE TABLE fm_approval_group_member`。
- 驗證：現有 30 筆成員皆已生效，未測停用未來成員。

### BUG-081：群組成員失效時間無法由編輯畫面清除

- 嚴重度：中。性質：Vue 編輯與 MyBatis NULL 欄位寫入。
- 預期：清空成員編輯畫面的失效時間後，`fm_approval_group_member.EFFECTIVE_TO` 應為 `NULL`。
- 實際：`ApprovalGroupForm.vue` 把空欄送成 `effectiveTo: null`，`saveMember()` 也設 Entity 欄位為 `null`；`FmApprovalGroupMemberMapper.xml` 的 `update` 只在 `effectiveTo != null` 時寫入，舊截止時間仍保留。
- 證據：`ApprovalGroupForm.vue` 的 `saveMember()`、`FmApprovalGroupLogicServiceImpl.saveMember()`、`FmApprovalGroupMemberMapper.xml` 的 `update`。
- 驗證：DDL 允許截止時間為 `NULL`；現有成員均無非空截止時間，尚無現成資料可重現清空操作。

### BUG-082：簽核群組管理端未檢查操作人 Tenant membership

- 嚴重度：高。性質：Java 權限邊界。
- 預期：群組及成員的查詢、建立、更新、停用和員工候選應驗證操作人的 Tenant 存取權。
- 實際：Controller 清單可不帶 Tenant 篩選，其他端點按請求 Tenant 或 OID 操作；Logic 驗證目標員工與群組屬於同 Tenant，但沒有 `FmTenantAccessGuard` 或等價的操作人 membership 檢查。`tenantOptions()` 也列出所有啟用 Tenant。
- 證據：`FMPROG003D0001Controller`、`FmApprovalGroupLogicServiceImpl`；同型權限邊界見 BUG-005。
- 驗證：資料庫只有一個 Tenant，無跨 Tenant 操作資料可重現。

