-- FlowMint FM_PROG010D0001 - AI Provider 管理程式註冊
-- 僅註冊 Program Family 與實際 Query／Create／Edit 頁面。
-- API Command C／U／D 不建立 tb_sys_prog Page Item。
-- 本檔不配置角色權限，可重複執行。
START TRANSACTION;

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG010D', 'FJ. FlowMint AI 服務',
       '/', 'N', 'N', 0, 0,
       'CORE', 'FOLDER', 'ORGANIZATION', 'robot',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
      FROM tb_sys_prog
     WHERE PROG_ID = 'FM_PROG010D'
);

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG010D0001Q', 'FJ01 - AI Provider 管理',
       '#/fm_prog010d0001', 'N', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'robot',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
      FROM tb_sys_prog
     WHERE PROG_ID = 'FM_PROG010D0001Q'
);

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG010D0001A', 'FJ01 - AI Provider 管理（新增）',
       '#/fm_prog010d0001/create', 'N', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'robot',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
      FROM tb_sys_prog
     WHERE PROG_ID = 'FM_PROG010D0001A'
);

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG010D0001E', 'FJ01 - AI Provider 管理（編輯）',
       '#/fm_prog010d0001/edit', 'Y', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'robot',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
      FROM tb_sys_prog
     WHERE PROG_ID = 'FM_PROG010D0001E'
);

COMMIT;
