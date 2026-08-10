# 08 API 規格

Base path：`/api/fm`。所有 API 使用登入 Account 與 active Tenant。

## 1. Tenant／Employee

```text
GET    /tenants/current
GET    /employees
POST   /employees
GET    /employees/{employeeId}
PUT    /employees/{employeeId}
POST   /employees/{employeeId}/deactivate
GET    /employees/{employeeId}/org-assignments
PUT    /employees/{employeeId}/org-assignments
POST   /employees/{employeeId}/manager-preview
```

## 2. Approval Level／Organization

```text
GET    /org-level-schemes
POST   /org-level-schemes
PUT    /org-level-schemes/{schemeId}
POST   /org-level-schemes/{schemeId}/validate
GET/POST/PUT /org-units/{orgUnitId}/titles
GET/POST/PUT /org-units/{orgUnitId}/duties
GET    /org-units/tree?effectiveAt=
POST   /org-units
GET    /org-units/{orgUnitId}
PUT    /org-units/{orgUnitId}
POST   /org-units/{orgUnitId}/deactivate
GET    /org-units/{orgUnitId}/versions
POST   /org-units/tree-move-preview
POST   /org-units/tree-move
GET    /org-units/{orgUnitId}/heads
PUT    /org-units/{orgUnitId}/heads
```

Move request 必須含 rootOrgUnitId、newParentOrgUnitId、newSortNo、expectedVersionNo、effectiveFrom。

## 3. Group／Delegation

```text
GET/POST/PUT /approval-groups
PUT          /approval-groups/{id}/members
GET/POST/PUT /delegations
POST         /delegations/{id}/deactivate
```

## 4. Form

```text
GET/POST      /forms
GET/PUT       /forms/{formId}
POST          /forms/{formId}/versions
POST          /forms/{formId}/versions/{versionNo}/validate
POST          /forms/{formId}/versions/{versionNo}/publish
POST          /form-data
GET/PUT       /form-data/{formDataId}
POST          /form-data/{formDataId}/attachments
DELETE        /form-data/{formDataId}/attachments/{attachmentId}
```

## 5. Approval Authority

`	ext
GET/POST/PUT /approval-authorities
PUT          /approval-authorities/{id}/rules
POST         /approval-authorities/{id}/preview
` 

## 6. Process Designer

```text
GET/POST      /processes
GET/PUT       /processes/{processDefId}
POST          /processes/{processDefId}/versions
PUT           /processes/{processDefId}/versions/{versionNo}/task-policies
POST          /processes/{processDefId}/versions/{versionNo}/validate
POST          /processes/{processDefId}/versions/{versionNo}/publish
POST          /assignment-resolver/preview
```

## 7. Request／Task

目前已實作的最小正式起單 API：

```text
POST   /api/fm/requests/start/tenants
POST   /api/fm/requests/start/catalog
POST   /api/fm/requests/start/load
POST   /api/fm/requests/submit
POST   /api/fm/requests/tasks/inbox
POST   /api/fm/requests/tasks/load
POST   /api/fm/requests/tasks/action
POST   /api/fm/requests/tasks/transfer-options
POST   /api/fm/requests/tasks/transfer
POST   /api/fm/requests/tasks/delegate
POST   /api/fm/requests/tasks/resolve
POST   /api/fm/requests/mine
POST   /api/fm/requests/mine/load
POST   /api/fm/requests/mine/withdraw
POST   /api/fm/requests/mine/cancel
```

- `start/tenants` 依 Security Context 回傳登入者有效且啟用的 Tenant membership，不接受帳號參數。
- `start/catalog` 以 `X-FlowMint-Tenant` header 與 body 的 `applicantAccount`，回傳同時通過 Tenant membership、在職、代起單、起單政策、已發布版本及表單綁定檢查的流程。
- `start/catalog`、`start/load` 與 `submit` 必須以 `X-FlowMint-Tenant` header 指定 Tenant，body 不接受 `tenantId`。
- `submit` 必須提供 `Idempotency-Key` header；相同 key 與相同內容重送時回傳原流程結果，相同 key 夾帶不同內容時拒絕。
- 發起人一律由 Security Context 取得；body 只能指定申請人，並仍須通過代起單授權。
- 未提供流程實例刪除 API。
- `tasks/inbox` 只回傳登入者為 assignee 或 candidate 的有效待辦；Tenant 與登入帳號不可由 body 覆寫。
- `tasks/load` 回傳唯讀 Form.io 表單、送單資料、節點政策、合法退回目標及歷次稽核動作，並重新檢查 Task 權限。
- `tasks/action` 首版接受 `APPROVE`、`RETURN`、`REJECT`。退回目標必須是同一流程已完成的前置 User Task；駁回會終止 Flowable instance；所有動作都建立不可變表單快照與 `fm_task_action`。
- `tasks/transfer-options` 只對目前可處理且節點允許轉派的 Task 回傳同 Tenant 有效員工；`tasks/transfer` 會再次檢查 Task 權限、`ALLOW_TRANSFER`、目標員工與 Tenant membership，移除原候選人並設定新 assignee，同時建立 `TRANSFER` Action 與新的 Assignment Snapshot。
- `tasks/delegate` 只能使用既有且有效的期間代理授權，透過 Flowable `delegateTask` 保留原 owner；`tasks/resolve` 只允許目前代理人回覆待處理的代理工作，透過 `resolveTask` 將 Task 還給 owner。代理中的 Task 不可直接核准、退回、駁回或重送。
- `mine` 回傳登入者本人申請或由登入者代發起的流程，包含狀態與目前 User Task 名稱。
- `mine/load` 僅允許表單 Owner 或實際發起人查看，回傳完整 Action 軌跡及各次不可變表單快照。
- `mine/withdraw` 僅允許表單 Owner 撤回 `RUNNING` 流程，原因必填；成功時終止 Flowable instance，將流程與表單狀態轉為 `CANCELLED`，並建立不可變 `WITHDRAW` 快照與 Action。
- `mine/cancel` 僅允許流程的實際發起人取消 `RUNNING` 流程，主要用於代申請；成功時同樣轉為 `CANCELLED`，但稽核 Action 明確記為 `CANCEL`，不與申請人的 `WITHDRAW` 混用。

```text
GET    /request-catalog
POST   /requests
GET    /requests/mine
GET    /requests/{businessKey}
POST   /requests/{businessKey}/submit
POST   /requests/{businessKey}/resubmit
POST   /requests/{businessKey}/withdraw

GET    /tasks/inbox
GET    /tasks/{taskId}
POST   /tasks/{taskId}/approve
POST   /tasks/{taskId}/reject
POST   /tasks/{taskId}/return
POST   /tasks/{taskId}/transfer
POST   /tasks/{taskId}/delegate
POST   /tasks/{taskId}/resolve
POST   /tasks/{taskId}/add-sign
```

## 8. Operations

```text
GET    /operations/process-instances
GET    /operations/process-instances/{id}
GET    /operations/incidents
POST   /operations/incidents/{id}/retry
POST   /operations/tasks/{taskId}/reassign
POST   /operations/process-instances/{id}/terminate
GET    /operations/audit
```

高風險 POST 必須包含 reason 及 idempotency key。

## 9. HTTP

- Query success：200。
- Create：201。
- Mutation：200；非同步才用 202。
- Validation：400。
- Unauthenticated：401。
- Forbidden：403。
- Not found／跨 Tenant：404。
- State or optimistic conflict：409。
- Unprocessable assignment incident：422。

