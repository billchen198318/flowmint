## 03 員工與任職（`FM_PROG002D0001`）— 靜態／唯讀盤查完成

**完成範圍（2026-10-02）：**已核對 README、組織與帳號規格、部門人員操作文件；`FMPROG002D0001Controller` 全部端點、`FmEmployeeLogicServiceImpl`、`FmEmployeeOrgAssignmentLogicServiceImpl`、員工及任職 Mapper；員工清單／建立／編輯頁、任職新增／編輯／停用元件；`fm_employee` 與 `fm_employee_org_assignment` DDL／唯讀資料。員工 140 筆且均 ACTIVE，均可連到 Tenant membership，140 筆均有非空截止時間；任職 141 筆，其中 ACTIVE 主要任職 140 筆，未來生效或已到期但狀態仍 ACTIVE 的任職 0 筆。BUG-003、BUG-004、BUG-052～BUG-054 均保留；目前只有一個 Tenant，未來任職與更新截止時間等異動案例未以資料重現，完成標記限靜態與現有資料盤查。

### BUG-003：員工維護 API 缺少操作人的 Tenant 授權檢查

- 嚴重度：高。性質：Java 權限邊界；Vue 透過 Tenant 選單傳入目標 Tenant。現有資料只有一個 Tenant，無法進行跨 Tenant 實際重現。
- 預期：具有員工維護程式權限的登入者，仍只能查詢或修改自己有權存取的 Tenant 資料；QIFU4 全域 Program Role 不等同 Tenant 資料授權（`backend/doc/17-QIFU4帳號與Role邊界.md` 第 4 節）。
- 實際：`FMPROG002D0001Controller.findPage()` 直接使用可由請求指定的 `tenantId` 查詢，且未限定可存取 Tenant；`FmEmployeeLogicServiceImpl` 的 `create`、`load`、`update`、`deactivate`、`tenantOptions`、`accountOptions` 均未呼叫 `FmTenantAccessGuard` 或等價檢查。`validateReferences()` 只驗證目標 Tenant 及帳號存在、啟用，沒有驗證操作人是否有該 Tenant 的 membership。同一 Controller 的任職端點使用 `FmEmployeeOrgAssignmentLogicServiceImpl`，以員工 OID 取資料，也沒有操作人的 Tenant 檢查。持有 `FM_PROG002D0001` 程式權限的人可藉由目標 OID 或 Tenant ID 存取其他 Tenant 的員工與任職資料。
- 證據：`backend/app/src/main/java/org/qifu/fm/controller/FMPROG002D0001Controller.java`、`backend/app/src/main/java/org/qifu/fm/logic/impl/FmEmployeeLogicServiceImpl.java`、`backend/app/src/main/java/org/qifu/fm/logic/impl/FmEmployeeOrgAssignmentLogicServiceImpl.java`；前端 `frontend-v-nx/pages/fm_prog002d0001/components/EmployeeForm.vue` 提供 Tenant 選單。
- 驗證：MariaDB 唯讀查詢顯示員工 140 筆、任職 141 筆，均為單一 Tenant；目前沒有跨 Tenant 資料可用於實際授權驗證。

### BUG-004：指定直屬主管可選未生效或已到期的任職

