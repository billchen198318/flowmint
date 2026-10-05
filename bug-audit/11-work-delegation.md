## 11 工作代理（`FM_PROG003D0002`）— 靜態／唯讀盤查完成

2026-10-03 核對 `backend/doc/03-組織與簽核人規格.md` 第 6.2 節、README 工作代理說明、`FMPROG003D0002Controller` 全端點、`FmWorkflowDelegationLogicServiceImpl`、`FmWorkflowDelegationMapper.xml`、Vue 清單／建立／編輯畫面，以及 `FmTaskRuntimeLogicServiceImpl` 的代理授權、再次轉代理和 `FmDelegationScopeEvaluator`。同人與日期先由 Java 驗證，資料表也有 CHECK；更新時以原資料固定 Tenant 與被代理人。組織健檢的循環漏報已記於 BUG-012，未重複編號。MariaDB 唯讀 `SHOW CREATE TABLE` 確認代理表有主鍵、`(TENANT_ID, DELEGATION_ID)` 唯一鍵及查詢索引；`SELECT COUNT(*)` 顯示代理 0 筆、Tenant 1 筆。無現成資料實測跨 Tenant、期間重疊與併發建立，以下新增問題均依程式及 DDL 推論。

### BUG-011：代理帳號候選與儲存驗證忽略 membership 有效期間及帳號啟用狀態

- 嚴重度：中。性質：Java 參照驗證；目前代理設定 0 筆，無現成資料可重現。
- 預期：被代理人與代理人在代理生效期間都應具備有效 Tenant membership 與啟用帳號；前端候選名單不應把已失效帳號顯示為可用。
- 實際：`FmWorkflowDelegationLogicServiceImpl.accountLabels()` 只查 `fm_tenant_account.STATUS='ACTIVE'`，未檢查 `EFFECTIVE_FROM`／`EFFECTIVE_TO`，也未查全域 `tb_account.ON_JOB`。`validate()` 以此候選 Map 判定被代理人與代理人有效，故可儲存指向已過期 membership 或停用登入帳號的代理設定。Vue 帳號候選同樣由 `accountOptions()` 取得。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmWorkflowDelegationLogicServiceImpl.java` 的 `accountLabels()`、`accountOptions()` 與 `validate()`；`backend/doc/03-組織與簽核人規格.md` 第 6.2 節規定過期、未生效或停用授權不可使用。MariaDB 唯讀查詢顯示目前狀態啟用但期間無效的 membership 0 筆、membership 啟用但全域登入帳號停用 0 筆。

### BUG-083：工作代理管理端未檢查操作人 Tenant membership

- 嚴重度：高。性質：Java 跨 Tenant 權限邊界；目前只有一個 Tenant，無跨 Tenant 實測資料。
- 預期：具備程式操作權限的非系統管理員，只能查詢及維護自身有權存取的 Tenant 代理設定；以 OID 操作時也須驗證該筆資料的 Tenant。
- 實際：`/findPage` 可省略 Tenant 篩選查詢全表；`/tenant-options` 列出所有啟用 Tenant；`/account-options`、`/group-options` 直接使用請求 Tenant；`/load`、`/update`、`/deactivate` 依 OID 載入或修改。Logic 僅驗證代理設定參照，未對操作人的 Tenant membership 呼叫 `FmTenantAccessGuard` 或等價檢查。Controller 的 Program 權限只限制功能入口，沒有把資料限於操作人所屬 Tenant。
- 證據：`FMPROG003D0002Controller` 全端點、`FmWorkflowDelegationLogicServiceImpl` 的 `create()`／`load()`／`update()`／`deactivate()`／選項方法，以及 Mapper 的 `findPage` 與 `selectByPrimaryKey`。MariaDB 唯讀查詢顯示 `fm_tenant` 1 筆，無跨 Tenant 操作可實測。

### BUG-084：代理期間重疊檢查在併發建立時沒有資料庫保護

- 嚴重度：中。性質：Java／MariaDB 併發一致性；代理表目前 0 筆，未以併發請求重現。
- 預期：同一 Tenant、被代理人及代理範圍的啟用期間不可重疊；兩個同時建立的請求也須維持此限制。
- 實際：`create()` 先以 `validateOverlap()` 查詢既有代理，再執行 `insert()`；兩個交易若同時讀到無既有資料，都可通過。`fm_workflow_delegation` 只有代理 ID 唯一鍵及一般查詢索引，沒有可排除期間交疊的鎖定或約束。因此可能建立兩筆同範圍、同期間的有效代理。
- 證據：`FmWorkflowDelegationLogicServiceImpl.create()`／`validateOverlap()`、`FmWorkflowDelegationMapper.xml` 的一般 `SELECT`／`INSERT`，以及 MariaDB `SHOW CREATE TABLE fm_workflow_delegation`。單一交易的順序新增會被 Java 擋下；併發情境仍待隔離環境驗證。

