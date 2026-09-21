# 35 System Task 的 Groovy 腳本規劃

日期：2026-09-08

接續更新：2026-09-21（主 JVM、Incident 與核心 E2E 現況見第 44～49 節）

狀態：Groovy 已改由 FlowMint backend 主 JVM 直接執行，獨立 worker 已移除；`HTTP_API` subtype 已取消，外部 API 由 Groovy 腳本處理。CHECK、Preview、發布、Runtime、Incident Retry／Recalculate 及 Flowable／MariaDB 核心 E2E 已完成；完整多帳號瀏覽器與正式部署權限回歸仍待執行。第 15～40 節中的舊架構內容僅為歷史紀錄。

System Task 是自動執行工作的流程節點，表單回寫為選用能力。

適用程式：`FM_PROG004D0001` BPMN 流程設計與版本，以及流程營運監控。

## 1. 目的與現況

讓流程設計者在 System Task 撰寫 Groovy，處理明細計算、欄位整理、條件判斷及產生 Gateway 分流欄位，無須為每個流程新增 Java。腳本在後端流程執行期間自動執行，不依賴使用者開啟瀏覽器。

`FmDataActionTaskDelegate` 呼叫固定版本的 Data Action。`FmBpmnDesignValidator` 拒絕原生 Script Task、自訂 class、expression、script 與 scriptFormat。Groovy 由固定平台 Delegate 在應用 JVM 執行，並補齊固定版本、內容 hash、Mapping、執行紀錄、Incident 與稽核；不直接把任意原生 Script Task 接入 Flowable。

本文件第 1～14 節定義產品契約；第 15～40 節保留歷史開發紀錄。若其中出現獨立 worker、子程序、runner classpath 或額外部署要求，一律由第 42 節的新決策取代。

## 2. 產品範圍

FlowMint 工作節點仍分為 User Task 與 System Task 兩大類；執行 subtype 不新增工作大類。System Task 的正式類型如下：

| 類型 | 使用方式 | 主要用途 |
| --- | --- | --- |
| `DATA_ACTION` | 選擇同 Tenant 已發布 Action Code 與固定版本 | 查詢／異動資料庫與既有整合；完整真實流程與專屬 Incident 維運仍待驗收 |
| `GROOVY` | 在節點編輯 Groovy 與輸入／輸出契約 | 資料計算、轉換、驗證、路由結果與受控 HTTP；核心 E2E 已完成 |

第一版採「節點內腳本、隨 Process Version 版本化」，不另建全域 Script 主檔、Script Code 或管理選單。需要重用時先複製節點或流程版本；集中腳本庫屬後續需求，不是第一版前置。

Groovy 允許 Map／List、字串、布林、BigDecimal、條件與迴圈、集合轉換、`groovy-json` 及 Java 21 `HttpClient`。不得直接存取 SQL、檔案、系統環境、Spring Bean、Flowable Service 或其他 Tenant；資料庫整合使用 Data Action，HTTP 呼叫須自行處理 timeout、非 2xx、JSON、秘密、冪等與結果不確定性：

```text
Data Action Task：取得來源資料
  → Groovy Task：計算與整理
  → Gateway：依輸出欄位分流
  → Data Action Task／User Task
```

Groovy 沒有 `ctx.executeDataAction`；該名稱是既有前端 JavaScript 的介面。Groovy 可使用 Java 21 `HttpClient` 呼叫外部 API，腳本開發者必須明確設定 timeout、HTTP method、Header、認證、錯誤處理與 JSON 轉換，並在測試環境完成驗證後才發布。

原生 `bpmn:scriptTask` 仍不開放；Groovy 使用 `bpmn:serviceTask` 加固定平台 Delegate，不接受設計者自訂 Java class／JUEL。

## 3. 使用者操作

在 BPMN Palette 增加「Groovy 腳本工作」，並在 System Task 屬性面板提供類型選擇。編輯頁沿用 QIFU4 Toolbar、驗證、toast、loading 與權限規範。

右側屬性面板只顯示節點 ID、中文名稱、用途、Groovy 類型、腳本行數、驗證狀態與「編輯腳本」入口；不在窄面板放置主要腳本編輯區。完整設定放在大型 Edit Dialog，採同一 Dialog 的「腳本編輯」與「教學說明」兩個 Tab，不另疊第二層教學 Dialog。

### 3.1 大型 Edit Dialog 與 CodeMirror

Dialog 預設約視窗寬、高的 90%，提供全螢幕切換；窄螢幕使用可用全螢幕空間。腳本編輯 Tab 包含：

1. 節點 ID、中文名稱與用途說明。
2. CodeMirror Groovy 編輯器：沿用既有 Form JavaScript 編輯器的版本、封裝與視覺慣例，實作前確認相容的 Groovy 語言支援，不以單純 TextArea 作為正式編輯介面。
3. 輸入 Schema 與 Mapping：只提供選定欄位，不把完整表單送進腳本。
4. 輸出 Schema 與 Mapping：明確列出可以回寫的表單欄位。
5. 逾時毫秒數：受平台上限約束；記憶體與併發限額由管理員設定。
6. 「檢查腳本」與「試跑」：試跑只接受人工輸入的測試 JSON，顯示輸出、驗證結果、耗時與受限日誌。

CodeMirror 須提供 Groovy 關鍵字、字串、註解與數字上色、行號、自動縮排、括號配對、程式碼折疊、搜尋／取代及復原／重做。輸入 `input.` 或 `context.` 時提示已宣告的輸入名稱與系統欄位，這是契約型提示，不宣稱具備完整 Groovy／Java IDE 型別推導。

編輯器占主要空間；輸入／輸出 Mapping 與可用變數區可收合，檢查結果置於下方。後端回傳的行／欄診斷可點擊跳至程式位置；語法上色不代表編譯或政策驗證成功。腳本或契約變更後，先前驗證結果標示失效。主 JVM Groovy engine 或 Runtime Readiness 不可用時，CHECK／Preview／發布須明確拒絕，不以瀏覽器上色冒充驗證通過。

可用變數區依綁定的 Form Schema 顯示欄位中文名稱、路徑與型別，支援巢狀物件及整個 Grid 明細陣列。選取表單欄位時指定輸入別名、加入 Mapping，再於游標處插入對應 `input` 引用；別名重複時提示修正，不靜默覆蓋。系統變數列出用途與 nullable 說明，可插入 `context` 引用。此目錄只顯示結構與說明，不自動載入正式表單 value；試跑值由人工 sample JSON 提供。

### 3.2 編輯狀態與套用

開啟 Dialog 時建立腳本、Schema、Mapping 與節點執行設定的編輯副本。底部提供「取消」與「套用至節點」；套用只更新流程頁的草稿狀態並標示未儲存，仍須按流程頁「儲存」才送至後端。整組 binding 套用須支援流程設計器 undo／redo，不只復原腳本文字。

切換 Tab、收合輔助區與切換全螢幕須保留編輯內容、游標、選取範圍、捲動位置及編輯器歷史；重新顯示編輯器時正確重算尺寸。按取消、關閉或 Esc 時，有未套用變更則確認是否放棄；點背景不直接關閉。Dialog 管理鍵盤焦點，關閉後焦點回到入口；Tab 可用鍵盤切換。Published／Retired 以唯讀方式開啟，允許查看、搜尋及複製，不提供套用或修改型操作。

### 3.3 教學說明 Tab

**同批交付要求：實作 Groovy Editor 時，必須同步更新既有 BPMN「配置說明」按鈕開啟的教學 Dialog，不可只新增 Groovy Edit Dialog 內的教學 Tab。** 更新入口為 `frontend-v-nx/pages/fm_prog004d0001/components/bpmnDesignerGuide.ts`，相關主題定位由既有 `BpmnDesignerHelp.vue` 與呼叫端配合；操作文件同步第 34 章。

既有 BPMN 教學已涵蓋新增 Groovy System Task、開啟大型 Editor Dialog、Editor／教學 Tab、選取表單欄位與系統變數、輸入／輸出 Mapping、CHECK／Preview、套用至節點與儲存流程的差異，以及已發布版本唯讀與執行限制。選取 Groovy 節點後按「配置說明」會直接定位 System Task／Groovy 操作主題；Groovy Dialog 內的教學提供詳細語法與程式範例。

編輯 Tab 提供「查看教學」，切到同一 Dialog 的教學 Tab 及相關段落；返回編輯時保留第 3.2 節狀態。教學支援關鍵字搜尋與主題導覽，內容包含：

1. 快速入門：設定輸入、撰寫腳本、設定輸出、檢查／試跑、套用至節點與儲存流程。
2. 表單 value：文字、數字、布林、null、巢狀欄位與 Grid 明細的 Mapping 和讀取範例。
3. 系統變數：第 4 節白名單、來源、型別、nullable 與 PREVIEW／RUNTIME 差異。
4. 輸出寫回：明確 `return`、輸出 Schema、FORM_DATA Mapping，以及後續 Gateway 如何使用結果。
5. 常用範例：金額加總、空值處理、條件判斷、明細轉換；範例附必要輸入／輸出契約和人工測試 JSON。
6. 限制與排錯：只讀已保存資料、SQL 使用 Data Action、HTTP 使用 Java 21 `HttpClient`，以及編譯／型別／權限／逾時／非 2xx／結果不確定的處理方式。

範例提供「複製程式碼」，不直接覆蓋目前腳本，也不因開啟教學執行範例或呼叫異動 API。範例須符合實際 engine profile、語法政策與 JSON 輸出限制；含 Groovy 字串插值時，回傳前轉為普通 String。教學與變數提示共用受控契約來源，未實作能力須標示規劃中，不能呈現為已可使用。

Draft 可編輯；Published／Retired 只能檢視。型別轉換需先提示將清除不相容的設定，並支援 undo／redo。User Task 轉換必須處理 Task Policy、Assignment、Form Rule 的孤兒資料；第一版若不能保證原子清理，禁止該轉換。

Preview 的 Script 與 Schema 可包含未儲存草稿，但必須攜帶 expected lock version；後端重驗 Tenant、編輯權與內容限制。試跑不讀正式業務資料、不建立流程、不寫表單、不觸發通知。CHECK、Preview、發布編譯與 Runtime 使用相同的主 JVM engine profile 與契約。

## 4. 腳本輸入與回傳契約

Groovy Binding 只注入 `input`、`context` 與受控 `log`，不得傳入 Entity、Java Service、HTTP Request 或 `DelegateExecution`。

| 介面 | 契約 |
| --- | --- |
| `input` | JSON Map 的獨立深複本，符合輸入 Schema；腳本修改此物件不會自動寫回資料庫 |
| `context` | 後端建立的唯讀 JSON 值；完整白名單與來源見第 4.2 節，不接受呼叫端覆蓋可信任身分 |
| `log.info(message)` | 受長度與筆數限制的文字日誌；不用 stdout 當協定回應 |
| `return` | 唯一正式輸出，必須是符合輸出 Schema 的 JSON object |

`mode` 為 `PREVIEW` 或 `RUNTIME`。Preview 的流程識別可為 null，Schema 須宣告 nullable；`tenantId` 仍取伺服器已驗證的 Tenant。`startedAt` 為本次 invocation 固定 UTC ISO 時間，不允許直接取得系統時鐘或亂數；相同輸入重試應得到相同業務結果。

JSON 只允許 object、array、string、boolean、有限 number 與 null；拒絕 Closure、Class、GString、日期物件、循環參照及任意 POJO。輸入小數轉為 BigDecimal，輸出保留十進位精度；超出 Form 欄位可接受範圍則拒絕回寫。輸入／輸出 Schema 第一版只支援受控型別、properties、required、items、enum、長度與數值界限，不支援遠端 `$ref`；預設禁止未宣告欄位。

未捕捉例外、回傳 null 或不符合 Schema，均為失敗。範例與編輯器指引要求明確 return；Runtime 依 Groovy 最終回傳值驗證，接受符合 Schema 的 object（包括隱式回傳），不能宣稱僅憑回傳值便能判定腳本是否寫了 return。

### 4.1 請購計算範例

輸入 Schema 宣告 `items` 為 object 陣列，每列有 quantity、unitPrice；taxRate 為 0～100 的數值。輸出 Schema 宣告 subtotal、taxAmount、totalAmount 為數值，requiresReview 為布林。

```groovy
def subtotal = 0G

for (def item : input.items) {
    def lineAmount = (item.quantity * item.unitPrice).setScale(
        2, java.math.RoundingMode.HALF_UP
    )
    subtotal += lineAmount
}

def taxAmount = (subtotal * input.taxRate / 100G).setScale(
    2, java.math.RoundingMode.HALF_UP
)
def totalAmount = subtotal + taxAmount

return [
    subtotal: subtotal,
    taxAmount: taxAmount,
    totalAmount: totalAmount,
    requiresReview: totalAmount > 300000G
]
```

此為新節點示例，不替換現行請購金額核決規則或表單 JavaScript；正式配置仍依該流程需求決定。

### 4.2 表單 value 與系統變數

Runtime 的 `FORM_DATA` 來源為 System Task 本次 invocation 建立輸入快照時，後端已保存的 Form Data revision，不含瀏覽器尚未送出的欄位修改。同 invocation 重試沿用固定快照。只將明確 Mapping 的欄位注入 `input`，保留 JSON 型別；數值依前述 BigDecimal 契約處理。

| 腳本引用範例 | Mapping 來源 | 值的型別／用途 |
| --- | --- | --- |
| `input.amount` | `FORM_DATA.totalAmount` | 數值，例如 12500 |
| `input.currency` | `FORM_DATA.currency` | 字串，例如 TWD |
| `input.items` | `FORM_DATA.items` | Grid 明細 object 陣列 |
| `input.departmentCode` | `FORM_DATA.department.code` | 巢狀欄位字串 |

上述為示例，來源欄位須存在於第 5 節指定的 Form Schema。缺少 required 輸入時於執行前拒絕；選配缺值正規化為 null 並要求 Schema 允許 null，腳本可自行給預設值。第一版不新增隱式 Mapping 預設值語法，也不把 null、false 或 0 自動轉為空字串。

現行 `context` 白名單如下；正式持久化來源與 DTO 必須維持一致，未完成來源接線的欄位不可在編輯器標示可用：

| 欄位 | 型別 | Runtime 來源與語意 |
| --- | --- | --- |
| `tenantId` | string | 後端驗證的 Tenant |
| `processInstanceId` | string | 目前流程實例 |
| `processVersionNo` | number | 固定發布流程版本 |
| `nodeId` | string | 本次 System Task 節點 |
| `invocationId` | string | 本次節點執行識別 |
| `startedAt` | string | invocation 固定 UTC ISO 時間 |
| `mode` | string | PREVIEW 或 RUNTIME |
| `businessKey` | string | 流程保存的業務識別碼 |
| `documentNo` | string 或 null | 流程的正式單據編號，未編號可為 null |
| `applicantAccount` | string | 流程保存的申請人帳號 |
| `initiatorAccount` | string | 流程保存的實際發起人帳號 |
| `applicantOrgUnitId` | string 或 null | 流程保存的申請人部門，不於執行時改查最新任職替換 |

以上身分與單號由後端流程資料建立，不從同名表單欄位信任取值，亦不得透過輸出 Mapping 修改。System Task 在背景執行，不提供含糊的「目前登入者」或「目前簽核人」；前一位操作者不屬於第一版白名單。

