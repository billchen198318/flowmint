-- Apply before deploying code that reads fm_data_action_version.ACTION_CONFIG.
ALTER TABLE fm_data_action_version ADD COLUMN ACTION_CONFIG LONGTEXT NULL AFTER CONTENT_SHA256;

-- Existing versions lack historical configuration. This captures the current
-- Action settings as a best-effort baseline; earlier drift cannot be recovered.
UPDATE fm_data_action_version v
JOIN fm_data_action a ON a.TENANT_ID = v.TENANT_ID AND a.ACTION_ID = v.ACTION_ID
SET v.ACTION_CONFIG = JSON_OBJECT(
    'actionName', a.ACTION_NAME,
    'poolId', a.POOL_ID,
    'actionType', a.ACTION_TYPE,
    'requestSchema', a.REQUEST_SCHEMA,
    'responseMode', a.RESPONSE_MODE,
    'status', CASE WHEN v.VERSION_STATUS = 'PUBLISHED' AND a.STATUS = 'DRAFT'
                   THEN 'ACTIVE' ELSE a.STATUS END,
    'rateLimitPerMinute', a.RATE_LIMIT_PER_MINUTE,
    'description', a.DESCRIPTION)
WHERE v.ACTION_CONFIG IS NULL;
