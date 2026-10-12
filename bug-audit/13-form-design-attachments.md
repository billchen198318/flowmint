## 13 表單設計、版本、Custom JavaScript 與附件（`FM_PROG005D0001`）— 靜態／唯讀盤查完成

> **修復接續點（2026-10-08）**：BUG-132、BUG-015 與 BUG-156 已完成修正及目標驗證；BUG-127 已由 BUG-118 修正，屬過時描述。第 13 單元原清單條目均有處理結論，下一步接第 14 單元。各項結果及未實測限制見本檔底部的修復複核。不要把下方原始「實際」敘述視為現行程式狀態；工作樹有未提交變更，接手時先檢查 `git status --short` 並保留。

2026-10-05 收尾：已對照表單／附件規格、`FMPROG005D0001Controller` 的清單／建立／更新／停用／草稿／建版／發布與 Tenant 選項、`FmAttachmentUploadController` 的 Session／上傳／列檔／刪除／下載入口、表單設計與執行 Vue／composable、表單驗證器及其測試；既有版本、Custom JavaScript、Binding、附件儲存與清理分析詳見下方 30 條觀察。先前唯讀資料顯示表單資料與正式附件為 0，Upload Session 有到期紀錄；沒有建立流程、上傳檔案或執行併發／網路故障情境。BUG-108／109 合併成版本切換工作包；BUG-128 是條件式風險，均未宣稱已重現。

### BUG-179：表單主檔停用後可繞過起單頁直接送單

- 嚴重度：中。性質：表單生命週期與正式起單 API；未停用現有表單或執行送單重現。
- 預期：表單主檔明確停用後，以該表單新建流程實例應由後端拒絕；起單頁與直接送單 API 應採用一致的表單狀態規則。既有待辦仍需依固定版本顯示表單，可另行處理。
- 實際：`FmFormDefLogicServiceImpl.deactivate()` 只把主檔設為 `INACTIVE`，不更動已發布表單版本。`FmProcessRuntimeLogicServiceImpl.startForms()` 透過 `activeFormDef()` 要求主檔 `STATUS='PUBLISHED'`，所以起單頁無法載入；但 `submitAs()` 只以 `publishedFormVersion()` 檢查表單版本為 `PUBLISHED` 並核對流程綁定，沒有讀取表單主檔狀態。已知流程與表單識別的呼叫者仍可能直接送出。BUG-108 是建立新草稿時主檔變為 `DRAFT` 造成 UI 中斷；本項是明確停用後後端未阻擋新送單。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmFormDefLogicServiceImpl.java` 的 `deactivate()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `startForms()`、`activeFormDef()`、`submitAs()`、`publishedFormVersion()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmTaskFormRuleMapper.xml` 的 `selectPublishedFormOptions`／`countPublishedFormVersion` 均排除 `d.STATUS='INACTIVE'`，所以設計器選項與流程發布也會擋住停用表單；既有待辦的 `FmTaskRuntimeLogicServiceImpl.formVersion()`／`formDef()` 依固定識別讀取且不篩主檔狀態。此為控制流程靜態推論；MariaDB 未建立停用測試資料。

### BUG-176：Data Action Binding 編輯器將巢狀表單欄位當成最外層 key

- 嚴重度：中。性質：Vue 表單設計與 Data Action Mapping 路徑；靜態推論，未以瀏覽器巢狀表單重現。
- 預期：Container、DataGrid／EditGrid 中的欄位應以完整表單資料路徑呈現，Request Mapping 從實際巢狀位置取值，Response Mapping 寫回相同位置；無法支援的 Grid 列索引應在設計時明確拒絕。
- 實際：`FormDataActionBindingEditor.vue` 的 `formFields` 遞迴蒐集子元件時只加入 `component.key`，不帶父 Container 路徑或 Grid 列索引；選擇 Action 時 `loadMetadata(true)` 預設把 Request 欄位映射到 `submission.<field>`。後端 `FmFormDesignValidator` 的 `keys` 集合同樣只存子欄位 key，`validateRequestMapping()` 只檢查根 key，`validateMappingTargets()` 也接受單一子 key。Runtime 的 `buildDataActionRequest()` 依所存路徑讀值，`applyResponse()`／`setFormDataPath()` 依目標路徑寫值；例如 `details.amount` 欄位被編輯器存為 `submission.amount`／`amount`，會讀不到 `details.amount`，並把回應寫成最外層 `amount`。正式送單的 `FmFormSubmissionValidator.validateScope()` 會拒絕這個未宣告的頂層 key。此問題與 BUG-120 的附件路徑不同，發生於 Data Action Binding。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDataActionBindingEditor.vue` 的 `formFields`、`loadMetadata()`；`frontend-v-nx/composables/useFormDataAction.ts` 的 `buildDataActionRequest()`、`applyResponse()`、`setFormDataPath()`；`FmFormDesignValidator.validateComponents()`／`validateRequestMapping()`／`validateMappingTargets()`；`FmFormSubmissionValidator.validateScope()`。尚未建立此種 Binding 及執行請求。

### BUG-177：後端送出驗證未核對 Form.io 固定選項值域

- 嚴重度：中。性質：表單送出資料完整性；靜態推論，未以固定選項欄位送出非法值重現。
- 預期：已發布 Form.io Schema 中 `select`、`radio`、`selectboxes` 等固定選項欄位，後端應檢查提交值屬於 Schema 定義的允許值；README 第 616 行要求後端在送單時依 Schema 重新驗證。
- 實際：`FmFormSubmissionValidator.validateScope()` 只確認欄位 key 已宣告；`validateScalar()` 僅對 number／currency、checkbox 和部分文字元件做型別檢查，`select`、`radio`、`selectboxes` 不在型別分支中，也沒有讀取 component 的 `data.values` 或其他選項來源做成員檢查。`validateValue()` 的 `multiple` 只驗證是否為 Collection，未驗證各項是否在可選清單。`FmFormDesignValidator` 也沒有拒絕這類元件發布。因此直接起單或待辦表單更新 API 可提交畫面選單未提供的值；這與 BUG-156 未執行 `validate.custom`／`validate.json` 的問題不同。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/runtime/FmFormSubmissionValidator.java` 的 `validateScope()`、`validateValue()`、`validateScalar()`、`isTextType()`；`FmFormDesignValidator.java` 的 `validateComponents()`；`FmProcessRuntimeLogicServiceImpl.submitAs()` 與 `FmTaskRuntimeLogicServiceImpl.updateTaskForm()` 呼叫共用驗證器。尚未核對目前資料庫中固定選項欄位數量或執行 HTTP 請求。

> 2026-10-04 接續盤查：核對 `saveDraft()`、`publish()`、版本 Mapper 與建表唯一鍵，擴充 BUG-110 的草稿儲存／發布競態範圍。`(TENANT_ID, FORM_ID, VERSION_NO)` 唯一鍵可阻止相同版號的兩次建立；未另列重複問題。另核對停用 API、設計器入口與 `FmFormDesignValidator`；自訂欄位驗證的前後端落差記為 BUG-156。其餘 Form.io 配置與 Runtime 行為仍須查。目前資料庫沒有 Draft，併發後果僅為靜態推論。

### BUG-156：Form.io 自訂欄位驗證可發布，但起單與待辦的後端不執行

- 嚴重度：高。性質：表單發布契約／Java 後端驗證；程式碼推論，未建立帶自訂驗證的表單送單重現。
- 預期：README 指出送單時後端依同一份已發布 Schema 再驗證；若 Form.io 設計器允許 `validate.custom` 或 `validate.json` 規則，後端應等效驗證，或在發布時拒絕無法驗證的規則。
- 實際：前端直接啟用 `@formio/js` 的標準 Builder；專案安裝的元件設定頁提供 `validate.custom` JavaScript 及 `validate.json` JSONLogic。`FmFormDesignValidator.rejectExecutableConfiguration()` 只拒絕 `customDefaultValue`、`calculateValue`、`calculateServer`、`customConditional` 和非空 `logic`，沒有檢查這兩種 `validate` 設定；`publish()` 因此可保存並發布。起單及待辦表單更新共用的 `FmFormSubmissionValidator` 僅檢查內建的必填、型別、長度、範圍、pattern 等規則，未執行 `validate.custom` 或 `validate.json`。直接呼叫起單／待辦 API 時，符合內建規則但違反自訂規則的資料可能通過後端驗證；在正常瀏覽器中則可能被 Form.io 擋住，造成兩端結果不一致。
- 證據：`README.md` 表單設計與後端再驗證說明；`frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue` 的 `Formio.builder()`；本地安裝的 `@formio/js/lib/mjs/components/_classes/component/editForm/Component.edit.validation.js` 定義兩類驗證欄位；`FmFormDefLogicServiceImpl.validateContent()`／`publish()`、`FmFormDesignValidator.rejectExecutableConfiguration()`、`FmFormSubmissionValidator.validateValue()`／`validateScalar()`，以及 `FmProcessRuntimeLogicServiceImpl.submitAs()`、`FmTaskRuntimeLogicServiceImpl.updateTaskForm()` 對共用驗證器的呼叫。MariaDB 唯讀查詢確認 `FM_GOODS_ACCEPTANCE` 的 PUBLISHED v1 在 `deliveryDate` 欄位已有非空 `validate.custom`，要求到貨日期不得晚於驗收日期；後端驗證器沒有此跨欄位檢查。其餘版本可能有空字串的 `custom`／`json` 設定；未送出違規日期請求重現後端結果。