Preview 保留伺服器產生的 tenantId、invocationId、startedAt、mode，以及經驗證的設計節點／版本資訊；無正式實例的 businessKey、documentNo、申請人／發起人及部門等值使用 null 或明確標示為人工 sample 的值，禁止為了補值讀取正式業務資料。Schema／提示須反映模式差異；sample context 不得覆蓋 Tenant、mode 或其他伺服器執行識別。PROCESS_CONTEXT Mapping 與直接 `context` 存取使用同一白名單及值來源。

```groovy
def amount = input.amount ?: 0G
def items = input.items ?: []

return [
    requiresReview: amount > 10000G,
    itemCount: items.size(),
    summary: (context.documentNo ?: '未編號').toString()
]
```

此範例需宣告 amount、items 的 nullable 輸入，以及 requiresReview、itemCount、summary 的輸出 Schema；結果須另設輸出 Mapping 才會寫回表單。

## 5. Mapping 與表單寫回

採用明確型別的 Groovy Mapping V1；不改變既有 Data Action 的字串 Mapping 格式。

```json
{
  "mappingVersion": 1,
  "input": {
    "items": { "source": "FORM_DATA", "path": "items" },
    "taxRate": { "source": "CONSTANT", "value": 5 }
  },
  "output": {
    "subtotal": { "target": "FORM_DATA", "path": "subtotal" },
    "taxAmount": { "target": "FORM_DATA", "path": "taxAmount" },
    "totalAmount": { "target": "FORM_DATA", "path": "totalAmount" },
    "requiresReview": { "target": "FORM_DATA", "path": "requiresReview" }
  }
}
```

輸入來源只允許 `FORM_DATA`、`PROCESS_CONTEXT`、`CONSTANT`；Process Context 使用第 4 節清單，CONSTANT 保留 JSON 型別。一般 path 使用 dot path，禁止任意 expression、萬用字元、陣列索引及 prototype／class 等特殊路徑。陣列以整個已宣告欄位輸入或回傳，第一版不提供逐列 patch。

輸出只允許 `FORM_DATA` 或明確 `DISCARD`。System Task 沒有 User Task Form Rule，發布時須以該 Process Version 的起單 Form 綁定取得唯一 Schema；綁定不明確或多 Form 不相容時拒絕發布，不猜測相鄰節點。

發布前驗證來源／目標存在、型別、持久化資格、重複與父子 path 衝突。禁止修改 Tenant、申請人／發起人身分、單號、Business Key、流程狀態、版本、附件 ID 等系統或授權欄位；新增明確的 System Task 可寫欄位分類，不以普通 User Task 的 Edit 權限替代。一般業務唯讀計算欄位可明確配置為 System Task 可寫。

Runtime 先驗證完整 result 與整份合併後表單，全部成功才寫入。Form Data 依 Tenant、Form Data ID、expected lock version 做 compare-and-set，成功增加 lock version 與 revision，建立不可變 Snapshot，同步 `flowmintFormData`，後續 Gateway 才可讀取新值。輸出為全 DISCARD 時不增加表單 revision，但仍記錄成功執行。

同一表單的平行分支可能衝突：第一版拒絕覆蓋，建立 `FORM_CONCURRENT_CHANGE` Incident，不自動以舊輸入重算後覆寫。管理員可明確選擇以最新 revision 重新執行，形成新的 invocation 並保留原紀錄。

## 6. 版本與儲存方式

Groovy 腳本／Schema／Mapping 存在 Process Version 子表，作為單一真實來源；BPMN 只保存 `taskType` 與 `bindingId`，不重複嵌入腳本文字。

```xml
<bpmn:serviceTask
    xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:flowmint="https://flowmint.qifu.org/schema/bpmn"
    id="calculateRequest"
    name="計算請購金額"
    flowmint:taskType="GROOVY"
    flowmint:bindingId="calculate-request" />
```

發布轉換器只為 GROOVY 產生固定 `${fmGroovyTaskDelegate}`，使用 Flowable async job，並以原生 fieldExtension 保存 bindingId 與 binding SHA。後端按可信任的 Tenant／Process Version／Node ID 讀取子表，重新核對 SHA，不能從表單值指定 binding、腳本或 engine。

Data Action 既有 `flowmint:actionCode`／版本／Mapping 屬性保持原契約；本期不強制遷移既有流程至子表。Validator 按 taskType 分派各自白名單，拒絕混用屬性；原生 scriptTask 仍拒絕。

腳本保存為 UTF-8、LF、無 BOM。binding SHA 涵蓋腳本、Schema、Mapping、timeout、engine profile 與 policy version；JSON 以固定鍵排序正規化，陣列保留順序。既有 BPMN_SHA256 不變更語意，新增整包發布 manifest hash 涵蓋設計 XML 與依 bindingId 排序的 binding hashes。

Published 子表不可修改；建立新流程版本時複製 binding，保留節點內 bindingId、重新產生 OID。刪除節點同步刪除 Draft binding。匯入／匯出使用包含 XML 與 bindings 的版本化 JSON bundle；單獨 XML 缺少 Groovy binding 時拒絕匯入成可發布流程。禁止從 URL 或外部檔案自動下載腳本。

發布固定 engine profile（Groovy、JDK、依賴與政策版本）。先核對專案 Maven 實際解析版本，不任意升級 Groovy。舊 profile 不可默默替換；安全撤銷時暫停相關執行並建立 Incident，再依明確升級程序處理。

## 7. 資料模型

下列模型已納入 MariaDB 與 `flowmint.sql`；其他部署環境仍須依正式 migration／完整 schema 升級。欄位型別、命名、FK、索引及長度以現行 schema 為準。

| 表 | 必要資料與約束 |
| --- | --- |
| `fm_process_system_task` | OID、Tenant、Process Def ID／Version、Node ID、bindingId、taskType、SCRIPT_CONTENT、INPUT_SCHEMA、OUTPUT_SCHEMA、MAPPING_CONTENT、TIMEOUT_MS、ENGINE_PROFILE、POLICY_VERSION、CONTENT_SHA256、LOCK_VERSION、Audit 欄位；Tenant＋Process Version＋Node ID 唯一，bindingId 在同版本唯一；關聯流程版本 |
| `fm_system_task_execution` | invocationId、Tenant、Process Instance、Process Version、Node ID、executionId、首次 job 識別、binding SHA、input revision／hash、固定 startedAt、狀態、lease generation／expiry、result hash、Snapshot ID、Incident ID；invocationId 唯一，保存已套用結果的 receipt |
| `fm_system_task_attempt` | execution OID、attemptNo、requestId、profile、起迄時間、duration、錯誤代碼與受控摘要、log 摘要、狀態；execution＋attemptNo 唯一；Preview 紀錄以 mode 區分，可無流程實例 |

Execution 的輸入需可供同 invocation 重試：保存最小必要的輸入快照於受存取控制的內容欄位，設定保留期，不在一般查詢 API 或 log 回傳。Preview 輸入／輸出預設不持久化，僅回給本次有權操作者。

共用執行表應可容納 `DATA_ACTION`，但既有 Data Action attempt／Incident 補齊另有工作量；不可因建表就宣稱第 33 章全部結案。Groovy 需要的 Snapshot／Incident／receipt 是首版必要交付，不留成「可執行但無法追蹤」的 MVP。

## 8. 主 JVM Groovy 執行器

CHECK、Preview、發布編譯與 Runtime 均使用 app 內的 `FmGroovyInProcessEngine`，正式部署只需
FlowMint 主程式 JAR，不啟動獨立 worker、子程序或額外 classpath。Groovy 定位為可信任 IT
管理腳本，不是執行不受信任程式碼的 OS sandbox；反射、程序、執行緒、檔案、JDBC、Spring Bean、
Flowable Service 與其他 Tenant 資料仍不屬於公開契約。HTTP 僅透過 Java 21 `HttpClient`，
JSON 使用 `groovy-json`，認證秘密不得寫入腳本或輸出。

timeout 會停止等待並拒絕過期結果，但主 JVM 無法保證強制終止惡意或不合作的執行緒，因此只有
具專用權限、經測試與審閱的 IT 腳本可發布。具體主 JVM 決策與驗收見第 42～49 節。
腳本上限 64 KiB；input／context／result 各 256 KiB，JSON 深度 20、值節點 10,000；
日誌最多 100 筆／16 KiB。Preview／Runtime 各自限制每 Tenant 2、全域 8 個併發，滿額拒絕。
回應核對 invocationId、attemptId、generation、bindingSha256 及 Schema；Runtime 提交另驗證
lease／generation，過期或逾時結果不得寫回。

## 9. Runtime、交易與重試

### 9.1 呼叫識別與執行順序

每次流程「到達節點」是一個 invocation。同一 job 重送重用 invocation；迴圈再次到達同節點必須建立新的 invocation，不能只以 Process Instance＋Node ID 去重。

1. 後端從可信任的 Flowable execution 解析 Tenant、流程版本、node 與 binding，檢查流程仍可執行及 profile 啟用。
2. 以首次 async job ID 建立穩定 invocation，execution 表保存 job／execution 關聯。job 搬至 dead-letter 或人工重試時必須顯式沿用原 invocation；不能假定搬移後 job ID 永遠不變。迴圈及 multi-instance 的 occurrence 隔離須有 Flowable 實測證據。
3. 短交易保存 invocation、输入 revision／最小快照、lease generation 與 attempt。失敗紀錄使用獨立交易，避免流程交易 rollback 後整筆紀錄消失。
4. 將腳本與已固定輸入送入主 JVM engine。等待受總 deadline 約束；工作排隊前不得持有表單 row lock。Flowable job 執行交易的最長占用時間與 lock timeout 需按壓測設定。
5. 回應後先驗證完整 result，再於 Flowable 所用的同一 MariaDB 交易中重新核對 active execution、lease generation、取消狀態與表單版本鎖，更新 Form／變數／Snapshot／成功 receipt，並推進流程。
6. 上述交易失敗時不得留下已成功 receipt 或部分表單寫回；記錄 attempt 失敗。成功 receipt 與流程推進在同一交易提交，避免重送時重複增加 revision／snapshot。

若目前 Flowable 與 FlowMint transaction manager 無法證明同庫原子性，先解決整合問題，不能以「兩個 transaction 都成功」當作原子提交。

### 9.2 重試與流程終止

主 JVM Runtime 暫時忙碌或啟動不可用時，平台 scheduler 依固定退避政策處理；禁止 Flowable 與 Runtime 各自乘倍重試。每次 attempt 更新 generation，過期回應失效。

語法／安全政策、輸入／輸出 Schema、腳本例外、timeout、memory limit、表單衝突及 profile 撤銷不自動重試。未知故障用盡預算後進 Incident，不跳過節點或視為核准。

申請撤回、取消、管理員終止或 execution 被刪除時，取消等待中工作、撤銷 lease；即使執行緒未立即停止，也禁止過期回應提交。Reaper 將 lease 過期但無終態的 attempt 轉為 `ABANDONED`，依重試政策接續，不永久卡在 RUNNING。

人工「重試」保留原輸入與固定腳本版本；「依最新表單重算」是新的 invocation，須另行預覽當前 revision 並記錄原因。Published 腳本有 bug 時建立新 Process Version；第一版不支援替換執行中流程的腳本。

## 10. Incident、稽核與權限

錯誤代碼包含 `GROOVY_COMPILE_ERROR`、`GROOVY_POLICY_DENIED`、`GROOVY_INPUT_INVALID`、`GROOVY_OUTPUT_INVALID`、`GROOVY_RUNTIME_ERROR`、`GROOVY_TIMEOUT`、`GROOVY_MEMORY_LIMIT`、`GROOVY_RUNTIME_BUSY`、`GROOVY_RUNTIME_START_UNAVAILABLE` 與 `ENGINE_PROFILE_DISABLED` 等受控分類；不得再把已移除的外部 worker 當成現行錯誤來源。

System Task 異常管理 UI 是必要功能，不是選配治理擴充。背景自動工作失敗時沒有使用者待辦可處理，維運人員必須能透過既有 `/operations/incidents` 入口查詢失敗、查看安全摘要與處理歷程，並在後端確認狀態可安全操作時執行受控補執行。

營運畫面顯示流程／節點中文名稱、腳本版本 hash、input revision、各 attempt、開始／結束、耗時、錯誤類型及可操作原因。補執行包含沿用原輸入與固定發布版本的人工重試，以及使用較新表單 revision 建立新 invocation 的受控重算；兩者都必須保存操作者、原因、requestId 與新舊工作關聯。第一版不提供跳過節點、手改成功狀態、直接修改 result／歷史輸入／歷史腳本、直接修改 Flowable 資料表或任意外部補償。

系統執行者記為 System Task 技術身分，另外保存原發起人；不可偽裝成某位簽核者建立 APPROVE。Snapshot／執行動作使用新增 `SYSTEM_TASK_APPLY` 語意，需同步狀態代碼與歷程顯示。一般使用者只看節點狀態與安全錯誤摘要；腳本、輸入快照與完整診斷僅對具明確權限的設計／維運人員提供。

設定與發布需要既有流程 Q／A／E／發布授權，另加 capability `FLOWMINT_GROOVY_DESIGN`、`FLOWMINT_GROOVY_PUBLISH`；營運操作要求 `FLOWMINT_GROOVY_OPERATE`，並同時要求既有營運角色與 Tenant 權限。UI 整合至既有異常處理 Program，不另建 System Task 選單；沒有自動授予一般使用者或所有 Tenant 管理員。

腳本 log、exception message 可能含輸入資料，不能假定只記文字就安全；正式稽核預設只存錯誤代碼、定位、hash 與受控摘要，debug 內容限制存取與保留期，禁止傳入 credential。Preview、檢查、發布、重試、重算與終止均記操作者及 Tenant；缺少權限時後端拒絕，不只隱藏按鈕。

## 11. API 與程式異動點

現行 API 使用 POST 與 QIFU4 結果封裝／checkFields；路徑須與 Controller 保持一致：

| 功能 | 現行介面／行為 |
| --- | --- |
| Draft load／save／clone | 擴充現有流程版本 DTO，含 `groovyBindings` 與 expected version；XML＋子表原子保存 |
| 語法檢查 | `POST /api/FM_PROG004D0001/groovy/validate`；回行／欄錯誤與 policy diagnostics |
| 試跑 | `POST /api/FM_PROG004D0001/groovy/preview`；人工 sample input，執行契約與配額一致 |
| 發布 | 擴充既有 Publish Validator；編譯結果綁定內容 SHA，內容一變就失效；不可只信任 client pass |
| 匯出／匯入 | 擴充流程 bundle 契約；校驗版本、內容、大小與 binding 引用 |
| 維運 | `POST /api/fm/operations/system-tasks/incidents`、`detail`、`retry/preview`、`retry`、`recalculate/preview`、`recalculate`，以及 `/handling/detail`、`/handling/handle`；每次重驗能力、狀態、Tenant 與 idempotency key |

主要異動包括：

- `FmBpmnDesignValidator`：按 subtype 驗證，允許受控 Groovy bindingId，仍拒絕未登錄執行屬性。
- 流程 Logic／Publisher／Multi-instance Runtime 轉換：固定 Delegate、版本子表、manifest、bundle 與 clone。
- `FmGroovyTaskDelegate`、binding validator、主 JVM engine、execution／attempt repository 與營運 Logic；共用 Mapping／Form apply 須回歸 Data Action 舊行為。
- bpmn-js moddle／Palette／屬性面板、腳本編輯器、營運明細與配置說明。

