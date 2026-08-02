-- FlowMint - 每個 User Task 僅允許綁定一張 Form
-- 套用前先確認既有資料沒有同一 User Task 綁定多張表單，否則新增唯一鍵會失敗。

SELECT TENANT_ID,
       PROCESS_DEF_ID,
       PROCESS_VERSION_NO,
       TASK_DEF_KEY,
       COUNT(*) AS FORM_COUNT
  FROM fm_task_form_rule
 GROUP BY TENANT_ID,
          PROCESS_DEF_ID,
          PROCESS_VERSION_NO,
          TASK_DEF_KEY
HAVING COUNT(*) > 1;

ALTER TABLE fm_task_form_rule
  DROP INDEX UK_FM_TFR_TASK,
  ADD UNIQUE KEY UK_FM_TFR_TASK (
      TENANT_ID,
      PROCESS_DEF_ID,
      PROCESS_VERSION_NO,
      TASK_DEF_KEY
  );