### BUG-108：建立表單新草稿使仍綁定舊發布版本的流程無法載入起單表單

- 嚴重度：高。性質：表單版本與正式起單可用性；未建立新草稿重現。
- 預期：表單建立下一版草稿後，既有 Published Form Version 應繼續供已發布流程起單，直到新版本發布或表單明確停用。流程綁定固定的表單版本，不應因編輯下一版草稿而中斷。
- 實際：`FmFormDefLogicServiceImpl.createVersion()` 建立 Draft 後將 `fm_form_def.STATUS` 設為 `DRAFT`；舊 `fm_form_version` 仍為 `PUBLISHED`。`FmProcessRuntimeLogicServiceImpl.startForms()` 透過 `activeFormDef()` 查表單主檔時卻要求 `STATUS='PUBLISHED'`，因此已發布流程綁定舊表單版本仍會在載入起單表單時失敗；`startCatalog()` 也呼叫 `startForms()`，會將該流程排除。直接呼叫 `submitAs()` 僅按版本查 `PUBLISHED` 表單，未使用 `activeFormDef()`；既有待辦的 `FmTaskRuntimeLogicServiceImpl.formDef()` 也不篩主檔狀態，因此目前證據不支持將直接送單或既有待辦列為此缺陷的受阻路徑。這與第 12 單元 BUG-100 的流程主檔狀態問題分屬不同主檔。
- 證據：`FmFormDefLogicServiceImpl.createVersion()`、`FmProcessRuntimeLogicServiceImpl.startForms()`／`activeFormDef()`／`startCatalog()`／`submitAs()`、`FmTaskRuntimeLogicServiceImpl.formDef()`，以及 README 的固定 Published Form Version 契約。MariaDB 唯讀查詢顯示目前 4 筆表單主檔／版本均為 PUBLISHED，沒有 Draft；未異動資料重現。

### BUG-109：表單新版發布後舊版退役，使尚未更新的已發布流程無法起單

- 嚴重度：高。性質：表單／流程發布順序與正式起單可用性；未發布新版本重現。
- 預期：依 README「Form 先、BPMN 後」的發布順序，表單新版發布與流程新版發布之間，仍在使用的已發布流程應可繼續以其固定綁定的舊表單版本起單；或發布流程與表單須以可控方式一起切換，避免服務中斷。
- 實際：`FmFormDefLogicServiceImpl.publish()` 發布新表單版時，先將上一個 `PUBLISHED` 版本設為 `RETIRED`。尚未建立或發布新流程版時，舊已發布流程的 `fm_task_form_rule` 仍指向該舊表單版本。`FmProcessRuntimeLogicServiceImpl.startForms()`／`publishedFormVersion()` 僅接受 `VERSION_STATUS='PUBLISHED'`，因此申請中心略過該流程，起單頁載入也失敗；`submitAs()` 同樣拒絕舊版。此項不同於 BUG-108 的表單主檔在新草稿期間變為 `DRAFT`，是新版表單已發布後的固定綁定失效。
- 證據：`FmFormDefLogicServiceImpl.publish()`、`FmProcessRuntimeLogicServiceImpl.startForms()`／`publishedFormVersion()`／`submitAs()`、`FmTaskFormRuleMapper.xml` 的版本綁定，以及 README 的 Form→BPMN 發布順序。MariaDB 唯讀查詢顯示目前已發布流程的 42 筆 Task Form Rule 均綁定 PUBLISHED 表單版本，沒有現成 RETIRED 綁定；未透過異動或 HTTP 重現。

### BUG-110：表單草稿儲存與發布缺少併發狀態轉換保護

- 嚴重度：高。性質：Java／MariaDB 草稿儲存與發布併發；目前沒有 Draft，未並行重現。
- 預期：同一表單版本從 `DRAFT` 轉 `PUBLISHED` 只能成功一次；並行的草稿儲存應在發布後拒絕，發布也應使用最後成功儲存的內容，不得回退狀態或覆寫發布稽核欄位。
- 實際：`saveDraft()` 與 `publish()` 都先透過 `draft()` 讀取並檢查 `DRAFT`，經內容驗證後才更新。`FmFormVersionMapper.update` 只以 OID 為條件，同時寫入 `VERSION_STATUS`、Schema、Script 與發布欄位，沒有 `FOR UPDATE`、樂觀鎖或預期狀態條件。若兩者先讀到同一份草稿，儲存交易在發布交易後更新，可將已發布版本的狀態寫回 `DRAFT`，使表單主檔顯示 `PUBLISHED` 卻沒有可用的發布版本；若發布交易後更新，則可能以舊內容覆蓋剛儲存的草稿。兩個發布交易也可能先後成功，後者覆寫 `PUBLISHED_BY`、`PUBLISHED_DATE`。此項與 BUG-091 的 Flowable 流程重複部署不同。
- 證據：`FmFormDefLogicServiceImpl.saveDraft()`／`publish()`／`draft()`、`FmFormVersionMapper.xml` 的 `update`。MariaDB 唯讀查詢顯示 0 筆表單草稿；上述交錯結果為靜態推論，未執行併發異動驗證。

### BUG-111：表單主檔儲存成功後先重載畫面，草稿儲存失敗會丟失編輯內容

- 嚴重度：中。性質：Vue／Java 分步儲存與編輯狀態；未以故意失敗請求重現。
- 預期：一次「儲存表單」若草稿 Schema、UI Schema 或 Script 儲存失敗，應保留使用者未成功保存的編輯內容，並清楚指出主檔與草稿各自的保存結果。
- 實際：`FormDesigner.vue.save()` 先將草稿內容複製到局部變數，呼叫 `/update` 保存主檔並透過 `showResponse()` 顯示成功，隨即 `apply(response.data.value)` 以伺服器舊草稿重新設定 `form` 與 `selectedVersion`、重繪設計器；之後才呼叫獨立的 `/version/save-draft`。若後者驗證或請求失敗，主檔已保存，畫面又已重載舊草稿，使用者原先未保存的圖形／Script 編輯可能消失。與 BUG-093 的流程設計器分步保存相似，但此處額外先重載畫面，形成直接編輯內容流失風險。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue` 的 `save()`／`apply()`／`selectVersion()`／`renderDesigner()`，`FmFormDefLogicServiceImpl.update()`／`saveDraft()` 各自交易。現有資料沒有 Draft，未製造草稿失敗情境。

### BUG-112：表單試跑驗證未在 beforeSubmit 後重新檢查欄位

- 嚴重度：中。性質：Vue 表單預覽與正式起單驗證不一致；未以實際表單操作重現。
- 預期：設計器「試跑驗證」應依正式起單的檢核順序執行；若 `beforeSubmit` 改變表單資料，應再次檢查必填與其他 Form.io 驗證規則，才顯示通過。
- 實際：`FormDesigner.vue.validatePreview()` 先呼叫 `designerInstance.checkValidity(null, true)`，再執行 `runCustomJavascript?.("beforeSubmit")`，只檢查 Script 回傳值便顯示「試跑驗證通過」。正式起單的 `useRuntimeSubmit.submitOnce()` 在 `beforeSubmit` 後再呼叫 `formInstance.checkValidity(null, true)`。若 Script 將原本有效的必填欄位清空或改為不合法值，設計器可能顯示通過，但正式起單會拒絕。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue` 的 `validatePreview()`；`frontend-v-nx/composables/useRuntimeSubmit.ts` 的 `submitOnce()`。此處為程式路徑推論，未建立可改寫欄位的 Script 做瀏覽器重現。

### BUG-113：Task Action Hook 發布時未驗證請求值的來源路徑