主 JVM engine、必要 profile 或 Runtime Readiness 未就緒時，Designer 明確顯示不可 CHECK／Preview／發布；仍可保存合法 Draft，以便部署補齊後繼續。

## 12. 驗收與交付範圍

Groovy 核心交付已依第 44～49 節完成主 JVM、HTTP／JSON、成功流程、async 完成同步、Incident Retry 與 Recalculate 實機驗收。尚待完整多帳號瀏覽器、正式權限與部署環境回歸。

## 13. 文件交付邊界

文件描述不代表功能已可發布；以實際部署、流程執行與操作驗收結果判定。

## 14. 接續開發入口

後續由第 44～49 節的完成證據及本章頂端現況接續，不再從第 15～40 節的歷史待辦自動產生新功能。
以下保留歷史實作紀錄供追查；其中獨立 worker、正式發布未開放及尚未核心 E2E 等描述均已被後續章節取代。

## 15. 2026-09-08 Phase A 首批程式

使用者授權開始開發後，新增獨立 `backend/groovy-worker` Maven 專案；詳細命令、JSON 協定、限制與待辦見 [worker README](../groovy-worker/README.md)。不修改原 Maven reactor，不依賴 FlowMint app／core／base，也未使用既有主 JVM ScriptExpressionUtils 執行設計者腳本。

已完成第一批程式及本機測試：

- Maven dependency tree 確認 app 使用 Groovy 5.0.6；worker 固定相同版本及 Java 21 編譯目標。
- 一次一份 JSON 的 CHECK／RUN 協定、語法政策、型別複製、唯讀 context、Map 回傳、大小／深度限制及有限日誌。
- 輸入／輸出 Schema 子集檢查，未知欄位／關鍵字、required、型別、nullable、陣列與數值界限驗證。
- `mvn -o package` 建置與 19 項單元測試通過。測試只用 repository 內固定、已審閱樣本，不能作為 OS sandbox 證據。

下一步先完成 worker README 的 Phase A 待辦與實機驗證，再接第 14.1 節版本子表、API、CodeMirror Editor 與兩處教學。尚未建立 migration／資料表、修改 Designer／Runtime／發布 Validator 或執行 MariaDB。Groovy 仍不可在流程中使用。

## 16. 2026-09-08 接續：Groovy 草稿儲存與 Editor 預備程式

使用者要求繼續後，先完成不執行腳本的草稿儲存與 Editor 接線；本批不開放 worker 檢查／試跑／Runtime。新增 `flowmint.groovy.draft-enabled` 設定，預設 false；未套用 migration 前必須保持關閉。正式 Groovy 發布仍由原驗證入口拒絕，不能只切換草稿開關就發布。

已實作程式：

- `FmProcessSystemTask` Entity／Mapper／QIFU4 Service，腳本、Schema、Mapping、timeout 與版本綁定分開保存，XML 只保存 bindingId。
- 流程草稿 DTO、load／save／clone 接線；啟用草稿時以父版本 row lock 與 expectedLockVersion 保護 XML 及子表更新，失敗由 Logic 交易回滾。停用時不查新表或新欄位。
- `FmGroovyDraftValidator` 驗證節點／binding 一致、腳本大小、JSON 結構、Mapping 來源、context 名称與保留欄位／父子 path 衝突；此為草稿結構驗證，不代表編譯／Schema 完整驗證或可發布。
- BPMN Groovy Palette、右側摘要與大型 CodeMirror Dialog；Editor／教學 Tab、Groovy 上色、行號、縮排、括號、縮排式折疊、搜尋／取代、變數提示與插入、唯讀模式、全螢幕、取消確認、整份 binding undo／redo。
- 變數選取只使用唯一綁定表單的 Schema，不查正式 value。Grid／物件子欄位須由設計者補齊 Schema；無法確定型別的 Select／Hidden 等欄位要求手動確認，不能一律當字串。
- 既有 `bpmnDesignerGuide.ts` 同批新增 Groovy Editor 主題及節點定位，與 Editor 內教學共用說明與範例來源。試跑與編譯按鈕停用並標明尚未開放。
- 草稿保存失敗時保留畫面中的未儲存內容；不先用主檔更新回應覆蓋待儲存的腳本。

當時已準備 Groovy draft 一次性 SQL，不放入 doc，也未修改 `flowmint.sql`。唯讀確認當時 `fm_process_version` 為 4 筆，尚無 `LOCK_VERSION` 或 `fm_process_system_task`。該 SQL 後續已套用並於 2026-09-19 隨整個 `migrations` 目錄刪除；最終結構以 `flowmint.sql` 為準。

驗證：後端針對性測試 22 項、前端 Node 測試 4 項通過；MyBatis XML 可解析並驗證 Tenant／DRAFT／lock 條件。前端 production build 與 Prettier 依本批異動檢查。這些不等於真實 MariaDB 交易或登入瀏覽器驗收。

待完成：migration 套用與實機草稿儲存／clone／併發驗收、專用 capability、完整 bundle 匯入／匯出、型別與表單可寫能力發布驗證、worker/API 檢查與試跑、行／欄診斷、固定 manifest、Runtime／Snapshot／execution／attempt／Incident 與 E2E。目前不支援單獨 XML 還原 Groovy 腳本；缺少 binding 時保存會拒絕，不以空腳本取代。

## 17. 2026-09-09 Worker 協定、診斷與清理補強

新增 `WorkerProtocol`，獨立 worker／supervisor 同步使用協定 V2，要求 attemptId、正整數
generation 與 bindingSha256；連同 protocolVersion、profile、invocationId 逐欄核對回應。
Transport 啟動 worker 前先驗證 request，收到結果先限制位元組數，再驗證 JSON、operation／status、
輸出 Schema、日誌及安全診斷。拒絕重複 key、尾隨 JSON、數字強制轉型與未知回應欄位。

bindingSha256 由未來可信 runner 提供；本批只核對本次 request 的識別，不代表已完成完整版本
manifest、資料庫 lease／generation 或提交時的狀態驗證。尚無正式 API 使用舊協定，V1 不再接受。

CHECK 只檢查 Schema 結構與編譯，不必提供 required sample；RUN 保持完整 sample 驗證。
語法錯誤提供最多 20 筆 code／line／column，行欄從 1 起算；不回傳原始碼、exception message
或 stack trace。編譯器包裝的 SecurityException 分類為 GROOVY_POLICY_DENIED；政策 AST
完整定位及前端診斷跳轉仍待完成。

Transport 工作逾時／失敗必須終止 worker；清理無法確認時不接受成功結果，並保留原始失敗供對帳。
程序啟動競態、transport crash、分段 timeout 與 reaper 尚待實機驗證。

驗證：`mvn -o package` 40 項測試通過，涵蓋 CHECK／RUN、識別不符、JSON 邊界、診斷不洩漏原文、
正常清理、啟動失敗、工作逾時及原始錯誤保留。
測試只使用已審閱固定腳本及模擬 Process。
資料庫唯讀查核仍為 4 筆版本，migration 未套用；草稿預設關閉，檢查／試跑 API 與 Runtime 仍未開放。

## 18. 2026-09-09 草稿 migration 已套用

使用者明確同意後，以第 21 章指定 MariaDB CLI 在本機 `flowmint` 套用
當時的 Groovy draft 一次性 SQL。套用前確認版本數 4 筆、新表及鎖欄位不存在；
套用後確認腳本子表為空、兩組唯一鍵／版本外鍵／type 與 timeout 約束存在，
父表 LOCK_VERSION 為 NOT NULL DEFAULT 0，4 筆版本的值均為 0。

本次只執行已核准的 CREATE TABLE 與 ALTER TABLE，沒有業務資料 DML，沒有修改 flowmint.sql。
草稿開關仍預設關閉；尚待草稿儲存／clone／併發與瀏覽器驗收。
本節取代前面各節的「migration 未套用」現況判斷，歷史紀錄保留。

## 19. 2026-09-09 接續：契約驗證與版本內容識別

新增 `FmGroovyContractJson` 並接入既有 `FmGroovyDraftValidator`。草稿 save／clone
會檢查完整受控 Schema 子集、常數 Mapping 的值型別，以及 JSON 深度 20／值節點 10,000／
各 JSON 256 KiB 限制。拒絕重複 JSON key、尾隨 JSON、不支援的 keyword、錯誤 required／items／
界限及不完整 Unicode surrogate；JSON parser 錯誤不回傳來源片段。
此為資料契約驗證，沒有編譯或執行腳本，也尚未驗證來源表單的型別與 System Task 可寫能力。

草稿腳本統一 LF、無 BOM；Schema 與 Mapping 以遞迴鍵排序儲存。內容 hash 的 V1 envelope
包含腳本、Schema、Mapping、timeout、engine profile、policy version；JSON 數值使用
BigDecimal 正規化，object 鍵排序、array 保留順序。這讓僅有 JSON 空白、鍵順序或等值小數表示的
變動不改變 hash。既有草稿若使用舊 hash 算法，下一次合法儲存／clone 時重算；本批未執行資料遷移。

新增 `FmGroovyVersionManifest`，以正式設計 XML、Tenant／Process／Version、依 bindingId
排序的 node／binding digests 產生 canonical manifest 與 SHA-256。EngineProfile 要求固定
Groovy 5.0.6、policy 1、JDK vendor／完整 runtime version 與 worker image SHA-256 digest；
拒絕 latest 或缺少完整 JDK build 的設定。該 profile 必須由後續受信任部署配置提供，格式驗證
不代表 image 已拉取或隔離已驗收。

Manifest 建構器會重新檢查 BPMN 與 bindings，但目前**沒有接入 Publish、資料庫保存或 worker
request**；沒有編譯通過證明，也沒有開放 Groovy 發布。完整 profile JSON 僅用於 manifest／hash，
不寫入既有 Entity 的 ENGINE_PROFILE 欄位。草稿仍用原語言政策 profile；正式 profile 的 binding
hash 與草稿 hash 不同，後續必須由可信 Publisher 固定並保存，不能把草稿 hash 當成發布憑證。

app 與 worker 共用 `backend/test-contracts/groovy-schema-v1.json` 的 22 個案例。
worker 同步拒絕重複 type／required，修正巢狀 object／array enum 的等值小數比較，
使兩端對相同契約有一致結果，且 worker 仍不依賴 app。

驗證結果：app 編譯與 53 項針對性測試通過，worker package 與 62 項測試通過。
其中兩端各執行 22 個共用 Schema 案例；這些測試不寫入 MariaDB。

驗證命令（PowerShell 的 `-Dtest=...` 參數請加單引號）：

```text
# backend/app
mvn -o -Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest test

# backend/groovy-worker
mvn -o package
```

本批未變更資料庫、flowmint.sql、前端功能或功能開關。工作目錄既有完整版本 bundle 匯入／匯出
已有程式及設計器接線，不能再描述為完全未實作，但仍待實機 round-trip／草稿交易與瀏覽器驗收。

下一步：接編譯檢查／試跑 API、診斷跳轉及專用 capability，
再完成發布保存／manifest 核對、Form 型別與可寫能力、Runtime／Snapshot／execution／attempt／
Incident／重試復原。上述 Runtime 接線仍是待開發項，不是只剩測試。

## 20. 2026-09-10 檢查／試跑接線與接續入口

本節取代前文「檢查／試跑 API 完全未實作」的現況描述。中斷工作目錄已具備
`FMGroovyPreviewController`、Preview Logic／Runner／Protocol／Quota 及設計權限檢查；
本批新增前端接線、人工 sample、結果與日誌、診斷跳轉，並同步 Editor 與 BPMN 配置說明。

- `POST /api/FM_PROG004D0001/groovy/validate`：檢查目前編輯副本，不要求 sample。
- `POST /api/FM_PROG004D0001/groovy/preview`：人工 JSON 作為 input，不讀正式單據，
  不執行輸出 Mapping、不寫回 Form Data、不套用或保存草稿。
- 請求包含已保存草稿的 oid／expectedLockVersion、目前 BPMN、全部 Groovy bindings 與目標 nodeId。
  目標 binding 使用 Dialog 副本，其餘 binding 使用 modeler 設定，不改動原始物件。
- 後端檢查 DRAFT、Tenant、版本鎖、明確 `FLOWMINT_GROOVY_DESIGN` 權限及既有流程更新權限。
  此權限檢查目前用於 Preview；完整設計／發布／營運 capability 邊界仍待接續。
- Preview 使用 manifest 建構器產生本次 binding hash；不代表正式 Publish manifest 已持久化。
- 修改內容、sample、關閉／卸載或切換版本後不接受過期結果。AbortController 只取消瀏覽器等待，
  不宣稱已終止 worker；supervisor deadline、清理與後續 reaper 仍各自負責。
- Dialog 使用局部 busy 狀態，保留取消／關閉；原生 modal dialog 會遮住 body 上的 SweetAlert，
  因此此處沿用 Editor 局部狀態，不使用全頁 useSwalLoading。HTTP 仍使用 QIFU4 getAxiosInstance、
  success flag、checkFields 與 escapeQifuHtmlMsg。

### 啟用前置（尚未驗收，不可只為顯示成功而開啟）

Runner 預設拒絕執行。部署需由可信設定提供：

```properties
flowmint.groovy.draft-enabled=false
flowmint.groovy.preview.enabled=false
flowmint.groovy.preview.java-executable=
flowmint.groovy.preview.supervisor-classpath=
flowmint.groovy.preview.worker-image=
flowmint.groovy.preview.jdk-vendor=
flowmint.groovy.preview.jdk-runtime-version=
```

java-executable 必須為可信 Java 的絕對路徑；supervisor-classpath 指向獨立 worker jar 與依賴。
worker-image 必須固定 SHA-256 digest，JDK runtime version 包含完整 build。不得從請求取得這些設定，
未配置／未驗收時 API 回報不可用為預期行為。

### 驗證與待辦

接續前重新執行 app 73 項與 worker 63 項測試，全部通過。本批前端 Node 測試 15 項通過，
Edge 固定 fixture 通過 CHECK／RUN、人工 sample、欄位錯誤、結果文字安全、過期回應、
關閉重開、唯讀與不套用行為。Nuxt production build 通過。

```text
node --test tests/groovy-draft.test.mjs tests/process-version-bundle.test.mjs tests/groovy-preview.test.mjs
node tests/groovy-preview.browser.mjs
```

瀏覽器 fixture 編譯真實 Vue 元件與 CodeMirror，但使用假 API，不接正式後端或 MariaDB。
本次未變更資料庫與開關。下一步為權限配置、真實 API／草稿交易／
bundle／多帳號驗收，再接正式發布 manifest 保存、表單型別與可寫能力、Runtime、Snapshot／
execution／attempt／Incident／重試復原。Groovy 正式發布仍未開放。

## 21. 2026-09-10 Runtime 準備入口與實際接續邊界

中斷盤點確認已有未完整記入第 20 節的 FormContract、RuntimeMapping、manifest verify、
execution／attempt／manifest 持久層及 `20260910_groovy_runtime.sql` 審閱草案。
既有檔案包含基礎測試，但尚無 `FmGroovyTaskDelegate` 與完整交易呼叫鏈。

本批新增 `FmGroovyRuntimePreparation`，串接固定版本 manifest verify、目標節點選取、
表單契約與可寫權限、輸入快照、完整輸出與合併後表單驗證。PUBLISHED／RETIRED 可供
既有固定版本準備；DRAFT、manifest 不符、未知節點與不可寫欄位拒絕。此入口只接受
由後續可信 Logic 載入的固定版本及 Form Schema；不自行證明 Form 綁定、profile 未撤銷、
呼叫權限或 worker 結果真實性。輸出套用計畫不執行 SQL，也不推進 Flowable。

