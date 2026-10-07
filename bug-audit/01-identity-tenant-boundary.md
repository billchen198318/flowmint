## 01 登入、身分與 Tenant 邊界 — 靜態／唯讀盤查完成

2026-10-02 接續核對 `AuthController` 的 signin／validLogined／refreshNewToken／logout、`WebSecurityConfig.jwtDecoder()`、`TokenBuilderUtils`、`TokenStoreValidateBuilder`、`JwtUserContextFilter`、`BaseUserDetailsService` 的資料庫與 LDAP 分支，以及 Vue 登入頁、auth middleware、baseStore、Workspace Tenant 選取與申請中心載入。BUG-037～BUG-040 的程式路徑仍存在。`JwtUserContextFilter` 對每次有效 JWT 請求重新查詢 `tb_user_role` 與 `tb_role_permission`，因此角色撤銷後沿用舊角色權限不是本輪的新發現。另核對 `FmTenantAccessGuard` 的呼叫範圍、表單／流程設計端與外部 API 管理端使用方式，`FmProcessRuntimeController` 起單 Header 到 Command 的 Tenant 傳遞，以及附件、Data Action、AI Task、平行加簽等直接 API 的 membership 檢查；`backend/doc/05-安全權限與稽核.md` 明定系統管理員可略過共用 Guard 的 membership 檢查，因此未將此例外誤列為新 BUG。MariaDB 唯讀 `SHOW CREATE TABLE tb_sys_token` 與聚合查詢顯示 token store 3 筆，其中未到期 Access／Refresh Token 均為 0，`tb_account.ON_JOB='N'` 帳號 0 筆；尚未用實際帳號異動重現登入／停用情境。LDAP 模式條件式缺口記於 BUG-051；申請中心 Tenant 切換的 Vue 競態記於 16 單元 BUG-050；停用 Tenant 後附件、Data Action、AI Task 及平行加簽入口仍可用的問題併入 BUG-049。

**完成範圍與限制：**已對照安全／帳號規格、Auth API 與 Token 持久層、前端登入與 Tenant 選取、共用 Tenant Guard、主要 `X-FlowMint-Tenant` Runtime Controller，以及 Groovy Incident 角色 Guard／組織健檢 membership。01 的靜態與現有資料盤查完成。資料庫目前沒有停用帳號、有效 Token 或第二個 Tenant，LDAP 亦關閉；這些情境的實際 HTTP／瀏覽器重現尚未執行，既有條目保留推論標示。

### BUG-001：設計端 Tenant 存取檢查未檢查 Tenant 狀態

- 修正狀態：**已修正（2026-10-06）**。`FmTenantAccessGuard` 現在先確認 Tenant 為 `ACTIVE`，管理員也不能存取停用 Tenant；可用 Tenant 清單排除停用資料。`FmTenantAccessGuardTest` 3 個測試通過。未在停用 Tenant 的實際部署資料重現。

