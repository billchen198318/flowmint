-- FlowMint FM_PROG006D0002 - Data Action 設計程式註冊
-- 僅註冊程式資料，目前不配置角色權限。
START TRANSACTION;

INSERT IGNORE INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
VALUES
    (UUID(), 'FM_PROG006D0002Q', 'FF02 - Data Action 設計',
     '#/fm_prog006d0002', 'N', 'N', 0, 0, 'CORE', 'ITEM',
     'DATABASE', 'database', 'admin', CURRENT_TIMESTAMP, NULL, NULL),
    (UUID(), 'FM_PROG006D0002A', 'FF02 - Data Action 設計（新增）',
     '#/fm_prog006d0002/create', 'N', 'N', 0, 0, 'CORE', 'ITEM',
     'DATABASE', 'database', 'admin', CURRENT_TIMESTAMP, NULL, NULL),
    (UUID(), 'FM_PROG006D0002E', 'FF02 - Data Action 設計（編輯）',
     '#/fm_prog006d0002/edit', 'Y', 'N', 0, 0, 'CORE', 'ITEM',
     'DATABASE', 'database', 'admin', CURRENT_TIMESTAMP, NULL, NULL);

COMMIT;