- 嚴重度：中。性質：表單發布驗證與任務 Hook 執行；未新增資料重現。
- 預期：發布表單時，Task Action Hook 的 `requestMapping` 應確認來源採用可解析的 `form.`、`json:form.` 或 `context.` 路徑，且必要路徑確實存在，避免任務執行時將錯誤資料送往 Data Action。
- 實際：`validateTaskActionHooks()` 只要求 `requestMapping` 是物件，`validateDataActionMetadata()` 只比較 mapping 的請求欄位名稱與 Action metadata，未檢查各欄位的來源值。`FmTaskActionHookExecutor.mappedRequest()` 對不符合三種前綴的來源直接設為 `null`，對不存在的路徑也由 `path()` 取得空值。像 `from.purchaseOrderNo` 這類拼錯前綴的設定可通過發布，執行時卻傳入 `null`，造成 Hook 檢核或後續流程依賴錯誤資料。
- 證據：`FmFormDefLogicServiceImpl.validateTaskActionHooks()`／`validateDataActionMetadata()`；`FmTaskActionHookExecutor.mappedRequest()`。此為靜態路徑確認，未修改現有 Hook 或觸發任務重現。

### BUG-114：Custom JavaScript 處理欄位異動時會丟掉中間事件

- 嚴重度：中。性質：Vue Custom JavaScript 事件處理；未以瀏覽器快速操作重現。
- 預期：`onFieldChange(ctx)` 應收到可編輯表單每次需要處理的欄位異動，尤其 Script 依 `ctx.changed` 查詢資料或更新其他欄位時，不應靜默遺失中間欄位事件。
- 實際：`useFormCustomJavascript.attach()` 的 `changeHandler()` 在前一個 `onFieldChange` 尚未完成時，把新事件放進單一 `pendingChange`；後續事件直接覆寫它。前一事件結束後只處理最後一次。因此若 Script 執行非同步 Data Action 期間連續修改 A、B 欄位，A 的 `ctx.changed` 事件可能被 B 覆蓋，對應欄位處理不執行。最後的 submission data 仍會更新，但依事件執行的副作用可能缺失。
- 證據：`frontend-v-nx/composables/useFormCustomJavascript.ts` 的 `processChange()`／`changeHandler()`；`FormCustomJavascriptEditor.vue` 將 `ctx.changed` 說明為當次異動。這是事件佇列的靜態推論，尚未做瀏覽器快速輸入驗證。

### BUG-115：送單時未核對附件欄位 ID 與暫存檔，會正式綁定未提交的檔案

- 嚴重度：中。性質：Java 附件與表單資料一致性；目前未建立附件及表單資料重現。
- 預期：送單時應逐一核對 submission 中的附件 ID、欄位 key 與本次 Upload Session 的暫存檔；只綁定實際提交的附件，拒絕不存在、已刪除或屬於其他欄位的 ID。
- 實際：`FmProcessRuntimeLogicServiceImpl.submitAs()` 先將 `command.formData()` 存入 `fm_form_data`，再呼叫 `FmAttachmentBindingService.bind()`；後者只核對 Session 的 Tenant、Owner、表單版本及期限，接著查出該 Session 的所有 `TEMPORARY` 檔案，逐一搬到正式區並新增 `fm_attachment`，沒有讀取本次 formData 中的附件 ID。使用者上傳後若從表單欄位移除某檔但未呼叫暫存刪除 API，該檔仍會成為正式附件；直接送出不屬於 Session 的附件 ID 也沒有在此路徑被拒絕。正式附件清單與表單欄位內容因此可能不一致。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `submitAs()`／`insertFormData()`；`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentBindingService.java` 的 `bind()`；`backend/doc/22-附件上傳與儲存規格.md` 第 7 節要求送單時重新驗證附件及欄位。這是靜態路徑推論，未建立資料或執行送單。


### BUG-116：附件上傳資料庫寫入失敗後可能留下無紀錄的暫存實體檔

- 嚴重度：中。性質：Java 檔案與資料庫一致性；未故意觸發資料庫例外重現。
- 預期：暫存檔寫入成功後，若中繼資料新增失敗或交易提交失敗，應移除該檔或由排程可靠回收。
- 實際：`FmAttachmentUploadLogicServiceImpl.upload()` 先呼叫 `storageService.storeTemporary()` 寫入實體檔，再執行 `fileService.insert()`；後段只捕捉 `ServiceException` 並刪檔。若 MyBatis／資料庫拋出執行期例外，或方法返回後交易提交失敗，檔案會留在 temporary 目錄而沒有 `fm_attachment_upload_file` 紀錄。`FmAttachmentTemporaryCleanupJob` 只按資料庫 TEMPORARY 明細刪檔，無法找到這種檔案；正式附件孤兒清理也只掃 attachments 目錄。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAttachmentUploadLogicServiceImpl.java` 的 `upload()`；`FmAttachmentStorageService.storeTemporary()`；`FmAttachmentTemporaryCleanupJob.cleanupSession()`；`FmAttachmentOrphanCleanupJob.quarantineOrphans()`。屬失敗路徑的靜態推論，未修改資料或磁碟檔案重現。

### BUG-117：刪除暫存附件時先刪實體檔，交易回滾可留下失效的 TEMPORARY 紀錄

- 嚴重度：中。性質：Java 檔案與資料庫一致性；未故意觸發資料庫例外重現。
- 預期：刪除暫存附件時，實體檔與資料庫狀態應一致；若資料庫更新失敗或提交回滾，應恢復檔案或讓紀錄不可再送單。
- 實際：`FmAttachmentUploadLogicServiceImpl.delete()` 在 `@Transactional` 方法中先執行 `storageService.deleteTemporary()`，再把明細 `FILE_STATUS` 更新為 `DELETED`。若後續 update／commit 失敗，資料庫回滾後仍為 `TEMPORARY`，但檔案已不存在。`FmAttachmentBindingService.bind()` 只檢查資料列的 `SCAN_STATUS='CLEAN'`，會選入這筆檔案，之後搬檔才失敗，令整筆送單失敗。清理工作遇到該紀錄時會呼叫 `deleteIfExists` 並可將其標為 DELETED，但須等 Session 到期。`FmAttachmentTemporaryCleanupJob.cleanupExpiredSessions()` 同樣在交易內先刪實體檔、再更新資料列；若其交易回滾，也會留下相同的狀態不一致。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAttachmentUploadLogicServiceImpl.java` 的 `delete()`；`FmAttachmentStorageService.deleteTemporary()`；`FmAttachmentBindingService.bind()`；`FmAttachmentTemporaryCleanupJob.cleanupSession()`。屬失敗路徑的靜態推論，未修改資料或磁碟檔案重現。


### BUG-118：上傳與送單同時進行時，BOUND Session 可殘留永不清理的暫存檔

- 嚴重度：中。性質：Java 並發與附件生命週期；未執行並發重現。
- 預期：送單正式綁定與同 Session 上傳須互斥或以原子狀態轉換封存 Session；BOUND Session 不應再新增 TEMPORARY 明細與實體檔。
- 實際：`upload()` 先由 `requireSession()` 讀取 OPEN 狀態，之後寫實體檔及 TEMPORARY 明細；`bind()` 也先讀 OPEN 狀態，再查詢當時 TEMPORARY 明細，最後更新 Session 為 BOUND。兩條路徑都未鎖定 Session 或在新增明細時再次條件檢查 OPEN。若上傳在送單查詢明細後提交，新檔不會被正式綁定，而 Session 已成 BOUND。`FmAttachmentTemporaryCleanupJob` 只掃描到期且 OPEN 的 Session，這筆暫存檔將不再由既有排程清理。即使上傳畫面成功顯示，新檔也可能不在正式附件清單。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAttachmentUploadLogicServiceImpl.java` 的 `requireSession()`／`upload()`；`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentBindingService.java` 的 `bind()`；`FmAttachmentTemporaryCleanupJob.cleanupExpiredSessions()`。起單頁的 `StartActionBar` 只依 `submitting` 停用送出按鈕，沒有依 `uploadingField` 停用，因此正常 UI 也可在上傳進行中送單。此為交易交錯的靜態推論，未建立附件或並發送單驗證。


### BUG-119：跨 UTC 月界送單時，正式附件搬檔月份與下載月份可能不同

- 嚴重度：低。性質：Java 正式附件路徑；需在 UTC 月界附近送單才可能重現。
- 預期：正式附件搬入的目錄與後續下載推算的目錄應使用同一個時間基準，確保附件可讀取。
- 實際：`FmProcessRuntimeLogicServiceImpl.submitAs()` 在起單早段建立 `Date now`，並傳給 `FmAttachmentBindingService.bind()` 寫入 `fm_attachment.CDATE`。但 `FmAttachmentStorageService.promote()` 搬檔時另外呼叫 `YearMonth.now(UTC)` 決定 `{yyyy}/{MM}` 目錄。若 `now` 在 UTC 月末、搬檔在下月初，資料列日期仍屬上月而實體檔在下月。`FmAttachmentDownloadService.download()` 把資料列 `CDATE` 傳給 `readFormal()`，後者依該日期推算上月路徑，最後回報找不到實體檔。孤兒清理也可能將下月目錄的檔案保留，因資料列的 FILE_OID 存在。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessRuntimeLogicServiceImpl.java` 的 `submitAs()`；`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentBindingService.java` 的 `bind()`；`FmAttachmentStorageService.promote()`／`readFormal()`；`FmAttachmentDownloadService.download()`。這是跨 UTC 月界的靜態推論，未於時間邊界實際送單。


