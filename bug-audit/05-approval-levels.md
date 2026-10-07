## 05 組織簽核層級（`FM_PROG002D0003`）— 6 項已確認並修正

2026-10-02 接續核對 `FMPROG002D0003Controller`、`FmOrgLevelSchemeLogicServiceImpl`、兩張 Mapper、`SchemeEditor.vue`、清單 Vue、職稱選項與 Resolver 的層級引用，以及 MariaDB DDL。現有方案 1 筆、層級 8 筆，全為啟用且層級均已生效；方案與 8 筆層級均有非空截止時間，10 筆職稱引用層級。新增情境未異動資料重現。

**完成範圍與限制：**方案及層級的建立、更新、移除／停用、清單、候選選取、Resolver 消費與兩張資料表約束均已靜態或唯讀核對。現有資料只有一個方案且無停用或未來生效層級，沒有執行寫入或並行測試；預設方案欄位目前只作維護與顯示，未找到 Runtime 按 `IS_DEFAULT` 自動選方案的路徑，故未把多預設方案視為已證實 Runtime 故障。

### BUG-006：層級順序互換時可能觸發資料庫唯一鍵錯誤

**狀態：已修正（2026-10-06）。** 修改前確認逐筆更新會先撞上另一筆尚未調整的唯一排序。現先將待調整層級移到同方案內未使用的暫存排序及代碼，再寫入最終值；排序互換回歸測試確認兩筆都先暫存再更新。

