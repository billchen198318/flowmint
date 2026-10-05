## 23 AI Provider 與簽核解說（`FM_PROG010D0001`）— 靜態／唯讀盤查完成

2026-10-05 收尾：已對照第 29 章、Provider 管理 Controller 的清單／建立／載入／更新／停用／測試及 Tenant 選項、管理 Vue、Task 分析 Runtime 與現有 Provider／Cipher／Client／內容雜湊及 Mapper 測試。十一項既有觀察保留各自的程式路徑證據；其中費用控制及強制重算見 BUG-154。先前唯讀資料顯示 Provider、Analysis、Access 均為 0；未讀取金鑰，也未呼叫外部 AI 服務或重現真實費用情境。

2026-10-04 續查 `backend/doc/29-AI簽核解說精靈規劃.md` 的費用控制要求及 `FmAiTaskRuntimeLogicServiceImpl.analyze()`。MariaDB 唯讀確認 Provider、Analysis、Access 三表均為 0 筆；未呼叫外部 AI Provider。

### BUG-154：強制重新分析缺少冷卻時間與每日額度限制

- 嚴重度：中。性質：AI Provider 呼叫費用與濫用控制；目前無 Provider 設定及分析紀錄，屬程式碼推論。
- 預期：`backend/doc/29-AI簽核解說精靈規劃.md` 第 12 節要求 `forceRefresh` 受冷卻時間與每日額度限制，第 15.4 節要求每人、Task、Tenant 的限流與每日額度。
- 實際：`FmAiTaskRuntimeLogicServiceImpl.analyze()` 在 `forceRefresh=true` 時跳過成功結果快取，建立下一代分析並呼叫 Provider；路徑中沒有查詢最近強制刷新時間、每日用量或保留額度。只要 Task 授權仍有效，使用者可重複發起強制分析，產生多筆付費呼叫。資料庫的 Generation 唯一鍵僅避免相同代次衝突，並不限制後續代次數量。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAiTaskRuntimeLogicServiceImpl.java` 的 `analyze()`、`startWithRetry()`；`backend/doc/29-AI簽核解說精靈規劃.md` 第 7、12、15.4 節。尚未在真實 Provider 上重現費用影響。

### BUG-027：AI Access Ledger 寫入失敗時，分析與快取讀取仍可成功

- 嚴重度：中。性質：Java 稽核完整性；現有分析與 Access 資料都是 0 筆，未用故障注入重現。
- 預期：`backend/doc/29-AI簽核解說精靈規劃.md` 第 119 行要求每次點擊、快取命中、開始分析、失敗或讀取結果都建立獨立 Access 紀錄，藉此記錄合法使用者的存取事實。
- 實際：`FmAiTaskRuntimeLogicServiceImpl.record()` 捕捉 `accessLedger.record()` 的所有 `Exception` 後直接忽略；`analyze()` 在 `CLICK`、`CACHE_HIT`、`START`、`SUCCEEDED`／`FAILED` 等位置都呼叫此方法。若 `fm_ai_analysis_access` 寫入失敗，快取結果或新分析仍可能成功回傳且沒有相應存取紀錄；由於 `FmAiAccessLedger.record()` 使用獨立交易，分析交易不會替它補上紀錄。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAiTaskRuntimeLogicServiceImpl.java` 的 `analyze()`、`record()`；`backend/app/src/main/java/org/qifu/fm/domain/ai/FmAiAccessLedger.java` 的 `REQUIRES_NEW` 寫入。MariaDB 唯讀查詢顯示 Provider、分析及 Access 紀錄均 0 筆。

### BUG-028：Container 內的表單欄位不會正確進入 AI 分析 Context

