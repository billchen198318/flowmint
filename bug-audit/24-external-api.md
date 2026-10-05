## 24 外部系統 API Client 與流程拋單（`FM_PROG010D0002`、`FM_PROG010D0003`）— 靜態／唯讀盤查完成

2026-10-05 收尾：已對照第 32 章、Client 管理 Controller 的 CRUD／停用／Key 發行輪替撤銷、外部設計／組織／請求 Controller、Authenticator、流程拋單與兩個管理 Vue，以及 Policy／Key／Mapper 測試。七項既有觀察涵蓋停用 Tenant、冪等紀錄、白名單、拒絕稽核、分頁及 IP Allowlist。先前唯讀資料沒有 Client／Key／Request；未發送外部 API 請求或重現跨 Tenant、網路及故障注入情境。

### BUG-174：API Client IP Allowlist 未驗證 CIDR，且執行時接受主機名稱解析

- 嚴重度：中。性質：外部 API Client 設定與來源 IP 授權；程式碼推論，尚未建立 Client 或送出認證請求。
- 預期：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 第 3.1 節規定 `IP_ALLOWLIST` 為 IPv4／IPv6 CIDR，應在管理端驗證、正規化後保存；無效規則應在儲存時回報。
- 實際：`FmExternalApiManagementLogicServiceImpl.validate()` 沒檢查 `ipAllowlist`；`apply()` 只用 `clean()` 修剪空白、去重後寫入 JSON。`FmIpAllowlistMatcher.matches()` 在每次認證時以 `InetAddress.getByName(rule)` 或 CIDR 前綴呼叫解析，非 IP 的主機名稱也會進入 DNS 解析；格式錯誤的規則則在請求時回 `false`。因此已保存的來源限制可能依 DNS 結果改變，或直到正式請求才發現 Client 無法通過 Allowlist。Vue `ApiClientForm.vue` 亦只依換行／逗號切分，沒有 CIDR 驗證。
- 證據：`FmExternalApiManagementLogicServiceImpl.validate()`、`apply()`、`clean()`；`FmIpAllowlistMatcher.allows()`／`matches()`；`FmExternalApiAuthenticator.authenticate()`；`frontend-v-nx/pages/fm_prog010d0002/components/ApiClientForm.vue` 的 `lines()`、`payload()`、`validate()`。MariaDB 先前唯讀確認 Client／Key 0 筆，未做 DNS 或 HTTP 實測。

### BUG-175：外部 Form Template 的 submissionContract 缺少巢狀路徑與值域結構

- 嚴重度：中。性質：外部設計查詢 API 契約；靜態核對，尚未用外部 Client 呼叫驗證。
- 預期：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 第 7.2 節要求 `submissionContract` 列出可提交 field key、型別、required、enum、array／object 結構及系統欄位，供外部系統產生合法 JSON。
- 實際：`FmExternalFormTemplateView.FieldContract` 只有 `key`、`type`、`required`、`multiple` 四個欄位，沒有 enum、父層路徑或巢狀結構。`FmExternalDesignQueryService.collectFields()` 遞迴走 `components`、`columns`、`rows` 時只加入當前 component 的單一 `key`，不組合 parent key；因此巢狀欄位在 `submissionContract` 中與頂層欄位無法區分，同名子欄位也可能產生重複 key。API 另有回傳完整 `templateJson`，但承諾的結構化契約資訊未由 `submissionContract` 提供。
- 證據：`backend/app/src/main/java/org/qifu/fm/dto/external/FmExternalFormTemplateView.java` 的 `FieldContract`；`backend/app/src/main/java/org/qifu/fm/domain/externalapi/FmExternalDesignQueryService.java` 的 `formTemplate()`／`collectFields()`；上述規格第 7.2 節。未以現有表單 Schema 或 HTTP 回應量測實際受影響欄位。

2026-10-04 續查 `backend/doc/32-外部系統API管理與流程拋單規劃.md`、`FmExternalApiAuthenticator` 與 `FmExternalApiQuotaGuard`。MariaDB 唯讀確認 API Client、Key、Access Log 三表均為 0 筆。文件第 586 行已明載配額檢查與 Log 寫入非原子、目前按 Tenant＋Client 計數的限制，因此未把已揭露的現況重複列為新 BUG；跨 Tenant、來源 IP、限流及外部拋單仍缺真實請求驗證。

### BUG-022：停用 Tenant 不會使其外部 API Key 失效

