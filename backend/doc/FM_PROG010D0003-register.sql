-- FlowMint FM_PROG010D0003 - 外部系統 API 說明程式註冊
-- 本檔只註冊實際 UI Page，不配置選單或角色權限，可重複執行。
START TRANSACTION;

UPDATE tb_sys_prog
   SET NAME = 'FJ03 - 外部系統 API 說明',
       URL = '#/fm_prog010d0003',
       EDIT_MODE = 'N',
       ITEM_TYPE = 'ITEM',
       FONT_ICON_CLASS_ID = 'file-earmark-code',
       UUSERID = 'admin',
       UDATE = CURRENT_TIMESTAMP
 WHERE PROG_ID = 'FM_PROG010D0003Q';

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG010D0003Q', 'FJ03 - 外部系統 API 說明',
       '#/fm_prog010d0003', 'N', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'file-earmark-code',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM tb_sys_prog WHERE PROG_ID = 'FM_PROG010D0003Q'
);

COMMIT;
