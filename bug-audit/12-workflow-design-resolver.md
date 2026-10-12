## 12 流程設計、版本、發布與 Resolver（`FM_PROG004D0001`）— 靜態／唯讀盤查完成

> 最新接手狀態（2026-10-10）：BUG-090、BUG-135 已在 2026-10-06／07 修正，現行驗證器分別檢查所有 Sequence Flow 的條件及 User Task 屬性；BUG-138 已由 Flowable H2 引擎測試驗證修正。本檔下方較早的「尚未修正／待重現」敘述是歷史盤查結果。其他條目仍須逐項確認，不可直接視為已修正。

2026-10-05 收尾結論：既有規格／Java／Vue／唯讀資料核對見下方紀錄。本輪補對 `FMPROG004D0001Controller` 的管理端點、`FmAssignmentResolverService.resolveType()` 的 15 種 Resolver 分支及核決目標 5 種分支、`FmIncidentOperationsLogicServiceImpl` 的 Incident 重派／重試與 ALL／SEQUENTIAL 邊界、設計器的版本匯入／匯出，以及 BPMN／多人簽核相關測試。BUG-085／088～089／096～099／103／107／148～149／180 涵蓋既有 Resolver 差異，BUG-138／139 涵蓋多人簽核 Incident；跨 Tenant 權限由 Controller／Logic 的 Guard 路徑核對。先前 MariaDB 唯讀證據為流程版本 4 筆 PUBLISHED、0 筆 DRAFT，現有員工／規則與權限資料見下方紀錄。本輪未重新連資料庫、部署流程或逐型建立 Preview／Runtime 案例；這些是重現限制，不將 42 條觀察宣稱為已確認缺陷。

> 計數校正（2026-10-04）：本檔有 42 個 `BUG-` 標題，涵蓋流程主檔／版本與設計器 8、BPMN／Gateway／發布 8、起單政策 2、Task Policy／多人簽核 4、Resolver／核決條件 20。這些是待確認問題條目，不能稱為「Resolver 已確認有 42 個 bug」；多數依靜態程式推論，尚未用隔離流程重現。整體 185 個編號均唯一且連續，先前總索引把本單元寫成 41 條、其他單元部分計數也有誤，已依各檔標題重算。

### 模組收尾清單（2026-10-04）

本單元只以「流程設計、版本、發布、Resolver」為範圍；跨到表單、起單、System Task 的問題記回對應單元。下列項目逐項核對規格、Java、Vue 與可用的 MariaDB 唯讀資料後，才能將本單元標為靜態／唯讀盤查完成。已記錄但無隔離資料可重現的競態與故障注入，保留為驗證限制，不使清單無限延長。

- [x] 流程主檔、分類、草稿建立／儲存、版本複製與退役：已核對主要 Logic／Vue／Mapper，問題見 BUG-013、086、091、093、100、104～106。
- [x] BPMN 元件、Gateway、User Task 與 Service Task 發布轉換：已核對驗證器、發布器及相關測試，問題見 BUG-090、133、135～137、145、178；Flowable 對手工 XML 的實際部署影響保留為驗證限制。
- [x] Task Form／Policy／Assignment Rule、起單政策與固定版本引用：已核對發布及 Runtime 的版本查詢，問題見 BUG-014、092、094、101～102。
- [x] 核決權限條件、編輯器、候選合併與 Fallback：已核對主要 Java／Vue 路徑，問題見 BUG-087、095、181～185；缺值及型別的端到端派送保留為驗證限制。
- [x] Resolver 類型與共用員工／任職／群組篩選：核對 15 種分支與既有發布、Preview、Runtime 紀錄；差異見 BUG-085、088～089、096～099、103、107、148～149、180；逐型端到端執行未做。
- [x] Multi-instance／Assignment Incident／重派及人工處理：BUG-138 已以 Flowable H2 隔離流程重現並修正；BUG-139 已修正二次解析，組織異動與循序流程的端到端驗證仍待執行。
- [x] 管理 API、Vue、版本匯入／匯出與 Tenant 邊界：核對 Controller 端點、設計器入口與既有 Guard 路徑；未執行瀏覽器操作。
- [x] MariaDB 唯讀現況與約束：沿用下方已記錄的流程、規則、核決權限表查詢；本輪無新查詢，沒有草稿及流程執行資料可重現時序問題。

**收尾判準：**清單全勾選，新增問題歸入本單元或正確的其他單元，完成證據與未重現限制寫回本檔後，才更新總索引的 `[x]`。每輪先消掉一個未完成項目，不再因相鄰新 BUG 無限擴展同一路徑。

2026-10-03 本輪對照 `backend/doc/04-流程與表單規格.md`、`backend/doc/34-BPMN流程設計操作說明.md`，核對 `FMPROG004D0001Controller` 的流程主檔／版本／預覽／分類／核決權限入口、`FmProcessDefLogicServiceImpl` 的草稿、建版本、發布、分類、起單政策、Gateway 表單欄位與固定帳號／核決權限引用檢查、`FmApprovalAuthorityLogicServiceImpl` 的 CRUD 與 Tenant Guard、`FmProcessPublishValidator`、`FmAssignmentRuleConfigValidator`、`FmBpmnDesignValidator` 的條件式驗證、`FmProcessVersionMapper.xml` 的草稿鎖與一般更新、Resolver 的固定帳號、群組、核決權限、`UP_TO_LEVEL`、部門主管鏈及部分組織規則，以及 Vue `ProcessDesignerForm.vue` 對草稿鎖、發布、預覽和主檔／草稿分步儲存的呼叫。主檔、版本和核決權限操作有 `FmTenantAccessGuard`；未將其他管理端的跨 Tenant 缺口套用到此單元。MariaDB 唯讀確認 `fm_process_version` 有 4 筆 PUBLISHED、0 筆 DRAFT，表有 `LOCK_VERSION` 但一般更新只以 OID 比對；`fm_employee` 有生效期間欄位，目前 140 筆均為 ACTIVE，期間無效者 0 筆；啟用任職搭配失效員工 0 筆；有效主管配置指向失效員工 0 筆；流程分類均為 ACTIVE，`PURCHASE` 有 3 筆、`ADMIN` 有 1 筆未停用流程引用；4 筆 ACTIVE 核決權限均有 ACTIVE 規則，期間無效主檔 0 筆、有效規則指向零有效成員群組 0 筆；現有 4 筆起單政策均為 `ALL/Y`，群組 30 筆有效成員皆屬 ACTIVE 主檔；4 個已發布流程版本各只綁定 1 張表單。本輪另核對發布按鈕與 `publishInTransaction()` 的驗證順序，缺少一次列出錯誤的報告記於 BUG-133；Resolver Preview 未套用自簽政策記於 BUG-134。MariaDB 唯讀仍為 4 筆 PUBLISHED、0 筆 DRAFT。本輪另確認 `FmBpmnDesignValidator` 只對 Service Task 檢查 Flowable 擴充屬性，User Task 缺口記於 BUG-135；Gateway 混用條件空白繞過記於 BUG-136，說明文字欄位誤判記於 BUG-137；多人簽核解析失敗不建立 Assignment Incident 記於 BUG-138；循序簽核二次解析可能使後續 Task 無法指派記於 BUG-139，兩者均尚未以實際流程重現。BPMN 設計驗證缺少 Start 至 Task／End 的連通性檢查記於 BUG-145，尚未執行 Flowable 部署。續查 `FmAssignmentResolverService.resolve()`／`approvalAuthority()`、Assignment Rule 與核決權限 Rule Mapper：兩類規則均依 `RULE_SEQ` 讀取，核決權限候選人以插入順序去重並受 `MAX_RESULTS` 限制；Rule Seq 本身未確認新的排序或截斷缺陷；但群組成員同 `PRIORITY` 時沒有穩定次排序鍵，已記於 BUG-148；部門主管同優先序時直接取第一筆也不穩定，另記 BUG-149。`STOP_AFTER_APPROVAL` 的設計器文案為「符合後停止」，說明明定規則匹配後停止後續處理；程式在條件命中後即停止與此一致，不另列缺陷。目標失效後的實際 Resolver 結果仍未用隔離資料重現。其餘 Resolver、Vue 與資料庫路徑尚未查完，維持部分盤查。

2026-10-04 續核流程版本與發布：`saveDraft()` 只接受 DRAFT，`createVersion()` 以最高版本複製設計 XML、表單／政策／Resolver 規則及起單政策，`publishInTransaction()` 先驗證再部署，接著將舊 PUBLISHED 版本改為 RETIRED 並寫入新版本的部署識別與發布時間。一般路徑的草稿鎖與併發發布問題已分別列為 BUG-013／091，建新版期間主檔狀態造成的起單問題已列為 BUG-100；本輪未找到可獨立列案的新缺口。另核對 `runtimeBpmnXml()` 只改寫 User Task 與 Service Task，Start Event 屬性未清除，已列 BUG-178。下一步檢查流程／End Event 的 Flowable 屬性在 converter 中是否保留，再查發布版本的外部引用與 Resolver 邊界；沒有執行部署或資料庫異動。

2026-10-04 續核 Runtime 版本引用：起單的 `publishedProcessVersion()` 按同 Tenant／流程 ID 取最新 PUBLISHED 版本及其 Flowable Process Definition ID；`startForms()` 與 `ensureFormBound()` 使用該版本號取表單綁定；`FmTaskAssignmentListener.context()` 從流程變數取版本號，再以同一版本讀 Task Policy 與 Assignment Rule。因此既有流程實例的派送規則不會因新版發布而自動改用新版。沒有找到新增缺口。`FmBpmnDesignValidator` 仍只對 Service Task 的屬性做白名單，流程與其他事件屬性尚未有部署驗證；本機未找到可直接檢查 Flowable converter 的依賴 JAR，不把 BUG-178 的 Runtime 影響寫成已重現。下一步查表單主檔狀態變更對已發布流程的起單影響，以及其餘 Resolver 路徑。

2026-10-04 續核候選與 Fallback：`ORG_TITLE` 明確依 `EMPLOYEE_ORG_ASSIGNMENT_ID` 升序查任職，候選達 `MAX_RESULTS` 時有穩定順序；`ORG_DUTY` 無排序的差異已記 BUG-180。主要 Resolver 回傳非 `RESOLVED` 時，`resolve()` 進入 `resolveFallback()`；Fallback 再依類型決定是否查主要任職。主要類型要求任職而申請人沒有任職時會提早回傳，但正式起單也要求有效主要任職，暫無可確認的 Runtime 繞過或錯誤派送。下一步查核決權限多規則的內部目標失敗、候選截斷及 Fallback 的發布／Runtime 一致性；未執行 Preview 或資料異動。

2026-10-04 續核核決條件保存與發布：`ApprovalAuthorityConditionEditor.vue` 使用結構化欄位、運算子與自由輸入比較值；`FmApprovalAuthorityLogicServiceImpl.validateRule()` 呼叫 `FmApprovalAuthorityConditionEvaluator.validateConfig()`，只驗證非空欄位字串、允許運算子與 IN／NOT_IN 的陣列形狀；流程發布的 `validateAuthorityTargets()` 只查內部目標主檔是否存在且啟用。欄位路徑未和綁定表單 Schema 比對的結果已併入 BUG-182。前端可將非數值字串留給 GT／GTE／LT／LTE，後端也未檢查預期值型別；後續核對本機 Jackson 原始碼後，已將 Runtime 型別例外列為 BUG-185。條件編輯器切換運算子的陣列形狀見 BUG-183，數字清單見 BUG-184。下一步查其他 Resolver。

### BUG-013：Groovy 草稿開關關閉時，流程草稿儲存忽略版本鎖

- 嚴重度：中。性質：Java 並行編輯；僅在 `flowmint.groovy.draft-enabled=false` 配置下發生。
- 預期：兩位設計者同時編輯同一流程草稿時，後送者若帶舊的 `expectedLockVersion`，應收到衝突訊息並重新載入，避免覆蓋先送者的 BPMN、表單綁定及簽核設定。
- 實際：`FmProcessDefLogicServiceImpl.saveDraft()` 只有在 `groovyDraftEnabled=true` 分支呼叫 `advanceDraftLock()` 比較預期版本；開關為 `false` 時略過檢查，直接覆寫草稿及各種關聯。前端 `ProcessDesignerForm.vue` 仍送 `expectedLockVersion`，但後端忽略。`@Value` 預設為 `false`，本機特定 profile 才設為 `true`。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `saveDraft()` 與建構子 `@Value`；`frontend-v-nx/pages/fm_prog004d0001/components/ProcessDesignerForm.vue` 送版本值。MariaDB `fm_process_version` 有 `LOCK_VERSION` 欄位，目前 4 筆 Published、0 筆 Draft，無現成草稿可唯讀重現並行衝突。

