## 07 部門主管（`FM_PROG002D0005`）— 5 項已確認並修正

2026-10-02 接續核對 README、`backend/doc/03-組織與簽核人規格.md` 與 `37-部門人員與主管配置操作說明.md`，以及 `FMPROG002D0005Controller` 全端點、`FmOrgUnitHeadLogicServiceImpl`、`FmOrgUnitHeadMapper.xml`、Resolver、主管清單／編輯 Vue 和 MariaDB DDL。現有主管配置 38 筆（正式 35、副主管 3、代理 0），未來生效 0、已有截止時間 37；沒有無對應啟用任職的主管，也沒有未來或已到期的啟用任職／員工。以下新情境未做寫入重現。

**完成範圍與限制：**主管類型、單一正式主管期間重疊檢查、候選來源、停用及 Resolver 取用路徑均已靜態核對；資料表唯一鍵與日期 CHECK 已唯讀確認。資料庫只有單一 Tenant、沒有未來生效主管或過期任職，跨 Tenant 與日期錯誤仍須隔離資料實測。

### BUG-008：主管 Resolver 可能把副主管或代理主管當作正式主管

**狀態：已修正（2026-10-06）。** 修改前確認 Resolver 只依優先序與有效期間取第一筆，未限制 `HEAD_TYPE`；操作文件亦明載此問題。現查詢與最終選取都只接受啟用且有效的正式 `HEAD`；回歸測試確認副主管、代理主管排序較前時仍選正式主管。

- 嚴重度：高。性質：Java Resolver。此缺陷也已由 `backend/doc/37-部門人員與主管配置操作說明.md` 第 5 節記載。
- 預期：`INITIATOR_ORG_HEAD` 等要求「本部門主管」的規則，應只選 `HEAD_TYPE='HEAD'` 的有效主管。
- 實際：`FmAssignmentResolverService.firstOrgUnitHead()` 查詢 `ACTIVE` 主管配置後，直接依 `PRIORITY` 取第一筆有效期間記錄，未篩選 `HEAD_TYPE`。若 `DEPUTY_HEAD` 或 `ACTING_HEAD` 的優先序較前，將解析錯誤簽核人。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/resolver/FmAssignmentResolverService.java` 的 `firstOrgUnitHead()`；MariaDB 唯讀查詢顯示 `fm_org_unit_head` 有 `HEAD` 35 筆、副主管 3 筆，目前沒有非正式主管排在同部門第一位的資料，故未用現況重現。

### BUG-067：主管候選與儲存驗證忽略任職及員工有效期間

**狀態：已修正（2026-10-06）。** 修改前確認候選與驗證只查 `ACTIVE`，沒有比對任職和員工日期。現候選只列目前有效者；儲存時要求員工與該部門任職涵蓋主管配置期間。測試覆蓋未來／到期任職排除，以及任職早於主管配置結束時拒絕建立。

- 嚴重度：高。性質：Java 參照驗證與 Vue 候選資料。
- 預期：主管員工在配置期間應有該部門有效任職，員工本身也應在有效期間內。
- 實際：`employeeOptions()` 按 Tenant、部門和 `STATUS='ACTIVE'` 查任職，`employeeMap()` 也只按 Tenant 與 `ACTIVE` 查員工，沒有比對各自的 `EFFECTIVE_FROM`／`EFFECTIVE_TO`；`validate()` 以同一候選清單判斷可否儲存。尚未生效或已到期但狀態仍為 `ACTIVE` 的任職／員工仍可被選作主管。操作文件第 5 節已明載任職期間限制。
- 證據：`FmOrgUnitHeadLogicServiceImpl.employeeOptions()`／`employeeMap()`／`validate()`；`OrgUnitHeadForm.vue` 的員工選單；`backend/doc/37-部門人員與主管配置操作說明.md` 第 5 節。
- 驗證：現有資料沒有未來或已到期的啟用任職／員工，未做異動重現。

### BUG-068：停用未來生效主管配置會違反日期約束

**狀態：已修正（2026-10-06）。** 修改前確認未來起日仍被設成現在截止，違反日期約束。現未來生效配置停用時保留原有效期間，僅改狀態；回歸測試檢查更新前的日期與狀態。

- 嚴重度：中。性質：Java 停用時間與 MariaDB CHECK。
- 預期：未來生效主管配置可在生效前撤銷，停用操作應成功或給出明確限制。
- 實際：`deactivate()` 把配置狀態設為 `INACTIVE`，若截止時間為空或在現在之後，便設 `EFFECTIVE_TO=now`。當原 `EFFECTIVE_FROM` 在未來時，截止早於起日，違反 `CK_FM_OUH_DATE` 而交易回滾。
- 證據：`FmOrgUnitHeadLogicServiceImpl.deactivate()`；MariaDB `SHOW CREATE TABLE fm_org_unit_head`。
- 驗證：現有 38 筆配置沒有未來生效資料，未操作重現。

### BUG-069：主管配置失效時間無法由編輯畫面清除

**狀態：已修正（2026-10-06）。** 修改前確認前端／Logic 會設 `null`，Mapper 更新卻略過。現更新 SQL 明確寫入 `EFFECTIVE_TO = NULL`；Mapper 動態 SQL 測試已通過。

- 嚴重度：中。性質：Vue 編輯與 MyBatis NULL 欄位寫入。
- 預期：清空編輯畫面的失效時間後，`fm_org_unit_head.EFFECTIVE_TO` 應變為 `NULL`。
- 實際：`OrgUnitHeadForm.vue` 將空欄轉成 `effectiveTo: null`，Logic `apply()` 也設為 `null`；`FmOrgUnitHeadMapper.xml` 的 `update` 只在 `effectiveTo != null` 時更新，資料庫保留舊截止時間。
- 證據：`OrgUnitHeadForm.vue` 的 `save()`；`FmOrgUnitHeadLogicServiceImpl.apply()`；`FmOrgUnitHeadMapper.xml` 的 `update`。
- 驗證：DDL 允許 `EFFECTIVE_TO` 為 `NULL`；目前 37 筆主管配置有截止時間，未執行更新。

### BUG-070：部門主管管理端未檢查操作人 Tenant membership

**狀態：已修正（2026-10-06）。** 修改前確認清單可跨 Tenant 查詢，依 OID 操作與候選端點也沒有 membership 驗證。現 Logic 入口與候選查詢要求 Tenant 存取權，Tenant 選項只列可讀者，清單在資料庫分頁前依可讀 Tenant 篩選。測試覆蓋跨 Tenant 讀取拒絕及空可讀清單 SQL。

- 嚴重度：高。性質：Java 權限邊界。
- 預期：主管配置查詢、候選選取、建立、更新與停用均應驗證操作人可存取目標 Tenant。
- 實際：Controller 的 `/findPage` 可查所有 Tenant，選項端點以請求 Tenant 查資料；Logic 只驗證部門與員工參照，`load()`／`deactivate()` 依 OID 操作，沒有 `FmTenantAccessGuard` 或等價 membership 檢查。`update()` 僅確認請求 Tenant 與原資料相同。
- 證據：`FMPROG002D0005Controller`、`FmOrgUnitHeadLogicServiceImpl`；同型權限邊界見 BUG-005。
- 驗證：現有資料僅單一 Tenant，無跨 Tenant 操作資料可重現。
