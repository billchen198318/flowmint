# 34 BPMN 流程設計操作說明

日期：2026-09-06；文件同步：2026-09-10

## 工作節點分類與目前可用功能

流程中的 Task 分為 User Task 與 System Task 兩大類：

- User Task：人工簽核、填寫與補件；會簽、加簽及派送方式依 User Task 政策配置，不另列為工作大類。
- System Task：底層為受控 Service Task；正式 subtype 為 `DATA_ACTION` 與 `GROOVY`。外部 API 直接由 Groovy 使用標準 Java HTTP Client 呼叫，腳本須在測試環境完成 GET／POST、timeout、認證、JSON 解析與錯誤處理驗證後才發布。

Start／End Event、Gateway 與 Sequence Flow 是流程元件，不屬於 Task。Timer Boundary Event、Call Activity 及原生 Script Task 目前不能發布；詳見 [04 流程與表單規格](04-流程與表單規格.md) 第 2 節。

Data Action 節點目前只設定固定 Action Version 與 request／response mapping。`CONSTANT:` 後的內容為字串，輸出只寫入 `FORM_DATA.<path>`；未列入 response mapping 的結果不寫回。節點 Timeout、自訂重試政策、Idempotency Key 與專屬 Incident 為待實作項目，不能依規劃文件直接填入設計 XML。

## 說明入口

2026-09-10：Groovy Editor 與配置說明已同步檢查／試跑操作。檢查目前編輯副本，試跑另使用人工
JSON；不儲存、不讀正式單據、不套用輸出 Mapping。結果與日誌以文字顯示，診斷可跳轉行／欄；
編輯或關閉後舊結果失效。此節取代下文歷史紀錄的「按鈕尚未接線」，不代表正式流程 E2E 已通過。

`FM_PROG004D0001` 編輯頁右側屬性面板提供「配置說明」。點選後開啟完整說明
dialog，可搜尋中文名稱、BPMN 元件名稱、Resolver 代碼與 JSON 配置。

說明依目前選取的 User Task、Service Task、
Sequence Flow、Gateway 或 Start／End Event 開啟相應主題。未選取節點時開啟入門步驟。
草稿及已發布版本皆可閱讀，開啟／關閉說明不會儲存流程或呼叫資料異動 API。

## 主題範圍

1. 從零建立流程：準備資料、建立主檔、畫圖、設定關卡、預覽與發布。
2. Start Event、End Event、Sequence Flow 與節點識別。
3. User Task 的表單固定版本、欄位權限及 Grid 子欄位 JSON。
4. ASSIGNEE、CANDIDATE、ALL、SEQUENTIAL、APPLICANT_CORRECTION 派送方式。
5. 設計器現行 15 種簽核人 Resolver 與所需資料。
6. 層級匹配、核決權限條件及目標配置。
7. 自簽、重複簽核、操作開關、平行加簽上限及期限提醒。
8. Exclusive、Inclusive、Parallel Gateway 分流／匯合及使用範例。
9. Gateway 出線條件、AND／OR、Default Flow、套用與儲存。
10. 申請人補件節點、退回操作及重送出線。
11. Data Action Task 固定版本、Request／Response Mapping、Context、常數型別及重試限制。
12. 啟動規則、版本狀態、簽核人預覽與發布檢查。
13. 不支援元件、常見配置錯誤與驗收案例。

## 最小操作範例

先發布一份同 Tenant 的表單，再建立以下流程：

```text
開始 → 主管審核 → 結束
```

主管審核使用 User Task，選取已發布表單、設定 `ASSIGNEE` 與 `DIRECT_MANAGER`，
並確認測試申請人的任職與主管有效。欄位預設可設為唯讀：

```json
{
  "default": "READ",
  "fields": {
    "reviewNote": "EDIT"
  }
}
```

`reviewNote` 必須替換成實際表單欄位 key。新增流程啟動允許規則，儲存草稿，
輸入測試申請人執行簽核人預覽，再發布並以真實帳號起單驗證。

需要金額分流時，在主管審核後加入 Exclusive Gateway；點選出線設定金額條件，
再指定另一條線為 Default Flow。每次設定都需按「套用流程條件」，最後儲存草稿。

## 內容維護

完整 dialog 內容統一維護於：

`frontend-v-nx/pages/fm_prog004d0001/components/bpmnDesignerGuide.ts`

視窗元件為同目錄的 `BpmnDesignerHelp.vue`。說明使用 Vue 文字插值呈現，
JSON／條件範例僅供閱讀，不執行程式碼。視窗使用原生 modal dialog 的焦點限制，
支援 Esc、關閉按鈕、背景捲動鎖定及關閉後焦點返回；窄螢幕改為上下排列。

增加／修改設計器欄位、Resolver、發布驗證或 Runtime 契約時，需同步更新相關主題。
現況以實際程式為準，不把規劃中的功能寫成可用操作，尤其是 System Task 的
timeout、專屬 attempt／Incident 與 Snapshot。相關背景見第 04、27、33 章。

## 驗證狀態

### Groovy Editor 實作時的同批教學交付

2026-09-08 接續開發：`bpmnDesignerGuide.ts` 已加入 `groovy-editor` 主題，選取 Groovy 節點可定位該主題；與新 Editor Dialog 的教學共用範例及說明來源。草稿功能預設關閉，migration 尚未套用；編輯器與教學程式已有接線，但登入瀏覽器驗收、worker 檢查／試跑與正式發布仍未完成。以下交付清單仍作為驗收依據。

開始實作第 35 章 Groovy Editor 時，必須同批更新既有 BPMN 配置說明 Dialog 的 `bpmnDesignerGuide.ts` 內容及主題定位，不可只完成新 Editor Dialog 裡的教學 Tab。既有教學須補上：

- 新增 Groovy System Task 與開啟大型編輯 Dialog。
- Editor／教學 Tab、表單欄位與系統變數選取、輸入／輸出 Mapping。
- 檢查、試跑、套用至節點、儲存流程的順序與差異。
- 已發布版本唯讀、執行限制與錯誤排查入口。

選取 Groovy 節點後按「配置說明」，須直接定位 System Task／Groovy 操作主題。詳細語法與程式範例放在 Groovy Dialog 的教學 Tab，兩處使用一致契約。上述內容與 Editor 一起納入瀏覽器驗收；未完成的能力仍標示未開放。本段只記錄後續交付要求，尚未修改教學程式或完成驗收。

### 既有驗證紀錄

2026-09-08 已同步 `bpmnDesignerGuide.ts`：入門補上 User Task／System Task 兩大類，System Task 主題釐清 Service Task 對應、Groovy 尚未開放、FORM_DATA 取自流程表單變數，以及不支援的 Mapping 名稱。保留 13 個主題與既有 `service-task` 主題 ID，節點開啟入口保持相容。Prettier check、Nuxt production build 與 `git diff --check` 通過；本輪未執行真實登入瀏覽器驗收。

2026-09-08 新增的 [Groovy System Task 規劃](35-SystemTask的Groovy腳本規劃.md) 尚未實作；現行 dialog 不應把 Groovy 編輯、試跑或發布描述成已可用。功能實作時需新增對應操作主題、輸入／輸出與錯誤排除說明。

此項為前端說明功能，不涉及 MariaDB 異動。真實登入環境的瀏覽器操作驗收仍待執行，
應檢查配置說明入口、各節點主題定位、搜尋無結果、Esc／按鈕關閉、焦點返回、
已發布版本閱讀與窄螢幕捲動，並確認關閉前後草稿內容一致。