- 嚴重度：中。性質：Java 更新順序與 MariaDB 唯一鍵。
- 預期：編輯頁允許調整既有層級的 `LEVEL_ORDER`；兩筆層級互換順序後應可儲存。
- 實際：`FmOrgLevelSchemeLogicServiceImpl.update()` 逐筆更新層級。MariaDB 的 `UK_FM_LEVEL_ORDER` 對 `TENANT_ID, LEVEL_SCHEME_ID, LEVEL_ORDER` 設唯一限制。假設 A 為 10、B 為 20，送出 A=20、B=10，第一筆更新即與尚未更新的另一筆衝突；交易回滾。Vue `SchemeEditor.vue` 將整份層級清單送出，沒有提供暫存排序策略。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmOrgLevelSchemeLogicServiceImpl.java` 的 `update()`；MariaDB `SHOW INDEX FROM fm_org_approval_level` 確認 `UK_FM_LEVEL_ORDER`。未異動資料做重現。

### BUG-060：移除層級後原代碼與排序仍佔用唯一鍵

**狀態：已修正（2026-10-06，採明確告知策略）。** 修改前確認軟停用仍佔用唯一鍵。現於寫入前核對請求與未保留層級的代碼及排序，若重用則回傳「已移除層級的代碼與排序仍保留，不可重用」，避免只收到資料庫唯一鍵錯誤。軟停用與歷史 ID 保留。

- 嚴重度：中。性質：Java 軟停用與 MariaDB 唯一鍵。
- 預期：編輯畫面移除層級後，應能用原代碼或原排序建立替代層級，或清楚告知這些值不可重用。
- 實際：`update()` 對不在請求清單的舊層級只設 `STATUS='INACTIVE'` 與截止時間，不刪除資料；`UK_FM_LEVEL_CODE` 和 `UK_FM_LEVEL_ORDER` 未排除停用資料。即使之後另一次儲存，新增層級使用原代碼或原排序仍觸發唯一鍵；若同一次請求先移除舊層級並新增替代層級，程式更會在處理移除前先插入而衝突。
- 證據：`FmOrgLevelSchemeLogicServiceImpl.update()` 的新增／移除順序；`SchemeEditor.vue` 的 `remove()`／`add()`；MariaDB `SHOW CREATE TABLE fm_org_approval_level` 的兩個唯一鍵。
- 驗證：現有 8 筆層級均啟用；未送出移除或新增請求。

### BUG-061：移除未來生效層級會違反有效期間約束

**狀態：已修正（2026-10-06）。** 修改前確認移除未來生效層級會把截止時間設為起日前的現在。現保留其原有效期間並設狀態為停用；回歸測試覆蓋未來起日且截止時間為空的情境。

- 嚴重度：中。性質：Java 停用時間與 MariaDB CHECK。
- 預期：管理員可在生效前撤銷尚未啟用的層級，儲存應成功或給出明確限制。
- 實際：`update()` 對被移除層級設 `STATUS='INACTIVE'`，且在原截止時間為空或晚於現在時，把 `EFFECTIVE_TO` 設為現在。若該層級的 `EFFECTIVE_FROM` 在未來，截止時間早於起日，違反 `CK_FM_LEVEL_DATE`，整筆更新交易回滾。
- 證據：`FmOrgLevelSchemeLogicServiceImpl.update()` 的未列入 `seen` 分支；MariaDB `SHOW CREATE TABLE fm_org_approval_level` 的 `CK_FM_LEVEL_DATE`。
- 驗證：目前沒有未來生效層級，尚未用 HTTP 操作重現。

### BUG-062：組織簽核層級管理端未檢查操作人 Tenant membership

**狀態：已修正（2026-10-06）。** 修改前確認清單、依 OID 讀取／修改、建立及 Tenant 選項缺少 membership 篩選。現清單由可讀 Tenant ID 在資料庫分頁前限縮，指定 Tenant 先驗權；Logic 各操作與 Tenant 選項也驗權。回歸測試覆蓋跨 Tenant 讀取被拒及空可讀清單。

- 嚴重度：高。性質：Java 權限邊界。
- 預期：方案與層級的查詢、建立、更新、停用除程式權限外，應驗證操作人可存取目標 Tenant。
- 實際：Controller 的查詢可按任意 `tenantId` 篩選或不篩選；`load()`／`update()`／`deactivate()` 用 OID 讀取和修改，`create()` 只確認目標 Tenant 啟用。Logic 與 Controller 沒有 `FmTenantAccessGuard` 或等價 membership 檢查，Tenant 下拉選項也列出所有啟用 Tenant。
- 證據：`FMPROG002D0003Controller` 全端點；`FmOrgLevelSchemeLogicServiceImpl` 的 `requireTenant()`／`load()`／`update()`／`deactivate()`；`SchemeEditor.vue` 的 Tenant 選單。與 BUG-005 同型，但影響簽核層級方案和明細。
- 驗證：資料庫目前只有一個 Tenant，沒有跨 Tenant 操作資料可重現。

### BUG-063：停用層級方案不會阻止其層級繼續用於簽核解析

**狀態：已修正（2026-10-06）。** 修改前確認停用方案不變更層級狀態，Resolver 與候選選項仍以層級啟用狀態判斷。現停用方案或將方案編輯為停用時，同交易停用其明細層級；回歸測試覆蓋停用連動。

- 嚴重度：高。性質：方案狀態與 Resolver 引用不一致。
- 預期：方案停用後，其所屬層級不應再作為職稱或新簽核解析的可用層級。
- 實際：`deactivate()` 只把 `fm_org_level_scheme.STATUS` 設為 `INACTIVE`，不改明細層級狀態。`FmAssignmentResolverService.approvalLevelById()` 只查層級的 Tenant、`ACTIVE` 狀態與有效期間，不查所屬方案；職稱候選與流程設計層級選項同樣只查層級。停用方案後，原層級仍可用於解析與新配置。
- 證據：`FmOrgLevelSchemeLogicServiceImpl.deactivate()`；`FmAssignmentResolverService.approvalLevelById()`；`FMPROG002D0004Controller.levelOptions()`；`FmProcessDefLogicServiceImpl.approvalLevelOptions()`。
- 驗證：現有唯一方案仍啟用，未停用測試資料；此為程式路徑推論。

### BUG-064：編輯方案或層級時無法清除失效時間

**狀態：已修正（2026-10-06）。** 修改前確認 Logic 可設 `null`，但兩張 Mapper 更新略過空值。現更新 SQL 明確寫入 `EFFECTIVE_TO = NULL`；Mapper 動態 SQL 回歸測試已涵蓋。

- 嚴重度：中。性質：Vue 編輯與 MyBatis NULL 欄位寫入。
- 預期：將方案或層級的失效時間清空後儲存，對應 `EFFECTIVE_TO` 應變為 `NULL`。
- 實際：`SchemeEditor.vue` 將空欄位轉成 `effectiveTo: null`，Logic 的 `apply()` 也把 Entity 設成 `null`；但 `FmOrgLevelSchemeMapper.xml` 與 `FmOrgApprovalLevelMapper.xml` 的 `update` 都只在 `effectiveTo != null` 時更新欄位。既有截止時間會留在資料庫，重新載入仍顯示舊值。
- 證據：`SchemeEditor.vue` 的 `dates()`／`save()`；`FmOrgLevelSchemeLogicServiceImpl.apply()`；兩張 Mapper 的 `update`。
- 驗證：MariaDB 唯讀查詢顯示現有 1 筆方案與 8 筆層級均有截止時間，欄位在 DDL 中可為 `NULL`；未送出更新請求。