### BUG-014：流程發布未拒絕沒有有效成員的簽核群組

- 嚴重度：高。性質：Java 發布檢查與 Resolver。
- 預期：`README.md` 的簽核群組驗收條件要求「群組不存在、停用、無有效成員、設定格式錯誤或派送模式不一致時禁止發布」。
- 實際：`FmProcessDefLogicServiceImpl.validateResolverReference()` 對 `APPROVAL_GROUP` 只確認同 Tenant 有 `STATUS='ACTIVE'` 的群組；`validateApprovalGroupModesForPublish()` 只檢查派送模式，沒有查 `fm_approval_group_member` 是否至少有一位目前有效的成員。`APPROVAL_AUTHORITY` 規則以群組為內部目標時，`validateAuthorityTargets()` 也僅確認群組主檔存在。只要主檔啟用，即使成員全部停用或到期，仍可通過發布檢查，Runtime 才會遇到無簽核人。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `validateResolverReference()`、`validateAuthorityTargets()`、`validateApprovalGroupModesForPublish()`；`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmProcessPublishValidator.java` 只驗證指派規則存在。MariaDB 唯讀查詢顯示目前啟用但無有效成員的群組 0 筆，啟用的群組指派規則 29 筆，啟用核決規則指向零有效成員群組 0 筆；未用現況重現。

### BUG-085：固定帳號 Resolver 發布檢查漏掉員工生效期間

- 嚴重度：中。性質：Java 發布檢查與 Runtime 解析條件不一致；現有 ACTIVE 但期間無效員工 0 筆，未重現。
- 預期：流程發布時，`FIXED_ACCOUNT`、其 Fallback 及核決權限內部固定帳號目標，都應符合 Runtime 的有效員工條件；已過期或尚未生效的員工不得作為可解析簽核人通過發布。
- 實際：`FmProcessDefLogicServiceImpl.validateFixedAccounts()` 以 `STATUS='ACTIVE'` 找員工並檢查有效 Tenant membership，卻未檢查員工 `EFFECTIVE_FROM`／`EFFECTIVE_TO`。同服務的 `resolverAccountOptions()` 也會列出這類員工。`FmAssignmentResolverService.activeEmployeeByAccount()` 在 Runtime／Preview 會檢查員工期間，故發布可通過但實際解析結果為找不到有效帳號。
- 證據：`FmProcessDefLogicServiceImpl` 的 `validateResolverReference()`／`validateAuthorityTargets()`／`validateFixedAccounts()`／`resolverAccountOptions()`，`FmAssignmentResolverService.fixedAccounts()`／`activeEmployeeByAccount()`。MariaDB 唯讀確認員工表有期間欄位，目前無符合此缺陷的現成資料；需在隔離資料下驗證實際發布結果。

### BUG-086：編輯流程分類可繞過使用中分類的停用檢查

- 嚴重度：中。性質：Java 同一狀態轉換的驗證不一致；現有資料可確認引用存在，但未執行異動重現。
- 預期：仍有未停用流程引用的分類，不論從專用停用操作或一般編輯儲存送出 `INACTIVE`，都應拒絕停用。
- 實際：`deactivateCategory()` 查詢同 Tenant、同分類且 `STATUS<>'INACTIVE'` 的流程，存在時拒絕；`saveCategory()` 的更新分支只驗證 Tenant 與分類代碼未變，隨即依 `command.status()` 將分類設為 `INACTIVE`，未做引用檢查。直接呼叫 `/category/save` 可繞過 `/category/deactivate` 的限制。
- 證據：`FMPROG004D0001Controller` 的兩個分類端點、`FmProcessDefLogicServiceImpl.saveCategory()`／`deactivateCategory()`。MariaDB 唯讀查詢顯示 `PURCHASE` 分類有 3 筆、`ADMIN` 分類有 1 筆未停用流程引用；`fm_process_category` 的 CHECK 僅限制狀態值，未阻止此轉換。未修改正式資料。

### BUG-087：核決權限沒有啟用規則或不在生效期間時仍可通過流程發布

- 嚴重度：高。性質：Java 發布驗證與 Runtime 解析條件不一致；目前無現成資料重現。
- 預期：流程的 `APPROVAL_AUTHORITY` 規則若引用沒有任何啟用核決規則、尚未生效或已過期的權限主檔，發布時應阻擋，避免簽核節點在 Runtime 必然找不到核決路徑。
- 實際：權限建立時要求 `rules` 非空，但允許全部設為 `INACTIVE`，之後亦可更新為全部停用。流程發布的 `validateResolverReference()` 只要求權限主檔 `STATUS='ACTIVE'`，未檢查 `EFFECTIVE_FROM`／`EFFECTIVE_TO`；`validateAuthorityTargets()` 只遍歷啟用規則，若集合為空就直接回傳 `true`。Runtime `FmAssignmentResolverService.approvalAuthority()` 會檢查主檔期間，在沒有啟用規則時也回傳 `NOT_FOUND`。
- 證據：`FmApprovalAuthorityLogicServiceImpl.validate()`／`validateRule()`／`update()`，`FmProcessDefLogicServiceImpl.validateResolverReference()`／`validateAuthorityTargets()`，`FmAssignmentResolverService.approvalAuthority()`。MariaDB 唯讀查詢顯示目前 4 筆 ACTIVE 權限，沒有「ACTIVE 主檔但零 ACTIVE 規則」或期間無效的資料；未修改資料驗證。

### BUG-088：逐級主管已達目標層級但候選人失效時誤回報解析成功

- 嚴重度：中。性質：Java Resolver 結果狀態與候選人不一致；現有資料無對應情境。
- 預期：`TARGET_LEVEL_HEAD` 的 `UP_TO_LEVEL` 模式須收集有效簽核人直到目標層級；到達目標層級但該層主管的員工或 Tenant membership 已失效、且沒有其他有效候選人時，應回報無法解析，避免 Preview 顯示成功而 Runtime 不能指派。
- 實際：`upToLevelHead()` 呼叫 `addEmployeeCandidate()` 後，只依主管職稱的 `LEVEL_ORDER` 判斷是否到達目標層級，立即回傳 `RESOLVED`；`addEmployeeCandidate()` 遇到失效員工或 membership 會略過，所以可能回傳 `RESOLVED` 且候選清單為空，或在缺少目標層主管的情況下回傳不完整候選鏈。`FmTaskAssignmentListener` 最後會因帳號清單為空而拒絕指派，與預覽狀態不一致。
- 證據：`FmAssignmentResolverService.upToLevelHead()`、`addEmployeeCandidate()`、`activeEmployeeById()`，以及 `FmTaskAssignmentListener.resolveAssignment()`。MariaDB 唯讀查詢顯示目前員工 140 筆均為 ACTIVE、啟用任職搭配失效員工 0 筆；未建立資料重現。

### BUG-089：第一順位部門主管帳號失效時不嘗試有效候補主管

- 嚴重度：中。性質：Java Resolver 候補解析；現有資料無對應失效配置。
- 預期：部門有多筆依優先序排列且期間有效的主管配置時，若第一筆指向的員工或 Tenant membership 失效，應繼續嘗試有效的候補主管。`backend/doc/03-組織與簽核人規格.md` 的解析順序包含 HEAD、ACTING_HEAD／DEPUTY_HEAD 候補。
- 實際：`firstOrgUnitHead()` 只按配置 `STATUS` 與期間篩選後取第一筆；`orgUnitHead()` 隨後才呼叫 `employeeResult()` 檢查員工與 membership，若無效直接回 `NOT_FOUND`。`levelHeadChain()` 同樣只取每層第一筆，`addEmployeeCandidate()` 會略過失效者但不再查同部門下一順位。因而可漏掉仍有效的候補主管。此項與既有主管類型優先序問題不同，重點是第一筆候選失效後沒有繼續搜尋。
- 證據：`FmAssignmentResolverService.firstOrgUnitHead()`、`orgUnitHead()`、`levelHeadChain()`、`activeEmployeeById()`。MariaDB 唯讀查詢顯示目前期間有效的主管配置指向失效員工 0 筆；未建立資料重現。

### BUG-090：非多出口 Gateway 的 Sequence Flow 條件未套用受控格式驗證

- 修正狀態：**已修正（2026-10-06；2026-10-10 複核）**。`validateAllConditions()` 檢查所有非空 Sequence Flow 條件，並拒絕混用邏輯運算子；原多出口 Gateway 的額外規則仍由 `validateGateway()` 處理。下方「實際」為修正前情況。

- 嚴重度：高。性質：BPMN 發布驗證邊界；未以構造的 BPMN 實際部署重現。
- 預期：所有可發布的條件 Sequence Flow 都應符合受控的 `flowmintFormData` 比較式，或明確拒絕不支援條件的來源節點；不能只因線不是多出口 Exclusive／Inclusive Gateway 的出口就接受任意表達式。
- 實際：`FmBpmnDesignValidator.validateGateways()` 僅走訪 Exclusive、Inclusive 與 Parallel Gateway；`validateGateway()` 對出口數不超過 1 的 Gateway 立即回傳，未檢查該線條件。從 Start Event、User Task、Service Task 等其他節點出發的條件 Sequence Flow 亦未受 `CONDITION` 格式檢查。`validateGatewayFormFieldsForPublish()` 只掃描文字中的合法欄位引用，不驗證其餘表達式。手工提交 BPMN XML 因此可能繞過預期的條件白名單；Flowable 對此類條件的實際執行影響仍須隔離環境驗證。
- 證據：`FmBpmnDesignValidator.validate()`／`validateGateways()`／`validateGateway()`、`FmProcessDefLogicServiceImpl.validateGatewayFormFieldsForPublish()`；現有 `FmBpmnDesignValidatorTest` 的不受控條件案例只放在多出口 Gateway。此次僅做靜態盤查，未變更資料庫或部署。

### BUG-091：一般流程草稿併發發布可能重複部署並覆寫部署 ID

- 嚴重度：中。性質：Java／MariaDB 併發發布；現有資料無草稿，未以同時請求實測。
- 預期：同一流程版本從 `DRAFT` 轉為 `PUBLISHED` 應只成功一次；第二個發布請求必須發現版本已改變並拒絕，不能再次部署同版 BPMN。
- 實際：沒有 Groovy 綁定的 `publish()` 直接透過交易執行 `publishInTransaction()`；當 `groovyDraftEnabled=false` 時，`draft()` 只讀取並檢查狀態，沒有 `FOR UPDATE` 或鎖版本更新。兩個交易可先後讀到 `DRAFT`，分別呼叫 Flowable `deploy()`；`FmProcessVersionMapper.update` 最後只以 OID 更新，沒有 `VERSION_STATUS='DRAFT'` 或 `LOCK_VERSION` 條件，較後提交者可能覆寫前者的 Deployment ID。Groovy 有綁定的發布另有編譯證據與鎖處理，本條限一般發布路徑。
- 證據：`FmProcessDefLogicServiceImpl.publish()`／`publishInTransaction()`／`draft()`、`FmProcessVersionMapper.xml` 的 `lockDraft` 與一般 `update`。MariaDB 唯讀 `SHOW CREATE TABLE fm_process_version` 顯示版本唯一鍵與 `LOCK_VERSION`，但目前 4 筆均已發布、0 筆草稿；未執行併發異動驗證。

### BUG-092：停用簽核群組後其成員仍可命中流程起單政策

