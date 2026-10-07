## 04 部門與組織樹（`FM_PROG002D0002`）— 6 項已確認並修正

2026-10-02 接續核對 README 與組織規格、`FMPROG002D0002Controller`、`FmOrgUnitLogicServiceImpl`、`FmOrgUnitVersionMapper.xml`、部門編輯與樹狀 Vue。MariaDB 唯讀確認版本表 35 筆均為 `ACTIVE`，未來生效與已到期均為 0，`CK_FM_OUV_DATE` 要求截止時間晚於起日；以下新增情境依程式推論，未異動資料重現。

**完成範圍與限制：**已核對部門 Controller 全端點、Logic 建立／編輯／停用／移動與版本處理、主檔和版本 Mapper、DTO、清單／編輯／組織樹 Vue，以及 README 與組織規格。MariaDB 唯讀確認 35 筆主檔、35 筆版本，全屬單一 Tenant 且版本號均為 1；缺失父節點 0、根節點 1、路徑尾段不符部門 ID 0。沒有歷史版本、停用節點、未來生效節點或併發修改資料，BUG-005 與 BUG-055～059 仍需隔離資料重現。

### BUG-005：組織樹操作未檢查操作人的 Tenant membership

**狀態：已修正（2026-10-06）。** 修改前確認所有入口僅檢查目標 Tenant 或直接依 ID 查詢，沒有操作人 membership 驗證。現於建立、依 OID 讀取／編輯／停用、樹查詢與移動預覽／執行前要求 Tenant 存取權；Tenant 選項只列可讀 Tenant。新增拒絕跨 Tenant 樹讀取測試。

