# 21 MariaDB 操作規範

## 1. 連線資訊

| 項目 | 設定值 |
| --- | --- |
| Host | `127.0.0.1` |
| Port | `3306` |
| Account | `root` |
| Password | `password` |
| Database | `flowmint` |
| MariaDB CLI | `C:\Program Files\MariaDB 12.3\bin\mariadb.exe` |

預設操作 database／schema 為 `flowmint`。若當次操作要使用其他 database／schema，必須先取得 user 明確同意，不得自行切換。

PowerShell 執行時使用上述 CLI 絕對路徑；該目錄目前未加入系統 `PATH`。本機連線需加入 `--skip-ssl`，否則可能發生 Windows TLS credential 錯誤。

唯讀查詢命令範例：

```powershell
& 'C:\Program Files\MariaDB 12.3\bin\mariadb.exe' `
  --host=127.0.0.1 `
  --port=3306 `
  --user=root `
  --password=password `
  --skip-ssl `
  --database=flowmint `
  --batch `
  --raw `
  --execute="SELECT 1;"
```

## 2. 查詢與檢查

- 可使用 `SELECT`、`SHOW`、`DESCRIBE`、`EXPLAIN` 等唯讀指令盤點資料及資料庫結構。
- 唯讀查詢仍應限制查詢範圍，避免不必要地讀取或輸出大量資料。
- 執行異動前，應先使用唯讀查詢確認 database、資料表、條件及預計影響筆數。

## 3. 資料異動授權

執行下列 SQL 前，每一次都必須先向 user 說明預計執行的 SQL、目標 database／資料表、篩選條件及預計影響範圍，並取得 user 明確同意：

- `INSERT`
- `UPDATE`
- `DELETE`

未取得 user 明確同意前，只能進行唯讀盤點，不得執行上述資料異動。

即使 user 已同意先前的一次異動，也不得視為後續異動的永久授權；新的 SQL 或影響範圍改變時，必須重新詢問。

## 4. 安全要求

- `UPDATE`、`DELETE` 原則上必須包含明確的 `WHERE` 條件。
- 執行前應先以相同條件執行 `SELECT`，向 user 回報預計影響的資料及筆數。
- 可行時使用 transaction；異動後先驗證結果，再決定是否提交。
- 不得在未經 user 明確要求與同意時執行 DDL、清空資料、批次覆寫或其他破壞性操作。
- 回覆或操作紀錄中避免不必要地重複顯示密碼。

## 5. 適用範圍

本文件適用於後續針對上述本機 MariaDB 的操作。若 user 在當次對話提供不同連線資訊或更嚴格限制，以當次明確指示為準。
