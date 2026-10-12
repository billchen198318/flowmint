## 08 部門職務（`FM_PROG002D0006`）— 靜態／唯讀盤查完成

2026-10-02 接續核對 README／組織規格、`FMPROG002D0006Controller` 全端點、`FmOrgDutyLogicServiceImpl`、兩張 Mapper、職務清單／編輯 Vue、`FmAssignmentResolverService.orgDuty()`，及 MariaDB `fm_org_duty`、`fm_employee_duty` DDL。現有職務與擔任人均為 0 筆，以下新增情境全部依程式推論，未異動資料。

**完成範圍與限制：**職務與擔任人的建立、更新、停用、候選來源及 Resolver 取用已靜態核對；兩張資料表的唯一鍵與日期 CHECK 已唯讀確認。因現有資料為空，無法用既有資料驗證職務搬移、主要擔任人或有效期間問題。

### BUG-009：職務擔任人可綁定未生效或已到期的部門任職

- 嚴重度：中。性質：Java 參照驗證與 Vue 候選清單。
- 預期：擔任部門職務的人員應有該部門有效任職。
- 實際：`FmOrgDutyLogicServiceImpl.requiredAssignment()` 只檢查任職 `STATUS='ACTIVE'`、Tenant 和部門，沒有檢查任職的生效與失效時間；`assignmentOptions()` 使用的候選資料也需依有效期間過濾。即使任職已到期，只要狀態仍是 `ACTIVE`，就能保存職務擔任人。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmOrgDutyLogicServiceImpl.java` 的 `requiredAssignment()`。MariaDB 唯讀查詢顯示目前 `fm_org_duty` 和 `fm_employee_duty` 均為 0 筆，無既有資料可重現；未修改資料。

### BUG-071：編輯職務可換部門卻未處理既有擔任人

- 嚴重度：高。性質：Java 參照一致性與 Resolver。
- 預期：職務更換部門時，既有擔任人的部門任職應重新驗證，或禁止直接更換職務所屬部門。
- 實際：`update()` 對請求新部門只驗證該部門存在，`applyDuty()` 隨後直接更新 `ORG_UNIT_ID`，沒有檢查既有 `fm_employee_duty`。Vue 編輯頁也允許更換部門。原擔任人仍指向舊部門任職，`ORG_DUTY` Resolver 在新部門查到職務後，以任職部門比對新部門而略過原擔任人，可能回傳無可用擔任者。
- 證據：`FmOrgDutyLogicServiceImpl.update()`／`applyDuty()`／`saveAssignee()`；`OrgDutyForm.vue` 的部門欄；`FmAssignmentResolverService.orgDuty()`。
- 驗證：目前職務和擔任人都為 0 筆，未建立資料重現。

### BUG-072：停用未來生效職務或擔任人會違反日期約束

- 嚴重度：中。性質：Java 停用時間與 MariaDB CHECK。
- 預期：未來生效的職務與擔任人可在生效前撤銷，停用應成功或回傳明確限制。
- 實際：`deactivate()` 和 `deactivateAssignee()` 在截止時間為空或晚於現在時設 `EFFECTIVE_TO=now`；若原 `EFFECTIVE_FROM` 在未來，便違反 `CK_FM_DUTY_DATE` 或 `CK_FM_ED_DATE`，交易回滾。
- 證據：`FmOrgDutyLogicServiceImpl` 的兩個停用方法；MariaDB `SHOW CREATE TABLE fm_org_duty`／`fm_employee_duty`。
- 驗證：兩表目前皆空，未執行停用測試。

### BUG-073：職務及擔任人的失效時間無法由編輯畫面清除

- 嚴重度：中。性質：Vue 編輯與 MyBatis NULL 欄位寫入。
- 預期：清空編輯畫面的失效時間後，對應 `EFFECTIVE_TO` 應更新為 `NULL`。
- 實際：`OrgDutyForm.vue` 的 `save()`／`saveAssignee()` 會把空值送成 `null`，Logic 也把 Entity 欄位設成 `null`；`FmOrgDutyMapper.xml`／`FmEmployeeDutyMapper.xml` 的 `update` 只在 `effectiveTo != null` 時更新，舊截止時間保留。
- 證據：`OrgDutyForm.vue`、`FmOrgDutyLogicServiceImpl.applyDuty()`／`saveAssignee()`、兩張 Mapper 的 `update`。
- 驗證：DDL 允許兩表 `EFFECTIVE_TO` 為 `NULL`，現有資料為空，未送出更新。

### BUG-074：部門職務管理端未檢查操作人 Tenant membership

- 嚴重度：高。性質：Java 權限邊界。
- 預期：職務及擔任人的查詢、建立、更新、停用與候選資料應驗證操作人的 Tenant 權限。
- 實際：Controller 清單可不帶 Tenant，其他端點以請求 Tenant 或 OID 存取；Logic 驗證部門與任職參照但沒有 `FmTenantAccessGuard` 或等價 membership 檢查。`update()` 核對資料 Tenant 與請求 Tenant 相等，仍未核對操作人。
- 證據：`FMPROG002D0006Controller`、`FmOrgDutyLogicServiceImpl`；同型權限邊界見 BUG-005。
- 驗證：資料庫只有單一 Tenant，沒有跨 Tenant 重現資料。

### BUG-075：ORG_DUTY 解析未優先選主要擔任人

- 嚴重度：中。性質：Resolver 候選排序。
- 預期：職務設定主要擔任人後，當簽核規則只允許一位候選人時，應先選該主要擔任人。
- 實際：`saveAssignee()` 保存 `IS_PRIMARY`，但 `FmAssignmentResolverService.orgDuty()` 以沒有指定排序的 `selectListByParams(memberParameters)` 取得全部啟用擔任人，按回傳順序填候選，到 `maxResults` 即停止，未使用 `IS_PRIMARY`。多位擔任人且 `maxResults=1` 時，結果可能不是主要擔任人。
- 證據：`FmOrgDutyLogicServiceImpl.saveAssignee()`；`FmAssignmentResolverService.orgDuty()`；README「部門與組織樹」操作步驟說明可指定主要擔任人。
- 驗證：現有擔任人 0 筆，未做 Resolver Preview 重現。

### 2026-10-07 修復複核

- **BUG-009 — 已修正（程式確認）：**儲存擔任人時只接受目前有效的部門任職；候選清單也排除未生效及已到期任職。原始問題由缺少日期過濾的程式路徑確認，尚未以資料庫異動重現。
- **BUG-071 — 已修正（程式確認）：**職務已有任何擔任人紀錄時禁止更換部門，避免停用紀錄日後再啟用而指向錯誤部門；需另建職務。原始問題由更新路徑未核對既有擔任人確認。
- **BUG-072 — 已修正（程式確認）：**停用未來生效的職務或擔任人只更新狀態，保留合法日期區間；已生效資料才把截止時間設為現在。原始問題由停用程式與日期約束確認。
- **BUG-073 — 已修正（程式確認）：**兩張 Mapper 的更新 SQL 固定寫入 `EFFECTIVE_TO`，可將編輯頁送出的 `null` 清除舊截止時間。原始問題由動態 SQL 條件確認。
- **BUG-074 — 已修正（程式確認）：**清單查詢要求指定且可讀的 Tenant；Logic 的單筆讀寫及候選查詢加入 Tenant 存取檢查，Tenant 選項只回傳可讀範圍。原始問題由 Controller 與 Logic 缺少 membership 檢查確認。
- **BUG-075 — 已修正（程式確認）：**ORG_DUTY 候選依 `IS_PRIMARY DESC,EFFECTIVE_FROM,EMPLOYEE_DUTY_ID` 排序，再套用 `maxResults`。原始問題由無排序查詢確認。
- 後端 `mvn -q -pl app -am -DskipTests compile` 通過。現有資料缺少職務與擔任人，並發與實際 Resolver Preview 仍待隔離資料驗證。