- 嚴重度：高。性質：Java 權限邊界；跨 Tenant 實際重現受限於目前只有一個 Tenant。
- 預期：查詢、建立、更新、停用或移動組織樹時，除全域程式權限外，還須驗證操作人有目標 Tenant 的存取權。
- 實際：`FmOrgUnitLogicServiceImpl` 的 `create()` 僅以 `validateTenant()` 確認目標 Tenant 為 `ACTIVE`；`tree()` 依請求 Tenant ID 查資料；`load()`、`update()`、`deactivate()` 依 OID 操作；`previewMove()`、`move()` 依請求 Tenant ID 處理。這些方法未使用 `FmTenantAccessGuard` 或等價的操作人 membership 驗證。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmOrgUnitLogicServiceImpl.java`；此問題與 BUG-003 相同類型，但影響組織樹資料及歷史版本，因此在對應功能單元個別列出。
- 驗證：MariaDB 唯讀查詢顯示部門主檔 35 筆、版本 35 筆、孤兒版本 0 筆，資料都在單一 Tenant；無跨 Tenant 資料可實際重現授權問題。

### BUG-055：組織樹目前版本查詢忽略生效期間

**狀態：已修正（2026-10-06）。** 修改前確認 `selectCurrentTree` 無時間條件且目前樹、父節點和移動來源直接使用。現由組織樹 Logic 查詢傳入當下時間，以 `EFFECTIVE_FROM`／`EFFECTIVE_TO` 限定節點；編輯既有目前版本仍可讀取未篩時間的版本。Mapper 動態 SQL 測試覆蓋兩種查詢。

- 嚴重度：高。性質：Mapper 時間條件與組織樹業務規則。
- 預期：目前組織樹、可選父部門及移動來源只包含當下已生效且未失效的版本；未來生效或過期部門不可作為當前有效節點。
- 實際：`selectCurrentTree` 使用主檔 `CURRENT_VERSION_NO` 連結版本，`currentConditions` 只有 Tenant、代碼、名稱、狀態條件，沒有 `EFFECTIVE_FROM`／`EFFECTIVE_TO` 篩選。`create()`／`validateMove()`／`deactivate()` 與 `/tree` 都沿用此樹。未來生效節點會立即出現在樹上並可當父部門或移動；過期但 `ACTIVE` 的節點仍可被當成啟用父部門，也會阻止建立新根部門。
- 證據：`backend/app/src/main/resources/org/qifu/fm/mapper/FmOrgUnitVersionMapper.xml` 的 `currentSelect`／`currentConditions`／`selectCurrentTree`；`FmOrgUnitLogicServiceImpl.currentTree()`、`resolveParent()`、`hasRoot()`、`validateMove()`；README「部門與組織樹」規定有效期間及依 `effectiveAt` 查詢。
- 驗證：資料庫現有 35 筆版本沒有未來或已到期資料，故尚未用實際資料重現。

### BUG-056：編輯狀態可繞過父部門停用限制

**狀態：已修正（2026-10-06）。** 修改前確認 `update()` 沒有子部門檢查。編輯把啟用父部門設為停用時，現與 `deactivate()` 共用有效啟用子部門檢查。

- 嚴重度：高。性質：Java 更新驗證與 Vue 可用操作。
- 預期：有啟用子部門的父部門不可停用，無論使用「停用部門」按鈕或編輯表單狀態。
- 實際：`deactivate()` 會檢查 `hasActiveChild`，但 `update()` 直接由 `applyCommand()` 寫入請求的 `status`，沒有等價的子部門檢查。編輯表單 `OrgUnitForm.vue` 提供 `INACTIVE` 狀態選項，儲存會呼叫 `/update`，因此可把有啟用子部門的父節點停用，造成啟用子部門掛在停用父節點下。
- 證據：`FmOrgUnitLogicServiceImpl.update()`／`deactivate()`／`applyCommand()`；`frontend-v-nx/pages/fm_prog002d0002/components/OrgUnitForm.vue` 狀態欄；`OrgUnitEditor.vue` 的 `save()`。
- 驗證：資料庫現況 35 筆版本均啟用，沒有停用父部門可驗證；未送出修改請求。

### BUG-057：編輯截止時間可能通過表單檢查後遭資料庫拒絕

**狀態：已修正（2026-10-06）。** 修改前確認 `update()` 覆蓋新版本起日，且未重新驗證截止時間。現對實際 `nextVersionTime` 檢查截止時間，再進行版本寫入。

- 嚴重度：中。性質：Java 版本時間與資料庫 CHECK。
- 預期：編輯部門時，截止時間應與實際新版本起日驗證；若早於或等於新版本起日，應回傳明確欄位錯誤。
- 實際：`validateCommand()` 只比較請求中的 `effectiveTo` 與 `effectiveFrom`；`update()` 隨後用 `nextVersionTime(oldVersion)` 把新版本起日改成操作時間，覆蓋請求起日。使用者若保留既有較早起日並設定介於該起日與操作時間之間的截止時間，會先通過 Java 驗證，插入時違反 `CK_FM_OUV_DATE` 而回滾。
- 證據：`FmOrgUnitLogicServiceImpl.validateCommand()`／`update()`／`applyCommand()`；`OrgUnitEditor.vue` 的 `toPayload()`；MariaDB `SHOW CREATE TABLE fm_org_unit_version` 的 `CK_FM_OUV_DATE`。
- 驗證：現有 35 筆版本均未設定已到期時間；此為靜態推論，未送出編輯請求。

### BUG-058：移動啟用部門時漏改停用後代的路徑與深度

**狀態：已修正（2026-10-06）。** 修改前確認移動子樹取自僅啟用的樹。現來源與新父節點仍從有效啟用樹驗證，受影響子樹則取有效期間內的全部目前節點，包含停用後代。

- 嚴重度：高。性質：Java 子樹選取與歷史組織結構。
- 預期：移動父部門後，同一子樹內所有後代的 `PATH`／`TREE_DEPTH` 應與新父鏈一致；停用後代日後重新啟用也應回到正確位置。
- 實際：`validateMove()` 用 `currentTree(tenantId, false)` 建立 `subtree`，排除了 `INACTIVE` 後代。`move()` 只替這份清單逐筆建立新版本。若啟用來源底下有停用後代，父節點已移到新位置，停用後代仍保留舊路徑與深度；重新啟用後，畫面雖依 `PARENT_ORG_UNIT_ID` 顯示在父部門下，依 `PATH`／`TREE_DEPTH` 取子樹或解析主管仍可能不一致。
- 證據：`FmOrgUnitLogicServiceImpl.validateMove()`／`move()`／`currentTree()`；`FmOrgUnitVersionMapper.xml` 的狀態過濾；`frontend-v-nx/pages/fm_prog002d0002/tree.vue` 允許選擇是否顯示停用節點。
- 驗證：MariaDB 現有版本全部啟用，尚無停用後代可重現；未執行移動。

### BUG-059：子樹移動只比對來源版本，未保護其他受影響節點

**狀態：已修正（2026-10-06）。** 修改前確認僅比對來源版本，主檔更新只按 OID。現逐一核對後代預覽版本，並以 `OID + CURRENT_VERSION_NO` 條件更新每個受影響主檔；一般編輯與停用也使用相同條件更新。版本不符時交易回滾並提示重載。

- 嚴重度：中。性質：Java 併發控制與 Mapper 更新條件。
- 預期：移動子樹時，所有待改寫節點都應維持預覽／讀取時的版本，若期間有管理員更新子節點，操作應拒絕並要求重載。
- 實際：`validateMove()` 只對來源部門呼叫 `assertVersion()`；`move()` 對每個後代用先前查出的版本建立新版本，但沒有逐一核對後代主檔版本。`FmOrgUnitMapper.xml` 的主檔 `update` 只有 `WHERE OID`，沒有預期版本條件。後代若在讀取與寫入之間被其他交易修改，可能覆蓋較新的版本號或遇到唯一鍵衝突，無法給出可靠的衝突提示。
- 證據：`FmOrgUnitLogicServiceImpl.validateMove()`／`move()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmOrgUnitMapper.xml` 的 `update`。
- 驗證：目前所有部門版本均為 1；未執行並行寫入測試，因此具體衝突型態依交易交錯而定。
