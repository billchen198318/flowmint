-- Allow unauthenticated external API attempts to be audited before a client is known.
ALTER TABLE fm_api_access_log
    MODIFY COLUMN TENANT_ID varchar(36) NULL,
    MODIFY COLUMN CLIENT_ID char(36) NULL;