本批後端編譯與 89 項測試通過，其中新增 3 項準備入口串接測試。
未修改前端、功能開關、MariaDB 或 flowmint.sql，未套用 Runtime migration。
接續順序：可信 Logic 載入與 Form 綁定 → 正式發布與 manifest 保存 → Runtime worker
協定／Delegate → lease／execution／attempt 與原子提交 → Incident／重試／取消／復原。
實機交易驗收仍是啟用前必要條件；上述尚未接線部分仍需開發，不能描述為只剩驗收。

## 22. 2026-09-10 固定版本與表單載入 Logic

新增 `FmGroovyRuntimeDefinitionLogicServiceImpl`，經 Service 層載入固定版本與 manifest、
全部 Groovy bindings、Task Form Rules、固定 Form Schema，接入第 21 節準備入口。
不依賴 Mapper、不提供外部 API、不使用主檔 Current Version。讀取交易為 read-only。
Process／Form 的 PUBLISHED 或 RETIRED 可供舊流程使用；DRAFT、查詢結果缺少／重複、
身分不符、profile 不符、manifest 竄改均拒絕。

現行起單表單來源為 Task Form Rules。首批要求全部 rule 指向相同 Form／Version，
且與呼叫端提供的固定 Form 識別一致；混合表單拒絕，不猜測相鄰 User Task。
呼叫端必須由持久化 process instance／Form Data 提供這些識別，並從可信設定選定 profile；
此入口不取代該來源驗證、profile 撤銷檢查、worker 協定核對或提交交易的 fencing。

驗證：93 項後端回歸通過；追加反向案例後，7 項載入測試重跑全部通過。
未執行 MariaDB／migration，未修改前端或啟用開關。此入口仍待 Delegate 呼叫，
正式 manifest 保存、instance／Form Data 載入、worker 請求、execution／attempt 與原子提交、
Incident／重試／取消／復原及實機 E2E 尚未完成。

## 23. 2026-09-10 發布編譯與 manifest 保存元件

`FmGroovyPublicationCompiler` 先驗證全部表單 Mapping，再逐節點透過現有 runner
執行 CHECK；每份回應重新核對協定識別、binding hash 與 VALID 狀態。全部成功才建立
私有 constructor 的 `VerifiedPublication`，固定 manifest／profile／Form Schema hash。
這是伺服器本次編譯的內容證據，不是發布授權，不接受瀏覽器宣告 Preview 已通過。

`FmGroovyPublicationLogicServiceImpl.persist` 必須加入外層交易，鎖定 DRAFT、核對版本鎖，
重讀版本與 binding，重建 manifest；同時確認唯一 Form 綁定與已發布 Form Schema 仍符合
證據後 insert。編譯後內容改動即拒絕。外層發布者必須在同交易部署並更新 PUBLISHED，
失敗時整筆回滾；不得只呼叫 persist 後獨立提交。本元件尚未接入既有 publish 方法。

Compiler 暫共用 Preview runner 與 quota，預設仍停用；正式發布前須完成專用部署政策、
權限與 profile 撤銷判斷，以及隔離驗收。現有程式沒有為了此元件開放 Groovy 發布。

驗證：編譯與 101 項後端測試通過，新增 5 項案例包含成功保存、過期鎖／XML、表單變動、
偽造回應／runner 不可用，以及 Spring 代理拒絕沒有外層交易的保存。測試使用模擬 runner
與 Service，不是實際資料庫交易證據。未執行 migration 或修改功能開關。

尚待外層發布接線、BPMN Groovy 轉換、Delegate、正式 worker 呼叫及 execution／attempt／
Form／Snapshot／receipt／Flowable 交易、Incident／取消／重試與復原；完整功能仍未完成。

## 24. 2026-09-10 中斷前交接：下一輪從這裡繼續

**整體程式未完成，不是只剩測試。** 本節整理實際停點；內部元件或 mock 測試不代表
正式功能交付。本次只更新文件，未新增程式或測試成果。

### 已有程式接點

下列路徑以 `backend/app/src/main/java/org/qifu/fm/` 為根：

| 接點 | 已有行為 | 尚缺接線 |
| --- | --- | --- |
| `domain/workflow/FmGroovyPublicationCompiler.java` | 逐節點 CHECK、回應核對、編譯證據 | 既有 publish 尚未呼叫；暫共用 Preview runner／quota |
| `logic/impl/FmGroovyPublicationLogicServiceImpl.java` | MANDATORY、草稿鎖、重讀比對、manifest insert | 外層部署／PUBLISHED 同交易 |
| `logic/impl/FmGroovyRuntimeDefinitionLogicServiceImpl.java` | 固定版本／manifest／Form／bindings 載入 | instance／Form Data 可信來源與 Delegate |
| `domain/workflow/FmGroovyRuntimePreparation.java` | 契約、輸入與輸出套用計畫 | 不執行 worker、不寫表單、不推進流程 |
| `domain/workflow/FmGroovyRuntimeMapping.java` | 輸入快照與合併後表單驗證 | 提交時 lease／revision／lock 核對 |
| execution／attempt／manifest Entity／Mapper／Service | 部分持久層與 claim／complete／cancel | 完整執行生命週期及復原 |

### 下一輪實作與完成條件

1. 接 `FmProcessDefLogicServiceImpl.publish`／`runtimeBpmnXml`：專用權限與啟用條件、可信
   profile、完整固定草稿編譯、重新鎖定比對、manifest 保存、Groovy Delegate BPMN 轉換，
   部署與 PUBLISHED 同交易。失敗不得留下 manifest；不得單獨呼叫 persist 後提交。
2. 新增 Groovy Delegate／Runtime Logic，從持久化 process instance／Form Data 取得 Tenant、
   固定版本、表單與 context，接既有 Definition／Preparation，再建立 worker RUN 請求。
   核對回應識別、hash 與 generation；不可直接以 Preview API 充當 Runtime。
3. 完成 invocation 識別、輸入快照、execution／attempt／lease，以及 Form revision／lock CAS、
   Snapshot、receipt、Flowable variables／推進同交易；以失敗注入驗證 rollback 與重送冪等。
4. 完成 Incident、取消／終止、人工重試／重算、單一重試控制者、reaper／crash 復原。
   晚到回應不得覆寫新表單或重啟已終止流程。
5. 完成 MariaDB／Flowable／瀏覽器多帳號 E2E 後才開啟正式發布。

上述仍需開發，不是已完成清單。不得把「新增類別」寫成「整體 code 已完成」。

### 最後驗證與環境狀態

最後一次後端命令在 `backend/app` 執行：

```text
mvn -o '-Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest' test
```

結果 101 項、0 failures、0 errors、0 skipped，BUILD SUCCESS；其中 Publication Logic
測試 5 項。Spring 代理驗證未有外層交易時拒絕保存，但 mock Service／runner 不代表
實際資料庫 rollback。89／93 項是歷史批次結果，本次未重新跑測試。

- `20260908_groovy_draft.sql`：依既有紀錄已套用，本次未回查資料庫。
- `20260910_groovy_runtime.sql`：仍為待審閱／核准草案，未套用；資料庫工作先完整讀第 21 章。
- 草稿與 Preview 預設關閉，正式 Groovy 發布仍被拒絕。
- 本輪接續未修改 `flowmint.sql`；Git 先前已有變更不得歸為本輪新成果。
- Git 仍有大量未提交／未追蹤程式、測試與文件。接續時保留工作目錄，不 reset／clean，
  不只依最後 commit 判斷進度。

## 25. 2026-09-11 Groovy BPMN 轉換元件

新增 `FmGroovyRuntimeBpmn.prepare(designXml, verifiedPublication)`，先核對原始設計 XML
與編譯證據的 BPMN hash，再逐節點核對 nodeId／bindingId。只轉換 GROOVY，固定使用
`${fmGroovyTaskDelegate}`、async／exclusive 與 `R0/PT1M`，以原生 fieldExtension 保存
`flowmintBindingId`、`flowmintBindingSha256`。缺少證據或 XML 改變即拒絕。

入口回傳 `BpmnModel`，沒有部署或寫資料庫。發布器須在同一 model 完成 User Task 與
Data Action 的既有驗證／轉換後才輸出 XML；混合測試確認中途 XML round trip 會丟失
Data Action 自訂屬性，因此不可先把此元件輸出序列化後再交回既有發布器。

後端編譯及 104 項針對性測試全數通過，0 failures／errors／skipped。新增 3 項案例涵蓋
原生 field／固定 Delegate 的 XML round trip、多 Groovy 與 Data Action 混合模型，以及
過期 XML／缺少證據。曾由混合測試重現屬性遺失，改為回傳 model 後回歸通過。

此為內部轉換元件，尚未接入 `publish`／`runtimeBpmnXml`；Groovy Delegate 本體、正式
worker RUN、原子交易、Incident／取消／重試／復原仍待開發。未執行 MariaDB、migration、
瀏覽器 E2E；未修改功能開關。第 24 節接續順序仍適用，新增元件可供發布器整合。

## 26. 2026-09-11 外層發布協調接線

`publish` 以 NOT_SUPPORTED 暫停呼叫端交易，從已保存版本取得 Groovy bindings、lock version
及唯一已發布表單契約。Groovy 專用權限與就緒檢查通過後才編譯；compiler 禁止在 active
transaction 內呼叫，避免 worker 等待占用草稿鎖。

新增 Logic interface／implementation `IFmGroovyPublishCoordinatorLogicService`／
`FmGroovyPublishCoordinatorLogicServiceImpl`，使用指定 `transactionManager` 的
TransactionTemplate。在同一交易重新檢查權限、就緒及 profile，呼叫既有 MANDATORY
manifest persist，再執行發布驗證、BPMN 轉換、Flowable deploy 與版本／主檔更新。
checked ServiceException 會觸發 rollback 後重新拋出原例外，RuntimeException 亦回滾。
一般無 Groovy 的流程使用同一交易 helper，但不要求 Groovy 發布權限。

`runtimeBpmnXml` 已接入 verified Groovy model，Data Action validator 增加混合模型入口；
Groovy 節點不再被覆寫成 Data Action Delegate。User Task 與 Data Action 均在同一 model
完成既有轉換後才輸出 XML。混合測試驗證兩個 Groovy 與一個 Data Action 的 Delegate、
binding fields、Action Code 及 retry cycle 能通過實際發布器轉換與 XML round trip。

`FmGroovyPublishAccess` 要求 explicit `FLOWMINT_GROOVY_PUBLISH`、既有
`FM_PROG004D0001X`、有效帳號與 Tenant 權限。`flowmint.groovy.publish.enabled` 預設 false，
並要求唯一 `RuntimeReadiness` provider 回報就緒。**目前沒有正式 provider，Groovy 仍不能
發布。** provider 必須等 Runtime／交易／復原／隔離驗收完成後實作，不得僅回 true 放行。
Compiler 暫共用 Preview runner／quota，發布專用部署與配額、profile 撤銷政策仍待補齊。

測試使用真實 Spring TransactionTemplate 配合 recording transaction manager、mock Service／
worker，驗證成功 commit、兩種例外 rollback、stale manifest／profile 拒絕及編譯交易邊界；
這不證明 MariaDB／Flowable 同庫原子性，也不是完整 publish 成功或瀏覽器 E2E。
未異動資料庫、migration、前端、功能開關或 `flowmint.sql`。

本批最終驗證：`mvn -o clean '-Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest,FmDataActionTask*Test' test`
完成乾淨重建，111 項測試全數通過，0 failures／errors／skipped；`git diff --check` 通過。

接續重點已移至第 24 節第 2～5 項：Delegate、可信 instance／Form Data context、正式 worker
RUN、execution／attempt／lease、表單 CAS／Snapshot／receipt／Flowable 同交易及 Incident／
取消／重試／復原；另須補發布專用 runner 政策與實際發布交易驗收。

## 27. 2026-09-12 可信 Runtime 輸入與 RUN 評估元件

本批完成的接點如下，Java 路徑以 `backend/app/src/main/java/org/qifu/fm/` 為根：

| 接點 | 本批行為 |
| --- | --- |
| `logic/impl/FmGroovyRuntimeInputLogicServiceImpl.java` | 從 Flowable execution／instance、FlowMint instance／固定 version／Form Data 取得可信身分、部署節點與輸入快照 |
| `domain/workflow/FmGroovyRuntimePreparation.java` | 保留 manifest 驗證過的腳本／契約，新增固定 RUNTIME 請求組裝、snapshot hash 與 worker ID 契約檢查 |
| `logic/impl/FmGroovyRuntimeEvaluationLogicServiceImpl.java` | 讀取已提交 RUNNING claim／attempt，RUN 前後核對身分／lease／generation／輸入／表單版本，驗證回應並回傳 ApplyPlan |
| `domain/workflow/FmGroovyRuntimeRunner.java` | 使用 Runtime 獨立設定與 admission 計數，透過既有 supervisor CLI transport 交換有界 JSON |

### 27.1 輸入與執行邊界

Input Logic 不使用 `flowmintFormData` 或其他 Flowable variable 推測 Tenant、Process Version、申請人、
發起人或 Form Data ID。Tenant 必須與引擎 execution／instance 及資料庫紀錄一致；固定版本的
Flowable Process Definition ID 必須與實際引擎 instance 一致。Form owner 是申請人，Process
Instance initiator 是發起人，兩者可以不同。表單 JSON 中的同名欄位不覆蓋這些 context 值。

部署節點必須為固定 `${fmGroovyTaskDelegate}`、async／exclusive、`R0/PT1M`，且只有兩個
原生 binding fields，並與重新驗證的 manifest 一致。Input Logic 為唯讀交易，沒有表單 row lock。

Evaluation Logic 只接受 `tenantId + invocationId + attempt request ID`，由 Service 載入資料，
不接受呼叫端傳入腳本、result 或任意輸入。其 `Propagation.NEVER` 與方法內檢查拒絕 active
Spring transaction。RUN 前後核對 RUNNING claim、唯一 RUNNING attempt、generation、有效 lease、
engine profile、固定 manifest／binding，以及保存的 input／context／hash、Form revision／lock。
重新讀取表單只用於比較，不能把新表單取代原 invocation 輸入後自動重試。

回應使用同一 supervisor 協定驗證器核對 invocation／attempt／generation／binding hash，限制
大小並檢查完整輸出 Schema；回傳的 ApplyPlan 只含提交所需資料，不攜帶 worker logs／diagnostics。
本批沒有持久化任何執行結果，也沒有成功 receipt。

### 27.2 Runtime runner 設定

新增設定前綴 `flowmint.groovy.runtime`：

- `enabled`：預設 false。
- `java-executable`、`supervisor-classpath`、`worker-image`、`jdk-vendor`、`jdk-runtime-version`：
  預設空值，必須由可信部署提供，profile 仍要求固定映像 digest 與完整 JDK build。
- 單機 admission 暫沿用 8 個全域／每 Tenant 2 個的有界即時拒絕政策，但計數物件與 Preview 獨立。

共用的是 transport 和 wire protocol，不是 Preview API 或其功能開關。尚未有
Runtime 部署 profile 撤銷管理與完整 timeout／memory／crash
錯誤分類；`GROOVY_RUNTIME_RUNNER_FAILED` 目前不可直接推導為可自動重試。
正式發布仍缺 RuntimeReadiness provider，所有開關維持原值，未啟用 Runtime。