- 嚴重度：中。性質：Java 表單資料轉換；目前沒有正式表單資料可重現 AI 請求。
- 預期：`backend/doc/29-AI簽核解說精靈規劃.md` 第 157 行要求 Container 等巢狀元件受控遞迴，依已發布 Schema 提取欄位標題及值。
- 實際：`FmAiContextBuilder.collectComponents()` 遇到 Container 會呼叫 `collectNested()`；後者遞迴子元件時仍傳入原來的 `data` Map，沒有先取 `data.get(containerKey)`。Form.io Container 的子值存在 Container key 對應的物件內，子欄位不在外層 Map 時，`data.containsKey(childKey)` 為 false，分析輸入省略這些欄位。這也會影響 Content Hash，因為 hash 使用已漏欄位的 Context。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/ai/FmAiContextBuilder.java` 的 `collectComponents()`、`collectNested()`；`backend/app/src/test/java/org/qifu/fm/domain/runtime/FmFormSubmissionValidatorTest.java` 的 Container 巢狀資料契約；`FmAiContentHashService.java`。MariaDB 唯讀查詢顯示 AI 分析 0 筆、表單資料 0 筆，未做寫入重現。

### BUG-029：OpenAI 預設 Base URL 與分析端點不符，正確端點又無法通過連線測試

- 嚴重度：高。性質：Vue 預設設定與 Java Provider Client／連線測試契約；目前 Provider 0 筆，未用真實 API Key 呼叫外部服務。
- 預期：管理頁預設的 OpenAI Base URL 應能通過連線測試並供分析使用；連線測試與分析應依同一種 Base URL 語意組成各自的端點。
- 實際：`AiProviderForm.vue` 預設 OpenAI Base URL 為 `https://api.openai.com/v1`。`FmOpenAiProviderClient.buildRequest()` 直接對 `config.baseUrl()` POST Responses API 內容，因此預設設定會送到 `/v1`，不是 `/v1/responses`。若管理者改填 `/v1/responses` 以符合分析端點（現有 Client 測試即採此值），`FmAiProviderConnectionTester.request()` 又無條件接上 `/models`，連線測試變成 `/v1/responses/models`。兩條路徑對 Base URL 的定義互相衝突。
- 證據：`frontend-v-nx/pages/fm_prog010d0001/components/AiProviderForm.vue` 的 `defaults.OPENAI.baseUrl`；`backend/app/src/main/java/org/qifu/fm/domain/ai/FmOpenAiProviderClient.java` 的 `buildRequest()`；`FmAiProviderConnectionTester.java` 的 `request()`；`backend/app/src/test/java/org/qifu/fm/domain/ai/FmOpenAiProviderClientTest.java` 斷言 `/v1/responses`。MariaDB 唯讀查詢顯示 `fm_ai_provider` 0 筆，未做外部請求重現。

### BUG-030：不同 AI Provider 的分析可能共用同一 Content Hash 快取

- 嚴重度：高。性質：Java 快取鍵與 MariaDB 查詢；目前 AI Provider 與分析資料 0 筆，尚無現成跨 Provider 重現案例。
- 預期：使用者選擇另一個 Provider 或模型時，應由該 Provider 產生分析，或只命中同一 Provider／模型設定的快取；不得把其他 Provider 的結果當成本次分析。
- 實際：`FmAiContentHashService.hash()` 納入 Tenant、流程、Task、表單 Revision、Context、`providerConfigVersion` 與 Prompt 版本，沒有納入 `providerCode`、`providerType` 或 Model ID。不同 Provider 的設定版本通常都從 1 起算，對同一 Task／Context 會產生相同 Hash。`FmAiTaskRuntimeLogicServiceImpl.analyze()` 用此 Hash 查快取；`FmAiAnalysisMapper.findLatestSucceeded` 只過濾 Tenant、Hash、成功狀態，未過濾 Provider。於是第二個 Provider 的一般分析請求可能直接回傳第一個 Provider 的結果，且回應中的 Provider Code 也顯示第一個 Provider。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/ai/FmAiContentHashService.java` 的 `hash()`；`FmAiTaskRuntimeLogicServiceImpl.java` 的 `analyze()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmAiAnalysisMapper.xml` 的 `findLatestSucceeded`；Provider 建立時 `CONFIG_VERSION=1`。MariaDB 唯讀查詢顯示相關三表目前皆無資料，未修改資料重現。

### BUG-031：DataGrid／EditGrid 原始列資料可能把隱藏欄位送往 AI Provider

- 嚴重度：高。性質：Java AI Context 資料遮罩；目前沒有正式表單資料或 AI 分析可實際重現。
- 預期：`backend/doc/29-AI簽核解說精靈規劃.md` 第 157、161 行要求 DataGrid／EditGrid 受控遞迴，僅按已發布 Schema 輸出允許的欄位，排除 Password、hidden 與未宣告欄位。
- 實際：`FmAiContextBuilder.collectNested()` 對 DataGrid／EditGrid 最後直接把 `data.get(key)` 的整個列資料交給 `safeValue()`，產生一個包含完整表格資料的 Context 欄位。`safeValue()` 對巢狀物件只按欄位名稱是否包含 `password`、`token` 等字串過濾；不檢查子元件的 `hidden`、`type=password` 或是否在 Schema 宣告。例如 DataGrid 中 hidden 的 `privateNote` 欄位不含敏感字串，仍會隨列資料進入送給外部 AI 的 Context。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/ai/FmAiContextBuilder.java` 的 `collectNested()`、`safeValue()`、`excluded()`；`FmAiTaskRuntimeLogicServiceImpl.java` 將此 Context 傳給 Provider Client。MariaDB 唯讀查詢顯示 `fm_form_data` 與 `fm_ai_analysis` 均 0 筆，未發送外部請求。