- 嚴重度：中。性質：Java Runtime 授權與群組狀態不一致；現有群組均啟用且起單政策未使用群組，無資料重現。
- 預期：`APPROVAL_GROUP` 起單政策只應讓目前有效且群組主檔仍為 `ACTIVE` 的成員命中；停用群組應立即停止藉該群組起單。
- 實際：`FmProcessRuntimeLogicServiceImpl.authorizeStart()` 只查 `fm_approval_group_member.STATUS='ACTIVE'` 與成員有效期間，直接收集 `APPROVAL_GROUP_ID`；不查 `fm_approval_group.STATUS`。`FmProcessStartPolicyEvaluator` 只比對 ID。`FmApprovalGroupLogicServiceImpl.deactivate()` 只把群組主檔改為 `INACTIVE`，未停用成員，因此原成員仍可能命中已發布版本的 `APPROVAL_GROUP/Y` 起單政策。
- 證據：`FmProcessRuntimeLogicServiceImpl.authorizeStart()`、`FmProcessStartPolicyEvaluator.matches()`、`FmApprovalGroupLogicServiceImpl.deactivate()`。MariaDB 唯讀查詢顯示起單政策 4 筆均為 `ALL/Y`，目前群組均為 ACTIVE、有效成員共 30 筆，沒有可實際重現的停用群組起單情境。

### BUG-093：設計器儲存草稿失敗時流程主檔可能已先儲存

- 嚴重度：中。性質：Vue／Java 分步儲存一致性；未以故意失敗請求實測。
- 預期：使用者按一次「儲存草稿」時，若 BPMN、節點設定或版本鎖檢查失敗，畫面應清楚表示哪些資料已保存；不應先提示儲存成功，卻只留下主檔變更而草稿仍是舊版。
- 實際：`ProcessDesignerForm.vue.save()` 先呼叫 `/update` 更新流程名稱、分類、排序和說明，`showResponse()` 立即顯示成功；之後才呼叫 `/version/save-draft` 保存 XML、表單、Task Policy、Resolver 與起單政策。兩個 API 各自交易；後者若因驗證或版本鎖失敗，前者不會回滾，畫面只再顯示第二步失敗。使用者會得到一次成功及一次失敗訊息，資料形成部分保存。
- 證據：`frontend-v-nx/pages/fm_prog004d0001/components/ProcessDesignerForm.vue` 的 `save()`／`showResponse()`，`FmProcessDefLogicServiceImpl.update()`／`saveDraft()` 分別標註獨立交易。現有資料沒有 DRAFT 版本，未透過瀏覽器製造失敗情境；此結論依呼叫與交易順序推論。

### BUG-094：Gateway 欄位發布檢查要求所有 Task 表單都含同一欄位

- 嚴重度：中。性質：Java 發布驗證過度限制；現有流程均使用單一表單，無多表單情境重現。
- 預期：每個 User Task 各自綁定一張已發布表單；Gateway 使用的欄位應針對實際提供該條件資料的表單或流程資料契約驗證，不應要求與該 Gateway 無關的其他 Task 表單也含同名欄位。
- 實際：`saveTaskForms()` 允許不同 Task 綁定不同表單；`validateGatewayFormFieldsForPublish()` 卻先擷取 BPMN 中全部 `flowmintFormData.*` 欄位，再對流程的每一筆 Task Form Rule 重複檢查完整欄位集合。只要某張其他 Task 表單缺少其中一欄，就阻擋整個流程發布，即使該欄位在條件使用時已由另一張綁定表單提供。
- 證據：`backend/doc/04-流程與表單規格.md` 第 8 節的每 Task 單一 Form 規格、`FmProcessDefLogicServiceImpl.saveTaskForms()`／`validateGatewayFormFieldsForPublish()`、`FmBpmnDesignValidator.referencedFormFields()`。MariaDB 唯讀查詢顯示現有 4 個已發布流程版本各只有 1 種表單，未用現況重現多表單發布。

### BUG-095：已發布流程引用的核決權限可原地修改並改變後續簽核結果

- 嚴重度：高。性質：發布版本與 Runtime 規則一致性；未異動資料重現。
- 預期：流程發布時通過驗證的核決權限條件與目標，應隨該發布版本固定；調整共用規則應建立可追溯的新版本，避免既有流程實例在相同表單資料下得到不同簽核人。`backend/doc/04-流程與表單規格.md` 明定已發布版本不可修改，並要求發布時驗證核決權限條件。
- 實際：`FmApprovalAuthorityLogicServiceImpl.update()` 可直接修改權限主檔，並由 `replaceRules()` 替換原規則，沒有檢查已發布流程引用或建立規則快照。`FmAssignmentResolverService.approvalAuthority()` 執行時依權限 ID 查目前主檔與規則，故發布後修改共用權限，會改變舊發布版本及既有流程實例後續任務的解析結果。Vue 編輯面板可儲存這些規則；即使已發布版本的面板設為停用，後端 `/approval-authority/update` 仍可直接呼叫。
- 證據：`FmApprovalAuthorityLogicServiceImpl.update()`／`replaceRules()`、`FmAssignmentResolverService.approvalAuthority()`、`FMPROG004D0001Controller` 的 `/approval-authority/update`，以及 `ProcessDesignerForm.vue`／`ApprovalAuthorityPanel.vue`。現有權限與發布流程的引用關係尚未逐筆驗證；結論依靜態呼叫與資料更新路徑推論。

### BUG-096：符合目標層級的主管員工失效時，不繼續尋找上層有效主管

- 嚴重度：中。性質：Java Resolver 候選人解析；現有員工均為 ACTIVE，未建立失效情境重現。
- 預期：`NEXT_HIGHER_LEVEL_HEAD` 應找到下一位有效且層級較高的主管；`TARGET_LEVEL_HEAD` 的 `EXACT_OR_HIGHER` 模式亦應在目前符合條件者失效時繼續沿主管鏈尋找有效人員。
- 實際：`nextHigherLevelHead()` 與 `targetLevelHead()` 找到第一個符合層級的任職後立即 `return employeeResult(...)`。若該任職指向的員工停用、過期或 Tenant membership 失效，`employeeResult()` 回傳 `NOT_FOUND`，外層不再檢查更上層且有效的主管。`EXACT` 模式在沒有其他同層主管時可能本就無候補；本條重點是允許更高層的兩種解析路徑。
- 證據：`FmAssignmentResolverService.nextHigherLevelHead()`、`targetLevelHead()`、`employeeResult()`、`activeEmployeeById()`。此項不同於 BUG-089 的同部門主管順位，也不同於 BUG-088 的 `UP_TO_LEVEL` 成功狀態錯誤。依靜態路徑推論，尚未用資料重現。

### BUG-097：表單選人 Resolver 發布時只查欄位存在，未確認帳號欄位型態

- 嚴重度：中。性質：流程發布驗證與 Runtime 解析契約不一致；尚未建立測試流程重現。
- 預期：`FORM_ACCOUNT_FIELD`／`FORM_ACCOUNT_FIELD_MANAGER` 應只允許能產生員工帳號的表單欄位，發布時須檢查欄位設定與值形態，避免非帳號值被當成簽核人。
- 實際：`validateFormAccountField()` 僅以 `FmFormFieldCatalog.fields()` 回傳的 key 集合檢查欄位存在；Catalog 收集所有輸入型元件，不辨識人員選擇、普通文字、數字或其他欄位。設計器的 `selectedFormFields` 也提供一般表單欄位。數字或一般文字欄位因此可通過發布；Runtime 會把值轉成字串並查員工帳號，通常回 `NOT_FOUND`。
- 證據：`FmProcessDefLogicServiceImpl.validateFormAccountField()`、`FmFormFieldCatalog.collect()`、`ProcessDesignerForm.vue` 的表單欄位選單，以及 `FmAssignmentResolverService.formAccountField()`／`formAccountFieldManager()`。目前僅靜態推論，未部署含非帳號欄位的流程。

### BUG-098：Fallback 表單選人欄位未經發布時的表單欄位存在檢查

- 嚴重度：中。性質：Java 發布驗證遺漏；尚未建立測試流程重現。
- 預期：主要或 Fallback 使用 `FORM_ACCOUNT_FIELD`／`FORM_ACCOUNT_FIELD_MANAGER` 時，都應確認 `fieldKey` 存在於該 User Task 綁定的已發布表單；否則 Fallback 觸發時必然無法依欄位取人。
- 實際：`validateAssignmentRulesForPublish()` 僅在主要 `rule.getResolverType()` 為這兩種類型時呼叫 `validateFormAccountField()`。Fallback 則只經 `FmAssignmentRuleConfigValidator.validate()` 檢查 `fieldKey` 非空，以及 `validateFallbackReference()`；後者的 `validateResolverReference()` 對表單選人類型落入 `default -> true`。因此 Fallback 指向不存在的欄位仍可通過發布，執行時 `readFormField()` 取得 null，回報 `NOT_FOUND`。
- 證據：`FmProcessDefLogicServiceImpl.validateAssignmentRulesForPublish()`／`validateFallbackReference()`／`validateResolverReference()`／`validateFormAccountField()`、`FmAssignmentRuleConfigValidator.validate()`、`FmAssignmentResolverService.resolveFallback()`／`formAccountField()`／`formAccountFieldManager()`。此項與 BUG-097 的既有欄位型態檢查不足不同；目前僅靜態推論。

### BUG-099：表單指定人員主管 Resolver 無法解析設計器宣稱支援的帳號陣列

- 嚴重度：中。性質：Vue 提示與 Java Resolver 契約不一致；尚未以多選欄位起單重現。
- 預期：設計器對 `FORM_ACCOUNT_FIELD` 與 `FORM_ACCOUNT_FIELD_MANAGER` 共用「欄位值可為單一帳號或帳號陣列」提示；若允許主管模式選用多選欄位，Runtime 應逐筆取得指定人員的直屬主管，並依 `MAX_RESULTS` 去重截斷；若主管模式只接受單人，設計器及發布檢查應明確限制。
- 實際：`formAccountField()` 將 List 視為多筆帳號處理；`formAccountFieldManager()` 卻沒有 List 分支，直接對整個陣列呼叫 `toString()`，再以類似 `[account1, account2]` 的字串查員工帳號，回傳 `NOT_FOUND`。發布檢查只確認欄位 key 存在，沒有拒絕多選欄位。
- 證據：`ProcessDesignerForm.vue` 共用的表單選人設定與提示、`FmAssignmentResolverService.formAccountField()`／`formAccountFieldManager()`、`FmProcessDefLogicServiceImpl.validateFormAccountField()`。此項依程式與畫面靜態推論，現有資料未提供多選起單重現。

### BUG-100：建立下一版草稿會使已發布流程暫時無法起單

- 嚴重度：高。性質：流程版本與正式起單可用性；現有資料沒有草稿，尚未異動重現。
- 預期：已發布流程建立下一版草稿後，舊的 Published 版本應繼續供申請中心選擇並起單，直到新版本發布或流程明確停用。編輯草稿不應中斷既有正式服務。
- 實際：`FmProcessDefLogicServiceImpl.createVersion()` 複製上一版為 Draft 後，將流程主檔 `status` 設為 `DRAFT`。`FmProcessRuntimeLogicServiceImpl.startCatalog()` 只查主檔 `STATUS='PUBLISHED'`；正式起單的 `activeProcessDef()` 也使用相同篩選。舊版 `fm_process_version` 雖仍是 `PUBLISHED`，其主檔因狀態改變而被排除，直到新草稿成功發布時主檔才重設為 `PUBLISHED`。
- 證據：`FmProcessDefLogicServiceImpl.createVersion()`／`publishInTransaction()`；`FmProcessRuntimeLogicServiceImpl.publishedParameters()`／`startCatalog()`／`activeProcessDef()`／`publishedProcessVersion()`。README 流程新版本操作與申請中心故障排查均以已發布版本可起單為背景。現有 4 筆版本均為 PUBLISHED、0 筆 DRAFT；未建立新草稿驗證實際 HTTP 行為。

### BUG-101：發布起單政策只檢查允許規則數量，未重驗對象有效性

- 嚴重度：中。性質：Java 發布檢查與 Runtime 起單資格；現有起單政策均為 `ALL/Y`，無資料重現。
- 預期：發布時若允許起單的 `ACCOUNT`、`ORG_UNIT` 或 `APPROVAL_GROUP` 對象已停用、過期或不再可用，應重新驗證；若所有允許規則都無法命中有效申請人，應阻擋發布或提示無人可起單。
- 實際：`saveStartPolicies()` 在草稿儲存時檢查對象 ID，但帳號只查 ACTIVE 員工、部門只查同 Tenant ID；後續資料可能變更。`validateStartPoliciesForPublish()` 只要求政策非空且至少一筆 `allowStart='Y'`，不再查對象狀態或有效期間。因此唯一允許規則即使已指向失效對象，流程仍可通過發布；Runtime 會以有效申請人、任職及群組成員判斷，無人命中時拒絕起單。此項聚焦發布時重驗，與 BUG-092 的停用群組成員在 Runtime 仍可命中不同。
- 證據：`FmProcessDefLogicServiceImpl.saveStartPolicies()`／`validateStartPoliciesForPublish()`、`FmProcessRuntimeLogicServiceImpl.activeApplicant()`／`authorizeStart()`、`FmProcessStartPolicyEvaluator`。MariaDB 先前唯讀查詢顯示現有 4 筆起單政策均為 `ALL/Y`；未異動資料測試失效對象。

