## 22 營運報表 — 靜態／唯讀盤查完成

2026-10-05 收尾：已核對 `FmIncidentOperationsController` 的 `/reports/summary`、`FmProcessMonitorLogicServiceImpl.report()`、營運報表 Vue、README 的日期與時區說明，以及相關 Logic 測試。非管理員 Tenant 資料授權見 BUG-021；日期使用伺服器預設時區符合已記載限制。現有流程實例為 0，未驗證實際統計值或跨時區情境。

2026-10-04 續查 `FmProcessMonitorLogicServiceImpl.report()` 的日期區間、流程統計及 Flowable 到期任務查詢，並對照 `README.md`。日期篩選採伺服器預設時區，與 README 第 737 行明載的現行限制一致；沒有另列重複 BUG。MariaDB 目前流程實例 0 筆，報表數值與跨時區影響仍待具體資料驗證。

### BUG-021：營運報表與流程監控未限制非管理員的 Tenant membership

- 嚴重度：高。性質：Java 跨 Tenant 資料查詢；與 BUG-020 同類型，但由另一個服務獨立實作。
- 預期：非系統管理員即使擁有全域 `FLOWMINT_OPERATIONS` 角色，仍應只看到其有資料授權的 Tenant 報表及流程明細。
- 實際：`FmProcessMonitorLogicServiceImpl.report()`、`load()` 等入口只執行 `requireOperator()`；它只檢查全域角色，不檢查操作人的 Tenant membership。請求中的 Tenant ID 直接用作查詢條件，因此持有營運角色的非管理員可查其他 Tenant 的報表、表單快照與簽核歷程。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessMonitorLogicServiceImpl.java` 的 `report()`、`load()`、`requireOperator()`。MariaDB 唯讀查詢顯示目前流程實例 0 筆，沒有可驗證跨 Tenant 明細的現成資料。

### 2026-10-09 BUG-021 修復複核

- **BUG-021 已修正**：核對 `report()`、`find()`、`load()` 原先均只檢查全域營運角色，未查指定 Tenant membership，缺陷成立。三個入口現在對非系統管理員要求指定 Tenant 的 ACTIVE 且在生效期間內的 membership；無權時在查詢報表或明細前拒絕。沿用第 20 單元 Incident 營運入口的授權語意。
- `FmProcessMonitorLogicServiceImplTest` 驗證營運角色無 membership 時三個入口均拒絕，既有管理員空頁測試仍通過；Maven 定向測試通過。尚未以雙 Tenant 的 HTTP／資料庫情境驗收。
