-- FM_PURCHASE_REQUEST Version 1：加入請購流程分流所需的核心欄位。
-- 目標固定為 A01 Tenant 的指定 DRAFT 與更新前 SHA-256，避免覆寫其他版本。

START TRANSACTION;

SET @purchase_form_id = '2d939f65-b78e-454d-a48d-cdd538222d96';
SET @expected_sha256 = 'd6e22653fecbb7c7ab0155d5934f38faca025b20a59e85235748695c15ce7509';

UPDATE fm_form_version v
SET v.SCHEMA_CONTENT = JSON_ARRAY_INSERT(
        JSON_ARRAY_INSERT(
            JSON_ARRAY_INSERT(
                JSON_SET(
                    v.SCHEMA_CONTENT,
                    '$.components[1].components[0].columns[0].components[0].data.values',
                    JSON_ARRAY(
                        JSON_OBJECT('label', '產線設備', 'value', 'PRODUCTION_EQUIPMENT'),
                        JSON_OBJECT('label', '資訊設備', 'value', 'IT_EQUIPMENT'),
                        JSON_OBJECT('label', '軟體／雲端服務', 'value', 'SOFTWARE_SERVICE'),
                        JSON_OBJECT('label', '品質／檢測設備', 'value', 'QUALITY_EQUIPMENT'),
                        JSON_OBJECT('label', '工安／環保設備', 'value', 'EHS_EQUIPMENT'),
                        JSON_OBJECT('label', '原物料', 'value', 'RAW_MATERIAL'),
                        JSON_OBJECT('label', '工程／修繕', 'value', 'CONSTRUCTION'),
                        JSON_OBJECT('label', '辦公設備／用品', 'value', 'OFFICE_SUPPLIES'),
                        JSON_OBJECT('label', '顧問／委外專業服務', 'value', 'PROFESSIONAL_SERVICE'),
                        JSON_OBJECT('label', '一般採購', 'value', 'GENERAL'),
                        JSON_OBJECT('label', '其他', 'value', 'OTHER')
                    )
                ),
                '$.components[2]',
                JSON_OBJECT(
                    'type', 'panel',
                    'key', 'purchaseGovernance',
                    'title', '支出與預算',
                    'theme', 'primary',
                    'components', JSON_ARRAY(
                        JSON_OBJECT(
                            'type', 'columns',
                            'key', 'purchaseGovernanceColumns1',
                            'input', FALSE,
                            'columns', JSON_ARRAY(
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT(
                                        'type', 'select', 'key', 'requestType',
                                        'label', '申請類型', 'input', TRUE,
                                        'dataSrc', 'values',
                                        'data', JSON_OBJECT('values', JSON_ARRAY(
                                            JSON_OBJECT('label', '新購', 'value', 'NEW'),
                                            JSON_OBJECT('label', '汰換', 'value', 'REPLACEMENT'),
                                            JSON_OBJECT('label', '增購', 'value', 'ADDITION'),
                                            JSON_OBJECT('label', '續約', 'value', 'RENEWAL'),
                                            JSON_OBJECT('label', '維修', 'value', 'REPAIR'),
                                            JSON_OBJECT('label', '租賃', 'value', 'LEASE')
                                        )),
                                        'validate', JSON_OBJECT('required', TRUE)
                                    )
                                )),
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT(
                                        'type', 'select', 'key', 'expenseType',
                                        'label', '支出性質', 'input', TRUE,
                                        'dataSrc', 'values',
                                        'data', JSON_OBJECT('values', JSON_ARRAY(
                                            JSON_OBJECT('label', '資本支出 CAPEX', 'value', 'CAPEX'),
                                            JSON_OBJECT('label', '營業費用 OPEX', 'value', 'OPEX')
                                        )),
                                        'validate', JSON_OBJECT('required', TRUE)
                                    )
                                )),
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT(
                                        'type', 'select', 'key', 'budgetStatus',
                                        'label', '預算狀態', 'input', TRUE,
                                        'dataSrc', 'values',
                                        'data', JSON_OBJECT('values', JSON_ARRAY(
                                            JSON_OBJECT('label', '預算內', 'value', 'IN_BUDGET'),
                                            JSON_OBJECT('label', '預算外', 'value', 'OUT_OF_BUDGET')
                                        )),
                                        'validate', JSON_OBJECT('required', TRUE)
                                    )
                                ))
                            )
                        ),
                        JSON_OBJECT(
                            'type', 'columns',
                            'key', 'purchaseGovernanceColumns2',
                            'input', FALSE,
                            'columns', JSON_ARRAY(
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'budgetNo',
                                        'label', '預算編號', 'input', TRUE,
                                        'validate', JSON_OBJECT('maxLength', 50))
                                )),
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'costCenter',
                                        'label', '成本中心', 'input', TRUE,
                                        'validate', JSON_OBJECT('required', TRUE, 'maxLength', 50))
                                )),
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'projectCode',
                                        'label', '專案代碼', 'input', TRUE,
                                        'validate', JSON_OBJECT('maxLength', 50))
                                ))
                            )
                        ),
                        JSON_OBJECT(
                            'type', 'number', 'key', 'projectTotalAmount',
                            'label', '專案總額（TWD，含本次及相關請購）',
                            'description', '用於重大投資與防拆單判斷；不得低於本次請購含稅總額。',
                            'input', TRUE,
                            'validate', JSON_OBJECT('required', TRUE, 'min', 0,
                                'max', 999999999999, 'decimalLimit', 2)
                        )
                    )
                )
            ),
            '$.components[3]',
            JSON_OBJECT(
                'type', 'panel',
                'key', 'sourcingInformation',
                'title', '採購方式與例外',
                'theme', 'primary',
                'components', JSON_ARRAY(
                    JSON_OBJECT(
                        'type', 'select', 'key', 'sourcingMethod',
                        'label', '採購方式', 'input', TRUE,
                        'dataSrc', 'values',
                        'data', JSON_OBJECT('values', JSON_ARRAY(
                            JSON_OBJECT('label', '一般詢比價', 'value', 'COMPETITIVE_QUOTE'),
                            JSON_OBJECT('label', '公開／邀請招標', 'value', 'TENDER'),
                            JSON_OBJECT('label', '單一來源', 'value', 'SINGLE_SOURCE'),
                            JSON_OBJECT('label', '框架合約', 'value', 'FRAMEWORK_AGREEMENT')
                        )),
                        'validate', JSON_OBJECT('required', TRUE)
                    ),
                    JSON_OBJECT('type', 'textfield', 'key', 'preferredSupplier',
                        'label', '建議供應商',
                        'description', '僅供採購評估，不代表免除詢比價程序。',
                        'input', TRUE, 'validate', JSON_OBJECT('maxLength', 150)),
                    JSON_OBJECT('type', 'textarea', 'key', 'singleSourceReason',
                        'label', '單一來源／指定供應商理由', 'input', TRUE, 'rows', 3,
                        'validate', JSON_OBJECT('maxLength', 1000)),
                    JSON_OBJECT('type', 'checkbox', 'key', 'emergencyPurchase',
                        'label', '緊急採購', 'input', TRUE, 'defaultValue', FALSE),
                    JSON_OBJECT('type', 'textarea', 'key', 'emergencyReason',
                        'label', '緊急原因與未即時採購的影響', 'input', TRUE, 'rows', 3,
                        'validate', JSON_OBJECT('maxLength', 1000)),
                    JSON_OBJECT('type', 'checkbox', 'key', 'newSupplier',
                        'label', '新供應商', 'input', TRUE, 'defaultValue', FALSE),
                    JSON_OBJECT('type', 'checkbox', 'key', 'relatedParty',
                        'label', '可能涉及關係人交易', 'input', TRUE, 'defaultValue', FALSE),
                    JSON_OBJECT('type', 'checkbox', 'key', 'importPurchase',
                        'label', '進口採購', 'input', TRUE, 'defaultValue', FALSE)
                )
            )
        ),
        '$.components[4]',
        JSON_OBJECT(
            'type', 'panel',
            'key', 'riskQuestionnaire',
            'title', '風險與專業審查問卷',
            'theme', 'primary',
            'description', '請依實際情況勾選；系統將據此增加必要專業審查，不代表可自行略過審查。',
            'components', JSON_ARRAY(
                JSON_OBJECT('type', 'checkbox', 'key', 'involvesInformationSystem',
                    'label', '涉及公司網路、帳號、系統或設備連網',
                    'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'involvesCompanyData',
                    'label', '涉及公司資料', 'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'involvesPersonalData',
                    'label', '涉及員工、客戶或其他個人資料',
                    'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'involvesConstruction',
                    'label', '涉及施工、動火、高處、用電、管線或廠務工程',
                    'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'involvesEhsRisk',
                    'label', '涉及機械安全、職安或其他環安衛風險',
                    'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'involvesEnvironmentalPermit',
                    'label', '涉及排放、化學品、環保或法規許可',
                    'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'requiresContract',
                    'label', '需要合約或特殊付款條件',
                    'input', TRUE, 'defaultValue', FALSE),
                JSON_OBJECT('type', 'checkbox', 'key', 'requiresConfidentiality',
                    'label', '涉及保密、智慧財產或 NDA',
                    'input', TRUE, 'defaultValue', FALSE)
            )
        )
    ),
    v.CUSTOM_SCRIPT_CONTENT = REPLACE(
        v.CUSTOM_SCRIPT_CONTENT,
        '    await ctx.redraw();\n    return true;\n  }\n};',
        CONCAT(
            '    const currentTotal = Number(ctx.data.totalAmount || 0);\n',
            '    const projectTotal = Number(ctx.data.projectTotalAmount || 0);\n',
            '    if (projectTotal < currentTotal) {\n',
            '      return { valid: false, message: "專案總額不得低於本次請購含稅總額" };\n',
            '    }\n',
            '    if (ctx.data.budgetStatus === "IN_BUDGET"\n',
            '        && !String(ctx.data.budgetNo || "").trim()) {\n',
            '      return { valid: false, message: "預算內請購必須填寫預算編號" };\n',
            '    }\n',
            '    if (ctx.data.sourcingMethod === "SINGLE_SOURCE"\n',
            '        && !String(ctx.data.singleSourceReason || "").trim()) {\n',
            '      return { valid: false, message: "單一來源採購必須填寫原因" };\n',
            '    }\n',
            '    if (ctx.data.emergencyPurchase\n',
            '        && !String(ctx.data.emergencyReason || "").trim()) {\n',
            '      return { valid: false, message: "緊急採購必須填寫原因與影響" };\n',
            '    }\n',
            '    await ctx.redraw();\n',
            '    return true;\n',
            '  }\n',
            '};'
        )
    ),
    v.UUSERID = 'SYSTEM',
    v.UDATE = NOW(3)
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND v.VERSION_STATUS = 'DRAFT'
  AND v.CONTENT_SHA256 = @expected_sha256
  AND JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'expenseType', NULL, '$**.key') IS NULL
  AND (CHAR_LENGTH(v.CUSTOM_SCRIPT_CONTENT)
      - CHAR_LENGTH(REPLACE(v.CUSTOM_SCRIPT_CONTENT,
          '    await ctx.redraw();\n    return true;\n  }\n};', ''))) > 0;

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
  AND v.VERSION_STATUS = 'DRAFT'
  AND @updated_rows = 1;

SELECT @updated_rows AS updated_rows,
       JSON_VALID(v.SCHEMA_CONTENT) AS schema_valid,
       JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'expenseType', NULL, '$**.key') AS expense_type_path,
       JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'sourcingMethod', NULL, '$**.key') AS sourcing_path,
       JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'involvesEhsRisk', NULL, '$**.key') AS ehs_path,
       LOCATE('專案總額不得低於本次請購含稅總額',
           v.CUSTOM_SCRIPT_CONTENT) > 0 AS script_validation_present,
       v.CONTENT_SHA256
  FROM fm_form_version v
 WHERE v.TENANT_ID = 'A01'
   AND v.FORM_ID = @purchase_form_id
   AND v.VERSION_NO = 1;

COMMIT;
