# 34 BPMN 流程設計操作說明

日期：2026-09-06

## 說明入口

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

此項為前端說明功能，不涉及 MariaDB 異動。真實登入環境的瀏覽器操作驗收仍待執行，
應檢查配置說明入口、各節點主題定位、搜尋無結果、Esc／按鈕關閉、焦點返回、
已發布版本閱讀與窄螢幕捲動，並確認關閉前後草稿內容一致。