### BUG-102：Fallback 與核決權限內部簽核群組未檢查派送方式一致

- 嚴重度：中。性質：Java 發布驗證遺漏；未建立不一致的測試流程重現。
- 預期：只要 Task 的主要、Fallback 或核決權限內部 Resolver 可能解析到簽核群組，發布前都應比對該群組 `ASSIGNMENT_MODE` 與 Task Policy 的派送方式；不一致時阻擋發布。
- 實際：`FmApprovalGroupModeValidator.validate()` 只處理 `rule.getResolverType() == APPROVAL_GROUP` 的主要規則，不解析 `fallbackConfig`，也不展開 `APPROVAL_AUTHORITY` 內部規則。`validateFallbackReference()` 與 `validateAuthorityTargets()` 只確認群組主檔存在且啟用，未比對派送方式。Runtime 透過 Fallback 或核決權限解析該群組後，仍依原 Task Policy 派送，可能與群組自身模式不一致。
- 證據：`FmProcessDefLogicServiceImpl.validateApprovalGroupModesForPublish()`／`validateFallbackReference()`／`validateAuthorityTargets()`、`FmApprovalGroupModeValidator.validate()`、`FmAssignmentResolverService.resolveFallback()`／`resolveAuthorityTarget()`。此項與 BUG-014 的空群組成員檢查不同；目前僅靜態推論。

### BUG-103：直屬主管與主管鏈 Resolver 未處理 ORG_HEAD／PARENT_HEAD 來源

- 嚴重度：高。性質：Java Resolver 與員工任職契約不一致；現有資料符合觸發條件，但未執行實際任務指派重現。
- 預期：任職設定 `MANAGER_SOURCE='ORG_HEAD'` 時，`DIRECT_MANAGER` 應依本部門有效正式主管解析；`PARENT_HEAD` 應依父部門主管解析。`MANAGER_CHAIN`、`NEXT_HIGHER_LEVEL_HEAD` 及 `TARGET_LEVEL_HEAD` 逐級查找時，也應依各任職的主管來源推進，而非只依顯式任職 ID。`FORM_ACCOUNT_FIELD_MANAGER` 對表單指定人員也應套用同一主管來源契約。
- 實際：`FmEmployeeOrgAssignmentLogicServiceImpl.apply()` 對非 `EXPLICIT` 來源將 `DIRECT_MANAGER_ASSIGNMENT_ID` 設為 null。`FmAssignmentResolverService.directManager()` 看到此 ID 空白便直接 `NOT_FOUND`；`managerChain()`、`nextHigherLevelHead()`、`targetLevelHead()`／`upToLevelHead()` 也都只沿此 ID 查找，因此 `ORG_HEAD`／`PARENT_HEAD` 的自動主管來源在這些 Resolver 中失效。`FORM_ACCOUNT_FIELD_MANAGER` 最後也呼叫 `directManager()`，同受影響。`INITIATOR_ORG_HEAD` 是另一種直接查部門主管的 Resolver，不補足上述路徑。
- 證據：`backend/doc/03-組織與簽核人規格.md` 的主管來源契約、`FmEmployeeOrgAssignmentLogicServiceImpl.apply()`、`FmAssignmentResolverService.directManager()`／`managerChain()`／`formAccountFieldManager()`／層級主管方法；`FmOrganizationHealthLogicServiceImpl.managerAssignment()` 已展示對 `ORG_HEAD`／`PARENT_HEAD` 的動態解析。MariaDB 唯讀查詢顯示 ACTIVE 任職中 `ORG_HEAD` 74 筆、`PARENT_HEAD` 35 筆，這 109 筆的 `DIRECT_MANAGER_ASSIGNMENT_ID` 均為 null；另有 `EXPLICIT` 31 筆、`NONE` 1 筆。已發布流程 ACTIVE 規則含 `DIRECT_MANAGER` 2 筆與 `FORM_ACCOUNT_FIELD_MANAGER` 1 筆；尚未觸發正式任務驗證失敗比例。

### BUG-104：切換流程版本會丟失未儲存 BPMN 圖，並保留未儲存節點設定

- 嚴重度：中。性質：Vue 草稿編輯狀態與資料一致性；未用瀏覽器操作重現。
- 預期：草稿 BPMN 圖有未儲存變更時，切換版本應提示並讓設計者儲存或放棄；切回時圖與 Task 表單／政策／Resolver 設定應來自同一編輯狀態。
- 實際：版本按鈕直接呼叫 `openVersion(version)`。該函式立即改變 `selectedVersion` 並以 `modeler.importXML(version.bpmnXml)` 載入已儲存的 XML，沒有 dirty 檢查或切換確認；未儲存的圖形變更因此消失。Task Forms、Task Policies、Assignment Rules 等陣列則直接掛在 `form.value.versions` 的版本物件上，編輯時會原地修改；切回原版本後這些未儲存設定仍可能存在。隨後儲存草稿可能把舊圖與新設定一併送出，造成節點 ID 不一致或覆蓋設計者原意。
- 證據：`ProcessDesignerForm.vue.openVersion()`、版本按鈕 `@click="openVersion(version)"`、`selectedTaskRule`／`selectedTaskPolicy`／`selectedAssignmentRule` 對版本陣列的引用及 `save()` 的 `modeler.saveXML()` 與設定收集。此項依前端狀態流程推論，未做互動重現。

### BUG-105：停用流程可透過建立並發布新版本重新上架

- 嚴重度：中。性質：流程主檔生命週期；現有流程主檔沒有 INACTIVE，未異動重現。
- 預期：管理者明確停用流程後，建立草稿或發布版本不應無條件清除停用狀態；若允許恢復，應有明確重新啟用操作與權限、驗證流程。
- 實際：`deactivate()` 將主檔設為 `INACTIVE`。`createVersion()` 沒有檢查主檔狀態，只要求目前無 Draft，建立新 Draft 後把主檔改為 `DRAFT`；`publishInTransaction()` 成功時又直接改為 `PUBLISHED`。即使停用時已有 Draft，也可直接發布。Vue 的「建立新版本」按鈕只判斷是否已有 Draft，未依主檔 `INACTIVE` 禁用。流程因此可透過版本操作重新進入申請中心與正式起單清單，繞過停用意圖。
- 證據：`FmProcessDefLogicServiceImpl.deactivate()`／`createVersion()`／`publishInTransaction()`，`ProcessDesignerForm.vue` 的建立新版本與發布按鈕，`FmProcessRuntimeLogicServiceImpl.activeProcessDef()` 的主檔 `PUBLISHED` 篩選。此項依狀態轉換推論，未以停用流程實際操作驗證。

### BUG-106：分類停用後流程從申請中心消失，但直接起單 API 仍可使用

- 嚴重度：中。性質：流程分類與 Runtime 可用性判斷不一致；現有分類均啟用，未異動重現。
- 預期：若停用分類代表該分類流程不可供新申請使用，申請中心清單、起單頁載入、正式送單及流程發布都應套用一致的分類狀態檢查。
- 實際：`startCatalog()` 只載入 ACTIVE 分類，查不到流程分類時略過該流程；`loadStart()` 與 `submitAs()` 卻只查 `PUBLISHED` 流程主檔與版本，不再驗證分類狀態，已知流程 ID 的呼叫者仍可能直接起單。`publishInTransaction()` 亦未呼叫 `requireActiveCategory()`，可將已停用分類下的草稿發布。`saveCategory()` 可繞過分類專用停用入口的引用檢查，形成此狀態；該繞過已記於 BUG-086，本項聚焦直接起單與發布缺少同一檢查。
- 證據：`FmProcessRuntimeLogicServiceImpl.startCatalog()`／`loadStart()`／`submitAs()`／`activeProcessDef()`、`FmProcessDefLogicServiceImpl.publishInTransaction()`／`requireActiveCategory()`／`saveCategory()`。MariaDB 先前唯讀查詢顯示現有分類均為 ACTIVE，未用停用分類做 HTTP 重現。

### BUG-107：職稱、職務及簽核層級發布檢查漏掉生效期間

- 嚴重度：中。性質：Java 發布驗證與 Runtime 解析條件不一致；尚未建立失效參照重現。
- 預期：流程發布時引用 `TARGET_LEVEL_HEAD`、`ORG_TITLE`、`ORG_DUTY` 或核決權限內部對應目標，應確認參照主檔目前啟用且在生效期間，避免發布後必然解析不到目標。
- 實際：`validateResolverReference()` 對這三類只用 `exists()` 查同 Tenant 且 `STATUS='ACTIVE'` 的主檔；`validateAuthorityTargets()` 的內部目標也相同，未檢查 `EFFECTIVE_FROM`／`EFFECTIVE_TO`。Runtime 的 `approvalLevelById()`、`orgTitle()`、`orgDuty()` 則會過濾有效期間。未生效或已過期但仍標為 ACTIVE 的參照可通過發布，執行時回 `NOT_FOUND`。
- 證據：`FmProcessDefLogicServiceImpl.validateResolverReference()`／`validateAuthorityTargets()`／`activeOptionParameters()`／`exists()`；`FmAssignmentResolverService.approvalLevelById()`／`orgTitle()`／`orgDuty()`；三種主檔 Entity 都有有效期間欄位。此項不同於 BUG-085 的固定帳號員工期間與 BUG-087 的核決權限主檔期間。MariaDB 唯讀查詢顯示目前 ACTIVE 但未生效或已過期的職稱、職務、簽核層級、核決權限主檔均為 0 筆，尚無資料重現。

### BUG-133：流程發布只回報第一個驗證錯誤，沒有規格要求的完整驗證報告