### BUG-120：Container／Grid 內附件 ID 寫在最外層，正式送單遭 Schema 驗證拒絕

- 嚴重度：中。性質：Vue 表單資料路徑；未以瀏覽器巢狀附件表單重現。
- 預期：附件元件位於 Container、DataGrid 或 EditGrid 時，前端應將附件 ID 放入該元件實際所在的巢狀資料路徑，再送給後端驗證與儲存。
- 實際：`useRuntimeAttachments.collectFileFields()` 會遞迴找到巢狀 File 元件，但 `uploadAttachment()` 和 `deleteAttachment()` 都固定寫入 `formInstance.submission.data[field.key]`。對 Container 內的 File，例如 `details.receipts`，送出的資料會多出最外層 `receipts`，而 `details.receipts` 缺失。`useRuntimeSubmit()` 直接送出該 submission；`FmFormSubmissionValidator.validateScope()` 禁止未宣告的最外層 key，並在 Container／Grid 內另行驗證子欄位，所以正常 UI 上傳成功後仍可能無法送單。Grid 每列還需要列索引，現有附件狀態只以 field key 儲存，無法區分不同列。
- 證據：`frontend-v-nx/composables/useRuntimeAttachments.ts` 的 `collectFileFields()`、`uploadAttachment()`、`deleteAttachment()`；`frontend-v-nx/composables/useRuntimeSubmit.ts` 的送出資料；`backend/app/src/main/java/org/qifu/fm/domain/runtime/FmFormSubmissionValidator.java` 的 `validateScope()`／`validateComponent()`；`FmFormDesignValidator.validateNested()` 允許巢狀元件。此為靜態路徑推論，未建立並發布巢狀附件表單重現。


### BUG-121：申請人可在執行中流程刪除已鎖定或隱藏欄位的正式附件

- 嚴重度：中。性質：Java 附件欄位修改權限；正式附件現況與關卡權限組合尚未建立重現。
- 預期：正式附件刪除應依目前 Task Form Rule／欄位權限判斷 `FIELD_KEY` 是否可修改；流程執行中不等於所有欄位仍可編輯。
- 實際：`FmAttachmentDownloadService.delete()` 只驗證附件仍 ACTIVE、呼叫者是 `OWNER_ACCOUNT` 或 `INITIATOR_ACCOUNT`，以及流程 `INSTANCE_STATUS='RUNNING'`，便將附件設為 DELETED。它沒有查目前 Task Form Rule 的 `FIELD_POLICY`，也沒有要求該欄位為 WRITE。即使欄位在簽核階段是 READ、HIDDEN 或 NONE，申請人仍可直接呼叫刪除 API 移除正式附件；後續附件清單也不再顯示該檔。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentDownloadService.java` 的 `delete()`、`taskFieldPolicy()`、`fieldVisible()`；`backend/doc/22-附件上傳與儲存規格.md` 第 8 節要求 Task Form Rule／欄位權限允許修改。此為靜態路徑推論，未修改正式附件資料重現。


### BUG-122：送單驗證後 Session 到期，清理排程可先刪暫存檔使送單失敗

- 嚴重度：中。性質：Java 送單與清理排程並發；未在到期瞬間執行競態重現。
- 預期：送單開始正式綁定並通過 Session 有效期檢查後，清理排程應避免刪除這次送單即將搬移的檔案；兩條路徑應以鎖或原子狀態轉換互斥。
- 實際：`FmAttachmentBindingService.bind()` 用一般 `SELECT COUNT(*)` 檢查 OPEN 且未到期，之後查 TEMPORARY 明細再逐檔 `promote()`，期間沒有鎖住 Session。若 Session 在檢查後到期，`FmAttachmentTemporaryCleanupJob.cleanupExpiredSessions()` 可讀到仍 OPEN 的 Session，執行 `deleteTemporary()`。清理先刪實體檔、後更新資料庫；送單即使已讀到暫存明細，搬檔時也可能因來源檔案消失而失敗。送單交易回滾無法還原由清理排程刪除的檔案，使用者須重新上傳。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentBindingService.java` 的 `bind()`；`FmAttachmentTemporaryCleanupJob.cleanupExpiredSessions()`／`cleanupSession()`；`FmAttachmentStorageService.promote()`。此為到期時交易交錯的靜態推論，未建立資料或故意觸發排程重現。


### BUG-123：申請詳情刪除正式附件後，表單內仍顯示舊附件連結

- 嚴重度：低。性質：Vue 唯讀表單呈現；未在瀏覽器實際刪除附件重現。
- 預期：正式附件刪除成功後，申請詳情的附件清單與表單欄位應同步移除該附件，避免顯示無法下載的舊連結。
- 實際：`pages/requests/[id].vue` 的 `renderData()` 在載入時以 `attachments.value` 將表單快照中的附件 ID 轉成 Form.io File 物件與下載連結。`deleteAttachment()` 成功後只從 `attachments.value` 過濾該檔，沒有重新執行 `renderData()` 或更新現有 `formInstance.submission`。因此側邊正式附件清單立刻消失，但唯讀表單仍保留舊連結；點擊時 `handleFormAttachmentClick()` 已無法從新清單找到附件，故不會下載。
- 證據：`frontend-v-nx/pages/requests/[id].vue` 的 `hydrateAttachmentFields()`、`renderData()`、`deleteAttachment()`、`handleFormAttachmentClick()`。此為 Vue 狀態流程推論，未用瀏覽器操作重現。


### BUG-124：附件 File Types 全部無效時，上傳限制回退成所有允許副檔名

- 嚴重度：中。性質：表單發布驗證與上傳規則；未建立錯誤配置的表單重現。
- 預期：設計者明確設定附件 `fileTypes` 時，發布應拒絕不在後端白名單內的值；上傳端不得把無效限制解讀成允許全部類型。
- 實際：`FormDesigner.normalizeAttachmentComponents()` 只在 `fileTypes` 空陣列時填入預設值，對非空但無效的自訂值會保留。`FmFormDesignValidator.validateComponents()` 遞迴檢查 key 等設定，沒有驗證 File Types；發布可保存例如僅有 `.exe` 或拼錯的 `.pdff`。`FmAttachmentUploadLogicServiceImpl.configuredExtensions()` 忽略不在白名單內的 token，若結果為空，直接回傳完整 `ALLOWED_EXTENSIONS`。因此這類表單在後端反而可上傳 PDF、圖片、Office、壓縮檔等所有系統支援類型；前端 `acceptedFileTypes()` 也同樣回退成全部類型。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue` 的 `normalizeAttachmentComponents()`；`frontend-v-nx/composables/useRuntimeAttachments.ts` 的 `acceptedFileTypes()`；`backend/app/src/main/java/org/qifu/fm/domain/form/FmFormDesignValidator.java` 的 `validateComponents()`；`FmAttachmentUploadLogicServiceImpl.configuredExtensions()`；`backend/doc/22-附件上傳與儲存規格.md` 第 2 節要求發布檢查 Extension 白名單。此為靜態路徑推論，未發布錯誤配置的表單。


### BUG-125：表單可發布超過全域上傳上限的單檔大小，畫面允許但儲存失敗

- 嚴重度：中。性質：表單發布與附件上傳大小契約；未建立超過 8 MB 的實際附件重現。
- 預期：表單設定的單檔大小不得超過系統可儲存的上限；若超過，發布時應拒絕或在畫面明確顯示實際較小的上限。
- 實際：設計器允許 `fileMaxSize` 自訂，`FmFormDesignValidator` 發布時不檢查此值。前端 `useRuntimeAttachments.uploadAttachment()` 只按表單的 `fileMaxSize` 判斷並顯示該上限；後端 `FmAttachmentUploadLogicServiceImpl.validateUpload()` 同樣只先比對表單上限。但 `FmAttachmentStorageService` 另外使用 `page.maxUploadSize` 作為最終大小上限，現行 `appConfig.properties` 設為 8 MiB，`storeTemporary()` 會拒絕超過該值的檔案。故例如表單設定 10 MB，畫面顯示可上傳 10 MB，8～10 MB 的檔案仍在儲存階段失敗。Servlet multipart 上限另為 10 MB，亦未與表單發布檢查整合。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue` 的 `normalizeAttachmentComponents()`；`frontend-v-nx/composables/useRuntimeAttachments.ts` 的 `uploadAttachment()`；`backend/app/src/main/java/org/qifu/fm/domain/form/FmFormDesignValidator.java` 的 `validateComponents()`；`FmAttachmentUploadLogicServiceImpl.validateUpload()`；`FmAttachmentStorageService.validateSize()`；`backend/app/src/main/resources/appConfig.properties` 的 `page.maxUploadSize`。此為靜態配置與程式路徑推論，未實際上傳超限檔案。


