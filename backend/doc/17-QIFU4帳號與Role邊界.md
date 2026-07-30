# 17 QIFU4 帳號與 Role 邊界規範

## 1. 修正原因

依據主要資料庫檔 `backend/doc/flowmint.sql` 與目前 QIFU4 Entity：

- `TB_ACCOUNT.ACCOUNT` 是唯一登入帳號。
- `TB_USER_ROLE` 將帳號連到 `TB_ROLE`。
- `TB_ROLE_PERMISSION` 將 `TB_ROLE` 連到程式權限。
- QIFU4 的 Role 是程式、選單、Controller 及 API 權限 Role。

因此，QIFU4 `TB_ROLE` 不能用來表示 BPMN 簽核角色、單位主管、財務簽核人或 Flowable Candidate Group。

本文件覆蓋其他規劃文件中將 QIFU4 Role 與 Workflow Role 混用，或預設另建 `FM_IDENTITY_LINK` 的內容。

## 2. QIFU4 現有關係

```text
TB_ACCOUNT
  |
  | ACCOUNT
  v
TB_USER_ROLE
  |
  | ROLE
  v
TB_ROLE
  |
  v
TB_ROLE_PERMISSION
```

用途：

```text
登入與帳號狀態
程式選單權限
Controller／API 權限
Program ID 權限
```

這一組資料不負責：

```text
員工屬於哪個部門
員工的直屬主管
單位主管
職位及職等
簽核群組
BPMN User Task Assignee
```

## 3. 員工與 TB_ACCOUNT 關聯

FlowMint 沿用 `TB_ACCOUNT` 作為唯一登入帳號主檔，不另建重複的 Workflow User Account。

建議：

```text
TB_ACCOUNT
- OID
- ACCOUNT            UNIQUE
- PASSWORD
- ON_JOB

FM_PERSON
- person_id
- account            FK -> TB_ACCOUNT.ACCOUNT, UNIQUE, NULLABLE
- display_name
- email
- mobile
- status
```

設計原則：

- 員工可以尚未開通帳號，因此 `FM_PERSON.account` 可為 `NULL`。
- 可被指派 User Task 的員工必須有有效的 `TB_ACCOUNT.ACCOUNT`。
- `TB_ACCOUNT.ON_JOB` 與 `FM_EMPLOYMENT.employment_status` 都必須有效。
- 不使用姓名、Email 或 Employee Number 作為 Flowable Assignee。
- 不建議使用 `TB_ACCOUNT.OID` 作為 Flowable Assignee，因現有登入與權限 API 以 `ACCOUNT` 為主要識別值。
- Account 更名需有受控 Migration 與歷史映射。
- 第一階段不需要 `FM_IDENTITY_LINK`。

若未來要支援一個自然人連結多個 Identity Provider Account，再新增獨立 Link 表；不應在第一階段預先增加不需要的抽象。

多 Tenant 使用 `fm_tenant_account` 表示全域 `tb_account.ACCOUNT` 可進入哪些 Tenant、預設 Tenant 及有效期間。這不是第二套帳號，也不取代 `fm_employee.ACCOUNT`；登入後必須建立受驗證的 Tenant Context，所有 FlowMint 查詢強制套用 `TENANT_ID`。QIFU4 Program Role 為全域程式權限；Tenant-specific 資料授權由 FlowMint app 驗證，不修改 core。

## 4. 三種 Role 必須分開

| 領域 | 資料來源 | 用途 | 不可用於 |
| --- | --- | --- | --- |
| QIFU4 Program Role | `TB_ROLE`、`TB_USER_ROLE`、`TB_ROLE_PERMISSION` | 程式、選單、Controller、API 權限 | 尋找簽核人 |
| Organization Role | `FM_ORG_ROLE_ASSIGNMENT` | 單位主管、代理主管、HR、財務窗口 | QIFU4 程式授權 |
| BPMN Assignment Rule | Process Definition／Task Policy | 描述 User Task 如何解析人員 | 登入及程式權限 |

範例：

```text
TB_ROLE
- admin
- COMMON01
- FLOW_DESIGNER
- FLOW_ADMIN

FM_ORG_ROLE_ASSIGNMENT.role_type
- HEAD
- DEPUTY_HEAD
- ACTING_HEAD
- FINANCE_APPROVER
- HR_PARTNER

BPMN Assignment Resolver
- INITIATOR_DIRECT_MANAGER
- INITIATOR_ORG_HEAD
- ORG_ROLE
- APPROVAL_GROUP
```

不可做：

```text
TB_ROLE = DEPARTMENT_MANAGER
TB_ROLE = FINANCE_APPROVER
TB_ROLE = BPMN_TASK_APPROVER
```