- 嚴重度：低。性質：流程發布診斷與使用者操作；未提交故意含多種錯誤的草稿重現。
- 預期：`backend/doc/04-流程與表單規格.md` 第 145 行要求發布前以同一份 validation report 一次列出 BPMN、Form、Task Policy、Assignment Rule、核決權限、Resolver Preview 與 Gateway 條件錯誤，全部通過後才部署。
- 實際：`ProcessDesignerForm.vue.publish()` 先呼叫 `/version/save-draft`，再直接呼叫 `/version/publish`，沒有取得或顯示彙總驗證報告。`FmProcessDefLogicServiceImpl.publishInTransaction()` 逐一呼叫驗證器，任一 `ServiceException` 立即中止，Controller 回傳一般錯誤訊息。Resolver Preview 另由使用者手動按 `/resolver-preview`，不屬於發布前必經的彙總報告。草稿若同時有多個缺陷，使用者需反覆修正、重試才能逐項看到錯誤。
- 證據：`frontend-v-nx/pages/fm_prog004d0001/components/ProcessDesignerForm.vue` 的 `publish()`／`previewResolvers()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `publish()`／`publishInTransaction()`；`FMPROG004D0001Controller.java` 的 `/version/publish`；規格第 145 行。此為規格與靜態流程對照，未實際發布錯誤草稿。

### BUG-134：Resolver Preview 將原始候選人標示為簽核人，未說明與實際派送的差異

- 嚴重度：低。性質：預覽結果語意與提示不清；未用自簽規則啟動實際流程重現。
- 預期：簽核人解析預覽應反映已儲存的 Task Policy；若候選人等於申請人且自簽政策為 `SKIP_TO_NEXT`、`REQUIRE_ALTERNATE` 或 `INCIDENT`，應顯示實際派送結果或清楚標示預覽尚未套用政策。
- 實際：`FmProcessDefLogicServiceImpl.resolverPreview()` 只載入該版本 ACTIVE Assignment Rule，對每筆直接呼叫 `FmAssignmentResolverService.resolve()`，回傳 Resolver 原始候選人；沒有載入 Task Policy。設計器畫面把候選人以 `RESOLVED` 顯示為「簽核人」。Runtime `FmTaskAssignmentListener.resolveAssignment()` 在相同 Resolver 結果後另執行 `applySelfApprovalPolicy()`；`SKIP_TO_NEXT`／`REQUIRE_ALTERNATE` 可移除申請人，`INCIDENT` 則會拋錯；`applyDuplicatePolicy()` 也可依同一流程先前已核准者移除候選人，但預覽命令沒有流程實例與歷史 Action。因而預覽可顯示成功，實際任務卻沒有該簽核人或進入異常。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `resolverPreview()`；`backend/app/src/main/java/org/qifu/fm/flowable/FmTaskAssignmentListener.java` 的 `resolveAssignment()`／`applySelfApprovalPolicy()`／`applyDuplicatePolicy()`；`frontend-v-nx/pages/fm_prog004d0001/components/ProcessDesignerForm.vue` 的預覽表格。MariaDB 唯讀確認目前 Task Policy 已使用 SKIP_TO_NEXT 與 MERGE_CONSECUTIVE 等設定，但沒有同節點多條 ACTIVE Assignment Rule；此為靜態路徑對照，未實際啟動自簽或重複簽核流程。

### BUG-135：User Task 的 Flowable 派送與執行屬性未在發布時拒絕

- 修正狀態：**已修正（2026-10-07；2026-10-10 複核）**。`assertElementWhitelist()` 現在呼叫 `validateUserTask()`，只接受無命名空間的 `id`、`name` 屬性。下方「實際」為修正前情況。

- 嚴重度：高。性質：BPMN 發布驗證與派送契約；未部署手工構造的 BPMN 重現實際 Flowable 派送。
- 預期：User Task 的簽核人只能由已發布的 Task Policy 與 Assignment Rule 解析；設計版 BPMN 不應攜帶可直接指定 assignee／candidate users 的 Flowable 擴充屬性。`backend/doc/04-流程與表單規格.md` 第 139 行要求簽核人規則保存在 FlowMint 結構化表，不把實際人員寫死於 BPMN XML。
- 實際：`FmBpmnDesignValidator.assertElementWhitelist()` 對 BPMN 元件名稱設白名單，但只在 `serviceTask` 上檢查屬性；`userTask` 的 `flowable:assignee`、`flowable:candidateUsers`、`flowable:async` 等擴充屬性沒有被拒絕。`runtimeBpmnXml()` 轉換模型時只新增 FlowMint Task Listener 與 Multi-instance 設定，未清除原 User Task 派送或執行屬性。手工提交的設計版 XML 因而可能把設計器未提供的 Flowable 設定帶入部署；各屬性在模型轉換後是否保留、與 Listener 派送的優先序及實際執行影響，仍需隔離環境驗證。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidator.java` 的 `assertElementWhitelist()`／`validateDataActionTask()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `validateBpmn()`／`runtimeBpmnXml()`；規格第 139 行。此為靜態發布路徑推論，未修改或部署流程。

### BUG-136：Gateway 條件無空白時可繞過 AND／OR 混用限制

- 嚴重度：中。性質：BPMN 條件式發布驗證；未手工部署混用條件重現執行結果。
- 預期：若流程條件契約禁止同一個 Sequence Flow 表達式混用 `&&` 與 `||`，不應因運算子周圍的空白不同而改變發布結果，避免依預設優先序產生難以預期的分流。
- 實際：`FmBpmnDesignValidator.CONDITION` 的正規式允許運算子前後 `\\s*`，所以 `${flowmintFormData.a==1&&flowmintFormData.b==2||flowmintFormData.c==3}` 符合受控格式；但 `validateGateway()` 的混用判斷只檢查字串是否同時含有精確的 `" && "` 與 `" || "`。無空白或只一個運算子帶空白時，混用表達式不被攔截，可進入 Flowable 部署。現有測試只涵蓋任意表達式、缺 Default Flow 及 Parallel Gateway 條件，未覆蓋混用空白變體。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidator.java` 的 `CONDITION` 與 `validateGateway()`；`backend/app/src/test/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidatorTest.java`。此為字串規則靜態推論，未修改或部署流程。
### BUG-137：BPMN 說明或註解中的欄位字樣會被誤當成 Gateway 條件參照

- 嚴重度：低。性質：BPMN 發布驗證誤判；未提交含說明文字的草稿重現。
- 預期：Gateway 欄位存在檢查只應解析實際 `conditionExpression` 中使用的 `flowmintFormData` 欄位；說明、註解、節點名稱或其他非條件文字不應改變發布結果。
- 實際：`FmBpmnDesignValidator.referencedFormFields()` 對完整 BPMN XML 字串直接套用 `FIELD_REFERENCE` 正規式，收集所有 `flowmintFormData.<key>` 片段，不限定 Gateway 或條件元素。`FmProcessDefLogicServiceImpl.validateGatewayFormFieldsForPublish()` 把這個集合當成 Gateway 欄位，要求每張綁定表單都具有該 key。因此在允許的 `<documentation>` 中寫例如 `flowmintFormData.oldField`，即使實際條件完全未使用，仍可能因表單沒有 `oldField` 而拒絕發布。此項與 BUG-094 對多張表單一律檢查同一組真實條件欄位不同。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidator.java` 的 `FIELD_REFERENCE`／`referencedFormFields()`／`ALLOWED_BPMN_ELEMENTS`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `validateGatewayFormFieldsForPublish()`。此為靜態字串掃描路徑推論，未修改或發布流程。
### BUG-138：ALL／SEQUENTIAL 多人簽核解析失敗時不建立 Assignment Incident

- 嚴重度：高。狀態：**已確認非誤判並修正（2026-10-09）**。性質：Flowable Multi-instance 派送失敗處理。
- 預期：找不到簽核人或解析規則失效時，應保留可維運的未指派 Task 與 `OPEN` Assignment Incident；`README.md` 第 754～755 行明定此契約。
- 實際：發布 Runtime BPMN 時，`ALL`／`SEQUENTIAL` User Task 設定 `MultiInstanceLoopCharacteristics.inputDataItem` 為 `${fmTaskAssignmentListener.multiInstanceAccounts(execution, '<taskKey>')}`。`multiInstanceAccounts()` 在解析 `ServiceException` 後直接拋 `FlowableException`，沒有呼叫 `FmAssignmentIncidentRecorder`。一般 `notify(DelegateTask)` 才有捕捉解析失敗、記錄 Incident 與將 Task 標記為未指派異常的邏輯；多人簽核的帳號清單在建立各 Task 前就需取得，失敗時無法進入該 Task Listener 路徑。因此起單或前一關核准交易可能直接失敗或回滾，而營運頁沒有對應的 Assignment Incident 供 Retry／Reassign。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `runtimeBpmnXml()`；`backend/app/src/main/java/org/qifu/fm/flowable/FmTaskAssignmentListener.java` 的 `multiInstanceAccounts()`／`notify()`／`resolveAssignment()`；`README.md` 第 754～755 行。此為呼叫順序與例外路徑的靜態推論，未建立失敗資料或執行 Flowable 重現。
### BUG-139：循序多人簽核建立後重算簽核人，可能使後續 Task 無法指派

- 修正狀態：**已修正（2026-10-10；程式路徑確認）**。新進入 ALL／SEQUENTIAL 節點的預檢保存帳號及候選人快照；Task 建立時使用該快照指派與記錄稽核，不再重新執行 Resolver。沒有候選人快照的舊部署流程仍沿用原解析路徑，避免升級後直接進入 Incident。原程式確實二次解析，且 `applyAssignment()` 會拒絕不在新結果中的既定簽核人，因此不是誤判。`FmMultiInstanceSavedAssignmentTest` 證實循序 Task 使用預檢帳號且不再呼叫 Resolver；後端編譯及既有 Multi-instance Incident 定向測試也通過。尚未以簽核期間變更群組／任職的資料執行端到端測試。下方「實際」描述修正前行為。

- 嚴重度：中。性質：Multi-instance 簽核人清單與 Task Listener 解析不一致；未以實際組織資料變更重現。
- 預期：`SEQUENTIAL` 進入節點時產生的簽核人清單應決定各個後續 Task 的指派人，讓該節點按原順序完成；若業務要求重新解析，應明確處理原清單與新結果的差異。
- 實際：`multiInstanceAccounts()` 呼叫 `resolveAssignment()` 產生 Flowable Multi-instance 清單；每個 Task 建立時，`notify()` 又呼叫 `resolveAssignment()`，`applyAssignment()` 要求當前 `flowmintAssignee` 仍在這次新解析的帳號集合中。若循序簽核期間群組成員、任職或核決權限規則變更，後續 Task 的原定簽核人可能不在新集合，Listener 便建立 Assignment Incident 並留下未指派 Task，雖然原清單內仍有可用簽核人。此項是成功建立清單後的二次解析不一致，與 BUG-138 在建立清單前即解析失敗不同。
- 證據：`FmProcessDefLogicServiceImpl.runtimeBpmnXml()` 設定 `inputDataItem` 與 `elementVariable`；`FmTaskAssignmentListener.multiInstanceAccounts()`、`notify()`、`resolveAssignment()`、`applyAssignment()`；`README.md` 第 513～514 行的 ALL／SEQUENTIAL 契約。靜態推論，尚未執行組織異動與循序 Flowable 測試。
### BUG-145：BPMN 設計驗證未檢查 Start 到 Task／End 的連通性

- 嚴重度：中。性質：流程圖發布前結構驗證；尚未對不連通流程執行 Flowable 部署或起單。
- 預期：發布前應確認 Start Event、各必要 User Task 與 End Event 以 Sequence Flow 形成可執行路徑；`README.md` 第 62、211 行要求可完整連通且可到達 End Event 的流程圖。
- 實際：`FmBpmnDesignValidator.validate()` 僅檢查元件白名單、Process 數量／ID 和 Gateway 條件，不走訪從 Start Event 可達的節點，也不檢查每個 User Task 是否有進出線。現有 `FmBpmnDesignValidatorTest` 的 `acceptsControlledGatewayConditions()` 範例中，Start、Gateway、User Task、End 彼此沒有任何從 Start 出發的連線，測試仍要求驗證通過。因此流程發布前的自有驗證可能接受孤立簽核節點；後續 Flowable 部署是否拒絕或起單是否跳過節點，仍須隔離環境確認。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidator.java` 的 `validate()`／`validateGateways()`；`backend/app/src/test/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidatorTest.java` 的 `acceptsControlledGatewayConditions()`；`FmProcessDefLogicServiceImpl.publishInTransaction()` 的驗證順序。此為驗證器與既有測試的靜態比對，未部署流程。
### BUG-148：簽核群組成員優先序相同時，循序簽核順序不穩定

- 嚴重度：中。性質：Resolver 候選排序與 SEQUENTIAL 派送；未以同優先序群組執行流程。
- 預期：`SEQUENTIAL` 逐一簽核應有可重現的成員順序；`README.md` 第 499、514 行要求多位候選人依明確順序產生，並依群組成員順序逐一簽核。
- 實際：`FmApprovalGroupLogicServiceImpl.saveMember()` 未限制群組成員 `PRIORITY` 重複，未填時一律使用 100；`fm_approval_group_member` 也沒有群組與優先序的唯一鍵。`FmAssignmentResolverService.approvalGroup()` 查詢成員時只指定 `ORDER BY PRIORITY ASC`，同優先序成員的資料庫回傳順序不確定，再以該順序組成候選清單；`FmTaskAssignmentListener.multiInstanceAccounts()` 會把此清單交給 Flowable 的循序 Multi-instance。因此同一組成員可能在不同解析時得到不同簽核順序，`MAX_RESULTS` 截斷時也可能選到不同成員。
- 證據：`FmApprovalGroupLogicServiceImpl.saveMember()`；`FmAssignmentResolverService.approvalGroup()`；`FmTaskAssignmentListener.multiInstanceAccounts()`；`backend/doc/flowmint.sql` 的 `fm_approval_group_member` 唯一鍵與 `PRIORITY` 預設；`README.md` 第 499、514 行。此為排序與資料模型的靜態推論，未建立同優先序群組或實際簽核。
### BUG-149：同優先序的有效部門主管可能被不穩定地選為簽核人