### BUG-126：單檔附件欄位可設定並送出多個附件

- 嚴重度：低。性質：Form.io Schema 與附件件數契約；未建立 `multiple=false` 且 `maxNumberOfFiles>1` 的表單重現。
- 預期：`multiple=false` 的 File 欄位至多一個有效附件；若設計者將件數設為大於 1，發布應拒絕或將限制收斂為 1。
- 實際：`FormDesigner.normalizeAttachmentComponents()` 保留既有的 `maxNumberOfFiles`，即使 `multiple=false` 也不調整。前端 `useRuntimeAttachments.uploadAttachment()` 按 `maxNumberOfFiles` 計數，使用者可逐次上傳多檔；後端 `FmAttachmentUploadLogicServiceImpl.attachmentRule()` 也按此值允許 1～20 件，沒有與 `multiple` 核對。`FmFormSubmissionValidator.validateValue()` 在 `multiple=false` 時把整個附件 ID 陣列交給 `validateScalar()`，而 `file` 類型沒有型別限制，因此多個 ID 可通過正式送單驗證。
- 證據：`frontend-v-nx/pages/fm_prog005d0001/components/FormDesigner.vue` 的 `normalizeAttachmentComponents()`；`frontend-v-nx/composables/useRuntimeAttachments.ts` 的 `uploadAttachment()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAttachmentUploadLogicServiceImpl.java` 的 `attachmentRule()`；`FmFormSubmissionValidator.validateValue()`／`validateScalar()`。此為靜態路徑推論，未發布該設定或實際送單。


### BUG-127：同一附件欄位並發上傳可超過件數與總容量上限

- 嚴重度：中。性質：Java 並發限制；未發出同 Session 同欄位的並發請求重現。
- 預期：表單 File 欄位的 `maxNumberOfFiles` 與 `flowmintMaxTotalSize` 應對同一 Upload Session 嚴格生效，並發上傳也不得超過設定。
- 實際：`FmAttachmentUploadLogicServiceImpl.upload()` 先以 `validateFileCount()` 查 TEMPORARY 筆數，再以 `validateTotalSize()` 加總大小，接著寫實體檔及新增明細。查詢未鎖 Session 或檔案明細，也沒有原子保留件數／容量。兩個請求若同時看到相同的舊計數，都可通過並各自新增檔案；例如上限 1 件且原本 0 件時可得到 2 件，或兩檔相加超過總容量。`FmAttachmentBindingService.bind()` 只驗證 `SCAN_STATUS`，未重新計算欄位件數與總容量，超限附件仍可能正式綁定。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAttachmentUploadLogicServiceImpl.java` 的 `upload()`、`validateFileCount()`、`validateTotalSize()`；`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentBindingService.java` 的 `bind()`。規格第 22 章已指出全域頻率限額非原子；本項是另外的每欄位件數與容量限制。屬並發路徑靜態推論，未實際重現。

### BUG-128：暫存檔正式綁定時未重新核對實體 SHA-256 與中繼資料

- 嚴重度：低。性質：Java 檔案完整性；未修改暫存檔實體內容重現。
- 預期：送單前應核對實體檔案仍存在，並重新計算 SHA-256 與上傳時保存的中繼資料一致，再搬入正式區；這是附件規格第 7 節的明確要求。
- 實際：上傳時 `FmAttachmentStorageService.write()` 計算 SHA-256 並存入暫存明細；`FmAttachmentBindingService.bind()` 讀出 `CONTENT_SHA256` 與 `SCAN_STATUS`，只檢查狀態為 `CLEAN`，隨後直接 `promote()` 並原樣複製 SHA-256 到正式附件資料列。它沒有重新讀取實體檔計算 hash，也未核對實際大小。若暫存目錄內容在上傳與送單之間損壞或被改動，正式附件的完整性資料便與內容不符。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentStorageService.java` 的 `write()`／`promote()`；`FmAttachmentBindingService.bind()`；`backend/doc/22-附件上傳與儲存規格.md` 第 7 節第 5 項。此為規格對照及靜態推論，未變動任何檔案重現。


### BUG-129：附件上傳或建立 Session 期間切換表單，舊回應可能覆蓋新表單狀態

- 嚴重度：中。性質：Vue 非同步上傳與表單切換；未用瀏覽器慢速網路重現。
- 預期：切換已選表單時，仍在進行的舊表單上傳應取消或忽略其結果；舊 Upload Session 的附件不可寫入新表單的附件清單及 submission。
- 實際：起單頁以 `watch(selectedForm, renderForm)` 切換表單；`renderForm()` 先呼叫 `destroyForm()`，其 `resetAttachments()` 清空附件清單與 Session，再建立新表單／Session。`useRuntimeAttachments.uploadAttachment()` 在送出請求後沒有保存並核對表單版本、Session 或 render generation；回應成功時直接把附件推入目前的 `attachmentFiles.value[field.key]`，並寫入目前 `getFormInstance().submission.data[field.key]`。若新舊表單有同名 File key，舊 Session 的附件 ID 會出現在新表單，但正式送單傳的是新 Session ID；若新表單沒有該 key，push 會遇到未定義陣列並使已成功上傳的暫存檔留在舊 Session。建立 Upload Session 本身也有同類競態：`createUploadBatch()` 發出請求後未核對選擇版本或 render generation，舊表單的 Session 回應可在新表單建立後覆寫共用的 `uploadSessionId`；`renderForm()` 在等待建批次後也沒有立即重查 generation。
- 證據：`frontend-v-nx/pages/requests/start/[processDefId].vue` 的 `watch(selectedForm, renderForm)`；`frontend-v-nx/composables/useRuntimeForm.ts` 的 `destroyForm()`／`renderForm()`；`frontend-v-nx/composables/useRuntimeAttachments.ts` 的 `resetAttachments()`／`uploadAttachment()`／`createUploadBatch()`。此為非同步交錯的靜態推論，未在瀏覽器操作重現。


### BUG-130：Upload Session 到期後未再上傳，直接送單會使用舊 Session 而失敗

- 嚴重度：中。性質：Vue 附件 Session 生命週期；未將頁面保持超過 24 小時實際重現。
- 預期：送單前應確認 Upload Session 仍有效；若已到期，應先建立新 Session 並要求重傳已失效附件，或在沒有附件時不傳舊 Session ID。
- 實際：`useRuntimeForm.renderForm()` 在含 File 欄位的表單載入後建立 Upload Session，有效期 24 小時。`useRuntimeAttachments.uploadAttachment()` 只在使用者再次選檔時檢查 `uploadBatchExpiresDate` 並建立新 Session；`useRuntimeSubmit.submitOnce()` 直接提交目前的 `uploadSessionId`，沒有到期檢查。若頁面停留超過 24 小時，使用者修改一般欄位後直接送單，即使附件欄位非必填且沒有附件，後端 `FmAttachmentBindingService.bind()` 仍會因 Session 已過期而拒絕，整筆送單失敗；已有附件時也沒有先提示重新上傳。
- 證據：`frontend-v-nx/composables/useRuntimeForm.ts` 的 `renderForm()`；`useRuntimeAttachments.ts` 的 `uploadAttachment()`／`createUploadBatch()`；`useRuntimeSubmit.ts` 的 `submitOnce()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmAttachmentUploadLogicServiceImpl.java` 的 Session 有效期；`FmAttachmentBindingService.bind()` 的到期檢查。此為生命週期靜態推論，未等待 Session 實際到期重現。


