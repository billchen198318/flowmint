## 26 共用單據編號與預設表單流程配置 — 靜態／唯讀盤查完成

2026-10-05 收尾：已核對第 23／24／30／31 章規格、`FmDocumentNumberService`、規則與序列 Mapper、`FmProcessRuntimeLogicServiceImpl.submitAs()` 的取號交易、送出成功與詳情 Vue、單據號碼服務及 Mapper 測試。先前唯讀資料顯示四套預設流程／表單綁定、四筆 ACTIVE 的 MONTH／六碼規則且格式含年月，序列表與正式單據資料為 0。規則週期和格式未連動見 BUG-157，API 顯示欄位污染見 BUG-158。跨週期、併發及正式送單沒有現成資料可重現；此處完成限靜態與既有唯讀範圍。

2026-10-04 週期／交易續查：`FmProcessRuntimeLogicServiceImpl.submitAs()` 的送單交易在建立 Form Data 與 Flowable 流程前呼叫取號；`FmDocumentNumberService.nextNumber()` 依 Tenant 時區由同一時間點算 `NONE`／`YEAR`／`MONTH` period key，`selectForUpdate()` 鎖現有序列，首次建立用 `(TENANT_ID,DOCUMENT_TYPE,PERIOD_KEY)` 唯一鍵及 `ON DUPLICATE KEY UPDATE OID = OID` 處理競爭，再以 OID＋lock version 條件遞增，超過位數上限則拒絕。未配置 `DOCUMENT_TYPE` 時直接保留 UUID，符合規格允許技術流程不取正式單號。規則格式與週期缺少連動檢查已在 BUG-157，顯示欄位可由 API 自填已在 BUG-158，本輪未確認獨立新缺陷。先前已唯讀確認四套預設流程／表單／ACTIVE 規則的綁定；目前沒有序列或正式單據資料，跨週期與併發送單仍未實測。

已檢查 `FmDocumentNumberService` 與 `FmDocumentSequenceMapper.xml`。首次建立序號使用 MariaDB `ON DUPLICATE KEY UPDATE`，目前未確認並發取號缺陷。資料庫唯讀查詢顯示編號規則 4 筆、序號 0 筆；預設表單流程配置、前端及實際取號流程仍待盤查。

2026-10-04 續查 `backend/doc/24-共用單據編號規格.md`、`FmProcessRuntimeLogicServiceImpl.submitAs()` 與 `nextDocumentNumber()`、Vue 申請送出成功頁與詳情頁。MariaDB 唯讀重新確認四筆單據規則均為 ACTIVE、MONTH／六碼且格式含 Tenant 與年月；序號仍為 0 筆。後端取號在建立表單與流程實例之前，送出頁優先顯示正式單號，詳情頁也讀取正式單號。尚無正式送單或併發資料，維持部分盤查。

### BUG-157：流水號按月或年重設時，格式未包含對應日期仍可通過規則驗證

- 嚴重度：中。性質：Java 編號規則驗證／MariaDB 唯一性；配置邊界的靜態推論，未異動規則重現。
- 預期：規則若使用 `MONTH` 或 `YEAR` 週期重設流水號，產生的可讀單號應能區分週期；不具備此條件的格式應在配置或取號前被拒絕，避免次期送單碰到既有單號。
- 實際：`FmDocumentNumberService.periodKey()` 依 Tenant 時區為 `MONTH` 產生 `yyyyMM`、為 `YEAR` 產生 `yyyy`，新週期建立新的序列並從 1 開始；`validateRule()` 只檢查格式含 `{SEQ}`，不要求 `MONTH` 格式含 `{YYYY}` 和 `{MM}`，也不要求 `YEAR` 格式含 `{YYYY}`。例如 `MONTH` 搭配 `{PREFIX}-{TENANT}-{SEQ}`，次月第一筆會產生與上月第一筆相同單號；`fm_form_data` 與 `fm_process_instance` 的 `(TENANT_ID,DOCUMENT_NUMBER)` 唯一鍵會拒絕插入，造成送單失敗。格式設定需要透過資料配置或 migration 引入，目前未見一般使用者管理入口。
- 證據：`backend/doc/24-共用單據編號規格.md` 的週期、格式與唯一性契約；`FmDocumentNumberService.validateRule()`／`periodKey()`／`format()`、`backend/doc/flowmint.sql` 的 `UK_FM_FORM_DATA_DOC_NO`／`UK_FM_PI_DOC_NO`。MariaDB 唯讀查詢顯示目前 4 筆 ACTIVE 規則均為 `MONTH` 且格式含 `{YYYY}{MM}`，`fm_document_sequence` 為 0 筆，因此沒有現成衝突；未建立跨月單據測試。

