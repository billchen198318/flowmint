## 16 Workspace、申請中心與正式起單 — 靜態／唯讀盤查完成

2026-10-05 收尾：已對照 README／API 規格、`FmProcessRuntimeController` 的 start tenants／applicants／catalog／load／submit，`FmProcessRuntimeLogicServiceImpl` 起單、冪等與版本綁定，以及 Workspace／申請中心／正式起單 Vue。七項既有觀察記錄前後端狀態及正式起單邊界；表單版本生命週期歸第 13 單元，Tenant 停用與 BUG-018 合併進同一工作包。現有流程實例為 0，未執行併發、版本切換或正式送單重現。

### BUG-140：相同冪等鍵重送先驗證目前狀態，可能無法回傳原起單結果

- 嚴重度：中。性質：正式起單冪等回應；未實際執行狀態變更後的重送。
- 預期：同一 Tenant、冪等鍵及相同請求內容的重送應回傳原流程結果；`README.md` 第 685～686 行及 `backend/doc/08-API規格.md` 第 122 行均載明此契約。
- 實際：`FmProcessRuntimeLogicServiceImpl.submit()` 在查詢 `existingSubmission()` 前，先執行目前已發布流程／表單版本、表單 Schema、有效申請人、Tenant membership、代理權及起單政策檢查。原請求成功後，若表單版本退役、流程暫無 PUBLISHED 版本、申請人停用或授權改變，完全相同的重送會在讀取原結果之前被拒絕。原流程已建立且 `existingSubmission()` 可依冪等鍵找到，但無法履行相同請求回傳原結果的契約。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `submit()`／`existingSubmission()`／`publishedProcessVersion()`／`publishedFormVersion()`；`README.md` 第 685～686 行；`backend/doc/08-API規格.md` 第 122 行。此為呼叫順序的靜態推論，未提交或修改業務資料。

### BUG-141：不同流程共用表單與冪等鍵時，可能回傳前一流程的起單結果

- 嚴重度：高。性質：正式起單冪等請求比對；尚未以兩個共用表單的流程實際重現。
- 預期：同一冪等鍵若改為送到另一個 `processDefId`，應視為不同請求而拒絕；`README.md` 第 685～686 行及 `backend/doc/08-API規格.md` 第 122 行要求相同 key、不同內容不得回傳舊結果。
- 實際：`existingSubmission()` 以 Tenant 與冪等鍵找到原表單資料，再以 business key 找到原流程實例。`sameRequest` 比對表單 ID／版本、申請人、起單者、Form Data ID 與 JSON，卻沒有比對 `command.processDefId()` 和已儲存的 `process.getProcessDefId()`。若流程 A 與 B 都綁定同一張表單版本，且使用者對兩者都有起單權，向 B 重送 A 的冪等鍵與相同表單內容會被視為同一請求，直接回傳 A 的流程實例，而不會建立 B，也不會回報鍵衝突。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `submit()`／`existingSubmission()`／`insertProcessInstance()`；`FmProcessInstance.processDefId`。此為欄位比對的靜態推論，未提交或修改業務資料。

### BUG-142：起單冪等比對忽略附件 Session，可能把不同附件請求視為相同

- 嚴重度：中。性質：正式起單冪等請求與附件綁定；目前正式附件及表單資料皆為 0 筆，未實際重現。
- 預期：同一冪等鍵若帶不同 `uploadSessionId`，且兩個 Session 的附件集合不同，應判定為不同送單請求並拒絕，避免讓使用者以為新附件已綁定。相同 key、不同內容拒絕的契約見 `README.md` 第 685～686 行與 `backend/doc/08-API規格.md` 第 122 行。
- 實際：`FmProcessRuntimeLogicServiceImpl.submit()` 對首次請求使用 `uploadSessionId` 呼叫 `FmAttachmentBindingService.bind()`，該服務會把 Session 內所有暫存檔正式綁定；但重送時 `existingSubmission().sameRequest` 只比較表單 JSON 等欄位，沒有比對或記錄原 `uploadSessionId`／附件集合。若表單 JSON 相同，而新 Session 另有未列在表單 JSON 的暫存檔，重送會直接回傳原流程結果，新 Session 的檔案不會綁定，也沒有請求內容衝突訊息。BUG-115 記錄的是首次送單時附件 Session 與表單欄位未核對；本項記錄冪等重送時附件 Session 被忽略。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `submit()`／`existingSubmission()`；`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentBindingService.java` 的 `bind()`。此為靜態呼叫路徑推論，未建立附件或提交表單。

### BUG-143：後續 User Task 表單被當成起單表單並可直接送出

- 嚴重度：高。性質：流程表單綁定與正式起單；現有已發布流程各只使用一種表單，未用多表單流程實際重現。
- 預期：申請中心只應提供業務定義為「起單用表單」的版本；後續簽核或補件 Task 的表單不能僅因綁定同一流程就成為起單選項。`backend/doc/25-Workspace與申請中心重構規劃.md` 第 313 行限定表單切換於多個起單用表單。
- 實際：`FmProcessRuntimeLogicServiceImpl.startForms()` 走訪該流程版本全部 `FmTaskFormRule`，按表單 ID／版本去重後全部回傳給起單頁，沒有按起始節點或用途篩選；`ensureFormBound()` 只要求送出的表單版本出現在任一 Task Form Rule。若某流程的簽核 Task 使用不同表單，起單頁會將簽核表單列為可選，直接 API 也可用該表單送單；後端會以該表單 Schema 驗證並建立流程，造成起單資料與原設計的申請表單不一致。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `startForms()`／`ensureFormBound()`／`submitAs()`；`backend/doc/04-流程與表單規格.md` 第 66、129 行的每 Task 表單綁定；`backend/doc/25-Workspace與申請中心重構規劃.md` 第 313 行。此為靜態推論，未建立多表單版本或送單。

