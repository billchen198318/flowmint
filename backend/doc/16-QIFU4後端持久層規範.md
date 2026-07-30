# 16 QIFU4 後端持久層實作規範

本文件由既有開發規範中可沿用的 Entity、Mapper、Service 與 Logic 強制規則整理而來；資料模型、表清單、字元集與 DDL 一律以 newdoc 新規格為準。

本節為 `fm_*` Java 持久化層的強制規則與 Review Checklist；若與前述範例、規劃文字或個人推論不同，以本節與 QIFU4 現有程式為準。

### 16.0 唯一實作依據

建立或修改 FlowMint Entity／Mapper 前，必須先直接讀取並比對以下 QIFU4 原始碼，不得只依文件摘要自行推論：

```text
Entity 參考：
backend/core/src/main/java/org/qifu/core/entity/*.java

Mapper interface 參考：
backend/core/src/main/java/org/qifu/core/mapper/*.java

Mapper XML 參考：
backend/core/src/main/resources/org/qifu/core/mapper/*.xml
```

截至 2026-07-27 的實際盤點結果：

- `core/mapper` 共有 28 個 Java interface；扣除 `DB1Config` 後，27 個業務 Mapper 全部標示 `@Mapper` 並繼承 `IBaseMapper<Entity, String>`。
- 對應的 27 個業務 Mapper XML 全部提供 `selectByPrimaryKey`、`baseConditions`、`selectListByParams`、`findPage`、`count`、`insert`、`update`、`delete`。
- 額外業務查詢或 command 必須加在上述標準骨架之後，不得取代標準骨架。
- 不得自行創造「Audit、Snapshot、Task Action 或 Process Instance 不需繼承 `IBaseMapper`」等例外。若未來確實需要例外，必須先修改本文件並記錄理由，再修改程式。

FlowMint 檔案固定放置位置：

```text
Entity:
backend/app/src/main/java/org/qifu/fm/entity/*.java

Mapper interface:
backend/app/src/main/java/org/qifu/fm/mapper/*.java

Mapper XML:
backend/app/src/main/resources/org/qifu/fm/mapper/*.xml
```

Mapper XML 不得放在 `src/main/java`；即使 Maven resource 設定能載入，也不視為符合專案結構。

### 16.1 Entity

1. Entity 必須實作 `java.io.Serializable`，並宣告 `serialVersionUID`。
2. `OID` getter 必須標示 `@EntityPK(name = "oid", autoUUID = true)`。
3. 一般含稽核欄位的 Entity 必須標示：
   - `CUSERID`：`@CreateUserField(name = "cuserid")`
   - `CDATE`：`@CreateDateField(name = "cdate")`
   - `UUSERID`：`@UpdateUserField(name = "uuserid")`
   - `UDATE`：`@UpdateDateField(name = "udate")`
4. 僅含建立稽核欄位的 append-only 表，只標示 Create annotations，不得虛構 Update 欄位。
5. Java 欄位、getter/setter、Mapper property 必須使用 camelCase，且逐欄宣告，不合併多個欄位。
6. DDL 的 Unique Key 是否標示 `@EntityUK`，須先確認 QIFU4 對多組複合 UK 的支援；未確認前以 DDL Unique Key 為正式約束，不任意加入可能產生錯誤語意的 annotation。

### 16.2 Mapper 分類

依 QIFU4 現有 Mapper 的一致作法，所有有 Entity 的 `fm_*` Mapper 均繼承 `IBaseMapper<Entity, String>`，並完整提供標準 statement；業務限制由 Service 層與專用 method 控制：

| 類型 | Mapper 規則 | Update/Delete |
| --- | --- | --- |
| 主檔與一般 CRUD 表 | 繼承 `IBaseMapper<Entity, String>`，XML 完整提供介面 statement | 依業務權限提供 |
| Process instance、Task action、Snapshot、Audit、Inbox/Outbox 等操作紀錄表 | 仍繼承 `IBaseMapper` 以符合 QIFU4 基礎設施，另加專用 Mapper 方法 | Service/API 禁止一般 Delete；Update 僅由明確 use case 呼叫 |

`fm_process_instance` 屬流程生命週期紀錄；Mapper 為 QIFU4 相容性保留標準 statement，但 Service/API 只能以明確 command 更新狀態及結束時間，不得暴露一般 delete。
`fm_task_action` 屬 append-only action ledger；Mapper 為 QIFU4 相容性保留標準 statement，但 Service/API 只允許 insert 與查詢，不得呼叫一般 update/delete。

### 16.3 Mapper XML

