-- FM_PURCHASE_REQUEST Version 1：新增唯讀請購單編號欄位。
-- 目標固定為 A01 Tenant 的指定 DRAFT 與更新前 SHA-256，避免覆寫其他版本。

START TRANSACTION;

SET @purchase_form_id = '2d939f65-b78e-454d-a48d-cdd538222d96';
SET @expected_sha256 = 'd380ab66d46a60c8e2e533a35c7c9374c6937f765b6beea44ddf44d1aeadfb97';

UPDATE fm_form_version v
SET v.SCHEMA_CONTENT = JSON_ARRAY_INSERT(
        v.SCHEMA_CONTENT,
        '$.components[1].components[0]',
        JSON_OBJECT(
            'type', 'textfield',
            'key', 'documentNumber',
            'label', '請購單編號',
            'description', '正式送出後由系統自動產生，不可自行修改。',
            'placeholder', '送出後由系統產生',
            'input', TRUE,
            'disabled', TRUE,
            'persistent', FALSE,
            'validate', JSON_OBJECT('maxLength', 100)
        )
    ),
    v.UUSERID = 'SYSTEM',
    v.UDATE = NOW(3)
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND v.VERSION_STATUS = 'DRAFT'
  AND v.CONTENT_SHA256 = @expected_sha256
  AND JSON_SEARCH(
        v.SCHEMA_CONTENT,
        'one',
        'documentNumber',
        NULL,
        '$**.key'
      ) IS NULL;

SET @updated_rows = ROW_COUNT();

UPDATE fm_form_version v
SET v.CONTENT_SHA256 = SHA2(CONCAT(
        v.SCHEMA_CONTENT,
        CHAR(10),
        COALESCE(v.UI_SCHEMA_CONTENT, ''),
        CHAR(10),
        COALESCE(v.CUSTOM_SCRIPT_CONTENT, '')
    ), 256)
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND @updated_rows = 1;

SELECT @updated_rows AS updated_rows,
       JSON_VALID(v.SCHEMA_CONTENT) AS schema_valid,
       JSON_SEARCH(
           v.SCHEMA_CONTENT,
           'one',
           'documentNumber',
           NULL,
           '$**.key'
       ) AS purchase_number_path,
       JSON_UNQUOTE(JSON_EXTRACT(
           v.SCHEMA_CONTENT,
           JSON_UNQUOTE(JSON_SEARCH(
               v.SCHEMA_CONTENT,
               'one',
               'documentNumber',
               NULL,
               '$**.key'
           ))
       )) AS purchase_number_key,
       v.CONTENT_SHA256
  FROM fm_form_version v
 WHERE v.TENANT_ID = 'A01'
   AND v.FORM_ID = @purchase_form_id
   AND v.VERSION_NO = 1;

COMMIT;