### 27.3 明確未完成與下一個入口

**仍需開發，不是只剩驗收：**

1. Delegate／job 協調與 occurrence：建立並提交 invocation／input snapshot、claim／attempt，
   以實際 async job 識別區分重送、dead-letter 狀態、平行 execution 及迴圈重入。
2. 將已提交 claim 交給 Evaluation Logic，在沒有 active Spring／Flowable command 交易的
   執行邊界呼叫；目前沒有任何正式 Delegate／scheduler 呼叫此元件。不得直接在既有 Delegate
   交易中呼叫，也不能只加 NOT_SUPPORTED 就假定引擎 CommandContext 已安全分離。
3. 回應提交協調：在 Flowable 所用交易重新核對 active execution、job occurrence、取消狀態、
   lease／generation、輸入 revision／lock、profile，原子完成 Form CAS、Snapshot、receipt、
   attempt 終態與流程推進。ApplyPlan 可讀不是已提交成功，不能另行保存 receipt 後推進流程。
4. 失敗獨立記錄、Incident、取消、單一重試預算、人工重算、過期 lease reaper 與 crash 復原。
5. Runtime migration 套用、真實 MariaDB／Flowable 原子交易与瀏覽器 E2E；
   原第 26 節的發布專用 runner／quota／profile 撤銷政策亦仍待完成。

本批無資料庫命令或 migration 變更。下一次涉及資料庫前仍須完整讀第 21 章並遵守其操作規範。

### 27.4 驗證

在 `backend/app` 執行：

```text
mvn -o '-Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest,FmDataActionTask*Test' test
```

127 項全部通過，0 failures／errors／skipped；新增 Input Logic 6 項、Evaluation Logic 9 項及
Runtime Runner 1 項。涵蓋可信申請人／發起人、固定 retired version、跨 Tenant、已終止／暫停、
部署／binding 不符、表單 lock 缺少或改變、claim／attempt 過期／取消／歧義、晚到回應、錯誤
generation／輸出型別／回應大小、固定 RUNTIME 請求，以及不建立成功 receipt。另有 compile 成功。
測試使用 mock Flowable／Service／runner，不代表實際 worker 或 MariaDB／Flowable 原子性。

## 28. 2026-09-13 中斷接續：Runtime occurrence 與 lease reaper

### 28.1 接續前已有成果

工作目錄已存在未記入第 27 節的 `FmGroovyTaskDelegate`、`FmGroovyJobIdentity`、
`FmGroovyFlowableTransactionGuard`、Runtime Claim／Commit Logic 及測試。
Claim 以 job correlation 與 Tenant 計算 invocation，保存毫秒精度的固定輸入，獨立短交易
建立 execution／attempt／lease。Delegate 使用 FutureJavaDelegate 在不同執行緒進行 claim
與交易外評估，再由 afterExecution 回到原引擎交易執行表單 CAS、Snapshot、成功紀錄與變數
更新；回滾後另行記錄失敗。這些是中斷前已有程式，本輪初始回歸 142 項通過。

### 28.2 本輪新增與修正

提交新增 `FmGroovyExecutingJob` adapter，從目前 CommandContext 的 ExecuteAsyncRunnableJobCmd
或 ExecuteJobCmd 取得 job ID，於同一 EntityCache 取出該 job，再用既有 JobIdentity 驗證
Tenant／execution／instance／definition／node 與 invocation。未知 command、缺少 job 或
不同 occurrence 拒絕寫回，不任選同 execution 的某筆 job。

本機 Flowable 8 bytecode 顯示 AsyncContinuationJobHandler 只把流程操作放入 agenda，
DefaultJobManager 隨後先排入 job deletion。因此 afterExecution 不以普通 JobQuery 的
「仍可查到可執行 job」作為條件；pending deletion 是該 command 正常生命週期的一部分，
仍與引擎推進同交易提交。測試使用真實 CommandContext／EntityCache／JobEntity，包含
pending deletion 與其他快取 job，但引擎完整執行及併發刪除仍需實機證明。

新增 Recovery Logic 與 Service／Mapper 到期查詢及更新：

1. Tenant 範圍內掃描到期 GROOVY RUNNING execution，每批最多 100 筆。
2. 每筆獨立 REQUIRES_NEW 交易鎖 invocation，重新核對 expected generation、lease 與唯一 attempt。
3. 同交易把 attempt 改為 ABANDONED，execution 改為 FAILED／GROOVY_LEASE_EXPIRED。
4. 任一更新失敗回滾；已終態、generation 改變或 lease 未到期不覆寫。
5. 不更改 Form、Snapshot 或 Flowable，不執行腳本，不直接重新 claim；未知結果不自動重試。

### 28.3 排程與設定

`FmGroovyLeaseReaper` 首次延遲 60 秒，固定延遲 30 秒；批次本身不開交易，呼叫代理 Logic
逐筆提交，單筆失敗繼續後續工作。日誌不包含 exception 內容、worker 日誌或輸入快照。

| 設定 | 預設與條件 |
| --- | --- |
| `flowmint.groovy.recovery.enabled` | false，停用時不查 Runtime 表 |
| `flowmint.groovy.recovery.tenant-ids` | 空字串；啟用時必填，逗號分隔、去重，最多 100 個，每個長度最多 36 |

Recovery 與 Runtime runner 開關分離，讓停止新工作後仍能收尾既有 lease。未完成 Runtime
migration 部署前不得啟用；本轮未變更設定或執行 migration。未列入設定的 Tenant 不掃描。
這是首批 lease 收尾，不是完整復原控制器；資料不一致的工作會保留原狀並回報 scan／recovery
deferred，後續仍需 Incident、告警與避免異常資料持續占用掃描批次的治理。

### 28.4 驗證與下一個入口

在 backend/app 執行：

```text
mvn -o '-Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest,FmDataActionTask*Test' test
```

最終 compile 與 154 項回歸全部通過，0 failures／errors／skipped。本輪新增 12 項測試：
command job 3 項、提交 occurrence 2 項、Recovery Logic 5 項、reaper 2 項；既有 Mapper 測試
追加到期 SQL 與 Tenant／批次上限驗證，Spring 可載入 Mapper。測試交易管理器只驗證交易
邊界，沒有 MariaDB rollback、完整引擎 job 時序或瀏覽器 E2E 證據。

接續工作：專屬 Incident 持久化／權限／維運 UI、取消與流程終止接線、人工重試／重算與單一
重試預算、runner 精細錯誤分類／profile 撤銷、發布專用 runner 政策，以及真實資料庫／引擎／
多帳號 E2E。正式 RuntimeReadiness provider 仍未實作，不得只開開關放行發布。
未異動 MariaDB、migration、flowmint.sql 或前端；既有未提交／未追蹤檔案全部保留。

## 29. 2026-09-13 流程撤回／取消／終止／駁回接線

新增 Cancellation Logic，以 MANDATORY 加入既有授權與流程終止交易：

- `FmRequestTrackingLogicServiceImpl`：WITHDRAW／CANCEL，外層補 checked exception 回滾。
- `FmIncidentOperationsLogicServiceImpl`：TERMINATE。
- `FmTaskRuntimeLogicServiceImpl`：REJECT，亦取消平行分支中的活躍 Groovy 工作。

呼叫點位於流程狀態更新後。Logic 以 Tenant＋instance 的 FOR UPDATE 確認狀態為 CANCELLED／
TERMINATED／REJECTED，再 locking read 該流程的 GROOVY READY／RUNNING execution，逐筆
取消 RUNNING attempt（CANCELLED／GROOVY_PROCESS_CANCELLED）及 execution。既有 execution
cancel SQL 增加 generation 並清除 lease。READY 預期沒有 RUNNING attempt；RUNNING 預期恰有
一筆同 generation attempt，數量不符或任一更新失敗時整個外層交易回滾。

Claim 在載入輸入與建立 invocation 前也先取得同一流程紀錄鎖並驗證 RUNNING。若 claim 先
取得鎖，終止等待其短交易完成後取消；若終止先完成，claim 讀到終態即拒絕。取消 execution
使用 current locking read，不受外層已建立的舊 repeatable-read snapshot 影響。worker RUN
仍在 claim 交易完成後執行，不持有新加入的流程鎖。

啟用條件為既有 `flowmint.groovy.runtime.enabled` 或 `flowmint.groovy.recovery.enabled`
任一 true；兩者皆 false 時不查 Runtime 表，維持未部署 migration 環境可執行一般流程。
停止新工作但仍需收尾舊工作時必須保留 Recovery enabled。未增加 Controller 或操作權限，
沿用原有撤回／取消／終止／駁回授權；本輪未修改設定、資料庫或 migration。

本批不提供立即取消 worker 的 transport；worker 由既有 timeout 清理。
取消後晚到提交由 execution 狀態／generation 與引擎有效性防線拒絕。直接繞過應用程式
刪除 Flowable execution 的即時取消尚未接入；異常中斷仍由到期 reaper 收尾。

驗證命令（backend/app）：

```text
mvn -o '-Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest,FmDataActionTask*Test,FmTask*Test,FmRequest*Test,FmIncident*Test' test
```

編譯與 180 項回歸全部通過，0 failures／errors／skipped；包含 6 項新增取消／claim 測試，
Mapper XML 亦驗證 Tenant、generation 與 FOR UPDATE。mock／測試交易管理器不證明實際
MariaDB lock 競爭、deadlock rollback 或 Flowable 終止 E2E，實機驗收仍必要。

下一項：Groovy 專屬 Incident 紀錄與維運查詢／權限，再接人工重試／重算、單一重試控制、
runner 錯誤分類與 profile 撤銷。正式 RuntimeReadiness、MariaDB／Flowable／瀏覽器
驗收仍未完成，不能啟用正式發布。

## 30. 2026-09-13 Incident 紀錄與安全查詢首批

本批以既有 execution ledger 作為 Groovy 失敗事件來源，不增設資料表。FAILED transition
與 expire 同 SQL 固定 `INCIDENT_OID = COALESCE(INCIDENT_OID, OID)`，与 attempt 終態同交易。
同一 invocation 首批只有一個 Incident 識別；旧資料缺 ID 時投影使用 execution OID，查詢
不異動資料。未實作獨立 OPEN／RESOLVED／IGNORED 狀態或 resolution audit，不能把此批
描述為完整 Incident 生命週期。未來人工重試須定義重開／解除與歷史保存，不能直接覆蓋事件。

### API 與權限

| POST 路徑 | Body | 回應 |
| --- | --- | --- |
| `/api/fm/operations/system-tasks/incidents` | `offset`，預設 0、每次增加 50，上限 10000 | 最多 50 筆 items 與 hasMore |
| `/api/fm/operations/system-tasks/detail` | `invocationId` | 單筆安全失敗摘要 |

Tenant 由 `X-FlowMint-Tenant` 提供並於 Logic 驗證，所有 SQL 固定 TENANT_ID、GROOVY、FAILED，
明細另限制 invocationId。投影只選事件識別、流程／版本／節點、次數、時間及 error code；
DTO 將 error code 轉成固定分類文字，不回傳原始例外、input、context、腳本或 worker log。
已終止流程的 FAILED 紀錄仍是可見歷史，不表示流程仍等待處理。

有效帳號需明確 CONTROLLER capability `FLOWMINT_GROOVY_OPERATE`，另具既有管理員或
FLOWMINT_OPERATIONS 角色，並通過 FmTenantAccessGuard 的原有 Tenant 規則。管理員不能
略過專用 capability；本輪不自動授權帳號。Runtime／Recovery 至少一個 enabled 才查表。

### 前端與驗證

`components/fm/GroovyIncidentPanel.vue` 接入既有 `/operations/incidents`，手動查詢、分頁、
明細與安全錯誤提示；不使用 assignment retry／reassign。Tenant 變更同步清空並增加請求
generation，關閉元件亦拒絕晚到回應，不顯示 raw server error。

後端沿用第 29 節命令：compile 與 186 項回歸全部通過，0 failures／errors／skipped。
新增 OperateAccess 2 項、IncidentLogic 4 項；既有 Mapper 測試追加固定 Incident ID、Tenant
與敏感欄位不查詢的檢查。前端 `node --test tests/groovy-incidents.test.mjs` 3 項通過，
包含跨 Tenant 晚到回應、卸載、分頁／明細與拒絕回應清理；使用真實 Vue reactivity、編譯後
setup 與假 API，不是 DOM 或登入 E2E。Prettier check 與 Nuxt production build 通過。

未異動 MariaDB、migration 或功能開關。下一項為 Incident 處理生命週期、人工重試／重算、
操作稽核與單一重試控制；profile 撤銷、runner 部署政策與實機驗收仍待完成，正式發布不開放。

## 31. 2026-09-13 Incident 處理狀態與操作稽核

Groovy 詳情 UI 已提供處理狀態、理由輸入及分頁稽核歷程。無歷程時為 OPEN、revision 0；
OPEN 可標記 IGNORED 或 RESOLVED，已關閉狀態只可重新 OPEN。RESOLVED 要求流程為
COMPLETED／CANCELLED／TERMINATED／REJECTED。這是管理標記，不會修改 FAILED 執行紀錄、
重試 worker 或推進 Flowable。清單仍列 FAILED 歷史，尚無處理狀態篩選。

新增 `fm_system_task_incident_action` 唯追加資料表，記錄 Tenant、execution、序號、requestId、
前後狀態、必填理由、登入操作者與時間。Service 禁止修改／刪除，Mapper 對應寫入亦不允許。
處理先鎖流程再鎖 execution，以 locking current read 讀最新稽核，校驗 expectedRevision；
相同 requestId 與相同內容重送不重複寫入，不同內容拒絕。狀態與稽核由同一追加操作產生，
寫入失敗回滾。沿用 `FLOWMINT_GROOVY_OPERATE`、角色及 Tenant guard，不接受客戶端 actor。

| POST 路徑 | Body | 用途 |
| --- | --- | --- |
| `/api/fm/operations/system-tasks/handling/detail` | invocationId、offset | 最新狀態與每頁 50 筆歷程 |
| `/api/fm/operations/system-tasks/handling/handle` | invocationId、requestId、expectedRevision、targetStatus、reason | 標記或重新開啟 |

Tenant 使用 `X-FlowMint-Tenant`。前端切換 Tenant 丟棄晚到回應；傳輸失敗時相同操作重用 requestId。
功能開關 `flowmint.groovy.incident-handling.enabled` 預設 false，與 Runtime／Recovery 開關分離。
啟用前須部署並驗證 `20260910_groovy_runtime.sql` 及新增的
`20260913_groovy_incident_action.sql`。本批只產生 migration 草案，未套用任何資料庫異動。

驗證：後端擴大回歸 193 項通過（含新增 7 項處理測試）；前端 6 項 setup 測試通過，
涵蓋重送、跨 Tenant 晚到回應及停用狀態。Mapper XML／Spring 載入亦通過。
前端 Prettier 排版與 Nuxt production build 完成，`git diff --check` 通過。
尚無真實 MariaDB 併發、Flowable 與瀏覽器 E2E 證據。

下一步為人工重試／重算及單一重試控制；runner 部署、profile 撤銷、正式 RuntimeReadiness
與實機驗收仍待完成。正式發布仍不開放，不能視為整體 System Task 完工。

## 32. 2026-09-13 沿用原輸入人工重試

已實作 `IFmGroovyRetryLogicService`／Logic、`FmGroovyRetryJob`、Controller 與既有 Incident
畫面的預檢／確認操作。此批只涵蓋同 invocation 重試；最新表單重算尚未接線。