1. Namespace 必須是 Mapper interface 的完整 package。
2. 每個欄位必須明確定義 `column`、`jdbcType` 與 `property`。
3. 所有表名必須透過 schema include：

```xml
<include refid="org.qifu.core.mapper.DB1Config.schema"/>fm_process_instance
```

4. `TENANT_ID` 表的業務查詢必須由後端帶入 tenant 條件；禁止只依跨租戶可能重複的業務鍵查詢。
5. 寫入參數必須標示 `jdbcType`；nullable 欄位尤其不得省略。
6. 每個業務 Mapper XML 都必須先完整實作 `IBaseMapper` 的八個標準區塊；專用方法再追加於標準 `delete` statement 之後。
7. 為符合 QIFU4 `IBaseMapper`，XML 保留標準 `update`／`delete` statement；Snapshot、Audit 與 Action Ledger 的 Service/API 不得呼叫它們。
8. 專用狀態更新必須同時限制目前狀態，例如 `PROCESS_STATUS = 'RUNNING'`，避免重複完成或非法跳轉。

### 16.4 Java 與 XML 格式

1. Mapper Java 必須沿用 core Mapper 的 copyright header、import 分組、`@Mapper`、tab 縮排與空行配置。
2. Mapper interface 基本形式固定如下：

```java
@Mapper
public interface FmExampleMapper extends IBaseMapper<FmExample, String> {

	public List<FmExample> findSomething(Map<String, Object> paramMap);

}
```

3. 多條件專用方法優先使用 `Map<String, Object> paramMap`，與 core 現有 custom Mapper 一致；不得任意混用未驗證的參數綁定方式。
4. XML 必須沿用 core Mapper 的多行排版；`<if>` 的欄位或參數內容不得壓成單行。
5. Dynamic insert 使用兩個 `<trim>`；dynamic update 使用 `<set>`；不得以超長單行 SQL 取代。
6. `findPage` 必須使用 `DB1Config.sql1` 與 `DB1Config.sql2`；一般 list 排序沿用 `orderBy`／`sortType` 寫法。
7. Java 與 XML 均使用 UTF-8 without BOM。

### 16.5 Review Checklist

產生或修改 Entity／Mapper 時至少確認：

- [ ] 已實際讀取 core Entity、Mapper Java 與 Mapper XML，不只閱讀本文件。
- [ ] Java/XML 位於第 16.0 節指定的正確目錄。
- [ ] Mapper Java 有 core header、@Mapper 並繼承 IBaseMapper<Entity, String>。
- [ ] XML 具備八個標準區塊，專用方法位於標準骨架之後。

- [ ] Entity 可序列化，PK 與 Audit annotations 完整。
- [ ] Table／Column 大小寫符合規範。
- [ ] XML 使用 `DB1Config.schema`。
- [ ] 所有 nullable 參數具有正確 `jdbcType`。
- [ ] Tenant 查詢包含 `TENANT_ID`。
- [ ] Mapper 完整繼承並實作 `IBaseMapper`；append-only 表未由 Service/API 暴露 update/delete。
- [ ] Interface 方法與 XML statement 一一對應。
- [ ] 編譯成功，並以 Spring Context 載入 Mapper XML 執行整合測試。
### 16.6 Service 與 Logic 配套規則

建立 Entity 與 Mapper 後，必須同步建立 QIFU4 Service 層，不得讓 Logic、Controller 或 API 直接操作 Mapper：

```text
IFm{Domain}Service extends IBaseService<Fm{Domain}, String>
Fm{Domain}ServiceImpl extends BaseService<Fm{Domain}, String>
```

Service implementation 必須：

- 標示 `@Service`。
- 使用 `@Transactional(propagation=Propagation.REQUIRED, timeout=300, readOnly=true)` 作為預設交易設定。
- 注入對應 Mapper。
- 實作 `getBaseMapper()`。
- 單表專用查詢或狀態更新包裝成 Service method，不得讓 Logic 直接取得 Mapper。

跨表或跨系統 use case 必須建立 `logic`／`logic.impl`：

- Logic 只能依賴 Service、Resolver 與 Flowable adapter/service，不得直接依賴 Mapper。
- `submit`、`approve`、`publish`、`return`、`delegate` 等涉及多表或 Flowable 的命令，其 `@Transactional` 邊界位於 Logic implementation。
- API facade 或相容 Service 只能委派 Logic，不得包含 SQL、Mapper、Flowable engine 操作或跨表交易。
- 新增 Mapper 時，Review 必須同時確認對應 Service 是否存在；缺少 Service 視為未完成。
