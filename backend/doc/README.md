# FlowMint 2.0 全系統重新規劃

日期：2026-07-30  
適用：台灣、中國大陸  
狀態：新系統設計與開發唯一規格集

## 核心結論

FlowMint 只做簽核。新模型移除集團、法人、據點、完整 HR 職務職等與其他未證明必要性的資料。Tenant 是唯一資料隔離範圍，「總公司」是部門樹根節點。

新 FlowMint 業務 Schema 只保留 34 張 `fm_*` 表；QIFU4 帳號／權限及 Flowable 引擎表是外部依賴，不在核心 DDL 重建。

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

## SQL

- [完整核心 DDL](flowmint-core-schema.sql)
- [初始層級與根部門 Seed](flowmint-core-seed.sql)

## Schema 邊界

### 保留

Tenant、Tenant Account、Employee、Approval Level、Department Tree、Department Title、Department Duty、Employee Department、Employee Duty、Department Head、Approval Authority、Approval Group、Delegation、Form、Process、Task Policy、Assignment Rule、Runtime Index、Snapshot、Incident、Attachment、Task Action、Notification。

### 移除

Enterprise Group、Legal Entity、Location、Employment、Job、Grade、Position、Project Organization、Work Queue、Org Service Scope、Sync Platform、Work Calendar 及沒有直接簽核案例的擴充表。

## 實作規則

後續不得先改舊 `flowmint.sql` 再回頭補文件。必須先核准本規格，從 `flowmint-core-schema.sql` 建立隔離資料庫並通過驗證，再開始 Entity／Mapper／Service／API／UI 與 migration。