- 嚴重度：中。性質：Java 驗證與 Vue 候選清單。
- 預期：`EXPLICIT` 直屬主管應指向有效任職；`backend/doc/37-部門人員與主管配置操作說明.md` 第 6 節明確要求「特定有效任職」。
- 實際：`FmEmployeeOrgAssignmentLogicServiceImpl.managerLabels()` 與 `validateExplicitManager()` 只以 `STATUS='ACTIVE'` 查主管任職，沒有比較 `EFFECTIVE_FROM`／`EFFECTIVE_TO`；因此尚未生效或已到期而狀態仍為 `ACTIVE` 的任職可出現在 Vue 候選清單，也可儲存為指定直屬主管。Runtime 可能因此找不到可用主管。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmEmployeeOrgAssignmentLogicServiceImpl.java` 的 `managerLabels()` 與 `validateExplicitManager()`；`frontend-v-nx/pages/fm_prog002d0001/components/EmployeeAssignmentSection.vue` 使用主管候選資料。
- 驗證：MariaDB 唯讀查詢顯示目前尚未生效或已到期而狀態仍為 `ACTIVE` 的任職 0 筆，無現成資料可重現。未修改資料。

### BUG-052：主要任職不重疊期間仍被判為重複

- 嚴重度：中。性質：Java 任職有效期間驗證；目前資料沒有同一員工的兩段主要任職可直接重現。
- 預期：依 `README.md`「員工與任職管理」，同一員工在同一有效時間點只能有一筆主要任職；前後不重疊的主要任職可用於預排調職或接續任職。
- 實際：`FmEmployeeOrgAssignmentLogicServiceImpl.validate()` 在新任職 `IS_PRIMARY='Y'` 時，只要同一員工存在另一筆 `STATUS='ACTIVE'` 且 `IS_PRIMARY='Y'`，就直接拒絕，沒有比較兩段 `EFFECTIVE_FROM`／`EFFECTIVE_TO` 是否重疊。例如現任職月底結束、下月新任職開始，仍無法預先保存第二筆主要任職。資料表只有包含期間欄位的查詢索引與關係唯一鍵，沒有禁止不同期間主要任職的唯一約束。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmEmployeeOrgAssignmentLogicServiceImpl.java` 的 `validate()` 與 `assignments()`；`README.md`「員工與任職管理」的同一有效時間點主要任職規則；`backend/doc/37-部門人員與主管配置操作說明.md` 第 67 行亦明示此現行限制及手動調整方式；MariaDB 唯讀 `SHOW CREATE TABLE fm_employee_org_assignment`。
- 驗證限制：現有任職 141 筆，其中 ACTIVE 主要任職 140 筆，沒有可供重現的不重疊雙主要任職；未新增或修改資料。

### BUG-053：停用未來生效的任職會觸發有效期間約束而失敗

- 嚴重度：中。性質：Java 停用邏輯與 MariaDB 日期約束；現有資料沒有未來生效的 ACTIVE 任職可直接重現。
- 預期：預先排定、尚未生效的任職應可取消或停用，操作後不再被 Runtime 選用。
- 實際：`FmEmployeeOrgAssignmentLogicServiceImpl.deactivate()` 先設 `STATUS='INACTIVE'`，再把空白或晚於現在的 `EFFECTIVE_TO` 設為現在；若該任職 `EFFECTIVE_FROM` 在未來，截止時間便早於起始時間。`fm_employee_org_assignment` 的 `CK_FM_EOA_DATE` 要求非空截止時間晚於起始時間，更新會被資料庫拒絕並回滾，使用者無法停用這筆預排任職。
- 證據：`backend/app/src/main/java/org/qifu/fm/logic/impl/FmEmployeeOrgAssignmentLogicServiceImpl.java` 的 `deactivate()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmEmployeeOrgAssignmentMapper.xml` 的 `update`；MariaDB 唯讀 `SHOW CREATE TABLE fm_employee_org_assignment`。
- 驗證限制：現有未來生效且 ACTIVE 的任職 0 筆，未執行異動；資料庫拒絕路徑依約束與程式碼推論。

### BUG-054：員工失效時間無法由編輯畫面清除

- 嚴重度：中。性質：Vue 編輯、Java 更新與 MyBatis NULL 欄位寫入。
- 預期：員工編輯畫面的「失效時間」清空並儲存後，`fm_employee.EFFECTIVE_TO` 應變為 `NULL`，讓員工不再因舊截止時間到期。
- 實際：`edit/[id].vue` 清空失效時間後將 `effectiveTo: null` 送到 `/update`；`FmEmployeeLogicServiceImpl.apply()` 也把 Entity 的 `effectiveTo` 設為 `null`。但 `FmEmployeeMapper.xml` 的 `update` 只在 `<if test="effectiveTo != null">` 成立時寫入 `EFFECTIVE_TO`，因此資料庫保留舊值。API 更新後重新載入，畫面也會顯示原截止時間。
- 證據：`frontend-v-nx/pages/fm_prog002d0001/edit/[id].vue` 的 `btnSave()`；`backend/app/src/main/java/org/qifu/fm/logic/impl/FmEmployeeLogicServiceImpl.java` 的 `update()`／`apply()`；`backend/app/src/main/resources/org/qifu/fm/mapper/FmEmployeeMapper.xml` 的 `update`。
- 驗證：MariaDB 唯讀 DDL 確認 `EFFECTIVE_TO` 可為 `NULL`；現有 140 筆員工均有非空截止時間，因此此操作具實際使用需求。未送出更新請求。

