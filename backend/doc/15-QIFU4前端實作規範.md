# FlowMint 前端實作強制規範

Last updated: 2026-07-29 Asia/Taipei

## 1. 規範地位

本文件是 FlowMint 在 `frontend-v-nx` 實作 Vue／Nuxt 頁面的強制規範。

新頁面、既有頁面重構及 Code Review 必須先比對 QIFU4 現有標準頁面，不得只做到功能可執行或 Production Build 通過。若本文件與早期規劃文件的前端作法不同，以本文件為準。

直接參考模板：

```text
frontend-v-nx/pages/prog001d0001/index.vue
frontend-v-nx/pages/prog001d0001/create.vue
frontend-v-nx/pages/prog001d0001/edit/[id].vue
frontend-v-nx/pages/prog001d0001/config.ts
frontend-v-nx/pages/prog001d0001/QueryPageStore.ts
```

FlowMint 頁面不得覆蓋或借用相同編號的 Core 頁面。FlowMint 使用獨立目錄：

```text
frontend-v-nx/pages/fm_prog001d0001/
```

## 2. 標準目錄

一般 Query／Create／Edit 程式至少包含：

```text
pages/fm_prog001d0001/
├── index.vue
├── create.vue
├── edit/
│   └── [id].vue
├── config.ts
└── QueryPageStore.ts
```

共用表單可放在該 Program 目錄內：

```text
components/TenantForm.vue
```

只有同一 Program 使用的元件不得提前放入全域 `components`。

## 3. `config.ts`

必須沿用 QIFU4 欄位名稱與大小寫：

```ts
export interface PageConfig {
    frontendNamespace: string;
    eventNamespace: string;
    QueryId: string;
    CreateId: string;
    EditId: string;
}

export const PageConstants: PageConfig = {
    frontendNamespace: '/fm_prog001d0001',
    eventNamespace: '/FM_PROG001D0001',
    QueryId: 'FM_PROG001D0001Q',
    CreateId: 'FM_PROG001D0001A',
    EditId: 'FM_PROG001D0001E'
};
```

禁止自行改成：

```text
queryId
createId
editId
apiBase
routeBase
```

## 4. Query Page

Query Page 必須使用：

```text
Toolbar.vue
HiddenQueryFieldAlertInfo.vue
GridPagination.vue
Grid.vue
QueryPageStore.ts
GridHelper.ts
BaseHelper.ts
```

必要結構：

1. `pageProgramId = PageConstants.QueryId`。
2. 查詢條件及 Grid 狀態保存在 Pinia `QueryPageStore`。
3. Grid 使用 `getGridConfig` 建立。
4. 換頁使用 `setConfigPage`。
5. 每頁筆數使用 `setConfigRow`。
6. 總筆數使用 `setConfigTotal`。
7. 返回 Query Page 時使用 `resetConfigByOld` 恢復查詢狀態。
8. Create／Edit URL 優先由 `getProgItem` 及 `getUrlPrefixFromProgItem` 取得。
9. HTTP Request 必須使用 `getAxiosInstance`，不可建立沒有 QIFU4 CSRF／Refresh Interceptor 的 Axios Instance。
10. 查詢 Request 使用 QIFU4 `field` 與 `pageOf` 格式。

禁止在一般維護 Query Page 自行實作：

```text
原生 HTML table
自訂上一頁／下一頁
另一套 Grid Config
另一套 Query Store
另一套 Axios Client
```

若標準 `Grid` 無法支援必要功能，應先擴充共用元件或記錄核准的例外，不得在單一頁面靜默建立另一套模式。

## 5. Create／Edit Page

Create／Edit Page 必須使用：

```text
Toolbar.vue
getAxiosInstance
checkInvalid
invalidFeedback
escapeQifuHtmlMsg
useSwalLoading
vue3-toastify
```

規則：

1. Create 使用 `PageConstants.CreateId`。
2. Edit 使用 `PageConstants.EditId`。
3. Toolbar 統一提供 Back、Refresh、Save。
4. Create／Edit 送出前必須執行前端欄位檢核；有錯誤時建立 `checkFields`、顯示提示並立即中止，不得送出 Request。
5. 後端仍必須使用 QIFU4 `CheckControllerFieldHandler` 驗證相同必要條件，不得只信任前端；驗證失敗時必須在進入 Logic／Transaction 前中止。
6. 後端 `checkFields` 必須回填至畫面，避免繞過前端或競態條件造成無欄位提示。
7. 欄位錯誤必須套用 `is-invalid`、`checkInvalid` 與 `invalidFeedback`。
8. 後端訊息顯示前使用 `escapeQifuHtmlMsg`。
9. Load 失敗返回 Query Program URL，不得留在無效 Edit Page。
10. 業務 ID 建立後不可修改時，Edit Page 必須設為 `readonly`。
11. Delete／Deactivate 等破壞性或狀態操作使用 `confirmFire`。
12. 不得由前端直接組合多個 CRUD Request 模擬應由後端 Transaction Logic 完成的 Use Case。