| POST 路徑 | Body | 用途 |
| --- | --- | --- |
| `/api/fm/operations/system-tasks/retry/preview` | invocationId | 驗證條件並回傳 revision、generation、原表單修訂、hash 與剩餘次數 |
| `/api/fm/operations/system-tasks/retry` | invocationId、requestId、expectedRevision、expectedGeneration、reason | 原子受理一次重試 |

兩者都驗證 Tenant、`FLOWMINT_GROOVY_OPERATE` 與原有角色；後端不接受客戶端 actor。
預檢不執行 worker、不寫稽核。確認時再次驗證，預檢結果不是免除後端校驗的授權憑證。
僅允許 FAILED 且處理狀態 OPEN、流程 RUNNING、未有成功 receipt、無有效 lease，
及同 execution 恰有一筆身分吻合的 dead-letter job；active／timer／suspended job 存在時拒絕。

Runtime Input Loader 重新驗證引擎與發布版本，並比對原 revision、lock、input、context、
input hash、binding hash 與 manifest hash；使用原 invocation 及固定 startedAt。任何差異均拒絕，
不覆寫原輸入，也不替換腳本。本批採保守總上限三次 attempt（首次加最多兩次人工重試）；
不是第 9 節自動暫時故障分類與 1 秒／5 秒 scheduler 的實作。

受理交易依序取得流程與 execution 鎖，使用 current locking read 核對最新稽核；
同一 requestId 僅能重送同一操作者、理由、revision 與 generation 的 RETRY。
即使第一次受理後工作已成功／失敗或流程結束，完全相同重送仍只回覆先前受理成功。
新請求遇過期 revision／generation 或非 FAILED 狀態即拒絕。

稽核新增 ACTION_TYPE（STATUS／RETRY）及 EXPECTED_GENERATION；RETRY 記 OPEN→OPEN，
只表示受理，不能當作工作成功。STATUS 仍必須真正變更處理狀態。CAS 將 FAILED 轉 READY、
增加 generation、清除完成時間，保留輸入、版本、Incident ID 與原錯誤；拒絕任何 RUNNING attempt。
attempt 歷史不改寫。Flowable command 內驗證共用 Spring transaction manager，恢復 dead-letter
job 時 retries=1，移動後再次核對 correlation 導出的 invocation，技術 job ID 可不同。
任一稽核、CAS 或引擎移動失敗均回滾，不在管理員交易內呼叫 executeJob／worker。

重試的 READY claim 先建立耐久 attempt，再由既有 Evaluation 比對完整輸入；若受理後表單
變更，Evaluation 拒絕並可寫入 FAILED，避免在 attempt 建立前因輸入差異卡住 READY。
Incident 查詢擴為 FAILED 或已有 Incident ID 的歷史，新增目前 executionStatus；
READY／RUNNING 不允許人工關閉，SUCCEEDED 可標記 RESOLVED，即使流程仍在後續節點執行。
畫面顯示重試稽核、理由及「已受理，待查詢」，不把受理回應當作腳本成功。

部署仍未執行：依序審閱 `20260910_groovy_runtime.sql`、`20260913_groovy_incident_action.sql`、
`20260913_groovy_manual_retry.sql`。新版 handling Mapper 使用新增欄位，啟用前需全部套用。
`flowmint.groovy.manual-retry.enabled` 預設 false，且要求 incident-handling enabled；
Runtime runner profile 也須可用。本批未修改任何設定或資料庫，正式發布仍不開放。

驗證：擴大後端回歸 207 項通過（新增 Retry Logic 8、Job Adapter 5、READY claim 1），
前端 9 項 setup 測試通過。涵蓋重送、錯誤身分、次數上限、輸入改變、取消、交易回滾及跨 Tenant
晚到回應；另補成功後處理與 STATUS／RETRY requestId 隔離兩項測試，處理類別 9 項重跑全通過。
Prettier 排版、Nuxt production build 與 diff check 通過。測試使用 mock、實際 Spring
transaction interceptor 與 Vue reactivity，不證明真實 MariaDB／Flowable 原子交易或瀏覽器 E2E。

接續待辦：最新表單重算須預覽當前 revision、建立新 invocation 並保存原事件關聯；
自動重試需單一 scheduler、錯誤分類及退避預算。profile 在受理後撤銷、READY 尚未成功 claim
就失敗或 job 不存在的復原仍待補，現有 reaper 只收尾到期 RUNNING。真實 dead-letter 搬移、
迴圈／multi-instance occurrence、資料庫交易及正式 RuntimeReadiness 仍待驗收。

## 33. 2026-09-13 最新表單重算

已新增 `IFmGroovyRecalculateLogicService`／Logic、Controller、command／view，並接入既有
`GroovyIncidentPanel.vue`。此處「預覽」是目前表單修訂與 hash 的確認，不執行腳本或預測結果。

| POST 路徑 | Body | 用途 |
| --- | --- | --- |
| `/api/fm/operations/system-tasks/recalculate/preview` | invocationId | 原／最新表單 revision、form lock、輸入與腳本 hash、事件 revision／generation |
| `/api/fm/operations/system-tasks/recalculate` | invocationId、requestId、expectedRevision、expectedGeneration、formRevision、formLock、inputSha256、bindingSha256、reason | 受理並回傳新 invocation ID |

沿用 Tenant、專用 operate capability 與角色授權；理由必填、actor 取登入帳號。
來源須為 FAILED、處理 OPEN、流程 RUNNING，且無成功 receipt／有效 lease。最新表單 revision
必須大於原輸入 revision；未變更表單不能用重算刷新同 invocation 的重試預算。
Runtime Input Loader 校驗目前引擎節點、表單與發布版本，binding／manifest 必須與原 invocation
相同，不支援以此更換執行中的腳本。

預檢以原 invocation／startedAt 計算目前輸入 hash，使確認請求可比對同一份修訂；確認時
依序鎖流程、原 execution、目前表單。新增 `IFmFormDataService.lockStateByFormDataId` 透過
Tenant／form ID 的 FOR UPDATE current read 取得 revision／lock／content／status，防止外層
repeatable-read 舊快照接受已變更表單。預檢與確認不一致、目前表單非 SUBMITTED 即拒絕。

確認通過後由伺服器產生新 correlation、對應 invocation 與毫秒精度 startedAt，再產生新 context
及輸入快照。Flowable command 內校驗共用 transaction manager 與唯一原 dead-letter occurrence，
將 managed dead-letter entity 的 correlation 改為新值後搬回 executable，retries 固定 1，
再驗證新 job 身分。原輸入重試仍走保留 correlation 的原分支，不混用兩種操作。
新 execution 保存新 job ID、原版本與新輸入，初態 READY／generation 0／attempt 0，
`PARENT_INVOCATION_ID` 指向原 invocation；原 FAILED execution／輸入／attempts 不更新。

稽核擴充 RECALCULATE（OPEN→IGNORED）、TARGET_INVOCATION_ID 與 REQUEST_SHA256。
指紋包含 Tenant、原 invocation、revision／generation 及預檢欄位；相同 requestId 必須同 actor、
理由及指紋，完全相同重送回傳既有新 invocation，即使後續流程已結束。不依客戶端資料指定
新 invocation 或 startedAt。新 execution、job 搬移與稽核共用交易，任一步驟失敗即回滾。
資料庫 parent 唯一鍵使每個來源最多一個後繼，同 Tenant 外鍵保留雙向查詢關聯。

查詢加入有 parent 的重算工作，READY／SUCCEEDED 亦可查看，不虛構失敗原因；回應提供
parentInvocationId，稽核提供 targetInvocationId，畫面可前後切換。原紀錄 IGNORED 表示已由
重算接續，不表示新工作成功。前端遇傳輸不確定時保留 requestId，Tenant 切換丟棄晚到回應。

Migration `20260913_groovy_recalculate.sql` 仍為審閱草案，須接在 Runtime、Incident Action、
Manual Retry migrations 後。新版 ledger／handling Mapper 依賴新增欄位，啟用前需完整部署。
`flowmint.groovy.recalculate.enabled` 預設 false，並要求 incident-handling enabled 及可用 Runtime
profile。本批未套用 SQL、未變更設定，正式 RuntimeReadiness gate 仍拒絕發布。

驗證：後端沿用第 29 節擴大回歸命令，222 項全通過；新增 Recalculate Logic 10 項、Job Adapter
2 項及重算查詢 1 項，Mapper 加驗 current form lock 與關聯欄位。前端 12 項 setup 測試通過。
Job Adapter 測試包含實際 Flowable 8 DefaultJobManager.copyJobInfo 與 entity，驗證 correlation
複製；其餘使用 mock、Spring transaction interceptor 與 Vue reactivity。尚非真實 MariaDB
transaction／dead-letter 搬移或瀏覽器 E2E，不可據此認定可正式啟用。
Prettier 排版、Nuxt production build 及 `git diff --check` 通過。

接續待辦：單一自動退避 scheduler 與故障分類、READY 受理後未 claim／job 不存在的復原、
profile 撤銷及直接刪除 engine execution 的收尾、runner 部署與隔離政策、實機整合驗收
及正式 RuntimeReadiness。整體 System Task 尚未完成。

## 34. 2026-09-13 READY 未開始工作復原

已新增 `FmGroovyReadyReaper`、`IFmGroovyReadyRecoveryLogicService`／Logic 及
`FmGroovyReadyJobProbe`。解決受理重試／重算後，profile 不可用等原因在 claim 前失敗，
或 job／execution 不存在造成 ledger 長期 READY 的可辨識情況。此批收尾至可查詢的異常／取消。

### 排程與判斷

Reaper 初始等待 60 秒、每批結束後 30 秒再掃描，僅處理 READY 等待滿 120 秒的紀錄。
等待起點取 `COALESCE(NEXT_ATTEMPT_AT, STARTED_AT)`；人工 rearm 現在把 NEXT_ATTEMPT_AT
設為受理時間。新重算沿用新 startedAt；未到期的未來排程不會被提前回收。
每 Tenant 每批最多 100 筆安全識別投影，以 OID 游標往後掃描，末頁後重繞。游標只在記憶體，
重啟後重新開始，CAS 可防重複收尾；正常暫停／等待的前 100 筆不會讓後面的異常永遠掃不到。

| 流程／引擎狀態 | READY 收尾 |
| --- | --- |
| 同 occurrence 有 executable、timer 或 suspended job，或引擎明確暫停 | 保留等待，不猜測 executor 忙碌就是失敗 |
| 同 occurrence 僅有 dead-letter job | FAILED／GROOVY_START_FAILED |
| 引擎仍在原節點，但四種 job 都不存在 | FAILED／GROOVY_JOB_MISSING |
| 流程已 COMPLETED／CANCELLED／TERMINATED／REJECTED | CANCELLED／GROOVY_PROCESS_ENDED_BEFORE_START |
| execution 已刪除／結束／離開節點，或 job 屬新的 loop occurrence | CANCELLED／GROOVY_EXECUTION_REMOVED，只收尾舊 ledger |
| Tenant／實例／版本／job 身分矛盾或多筆 job | 拒絕異動並延後，不猜測哪筆工作正確 |

Job Probe 不修改、刪除或執行 Flowable job，沒有 worker 呼叫；不會清掉新迴圈工作。
如果原流程紀錄遺失或資料矛盾而不能核對身分，保持現況並記安全 warning，仍需人工調查。

### 交易、預算與啟用

每筆復原使用獨立 REQUIRES_NEW，先讀識別再依流程→execution 取得 current locks；
重新檢查 READY、expected generation、等待起點、lease／receipt。claim、取消或較新操作先完成時，
舊掃描結果無作用。`finishReady` SQL 同時條件化 Tenant／GROOVY／READY／generation／cutoff，
且拒絕任何 RUNNING attempt。更新失敗回滾；成功增加 generation、固定 Incident ID、記錄原因
與完成時間，不變更輸入／版本，不增加 attemptNo，也不製造未實際執行的 attempt。

generation 0／attempt 0 的新重算若在啟動前失敗，復原後 generation 為 1、attempt 仍為 0。
人工重試 eligibility 與 rearm SQL 已允許 0～2 次已執行 attempt，總上限仍為 3 次實際執行。
重算也允許這類未開始即失敗的來源，但仍須較新的表單 revision。失敗佇列工作可在修正啟動原因後
經原人工重試操作復原；MISSING job 不會被虛構，現有人工重試仍要求真實匹配的 dead-letter job。
前端沿用既有 Incident 區塊，後端新增固定中文原因分類。

需要 `flowmint.groovy.recovery.enabled=true`、`flowmint.groovy.recovery.ready-enabled=true`
及明列 `flowmint.groovy.recovery.tenant-ids`；新開關預設 false。整批不開長交易，一筆或一個 Tenant
失敗不阻擋其他項目；log 只有固定文字及 Tenant，不輸出 input、worker log 或原始例外。
本批沿用既有欄位，未新增 migration，也未修改設定／資料庫。先前 migration 仍未套用。

### 驗證與接續

後端第 29 節擴大回歸命令 239 項全通過，0 failures／errors／skipped。新增 Recovery Logic 8 項、
Job Probe 5 項、Reaper 3 項及零次執行人工重試 1 項；Mapper 追加 scan 游標／due time／CAS／
零次 attempt 預算條件。`git diff --check` 通過。未修改前端，因此未重跑前端建置；本批為
mock／Spring transaction interceptor 測試，尚無 MariaDB／Flowable 真實併發復原或 E2E 證據。

接續：自動退避 scheduler 與可重試故障分類、profile 撤銷治理、RUNNING 被刪除時的即時收尾、
runner 部署與實機整合驗收、正式 RuntimeReadiness。
一般 active／timer／suspended job 的長期等待不自動判敗，仍需監控 executor 與人工操作。
整體 System Task 尚未完成，正式發布仍關閉。

## 35. 2026-09-14 execution 刪除事件收尾

新增 `FmGroovyExecutionDeletionConfiguration` 與 `FmGroovyExecutionDeletionListener`。
設定器保留已有的引擎 listeners，透過 ObjectProvider 延遲取得 Cancellation Logic 與交易 guard。
Listener 僅接受 ENTITY_DELETED 的 ExecutionEntity，使用引擎 Tenant、process instance ID
及 execution ID；忽略其他 entity／event 及無 Tenant 的引擎資料。

Callback 同步執行，`isFailOnException=true`，不延後至 after-commit。先以既有 guard 確認
Flowable command context、可寫 Spring transaction 與相同 transaction manager，再呼叫
MANDATORY 的 `cancelForExecution`。依流程→active ledger 取得 current locks，僅取消對應
execution 的 READY／RUNNING，沿用既有 attempt 取消與 generation 遞增；其他平行 execution
不改變。正常成功後 receipt 已在同一交易寫入，SUCCEEDED 不在 active 查詢內。
Ledger 異動失敗必須使刪除交易回滾；沒有 FlowMint 流程主紀錄則不存取 ledger。

新增 `flowmint.groovy.recovery.execution-deletion-enabled=true` 才註冊 Listener，預設關閉。
Cancellation 同時要求 runtime.enabled 或 recovery.enabled；完整 runtime migrations 仍須先部署。
本批未套用 migration、未修改資料庫與任何功能設定、未修改前端。

