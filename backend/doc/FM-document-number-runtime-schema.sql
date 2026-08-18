-- FlowMint 共用單據編號 Runtime schema 與 A01 請購規則。
-- 執行前須確認 fm_document_number_rule 與 fm_document_sequence 已存在。

ALTER TABLE fm_process_def
  ADD COLUMN DOCUMENT_TYPE varchar(50) NULL AFTER CATEGORY,
  ADD KEY IDX_FM_PD_DOCUMENT_TYPE (TENANT_ID, DOCUMENT_TYPE);

ALTER TABLE fm_form_data
  ADD COLUMN IDEMPOTENCY_KEY varchar(100) NULL AFTER BUSINESS_KEY,
  ADD UNIQUE KEY UK_FM_FORM_DATA_IDEMPOTENCY (TENANT_ID, IDEMPOTENCY_KEY);

START TRANSACTION;

INSERT INTO fm_document_number_rule
    (OID, TENANT_ID, DOCUMENT_TYPE, PREFIX, PERIOD_TYPE, SEQUENCE_LENGTH,
     FORMAT_PATTERN, STATUS, CUSERID, CDATE)
SELECT UUID(), 'A01', 'PURCHASE_REQUEST', 'PR', 'MONTH', 6,
       '{PREFIX}-{TENANT}-{YYYY}{MM}-{SEQ}', 'ACTIVE', 'SYSTEM', NOW(3)
 WHERE NOT EXISTS (
       SELECT 1
         FROM fm_document_number_rule
        WHERE TENANT_ID = 'A01'
          AND DOCUMENT_TYPE = 'PURCHASE_REQUEST'
 );

SET @inserted_rules = ROW_COUNT();

UPDATE fm_process_def
   SET DOCUMENT_TYPE = 'PURCHASE_REQUEST',
       UUSERID = 'SYSTEM',
       UDATE = NOW(3)
 WHERE TENANT_ID = 'A01'
   AND PROCESS_KEY = 'FM_PURCHASE_APPROVAL'
   AND DOCUMENT_TYPE IS NULL;

SET @updated_processes = ROW_COUNT();

SELECT @inserted_rules AS inserted_rules,
       @updated_processes AS updated_processes;

COMMIT;
