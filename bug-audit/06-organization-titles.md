## 06 組織職稱（`FM_PROG002D0004`）— 3 項已確認並修正

2026-10-02 接續核對 README／組織規格、`FMPROG002D0004Controller` 全端點、`FmOrgTitleLogicServiceImpl`、`FmOrgTitleMapper.xml`、職稱清單與編輯 Vue、職稱／層級參照，以及 MariaDB `fm_org_title` DDL 和資料。現有職稱 10 筆，均啟用且有非空截止時間；無孤兒簽核層級參照。現有單一 Tenant，未進行異動或跨 Tenant HTTP 重現。

### BUG-007：職稱可綁定未生效或已到期的簽核層級

**狀態：已修正（2026-10-06）。** 修改前確認職稱參照與候選清單都只檢查層級 `ACTIVE`，不查層級／方案有效期間。現建立與更新職稱時檢查層級及其啟用方案涵蓋整段職稱有效期間，候選清單僅列目前有效的層級。回歸測試排除未來、已到期及停用方案的層級。

- 嚴重度：中。性質：Java 參照驗證與 Vue 選項。
- 預期：職稱的簽核層級應屬於同 Tenant 且在職稱生效期間可用；不可將無效層級當作可用候選。
- 實際：`FmOrgTitleLogicServiceImpl.requireReferences()` 只查目標 Tenant 且 `STATUS='ACTIVE'` 的層級，未檢查層級的 `EFFECTIVE_FROM`、`EFFECTIVE_TO` 或所屬方案狀態。`FMPROG002D0004Controller.levelOptions()` 同樣只按狀態列出選項。使用者可建立或更新職稱指向尚未生效或已到期的層級。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmOrgTitleLogicServiceImpl.java`、`backend/app/src/main/java/org/qifu/fm/controller/FMPROG002D0004Controller.java`；Vue `frontend-v-nx/pages/fm_prog002d0004/components/OrgTitleForm.vue` 載入層級選項。MariaDB 唯讀查詢顯示目前 10 筆職稱中沒有指向停用或過期層級的資料。

### BUG-065：組織職稱管理端未檢查操作人 Tenant membership

**狀態：已修正（2026-10-06）。** 修改前確認清單可不帶 Tenant 查詢，依 OID 讀取／停用與建立／更新都不驗 membership。現 Logic 各入口及層級選項驗權，Tenant 選項只列可讀 Tenant，職稱清單在資料庫分頁前按可讀 Tenant 限縮。回歸測試覆蓋跨 Tenant 讀取拒絕及空可讀清單。

- 嚴重度：高。性質：Java 權限邊界。
- 預期：職稱清單、載入、建立、更新、停用及層級選項應按操作人的 Tenant 權限限制。
- 實際：Controller 的 `/findPage` 可不帶 Tenant 篩選，`/load`／`/deactivate` 依 OID 處理；Logic 的 `create()`／`update()` 只驗證目標 Tenant 的層級為 `ACTIVE`，未驗證操作者的 membership。`/tenant-options` 直接列出所有啟用 Tenant。`update()` 有核對請求 Tenant 與既有職稱 Tenant 相等，但未核對操作者授權。
- 證據：`FMPROG002D0004Controller`、`FmOrgTitleLogicServiceImpl`；與 BUG-005、BUG-062 同型，影響職稱資料。
- 驗證：目前只有一個 Tenant，沒有跨 Tenant 實際操作資料。

### BUG-066：職稱失效時間無法由編輯畫面清除

**狀態：已修正（2026-10-06）。** 修改前確認前端與 Logic 都會送出／設定 `null`，Mapper 卻略過該欄。現更新 SQL 明確寫入 `EFFECTIVE_TO = NULL`；Mapper SQL 測試通過。

- 嚴重度：中。性質：Vue 編輯與 MyBatis NULL 欄位寫入。
- 預期：清空職稱編輯畫面的失效時間後儲存，`fm_org_title.EFFECTIVE_TO` 應更新為 `NULL`。
- 實際：`OrgTitleForm.vue` 把空值轉為 `effectiveTo: null`；`FmOrgTitleLogicServiceImpl.apply()` 也將 Entity 欄位設為 `null`。`FmOrgTitleMapper.xml` 的 `update` 僅在 `effectiveTo != null` 時寫入，舊截止時間留在資料庫並在重載後再次顯示。
- 證據：`frontend-v-nx/pages/fm_prog002d0004/components/OrgTitleForm.vue` 的 `save()`；`FmOrgTitleLogicServiceImpl.apply()`；`FmOrgTitleMapper.xml` 的 `update`。
- 驗證：MariaDB DDL 允許 `EFFECTIVE_TO` 為 `NULL`，現有 10 筆職稱都有截止時間；未送出更新請求。
