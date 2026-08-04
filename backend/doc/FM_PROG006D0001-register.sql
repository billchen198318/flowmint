-- FlowMint FM_PROG006D0001 - DataSource Pool 管理程式註冊
-- 僅註冊程式資料，目前不配置角色權限。
START TRANSACTION;

INSERT IGNORE INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
VALUES
    (UUID(), 'FM_PROG006D', 'FF. FlowMint 動態資料服務',
     '/', 'N', 'N', 0, 0, 'CORE', 'FOLDER', 'DATABASE', 'database',
     'admin', CURRENT_TIMESTAMP, NULL, NULL),
    (UUID(), 'FM_PROG006D0001Q', 'FF01 - DataSource Pool 管理',
     '#/fm_prog006d0001', 'N', 'N', 0, 0, 'CORE', 'ITEM', 'DATABASE', 'database',
     'admin', CURRENT_TIMESTAMP, NULL, NULL),
    (UUID(), 'FM_PROG006D0001A', 'FF01 - DataSource Pool 管理（新增）',
     '#/fm_prog006d0001/create', 'N', 'N', 0, 0, 'CORE', 'ITEM', 'DATABASE', 'database',
     'admin', CURRENT_TIMESTAMP, NULL, NULL),
    (UUID(), 'FM_PROG006D0001E', 'FF01 - DataSource Pool 管理（編輯）',
     '#/fm_prog006d0001/edit', 'Y', 'N', 0, 0, 'CORE', 'ITEM', 'DATABASE', 'database',
     'admin', CURRENT_TIMESTAMP, NULL, NULL);

COMMIT;