此接線只涵蓋同一應用引擎 dispatcher 發出的刪除事件，不保證外部 SQL 或關閉 dispatcher
的刪除可即時收尾。未發事件的路徑仍依 reaper；不主動中止 worker，晚到結果由既有
status／generation 檢查拒絕提交。部署驗收必須包含直接刪除、正常完成、平行 execution、
刪除與 worker 提交競爭、交易失敗回滾及 application context 啟動。

驗證：沿用第 29 節擴大後端回歸命令，246 項通過，0 failures／errors／skipped。
新增取消 Logic 3 項與 Listener 4 項；另補引擎設定保留 listener 及延遲依賴解析測試後，相關 13 項重跑全通過；git diff --check 通過。
使用實際 Flowable entity／configuration、mock 與 Spring transaction interceptor；未構成真實
MariaDB／Flowable 原子交易或隔離部署驗收證據。正式 RuntimeReadiness 仍拒絕發布。

接續：自動重試 scheduler 與故障分類、profile 撤銷治理、worker 部署
及實機併發驗收。System Task 整體尚未完工。

## 36. 2026-09-14 暫時啟動故障自動退避重試

### 36.1 故障來源與重試邊界

Runtime 原先將所有 transport／admission 失敗壓成 `GROOVY_RUNTIME_RUNNER_FAILED`。
本批只拆出具可信來源、發生於腳本執行前的兩類：

| 來源 | Runtime 安全代碼 | 自動重試 |
| --- | --- | --- |
| Runtime quota 容量忙碌，尚未呼叫 transport | `GROOVY_RUNTIME_BUSY` | 是 |
| supervisor launcher 拋 IOException，尚未取得 Process | `GROOVY_RUNTIME_START_UNAVAILABLE` | 是 |
| supervisor 非零退出、輸出超限、傳輸中斷、外層 deadline、取消等 | `GROOVY_RUNTIME_RUNNER_FAILED` | 否 |
| 腳本／契約／政策錯誤、timeout、memory limit、profile 不可用、表單衝突 | 沿用既有代碼 | 否 |
| lease expiry／未知交易結果／READY 尚未啟動 | 沿用既有代碼 | 否 |

Preview transport 的 launcher IOException 使用 `GROOVY_SUPERVISOR_START_FAILED`，Runtime
再轉成上述固定代碼；不依 stdout、stderr、腳本錯誤文字或原始 IOException message 判斷。
Delegate 安全代碼白名單同步，確保 rollback 後能耐久保存正確分類。
通道／crash 的一般非零退出無法證明排除 timeout、memory 或 cleanup failure，仍須後續 supervisor
協定提供可信分類，不能把這類故障全部當作可重試。

### 36.2 排程與原子受理

`FmGroovyAutomaticRetryPolicy` 以已保存的 FAILED completedAt 為基準，attemptNo=1 等待
至少 1 秒、attemptNo=2 等待至少 5 秒；首次加人工／自動重試總共最多 3 次 attempt。
未開始的 attemptNo=0 不自動重試。Claim Logic 與 SQL 同時限制已執行次數 0～2，阻止第四次 claim。

Scheduler 初始等待 60 秒，每批結束後 1 秒再掃描；退避為最早受理時間，排程負載及引擎
dead-letter 落盤可能延後執行。每 Tenant 最多 100 筆，投影僅 OID／Tenant／invocation／generation，
以 OID 游標跨批推進，末頁重繞。重啟後以耐久 failure time 重新判斷，不重設退避起點。
單筆／單 Tenant 失敗不阻擋後續，日誌不輸出原始例外或輸入內容。

每筆受理由獨立 REQUIRES_NEW Logic 執行：

1. 依流程→ledger current locks 重新確認 Tenant、invocation、RUNNING 流程與 expected generation。
2. 檢查 FAILED、到期時間、無 receipt／lease、attempt 預算及目前 Incident handling 為 OPEN。
3. 確認有且只有一筆同 generation 的 FAILED attempt，其次數、錯誤、完成時間及固定 profile 相符。
4. 重載原版本／表單／輸入並完整比較，禁止把最新表單改成原 invocation 的新輸入。
5. 確認同 occurrence 唯一 dead-letter job，無 executable／timer／suspended job；尚未落盤則延後。
6. 寫入既有 RETRY 稽核，actor=SYSTEM_TASK、固定自動重試理由、server UUID requestId；
   使用最新 action revision，OPEN→OPEN 只代表受理。無登入的 CUSERID 回填 SYSTEM_TASK。
7. CAS rearm 並透過既有 Job Adapter 原子恢復 retries=1 的 job；任一步失敗整筆回滾。

受理只增加 generation，實際 worker claim 才增加 attemptNo。多 scheduler 或與人工操作競爭
依相同 lock／generation 序列化；較舊掃描遇 READY／新 generation 即無作用。
不自行 executeJob、不在交易內執行 worker、不修改原輸入或版本。
恢復後 claim／Evaluation 仍重查 profile、原輸入及狀態；READY 啟動前失敗沿用既有 reaper。

### 36.3 啟用、驗證與接續

需同時設定 `flowmint.groovy.automatic-retry.enabled=true`、`flowmint.groovy.runtime.enabled=true`
及明列 `flowmint.groovy.recovery.tenant-ids`。新開關預設 false，disabled 時不查詢 ledger。
先前 Runtime／Incident Action／Manual Retry／Recalculate migrations 全部仍為部署前置；
本批不新增 schema 或 action type，沒有套用 SQL、修改資料庫、前端或既有功能設定。

驗證命令（backend/app）：

```powershell
mvn -o '-Dtest=FmGroovy*Test,FmProcessGroovyDraftLogicTest,FmBpmnDesignValidatorTest,FmProcessMultiInstanceBpmnTest,FmDataActionTask*Test,FmTask*Test,FmRequest*Test,FmIncident*Test' test
```

擴大回歸 263 項通過，0 failures／errors／skipped；另補重複排程、缺失 dead-letter 及 Service
cutoff／系統稽核測試後，相關 14 項重跑全通過。涵蓋 1 秒／5 秒邊界、總預算、永久錯誤拒絕、舊 generation、關閉事件、
輸入／profile 改變、attempt 不符、CAS／job 恢復失敗回滾、掃描游標與 Tenant 隔離。
MyBatis XML parse／Spring Mapper 載入與 diff check 同步驗證。
上述是 mock、實際 Spring transaction interceptor 與 SQL 組合驗證，尚非真實資料庫／Flowable
併發及端到端 scheduler 驗收，正式 RuntimeReadiness 仍拒絕發布。

接續：profile 撤銷治理、通道／crash 可信分類、worker 部署與真實
MariaDB／Flowable 原子交易驗收。System Task 整體尚未完工。

## 37. 2026-09-14 部署檔案型 profile 撤銷檢查

### 37.1 政策格式與來源

新增 `FmGroovyProfileRevocationPolicy`。部署端設定
`flowmint.groovy.profile-revocations-file` 為絕對路徑，內容必須是以下唯一欄位：

```json
{
  "revokedProfileSha256": []
}
```

陣列填入完整 `EngineProfile.canonical()` 字串經 `FmGroovyContractJson.sha256(...)` 計算的
64 位小寫 SHA-256。Hash 涵蓋 Groovy 版本、完整 JDK vendor／runtime version、worker image
digest 與語法政策版本；不是只有 image digest，也不是 binding 或 manifest hash。
最多 500 筆、不得重複，整份檔案最多 64 KiB；未知欄位、重複 JSON key、尾隨 JSON、非法值皆拒絕。

每次 profile 檢查重新限量讀取並解析，不快取最後成功版本。已設定但檔案不存在、不可讀或解析
失敗時，回傳固定 `GROOVY_PROFILE_POLICY_UNAVAILABLE`；命中撤銷則回傳
`ENGINE_PROFILE_DISABLED`。不輸出檔案路徑、清單或原始例外。未設定路徑保持既有行為，
但不會因此開放 runtime 或正式發布 gate。

政策只由可信部署端維護，不接受 API、流程設計者或腳本指定來源；建議以已驗證的新檔原子
替換目標檔案。部署端負責權限、版本稽核與所有應用節點同步。移除清單項目是部署管理行為，
不自動恢復已 FAILED 的工作；恢復仍走原輸入人工重試，不能改寫已發布 profile。

### 37.2 接線與 Incident

Preview runner 與 Runtime runner 注入同一政策。既有發布 Compiler 與發布 Coordinator
透過 Preview runner.profile 檢查，因此編譯及發布交易都受撤銷限制。
Preview／Runtime 執行前與 supervisor 返回後重查；Evaluation 在開始與取得結果後重查；
Commit 在交易內開始及實際回寫前重查。這些邊界讀到撤銷時拒絕工作／回應／提交，
不替換 profile、不重編歷史腳本，也不視為成功或同意。

Runtime 保留撤銷／政策不可用代碼，不再壓成一般 runner failure。兩者均不在自動重試白名單。
`FmGroovyTaskDelegate` 的 Outcome 保存內部 invocation state，Commit 拋出安全代碼時
在 rollback callback 前更新失敗原因，使耐久 FAILED Incident 可區分撤銷與一般回滾。
Incident 分別顯示「執行環境已撤銷」與「無法確認執行環境撤銷政策」。

Claim 遇撤銷／政策不可用時，只為建立耐久拒絕紀錄讀取原可信配置 profile；此 accessor
仍檢查 runtime enabled 及 profile 格式，不授權 worker 執行。既有 Input Loader
仍驗證流程、版本、manifest 及表單，建立正常的 admission attempt 後，Evaluation 再以完整
政策檢查拒絕執行並產生 FAILED。如此有效的首次 occurrence 也能留下可查 Incident；
attempt 算入既有總預算，不自動重試。Runtime 停用／配置錯誤不使用這條特例。

### 37.3 驗收與限制

新增測試涵蓋不重啟讀取更新、完整 profile hash 比對、清單遺失／損壞／超限／重複值、
Preview／Runtime 共用政策、停用設定不被繞過、首次拒絕帳本、Evaluation 不呼叫 worker，
以及 Commit 撤銷的 rollback 後 Incident 代碼。使用暫存政策檔、mock 與 Spring transaction
interceptor；未啟動實際 supervisor 或修改部署清單。
沿用第 36 節擴大回歸命令，275 項全通過（0 failures／errors／skipped）；git diff --check 通過。

本批未新增 migration、未執行 SQL、未修改資料庫、前端或實際功能設定。正式 gate 仍關閉。
檔案檢查是各邊界的即時讀取，並非與 DB commit 共用原子鎖；最後一次檢查後的撤銷不保證
阻止已進入提交的交易。尚無管理 API／撤銷操作資料庫稽核、全節點同步／提交屏障或主動掃描
所有等待工作的機制。未被 executor 取出的工作要等既有執行／復原路徑觸發檢查；也不直接
強制終止 worker 程序。這些限制與實機併發驗收仍屬接續工作，不能宣稱完整撤銷治理完工。

下一個入口：通道／crash 可信故障分類、部署與全節點撤銷協調、真實
MariaDB／Flowable 原子交易驗收。System Task 整體尚未完成。

## 38. 2026-09-14 本機 Java worker 接續

後端以 ProcessBuilder 直接啟動一次性 Java worker，CHECK／Preview／Runtime 使用同一 transport。
正式發布仍受 RuntimeReadiness 限制。

先在 backend/groovy-worker 執行 `mvn -o package`，再配置 Preview：

```properties
flowmint.groovy.preview.enabled=true
flowmint.groovy.preview.worker-classpath=C:/home/flowmint/backend/groovy-worker/target/flowmint-groovy-worker-0.0.1-SNAPSHOT.jar;C:/home/flowmint/backend/groovy-worker/target/lib/*
flowmint.groovy.preview.worker-id=jvm:groovy-5.0.6-policy1
```

範例是 Windows classpath；Linux 用冒號分隔，部署時改成實際絕對路徑。
java-executable 留空使用後端 java.home 下的 Java；指定時須為 Java executable 絕對路徑。
jdk-vendor／jdk-runtime-version 留空沿用後端 JVM；若指定不同 Java，必須同步提供其實際版本。
worker-id 是可信部署識別；更換 worker artifact 時應更新識別，不代表自動核對 artifact hash。
舊 supervisor-classpath 保留為 classpath 相容別名；既有 workerImage manifest 欄位保留，接受 jvm: 識別。
Runtime 使用 flowmint.groovy.runtime 下的同名設定與獨立配額，仍須先部署 migrations 及完成交易驗收。
以上為設定範例，本批沒有修改實際開關或資料庫。

每次呼叫使用獨立 JVM，heap 256 MiB、metaspace 128 MiB；總 deadline 100～10,000 ms，
包含啟動、編譯與執行。逾時回報 GROOVY_TIMEOUT，強制終止 worker 並另等最多 2 秒確認退出。
Request 最多 768 KiB、transport response 最多 300 KiB；後端仍驗證協定、Schema、識別及撤銷政策。
同帳號子 JVM 沒有 OS 檔案／網路隔離，heap／metaspace 也不是程序總記憶體硬上限；
語法 allowlist、禁止檔案／網路／程序存取與 Grape 關閉仍保留。

本批修正 Windows 測試以 Path.resolve("*") 建立 classpath 時的 InvalidPathException，
改以字串組合 Java classpath wildcard。實際執行與驗證結果見第 18 章本批紀錄。
尚待真實 MariaDB／Flowable 原子交易、多帳號瀏覽器 E2E、正式 RuntimeReadiness 與部署驗收。

## 39. 2026-09-14 範圍收斂：唯一剩餘工作清單

System Task 的用途是流程到達節點時自動執行指定工作，成功後繼續，失敗時留下可查原因。
DATA_ACTION 與 GROOVY 是正式執行類型；HTTP_API subtype 已取消。表單讀取／寫回是選用的資料
Mapping，不是每個 System Task 必須做的工作；外部 HTTP 整合由 Groovy 腳本處理。

### 39.1 尚未完成：Groovy 主 JVM 重構與既有整合

以下按可驗收成果計數，不把每個測試案例、類別或部署步驟另算一項。

| 編號 | 剩餘項目 | 目前缺口與完成標準 |
| --- | --- | --- |
| 0（最高） | Groovy 主 JVM 重構 | 移除外部 worker／子程序／worker classpath，讓 CHECK、Preview、發布與 Runtime 使用 FlowMint 本體 Groovy library；測試與真實流程通過後刪除 `backend/groovy-worker` 整個目錄。此項完成前暫停 HTTP_API 後續開發。 |
| 1 | Groovy 單一 JAR 部署 | Runtime／Incident Action／人工重試／重算 schema 已部署；確認只部署 FlowMint 主 JAR即可啟動並執行 Groovy，不再需要額外 JAR、lib 或 OS 路徑。 |
| 2 | Groovy 正式發布接線 | 已補正式 RuntimeReadiness：檢查實際 async executor 啟動、共用交易管理器與啟用且一致的編譯／Runtime profile。仍待驗證保存、發布、固定版本、Flowable deployment 同成同敗。 |
| 3 | 真實流程自動執行 | DATA_ACTION 的 QUERY／COMMAND 與 GROOVY 均須實際從流程走到節點、完成工作並進入下一節點；必含沒有輸出 Mapping 的節點，不強制表單寫回。既有獨立執行器測試不等於主 JVM 真實流程通過。 |
| 4 | 異常管理、補執行與一致性驗收 | System Task 異常管理 UI 為必要功能：須以真實登入驗證失敗查詢、安全摘要、處理歷程、權限與 Tenant 邊界，以及狀態可確認時的原輸入人工重試與較新表單受控重算。另驗證逾時／腳本錯誤不跳過節點、取消後晚到結果不提交、requestId 重送不重複套用；只有設定回寫時才驗證資料寫入與流程推進的交易一致性，外部異動失敗不盲目重試。不得提供跳過節點、手改成功狀態、修改歷史輸入／腳本或直接修改 Flowable 資料。 |
| 5 | Designer 與權限操作驗收 | 真實登入測試新增、編輯、保存、複製／匯入匯出、Groovy CHECK／Preview、發布版本唯讀與跨 Tenant 拒絕；修正實際發現的問題，不新增編輯器功能。 |