- 嚴重度：中。性質：Java 權限邊界；MariaDB 現有資料沒有停用 Tenant，因此尚未做實際重現。
- 預期：Tenant 停用後，與該 Tenant 相關的新設計操作與 Runtime 存取應拒絕，並保留歷史稽核資料。`README.md`「登入、身分與 Tenant 邊界」載明 Tenant 停用後 Runtime 不再開放；程式操作手冊也要求資料狀態與有效期間驗證。
- 實際：`FmTenantAccessGuard.accessibleTenantIds()` 只查 `fm_tenant_account` 的 `ACTIVE` 狀態與有效期間，沒有查 `fm_tenant.STATUS`。`requireAccess()` 以此集合核准存取；表單、BPMN、AI Provider 與外部 API 管理等設計服務會呼叫此 guard。停用 Tenant 後，只要 membership 仍有效，這些設計 API 仍可能通過此層檢查。此項不涵蓋 Runtime 各路徑，Runtime 尚需另查。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/tenant/FmTenantAccessGuard.java`；`FmFormDefLogicServiceImpl`、`FmProcessDefLogicServiceImpl` 等呼叫 `requireAccess()`。MariaDB 唯讀查詢顯示目前 `fm_tenant` 全部為 `ACTIVE`，無停用資料可重現。

### BUG-037：更新 Token 可用自己的 refresh token 指定其他帳號發證

- 修正狀態：**已修正（2026-10-06）**。`AuthController.refreshNewToken()` 只從已驗證 refresh token 對應的 token store 紀錄取得發證帳號，忽略 request body 的 `username`。Maven 編譯通過；未以真實 Token 呼叫 API 驗證。

- 嚴重度：嚴重。性質：Java 身分驗證與帳號冒用；未實際呼叫 API，避免建立新 Token 或改變資料。
- 預期：有效 refresh token 只能替它在 token store 中綁定的同一個帳號產生新 Access／Refresh Token；請求 body 不可指定另一個使用者。
- 實際：`AuthController.refreshNewToken()` 先以 `tsv.refreshValidate(refreshToken)` 確認 refresh token 有效，之後卻用 `StringUtils.defaultIfBlank(loginRequest.getUsername(), tsv.getUserIdByRefreshToken(refreshToken))` 決定發證帳號。只要請求 body 提供非空的 `username`，便不讀取 refresh token 原綁定的帳號，也沒有呼叫 `refreshValidate(refreshToken, userId)`。`TokenBuilderUtils.createToken()` 會把指定帳號寫入 JWT claim，並由 `TokenStoreBuilder` 保存為新的有效 Token。`/api/auth/refreshNewToken` 在 WebSecurityConfig 中為 `permitAll`，所以持有自身有效 refresh token 的呼叫者可能替任意帳號發證。
- 證據：`backend/app/src/main/java/org/qifu/core/api/AuthController.java` 的 `refreshNewToken()`；`backend/base/src/main/java/org/qifu/base/util/TokenBuilderUtils.java`、`backend/base/src/main/java/org/qifu/base/support/TokenStoreBuilder.java`；`backend/app/src/main/java/org/qifu/core/config/WebSecurityConfig.java`。MariaDB 唯讀查詢顯示 token store 目前 3 筆；未查詢 Token 值、未執行發證重現。

### BUG-038：登出只清 Cookie，已核發的 Token 仍可使用至到期

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原 `logout()` 確實只清 Cookie，驗證仍依 token store。現在登出會依請求 Cookie 中的 Access／Refresh Token 刪除對應 token store 紀錄，再清 Cookie；沒有 Cookie 的請求不撤銷其他 session。尚未用真實 Token 執行 HTTP 重現。

- 嚴重度：中。性質：Java Session／Token 生命週期；目前 token store 有 3 筆，未用實際 Token 呼叫 API。
- 預期：使用者登出後，該次登入的 Access／Refresh Token 應失效，避免已複製的 Token 繼續通過驗證。
- 實際：`AuthController.logout()` 只把瀏覽器 Access／Refresh Cookie 設成空值與零期限，沒有從 token store 刪除或標記失效。`WebSecurityConfig.jwtDecoder()` 仍用 `TokenStoreValidateBuilder.accessValidate()` 驗證資料庫中的 Access Token 與到期時間；`refreshNewToken()` 亦只查 Refresh Token 是否在有效期限。因此已取得 Token 字串的其他請求，在登出後仍可使用至各自到期。
- 證據：`backend/app/src/main/java/org/qifu/core/api/AuthController.java` 的 `logout()`；`backend/app/src/main/java/org/qifu/core/config/WebSecurityConfig.java` 的 `jwtDecoder()`；`backend/base/src/main/java/org/qifu/base/support/TokenStoreValidateBuilder.java`。MariaDB 只查 token store 筆數，未讀取或揭露 Token 值。

### BUG-039：停用全域帳號後，已核發 Token 仍可驗證與更新

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原 Access／Refresh 驗證 SQL 沒有帳號狀態條件。現在 token store 驗證及以 Refresh Token 查帳號時，皆要求對應 `tb_account.ON_JOB='Y'`。現有資料無停用帳號與有效 Token，尚未做實際重現。

- 嚴重度：高。性質：Java 帳號停用與 Token 驗證；目前沒有停用帳號或其有效 Token 可實際重現。
- 預期：全域帳號停用（`tb_account.ON_JOB='N'`）後，既有 Access／Refresh Token 應失效，不能繼續存取 API 或取得新 Token。
- 實際：`BaseUserDetailsService.loadFromDB()` 只在密碼登入時檢查 `ON_JOB`；`WebSecurityConfig.jwtDecoder()` 對既有 JWT 僅驗證簽章、時間與 token store 是否有未到期的原字串；`JwtUserContextFilter` 根據 JWT claim 重建角色，沒有讀取帳號狀態。`AuthController.refreshNewToken()` 也只驗證 refresh token 有效，沒有重新檢查 `tb_account.ON_JOB`。因此停用前取得的 Token 在有效期內仍可用；refresh token 更可再取得新 Token。部分 Tenant Runtime API 可能另因 membership 被停用而拒絕，但一般已驗證 API 沒有這層全域帳號狀態檢查。
- 證據：`backend/core/src/main/java/org/qifu/base/service/impl/BaseUserDetailsService.java`、`backend/app/src/main/java/org/qifu/core/config/WebSecurityConfig.java`、`JwtUserContextFilter.java`、`AuthController.java`、`backend/base/src/main/java/org/qifu/base/support/TokenStoreValidateBuilder.java`。MariaDB 唯讀查詢顯示 `ON_JOB='N'` 帳號 0 筆、停用帳號的有效 Token 0 筆；未修改帳號或 Token。

### BUG-040：登入失敗回應仍會被前端當作成功登入

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原登入頁與 middleware 只判斷回應物件是否存在；現在要求 `onJob='Y'`、非空 `userId` 及兩個成功 Token 標記，失敗時清除本地狀態並返回登入頁。尚未以錯誤密碼執行瀏覽器驗證。

- 嚴重度：低。性質：Vue 登入狀態；只影響前端顯示與導頁，後端驗證仍會拒絕無效 Cookie。
- 預期：`/auth/signin` 或 `/auth/validLogined` 回傳空帳號、`login=N` 時，前端應保持未登入，清除本地狀態及標記，並顯示登入失敗。
- 實際：`AuthController.authenticateUser()`、`validCheck()` 在失敗且未丟出例外時會回傳非空 `User` 物件，帳號與 Token 為空、登入旗標為 `N`。`pages/login.vue` 只以 `if (responseJson)` 判斷成功；`middleware/auth.ts` 還原時也只以 `if (uData)` 判斷。兩者都呼叫 `baseStore.setUserData()`，該方法無條件把 `user.login` 設為 `Y`；`setRefreshAndAccessTokenCookie()` 亦無條件寫入兩個 `Y` 標記。使用者可能被導往工作台，接著由受保護 API 的 401 再導回登入頁。
- 證據：`backend/app/src/main/java/org/qifu/core/api/AuthController.java` 的失敗回應；`frontend-v-nx/pages/login.vue`、`frontend-v-nx/middleware/auth.ts`、`frontend-v-nx/store/baseStore.ts`、`frontend-v-nx/components/BaseHelper.ts`。未以錯誤密碼嘗試登入或改動帳號資料。

### BUG-041：快速切換 Tenant 時，舊請求可覆蓋目前工作台資料

- 複核與修正狀態：**程式路徑確認，已修正（2026-10-06）**。原 `loadWorkspace()` 完成後無條件寫入共用狀態。現在每次載入記錄請求序號與 Tenant，切換時清空舊資料，過期回應不得覆蓋目前畫面。現有資料僅一個 Tenant，尚未以雙 Tenant 操作重現。

- 嚴重度：中。性質：Vue 非同步狀態競態；目前只有一個 Tenant，沒有跨 Tenant 畫面資料可實際重現。
- 預期：工作台的待辦、我的申請、通知與可起單數量，必須始終對應畫面上目前選取的 Tenant；切換後不應被前一個 Tenant 的遲到回應覆蓋。
- 實際：`pages/workspace.vue` 以 `watch(tenantId, loadWorkspace)` 對每次選擇發出四個並行 API 請求；`loadWorkspace()` 完成後直接更新共用的 `inbox`、`myRequests`、`notifications` 與 `processCount`，沒有請求序號、取消舊請求或核對完成時的 Tenant ID。若先選 A 再快速選 B，B 先完成、A 後完成，畫面選單仍顯示 B，但清單被 A 的回應覆蓋；其中任一 API 失敗時還可能保留上一個 Tenant 的舊清單。
- 證據：`frontend-v-nx/pages/workspace.vue` 的 `watch(tenantId, loadWorkspace)`、`loadWorkspace()`。MariaDB 唯讀查詢與既有盤查顯示目前僅一個 Tenant，`fm_tenant_account` 140 筆且沒有逾期或未生效的 ACTIVE membership；無兩 Tenant 情境可直接重現。資料庫 `SHOW CREATE TABLE fm_tenant_account` 確認 Tenant／Account 唯一鍵與有效期間 CHECK，未修改資料。

### BUG-051：啟用 LDAP 登入時可繞過既有帳號的全域停用狀態

- 複核與修正狀態：**條件式程式路徑確認，已修正（2026-10-06）**。原 LDAP 成功即建立啟用的 `User`，沒有讀取本地既有帳號狀態。現在 LDAP 驗證後查詢本地同名帳號，若存在且 `ON_JOB` 非 `Y` 即拒絕；查詢失敗亦拒絕登入。LDAP 目前關閉，未做實際登入重現。

- 嚴重度：高。性質：條件式登入授權；目前 `ldap.loginEnable=N`，沒有啟用 LDAP 的實際環境可重現。
- 預期：無論以資料庫密碼或 LDAP 驗證，若既有 `tb_account.ON_JOB='N'`，全域停用的帳號都不能登入 FlowMint。
- 實際：`BaseUserDetailsService.loadUserByUsername()` 在 `ldap.loginEnable=Y` 時直接改走 `loadFromLDAP()`。LDAP 驗證成功便建立 `User(..., ON_JOB='Y')`，沒有查既有 `tb_account.ON_JOB`；`AuthController.authenticateUser()` 隨後核發 Token。登入成功後的 LDAP 同步腳本若發現同名帳號已存在就直接返回，不會再檢查或拒絕 `ON_JOB='N'`。因此已在本地全域停用、但 LDAP 憑證仍有效的既有帳號，可在 LDAP 模式取得新 Token。
- 證據：`backend/core/src/main/java/org/qifu/base/service/impl/BaseUserDetailsService.java` 的 `loadUserByUsername()`／`loadFromLDAP()`／`loadFromDB()`；`backend/app/src/main/java/org/qifu/core/api/AuthController.java` 的 `authenticateUser()`；`backend/core/src/main/resources/resource/create-user-data-ldap-mode.groovy` 的既有帳號分支；`backend/app/src/main/resources/appConfig.properties` 的目前設定。
- 驗證限制：目前設定關閉 LDAP，MariaDB 唯讀查詢顯示停用帳號 0 筆；未改設定、帳號或資料庫。本項依條件式程式路徑推論。