### BUG-032：簽核歷程超過 50 筆後，最新意見不會進入 AI Context 與快取鍵

- 嚴重度：中。性質：Java 歷程截取與快取；目前 Task Action 0 筆，無法用現有資料重現超過 50 筆的情境。
- 預期：AI 分析應反映目前已保存的簽核意見；若為控制輸入量只取部分歷程，應保留最近的意見，新增意見後 Content Hash 應能改變。
- 實際：`FmTaskRuntimeLogicServiceImpl.actionHistory()` 依 `ACTION_DATE ASC` 回傳舊到新的 Action；`FmAiContextBuilder.build()` 對這個 List 直接 `stream().limit(50)`，只取最早的 50 筆。第 51 筆以後的新 Comment／Reason 不進入 Context，也不影響使用 Context 算出的 Hash；在同一 Task、表單 Revision 未變時，一般請求可能繼續命中加入新意見前的舊分析。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmTaskRuntimeLogicServiceImpl.java` 的 `actionHistory()`；`backend/app/src/main/java/org/qifu/fm/domain/ai/FmAiContextBuilder.java` 的 `build()`；`FmAiContentHashService.java` 的 `hash()`。MariaDB 唯讀查詢顯示 `fm_task_action` 0 筆，未建立重現資料。

### BUG-033：部分 AI Provider 回應大小限制在完整讀入後才檢查，OpenAI 無上限

- 嚴重度：中。性質：Java 外部 HTTP 回應資源保護；`backend/doc/29-AI簽核解說精靈規劃.md` 開頭已明列此未完成項。未連接真實 Provider 重現。
- 預期：Provider Client 應在讀取期間限制回應大小，超限時停止讀取，避免異常或惡意回應占用大量記憶體；同文件第 238 行要求 response size limit。
- 實際：`FmOpenAiProviderClient.analyze()` 使用無上限的 `HttpResponse.BodyHandlers.ofString()`，收到完整內容後直接解析；`FmGeminiProviderClient.analyze()` 也先以 `ofString(UTF_8)` 讀完整回應，再比較 UTF-8 bytes 是否超過 1 MiB。Groq／OpenRouter 共用的 Chat Completions Client 已用 `ofInputStream()` 與 `readNBytes(MAX_RESPONSE_BYTES + 1)` 在讀取時設限，顯示同功能路徑行為不一致。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/ai/FmOpenAiProviderClient.java`、`FmGeminiProviderClient.java`、`FmChatCompletionsProviderClient.java` 的 `analyze()`；`backend/doc/29-AI簽核解說精靈規劃.md` 第 9、238 行。此項屬已知但未落地的保護要求；未發送外部請求或修改資料。

