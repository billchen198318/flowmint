## 09 組織人員資料檢查（`FM_PROG002D0007Q`）— 靜態／唯讀盤查完成

2026-10-02 接續核對 `backend/doc/36-組織人員資料檢查規劃.md` 明定的第一版 14 類檢查及後續目標，並查看 `FMPROG002D0007Controller`、`FmOrganizationHealthLogicServiceImpl` 全部四區算法、`frontend-v-nx/pages/fm_prog002d0007/index.vue` 的摘要、篩選與修正導向。Controller 只取 `X-FlowMint-Tenant`，Logic 對目前登入帳號檢查有效 Tenant membership；此邊界符合第一版契約。MariaDB 唯讀確認員工 140 筆、任職 141 筆、主管 38 筆、代理 0 筆；沒有啟用任職搭配停用員工。第一版明確排除排程、持久化、通知、匯出與進階規則，未將其當成既有功能缺陷。

**完成範圍與限制：**四區現行檢查、Tenant 邊界、結果呈現與資料現況已核對；沒有代理資料或失效員工情境，也未建立測試資料，新增 Java 問題依程式推論。

### BUG-012：代理循環檢查未保留同一人的多筆代理邊，也未檢查範圍交集

- 嚴重度：中。性質：Java 健檢演算法；目前代理設定 0 筆，無現成資料可重現。
- 預期：`DELEGATION_CYCLE` 只在代理期間及範圍可同時命中的多段關係形成循環時回報；同一人有多筆不同範圍代理時，每筆關係都須參與檢查。`backend/doc/36-組織人員資料檢查規劃.md` 第 2.4 節明確要求此條件。
- 實際：`FmOrganizationHealthLogicServiceImpl.inspectDelegations()` 將代理關係寫入 `Map<String,String> edges`，同一被代理人的後一筆覆蓋前一筆，可能漏報循環；`cycleIssues()` 只沿帳號關係找環，沒有判斷各邊的代理範圍是否有交集，因此也可能把不同流程／群組的代理誤報為循環。雖已先過濾「目前有效」記錄，範圍交集仍未檢查。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmOrganizationHealthLogicServiceImpl.java` 的 `inspectDelegations()` 與 `cycleIssues()`；Vue `frontend-v-nx/pages/fm_prog002d0007/index.vue` 顯示後端回傳的問題。MariaDB 唯讀查詢顯示目前 `fm_workflow_delegation` 為 0 筆。

### BUG-076：直屬主管健檢可能把失效員工任職視為有效主管

- 嚴重度：中。性質：Java 健檢參照判定。
- 預期：直屬主管來源解析出的任職，除任職本身有效外，所屬員工也應為有效員工；否則須回報無法解析主管。
- 實際：`inspect()` 分別按有效期間過濾員工和任職，`inspectManagers()` 建立的 `byId` 卻包含所有有效任職，`managerAssignment()` 對 `EXPLICIT` 只檢查主管任職 ID 是否在 `byId`，對 `ORG_HEAD`／`PARENT_HEAD` 只依正式主管員工 ID 找有效任職，均未確認該主管員工仍在有效員工清單。因此主管員工失效而任職仍為 `ACTIVE` 時，不會產生 `DIRECT_MANAGER_MISSING`。
- 證據：`FmOrganizationHealthLogicServiceImpl.inspect()`／`inspectManagers()`／`managerAssignment()`；`backend/doc/36-組織人員資料檢查規劃.md` 第 2.2 節要求主管鏈節點的員工有效。
- 驗證：MariaDB 現有 141 筆任職中，啟用任職搭配停用員工為 0；未建隔離資料重現。

### BUG-077：關鍵字篩選無命中時把有問題區塊顯示為檢查正常

- 嚴重度：低。性質：Vue 結果呈現。
- 預期：區塊原有問題但關鍵字沒有命中時，畫面應顯示「沒有符合篩選條件的問題」，保留區塊實際異常狀態。
- 實際：區塊計數從完整 `result` 計算，但空畫面條件使用 `!filteredIssues(section.key).length`，直接顯示「檢查正常」。輸入與現有問題不符的關鍵字後，區塊會同時顯示非零 ERROR／WARNING 計數和「檢查正常」。
- 證據：`frontend-v-nx/pages/fm_prog002d0007/index.vue` 的 `filteredIssues()`、`count()` 與區塊空狀態模板。
- 驗證：Vue 靜態核對；未啟動瀏覽器操作。

