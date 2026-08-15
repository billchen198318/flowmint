-- 將既有請購表單的專用 purchaseRequestNo 欄位改為平台共用 documentNumber。
-- 執行前須依 MariaDB 存取規範確認 UPDATE，並先以 transaction rollback 試跑。
UPDATE fm_form_version v
JOIN fm_form_def d
  ON d.TENANT_ID = v.TENANT_ID
 AND d.FORM_ID = v.FORM_ID
SET v.SCHEMA_CONTENT = JSON_REPLACE(
      v.SCHEMA_CONTENT,
      JSON_UNQUOTE(JSON_SEARCH(
        v.SCHEMA_CONTENT, 'one', 'purchaseRequestNo', NULL, '$**.key'
      )),
      'documentNumber'
    ),
    v.UUSERID = 'SYSTEM',
    v.UDATE = NOW(3)
WHERE d.TENANT_ID = 'A01'
  AND d.FORM_CODE = 'FM_PURCHASE_REQUEST'
  AND v.VERSION_NO = 1
  AND v.VERSION_STATUS = 'DRAFT'
  AND JSON_SEARCH(
        v.SCHEMA_CONTENT, 'one', 'purchaseRequestNo', NULL, '$**.key'
      ) IS NOT NULL
  AND JSON_SEARCH(
        v.SCHEMA_CONTENT, 'one', 'documentNumber', NULL, '$**.key'
      ) IS NULL;

SET @document_number_key_updated_rows = ROW_COUNT();

UPDATE fm_form_version v
JOIN fm_form_def d
  ON d.TENANT_ID = v.TENANT_ID
 AND d.FORM_ID = v.FORM_ID
SET v.CONTENT_SHA256 = SHA2(CONCAT(
      v.SCHEMA_CONTENT,
      CHAR(10),
      COALESCE(v.UI_SCHEMA_CONTENT, ''),
      CHAR(10),
      COALESCE(v.CUSTOM_SCRIPT_CONTENT, '')
    ), 256)
WHERE d.TENANT_ID = 'A01'
  AND d.FORM_CODE = 'FM_PURCHASE_REQUEST'
  AND v.VERSION_NO = 1
  AND v.VERSION_STATUS = 'DRAFT'
  AND @document_number_key_updated_rows = 1
  AND JSON_SEARCH(
        v.SCHEMA_CONTENT, 'one', 'documentNumber', NULL, '$**.key'
      ) IS NOT NULL;