- 嚴重度：高。性質：Java 外部 API 授權；目前沒有 API Client、Key 或停用 Tenant 可實際重現。
- 預期：Tenant 停用後，其外部系統 API 不應再取得資料或起單；Tenant 是所有 FlowMint 業務資料的最高隔離邊界。
- 實際：`FmExternalApiAuthenticator.authenticate()` 驗證 Key 狀態、有效期間、密碼雜湊、Client 狀態與來源 IP，卻沒有查 `fm_tenant.STATUS`。`loadClient()` 同樣只查 Client 為 `ACTIVE`。因此停用 Tenant 時，既有 Client／Key 仍可完成認證，並繼續呼叫只依 Principal Tenant ID 查詢的外部資料 API。起單路徑是否成功還取決於後續 Runtime 條件，見 BUG-018。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/externalapi/FmExternalApiAuthenticator.java`；MariaDB 唯讀查詢顯示 `fm_api_client` 和 `fm_api_client_key` 目前都是 0 筆，未做寫入重現。

### BUG-159：外部拋單已成功提交後，請求紀錄更新失敗會讓冪等重送永久不可回放

- 嚴重度：高。性質：Java 外部 API 交易／冪等結果一致性；故障時序的靜態推論，未注入更新失敗重現。
- 預期：外部拋單的流程提交與請求紀錄應有可恢復的一致結果；若流程已成功建立，使用同一冪等鍵與 payload 重送應能取得原結果，不能因紀錄更新失敗再建流程或永久只回衝突。
- 實際：`FmExternalRequestSubmitService.submit()` 本身沒有 `@Transactional`。它先插入 `fm_api_request` 的 `PROCESSING` 紀錄，再呼叫有獨立交易的 `FmProcessRuntimeLogicServiceImpl.submitAs()` 建立流程，最後用 `complete()` 將紀錄改為 `SUCCEEDED`。若 Runtime 交易已提交，而 `updateResult()` 回傳 0 或拋錯，外部 API 回錯，但流程已存在；`catch (ServiceException)` 還會嘗試將紀錄標為 `FAILED`，失敗則保留 `PROCESSING`。兩種狀態都不是 `SUCCEEDED`，之後相同冪等鍵由 `replay()` 回 `IDEMPOTENCY_REQUEST_NOT_REPLAYABLE`。狀態查詢也只接受 `SUCCEEDED` 紀錄，外部系統無法用此 API 對帳已建立的流程。
- 證據：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 的冪等與狀態對帳契約；`FmExternalRequestSubmitService.submit()`／`complete()`／`replay()`、`FmExternalRequestStatusService.status()`、`FmProcessRuntimeLogicServiceImpl.submitAs()` 的交易註記、`FmApiRequestMapper.xml.updateResult`。MariaDB 唯讀查詢顯示目前 `fm_api_request`、`fm_process_instance` 均為 0 筆；未執行外部請求或故障注入驗證。

2026-10-04 續查外部拋單與狀態 API：確認狀態查詢依 Tenant、Client 與流程白名單篩選，未把僅知道 Business Key 即可跨 Client 讀取誤列為缺陷。外部拋單把申請人系統欄位改為後端解析值，但沒有清除外部送入的 `documentNumber`；這與第 26 單元 BUG-158 同源，已在該項補註，不重複編號。API Client、Key、Request 及流程實例目前仍沒有現成資料；第 24 單元維持部分盤查。

### BUG-160：API Client 流程白名單留空時會放行 Tenant 內所有流程

- 嚴重度：高。性質：Java 外部 API 授權／Client 設定；靜態推論，沒有現成 Client／Key 實測。
- 預期：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 明定 `ALLOWED_PROCESS_IDS` 是可發起流程白名單，空值不自動代表全部；Client 沒有明確配置流程時，不應因具備 `runtime.request.submit` 或設計查詢 Scope 即存取所有流程。
- 實際：Client 建立／編輯的 `validate()` 只要求 Scope，`apply()` 接受空的流程 ID 清單並存為 JSON `[]`；Vue 表單也沒有流程白名單必填檢查。認證器將 `[]` 轉成空集合後，`FmExternalApiPrincipal.allowsProcess()` 以 `allowedProcessIds.isEmpty() || allowedProcessIds.contains(processId)` 判斷，空集合直接允許任意流程。此判斷被外部拋單、流程表單查詢、表單範本綁定檢查及狀態查詢使用；具相應 Scope 的 Client 因而繞過預期的流程白名單範圍。
- 證據：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 第 3.1 節；`FmExternalApiManagementLogicServiceImpl.validate()`／`apply()`、`FmExternalApiAuthenticator.readSet()`、`FmExternalApiPrincipal.allowsProcess()`、`FmExternalRequestSubmitService.validate()`、`FmExternalDesignQueryService.processForms()`／`isAllowedBinding()`、`FmExternalRequestStatusService.status()`，以及 `ApiClientForm.vue` 的表單驗證。MariaDB 唯讀查詢顯示目前 API Client、Key 均為 0 筆；未建立空白名單 Client 或執行外部請求。

2026-10-04 另核對 Key 簽發、輪替、撤銷與 Client 停用路徑：輪替與停用在後端交易內撤銷舊 Key，認證端同時檢查 Key 與 Client 狀態；Vue 對明文 Key 僅在簽發／輪替成功後顯示，關閉或離頁會清除畫面狀態。這些已查路徑未發現另一項獨立缺陷；IP Allowlist、配額及管理稽核仍待補查。

### BUG-161：外部 API 認證與來源 IP 拒絕未留下安全稽核紀錄

- 嚴重度：中。性質：Java 認證 Filter／MariaDB 稽核資料模型；靜態推論，尚未送出失敗認證請求。
- 預期：外部 API 規格要求 Key 驗證失敗、IP 禁止、Scope 禁止、限流、撤銷及發單都留下安全稽核，以便追查未授權存取及撤銷後使用；不能只記錄已成功認證的請求。
- 實際：`FmExternalApiAuthenticationFilter.doFilterInternal()` 在 `authenticator.authenticate()` 或 `keyService.markUsed()` 拋錯時，只回 `AUTHENTICATION_FAILED`、清除 Context／MDC 後直接 `return`，沒有呼叫 `record()`；因此無效、過期、撤銷的 Key 及 IP Allowlist 拒絕均不會寫入 `fm_api_access_log`。Scope 拒絕與配額拒絕路徑則有呼叫 `record()`。目前 `FmExternalApiAccessLedger.record()` 必須取得 Principal，資料表 `fm_api_access_log.TENANT_ID`、`CLIENT_ID`、`SOURCE_IP` 均為 NOT NULL，也沒有對無法識別 Client 的認證失敗建立替代稽核路徑。
- 證據：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 第 10 節；`FmExternalApiAuthenticationFilter.doFilterInternal()`／`record()`、`FmExternalApiAuthenticator.authenticate()`、`FmExternalApiAccessLedger.record()` 與 `backend/doc/flowmint.sql` 的 `fm_api_access_log` 結構。MariaDB 唯讀查詢顯示目前存取稽核 0 筆；未執行失敗認證或 IP 拒絕重現。

2026-10-04 配額路徑核對：`FmExternalApiQuotaGuard` 依 Tenant＋Client 查分鐘與當日 Log，Filter 在請求後另行寫 Log，並非原子保留；文件第 10 節已明確揭露此限制，不重複列 BUG。Filter 使用 Servlet `getRemoteAddr()` 作來源 IP，未採用可由呼叫端任意指定的 Forwarded Header；單純 Header 偽造不構成此處已確認缺陷。IP Allowlist 值的管理驗證及反向代理部署方式仍待完整核對。

### BUG-162：外部組織清單的極大頁碼溢位後回傳 500

- 嚴重度：低。性質：Java 外部查詢分頁參數；靜態推論，未送出極大頁碼 HTTP 請求。
- 預期：`page` 為正整數且超出總頁數時，部門員工與 FLAT 組織樹清單應回空頁或受控參數錯誤；任何可解析的頁碼都不應因整數溢位變成伺服器內部錯誤。
- 實際：`FmExternalOrganizationQueryService.positive()` 僅檢查 `page >= 1`，`boundedPageSize()` 允許最多 200；兩個清單都用 Java `int` 計算 `Math.min((page - 1) * pageSize, list.size())`。當 `page=2147483647`、`pageSize=200`，乘積在 32 位有號整數中溢位為 `-400`，`subList(from,to)` 使用負索引會拋 `IndexOutOfBoundsException`。`FmExternalOrganizationController` 的一般例外分支將其轉成 500 `INTERNAL_ERROR`。
- 證據：`backend/doc/32-外部系統API管理與流程拋單規劃.md` 第 6.3、6.10 節及清單分頁要求；`FmExternalOrganizationQueryService.departmentEmployees()`／`departmentTree()`／`positive()`／`boundedPageSize()`、`FmExternalOrganizationController` 對應端點。MariaDB 唯讀查詢顯示目前任職 141 筆、部門版本 35 筆；資料量不影響溢位條件。未建立 API Client／Key 或執行外部 HTTP 請求。

2026-10-04 組織查詢續核：部門與員工入口均從 Principal 限定 Tenant，`effectiveAt` 轉成時間點，員工清單對任職做有效期間篩選；`pageSize` 已有 200 上限。員工主檔與任職狀態的組合語意仍須對照更多規格與資料，不因局部檢查將第 24 單元標為完成。
