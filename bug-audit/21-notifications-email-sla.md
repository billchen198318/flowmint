## 21 站內通知、Email 與 Task SLA — 靜態／唯讀盤查完成

2026-10-05 收尾：已核對通知 Controller 的 inbox／read／read-all、指派通知、Email Outbox／寄送及同步排程、SLA 到期掃描，以及現有通知／郵件／SLA 單元測試。既有差異見 BUG-026／146／147／153；重入去重未發現獨立根因。現有流程實例不足以重現大量任務分頁、跨月待寄及真實郵件失敗，這些維持驗證限制。

2026-10-04 續查 SLA 排程分頁與任務完成時的競態。MariaDB 目前沒有流程實例可建立實際到期任務，以下 BUG-153 僅依 Flowable live Task 查詢路徑推論，尚未執行超過 500 筆任務的重現。

2026-10-03 續查 `FmTaskAssignmentListener.notify()`、`FmNotificationPublisher.taskAssigned()` 與 `FmNotificationMailOutbox.enqueue()`：多人簽核的通知收件人與單一 Task 受派者不一致已記於 BUG-146。Listener 重入時，站內通知依 Tenant／事件／Task／收件人產生固定 ID，Email 通知再由站內通知 ID 產生固定 ID，兩者均經 `insertIfAbsent()`；目前未發現同一事件重入會新增重複通知的獨立缺陷。續查 `FmTaskDeadlineNotificationJob`：排程以 Flowable Task 的實際 assignee／candidate 作為到期通知收件人；平行加簽獨立 Task 會回查父 Task 的流程變數與批次 Task Key，且有既有單元測試。提前提醒 0 小時在排程條件下無法觸發，記於 BUG-147。SLA 掃描的實際資料量、任務同時完成時的分頁行為及排程故障仍未驗證。續查 `SendMailHelperJob.process()`／`handleSuccess()`／`handleFailure()` 與 `FmNotificationDeliverySyncJob.synchronizeOne()`：失敗次數及下次重試由寄送排程更新，成功後由寄送排程或同步排程標記 Email 通知 SENT；固定 ID 的重入去重仍成立。跨月待寄無法被選取已記於 BUG-026；本輪未確認新的獨立缺陷。尚未執行實際 Flowable 任務、通知或 Email 重試驗證。

### BUG-146：多人簽核的每個 Task 通知整組人員，包含目前無權處理者

- 嚴重度：中。性質：多人簽核通知收件人；未實際建立 ALL／SEQUENTIAL Task 或寄送通知。
- 預期：Task 指派通知只送給該 Task 的目前 assignee 或 candidate；`README.md` 第 777 行明定此契約。`SEQUENTIAL` 模式應在輪到各成員時通知該成員，不應把某一人的 Task 通知其他成員。
- 實際：`FmTaskAssignmentListener.notify()` 對 `ALL`／`SEQUENTIAL` 先由 `applyAssignment()` 將當前 `flowmintAssignee` 設為該 Task 的單一 assignee，接著卻把 `resolveAssignment()` 回傳的整組 `accounts` 傳給 `notificationPublisher.taskAssigned()`。後者逐一為每個帳號建立站內通知並排入 Email Outbox，通知的 Task ID 仍是當前單一 assignee 的 Task。因此平行多人簽核的每個 Task 都會通知整組成員；循序簽核亦會提前通知尚未輪到的人，且可能通知已完成該節點簽核的人。收件者可能看到自己沒有權限處理的 Task 通知。
- 證據：`backend/app/src/main/java/org/qifu/fm/flowable/FmTaskAssignmentListener.java` 的 `notify()`／`applyAssignment()`；`backend/app/src/main/java/org/qifu/fm/domain/notification/FmNotificationPublisher.java` 的 `taskAssigned()`；`README.md` 第 513～514、777 行。此為靜態呼叫路徑推論，未建立流程實例或寄送郵件。

### BUG-147：提前提醒設為 0 小時時永遠不會產生即將到期通知

