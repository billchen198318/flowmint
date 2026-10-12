## 27 安全權限、部署設定與資料庫遷移 — 靜態／唯讀盤查完成

2026-10-05 收尾：已對照第 05／17／21／38 章的身分、Tenant 資料權限、MariaDB 操作與敏感設定規範，`Constants.TOKEN_SECRET`、JWT 簽發／驗證路徑、應用設定及 `backend/doc/flowmint.sql` 的單一 schema 交付方式。既有跨 Tenant 授權差異保留在各功能單元；本單元獨立觀察為 BUG-023。未讀取實際部署密鑰或執行部署／遷移，故無法判斷現場是否使用固定 JWT 預設值；此處完成限文件與原始碼盤查，不代表上線環境驗收完成。

### BUG-023：未設定 JWT 簽章密鑰時，服務會使用程式內固定預設值

- 狀態：已修正（2026-10-09）。`Constants` 移除固定預設值，缺少 `QIFU_JWT_SECRET` 或 UTF-8 長度不足 32 bytes 時拒絕啟動；實際部署仍須提供獨立密鑰。

- 嚴重度：高。性質：Java 安全設定；是否影響實際部署，取決於部署環境是否設定 `QIFU_JWT_SECRET`。
- 預期：啟動時必須取得部署專屬的強密鑰；若未提供，應拒絕啟動，避免不同環境共用可從原始碼讀出的簽章密鑰。
- 實際：`Constants.TOKEN_SECRET` 在環境變數缺失或空白時，會落到原始碼內的固定字串；`TokenBuilderUtils` 用它簽發 JWT，`WebSecurityConfig.jwtDecoder()` 用同一值驗證。程式沒有啟動失敗檢查。資料庫 token store 另有驗證，因此僅憑這個問題尚不能斷言可直接偽造新登入；但固定密鑰降低 JWT 簽章的保密性，部署失誤時會實際啟用該值。
- 證據：`backend/base/src/main/java/org/qifu/base/Constants.java` 的 `TOKEN_SECRET`；`backend/base/src/main/java/org/qifu/base/util/TokenBuilderUtils.java`；`backend/app/src/main/java/org/qifu/core/config/WebSecurityConfig.java` 的 `jwtDecoder()`。未讀取或揭露部署環境的實際密鑰。