### BUG-131：附件上傳遇網路例外時沒有顯示失敗原因

- 嚴重度：低。性質：Vue 上傳錯誤處理；未中斷實際網路請求重現。
- 預期：上傳因網路中斷、HTTP 非成功狀態或重新認證失敗而拋出例外時，畫面應清楚提示失敗並保留可重試狀態。
- 實際：`useApi()` 在 `$fetch` 失敗後會重新拋出例外；`useRuntimeAttachments.uploadAttachment()` 對上傳請求只有 `try/finally`，沒有 `catch`，`showError()` 只處理 API 正常回傳但業務 `success` 為否的情況。網路例外因此沿著 Vue 事件處理器向外拋出，`finally` 雖會清除 `uploadingField` 與檔案輸入，但此頁不會顯示附件上傳失敗的 toast；無 HTTP 狀態的連線錯誤也不會觸發 `useApi()` 的 HTTP alert。
- 證據：`frontend-v-nx/composables/useApi.ts` 的 catch／throw；`frontend-v-nx/composables/useRuntimeAttachments.ts` 的 `uploadAttachment()`；`frontend-v-nx/components/flowmint/start/RuntimeAttachmentFields.vue` 的上傳事件。此為例外路徑靜態推論，未實際斷線重現。


### BUG-132：建立 Upload Session 拋錯後，已顯示表單但未掛載 Script 與 Data Action

- 嚴重度：中。性質：Vue 表單初始化順序與網路錯誤；未故意中斷 Session API 重現。
- 預期：附件 Session 建立失敗時，表單應明確進入不可送單或可重試狀態；Custom JavaScript 與 Data Action 的掛載不應因附件 API 例外而悄悄缺席。
- 實際：`useRuntimeForm.renderForm()` 先建立 Form.io 表單與 submission，再對含 File 欄位的表單 `await createUploadBatch(false)`，此後才呼叫 `attachCustomJavascript()` 與 `attachDataActionBridge()`。`createUploadBatch()` 對業務失敗只回傳 false，但底層 `useApi()` 遇網路錯誤會 throw，這裡沒有 catch。例外會讓 `renderForm()` 中斷，頁面仍保留已建立的表單；附件輸入因無 Session 被停用，但 `StartActionBar` 仍可送單，`useRuntimeSubmit()` 取得的 Script Runner 為 null，故 `beforeSubmit` 不會執行，Data Action 也未掛載。
- 證據：`frontend-v-nx/composables/useRuntimeForm.ts` 的 `renderForm()`；`useRuntimeAttachments.ts` 的 `createUploadBatch()`；`useApi.ts` 的例外重拋；`frontend-v-nx/composables/useRuntimeSubmit.ts` 的可選 `scriptRunner?.("beforeSubmit")`；`frontend-v-nx/pages/requests/start/[processDefId].vue` 的送出按鈕。此為網路失敗路徑靜態推論，未實際斷線重現。


### BUG-015：正式附件讀取只驗證帳號身分，未重新驗證 Tenant membership