### BUG-034：切換 AI Provider 後仍顯示上一個 Provider 的分析結果

- 嚴重度：低。性質：Vue 畫面狀態；目前 Provider 0 筆，沒有現成資料可做瀏覽器重現。
- 預期：在待辦 AI 面板切換分析來源時，應清除或明確標示既有結果所屬 Provider，避免將先前結果誤認為新選項的分析。
- 實際：`pages/tasks/[id].vue` 的選單直接以 `v-model="selectedAiProviderCode"` 更新選擇；`aiAnalysis` 只在新請求成功時改寫，沒有對選項變更清除結果的 watch，也沒有在結果區顯示 `aiAnalysis.providerCode`。因此選擇另一個 Provider 後、按下重新分析前，畫面仍顯示上一個 Provider 的摘要與建議，卻將新 Provider 顯示為目前選擇。
- 證據：`frontend-v-nx/pages/tasks/[id].vue` 的 `selectedAiProviderCode`、`analyzeWithAi()` 及 AI 面板模板。MariaDB 唯讀查詢顯示 `fm_ai_provider`、`fm_ai_analysis` 均 0 筆，未做瀏覽器實際重現。

### BUG-035：AI 結果驗證器保留未宣告欄位，可能儲存並回傳額外模型輸出

- 嚴重度：中。性質：Java 回應契約與資料保護；目前 AI 分析 0 筆，未用真實 Provider 回應重現。
- 預期：`backend/doc/29-AI簽核解說精靈規劃.md` 第 178 行要求未知欄位忽略或拒絕，超長內容截斷；本地驗證應只保留允許的結構化欄位。
- 實際：`FmAiStructuredResultValidator.validate()` 驗證 `summary`、`keyFacts`、`risks`、`questions`、`recommendation`、`disclaimer` 的形狀與長度，最後直接 `return result`，完全沒有檢查、刪除或限制其他欄位。`FmGroqProviderClient` 以 `json_object` 模式請求，回應可含額外欄位；`FmChatCompletionsProviderClient.parseResponse()` 將驗證器回傳的整份結果交給 Runtime 儲存及回傳。因此模型額外輸出的欄位可繞過已知欄位長度限制並保存在 `fm_ai_analysis.RESULT_CONTENT`。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/ai/FmAiStructuredResultValidator.java` 的 `validate()`；`FmGroqProviderClient.java` 的非 strict 設定；`FmChatCompletionsProviderClient.java` 的 `parseResponse()`；`FmAiTaskRuntimeLogicServiceImpl.java` 的結果儲存。MariaDB 唯讀查詢顯示 `fm_ai_analysis` 0 筆，未呼叫 Provider。

### BUG-036：AI Access 紀錄的 Analysis ID 缺少規格要求的外鍵約束

- 嚴重度：低。性質：MariaDB 資料完整性；目前 Access 紀錄 0 筆，孤兒紀錄 0 筆，尚未發生可觀察的資料異常。
- 預期：`backend/doc/29-AI簽核解說精靈規劃.md` 第 119 行規定 Access 紀錄以外鍵引用 `fm_ai_analysis`；`CLICK` 等尚無 Analysis ID 的紀錄可保持 `NULL`。
- 實際：`fm_ai_analysis_access.ANALYSIS_ID` 可為 `NULL`，有 `(TENANT_ID, ANALYSIS_ID, REQUEST_DATE)` 索引，但 MariaDB `SHOW CREATE TABLE` 顯示沒有指向 `fm_ai_analysis` 的 FOREIGN KEY。資料庫因此容許非空但不存在或跨 Tenant 的 Analysis ID，Access 與分析紀錄的對應只能靠應用程式維持。
- 證據：MariaDB 唯讀 `SHOW CREATE TABLE fm_ai_analysis_access` 及 `SELECT` 孤兒紀錄計數；`backend/doc/29-AI簽核解說精靈規劃.md` 第 119 行。未修改資料驗證資料庫拒絕行為。
