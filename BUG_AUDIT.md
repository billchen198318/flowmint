# FlowMint 功能單元 Bug 盤查

> **備註：2026/10/05 進行收斂發散盤整。**

> **目前結論（2026-10-05）：01～27 全部完成靜態／既有唯讀資料盤查，本次探索式盤查結束。** `BUG-001`～`BUG-185` 是 185 條唯一原始觀察，**不是 185 個已重現缺陷**。修復時依 01→27 功能單元順序處理各單元全部條目；[11 個跨單元工作包與降級／合併決定](bug-audit/00-convergence.md)供判斷重複根因及風險，不決定修復順序。各單元收尾段記錄未實測限制；完成盤查不代表問題已修正。

> **接手規則：**從 01 單元開始，按 [功能單元清單與進度](#功能單元清單與進度)逐一處理到 27；每條觀察須留下「已修正並驗證／驗證後不成立／規格待決／受環境阻擋」之一的結果。工作包僅用來合併同根因，不得跳過不在工作包中的條目。

## 下次 Codex：從這裡開始修 Bug

1. 先讀本節與 [01 身分與 Tenant 邊界](bug-audit/01-identity-tenant-boundary.md)，由該檔第一條觀察開始，依單元檔的條目順序逐條核對規格、程式、測試及可用的隔離情境。下方日期快照只供追溯。
2. 對每條觀察先確認是否仍成立；成立者修正並加入有意義的回歸測試，不成立者寫明反證。規格待決或環境不足者記錄具體阻礙，不將其誤標已修正。
3. 當前單元的全部條目都有處理結果，且相關測試通過後，記錄修改檔案與驗證結果，再進下一單元。跨單元同根因依[收斂決策](bug-audit/00-convergence.md)合併實作，並在每個原條目交叉記錄結果；不因風險等級跳到較後單元。
4. 保留 `BUG-001`～`BUG-185` 編號作證據索引，不重新展開探索式盤查。目前**尚未開始修正**；單元的「盤查完成」與「修復完成」是不同狀態。

> 2026-10-04 前一輪接續盤查：新增 [BUG-150（待辦動作）](bug-audit/17-task-approval.md)、[BUG-151／152（Data Action Task）](bug-audit/25-system-tasks.md)、[BUG-153（SLA 分頁）](bug-audit/21-notifications-email-sla.md)、[BUG-154（AI 強制刷新）](bug-audit/23-ai-provider.md)、[BUG-155（Data Action 參數）](bug-audit/15-data-action.md)。當時共記錄 155 項；15 單元 2 項、17 單元 2 項、21 單元 4 項、23 單元 11 項、25 單元 2 項。該輪問題均為程式碼推論，未執行寫入重現。下列 2026-10-03 快照為歷史紀錄。

> 2026-10-04 表單版本前一輪續查：第 13 單元 BUG-110 已涵蓋 `saveDraft()` 與 `publish()` 的並行更新；版本更新只按 OID、沒有狀態或修訂條件。`fm_form_version` 具有 `(TENANT_ID, FORM_ID, VERSION_NO)` 唯一鍵，兩次建立相同版號會由資料庫阻擋，未另列新問題。表單停用入口已對照 Vue／Controller／Logic 與起單篩選；停用本就阻止新使用，未列缺陷。`FmFormDesignValidator` 未阻擋 Form.io Builder 的 `validate.custom`／`validate.json`，正式送單後端亦未執行，新增 BUG-156。MariaDB 唯讀確認 4 筆表單版本均為 PUBLISHED，且驗收單已有非空 `deliveryDate.validate.custom`；先前聲稱沒有這類設定是查詢引號跳脫錯誤，已更正。該輪結束時共 156 項，第 13 單元 27 項；未以違規送單重現。

> 2026-10-04 單據編號續查：第 26 單元新增 [BUG-157／158](bug-audit/26-document-numbers.md)：週期重設與格式 token 未連動檢查；包含系統欄位的表單可經直接起單 API 把自填 `documentNumber` 存入表單資料。MariaDB 唯讀確認現有 4 筆規則均為 ACTIVE、MONTH 且格式含年月，序列表 0 筆；4 張已發布表單均含 `documentNumber` 欄位，表單資料 0 筆。先前說不含欄位是 `LIKE` 引號跳脫造成的誤判，已用 `INSTR` 與 Schema 片段覆核。已核對四套已發布流程均連到對應 ACTIVE 編號規則與表單。下一步續查第 26 單元跨週期取號及預設表單流程配置，再回第 13／12 單元未覆蓋路徑。目前共 158 項，第 26 單元 2 項，下一編號 BUG-159；未異動規則或送單重現。

> 2026-10-04 外部 API 續查：第 24 單元新增 [BUG-159](bug-audit/24-external-api.md)：外部拋單的 Runtime 交易提交後若 API Request 結果更新失敗，冪等重送及狀態查詢均無法回報已建流程。外部 submission 未清除顯示用 `documentNumber`，已併入第 26 單元 BUG-158。MariaDB 唯讀確認 API Request、流程實例均為 0 筆，故障時序未實測。下一步續查第 24 單元 Client 管理、Key 輪替與外部資料查詢；第 26／13／12 單元仍需接續。目前共 159 項，第 24 單元 2 項，下一編號 BUG-160。

> 2026-10-04 Client 權限續查：第 24 單元新增 [BUG-160](bug-audit/24-external-api.md)：流程白名單 `[]` 在 Principal 被當成全部允許，違反規格的空名單不得代表所有流程。已核對 Client 建立／編輯、Key 簽發／輪替／撤銷、認證及 Vue 管理入口；MariaDB 唯讀確認 Client 與 Key 均為 0 筆，未實測外部請求。下一步續查第 24 單元 IP Allowlist、配額、稽核與其餘外部資料查詢；第 26／13／12 單元仍需盤查。目前共 160 項，第 24 單元 3 項，下一編號 BUG-161。

> 2026-10-04 認證稽核續查：第 24 單元新增 [BUG-161](bug-audit/24-external-api.md)：Key 驗證與來源 IP 拒絕在 Filter 的早退路徑沒有安全稽核紀錄。配額按 Tenant＋Client 計數及 Log 非原子保留已由規格揭露，未重複列 BUG。MariaDB 唯讀確認 Access Log 0 筆；未送出失敗請求。下一步續查第 24 單元 Client IP 設定、外部組織查詢與管理稽核，再接續第 26／13／12 單元。目前共 161 項，第 24 單元 4 項，下一編號 BUG-162。

> 2026-10-04 外部組織查詢續查：第 24 單元新增 [BUG-162](bug-audit/24-external-api.md)：部門員工與 FLAT 組織樹接受極大正數頁碼，計算偏移量溢位成負索引後回 500。已核對 Tenant、`effectiveAt`、`pageSize` 與 Controller 錯誤分支；MariaDB 唯讀查詢任職 141 筆、部門版本 35 筆，未送出外部 HTTP 請求。下一步續查第 24 單元員工／任職狀態語意及 Client IP 設定，再接續第 26／13／12 單元。目前共 162 項，第 24 單元 5 項，下一編號 BUG-163。

> 2026-10-04 DataSource Pool 續查：第 14 單元新增 [BUG-163](bug-audit/14-datasource-pool.md)：既有 Pool 的連線測試可解密沿用舊密碼，卻把它用於表單另填的新 JDBC URL／帳號。MariaDB 唯讀確認目前 `A01/FLOWMINT` 為 1 筆 ACTIVE Pool，未讀密碼或測試連線。下一步續查第 14 單元 Registry 快取、Validation Query 與 Data Action 引用；第 24／26／13／12 單元也未完成。目前共 163 項，第 14 單元 2 項，下一編號 BUG-164。

> 2026-10-04 Validation Query 續查：第 14 單元新增 [BUG-164](bug-audit/14-datasource-pool.md)：驗證 SQL 未限制唯讀，測試及正式 Registry 均交由 Hikari 執行，若連線帳號有寫入權限，借用連線時可能反覆執行資料異動。此為程式與 HikariCP 行為推論，未執行寫入語句。下一步續查 Registry 快取與 Data Action 引用；第 24／26／13／12 單元也未完成。目前共 164 項，第 14 單元 3 項，下一編號 BUG-165。

> 2026-10-04 Registry 生命週期續查（後續更正）：第 14 單元 [BUG-165](bug-audit/14-datasource-pool.md) 起初記為停用後快取未清理；後續核對 Pool Mapper 發現 `selectListByParams` 忽略 `poolId`／`status`，Registry 不會先拒絕停用 Pool，原推論不成立。BUG-165 已改記實際根因：Data Action 可能連到錯誤或停用的 Pool，詳見單元條目。該時點編號共 165 項，下一編號 BUG-166。

> 2026-10-04 Data Action 稽核續查：第 15 單元新增 [BUG-166](bug-audit/15-data-action.md)：Executor 在參數解析、Step 驗證或取得 Pool 時，尚未進入記錄 Execution Audit 的 `try/finally`，前置失敗不會留下執行紀錄。此為靜態推論，未送出失敗請求。下一步續查第 15 單元執行／Streaming、發布與版本引用；第 14／24／26／13／12 單元仍未完成。目前共 166 項，第 15 單元 3 項，下一編號 BUG-167。

> 2026-10-04 FOR_EACH 執行限制續查：第 15 單元新增 [BUG-167](bug-audit/15-data-action.md)：Batch 與 Generated Key 執行路徑未套用該 Step 的 Query Timeout，可能使用預設值或沿用前一個 Step 的值。MariaDB 先前唯讀確認 FOR_EACH Step 0 筆，未以慢查詢重現。下一步續查第 15 單元分頁與 Streaming、發布驗證；第 14／24／26／13／12 單元仍未完成。目前共 167 項，第 15 單元 4 項，下一編號 BUG-168。

> 2026-10-04 Pool 管理頁續查：第 14 單元新增 [BUG-168](bug-audit/14-datasource-pool.md)：後端支援 Idle Timeout 與 Max Lifetime，但 Vue 表單沒有兩個欄位可供調整。已核對 Controller、Mapper、Driver 白名單、加密與管理頁，並將 BUG-165 更正為 Mapper 忽略 Pool ID／狀態的根因。MariaDB 唯讀確認唯一 ACTIVE Pool 的驗證 SQL 是 `SELECT 1`、最小閒置數 1，並被 25 個 Action 引用；沒有現成的異動 Validation Query 或第二個 Pool 可重現 BUG-164／165。下一步續查第 14 單元跨資料庫與生命週期、第 15 單元分頁及發布；第 24／26／13／12 單元仍未完成。目前共 168 項，第 14 單元 5 項，下一編號 BUG-169。

> 2026-10-04 Data Action Task 發布續查：第 25 單元新增 [BUG-169](bug-audit/25-system-tasks.md)：發布驗證查詢 Step 時使用不存在的 `STEP_NO` 排序欄位。MariaDB 唯讀查詢確認 Step 49 筆，且相同排序 SQL 回 `ERROR 1054 Unknown column`；未發布新流程。Action／Version／Step Mapper 的條件篩選沒有第 14 單元 Pool Mapper 的同型缺口。下一步續查第 25 單元其他發布與 Runtime 路徑、第 15 單元分頁、第 14 單元生命週期；第 24／26／13／12 單元仍未完成。目前共 169 項，第 25 單元 3 項，下一編號 BUG-170。

> 2026-10-04 Data Action Task Mapping 續查：第 25 單元新增 [BUG-170](bug-audit/25-system-tasks.md)：發布驗證把 INACTIVE Step 的 Result Key 當成可用 Response 來源，Runtime 跳過 Step 後可能把缺失結果以 `null` 寫回表單。BUG-169 目前先阻斷正式發布；MariaDB 唯讀確認 Published 版本 34 筆 Step 全為 ACTIVE，未有現成重現配置。下一步續查第 25 單元 System Task Runtime／Groovy worker、第 15 單元分頁、第 14 單元生命週期；第 24／26／13／12 單元仍未完成。目前共 170 項，第 25 單元 4 項，下一編號 BUG-171。

> 2026-10-04 Groovy 恢復路徑續查：核對 Lease／READY Reaper、Recovery、Automatic Retry、Claim／Evaluation 初步時序與 Mapper；較新文件明確規定 Lease 到期未知結果不直接重送，程式一致，未新增 bug。Claim 的 60 秒 lease 長於目前允許的腳本設定 Timeout 上限 10 秒。MariaDB 唯讀確認 System Task Execution、Attempt、Groovy Manifest、Process System Task 均為 0 筆。本機 `system-task-local` profile 啟用 Runtime 與 Lease／READY recovery，Automatic Retry 開關仍預設關閉。下一步查第 25 單元 Commit、取消與人工處理及前端營運頁；第 15／14／24／26／13／12 單元仍未完成。總數維持 170 項，下一編號 BUG-171。

> 2026-10-04 Groovy Commit／取消續查：核對 Commit 的 Flowable 同交易、Form CAS／Snapshot／Attempt／Execution 成功紀錄，及取消時的流程鎖、generation 防護與 Execution deletion listener；目前未見獨立新缺陷，且無 System Task 執行資料可重現競態。下一步查第 25 單元人工 Retry／Recalculate、Incident 查詢與前端營運頁，再回頭核對交易測試覆蓋；第 15／14／24／26／13／12 單元仍未完成。總數維持 170 項，下一編號 BUG-171。

> 2026-10-04 Groovy 人工處理續查：核對 Retry／Recalculate 預檢與交易命令的 Tenant 權限、Process／Execution 鎖定、revision／generation、requestId 冪等與表單快照；Incident 查詢與狀態處理有操作權限檢查、狀態轉移及歷史分頁。Vue `GroovyIncidentPanel` 以 preview 值提交並在網路例外保留同一 requestId 重送；一般 Incident 營運頁是另一套 API。此輪未確認獨立新缺陷，也未因 Controller 無權限註解誤判為未授權。下一步核對第 25 單元交易測試、功能開關與查詢邊界；第 15／14／24／26／13／12 單元仍未完成。總數維持 170 項，下一編號 BUG-171。

> 2026-10-04 Groovy 測試／開關／查詢續查：相關 Retry、Recalculate、Incident、Claim、Commit 等測試以 Mockito 與 `GroovyTransactionFixture` 模擬依賴和 Spring 交易邊界；fixture 明確不模擬資料庫原子性，故 MariaDB 回滾與真實並發仍未驗證。本機 `system-task-local` profile 明確啟用 Incident Handling、Manual Retry、Recalculate，核心服務預設關閉；Incident 清單 offset 上限 10000、每頁 50，狀態歷史 offset 上限 1000000、每頁 50，Service 與 Mapper 條件一致。未新增獨立 bug。下一步續查第 25 單元 Groovy 發布／Runtime 整合邊界，再回第 15／14／24／26／13／12 單元。總數仍為 170 項，下一編號 BUG-171。

> 2026-10-04 Groovy 發布／Runtime 續查：流程發布先在交易外編譯全部 Groovy binding，再由 Coordinator 的交易保存 manifest、部署 BPMN 並更新版本；Runtime Definition 僅載入 PUBLISHED／RETIRED 固定版本、對應表單與 manifest，Preparation 重新核對 XML／binding SHA 與表單 Mapping。`backend/doc/35-SystemTask的Groovy腳本規劃.md` 記有歷史 Flowable／MariaDB 核心 E2E 驗收；本輪未重新執行，且目前相關執行表為空，不能將歷史驗收當成本輪競態重現。未確認新缺陷。下一步回第 15 單元 Data Action 分頁／Streaming，再查第 14／24／26／13／12 單元。總數維持 170 項，下一編號 BUG-171。

> 2026-10-04 Data Action 分頁續查：第 15 單元新增 [BUG-171](bug-audit/15-data-action.md)：`SELECT_LIST` 深頁請求在 SQL 設定 `offset + pageSize` 最大列數後，以 `queryForList()` 將之前所有列載入 JVM，再於記憶體切片；`page` 可至 1,000,000，Step `maxRows` 只限制單頁筆數。此為程式碼推論，未以大量資料量測。下一步續查同單元 Streaming、分頁 COUNT SQL 與版本引用，再查第 14／24／26／13／12 單元。目前共 171 項，第 15 單元 5 項，下一編號 BUG-172。

> 2026-10-04 Data Action COUNT SQL 續查：第 15 單元新增 [BUG-172](bug-audit/15-data-action.md)：`stripTopLevelOrderBy()` 只識別單引號，遇到雙引號字串中的 `ORDER BY` 會截斷 SQL，分頁 COUNT 查詢因此失敗；SQL Validator 未排除此類 SQL。此為靜態推論，尚未用資料庫請求重現。Streaming 單一 ACTIVE Step／QUERY／ONCE／SELECT_LIST／禁重試限制與程式一致；下一步查 Streaming Controller 回應及版本引用，再回第 14／24／26／13／12 單元。目前共 172 項，第 15 單元 6 項，下一編號 BUG-173。

> 2026-10-04 Data Action 版本引用續查：第 15 單元新增 [BUG-173](bug-audit/15-data-action.md)：表單設計器編輯既有 Data Action Binding 時，重新取得目前 Action Metadata，保存時無條件將 `actionVersion` 改為目前版本，可能讓原本固定的舊版引用隨 Mapping 編輯而升級。此為靜態推論，未在瀏覽器重現。Streaming Controller 以 `StreamingResponseBody` 回傳 NDJSON，後端限制單一 ACTIVE QUERY／ONCE／SELECT_LIST Step 且不重試；部分輸出後的失敗無明確規格可據，暫不另列缺陷。下一步續查第 14 單元 Pool 生命週期與第 24／26／13／12 單元。目前共 173 項，第 15 單元 7 項，下一編號 BUG-174。

> 2026-10-04 Pool 生命週期續查：建立、更新、停用均已對照 Logic 與 Mapper；更新使用 `LOCK_VERSION` 條件，Registry 下次取用同一 Pool ID 的新版本時關閉舊快取。停用後仍可能借用的根因已在 BUG-165，未重複列案。尚未以真實停用、借用中連線及跨節點情境驗證；下一步查第 24 單元外部 API 剩餘查詢與權限邊界，再查第 26／13／12 單元。總數維持 173 項，下一編號 BUG-174。

> 2026-10-04 外部 API Client 續查：第 24 單元新增 [BUG-174](bug-audit/24-external-api.md)：IP Allowlist 管理端只修剪字串，未按規格驗證／正規化 IPv4／IPv6 CIDR；認證端以 `InetAddress.getByName()` 解析規則，會接受主機名稱並在請求時才處理錯誤格式。此為靜態推論，現有 Client／Key 為 0 筆，未實測 DNS 或認證。員工與任職查詢已核對 Tenant、effectiveAt 及 assignment ACTIVE 條件；員工 detail 明確回傳在職狀態，未把其可查 INACTIVE 人員直接列為缺陷。下一步查第 24 單元外部查詢的其餘邊界與第 26／13／12 單元。目前共 174 項，第 24 單元 6 項，下一編號 BUG-175。

> 2026-10-04 外部 Form Template 續查：第 24 單元新增 [BUG-175](bug-audit/24-external-api.md)：API 的 `submissionContract` DTO 只有 key、type、required、multiple，缺少規格要求的 enum、巢狀路徑與 array／object 結構；收集器遞迴走子元件時也未保留父路徑。完整 `templateJson` 仍回傳，未以外部 Client 或現有巢狀表單重現契約使用問題。組織樹與部門員工查詢沿用 BUG-162 的頁碼溢位問題；Tenant／Scope 篩選未見新獨立缺口。下一步查第 26 單元單據編號跨週期與預設配置，再回第 13／12 單元。目前共 175 項，第 24 單元 7 項，下一編號 BUG-176。

> 2026-10-04 單據編號週期／交易續查：核對送單交易內的 Tenant 時區週期計算、序列 `FOR UPDATE`、首次建立唯一鍵競爭處理、lock version 遞增與溢位拒絕；規則格式未含週期的衝突已在 BUG-157，直接 API 可填顯示欄位已在 BUG-158，未新增條目。先前唯讀確認四套預設流程／表單／規則相互綁定，但序列表及正式表單資料為 0，沒有跨週期或併發送單重現。本輪接著查第 13 單元表單剩餘路徑及第 12 單元發布邊界。總數維持 175 項，下一編號 BUG-176。

> 2026-10-04 表單 Data Action Mapping 續查：第 13 單元新增 [BUG-176](bug-audit/13-form-design-attachments.md)：Binding 編輯器蒐集 Container／Grid 子欄位時只用單一 key，預設 Mapping 讀寫最外層路徑；後端發布驗證也只核對 key 集合，巢狀資料可能讀不到或寫成未宣告的頂層欄位。此與附件路徑 BUG-120 不同，屬靜態推論，未用瀏覽器表單重現。下一步續查第 13 單元其他 Form.io Schema／Runtime 交界，再查第 12 單元發布邊界。目前共 176 項，第 13 單元 28 項，下一編號 BUG-177。

> 2026-10-04 表單 Schema／Runtime 續查：第 13 單元新增 [BUG-177](bug-audit/13-form-design-attachments.md)：後端共用送出驗證器沒有讀取 Form.io 固定選項值域，`select`／`radio`／`selectboxes` 欄位可由直接 API 提交畫面未提供的值；這是內建選項驗證缺口，與 BUG-156 的自訂規則執行不同。此為靜態推論，未以現有表單送出非法選項重現。Container／Grid 資料結構與非物件列的拒絕已有程式及單元測試，未沿用規格舊段落的歷史缺陷描述。下一步查第 12 單元 BPMN 發布與版本邊界。目前共 177 項，第 13 單元 29 項，下一編號 BUG-178。

盤查日期：2026-10-01。範圍：Java、Vue、MariaDB。只記錄盤查結果，不修改程式或資料。`[ ]` 表示尚未完成該單元的完整盤查；`[x]` 表示已完成。缺少可重現資料時，以程式碼推論標明。

## 下次接續盤查：先讀這一節

- **目前進度（2026-10-03）：**27 個功能單元已建清單；BUG-001 至 BUG-149 共 149 項，編號依發現順序而非單元順序。01～11 已完成規格、程式、Vue 與現有資料的靜態／唯讀盤查；其餘 16 個單元仍為 `[ ]`。`[x]` 不表示所有缺陷已修復，無可用資料的情境仍須保留重現限制。此檔目前是 Git 未追蹤檔案（`?? BUG_AUDIT.md`），**請保留並直接續寫，不要刪除或從頭重建**。
> 2026-10-04 BPMN Start Event 續查：第 12 單元新增 [BUG-178](bug-audit/12-workflow-design-resolver.md)：設計 XML 的 Start Event 可帶 `flowable:async`，自有驗證器只檢查元素名稱，發布轉換也未明確清除；可能改變起單交易時序。這是靜態推論，尚未手工部署驗證 Flowable converter 與 Runtime。Gateway 混用條件、欄位掃描、User Task 屬性及連通性缺口已有 BUG-135～137／145，未重複列案。下一步查第 12 單元版本引用及其他事件／流程屬性，再回第 13／16／21 單元。目前共 178 項，第 12 單元 35 項，下一編號 BUG-179。

> 2026-10-04 表單停用續查：第 13 單元新增 [BUG-179](bug-audit/13-form-design-attachments.md)：表單主檔停用後起單頁因主檔狀態拒絕載入，但直接送單僅驗證表單版本仍為 PUBLISHED，未檢查主檔 INACTIVE，可繞過停用。與建草稿造成主檔 DRAFT 的 BUG-108 不同；既有待辦依固定版本讀取表單，不列為同一問題。此為靜態推論，未停用現有表單或送單。下一步查第 13 單元停用與發布引用、第 12 單元其餘 Resolver 邊界。目前共 179 項，第 13 單元 30 項，下一編號 BUG-180。

> 2026-10-04 ORG_DUTY Resolver 續查：第 12 單元新增 [BUG-180](bug-audit/12-workflow-design-resolver.md)：職務擔任者查詢沒有排序，候選人達 `MAX_RESULTS` 即截斷，多人有效時入選帳號可能不穩定。已核對 Java Resolver 與 Employee Duty Mapper；與群組同優先序的 BUG-148 不同。尚未以多人職務資料執行 Preview。下一步查 `ORG_TITLE`／其他候選截斷和 Fallback 整合，避免重複列案。目前共 180 項，第 12 單元 36 項，下一編號 BUG-181。

> 2026-10-04 核決條件缺值續查：第 12 單元新增 [BUG-181](bug-audit/12-workflow-design-resolver.md)：條件欄位缺失時，`NE`／`NOT_IN` 可判為命中，數值比較則丟例外，讓後續核決規則與 Fallback 無法處理；缺值語意需確認。已核對評估器與 Resolver，未以缺值表單執行 Preview／正式派送。多條規則依順序處理、候選去重與 `STOP_AFTER_APPROVAL` 的已知語意沒有另列問題。下一步查核決權限條件欄位的發布驗證及 Vue 編輯器，再回第 12／13 單元其他路徑。目前共 181 項，第 12 單元 37 項，下一編號 BUG-182。

> 2026-10-04 核決權限欄位路徑續查：第 12 單元新增 [BUG-182](bug-audit/12-workflow-design-resolver.md)：Vue 編輯器列出巢狀欄位時只保存 `form.<子 key>`，Runtime 依完整路徑讀值，Container／Grid 子欄位可能讀不到；與第 13 單元 Data Action Mapping 的 BUG-176 不同。後端只驗證條件欄位非空，尚未對綁定表單做路徑檢查。未以巢狀表單 Preview 重現。下一步查核決權限條件的型別驗證與發布引用，再回第 13 單元其他巢狀欄位路徑。目前共 182 項，第 12 單元 38 項，下一編號 BUG-183。

> 2026-10-04 核決條件編輯續查：第 12 單元新增 [BUG-183](bug-audit/12-workflow-design-resolver.md)：從單值運算子切到 `IN`／`NOT_IN` 只變更運算子，原比較值仍為純量；畫面可直接按儲存，但後端要求陣列而拒絕。再輸入一次比較值才會轉成陣列。未在瀏覽器重現，從清單切回單值的 Runtime 語意也未驗證。下一步查核決權限編輯器的數字清單與型別處理、其他 Resolver 發布邊界。目前共 183 項，第 12 單元 39 項，下一編號 BUG-184。

> 2026-10-04 數字清單條件續查：第 12 單元新增 [BUG-184](bug-audit/12-workflow-design-resolver.md)：Vue 的 `IN`／`NOT_IN` 清單輸入一律產生字串陣列，後端對數值表單欄位則呼叫清單項目的 `decimalValue()`，兩端型別契約不一致。已核對本機 Jackson 3.1.2／3.1.4 原始碼：文字節點 `decimalValue()` 拋型別例外，後端捕捉後使數值 `IN` 不命中、`NOT_IN` 命中；尚未執行端到端 Preview；與 BUG-183 切換運算子後殘留舊值不同。下一步查核決權限數值比較預期值型別與測試覆蓋，再續查第 12／13 單元其他邊界。目前共 184 項，第 12 單元 40 項，下一編號 BUG-185。

> 2026-10-04 核決數值比較續查：第 12 單元新增 [BUG-185](bug-audit/12-workflow-design-resolver.md)：`GT`／`GTE`／`LT`／`LTE` 可把文字比較值存入規則，Runtime 對 Jackson 文字節點執行 `decimalValue()` 時拋錯，中斷核決解析。與 BUG-181 的實際欄位缺值不同；已核對本機 Jackson 3.1.2／3.1.4 原始碼，尚未執行端到端 Preview。下一步查其他 Resolver、核決權限編輯器與發布關聯的剩餘邊界。目前共 185 項，第 12 單元 41 項，下一編號 BUG-186。

- **下次進入的起點：**已確認附件暫存檔與送單欄位缺少對照驗證（BUG-115）；已確認上傳與刪除暫存檔的交易補償缺口（BUG-116／117）。已確認上傳與送單共用 Session 的並發缺口（BUG-118）。已確認正式附件跨 UTC 月界可能無法下載（BUG-119）；前端正常上傳／刪除會同步附件 ID 至 submission。已確認巢狀附件欄位的前端資料路徑問題（BUG-120）。正式附件刪除缺少欄位修改權限驗證記於 BUG-121；過期清理的刪檔回滾風險併入 BUG-117。已確認送單跨過 Session 到期時間時可能與清理排程競爭（BUG-122）；申請詳情刪除正式附件後的表單呈現不同步記於 BUG-123；附件 File Types 無效值回退成全部允許類型記於 BUG-124；附件單檔配置超過全域 8 MB 時前後端顯示與實際儲存不一致記於 BUG-125；單檔欄位可接受多個附件記於 BUG-126；上傳中離頁提醒缺口已載於附件規格第 169 行，不重複列 bug。同欄位並發上傳可超過件數／總容量上限記於 BUG-127；正式綁定缺少實體 SHA-256 複驗記於 BUG-128。上傳中切換表單可讓舊回應寫入新表單狀態記於 BUG-129；逾期 Session 直接送單失敗記於 BUG-130；送出按鈕未等待上傳完成的前端路徑已補入 BUG-118。上傳網路例外缺少畫面錯誤提示記於 BUG-131；MariaDB 唯讀現況為正式附件 0、暫存檔明細 0、Session 13（其中 1 筆到期仍 OPEN，且無檔案），尚無附件可重現。Upload Session 建立拋錯可跳過表單 Script／Data Action 掛載記於 BUG-132；本輪將建立 Upload Session 期間切換表單的競態併入 BUG-129，並核對附件 Session／檔案 Mapper 的篩選條件。12 單元續查發布驗證報告與 Resolver Preview，新增 BUG-133／134；MariaDB 唯讀確認流程版本仍為 4 筆 PUBLISHED、0 筆 DRAFT。下輪先補查 **12 流程設計、版本、發布與 Resolver（`FM_PROG004D0001`）**尚未覆蓋的版本與發布路徑；13 單元維持部分盤查。12 單元目前已有 BUG-013、BUG-014、BUG-085～BUG-107。核決權限內部目標已併入 BUG-014／085／087，Gateway 多表單檢查記於 BUG-094，發布後權限規則漂移記於 BUG-095；已核對版本建立複製、發布退役、起單政策，以及主要／Fallback／核決權限內部群組的派送模式驗證；內部群組缺口已併入 BUG-102，總數不變。另確認既有流程實例按版本號讀取 Task Policy／Assignment Rule，不因版本退役改用新規則；版本建立併發仍有資料庫唯一鍵保護，但未做實際競態測試。BUG-103 已擴及 `FORM_ACCOUNT_FIELD_MANAGER`；MariaDB 有 109 筆 ACTIVE 任職使用 `ORG_HEAD`／`PARENT_HEAD` 且主管任職 ID 為空，已發布流程另有 `DIRECT_MANAGER` 2 筆與 `FORM_ACCOUNT_FIELD_MANAGER` 1 筆 ACTIVE 規則。尚未以實際任務確認命中情形。本輪另核對 `ORG_TITLE`／`ORG_DUTY` 的主檔、任職、員工有效性篩選，以及父部門／根部門主管與 `LEVEL_HEAD_CHAIN` 的解析；可確認的第一順位主管失效問題已在 BUG-089，未新增重複條目。已核對版本切換、預覽與完整版本檔匯入／匯出：預覽明確使用已儲存規則；匯入有同 Tenant／流程代碼、草稿狀態、XML／節點引用、Groovy 綁定檢查，並先在獨立畫布驗證；未新增匯入缺陷。版本切換未保存未儲存圖形記為 BUG-104。本輪發現停用主檔可經建立／發布版本重新上架（BUG-105）。分類停用後清單與直接起單／發布的檢查不一致記為 BUG-106。續查確認 `draft()` 只檢查版本狀態，主檔停用後仍可保存既有草稿；發布會將主檔改回 PUBLISHED，這已涵蓋於 BUG-105，未另列重複問題。職稱、職務及簽核層級參照的有效期間漏檢記為 BUG-107。MariaDB 唯讀確認目前 ACTIVE 但期間無效的職稱、職務、簽核層級及核決權限主檔均為 0 筆；簽核群組主檔無期間欄，成員空缺已記於 BUG-014。續查發現表單新草稿會使已發布流程無法載入舊發布表單版本，已記於 13 單元 BUG-108；本輪確認影響申請中心與起單頁載入，直接送單及既有待辦未因同一主檔狀態檢查受阻。表單新版發布後舊版退役、尚未更新的流程無法起單另記於 13 單元 BUG-109。MariaDB 唯讀確認目前 4 筆表單均為 PUBLISHED，已發布流程的 42 筆 Task Form Rule 也全數綁定 PUBLISHED 表單版本，未現成重現 BUG-108／109；流程設計器可從發布表單選單改綁新版，後端儲存會重新驗證版本。表單草稿並行發布缺少一次性狀態轉換保護記於 13 單元 BUG-110；表單設計器分步儲存先重載畫面造成編輯流失記於 BUG-111。表單預覽的 beforeSubmit 後漏掉正式起單具有的再次欄位驗證，記於 BUG-112。Task Action Hook 發布未驗證 requestMapping 來源路徑記於 BUG-113。Custom JavaScript 的 onFieldChange 單槽待處理事件可能漏掉中間異動，記於 BUG-114。附件刪除與讀取權限已檢視，membership 缺口沿用 BUG-015；本輪確認 Runtime 仍按流程版本號讀取 Task Policy／Assignment Rule；重複簽核預覽差異併入 BUG-134，現有多規則節點 0 筆。BPMN User Task 派送擴充屬性缺少發布檢查記於 BUG-135；Gateway 條件混用空白繞過記於 BUG-136；全 XML 字串掃描誤把說明欄位視為 Gateway 參照記於 BUG-137。ALL／SEQUENTIAL 多人簽核解析失敗不建立 Assignment Incident 記於 BUG-138（靜態推論，未實際執行）。循序多人簽核二次解析與原清單不一致記於 BUG-139（靜態推論，未實際執行）。起單冪等重送會先驗證目前流程／表單／授權狀態，導致原結果可能無法回傳，記於 16 單元 BUG-140；跨流程共用表單時冪等請求漏比對流程 ID 記於 BUG-141；不同附件 Session 可被視為同一冪等請求記於 BUG-142，均為靜態推論。後續 User Task 表單被列為起單表單並可直接送出記於 16 單元 BUG-143；現有流程只有單一表單，尚未重現。起單頁載入舊版後若新版發布，送單可能無提示改用新版流程，記於 16 單元 BUG-144（靜態推論）。BPMN 設計驗證缺少連通性檢查記於 12 單元 BUG-145，尚未確認 Flowable 部署結果；本輪亦將 User Task 的執行屬性缺口併入 BUG-135，未新增重複條目。多人簽核通知會對每個 Task 傳入整組人員，記於 21 單元 BUG-146；未執行通知重現。本輪核對站內與 Email 通知的固定 ID、`insertIfAbsent()` 去重路徑，未發現 Listener 重入導致重複建立通知的獨立缺陷；另核對 SLA 排程實際 Task 收件人與平行加簽回查路徑，未新增條目，待驗證大量任務及併發分頁。提前提醒 0 小時無法產生即將到期通知記於 21 單元 BUG-147（靜態條件推論）。本輪核對 Email 寄送失敗／成功與同步排程的狀態路徑，未發現超出 BUG-026 的新獨立缺陷；後續可查長期 PENDING／失敗通知的資料現況與重試邊界。本輪續核 Resolver／核決權限規則排序及 MAX_RESULTS 截斷；群組成員同優先序時的循序簽核順序不穩定記於 12 單元 BUG-148（靜態推論）。設計器已明定 STOP_AFTER_APPROVAL 是條件符合後停止後續規則，程式語意一致，不另列缺陷；目標失效情境仍未實際重現。部門主管同優先序時 Resolver 取第一筆不穩定記於 12 單元 BUG-149（靜態推論），與 BUG-148 的群組循序簽核排序不同。下一輪續查 12 單元 BPMN XML 其餘發布邊界、版本引用與多人簽核失敗路徑；13 單元維持部分盤查；12 單元仍需補完整盤查。不要因局部檢查把 12 標為完成。
- **11 完成範圍（2026-10-03）：**已核對工作代理規格、README 操作說明、Controller 全端點、Logic 建立／更新／停用與重疊驗證、Mapper、Vue 清單及表單，以及 Task Runtime 的代理與再次轉代理判斷、Scope Evaluator、組織健檢代理循環檢查。MariaDB 唯讀確認代理表結構、索引及現況。該單元新增 BUG-083、BUG-084。
- **重現限制：**現有 `fm_workflow_delegation` 為 0 筆，且只有單一 Tenant；BUG-011、BUG-083、BUG-084 及 10 單元的多期間成員／未來生效成員／群組派送方式變更等異動情境尚無現成資料重現。先完成靜態與唯讀盤查，不重做 01～11 已完成範圍。
- **02 完成範圍（2026-10-02）：**已核對 README、核心資料模型、帳號與 Role 邊界及 API 規格；Tenant 清單／建立／編輯 Vue 與所有可見操作；`FMPROG001D0001Controller` 全部端點、`FmTenantLogicServiceImpl`、`FmTenantMapper.xml`／`FmTenantAccountMapper.xml`、帳號新增所用 `TbAccountMapper.xml`、兩張 Tenant 表與 `tb_account` 的 DDL／索引及現有資料；並對照 Token store、預設 Tenant、單據取號、待辦／我的申請／通知 Runtime 入口。`fm_tenant` 1 筆且 ACTIVE；`fm_tenant_account` 140 筆，全部 ACTIVE，倒置期間、孤兒 membership、停用帳號搭配 ACTIVE membership 均為 0；140 筆 membership 均有 Program Role。新增帳號程式會填齊 `tb_account` 必要欄位，Program Role 須另外配置，符合規格。現行 `FM_PROG001D0001*` Controller 權限資料只授予 `BPM_ADMIN`；若角色授權變更需重查管理 API 的跨 Tenant 行為。此完成標記限靜態程式與唯讀資料盤查；沒有第二個或停用 Tenant、也未建立新帳號，因此跨 Tenant、停用及新帳號瀏覽器 E2E 尚無資料重現，相關 BUG 依程式推論並保留限制。
- **其他已查且不必重做的證據：**各單元下方 BUG 條目已記錄 01–24、27 的發現；25 已看 `FmDataActionTaskDelegate`、`FmGroovyTaskDelegate`、`FmGroovyRuntimeCommitLogicServiceImpl`，26 已看 `FmDocumentNumberService`、`FmDocumentSequenceMapper.xml`，均仍須補查其餘路徑。18 已看 `FmRequestTrackingLogicServiceImpl`、`FmProcessRuntimeController` 的 mine API、`frontend-v-nx/pages/requests/[id].vue`、README 第 19 節與 API 規格第 8 章。
- **歷史接手指示：**原先從 13 單元附件流程接續、再從 14 單元收尾的安排均已執行；現以本文件開頭的 2026-10-05 結論為準。
- **已查範圍與限制：**13 單元本輪續查 Upload Session 上傳／刪除、檔案儲存、綁定與兩種清理排程，記錄 BUG-115～132；規格已明列上傳頻率計數非原子，未重複列 bug。尚未以失敗注入、並發送單、Session 到期競態或 UTC 月界送單重現。前端最外層附件正常上傳／刪除會把附件 ID 陣列同步到 submission；巢狀欄位路徑問題見 BUG-120。13 單元已核對表單主檔／版本建立、草稿儲存、發布、設計器預覽、`beforeSubmit`、Task Action Hook 發布與執行、Custom JavaScript 的 `onFieldChange` 事件，以及正式附件讀取／刪除的主要權限路徑。BUG-108～114 是靜態程式推論，未建立草稿、並發發布、瀏覽器快速輸入或故障請求重現；BUG-015 的正式附件資料仍為 0 筆。本輪 MariaDB 唯讀確認 `fm_attachment` 0、`fm_attachment_upload_file` 0、`fm_attachment_upload_session` 13（12 EXPIRED、1 OPEN 且已到期），`fm_form_data` 0；無附件資料可重現。先前 MariaDB 唯讀結果：4 筆表單主檔／版本均為 PUBLISHED，已發布流程 42 筆 Task Form Rule 全部綁定 PUBLISHED 表單版本。資料現況可能變動，需查現況時重新唯讀確認。
- **2026-10-04 收斂快照：**當時決定停止追新 BUG，並先收尾 14 DataSource Pool；該單元及其餘功能單元現已完成靜態／既有唯讀資料盤查。185 條均為原始觀察；後續依 11 個工作包定向驗證或修正。
- **編號封存規則（2026-10-04 更新）：**BUG-001～185 保留作原始觀察索引；目前不新增 BUG-186。新工作先進入收斂工作包，證據足夠且根因獨立時才另編號。
- **資料庫與修改限制：**依 `backend/doc/21-MariaDB操作規範.md` 使用 MariaDB `mysql`／`mariadb` 指令，只執行 `SELECT`／`SHOW`。不要修改 Java、Vue、SQL 或資料庫；只更新本文件。不要把資料庫密碼寫入本文件或回覆。
- **收斂後記錄規則：**先讀 `bug-audit/00-convergence.md`，只更新正在處理的工作包、驗證結果與相關原條目；不要為每個相鄰風險增加新編號。只有該單元規格、Java、Vue、MariaDB 相關路徑均查完，才可標 `[x]`。

### 2026-10-03 接手快照（歷史紀錄，勿作為下一步指示）

- **進度與檔案（2026-10-04 更新）：**已封存 BUG-001～BUG-185，共 185 條觀察；目前不新增編號。01～11 單元已完成靜態／唯讀盤查；12～27 仍為 `[ ]`。12 單元 42 項、13 單元 30 項、14 單元 5 項、15 單元 7 項、16 單元 7 項、17 單元 2 項、21 單元 4 項、23 單元 11 項、24 單元 7 項、25 單元 4 項、26 單元 2 項。`BUG_AUDIT.md` 是 Git 未追蹤檔（`??`），請在原檔接續，不要重建、刪除或標記已完成。
- **最近已查：**12 單元 BPMN 發布驗證、Resolver Preview、Multi-instance 派送、群組及部門主管排序；13 單元表單版本、Custom JavaScript、附件上傳／綁定；16 單元正式起單與冪等比對；21 單元通知、Email、Task SLA。最近新增 BUG-133～149，詳細證據在各單元條目；同原因的缺口已併入原 BUG，尤其 User Task 執行屬性併入 BUG-135。`STOP_AFTER_APPROVAL` 的「符合後停止」與設計器說明一致，不另列缺陷。
- **2026-10-03 歷史下一步：**當時預定先盤查 14 DataSource Pool；現已完成。若進入修復，依[11 個優先工作包](bug-audit/00-convergence.md)從 P0 的 BUG-037／165／169 開始；其餘條目保留為證據，不再探索式盤查。
- **重現限制：**BUG-133～149 主要由程式與規格靜態比對，尚未以流程部署、並發、瀏覽器或故障注入重現。先前唯讀 MariaDB 顯示流程版本 4 筆 PUBLISHED／0 筆 DRAFT；正式附件 0、暫存檔 0、Upload Session 13（12 EXPIRED、1 OPEN 但已到期）、表單資料 0。這是查詢當時的快照，後續使用前需重新唯讀確認。
- **操作規則：**盤查紀錄寫入對應 `bug-audit/NN-*.md` 並同步更新本索引的完成狀態；資料庫查詢遵守 `backend/doc/21-MariaDB操作規範.md`，只用 CLI 執行 `SELECT`／`SHOW`。既有未重現的影響須標明靜態推論，不把假設寫成已驗證結果；不以新增 BUG 編號推進盤查。

## 功能單元清單與進度

依 `README.md` 程式操作手冊及 `backend/doc/*.md` 的功能規格排序。下列「項數」按各功能檔 `### BUG-` 標題實際計算，表示待確認條目數，不等於已在 Runtime 重現或修正的缺陷數；歷史進度段落保留當時快照。第 12 單元 42 條分別涉及流程主檔／版本與設計器 8、BPMN／Gateway／發布 8、起單政策 2、Task Policy／多人簽核 4、Resolver／核決條件 20。

- [x] 01 登入、身分與 Tenant 邊界 — 靜態／唯讀盤查完成；已記錄 7 項條目，實際停用／跨 Tenant 情境待隔離資料重現
- [x] 02 Tenant 與 membership（`FM_PROG001D0001`）— 靜態／唯讀盤查完成；已記錄 9 項問題，實際多 Tenant／停用情境待隔離資料重現
- [x] 03 員工與任職（`FM_PROG002D0001`）— 靜態／唯讀盤查完成；已記錄 5 項條目，異動情境待隔離資料重現
- [x] 04 部門與組織樹（`FM_PROG002D0002`）— 靜態／唯讀盤查完成；已記錄 6 項條目，異動及併發情境待隔離資料重現
- [x] 05 組織簽核層級（`FM_PROG002D0003`）— 靜態／唯讀盤查完成；已記錄 6 項條目，異動情境待隔離資料重現
- [x] 06 組織職稱（`FM_PROG002D0004`）— 靜態／唯讀盤查完成；已記錄 3 項問題，異動情境待隔離資料重現
- [x] 07 部門主管（`FM_PROG002D0005`）— 靜態／唯讀盤查完成；已記錄 5 項條目，異動情境待隔離資料重現
- [x] 08 部門職務（`FM_PROG002D0006`）— 靜態／唯讀盤查完成；已記錄 6 項條目，現有資料為空且異動情境待重現
- [x] 09 組織人員資料檢查（`FM_PROG002D0007Q`）— 靜態／唯讀盤查完成；已記錄 3 項問題，代理及失效員工情境待隔離資料重現
- [x] 10 簽核群組（`FM_PROG003D0001`）— 靜態／唯讀盤查完成；已記錄 6 項條目，異動情境待隔離資料重現
- [x] 11 工作代理（`FM_PROG003D0002`）— 靜態／唯讀盤查完成；已記錄 3 項問題，跨 Tenant 與併發情境待隔離資料重現
- [x] 12 流程設計、版本、發布與 Resolver（`FM_PROG004D0001`）— 靜態／唯讀盤查完成；42 項既有觀察，逐型 Runtime 待重現
- [x] 13 表單設計、版本、Custom JavaScript 與附件（`FM_PROG005D0001`）— 靜態／唯讀盤查完成；30 項既有觀察，附件與併發情境待重現
- [x] 14 DataSource Pool（`FM_PROG006D0001`）— 靜態／唯讀盤查完成；5 項既有觀察，隔離情境仍待重現
- [x] 15 Data Action（`FM_PROG006D0002`）— 靜態／唯讀盤查完成；7 項既有觀察，執行情境待重現
- [x] 16 Workspace、申請中心與正式起單 — 靜態／唯讀盤查完成；7 項既有觀察，正式送單待重現
- [x] 17 我的待辦、簽核動作、退回、加簽與改派 — 靜態／唯讀盤查完成；2 項既有觀察，Runtime 情境待重現
- [x] 18 我的申請、撤回與取消 — 靜態／唯讀盤查完成；1 項既有觀察，撤回契約待決
- [x] 19 流程進度、表單快照與不可變稽核 — 靜態／唯讀盤查完成；1 項既有觀察，資料異動未重現
- [x] 20 異常處理與營運流程管理 — 靜態／唯讀盤查完成；1 項既有觀察，跨 Tenant 情境待重現
- [x] 21 站內通知、Email 與 Task SLA — 靜態／唯讀盤查完成；4 項既有觀察，排程邊界待重現
- [x] 22 營運報表 — 靜態／唯讀盤查完成；1 項既有觀察，實際統計待重現
- [x] 23 AI Provider 與簽核解說（`FM_PROG010D0001`）— 靜態／唯讀盤查完成；11 項既有觀察，外部服務待重現
- [x] 24 外部系統 API Client 與流程拋單（`FM_PROG010D0002`、`FM_PROG010D0003`）— 靜態／唯讀盤查完成；7 項既有觀察，外部請求待重現
- [x] 25 System Task、Data Action Task 與 Groovy worker — 靜態／唯讀盤查完成；4 項既有觀察，交易與故障注入待重現
- [x] 26 共用單據編號與預設表單流程配置 — 靜態／唯讀盤查完成；2 項既有觀察，跨週期及併發待重現
- [x] 27 安全權限、部署設定與資料庫遷移 — 靜態／唯讀盤查完成；1 項既有觀察，實際部署待驗收

## 功能項目文件

- [01 登入、身分與 Tenant 邊界 — 靜態／唯讀盤查完成](bug-audit/01-identity-tenant-boundary.md)
- [02 Tenant 與 membership（`FM_PROG001D0001`）— 靜態／唯讀盤查完成](bug-audit/02-tenant-membership.md)
- [03 員工與任職（`FM_PROG002D0001`）— 靜態／唯讀盤查完成](bug-audit/03-employees-employment.md)
- [04 部門與組織樹（`FM_PROG002D0002`）— 靜態／唯讀盤查完成](bug-audit/04-departments-organization.md)
- [05 組織簽核層級（`FM_PROG002D0003`）— 靜態／唯讀盤查完成](bug-audit/05-approval-levels.md)
- [06 組織職稱（`FM_PROG002D0004`）— 靜態／唯讀盤查完成](bug-audit/06-organization-titles.md)
- [07 部門主管（`FM_PROG002D0005`）— 靜態／唯讀盤查完成](bug-audit/07-department-managers.md)
- [08 部門職務（`FM_PROG002D0006`）— 靜態／唯讀盤查完成](bug-audit/08-department-duties.md)
- [09 組織人員資料檢查（`FM_PROG002D0007Q`）— 靜態／唯讀盤查完成](bug-audit/09-organization-health.md)
- [10 簽核群組（`FM_PROG003D0001`）— 靜態／唯讀盤查完成](bug-audit/10-approval-groups.md)
- [11 工作代理（`FM_PROG003D0002`）— 靜態／唯讀盤查完成](bug-audit/11-work-delegation.md)
- [12 流程設計、版本、發布與 Resolver（`FM_PROG004D0001`）— 靜態／唯讀盤查完成](bug-audit/12-workflow-design-resolver.md)
- [13 表單設計、版本、Custom JavaScript 與附件（`FM_PROG005D0001`）— 靜態／唯讀盤查完成](bug-audit/13-form-design-attachments.md)
- [14 DataSource Pool（`FM_PROG006D0001`）— 靜態／唯讀盤查完成](bug-audit/14-datasource-pool.md)
- [15 Data Action（`FM_PROG006D0002`）— 靜態／唯讀盤查完成](bug-audit/15-data-action.md)
- [16 Workspace、申請中心與正式起單 — 靜態／唯讀盤查完成](bug-audit/16-workspace-requests.md)
- [17 我的待辦、簽核動作、退回、加簽與改派 — 靜態／唯讀盤查完成](bug-audit/17-task-approval.md)
- [18 我的申請、撤回與取消 — 靜態／唯讀盤查完成](bug-audit/18-my-requests.md)
- [19 流程進度、表單快照與不可變稽核 — 靜態／唯讀盤查完成](bug-audit/19-audit-snapshots.md)
- [20 異常處理與營運流程管理 — 靜態／唯讀盤查完成](bug-audit/20-operations-incidents.md)
- [21 站內通知、Email 與 Task SLA — 靜態／唯讀盤查完成](bug-audit/21-notifications-email-sla.md)
- [22 營運報表 — 靜態／唯讀盤查完成](bug-audit/22-reports.md)
- [23 AI Provider 與簽核解說（`FM_PROG010D0001`）— 靜態／唯讀盤查完成](bug-audit/23-ai-provider.md)
- [24 外部系統 API Client 與流程拋單（`FM_PROG010D0002`、`FM_PROG010D0003`）— 靜態／唯讀盤查完成](bug-audit/24-external-api.md)
- [25 System Task、Data Action Task 與 Groovy worker — 靜態／唯讀盤查完成](bug-audit/25-system-tasks.md)
- [26 共用單據編號與預設表單流程配置 — 靜態／唯讀盤查完成](bug-audit/26-document-numbers.md)
- [27 安全權限、部署設定與資料庫遷移 — 靜態／唯讀盤查完成](bug-audit/27-security-deployment.md)
