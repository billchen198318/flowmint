# FlowMint 2.0 全系統重新規劃

日期：2026-07-30  
適用：台灣、中國大陸  
狀態：新系統設計與開發唯一規格集

## 核心結論

FlowMint 只做簽核。新模型明確排除集團、法人、據點、完整 HR 職務職等與其他未證明必要性的核心主檔；這是產品範圍決策，不是未完成 Backlog，也不應作為企業簽核完成度的扣分項目。Tenant 是唯一資料隔離範圍，「總公司」是部門樹根節點。

本機 `flowmint` 目前共有 55 張 `fm_*` 表，包含核心簽核、後續營運／整合擴充，以及採購單並行占額使用的 `fm_purchase_order_reservation`。QIFU4 帳號／權限及 Flowable 引擎表是外部依賴，不計入 `fm_*` 表數。

## 目前狀態（2026-08-31）

- Phase 1～5 核心簽核與營運程式已完成，請購流程 Version 2 已發布；完整瀏覽器、多帳號及實際資料量 E2E 仍待完成。
- `FM_PROG010D0001` AI Provider 管理、四種 Provider Adapter、Task AI 分析、快取與稽核已完成程式實作，但尚未使用真實 API Key 完成正式整合驗收。
- `31` 公司名片申請單的 Form、Data Action、Document Number Rule、BPMN、Task Policy 與 Resolver
  Version 1 均已發布並完成配置，目前只待真實多帳號瀏覽器／MariaDB／Flowable E2E 驗收；`30`
  採購單／驗收單及 `32` 外部系統 API 管理／流程拋單的個別狀態以各自文件為準。
- 共用待辦頁已修正 `REJECT／RETURN` 誤執行完整 Form.io 與 Custom JavaScript `beforeSubmit`
  驗證的問題；目前只有 `APPROVE／RESUBMIT` 會提交並驗證表單資料。請購單與公司名片申請單
  均已建立 Form Version 2 草稿，修正待辦階段重載或改寫申請人、任職及路由欄位的問題；兩個
  草稿尚未發布，也尚未變更正式流程綁定。
- 本機 `flowmint.tb_sys_prog` 的 `FM_PROG010D` Folder 名稱已更新為 `FJ. API-整合服務`；既有 `FM_PROG010D0001` 維持不變，規劃中的外部 API 管理使用 `FM_PROG010D0002`。

## 閱讀順序

1. [新系統總綱](00-新系統總綱.md)
2. [核心資料模型](01-核心資料模型.md)
3. [系統架構](02-系統架構.md)
4. [組織與簽核人規格](03-組織與簽核人規格.md)
5. [流程與表單規格](04-流程與表單規格.md)
6. [安全、權限與稽核](05-安全權限與稽核.md)
7. [後端程式規範](06-後端程式規範.md)
8. [前端程式規範](07-前端程式規範.md)
9. [API 規格](08-API規格.md)
10. [程式編排](09-程式編排.md)
11. [測試與驗收](10-測試與驗收.md)
12. [遷移與重建計畫](11-遷移與重建計畫.md)
13. [開發路線](12-開發路線.md)
14. [資料庫規範](13-資料庫規範.md)
15. [狀態與代碼](14-狀態與代碼.md)
16. [QIFU4 前端實作規範](15-QIFU4前端實作規範.md)
17. [QIFU4 後端持久層規範](16-QIFU4後端持久層規範.md)
18. [QIFU4 帳號與 Role 邊界](17-QIFU4帳號與Role邊界.md)
19. [目前開發進度](18-開發進度.md)
20. [原始碼排版與交付規範](19-原始碼排版與交付規範.md)（強制）
21. [動態資料服務規格](20-動態資料服務規格.md)
22. [MariaDB 操作規範](21-MariaDB操作規範.md)
23. [附件上傳與儲存規格](22-附件上傳與儲存規格.md)
24. [請購單流程與表單配置規格](23-請購單流程與表單配置規格.md)
25. [共用單據編號規格](24-共用單據編號規格.md)
26. [Workspace 與申請中心重構規劃](25-Workspace與申請中心重構規劃.md)
27. [平行加簽規劃](26-平行加簽規劃.md)
28. [正式會簽配置與實作指南](27-正式會簽配置與實作指南.md)
29. [流程簽核改派規劃](28-流程簽核改派規劃.md)
30. [AI 簽核解說精靈規劃](29-AI簽核解說精靈規劃.md)
31. [採購單與驗收單表單流程配置規劃](30-採購單與驗收單表單流程配置規劃.md)
32. [公司名片申請單表單流程配置規劃](31-公司名片申請單表單流程配置規劃.md)
33. [外部系統 API 管理與流程拋單規劃](32-外部系統API管理與流程拋單規劃.md)

## SQL

- [完整資料庫 DDL、主資料與 Program 註冊](flowmint.sql)

## Schema 邊界

### 保留

Tenant、Tenant Account、Employee、Approval Level、Department Tree、Department Title、Department Duty、Employee Department、Employee Duty、Department Head、Approval Authority、Approval Group、Delegation、Form、Process、Task Policy、Assignment Rule、Runtime Index、Snapshot、Incident、Attachment、Task Action、Notification。

### 移除

Enterprise Group、Legal Entity、Location、Employment、Job、Grade、Position、Project Organization、Work Queue、Org Service Scope、Sync Platform、Work Calendar 及沒有直接簽核案例的擴充表。

其中集團、法人與據點資料不屬於簽核核心。若特定表單需要簽約主體、統編、付款公司、工安廠區或收貨地址，應使用業務欄位、動態資料服務或外部 ERP／HR 主檔整合，不把它們重新建立成 FlowMint 共用 ORG 的必要依賴。

## 實作規則

後續資料庫基準只保留 `flowmint.sql`。必須先核准規格，從該主檔建立隔離資料庫並通過驗證，再開始 Entity／Mapper／Service／API／UI 與正式 migration；不得在 `doc` 另留一次性 SQL。

## SQL 檔案政策

`backend/doc` 只保留 `flowmint.sql`，作為完整 DDL、主資料、Program 與目前選單資料的文件基準。
一次性 schema、seed、資料修正與 Program register SQL 執行完成並併入主檔後必須刪除；既有環境的
增量升級應放在正式 migration 位置或由 QIFU4 管理功能處理，不得再於 `doc` 新增其他 `.sql`。
