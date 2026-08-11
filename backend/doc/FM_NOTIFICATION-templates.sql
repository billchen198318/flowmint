-- FlowMint Phase 5 default notification templates for QIFU4 Template Manager.
-- INSERT IGNORE preserves administrator-customized templates on repeat deployment.

INSERT IGNORE INTO tb_sys_template
    (OID, TEMPLATE_ID, TITLE, MESSAGE, DESCRIPTION, CUSERID, CDATE)
VALUES
    (UUID(), 'FMTASKASG', '你有新的流程待辦', '${taskName}',
     'FlowMint 待辦指派通知', 'system', NOW()),
    (UUID(), 'FMPROCMP', '你的流程已完成', '流程編號：${referenceId}',
     'FlowMint 流程完成通知', 'system', NOW()),
    (UUID(), 'FMPROREJ', '你的流程已駁回', '流程編號：${referenceId}',
     'FlowMint 流程駁回通知', 'system', NOW()),
    (UUID(), 'FMPROCAN', '流程已取消', '流程編號：${referenceId}',
     'FlowMint 流程撤回或取消通知', 'system', NOW());

INSERT IGNORE INTO tb_sys_template_param
    (OID, TEMPLATE_ID, IS_TITLE, TEMPLATE_VAR, OBJECT_VAR, CUSERID, CDATE)
VALUES
    (UUID(), 'FMTASKASG', 'N', 'taskName', 'taskName', 'system', NOW()),
    (UUID(), 'FMPROCMP', 'N', 'referenceId', 'referenceId', 'system', NOW()),
    (UUID(), 'FMPROREJ', 'N', 'referenceId', 'referenceId', 'system', NOW()),
    (UUID(), 'FMPROCAN', 'N', 'referenceId', 'referenceId', 'system', NOW());