- 嚴重度：中。性質：部門主管 Resolver 排序；未以同優先序主管資料實際重現。
- 預期：同一部門存在多位期間有效、優先序相同的主管時，Resolver 應有明確次排序鍵或拒絕模糊設定，讓相同輸入穩定選到同一位主管。
- 實際：`FmOrgUnitHeadLogicServiceImpl` 新增或編輯主管時允許相同 `PRIORITY`，未填時預設為 100；`fm_org_unit_head` 的唯一鍵也不含優先序。`FmAssignmentResolverService.firstOrgUnitHead()` 只指定 `ORDER BY PRIORITY ASC`，在過濾有效期間後直接 `findFirst()`。同優先序主管的資料庫回傳順序未定，`INITIATOR_ORG_HEAD`、`PARENT_ORG_HEAD`、`ROOT_ORG_HEAD` 等使用這個方法的解析結果可能改變。BUG-089 記錄第一筆主管員工失效時不嘗試候補，本項聚焦多筆有效主管的選擇不穩定。
- 證據：`FmOrgUnitHeadLogicServiceImpl` 的主管儲存路徑；`FmAssignmentResolverService.firstOrgUnitHead()` 與呼叫端；`backend/doc/flowmint.sql` 的 `fm_org_unit_head` 唯一鍵及 `PRIORITY` 預設。此為排序與資料模型的靜態推論，未建立同優先序主管或執行 Resolver。

### BUG-178：Start Event 的 Flowable 執行屬性未在發布前檢查

- 嚴重度：中。性質：BPMN 發布與起單交易邊界；未以手工 XML 部署重現。
- 預期：設計版只允許一般 Start Event；影響 Flowable 執行時機的擴充屬性應在發布前拒絕或由發布器明確重設，避免改變起單交易的流程進入時機。
- 實際：`FmBpmnDesignValidator.assertElementWhitelist()` 對 BPMN 元件只檢查名稱，屬性檢查僅涵蓋 `serviceTask`。因此 `startEvent` 上的 `flowable:async="true"` 不會被自有驗證器攔截。`FmProcessDefLogicServiceImpl.runtimeBpmnXml()` 將設計 XML 轉成 Flowable model 後，只改寫 User Task 與 Service Task，沒有清除 Start Event 的非同步設定；手工提交的 XML 可能使起單後的流程進入非同步工作，而非一般同步起單路徑。此項針對 Start Event，與 BUG-135 的 User Task 派送／執行屬性不同。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/workflow/FmBpmnDesignValidator.java` 的 `assertElementWhitelist()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmProcessDefLogicServiceImpl.java` 的 `runtimeBpmnXml()`；`README.md` 第 62、73 行。Flowable converter 是否保留該屬性及起單後的實際時序仍須隔離部署驗證；目前只確認自有檢查與轉換程式未主動拒絕／清除。

### BUG-180：ORG_DUTY 候選人達上限時缺少固定排序

- 嚴重度：中。性質：Resolver 候選截斷與派送穩定性；未用多人擔任同一職務的資料重現。
- 預期：指定部門職務有超過 `MAX_RESULTS` 位有效擔任者時，應依明確且穩定的排序選取候選人，使相同輸入得到相同簽核人。
- 實際：`FmAssignmentResolverService.orgDuty()` 呼叫 `employeeDutyService.selectListByParams(memberParameters)` 時沒有傳入排序條件，再依回傳順序加入候選人並在候選數達 `MAX_RESULTS` 時停止。`FmEmployeeDutyMapper.xml` 只有呼叫者指定 `orderBy`／`sortType` 才產生 `ORDER BY`。因此多位有效擔任者超過上限時，被選中的帳號集合可能依資料庫未保證的回傳順序改變。BUG-148 是簽核群組同優先序的循序排序，本項是職務擔任者完全沒有排序而被截斷。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/resolver/FmAssignmentResolverService.java` 的 `orgDuty()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmEmployeeDutyMapper.xml` 的 `selectListByParams`；`README.md` 第 506、588 行的 `ORG_DUTY` 與 `MAX_RESULTS` 契約。此為 SQL 與 Java 控制流程推論，未查詢現有職務人數或執行 Resolver Preview。

### BUG-181：核決條件欄位缺值時，否定與數值比較處理不一致