既有程式已存在但尚未實機驗收的工作，不重新列為待實作功能。

### 39.2 從本次待辦與完成條件移除

下列項目不是本批待辦，也不自動移入下一階段；需要新的實際需求才重新立項：

- profile 撤銷管理 API／管理畫面、全節點同步、提交屏障與主動掃描。
- 獨立發布 runner／quota。
- 編譯／執行分段 timeout，以及通道／crash 更細的故障分類平台。
- 超出既有查詢、處理歷程、人工重試／重算與稽核範圍的自訂自動退避排程及額外 Incident 工作流。
- Data Action 另建一套 System Task attempt／Incident 管理、通用外部補償框架。
- 新增 Context Pad 類型轉換、集中脚本庫及額外 BPMN capability 管理機制。

既有已接線的異常查詢、處理歷程、人工重試／重算、退避、撤銷檢查與 ledger 不在本次盲目拆除：它們與 Controller、Mapper、取消及提交有依賴。異常管理 UI、受控人工重試／重算與操作稽核是本批必要功能；其他治理擴充保留既有預設關閉設定，不再擴充。
此處移除的是額外開發要求，不聲稱相關程式已刪除。Tenant、固定版本、執行上限、
既有 lease／generation 防止過期提交、權限與交易正確性仍保留。

### 39.3 本批實際程式刪減

FmDataActionTaskDelegate 原本在 responseMapping 缺省或為空時仍更新流程表單變數、
要求 FORM_DATA_ID 並寫入 fm_form_data。已移除此無條件寫入；現在沒有輸出 Mapping 時，
執行 Data Action 後直接返回，讓引擎繼續。只有配置輸出 Mapping 才套用並保存表單。
這不是取消表單回寫能力，也不宣稱整個流程可完全不綁表單。

第 1～11 節為既有契約背景；第 15～40 節為歷史紀錄。其中的「下一步／尚待／全部完成前」
不能再當作新增待辦。現行架構與順序以第 42 節及本節表格為準。

## 40. 2026-09-14 發布就緒判斷與部署盤點

已實作 FmGroovyRuntimeReadiness，透過既有介面供 PublishAccess 使用。
每次檢查唯一 SpringProcessEngineConfiguration、共用 transactionManager、實際啟動的
async executor，以及編譯／Runtime runner 啟用且 profile 相同。未啟用、撤銷或設定錯誤仍拒絕。
使用延遲 ObjectProvider 取得引擎，不在 Spring 建構階段提前建立引擎。
此檢查不是 SQL schema 驗證或完整 E2E，發布既有編譯與持久化流程仍須成功。

唯讀確認本機只有草稿表，Runtime 等 4 張表未建立，草稿 binding 為 0 筆。
完整 SQL、執行順序及影響範圍見
當時的部署 SQL 審閱檔。該批 DDL 後續已套用，審閱檔於 2026-09-19 隨整個
`migrations` 目錄依使用者指示刪除；最終結構以 `flowmint.sql` 為準。
此批僅推進第 39 節第 1／2 項；尚未宣稱任一完整交付項通過。

本批驗證：後端擴大回歸 281 項通過；新增 Runtime 獨立設定執行空 Map 腳本與回應協定
校驗後，實機／發布存取／readiness 共 8 項重跑通過；git diff --check 通過。

## 41. 2026-09-14 本機 Runtime schema 已部署

經使用者同意，依第 40 節審閱順序套用 4 份 migration。新建 execution、attempt、manifest、
incident action 共 4 表，並完成人工重試／重算所需欄位與約束。
SHOW CREATE TABLE 已逐表核對；4 表筆數皆為 0，未異動既有業務資料。
flowmint.sql、功能設定與角色權限未修改，沒有啟動新流程或執行業務資料測試。

## 42. 2026-09-19 正式決策：改由 FlowMint 本體直接執行 Groovy

System Task 的 Groovy 腳本由專業 IT 人員在測試環境設計、CHECK、Preview 與流程整合測試，
通過後才發布至正式環境；它不是提供一般使用者或不可信租戶任意執行程式碼的平台。FlowMint
本體已有 Groovy library，因此撤回獨立 Java worker、一次性子程序、額外 worker JAR／lib、
stdin／stdout transport 與 worker-classpath 部署架構。

正式 Runtime 改為：

```text
Flowable GROOVY System Task
  → 固定平台 Delegate 載入已發布 Process Version 與腳本 hash
  → FlowMint 主 JVM 直接編譯／執行 Groovy
  → 驗證輸出 Schema 與 Mapping
  → 保存 attempt／Incident／Audit，成功後繼續流程
```

保留權限、Tenant、固定版本、內容 hash、輸入／輸出契約、受控 context、錯誤分類、交易一致性
與異常管理。Groovy 可使用標準 Java HTTP Client存取 HTTP。既然改為主 JVM，
文件不再宣稱可以可靠強制終止惡意無限迴圈或提供程序級沙箱；腳本治理基礎是受信任 IT 人員、
測試機驗證、發布權限與正式版本不可變。

實作順序：

1. 將 CHECK、Preview、發布編譯與 Runtime runner 改為 app 內 Groovy 執行器。
2. 移除外部程序協定、classpath、Java executable、heap／metaspace及 supervisor 設定。
3. 將仍有價值的共用 Schema 測試案例移入 `app/src/test/resources`，同步調整測試路徑。
4. 通過 app compile、Groovy 單元／整合測試、單一 JAR啟動及真實 Flowable 流程 E2E。
5. 確認程式、設定、測試及文件均不再引用後，刪除 `backend/groovy-worker` 整個目錄；若
   `backend/test-contracts` 已無引用亦一併刪除。

不得先刪目錄再留下無法編譯或無法執行的引用，也不得保留兩套 Runtime 作為長期相容模式。
最終部署目標只有 FlowMint 主程式 JAR。

## 43. Groovy 呼叫 HTTP 與 JSON 操作教學需求

Groovy Editor 的教學 Tab 必須提供可複製的 Java 21 `HttpClient` 範例，不另建 HTTP_API subtype。
GET 與 JSON 轉 Map：

```groovy
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import groovy.json.JsonSlurper

def client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build()
def request = HttpRequest.newBuilder(URI.create('https://example.com/api/orders/123'))
    .timeout(Duration.ofSeconds(15))
    .header('Accept', 'application/json')
    .GET()
    .build()
def response = client.send(request, HttpResponse.BodyHandlers.ofString())
if (response.statusCode() < 200 || response.statusCode() >= 300) {
    throw new IllegalStateException("HTTP status ${response.statusCode()}")
}
Map json = (Map) new JsonSlurper().parseText(response.body())
return [externalStatus: json.status, externalId: json.id]
```

POST、Map 轉 JSON，再將回應 JSON 轉 Map：

```groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

Map payload = [orderNo: input.orderNo, amount: input.amount]
String requestJson = JsonOutput.toJson(payload)
def request = HttpRequest.newBuilder(URI.create('https://example.com/api/orders'))
    .timeout(Duration.ofSeconds(15))
    .header('Content-Type', 'application/json')
    .header('Accept', 'application/json')
    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
    .build()
def response = client.send(request, HttpResponse.BodyHandlers.ofString())
if (response.statusCode() < 200 || response.statusCode() >= 300) {
    throw new IllegalStateException("HTTP status ${response.statusCode()}")
}
Map result = (Map) new JsonSlurper().parseText(response.body())
return [externalId: result.id]
```

正式教學另須說明：URL encode、Bearer／API Key Header、不得在腳本硬編正式密碼、connect／request
timeout、2xx 判斷、空回應、JSON array 對應 `List`、JSON object 對應 `Map`、POST 冪等鍵，以及
斷線時結果可能不確定。這些是 IT 腳本開發規範，不再發展另一套 HTTP Designer／Definition／Delegate。

第 39 節第 1 項的資料庫部分完成；尚待實際應用部署、權限與端到端驗收。
較早「migrations 尚未套用」僅代表歷史狀態，不再適用本機。

交接補註：以上 4 份 migration 已依使用者指示於套用後刪除，部署紀錄保留。使用者已自行
匯出 flowmint.sql，Agent 未刪除或修改，不再處理。最新接續狀態集中於第 18 章最上方摘要。

## 44. 2026-09-20 主 JVM 重構第一批實作

CHECK、Preview、發布編譯與 Runtime 已改接 app 內 Groovy engine；外部程序 transport、Java
executable 與 worker classpath 設定均已從 live code/config 移除。Schema 契約案例移入 app 後，
已刪除 `backend/groovy-worker` 及 `backend/test-contracts`。後端 Groovy 相關 240 項測試通過。

此批只完成第 39 節第 0 項的程式重構與清場，不代表第 1～5 項 E2E 已完成。下一個程式入口為
第 43 節的 HTTP／JSON 教學與執行契約，之後才進行單一 JAR 啟動及真實流程驗收。

## 45. 2026-09-20 HTTP／JSON 契約完成

主 JVM engine profile 升為 `groovy-5.0.6-java21-trusted1`；舊 worker `policy1` manifest 不得視為
相同執行環境。Groovy 正式加入 `groovy-json`，CHECK 可編譯 Java 21 HttpClient，Preview 可執行
JsonOutput／JsonSlurper。Designer 教學已提供第 43 節要求的 GET／POST、timeout、Header、JSON、
Map、空回應、2xx、URL encode、認證秘密政策與冪等／結果不確定說明。下一步改為單一主 JAR
實際啟動，以及真實 Flowable／MariaDB／Incident／權限 E2E。

## 46. 2026-09-20 主 JVM 實機驗收

已以單一 FlowMint app JAR、MariaDB 與 Flowable 建立並發布隔離的 A01 測試流程：User Task
核准後進入 async Groovy System Task，再到 End Event。CHECK／Preview 實際執行成功，Preview
輸入 `amount = 21` 得到 `amount = 42`；發布後的 Runtime execution 與 attempt 均為
`SUCCEEDED`、第一次即成功且沒有 Incident，Flowable 歷史流程已有結束時間，Runtime execution
亦已清空。整條成功路徑未啟動外部 worker，也未依賴 worker JAR／classpath。

實機驗收修正兩個問題：Process System Task 查詢排序不得把不可變 `Map.of(...)` 傳入會補排序
欄位的共用查詢；主 JVM context 必須容許契約中合法的 null 值，不能使用拒絕 null 的
`Map.copyOf(...)`。兩者均已補回歸測試。

MariaDB 預設 `REPEATABLE-READ` 下，Flowable 原交易與 Groovy claim 的 `REQUIRES_NEW` 交易會在
同一 job 紀錄產生 error 1020。`system-task-local` 驗收 profile 已固定使用
`TRANSACTION_READ_COMMITTED`，成功驗收以此條件執行；正式環境啟用 Groovy Runtime 時也必須
採相同 isolation，或先重新設計這段交易邊界。第一次失敗 execution／attempt 保留為 Incident
稽核證據，沒有竄改成成功。

目前仍有一項流程層缺口：async System Task 在 User Task API 回應後才到 End Event，Flowable
雖已完成，但 `fm_process_instance.INSTANCE_STATUS` 尚未由 `RUNNING` 同步為 `COMPLETED`。這不是
Groovy 執行失敗；需以共用的 Flowable process-end 同步機制處理，且應一併涵蓋 Data Action 等
其他 async 尾端節點，不應只在 Groovy delegate 內做局部修補。

## 47. 2026-09-20 async 流程完成狀態同步

第 46 節記錄的流程層缺口已完成。新增共用 Flowable `PROCESS_COMPLETED` listener，在引擎完成
事件的同一交易內，以 compare-and-set 將 FlowMint `fm_process_instance` 從 `RUNNING` 更新為
`COMPLETED`，同時寫入 `END_DATE` 與系統更新者。同步 User Task 路徑若已先完成狀態更新，listener
的零筆更新視為冪等結果，不會覆寫既有終態。此機制不是 Groovy 特例，同樣涵蓋 Data Action
及其他 async 尾端節點。

重新以正式登入及 API 建立第三筆隔離流程後，User Task 核准回應當下仍合理顯示 `RUNNING`；
async Groovy job 完成後，execution 為 `SUCCEEDED`、attempt 1 無錯誤、Flowable runtime execution
為 0、歷史流程已有結束時間，且 `fm_process_instance` 已成為 `COMPLETED` 並寫入 `END_DATE`。

## 48. 2026-09-20 Incident 人工重試實機驗收

`system-task-local` 驗收 profile 已啟用 recovery、Incident handling、manual retry、recalculate 與
execution deletion listener；正式預設值仍維持關閉，部署時須明確決定 Tenant 與功能開關。

以第 46 節因 MariaDB isolation 失敗且保留的真實 execution，透過正式登入及營運 API 完成清單、
明細、處理狀態、原輸入重試預覽與人工重試。原 attempt 1 保持 `FAILED`，attempt 2 為
`SUCCEEDED`；execution 轉為 `SUCCEEDED`，流程繼續至 End Event，FlowMint instance 同步為
`COMPLETED`。RETRY 歷程保存 admin、理由與 requestId；相同 requestId 重送只回傳既有受理結果，
沒有產生第三次 attempt。跨 Tenant 使用同 invocation 查詢被拒絕且不回傳資料。

流程完成後再透過 handling API 將 Incident 從 `OPEN` 標記為 `RESOLVED`，revision 由 1 增為 2；
相同 handling requestId 重送維持 revision 2。execution 保留最初 Incident 的錯誤碼供歷史查詢，
最終成功狀態則由 execution status 與 attempt 2 表達，兩者不得混為資料殘留錯誤。

## 49. 2026-09-20 Recalculate 正式 API 與 MariaDB E2E

以仍在 `RUNNING` 的 Flowable dead-letter occurrence、`FAILED` Groovy execution 與新版表單資料完成重新計算驗收。表單 revision 由 1 更新為 2，preview 回傳 revision 0、generation 1、form revision 2、form lock 1，以及與已發布定義一致的 input/binding SHA-256。

正式 recalculate 將 dead-letter occurrence 以新 correlation 還原，建立 successor invocation `c90c6998-6153-3211-96ba-3c69361baab4`；successor 的 `PARENT_INVOCATION_ID` 指向 `f957abf7-ccff-399c-9199-179dab3f0487`、輸入 revision 為 2、attempt 1 為 `SUCCEEDED`。流程最後到達 End Event，Flowable active execution 為 0，FlowMint process instance 為 `COMPLETED`。

冪等驗收使用 requestId `bda79d28-43dc-4d96-b3a7-b5d9f315ec35`：相同 request/body 重送回傳同一 successor，且只建立一筆 action。該 action 為 `RECALCULATE`，狀態 `OPEN -> IGNORED`，actor 為 `admin`。另外驗證已發布 BPMN 的 `flowmintBindingSha256` 會阻擋發布後腳本遭直接竄改；驗收結束後已將 system-task script、content hash、manifest content 與 manifest hash 還原為原始發布內容。
