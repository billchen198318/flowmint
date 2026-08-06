-- FlowMint FM_PROG005D0001 Demo 001
-- Creates a DRAFT Form.io form that calls the published
-- FM_GET_CURRENT_EMPLOYEE Data Action for tenant A01.
-- This script is idempotent and never overwrites an existing form.

SET NAMES utf8mb4;
START TRANSACTION;

SET @demo_tenant_id = 'A01';
SET @demo_form_oid = 'f5000001-0001-4000-8000-000000000001';
SET @demo_form_id = 'f5000001-0001-4000-8000-000000000002';
SET @demo_version_oid = 'f5000001-0001-4000-8000-000000000003';
SET @demo_form_code = 'FLOWMINT_DATA_ACTION_DEMO';

SET @demo_schema = '{"display":"form","components":[{"type":"content","key":"demoIntroduction","html":"<div><strong>FlowMint Data Action Demo</strong><br>按下按鈕後，表單會呼叫已發布的 <code>FM_GET_CURRENT_EMPLOYEE</code>，並將目前登入者的員工資料回填至下方欄位。</div>","input":false},{"type":"button","key":"loadCurrentEmployee","label":"讀取目前登入者","action":"event","event":"load-current-employee","theme":"primary","block":false,"input":true},{"type":"columns","key":"employeeColumns","columns":[{"width":6,"size":"md","components":[{"type":"textfield","key":"employeeId","label":"員工 ID","input":true,"disabled":true},{"type":"textfield","key":"employeeNo","label":"員工編號","input":true,"disabled":true},{"type":"textfield","key":"account","label":"登入帳號","input":true,"disabled":true},{"type":"textfield","key":"displayName","label":"顯示名稱","input":true,"disabled":true}]},{"width":6,"size":"md","components":[{"type":"email","key":"email","label":"Email","input":true,"disabled":true},{"type":"textfield","key":"locale","label":"語系","input":true,"disabled":true},{"type":"textfield","key":"timezone","label":"時區","input":true,"disabled":true},{"type":"textfield","key":"dataActionStatus","label":"執行狀態","input":true,"disabled":true,"defaultValue":"READY"}]}],"input":false},{"type":"textarea","key":"dataActionError","label":"錯誤訊息","input":true,"disabled":true,"rows":3}]}';

SET @demo_ui_schema = '{"engine":"FORMIO","version":1,"dataActions":[{"bindingId":"load-current-employee","event":"load-current-employee","actionCode":"FM_GET_CURRENT_EMPLOYEE","actionVersion":1,"requestMapping":{},"responseMapping":{"employee.employeeId":"employeeId","employee.employeeNo":"employeeNo","employee.account":"account","employee.displayName":"displayName","employee.email":"email","employee.locale":"locale","employee.timezone":"timezone"},"statusTarget":"dataActionStatus","errorTarget":"dataActionError"}]}';

-- The prerequisites intentionally gate creation. If either prerequisite is
-- missing, both INSERT statements are no-ops and the diagnostic SELECT below
-- reports PREREQUISITE_MISSING.
INSERT INTO fm_form_def
    (OID, TENANT_ID, FORM_ID, FORM_CODE, FORM_NAME,
     CURRENT_VERSION_NO, STATUS, DESCRIPTION, CUSERID, CDATE)
SELECT
    @demo_form_oid,
    @demo_tenant_id,
    @demo_form_id,
    @demo_form_code,
    'Data Action Demo－目前登入者資料',
    1,
    'DRAFT',
    '示範 Form.io 呼叫 FlowMint Data Action 並回填欄位',
    'SYSTEM',
    NOW(3)
WHERE EXISTS (
    SELECT 1
      FROM fm_tenant
     WHERE TENANT_ID = @demo_tenant_id
       AND STATUS = 'ACTIVE'
)
AND EXISTS (
    SELECT 1
      FROM fm_data_action action
      JOIN fm_data_action_version version
        ON version.TENANT_ID = action.TENANT_ID
       AND version.ACTION_ID = action.ACTION_ID
       AND version.VERSION_NO = 1
       AND version.VERSION_STATUS = 'PUBLISHED'
     WHERE action.TENANT_ID = @demo_tenant_id
       AND action.ACTION_CODE = 'FM_GET_CURRENT_EMPLOYEE'
       AND action.STATUS = 'ACTIVE'
)
AND NOT EXISTS (
    SELECT 1
      FROM fm_form_def
     WHERE TENANT_ID = @demo_tenant_id
       AND FORM_CODE = @demo_form_code
);

INSERT INTO fm_form_version
    (OID, TENANT_ID, FORM_ID, VERSION_NO, VERSION_STATUS,
     SCHEMA_CONTENT, UI_SCHEMA_CONTENT, CONTENT_SHA256, CUSERID, CDATE)
SELECT
    @demo_version_oid,
    @demo_tenant_id,
    @demo_form_id,
    1,
    'DRAFT',
    @demo_schema,
    @demo_ui_schema,
    SHA2(CONCAT(@demo_schema, CHAR(10), @demo_ui_schema), 256),
    'SYSTEM',
    NOW(3)
WHERE EXISTS (
    SELECT 1
      FROM fm_form_def
     WHERE OID = @demo_form_oid
       AND TENANT_ID = @demo_tenant_id
       AND FORM_ID = @demo_form_id
       AND FORM_CODE = @demo_form_code
)
AND NOT EXISTS (
    SELECT 1
      FROM fm_form_version
     WHERE TENANT_ID = @demo_tenant_id
       AND FORM_ID = @demo_form_id
       AND VERSION_NO = 1
);

COMMIT;

SELECT
    CASE
        WHEN EXISTS (
            SELECT 1
              FROM fm_form_def
             WHERE TENANT_ID = @demo_tenant_id
               AND FORM_CODE = @demo_form_code
        ) THEN 'READY'
        ELSE 'PREREQUISITE_MISSING'
    END AS DEMO_STATUS,
    @demo_tenant_id AS TENANT_ID,
    @demo_form_code AS FORM_CODE,
    'FM_GET_CURRENT_EMPLOYEE' AS ACTION_CODE;