2026-10-04 本輪另核對 Rule／Sequence Service 與 Mapper 的選取、鎖定、首次建立及條件遞增入口，並搜尋一般管理 Controller／Vue，未找到單據編號規則的使用者維護入口；目前規則來自資料配置。起單成功頁、申請詳情及待辦詳情以 `documentNumber` 顯示並保留 `businessKey` 作技術識別；仍需完整核對預設四套 Form／Process 綁定及跨週期實際送單。上述檢查未產生另一項獨立缺陷，26 單元維持部分盤查。

### BUG-158：直接送單可把自填的顯示用 documentNumber 存入表單資料

- 嚴重度：中。性質：Java 起單 API／系統欄位資料完整性；靜態推論，尚未直接送單重現。
- 預期：`documentNumber` 只能由後端規則服務產生並存在正式單號欄位，Renderer 可顯示但不得接受使用者提供或寫入一般 `submission.data`；直接 API 請求應與前端送單得到相同保護。
- 實際：表單設計器的「加入單據編號欄位」會建立 `key=documentNumber`、`input=true`、`persistent=false` 的 Form.io 欄位。前端 `withoutFlowmintDisplayFields()` 在正常起單時刪除該 key；但 `FmFormSubmissionValidator.collectScopeKeys()` 仍會將已發布 Schema 的 `documentNumber` 視為合法資料欄位，`validateValue()` 只依 `textfield` 驗證型別，沒有拒絕顯示專用欄位。`FmProcessRuntimeLogicServiceImpl.submitAs()` 用原始 `command.formData()` 驗證並在 `insertFormData()` 原樣序列化為 `fm_form_data.DATA_CONTENT`。因此表單包含此欄位時，直接呼叫起單 API 可把自填字串存進表單資料；正式 `DOCUMENT_NUMBER` 欄仍由服務另外產生，兩者可能互相矛盾。外部拋單 `FmExternalRequestSubmitService.submit()` 將 `request.submission()` 複製後只覆寫申請人系統欄位，也沒有刪除 `documentNumber`，同樣會進入共用 Runtime 路徑。
- 證據：`backend/doc/24-共用單據編號規格.md` 第 4、8 節、`backend/doc/32-外部系統API管理與流程拋單規劃.md` 的 submission 契約；`FormDesigner.vue.addDocumentNumberField()`、`useFlowmintSystemFields.ts.withoutFlowmintDisplayFields()`、`useRuntimeSubmit.ts`；`FmFormSubmissionValidator.collectScopeKeys()`／`validateValue()`、`FmProcessRuntimeLogicServiceImpl.submitAs()`／`insertFormData()`、`FmExternalRequestSubmitService.submit()`。MariaDB 以 `INSTR` 唯讀覆核確認目前 4 筆已發布 Form Schema 均含 `documentNumber`；其中驗收單欄位為 `textfield`、`input=true`、`disabled=true`。`fm_form_data` 仍為 0 筆；未執行直接或外部送單請求。

2026-10-04 預設配置唯讀核對：`FM_BUSINESS_CARD_APPROVAL`、`FM_GOODS_ACCEPTANCE_APPROVAL`、`FM_PURCHASE_APPROVAL`、`FM_PURCHASE_ORDER_APPROVAL` 四筆流程主檔均為 PUBLISHED，各自 `DOCUMENT_TYPE` 對應一筆 ACTIVE 編號規則，Task Form Rule 皆連到對應的業務表單。四張已發布表單 Schema 均含 `documentNumber`；前端正常起單與補件送出會移除顯示專用單號，詳情與待辦 Renderer 則另外注入。先前 `LIKE` 查詢因 PowerShell／SQL 引號跳脫而誤判欄位不存在，已用 `INSTR` 及 Schema 片段覆核修正。仍需查各流程起單表單選取、預設業務欄位及跨週期取號實際路徑，不標完成。