## 6. Embedded Child Data

只在主檔脈絡下有意義的子資料應放在 Edit Page 的 Embedded Section。

例如：

```text
Tenant
└── Tenant Account
```

Embedded Child Data：

- 清單優先使用標準 `Grid`。
- 新增／修改表單沿用 `checkFields` 驗證方式。
- Tenant Account 新增必須明確區分「建立新 FlowMint 登入帳號」與「加入既有帳號」。
- 新帳號模式必須要求初始密碼與確認密碼；密碼欄位使用 `type="password"`、不得回填、不得寫入前端 Store 或 Log。
- 新帳號送至整合 API，由後端同 Transaction 建立 `tb_account` 與 `fm_tenant_account`；既有帳號模式不得修改 `tb_account`。
- 狀態操作使用 `confirmFire`。
- 後端必須重新驗證 Parent、Tenant Scope、Account、狀態及有效期間。
- 子資料量大、需要獨立查詢／批次處理時，才建立 `setparam` 或獨立 Program。

## 7. Program ID 與 `tb_sys_prog`

有實際 Vue Page 的項目才建立 Page Program：

```text
FM_PROG001D0001Q  Query Page
FM_PROG001D0001A  Create Page
FM_PROG001D0001E  Edit Page
```

沒有獨立 UI Page 的 API Command 不建立 `tb_sys_prog` Page Item：

```text
FM_PROG001D0001C
FM_PROG001D0001U
FM_PROG001D0001D
```

Controller 仍可使用上述 Command ID 執行 API 權限檢查，但不得因為存在 Controller Method 就宣稱存在 UI。

Program Family Folder 是選單目錄，不是業務 Page，可依選單需要註冊：

```text
FM_PROG001D
```

不得直接修改正式資料庫或未經確認覆寫 `flowmint.sql`。需要註冊時提供明確 INSERT SQL，由部署／資料庫變更程序執行。

## 8. API、Cookie 與 CSRF

本機開發必須固定使用相同 Hostname：

```text
Frontend http://127.0.0.1:8077
Backend  http://127.0.0.1:8088
```

不得混用：

```text
localhost
127.0.0.1
```

所有 API Request 使用 `getAxiosInstance`，保留：

```text
withCredentials
XSRF-TOKEN Cookie
X-XSRF-TOKEN Header
Refresh Token Queue
401 處理
```

## 9. Review Checklist

每個 FlowMint UI 完成前必須確認：

- [ ] 使用 `fm_prog...` 獨立目錄，未覆蓋 Core Program。
- [ ] `config.ts` 使用 `QueryId/CreateId/EditId`。
- [ ] Query Page 使用 `Toolbar`。
- [ ] Query Page 使用 `HiddenQueryFieldAlertInfo`。
- [ ] Query Page 使用 `GridPagination`。
- [ ] Query Page 使用 `Grid`。
- [ ] Query Page 使用 Pinia `QueryPageStore`。
- [ ] Grid 狀態使用 `GridHelper`。
- [ ] API 使用 `getAxiosInstance`。
- [ ] Create／Edit 使用 `checkFields`。
- [ ] Create／Edit 使用 `checkInvalid` 與 `invalidFeedback`。
- [ ] 後端訊息使用 `escapeQifuHtmlMsg`。
- [ ] Loading 使用 `useSwalLoading`。
- [ ] 狀態操作使用 `confirmFire`。
- [ ] Child Data 符合 Embedded／Setparam 決策。
- [ ] 只有實際 UI Page 產生 `tb_sys_prog` Page Item。
- [ ] `npm run build` 通過。
- [ ] 瀏覽器以實際後端完成 Query／Create／Edit／Deactivate 驗證。

缺少任一適用項目，不得標記 UI 完成；必須記錄未完成項目或經核准的例外。

## 10. `FM_PROG001D0001` 基準

`FM_PROG001D0001` 是第一個 FlowMint QIFU4 前端基準：

```text
index.vue
    Toolbar + HiddenQueryFieldAlertInfo + GridPagination + Grid

create.vue
    Toolbar + TenantForm + checkFields

edit/[id].vue
    Toolbar + TenantForm + Tenant Account Embedded Grid + checkFields
```

後續 `FM_PROG001D0002` 及其他 FlowMint Program 必須先複製此結構，再依業務欄位調整。