- 嚴重度：中。性質：核決權限條件判斷與簽核路由；未以缺值表單執行 Preview 或正式派送。
- 預期：規則參照的欄位不存在或尚未填寫時，應有一致且明確的缺值語意；至少不應把缺值當成一般值命中否定條件，或讓單條數值條件的型別錯誤中斷整組核決規則與 Fallback。
- 實際：`FmApprovalAuthorityConditionEvaluator.valueByPath()` 對缺失路徑回傳 `null`。`NE` 使用 `!equalsValue(null, expected)`，預期值非 null 時得到 `true`；`NOT_IN` 同樣會因陣列不含 null 得到 `true`。相反地，`GT`／`GTE`／`LT`／`LTE` 呼叫 `compareNumber()` 將 null 轉為 `"null"` 建立 `BigDecimal`，丟 `ServiceException`。`FmAssignmentResolverService.approvalAuthority()` 未逐條捕捉該例外，最外層 `resolve()` 也只在取得非 `RESOLVED` 結果時才進入 Fallback。因此可選欄位未填時，核決路由可能誤命中或整體解析失敗。現有單元測試只覆蓋有值的條件。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/authority/FmApprovalAuthorityConditionEvaluator.java` 的 `valueByPath()`、`evaluate()`、`equalsValue()`、`compareNumber()`、`contains()`；`backend/app/src/main/java/org/qifu/fm/domain/resolver/FmAssignmentResolverService.java` 的 `approvalAuthority()`、`resolve()`；`FmApprovalAuthorityConditionEvaluatorTest.java`。規格列出受控比較運算子，但未定義缺值應為 false 或獨立狀態；本項聚焦目前同一缺值會產生互相矛盾的判斷與例外，具體期望語意需產品確認。

### BUG-182：核決權限編輯器將巢狀表單欄位存成最外層條件路徑

- 嚴重度：中。性質：Vue 核決權限條件與 Runtime 表單資料路徑；未用巢狀表單執行 Preview。
- 預期：Container、Columns、Table 等巢狀欄位應保存可從表單資料取值的完整路徑；無法用單一路徑表示的 Grid 列資料應明確限制或另外定義集合語意。
- 實際：`ApprovalAuthorityPanel.vue.collectFields()` 會遞迴走子元件，卻一律把選項值設為 `form.${component.key}`，沒有保留父層 key。若欄位實際位於 `form.details.amount`，選擇後儲存的是 `form.amount`。`FmApprovalAuthorityConditionEvaluator.valueByPath()` 依點分隔路徑逐層讀取，結果為 null；後續可能出現 BUG-181 所述的否定誤命中或數值比較例外。後端 `validateConfig()` 只檢查欄位字串非空，核決權限儲存與流程發布均未比對該條件路徑是否存在於綁定表單。此項與 BUG-176 的 Data Action Binding 路徑不同，影響的是核決簽核路由。
- 證據：`frontend-v-nx/pages/fm_prog004d0001/components/ApprovalAuthorityPanel.vue` 的 `collectFields()`／`fields`；`frontend-v-nx/components/fm/ApprovalAuthorityConditionEditor.vue` 的欄位選項；`backend/app/src/main/java/org/qifu/fm/domain/authority/FmApprovalAuthorityConditionEvaluator.java` 的 `validateConfig()`／`valueByPath()`；`FmApprovalAuthorityLogicServiceImpl.validateRule()`。規格要求巢狀欄位目錄與點分隔條件路徑；目前只做靜態比對。

### BUG-183：核決條件切換到 IN／NOT_IN 時未轉換既有比較值

- 嚴重度：低。性質：Vue 核決條件編輯與儲存；未在瀏覽器實際操作重現。
- 預期：使用者從 `EQ` 等單值運算子切換為 `IN`／`NOT_IN` 時，編輯器應將既有比較值轉成清單或清楚要求重新輸入，讓畫面顯示與即將送出的 JSON 型別一致。
- 實際：`ApprovalAuthorityConditionEditor.vue.changeConditionOperator()` 只改 `operator`，保留原本字串、數字或布林值；`displayValue()` 仍把它顯示在同一文字框，因此切換後不編輯比較值即可按儲存。`FmApprovalAuthorityConditionEvaluator.validateConfig()` 對 `IN`／`NOT_IN` 強制 `value` 為陣列，後端拒絕該規則。只有再次觸發文字框 `input`，`parseValue()` 才會把內容轉成陣列。從清單運算子切回單值運算子時也會保留陣列，但後端沒有對非清單運算子的型別作對應檢查；實際比較語意未驗證。
- 證據：`frontend-v-nx/components/fm/ApprovalAuthorityConditionEditor.vue` 的 `changeConditionOperator()`、`parseValue()`、`displayValue()`；`frontend-v-nx/pages/fm_prog004d0001/components/ApprovalAuthorityPanel.vue` 的 `save()`；`backend/app/src/main/java/org/qifu/fm/domain/authority/FmApprovalAuthorityConditionEvaluator.java` 的 `validateConfig()`。此為靜態 UI／API 路徑推論。

### BUG-184：IN／NOT_IN 數字清單由前端存成字串，與後端數值比對型別不符

- 嚴重度：中。性質：核決條件編輯與數值欄位路由；未以數值表單執行 Preview。
- 預期：對數值欄位設定 `IN`／`NOT_IN` 時，編輯器輸入的數字清單應以數值保存，或後端明確把清單項目轉成數值再比較，使畫面上的 `100, 200` 與表單數值 100／200 具有一致語意。
- 實際：`ApprovalAuthorityConditionEditor.vue.parseValue()` 對清單運算子只執行字串 `split()`、`trim()`，產生例如 `["100","200"]`；`FmApprovalAuthorityConditionEvaluator.contains()` 逐項呼叫 `equalsValue()`，當表單實際值是 `Number` 時，該分支直接呼叫文字節點的 `decimalValue()`。本機 Jackson 3.1.2／3.1.4 原始碼顯示 `StringNode` 沿用 `BaseJsonNode.decimalValue()`，該方法對非數值節點拋型別轉換例外；`equalsValue()` 捕捉後回傳 `false`。因此數值欄位用編輯器輸入的數字清單做 `IN` 會一直不命中，`NOT_IN` 則會一直命中。`validateConfig()` 只要求清單為陣列，沒有檢查項目型別。
- 證據：`frontend-v-nx/components/fm/ApprovalAuthorityConditionEditor.vue` 的 `parseValue()`；`backend/app/src/main/java/org/qifu/fm/domain/authority/FmApprovalAuthorityConditionEvaluator.java` 的 `contains()`、`equalsValue()`、`validateConfig()`；本機 Maven 快取的 Jackson 3.1.2／3.1.4 `StringNode.java` 與 `BaseJsonNode.java` 原始碼。這與 BUG-183 切換運算子時未轉換既有值不同：此處即使重新輸入數字清單，前端仍輸出文字陣列。已確認函式分支，尚未執行端到端 Preview。

### BUG-185：核決數值比較允許文字比較值儲存，Runtime 才拋錯

- 嚴重度：中。性質：核決條件型別驗證與 Runtime 路由；未以文字比較值執行正式流程。
- 預期：`GT`／`GTE`／`LT`／`LTE` 的預期值應在編輯或儲存時驗證為數字，避免已發布核決規則在執行時因無法比較而中止派送。
- 實際：`ApprovalAuthorityConditionEditor.vue.parseValue()` 遇到 `abc` 等非數值輸入會保留字串；`FmApprovalAuthorityConditionEvaluator.validateConfig()` 對數值比較運算子只檢查名稱與 `value` 存在，不檢查它是數值節點，因此核決權限可儲存。Runtime `compareNumber()` 先解析表單實際值，再對預期值呼叫 `decimalValue()`；本機 Jackson 3.1.2／3.1.4 的 `StringNode` 繼承 `BaseJsonNode.decimalValue()`，對文字節點拋型別轉換例外；服務將其轉成「核決權限數值條件無法比較」的 `ServiceException`。`FmAssignmentResolverService.approvalAuthority()` 沒有逐條捕捉，後續規則與 Fallback 不會接手。BUG-181 是實際欄位缺失造成比較失敗，本項是在欄位有數值時，規則本身的預期值型別錯誤仍可通過儲存。
- 證據：`frontend-v-nx/components/fm/ApprovalAuthorityConditionEditor.vue` 的 `parseValue()`；`backend/app/src/main/java/org/qifu/fm/domain/authority/FmApprovalAuthorityConditionEvaluator.java` 的 `validateConfig()`／`compareNumber()`；`FmApprovalAuthorityLogicServiceImpl.validateRule()`；`FmAssignmentResolverService.approvalAuthority()`；本機 Maven 快取的 Jackson `BaseJsonNode.java`。已確認型別與例外路徑，未執行端到端 Preview 或正式派送。

### 2026-10-07 修復複核（進行中）

- **BUG-013 — 已修正（程式確認）：**原本 Groovy 草稿關閉時略過 `advanceDraftLock()`，且 API 回傳的 lock version 為空；與前端仍送版本值的契約不符。現草稿儲存一律以預期版本做 compare-and-set，API 一律回傳實際版本號。
- **BUG-091 — 已修正（程式確認）：**原一般發布路徑的 `draft()` 不鎖版本資料列，可能同版重複部署。現發布交易在驗證與 Flowable 部署前一律以 `lockDraft()` 鎖定草稿；第二個交易取得鎖時若已發布即被拒絕。Groovy 發布仍沿用既有編譯證據與交易流程。
- **BUG-014 — 已修正（程式確認）：**原發布檢查只確認群組主檔 ACTIVE，確實允許沒有可用成員的群組。現在一般群組 Resolver 與核決權限內部群組目標均要求至少一位目前有效、員工與 Tenant membership 也有效的成員。
- **BUG-085 — 已修正（程式確認）：**原固定帳號發布檢查及候選選項只看員工狀態。現在兩者都核對員工生效期間；固定帳號 Fallback 與核決目標共用同一驗證路徑。
- **BUG-086 — 已修正（程式確認）：**原 `/category/save` 可直接將仍被未停用流程引用的分類改為 INACTIVE；現在編輯停用與專用停用入口共用相同的流程引用檢查。
- **BUG-087 — 已修正（程式確認）：**原核決權限發布檢查只看主檔 ACTIVE，且零個 ACTIVE 規則時空迴圈仍回傳成功。現在要求權限主檔目前在生效期間內，且至少有一條 ACTIVE、目標有效的規則。
- **BUG-088 — 已修正（程式確認）：**原 `UP_TO_LEVEL` 在到達目標層級時不確認該主管是否真的加入候選。現在只有有效主管被加入後才回傳 `RESOLVED`；失效者會繼續沿主管鏈尋找可用的更高層。
- **BUG-089 — 已修正（程式確認）：**原主管解析取第一筆期間有效的正式主管後才檢查員工與 membership，第一筆失效會漏掉候補。現在依正式、代理、代理副主管及優先序逐一尋找可用員工，供單位主管及逐級主管解析共用。
- **BUG-149 — 已修正（程式確認）：**同優先序主管原本沒有固定次排序鍵；現在以 `ORG_UNIT_HEAD_ID` 作為穩定次排序鍵。
- **BUG-090 — 已修正（程式與定向測試確認）：**原條件格式檢查只涵蓋多出口 Gateway；Start Event 與單出口 Gateway 的任意條件可通過。現在所有 Sequence Flow 的非空條件都須符合受控比較式，多出口 Gateway 與 Parallel Gateway 仍保留原有額外限制。`FmBpmnDesignValidatorTest` 已加入兩條繞過路徑的拒絕案例並通過。
- **BUG-092 — 已修正（程式確認）：**起單授權原本僅從 ACTIVE 成員與期間收集群組 ID，沒有查群組主檔。現在只保留仍為 ACTIVE 的群組 ID，停用群組的成員不再命中起單政策。
- **BUG-093 — 已修正（畫面回報，程式確認）：**原畫面在主檔儲存成功後立即提示成功，草稿失敗時形成互相矛盾的訊息。現在完成兩步後才顯示完整成功；草稿回應失敗或網路例外時明確指出主檔已保存、草稿未保存或未確認。兩個 API 仍各自交易，部分保存情境須依畫面提示重新載入檢查。前端 `npm run build` 通過。
- **BUG-094 — 待契約確認，未修改：**確實存在「將所有 Gateway 欄位對每張 Task 表單檢查」的程式路徑；但 Runtime 的 `FORM_DATA` 由起單選定的表單資料提供，現行規格未明確界定多張 Task 表單對同一 Gateway 欄位的來源與可達性。尚不能只因另一張 Task 表單缺欄位就斷定發布檢查是誤判或應放寬；需先定義來源契約並用多表單流程驗證。
- 後端 `mvn -q -pl app -am -DskipTests test-compile`，以及 `FmProcessGroovyDraftLogicTest`、`FmGroovyPublicationLogicTest`、`FmProcessMultiInstanceBpmnTest`、`FmFormalHeadSelectionTest` 通過；尚未以雙連線部署競態或失效主管資料重現。其餘第 12 單元條目未在本輪標示修復。

### 2026-10-07 修復複核（續）

- **BUG-095 已修正（經靜態呼叫鏈核實）**：已發布／已退役版本的 ACTIVE 指派規則如在主要或備援設定引用核決權限，更新及停用該權限會被拒絕，須建立新權限 ID 並發布新流程版本。Runtime 確實按權限 ID 讀取即時主檔與規則，因此原問題並非誤判。`FmApprovalAuthorityPublishedReferenceTest` 已驗證主要引用、備援引用與僅草稿引用三種情況。此修正未建立發布快照；更新與發布同時發生的交易競態仍需資料庫層序列化驗證。
- **BUG-096 已修正（經靜態主管鏈核實）**：`NEXT_HIGHER_LEVEL_HEAD` 和 `TARGET_LEVEL_HEAD` 遇到符合層級但員工或 Tenant membership 失效時，現在繼續尋找上層有效主管。原邏輯確實在第一個符合層級的任職直接回傳 `NOT_FOUND`，並非誤判。`EXACT` 仍依原定義只接受指定層級。

### 2026-10-07 修復複核（表單選人）

- **BUG-097 誤判，未修改程式**：原項目要求限定「人員選擇器」元件型態，但 `backend/doc/30-採購單與驗收單表單流程配置規劃.md` 的 `acceptanceOwnerAccount`、`backend/doc/31-公司名片申請單表單流程配置規劃.md` 的 `applicantAccount` 等既有配置使用帳號欄位；規格也描述從指定欄位讀取帳號，沒有要求元件必須是人員選擇器。Runtime 會再次驗證帳號對應有效員工與 Tenant membership。依元件型態擋一般文字欄位會誤擋合法流程；任意輸入造成 Runtime `NOT_FOUND` 屬表單輸入管制議題，不能據此判定發布驗證缺陷。
- **BUG-098 已修正（經呼叫鏈核實）**：Fallback 的 `FORM_ACCOUNT_FIELD`／`FORM_ACCOUNT_FIELD_MANAGER` 現在沿用主要 Resolver 的 Task 表單、已發布版本及 `fieldKey` 存在性檢查。原本 Fallback 僅檢查 `fieldKey` 非空，對不存在欄位落入 `validateResolverReference()` 的預設放行，確實會讓無法解析的配置發布。已通過 `mvn -q -pl app -am -DskipTests test-compile`。
- **BUG-099 已修正（經 UI 與 Runtime 契約核實）**：設計器對兩種表單選人共用「單一帳號或帳號陣列」提示，`FORM_ACCOUNT_FIELD_MANAGER` 原先確實將整個陣列轉成字串查帳號。現在逐筆解析帳號、查直屬主管、以員工 ID 去重並遵守 `MAX_RESULTS`；單一帳號及 `{value,label}` 仍沿原路徑處理。已通過 `mvn -q -pl app -am -DskipTests test-compile`；尚未以真實多選表單執行端到端流程。

### 2026-10-07 修復複核（起單版本）

- **BUG-100 已修正（經版本及 Runtime 呼叫鏈核實）**：建立下一版草稿會把流程主檔改為 DRAFT，但舊版本仍為 PUBLISHED 且有 Flowable 部署；原本申請中心及正式起單都以主檔 PUBLISHED 篩選，確實造成中斷。現在 Runtime 接受主檔 PUBLISHED 或 DRAFT，仍要求存在具部署 ID 的 PUBLISHED 流程版本；INACTIVE 主檔及從未發布的草稿仍不會開放起單。`mvn -q -pl app -am -DskipTests test-compile` 已通過。若綁定表單另有草稿造成載入失敗，屬第 13 單元 BUG-108，未包含在本項修正。

### 2026-10-07 修復複核（起單政策）

- **BUG-101 已修正（經發布與 Runtime 路徑核實）**：發布檢查原本只確認至少一筆 `allowStart=Y`，確實不重驗規則對象。現在逐筆重驗允許規則：`ACCOUNT` 需有效員工及 Tenant membership；`APPROVAL_GROUP` 需啟用群組、有效成員及可用帳號；`ORG_UNIT` 需存在且至少一筆有效任職，其員工與 Tenant membership 也須有效；`ALL` 可直接通過。失效對象會阻擋發布。`FmProcessStartPolicyPublishTest` 驗證失效帳號被拒與 `ALL` 通過，相關 Groovy 發布、草稿及多人簽核測試通過。這項檢查只驗證允許規則的對象可用性，不能保證所有政策組合都有實際可起單者，例如另有 `ALL/N` 否決規則時仍以 Runtime 政策評估為準。

### 2026-10-07 修復複核（群組派送方式）

- **BUG-102 已修正（經發布與 Runtime 呼叫鏈核實）**：既有 `FmApprovalGroupModeValidator` 只看主要 `APPROVAL_GROUP`，但 Runtime 的 Fallback 與 `APPROVAL_AUTHORITY` 內部目標確實都會呼叫群組 Resolver，因此原項並非誤判。發布時現在蒐集同 Tenant 的 ACTIVE 核決權限群組目標，並同時檢查主要、Fallback 及核決權限內部群組的 `ASSIGNMENT_MODE` 是否與 Task Policy 一致；停用群組亦會被拒。`FmApprovalGroupModeValidatorTest` 已涵蓋備援不一致、核決權限內部不一致與一致情況，Maven 測試通過。

### 2026-10-07 修復複核（主管來源）

- **BUG-103 已修正（經任職儲存、組織健康與 Resolver 呼叫鏈核實）**：`ORG_HEAD`／`PARENT_HEAD` 任職的顯式主管 ID 本來就為空；原 Resolver 僅讀該 ID，因此確實無法解析。現在依 `MANAGER_SOURCE` 統一取得下一位主管任職：`EXPLICIT` 使用指定 ID，`ORG_HEAD` 使用本部門有效正式 `HEAD`，`PARENT_HEAD` 使用上級部門有效正式 `HEAD`，`NONE` 無主管；動態主管也須有有效員工、Tenant membership 與相應部門任職。`DIRECT_MANAGER`、`MANAGER_CHAIN`、`NEXT_HIGHER_LEVEL_HEAD`、`TARGET_LEVEL_HEAD`／`UP_TO_LEVEL` 及由 `directManager()` 進入的 `FORM_ACCOUNT_FIELD_MANAGER` 都已接入。`FmManagerSourceResolverTest` 驗證本部門、上級部門與 NONE，Maven 測試及 `git diff --check` 通過；尚未跑真實組織資料的端到端指派。

### 2026-10-07 修復複核（版本切換）

- **BUG-104 已修正（經 Vue 狀態流核實）**：版本按鈕原本直接 `importXML()`，未儲存 BPMN 圖會遺失；Task Forms／Policies／Assignment Rules 等則直接改動版本物件，切回後仍殘留，因此不是誤判。現在開啟草稿時記錄圖與設定基準，切換前比較 BPMN XML、表單與節點設定、起單政策及 Groovy 綁定；有變更時提示先儲存，使用者確認放棄才會將該草稿的 XML 與設定一起還原並切換。`npm run build` 通過；尚未在瀏覽器手動驗證確認視窗操作。
- **BUG-105 已修正（經停用及版本狀態轉換核實）**：原 `deactivate()` 會把主檔設為 `INACTIVE`，但 `createVersion()` 和 `publishInTransaction()` 無狀態檢查且會覆寫主檔狀態，確實可重新上架。現在兩個後端入口都拒絕停用流程；畫面的建立新版與發布按鈕也不再顯示。`FmProcessGroovyDraftLogicTest` 新增停用流程不得建版的測試，Maven 測試及 `npm run build` 通過。既有草稿仍可檢視，發布須先有明確的重新啟用機制；目前沒有該入口。

### 2026-10-07 修復複核（分類狀態）

- **BUG-106 已修正（經申請中心、直接起單與發布路徑核實）**：申請中心只列 ACTIVE 分類，但 `loadStart()`／新送單經 `activeProcessDef()` 時原本未查分類，發布也未重查；此不一致確實存在。現在正式起單主檔查詢要求分類仍為 ACTIVE，發布入口也呼叫 `requireActiveCategory()`；停用分類下的新起單與新版發布會被拒絕。相關 Maven 測試通過。既有冪等送單結果仍可按原邏輯回傳；未以停用分類的實際 HTTP 請求重現。
- **BUG-107 已修正（經發布與 Runtime 生效期間判斷核實）**：`TARGET_LEVEL_HEAD`、`ORG_TITLE`、`ORG_DUTY` 原發布檢查只查 ACTIVE，Runtime 會再檢查有效期間，確實可能發布後立即解析失敗。現在主要及 Fallback Resolver、核決權限內部 `APPROVAL_LEVEL`／`ORG_TITLE`／`ORG_DUTY` 目標皆使用與 Runtime 一致的期間檢查。`FmProcessPublishEffectiveReferenceTest` 驗證三種主檔過期時拒絕、有效時通過；Maven 測試通過。

### 2026-10-07 修復複核（BUG-133～135）

- **BUG-133 已確認，待修正**：對照 `backend/doc/04-流程與表單規格.md` 的發布驗證報告要求與 `FmProcessDefLogicServiceImpl.publishInTransaction()` 的逐項例外路徑，確認目前只能回報第一個驗證錯誤；本輪未改動發布 API。
- **BUG-134 已修正（介面標示）**：核對 `resolverPreview()` 只呼叫 Resolver、執行期另套用自簽與重複簽核政策；預覽沒有流程實例與歷史簽核資料，不能呈現最終派送。設計器將預覽標示為「原始候選人」，明示兩項政策尚未套用及正式派送差異。`npm run build` 通過。
- **BUG-135 已修正**：確認 `FmBpmnDesignValidator.assertElementWhitelist()` 原先未檢查 User Task 屬性，且部署轉換會保留模型的派送與執行設定。新增 User Task 屬性白名單，只接受 `id`、`name`，拒絕 `flowable:assignee`、`flowable:candidateUsers`、`flowable:async` 等自帶設定；新增驗證測試。

### 2026-10-07 修復複核（BUG-136）

- **BUG-136 已修正，先重現再修改**：先新增無空白與部分空白的 `&&`／`||` 混用測試，原驗證器未拋出 `ServiceException`，Maven 測試確實失敗，排除誤判。修正兩處 Sequence Flow 驗證，忽略引號內的文字後檢查是否混用運算子；另確認字串值包含 `||` 而實際只有 `&&` 時仍可通過。`FmBpmnDesignValidatorTest` 全數通過；未執行 Flowable 實際部署。

### 2026-10-07 修復複核（BUG-137）

- **BUG-137 已修正，先重現再修改**：先以含 `<documentation>flowmintFormData.oldField</documentation>`、節點名稱 `flowmintFormData.nodeName` 及真實條件欄位 `amount` 的 BPMN 測試 `referencedFormFields()`；原程式回傳 `oldField`、`nodeName`、`amount`，測試失敗，確認不是誤判。改以安全設定的 XML reader 僅讀取 BPMN `conditionExpression` 文字，再擷取表單欄位；完整 `FmBpmnDesignValidatorTest` 通過。未執行實際發布或資料庫異動。

### 2026-10-07 修復複核（BUG-138、139、148）

- **BUG-138 已確認呼叫路徑，待實際重現與設計修正**：`multiInstanceAccounts()` 在建立多人 Task 前解析候選人，失敗直接拋 `FlowableException`；`notify()` 的 Incident 捕捉無法處理該路徑。尚未以 Flowable 啟動失敗流程驗證交易及持久化結果，本輪未修改。
- **BUG-139 已確認二次解析路徑，待情境驗證**：Multi-instance 清單建立時與各 Task 建立時都會呼叫 `resolveAssignment()`；若資料在兩次呼叫間變動，`applyAssignment()` 可能拒絕原清單帳號。尚未重現組織異動與循序簽核，本輪未修改。
- **BUG-148 已修正**：核對群組成員優先序可重複，Resolver 原本僅按 `PRIORITY` 查詢；同分順序無保證。候選人處理前增加 `PRIORITY`、`EMPLOYEE_ID`、`APPROVAL_GROUP_MEMBER_ID` 的固定排序，避免循序簽核順序及 `MAX_RESULTS` 截斷依賴資料庫同分回傳順序。`mvn -q -pl app -am -DskipTests compile` 通過；未執行實際多人流程。

### 2026-10-07 修復複核（BUG-149、178、180）

- **BUG-149 已修正（既有修正，未重複修改）**：重新檢查 `firstOrgUnitHead()`，目前查詢用 `PRIORITY,ORG_UNIT_HEAD_ID`，程式再依主管類型、優先序、`ORG_UNIT_HEAD_ID` 排序並尋找有效員工；清單既有修正紀錄仍成立，原缺陷描述為舊狀態。
- **BUG-180 已修正（既有修正，未重複修改）**：重新檢查 `orgDuty()`，目前 `FmEmployeeDuty` 查詢已有 `IS_PRIMARY DESC,EFFECTIVE_FROM,EMPLOYEE_DUTY_ID` 排序，末項為固定唯一識別碼；原「完全沒有排序」的描述已過期。本輪未修改。
- **BUG-178 已修正，先重現再修改**：以單一 Start Event 加 `flowable:async="true"` 新增測試，原驗證器沒有拋錯，確認缺口存在。新增 Start Event 屬性檢查，僅接受 `id`、`name`；`FmBpmnDesignValidatorTest` 全數通過。尚未執行 Flowable 實際部署。

### 2026-10-07 修復複核（BUG-145）

- **BUG-145 已修正，先重現再修改**：先新增 Start 直接連 End、User Task 孤立的 BPMN 測試，原驗證器未拋錯，確認不是誤判。新增流程連通性檢查：要求一個 Start Event、至少一個 End Event，且每個流程節點都可由 Start 到達並有路徑抵達 End；另覆蓋從 Start 可達但無法到 End 的死端 Task。修正原本缺少 Sequence Flow 的正向測試樣本；`FmBpmnDesignValidatorTest` 全數通過。未執行 Flowable 實際部署。

### 2026-10-07 修復複核（BUG-181、183、184）

- **BUG-181 待釐清，未修改**：缺值時 `NE`／`NOT_IN` 命中而數值比較拋錯的程式路徑存在，但規格未定義缺值在否定條件下的產品語意；本輪不擅自改變核決規則的匹配結果。
- **BUG-183 已修正（程式路徑確認）**：`changeConditionOperator()` 原本只改運算子，從單值切到 `IN`／`NOT_IN` 時會保留非陣列值，與後端 `validateConfig()` 的陣列要求衝突。現在切換時同步轉換比較值；多項清單切回單值運算子時清空值，避免靜默取第一項。前端 `npm run build` 通過，未做瀏覽器互動測試。
- **BUG-184 已修正，先重現再修改**：新增數值表單值 100 對編輯器字串清單 `["100","200"]` 的測試，原後端回傳不匹配，確認不是誤判。編輯器現在將數字清單項目存成數值；後端亦能把既有字串數字清單與數值表單欄位比較。`FmApprovalAuthorityConditionEvaluatorTest` 與前端 `npm run build` 通過，未執行實際 Resolver Preview。

### 2026-10-07 修復複核（BUG-185）

- **BUG-185 已修正，先重現再修改**：新增 `GT` 規則以文字 `abc` 作為預期值的測試；原 `validate()` 未拋例外，確認規則會通過儲存前驗證。`validateConfig()` 現在要求 `GT`／`GTE`／`LT`／`LTE` 的 `value` 為 JSON 數字節點，無效值在核決規則儲存時即遭拒絕。`FmApprovalAuthorityConditionEvaluatorTest` 全數通過；未執行實際流程派送。

### 2026-10-07 修復複核（BUG-182）

- **BUG-182 已修正（新選取欄位）**：核對專案安裝的 Form.io `ContainerComponent`，子元件使用 `this.dataValue` 作為資料上下文，提交資料因此位於 Container key 之下；原 `ApprovalAuthorityPanel.collectFields()` 卻只產生 `form.<childKey>`。現在遞迴累積 Container key，選項會存成 `form.details.amount`；Columns、Panel、Table 等版面元件不增加資料路徑，DataGrid／EditGrid 子欄位因需要列索引而不列入單值條件選項。後端新增完整點分隔路徑的核決比對測試，`FmApprovalAuthorityConditionEvaluatorTest` 與前端 `npm run build` 通過。既有已儲存的錯誤欄位路徑不會自動改寫，需重新選取並儲存；未執行瀏覽器或真實表單 Preview。

### 2026-10-07 待決項目複核（BUG-133、138、139、181）

- **BUG-133 規格待決**：多項發布驗證目前確實於首個錯誤中止；但完整報告要求列出 Resolver Preview 錯誤，而 Preview 需測試申請人與表單資料，發布命令目前只有版本 OID。需先定義發布時使用哪組輸入、是否必須每條條件分支有測試樣本，以及 Groovy 編譯失敗如何併入報告；未以缺少輸入的偽報告冒充完整修正。
- **BUG-138 規格待決**：`multiInstanceAccounts()` 解析失敗時尚未建立 Flowable Task。資料表雖允許 `TASK_ID` 為 NULL，現有 Retry／Reassign 均要求 Task ID 與 `FLOWMINT_INCIDENT` Task category，且例外回滾可能使同交易 Incident 消失。需要先定義可恢復的流程停駐節點與營運操作契約；本輪不寫入無法 Retry 的 OPEN Incident。

### 2026-10-09 BUG-138 定向重現

- **BUG-138 確認成立，尚未修正**：新增 `FmMultiInstanceAssignmentFailureTest`，以 ALL 政策且沒有啟用的 Assignment Rule 呼叫 `multiInstanceAccounts()`，驗證它在 Task 建立前拋 `FlowableException`，且未呼叫 Incident Recorder；定向測試通過。現有 Incident Retry／Reassign 要求 Task ID，且 ALL／SEQUENTIAL Retry 只接受原 `flowmintAssignee` 位於重新解析的候選清單。不能用單一占位帳號建立 Task，否則可能把多人簽核錯誤地縮成單人簽核；需以保留原簽核人集合與順序的停駐／重試設計修正，並以 Flowable 整合測試驗證。
- 2026-10-09 設計查核：專案使用 Flowable 8.0.0；其 `RuntimeService.addMultiInstanceExecution()` 有增補實例 API，但本機依賴原始碼顯示順序模式只增加 `nrOfInstances` 與建立子 Execution，沒有同步更新 Collection。直接用此 API 補一個占位 Task 不足以恢復 SEQUENTIAL 的原候選人順序，故未套用未經驗證的 Runtime 變更。
- **2026-10-09 修正與驗證**：在發布轉換時於 ALL／SEQUENTIAL 前加入指派預檢、排他閘道與 Incident 等待 Task；預檢先解析並儲存完整候選集合，失敗則建立帶實際 Task ID 的 OPEN Incident。管理員修復規則後以 Retry 完成等待 Task，重新預檢再進入原多人簽核節點；Reassign 拒絕將此事件改成單人。Flowable H2 引擎測試確認 ALL 恢復後建立兩筆並行 Task，SEQUENTIAL 恢復後依序建立兩筆 Task；另有 Listener、Retry 定向測試。測試同時抓出舊集合表達式中的 `execution` 無法解析及 execution-local 變數不會傳給 Multi-instance root，已改由流程變數傳遞預檢集合。此修正只對重新發布的 BPMN 生效，舊部署不會自動遷移。
- **BUG-139 歷史待決紀錄，已由 2026-10-10 修正取代**：當時已確認 Multi-instance 起始清單與 Task Listener 的二次解析可能不一致；現採節點預檢名單凍結並保留舊部署流程相容路徑，詳見條目修正狀態。
- **BUG-181 規格待決**：缺值的否定條件與數值條件目前行為不一致，但規格未定義欄位缺失與明確 `null` 各自的比較語意；本輪不擅自改變已發布核決條件的匹配結果。