- 嚴重度：高。性質：Java 附件存取權限；目前正式附件 0 筆，無資料可做讀取重現。
- 預期：帳號的 Tenant membership 停用或到期後，應無法再透過該 Tenant 的附件 API 讀取正式附件；`README.md` 明定 Tenant membership 失效後 Runtime 不再開放。
- 實際：`FmAttachmentDownloadService.currentAccount()` 只檢查 Tenant ID 非空，並讀取登入帳號。`download()`、`listByProcess()`、`listByTask()` 與 `delete()` 都使用此方法，未呼叫 `FmTenantAccessGuard` 或等價的 membership 有效期間檢查。對附件擁有者或流程申請人，`authorize()`／`authorizeProcess()` 僅比較帳號字串，即使 membership 已撤銷仍可能列出或下載附件。`FmAttachmentUploadController` 也沒有另行檢查 membership。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/attachment/FmAttachmentDownloadService.java` 的 `currentAccount()`、`authorize()`、`authorizeProcess()`；`backend/app/src/main/java/org/qifu/fm/controller/FmAttachmentUploadController.java`。MariaDB 唯讀查詢顯示 `fm_attachment` 與 `fm_form_data` 目前皆為 0 筆；未修改資料。

### 2026-10-07 修復複核（BUG-108、179）

- **BUG-108 已修正（程式路徑與狀態測試確認）**：表單新草稿會把主檔設成 DRAFT，舊發布版本仍可用；原 `startForms()` 透過只查 PUBLISHED 主檔的 `activeFormDef()` 使既有流程載入失敗。現在主檔為 PUBLISHED 或 DRAFT 時均可載入已發布版本。`FmProcessRuntimeFormStatusTest` 驗證 DRAFT 可讀。
- **BUG-179 已修正（程式路徑與狀態測試確認）**：原 `submitAs()` 只檢查表單版本與流程綁定，未檢查表單主檔是否停用。現在新送單在冪等既有結果查詢後，還會檢查主檔狀態；INACTIVE 不能建立新流程實例。`FmProcessRuntimeFormStatusTest` 驗證 INACTIVE 被拒；未執行 HTTP／MariaDB 停用送單整合情境。
### 2026-10-07 修復複核（BUG-176）

- **BUG-176 已修正，先重現再修改**：新增 Container `details.amount` Binding 測試，原驗證器接受錯誤的 `submission.amount`，確認不是誤判。編輯器改用完整 Container 路徑，Metadata 預設對應只在唯一匹配時自動填入；DataGrid／EditGrid 子欄位不再提供單一路徑選項。後端 Binding 驗證改以完整 Schema 資料路徑比對，拒絕錯誤頂層欄位及沒有列索引的 Grid 子欄位。`FmFormDesignValidatorTest` 與前端 `npm run build` 通過；既有錯誤 Binding 需在再次發布前重選路徑，未執行真實 Data Action／瀏覽器測試。
### 2026-10-07 修復複核（BUG-177）

- **BUG-177 已修正，先重現再修改**：新增固定選項 `select`、`radio`、`selectboxes` 的非法值測試，原送單驗證沒有拋錯，確認不是誤判。後端現在對 `dataSrc=values` 的固定選項逐一檢查提交值；多選 Select 的每個項目及 Select Boxes 的 key／布林值也納入。URL 等動態來源維持既有處理，不以 Schema 固定選項誤擋。`FmFormSubmissionValidatorTest` 全數通過；未執行 HTTP／瀏覽器送單測試。
- **BUG-177 補充驗證**：Form.io `radio`／`select` 的 `auto` 型別會將文字選項值如 `"100"`、`"true"` 正規化成數字／布林提交值。新增合法正規化值測試，先確認初版修正會誤擋，再調整固定選項比對接受對應數字／布林；重新執行 `FmFormSubmissionValidatorTest` 全數通過。
### 2026-10-07 BUG-156 修正進度
- **BUG-156 部分修正（新版本發布防護）**：先以 `FmFormDesignValidatorTest.runtimePublishingRejectsUnsupportedCustomValidation` 重現：帶非空 `validate.custom` 的 Schema 原可通過 `validateRuntimeSchema()`。確認 `publish()` 會呼叫此方法、送單後端不執行自訂 JavaScript／JSONLogic，因此不是誤判。
- `validateRuntimeSchema()` 現會遞迴檢查一般與巢狀欄位，拒絕非空的 `validate.custom`、`validate.json`；空字串及空物件／陣列設定仍允許。測試修正前失敗，修正後 `FmFormDesignValidatorTest` 通過。
- **尚未全數修復**：既有 PUBLISHED `FM_GOODS_ACCEPTANCE` v1 已含非空 `deliveryDate.validate.custom`，其後端送單仍不執行該規則；須另行遷移表單版本或實作對應後端驗證後，才可標為完全修正。
### 2026-10-07 BUG-109 修復複核
- **BUG-109 已修正**：核對表單發布會將舊 `PUBLISHED` 版改為 `RETIRED`，而流程規則仍固定指向舊版；原起單查詢只接受 `PUBLISHED`。先以 `FmProcessRuntimeFormStatusTest.retiredFormVersionRemainsAvailableToBoundPublishedProcess` 重現失敗，確認不是誤判。
- 起單現在接受固定綁定的 `PUBLISHED` 或 `RETIRED` 表單版本；送單先檢查表單版確實綁定目前已發布流程，再讀取版本。`DRAFT` 仍拒絕。目標測試通過；尚未執行實際 HTTP 與資料庫發布競態測試。
### 2026-10-07 BUG-110 修復複核
- **BUG-110 已修正（程式與 SQL 路徑）**：核對 `saveDraft()`／`publish()` 原本各自讀取 `DRAFT`，Mapper 更新僅以 OID 為條件，兩交易可先讀後互相覆寫，並非誤判。
- 兩路徑現在於交易內使用 `SELECT ... FOR UPDATE` 取得表單版本，再檢查是否仍為 `DRAFT`。後進交易會等待前一交易完成，重新讀取最新狀態或內容，避免儲存覆寫已發布版本及重複發布。`FmFormVersionMapperXmlTest` 與 `FmFormDraftLockTest` 通過；目前沒有 Draft 資料，尚未執行雙連線 MariaDB 並發測試。
### 2026-10-07 BUG-111 修復複核
- **BUG-111 已修正**：核對 `save()` 原本在 `/version/save-draft` 前呼叫 `apply()`；`apply()` 會重設選中版本並重繪設計器，故草稿儲存失敗時編輯內容確實可能消失，非誤判。
- 現在主檔更新成功後先送草稿儲存，兩步均成功才 `apply()`；草稿回應失敗或請求拋錯時保留目前畫面內容，並提示「主檔已儲存、草稿未儲存」。`npm run build` 通過；未以瀏覽器故意製造草稿 API 失敗進行端到端驗收。
### 2026-10-07 BUG-112 修復複核
- **BUG-112 已修正**：對照設計器 `validatePreview()` 與正式送單 `submitOnce()`，確認試跑原本在 `beforeSubmit` 後未再次呼叫 Form.io `checkValidity()`，可對變更後的無效欄位誤報通過，非誤判。
- 試跑現在於 `beforeSubmit` 成功後再次檢查欄位；失敗時顯示與正式送單相同的提示，不再顯示驗證通過。`npm run build` 通過；尚未以瀏覽器實際執行會清空必填欄位的 Script 驗收。
### 2026-10-07 BUG-113 修復複核
- **BUG-113 已修正**：核對發布路徑原僅檢查 `requestMapping` 物件及目標欄位名稱；執行器對未知來源前綴傳 `null`，確認非誤判。
- 表單發布現在依 Form.io Schema 檢查 Hook 的 `form.`／`json:form.` 來源欄位確實存在，也限制 `context.` 只使用執行器提供的 `taskId`、`processInstanceId`、`businessKey`。`FmFormDesignValidatorTest.taskActionHookRejectsUnknownRequestSource` 與目標測試套件通過；未建立實際 Task Action Hook 執行驗收。
### 2026-10-07 BUG-114 修復複核
- **BUG-114 已修正**：核對 `changeHandler()` 在前一事件尚未結束時，以單一 `pendingChange` 覆寫較早事件；對依 `ctx.changed` 執行的 Script 會漏處理，確認非誤判。
- 改用先進先出待處理佇列，逐筆串行執行 `onFieldChange`；表單卸載時清空佇列並停止後續處理。`npm run build` 通過；尚未以瀏覽器快速異動與非同步 Data Action 做端到端重現。
### 2026-10-07 BUG-115 修復複核
- **BUG-115 已修正（送單一致性檢查）**：核對前端將 `attachmentId` 寫入 submission，原 `bind()` 卻把 Session 內所有 `TEMPORARY` 檔案正式綁定，沒有比對欄位及 ID；確認不是誤判。
- `bind()` 搬檔前從 Form.io Schema 與送出資料收集附件欄位 ID，與 Session 暫存檔的 `FIELD_KEY`／`ATTACHMENT_ID` 集合完全比對；缺少 Session、額外暫存檔、缺少檔案或錯欄位皆拒絕送單，避免正式附件與表單資料不一致。`FmAttachmentBindingServiceTest` 通過；未建立實際資料庫附件與 HTTP 送單驗收。額外暫存檔目前需先刪除再送單，不會自動略過或清理。
### 2026-10-07 BUG-116 修復複核
- **BUG-116 已修正**：核對上傳先寫實體檔、後寫資料庫，原本只捕捉 `ServiceException` 並立即刪檔，無法處理資料庫執行期例外或方法返回後的交易回滾；暫存清理排程也只查資料庫明細，確認不是誤判。
- 上傳現在在檔案寫入後登記交易回滾補償；不論後續是受檢例外、執行期例外或交易回滾，回滾完成時均嘗試刪除實體暫存檔。孤兒清理排程也掃描超過 24 小時的 temporary `.bin`，僅在資料庫查無同 Tenant／Session／File OID 紀錄時隔離。回滾及暫存孤兒掃描測試通過；未實際誘發 MariaDB 提交失敗驗收。
### 2026-10-07 BUG-117 修復複核
- **BUG-117 已修正**：核對使用者刪除與到期清理原本都在資料庫交易提交前刪實體檔；更新或提交回滾後可留下 `TEMPORARY` 紀錄卻無檔案，確認非誤判。
- 兩條路徑現在先把明細更新為 `DELETED`，僅於交易成功提交後刪實體檔。回滾時檔案仍在；若提交後刪檔失敗，日誌記錄錯誤，超過 24 小時的暫存孤兒掃描會隔離不再為 `TEMPORARY` 的實體檔。提交／回滾測試通過；未實際誘發 MariaDB 提交失敗。
### 2026-10-07 BUG-118 修復複核
- **BUG-118 已修正（同 Session 交易互斥）**：核對上傳與正式綁定原本各自讀 `OPEN` 且未鎖 Session，可能交錯產生 `BOUND` Session 下的 TEMPORARY 明細；送出按鈕在上傳中也可按，確認非誤判。
- 上傳寫檔前與送單查暫存明細前，現在都以 `SELECT ... FOR UPDATE` 鎖定同一 OPEN Session，等待後重新核對狀態與到期時間。前端附件上傳期間停用送出按鈕。兩條鎖定 SQL 的測試、後端目標測試及 `npm run build` 通過；尚未執行雙連線 MariaDB 併發送單驗收。
### 2026-10-07 BUG-119 修復複核
- **BUG-119 已修正**：核對 `promote()` 原使用搬檔當下 UTC 月份，而 `readFormal()` 使用 `fm_attachment.CDATE` 的 UTC 月份；跨月時路徑不同，確認非誤判。
- `promote()` 現使用綁定時寫入 `CDATE` 的同一個 `Date` 決定年月目錄。以固定 `2025-01-31T23:59:59Z` 測試搬檔與下載均指向 `2025/01`，附件儲存目標測試通過；未在真實 UTC 月界送單驗收。
### 2026-10-07 BUG-120 修復複核
- **BUG-120 已修正（前端巢狀路徑）**：核對後端 `validateScope()` 要求 Container／Grid 子欄位位於巢狀物件／列資料，前端原本固定寫入最外層 `data[field.key]` 且不同 Grid 列共用同一附件狀態，確認非誤判。
- 起單附件欄位現在依 Container 路徑及 DataGrid／EditGrid 列索引展開，附件 ID 寫回對應 submission 位置；畫面和必填檢查使用各列的獨立狀態。刪除含附件的 Grid 列時保留可刪除的暫存附件提示，清除前阻止送單；上傳途中列位置變更則刪除該次新上傳檔並要求重試。前端 `npm run build` 與後端 Grid 附件收集測試通過；尚未以瀏覽器實際建立巢狀附件表單驗收。
### 2026-10-07 BUG-121 修復複核
- **BUG-121 已修正**：核對規格要求正式附件刪除依目前 Task／欄位修改權限，原刪除 API 只查申請人或發起人及流程 RUNNING，READ／HIDDEN 欄位仍可刪，確認非誤判。
- 刪除現在要求呼叫者有目前可處理的 Active Task，且固定流程版本的 Task Form Rule 對該 `FIELD_KEY` 為 `WRITE`；找不到或無法解析權限時拒絕。申請詳情附件清單回傳 `deletable`，畫面依此顯示刪除按鈕。READ／WRITE 目標測試及 `npm run build` 通過；未用實際流程待辦和正式附件做 HTTP 驗收。
### 2026-10-07 BUG-122 修復複核
- **BUG-122 已修正**：先複核 BUG-118 已讓送單綁定鎖定 OPEN Session；但到期清理仍未鎖定同一列，在送單綁定期間可讀舊狀態，故仍有獨立競態，非重複誤判。
- 到期清理現在在讀取暫存明細前以 `SELECT ... FOR UPDATE` 鎖定 Session，等待後重新確認仍為 OPEN 且已到期。與送單綁定的 Session 列鎖互斥；已鎖定並開始綁定的 Session 不會被清理排程同時處理。`FmAttachmentTemporaryCleanupJobTest` 與附件目標測試通過；尚未進行到期瞬間的雙連線 MariaDB 併發測試。
### 2026-10-07 BUG-123 修復複核
- **BUG-123 已修正**：核對刪除成功後原本只更新側邊附件清單、未更新 Form.io 唯讀 submission；且欄位轉換只遍歷仍有 ACTIVE 附件的欄位，刪除最後一檔時舊 ID 可能留在表單，確認非誤判。
- 詳情頁現在按 Form.io Schema 逐一轉換根層、Container 與 Grid 附件欄位，僅保留仍在正式附件清單的 ID；刪除成功後重繪目前選取的快照。`npm run build` 通過；尚未以瀏覽器刪除實際正式附件驗收。
### 2026-10-07 BUG-124 修復複核
- **BUG-124 已修正**：核對非空但無效的 `fileTypes` 可留下並發布，後端 `configuredExtensions()` 與前端 `acceptedFileTypes()` 在有效集合為空時都回退為完整白名單，確認非誤判。發布重現測試在修正前失敗。
- 表單發布現在拒絕明確設定的無效或混雜無效副檔名；後端上傳端也會拒絕既有錯誤配置，不再回退成允許全部。前端遇此類既有配置不再顯示完整白名單。空設定仍沿用預設。發布與上傳目標測試、`npm run build` 通過；未實際發布錯誤配置或上傳檔案做 HTTP 驗收。
### 2026-10-07 BUG-125 修復複核
- **BUG-125 已修正（新版本發布防護）**：核對發布驗證原本沒有檢查 `fileMaxSize`，上傳規則可接受表單設定的 10 MB，而儲存服務按 `page.maxUploadSize=8388608` 拒絕超過 8 MiB。新增發布回歸測試並確認修正前失敗，因此不是誤判。
- 發布驗證現在讀取與附件儲存服務相同的 `page.maxUploadSize`，遞迴檢查 File 欄位的單檔上限，拒絕超過全域上限、非正數或無效格式；8 MB 設定可通過。`FmFormDesignValidatorTest` 通過。既有已發布的超限表單仍可能顯示原設定值，尚未執行實際 HTTP 超限上傳驗收。
### 2026-10-07 BUG-126 修復複核
- **BUG-126 已修正**：核對 `multiple=false` 與 `maxNumberOfFiles=2` 在設計器和前後端上傳限制仍可保留兩件，送單驗證也允許兩個附件 ID；發布及送單回歸測試均在修正前失敗，確認不是誤判。
- 設計器會將單檔欄位件數收斂為 1；發布拒絕仍設定多件的 Schema。前後端上傳對既有版本也以 1 件為上限，送單驗證拒絕單檔欄位的多個附件 ID，仍接受目前前端使用的單一 ID 陣列。後端目標測試與 `npm run build` 通過；尚未執行瀏覽器及實際上傳驗收。
### 2026-10-07 BUG-127 複核結果
- **BUG-127 已由 BUG-118 修正，原清單描述對現行程式屬過時誤判**：`upload()` 現在於 `validateFileCount()`、`validateTotalSize()` 及寫入明細前，以 `SELECT ... FOR UPDATE` 鎖定同一筆 OPEN Upload Session，且整段位於同一資料庫交易。第二個同 Session 上傳須等待前一交易結束，之後重新查件數與總容量。送單綁定亦鎖同一 Session。既有 `FmAttachmentUploadRollbackTest` 驗證鎖定 SQL，目標測試通過；尚未執行雙連線 MariaDB 並發上傳實測。本項不再修改程式。
### 2026-10-07 BUG-128 修復複核
- **BUG-128 已修正**：對照附件規格第 7 節及實際綁定路徑，確認原本直接搬移暫存檔並複製資料庫保存的大小與 SHA-256，沒有重新讀檔驗證；不是誤判。
- 正式綁定搬檔前現在重新讀取暫存實體檔，核對大小與 SHA-256；內容遭修改、截斷、超出上限或檔案不存在時拒絕綁定。新增同長度但內容遭修改的回歸測試；`FmAttachmentStorageServiceTest` 與 `FmAttachmentBindingServiceTest` 通過。尚未在實際送單流程故意竄改暫存檔進行 HTTP 驗收。
### 2026-10-07 BUG-129 修復複核
- **BUG-129 已修正（前端非同步狀態防護）**：核對 `createUploadBatch()` 與 `uploadAttachment()` 原在 await 後直接更新共用狀態，沒有比對當前表單、Session 或表單實例；`renderForm()` 等待建批次後也未檢查世代。切換表單時舊回應確實可能覆蓋新狀態，非誤判。
- 附件重設時遞增世代；建立 Session、上傳及刪除回應返回後核對世代與 Session，上傳另核對表單實例；過時回應不更新新表單。`renderForm()` 等待建批次後重新檢查 render 世代。`npm run build` 通過；尚未以瀏覽器慢速網路重現交錯。已成功但被忽略的舊 Session 暫存檔仍依既有到期清理機制處理。
### 2026-10-07 BUG-130 修復複核
- **BUG-130 已修正**：核對 `submitOnce()` 原直接帶入目前 `uploadSessionId`，沒有讀 `uploadBatchExpiresDate`；後端綁定會拒絕過期 Session，確認不是誤判。
- 送單前現在檢查 Session 到期時間並預留 30 秒；若已有附件且 Session 過期或不存在，建立新批次、清除失效附件並要求重傳，本次不送出；若沒有附件，過期 Session 不隨送單送出。`npm run build` 通過；尚未讓頁面實際停留至 Session 到期進行瀏覽器驗收。
### 2026-10-07 BUG-131 修復複核
- **BUG-131 已修正**：核對 `useApi()` 對網路及 HTTP 例外會重拋，原 `uploadAttachment()` 的上傳請求僅有 `finally`，建立批次的 await 也沒有捕捉例外；無 HTTP 狀態的網路中斷不會顯示頁面提示，確認不是誤判。
- 上傳及上傳前重建批次的例外現在顯示可重試提示，過時表單回應不顯示錯誤；失敗時清空檔案輸入，允許再次選取同一檔案。`npm run build` 通過；尚未在瀏覽器故意中斷網路驗收。
### 2026-10-08 BUG-132 修復複核
- **BUG-132 已修正**：複核 `renderForm()` 的 Session 建立位於 Script 與 Data Action 掛載之前，網路例外會中斷初始化，頁面原本仍顯示送出按鈕；確認不是誤判。
- 表單現在只有完整建立 Session、掛載 Script 與 Data Action 後才顯示送出按鈕。初始化失敗會顯示錯誤與重試按鈕，重試會重新建立表單和 Session；切換表單時舊請求不會改變新表單狀態。`npm run build` 通過；尚未在瀏覽器故意中斷網路驗收。
### 2026-10-08 BUG-015 修復複核
- **BUG-015 已修正**：核對正式附件四個入口都呼叫 `currentAccount()`，該方法原只呼叫 `FmTenantStatusGuard.requireActive()`；此 Guard 不檢查帳號的 Tenant membership。規格要求一般帳號須有有效 membership，故不是誤判。
- 正式附件的流程清單、待辦清單、下載及刪除現在統一先經 `FmTenantAccessGuard.requireAccess()` 檢查 Tenant 狀態與 membership 有效期；系統管理員維持規格中的例外。`FmAttachmentDownloadServiceTest` 驗證拒絕路徑，目標測試通過；尚未用真實帳號撤銷 membership 後進行 HTTP 驗收。
### 2026-10-08 BUG-156 完成複核
- **BUG-156 已修正**：唯讀查核現有 4 個 PUBLISHED 表單版本，僅 `FM_GOODS_ACCEPTANCE` v1 的 `deliveryDate` 有非空 `validate.custom`，內容要求到貨日期不晚於驗收日期。起單與待辦更新共用的後端驗證器原本完全忽略此規則，故既有版本的漏洞不是誤判。
- 後端驗證器現在對該既有規則執行日期比較，拒絕違規或格式錯誤日期；其他非空 `validate.custom`／`validate.json` 會拒絕送出，避免未知規則靜默略過。新版本發布防護維持不變。`FmFormSubmissionValidatorTest` 目標測試通過；尚未以真實驗收單執行 HTTP 起單及待辦更新驗收。