因為人員是否為單位主管取決於組織單位、任職及有效時間，而不是全域的程式權限 Role。

## 5. User Task 的正確解析

BPMN 設定：

```json
{
  "resolver": "INITIATOR_ORG_HEAD",
  "scope": "PRIMARY_ASSIGNMENT",
  "orgRole": "HEAD"
}
```

Runtime：

```text
取得發起人的 TB_ACCOUNT.ACCOUNT
        |
        v
找到 FM_PERSON
        |
        v
找到有效 Employment 與主職 Assignment
        |
        v
找到 Assignment 所屬 Organization Unit
        |
        v
找到該單位有效的 HEAD／ACTING_HEAD
        |
        v
找到主管 Person 關聯的 TB_ACCOUNT.ACCOUNT
        |
        v
寫入 Flowable assignee
```

結果：

```text
assignee = U000008
```

`assignee` 是 `TB_ACCOUNT.ACCOUNT`，不是：

```text
TB_ROLE
Organization Role Type
Employee Number
Person ID
Assignment ID
```

## 6. Candidate User 與 Candidate Group

### Candidate User

直接保存 `TB_ACCOUNT.ACCOUNT`：

```text
candidateUser = U000008
candidateUser = U000009
```

### Candidate Group

`candidateGroups` 只用於 Workflow Work Queue 或 Approval Group：

```text
candidateGroup = approval-group:FINANCE_APPROVERS
candidateGroup = work-queue:AP_INVOICE
```

不使用：

```text
candidateGroup = TB_ROLE:admin
candidateGroup = TB_ROLE:COMMON01
candidateGroup = dept:ACCOUNTING
```

部門、職位與組織角色應先經 Resolver 轉成 Account List，或映射到明確的 Workflow Approval Group。

如果使用 Flowable 原生 Candidate Group Query，FlowMint 必須提供自己的 Group Membership 查詢整合；不得假設 `TB_USER_ROLE` 就是 Workflow Group Membership。

## 7. 權限檢查必須同時成立

即使某 Account 是 Task Assignee，也不能跳過 QIFU4 程式權限。

完成 Task 時至少檢查：

```text
1. 使用者已登入且 TB_ACCOUNT 有效
2. 使用者具有該 FlowMint Program／API 權限
3. 使用者是目前 Task Assignee、Candidate 或合法 Delegate
4. 使用者具有該 Business Data 的資料存取權
5. Task 仍為 Active 且未被其他人完成
```

兩種權限不同：

```text
TB_ROLE／Permission
    -> 使用者能不能開啟簽核程式、呼叫 API

Flowable Assignee／Candidate
    -> 使用者能不能處理這一筆 Task
```

兩者必須同時通過。

## 8. 建議資料庫約束

若 MariaDB 外鍵策略允許：

```text
FM_PERSON.account
    FK -> TB_ACCOUNT.ACCOUNT
```

並建立：

```text
UNIQUE (tenant_id, account)
INDEX  (account, status)
```

如果考量既有 QIFU4 Schema 升級相容性而不建立實體 FK，仍必須由 Service 層保證：

- Account 存在。
- Account 不重複綁定。
- Account 刪除或停用前檢查 Person／Active Task。
- 同步與修復工作可偵測孤兒關聯。

建議不要直接刪除 `TB_ACCOUNT`；使用 `ON_JOB` 或狀態停用並保留歷史。

## 9. 命名規範

為避免程式混淆，Java 類別與 API 不應只命名為 `Role`：

```text
ProgramRole
ProgramPermission
OrgRoleAssignment
ApprovalGroup
WorkQueue
AssignmentResolverType
```

避免：

```text
Role
UserRole
WorkflowRole
```

若使用 `WorkflowRole`，仍容易和 BPMN Resource Role 混淆，應使用更具體名稱。

## 10. 最終結論

FlowMint 的正確邊界是：

```text
QIFU4 TB_ACCOUNT
    -> 登入帳號，也是 Flowable assignee 的最終值

QIFU4 TB_ROLE
    -> 程式與 API 權限

FlowMint Organization Role
    -> 描述誰是某單位的主管或業務負責人

BPMN Assignment Resolver
    -> 根據流程規則及 ORG 找出真正的 TB_ACCOUNT.ACCOUNT
```

所以「單位主管簽核」的正確結果為：

> BPMN 只配置 `INITIATOR_ORG_HEAD`，FlowMint ORG 找到主管，再將主管關聯的 `TB_ACCOUNT.ACCOUNT` 設為 Flowable `assignee`。