- 嚴重度：中。性質：Task SLA 設定與排程條件不一致；未建立 0 小時設定的實際 Task 重現。
- 預期：既然流程設計、後端與資料庫允許 `reminderBeforeHours=0`，系統應明確定義並執行到期當下的提醒，或在設定時拒絕此值；不能接受後卻永遠不產生對應通知。
- 實際：`FmProcessDefLogicServiceImpl.validSla()` 接受提醒時數大於等於 0 且小於期限，資料庫 `CK_FM_TP_REMINDER_HOURS` 也接受 0。`FmTaskDeadlineNotificationJob.notifyTask()` 只有在 `dueDate > now` 時才考慮 `TASK_DUE_SOON`，同時要求 `dueDate - reminderBeforeHours <= now`。當提醒時數為 0，兩條件分別變成 `dueDate > now` 與 `dueDate <= now`，不可能同時成立；到期後則只送 `TASK_OVERDUE`。因此 0 小時的提前提醒設定沒有可觸發的時點。
- 證據：`FmProcessDefLogicServiceImpl.validSla()`；`FmTaskDeadlineNotificationJob.notifyTask()`；`backend/doc/flowmint.sql` 的 `CK_FM_TP_REMINDER_HOURS`；`README.md` 第 553～554 行。此為條件式靜態推論，未執行排程或修改資料。

### BUG-153：SLA 掃描分頁期間任務完成，可能跳過後續到期任務

- 嚴重度：中。性質：Java 排程分頁競態；需要超過 500 筆符合條件的 Task 並在掃描中改變查詢結果，目前尚無資料重現。
- 預期：`README.md` 的 Task SLA 排程應掃描所有已進入提醒或逾時區間的 active Task；任務同時完成不應使其他仍有效的 Task 漏掉本輪提醒。
- 實際：`FmTaskDeadlineNotificationJob.notifyDeadlines()` 每頁重新執行 `taskDueBefore(horizon).orderByTaskDueDate().asc().listPage(offset, 500)`，處理後將 offset 加上該頁筆數。若已掃描的任務在下一頁查詢前完成並從 live Task 查詢結果移除，後續資料索引前移，下一次固定 offset 會跨過尚未掃描的 Task。本輪漏掉的 Task 需等下一次五分鐘排程；若在此期間到期，可能失去 `TASK_DUE_SOON` 通知，只收到逾時通知。
- 證據：`backend/app/src/main/java/org/qifu/fm/domain/notification/FmTaskDeadlineNotificationJob.java` 的 `notifyDeadlines()` 與 `notifyTask()`；排程以 `fixedDelay = 300000` 執行。這是 live 查詢加 offset 分頁的條件推論，尚未做並發或大資料量測試。

### BUG-026：跨月後上月待寄 Email 不再被寄送排程選取

- 嚴重度：中。性質：Java 寄送排程與 SQL 篩選；目前無待寄資料可實際重現。
- 預期：失敗後等待重試的 Email，即使跨過月份，也應在 `NEXT_RETRY_DATE` 到達後繼續嘗試，直至成功或達到永久失敗次數；`README.md` 第 23 節要求延遲重試與永久 `FAILED` 狀態同步。
- 實際：`SendMailHelperJob.execute()` 每次只產生當月 `yyyyMM%` 前綴，`FIND_ELIGIBLE_MAIL_SQL` 以 `m.MAIL_ID LIKE :mailIdPrefix` 過濾。`FmNotificationMailOutbox.enqueue()` 用建立當日生成的 `MAIL_ID`。月底建立而寄送失敗的郵件，到下月即使重試時間已到，仍因上月 `MAIL_ID` 前綴被排除，永久停在待寄／`PENDING`，除非另有人工處理。
- 證據：`backend/app/src/main/java/org/qifu/core/scheduled/SendMailHelperJob.java` 的 `FIND_ELIGIBLE_MAIL_SQL`、`execute()`；`backend/app/src/main/java/org/qifu/fm/domain/notification/FmNotificationMailOutbox.java` 的 `enqueue()`。MariaDB 唯讀查詢顯示 `tb_sys_mail_helper` 未寄 0 筆、`fm_notification` 的 Email `PENDING` 0 筆，無現成跨月案例。