### BUG-144：起單頁載入後發布新版流程，送單可能無提示地改用新版

- 嚴重度：中。性質：起單頁與流程版本一致性；未實際進行頁面停留期間的發布與送單。
- 預期：使用者依載入時顯示的流程版本填單，送出時若已切換流程版本，系統應確認原版本仍可使用，或明確要求重新載入並檢視新版設定，避免將申請送進未檢視的新核決路徑。
- 實際：`loadStart()` 回傳當時的 `versionNo` 與表單；起單頁送出的 `FmProcessSubmitCommand` 只有 `processDefId`、表單 ID／版本及資料，沒有流程版本或預期版本欄位。`submitAs()` 再呼叫 `publishedProcessVersion()` 取當下最新的 PUBLISHED 版本。若頁面載入 v1 後發布 v2，且 v2 仍綁同一張表單，`ensureFormBound()` 會通過，系統直接以 v2 Flowable Definition 與 v2 起單／簽核政策建立流程，前端沒有版本變更提示。這與 BUG-100 的建立草稿期間無法起單不同。
- 證據：`FmProcessRuntimeLogicServiceImpl.loadStart()`／`submitAs()`／`publishedProcessVersion()`／`ensureFormBound()`；`FmProcessSubmitCommand`；`frontend-v-nx/composables/useRuntimeSubmit.ts`。此為載入與送單時序的靜態推論，未發布新版本或提交表單。

### BUG-018：停用 Tenant 仍可能透過直接 API 起單

- 跨單元修正紀錄（2026-10-06）：與 02 單元 BUG-049 同一 Tenant 狀態根因。`FmProcessRuntimeLogicServiceImpl.validateTenantMembership()` 現在於 membership 檢查前要求 Tenant 為 `ACTIVE`，直接起單入口會經過此檢查；已編譯，尚未以停用 Tenant 執行 HTTP 情境。16 單元其餘條目尚未處理。

- 嚴重度：高。性質：Java Runtime 授權；限於流程未設定 `DOCUMENT_TYPE` 的情境。現有 Tenant 皆啟用，尚無資料可重現。
- 預期：Tenant 停用後，起單目錄及 Runtime API 都應拒絕新起單（`README.md`「登入、身分與 Tenant 邊界」）。
- 實際：`FmProcessRuntimeLogicServiceImpl.tenants()` 會過濾停用 Tenant，因此前端選單看不到；但 `catalog()`、`loadStart()`、`submitAs()` 只透過 `validateTenantMembership()` 確認 membership 有效，該方法不查 `fm_tenant.STATUS`。`nextDocumentNumber()` 只在流程有 `DOCUMENT_TYPE` 時查啟用 Tenant；沒有單據類型的流程會直接回傳 `null`，可繼續送單。知道 Tenant ID 與流程 ID 的使用者可直接呼叫 API。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `tenants()`、`validateTenantMembership()`、`submitAs()` 與 `nextDocumentNumber()`。MariaDB 唯讀查詢顯示目前 Tenant 1 筆且啟用，現有 Published 流程沒有缺少 `DOCUMENT_TYPE` 的資料，因此此項為條件式程式碼缺陷。

### BUG-050：申請中心快速切換 Tenant 時，舊申請人清單可能覆蓋新 Tenant

- 嚴重度：中。性質：Vue 非同步狀態競態；與 BUG-041 的工作台載入競態是不同頁面與不同資料流程。現有資料只有一個 Tenant，無法直接在瀏覽器重現。
- 預期：申請中心選取 Tenant B 後，申請人選項與可發起流程必須只對應 B；Tenant A 的遲到請求不得更新 B 的畫面狀態。
- 實際：`useProcessStartCatalog` 的 `watch(tenantId, loadApplicants)` 每次切換都發出 `/start/applicants`。`loadApplicants()` 收到回應後直接覆寫共用 `applicants`、`applicantAccount`，沒有比對請求時的 Tenant 或取消舊請求。若先選 A 再選 B，B 先回、A 後回，畫面仍顯示 B，但申請人選項被 A 覆蓋，接著 `loadCatalog()` 使用目前 Tenant B 搭配 A 的申請人查目錄。`loadCatalog()` 有 generation 防護，卻無法阻止較舊的 `loadApplicants()` 改寫選項。後端仍會驗證 Tenant 與申請人，此項沒有證據顯示跨 Tenant 資料外洩。
- 證據：`frontend-v-nx/composables/useProcessStartCatalog.ts` 的 `loadApplicants()`、`loadCatalog()` 與 `watch(tenantId, loadApplicants)`；`frontend-v-nx/pages/requests/start/index.vue` 使用此 composable，`frontend-v-nx/pages/workspace.vue` 將 Tenant ID 帶到申請中心。未修改資料或建立第二個 Tenant。
