-- FlowMint FM_PROG002D0006 - 部門職務與擔任人程式註冊
-- 選配功能：一般主管簽核與簽核群組不需要此設定。
-- 僅註冊程式，不配置角色權限；請透過正式部署／資料庫變更程序執行。

START TRANSACTION;

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG002D0006Q', 'FB06 - 部門職務與擔任人（選配）',
       '#/fm_prog002d0006', 'N', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'person-workspace',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_sys_prog
    WHERE PROG_ID = 'FM_PROG002D0006Q'
);

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG002D0006A', 'FB06 - 部門職務與擔任人（新增／選配）',
       '#/fm_prog002d0006/create', 'N', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'person-workspace',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_sys_prog
    WHERE PROG_ID = 'FM_PROG002D0006A'
);

INSERT INTO tb_sys_prog
    (OID, PROG_ID, NAME, URL, EDIT_MODE, IS_DIALOG, DIALOG_W, DIALOG_H,
     PROG_SYSTEM, ITEM_TYPE, ICON, FONT_ICON_CLASS_ID,
     CUSERID, CDATE, UUSERID, UDATE)
SELECT UUID(), 'FM_PROG002D0006E', 'FB06 - 部門職務與擔任人（編輯／選配）',
       '#/fm_prog002d0006/edit', 'Y', 'N', 0, 0,
       'CORE', 'ITEM', 'ORGANIZATION', 'person-workspace',
       'admin', CURRENT_TIMESTAMP, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_sys_prog
    WHERE PROG_ID = 'FM_PROG002D0006E'
);

COMMIT;
