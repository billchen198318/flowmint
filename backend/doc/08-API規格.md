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
```

- `start/tenants` 依 Security Context 回傳登入者有效且啟用的 Tenant membership，不接受帳號參數。
- `start/catalog` 以 `X-FlowMint-Tenant` header 與 body 的 `applicantAccount`，回傳同時通過 Tenant membership、在職、代起單、起單政策、已發布版本及表單綁定檢查的流程。
- `start/catalog`、`start/load` 與 `submit` 必須以 `X-FlowMint-Tenant` header 指定 Tenant，body 不接受 `tenantId`。
- `submit` 必須提供 `Idempotency-Key` header；相同 key 與相同內容重送時回傳原流程結果，相同 key 夾帶不同內容時拒絕。
- 發起人一律由 Security Context 取得；body 只能指定申請人，並仍須通過代起單授權。
- 未提供流程實例刪除 API。

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

