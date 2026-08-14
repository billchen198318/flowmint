-- FM_PURCHASE_REQUEST Version 1 第二批：設備工程、外幣付款與 CAPEX 投資效益。

START TRANSACTION;

SET @purchase_form_id = '2d939f65-b78e-454d-a48d-cdd538222d96';
SET @expected_sha256 = '247f1d02d598004aef062476bea5389bb9b75ce75204f913fe06d2cc7cbaa4c3';

UPDATE fm_form_version v
SET v.SCHEMA_CONTENT = JSON_ARRAY_INSERT(
        JSON_ARRAY_INSERT(
            JSON_ARRAY_INSERT(
                JSON_SET(
                    v.SCHEMA_CONTENT,
                    '$.components[1].components[0].columns[2].components[0].data.values',
                    JSON_ARRAY(
                        JSON_OBJECT('label', '新臺幣 TWD', 'value', 'TWD'),
                        JSON_OBJECT('label', '美元 USD', 'value', 'USD'),
                        JSON_OBJECT('label', '歐元 EUR', 'value', 'EUR'),
                        JSON_OBJECT('label', '日圓 JPY', 'value', 'JPY'),
                        JSON_OBJECT('label', '人民幣 CNY', 'value', 'CNY')
                    ),
                    '$.components[6].components[0].columns[3].components[0].label',
                    '含稅總金額（原幣）'
                ),
                '$.components[5]',
                JSON_OBJECT(
                    'type', 'panel',
                    'key', 'equipmentEngineeringDetails',
                    'title', '設備／工程與驗收資料',
                    'theme', 'primary',
                    'description', '產線、品質、工安設備或涉及施工時必須完整填寫。',
                    'customConditional', 'show = ["PRODUCTION_EQUIPMENT", "QUALITY_EQUIPMENT", "EHS_EQUIPMENT", "CONSTRUCTION"].includes(data.purchaseCategory) || data.involvesConstruction;',
                    'components', JSON_ARRAY(
                        JSON_OBJECT('type', 'textfield', 'key', 'installationLocation',
                            'label', '安裝／使用地點', 'input', TRUE,
                            'validate', JSON_OBJECT('maxLength', 200)),
                        JSON_OBJECT('type', 'textarea', 'key', 'technicalSpecification',
                            'label', '技術規格', 'input', TRUE, 'rows', 4,
                            'validate', JSON_OBJECT('maxLength', 4000)),
                        JSON_OBJECT('type', 'textarea', 'key', 'acceptanceCriteria',
                            'label', '驗收標準', 'input', TRUE, 'rows', 4,
                            'validate', JSON_OBJECT('maxLength', 4000)),
                        JSON_OBJECT(
                            'type', 'columns', 'key', 'capacityColumns', 'input', FALSE,
                            'columns', JSON_ARRAY(
                                JSON_OBJECT('width', 6, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'currentCapacity',
                                        'label', '目前產能／能力', 'input', TRUE,
                                        'validate', JSON_OBJECT('maxLength', 200))
                                )),
                                JSON_OBJECT('width', 6, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'expectedCapacity',
                                        'label', '預計產能／能力', 'input', TRUE,
                                        'validate', JSON_OBJECT('maxLength', 200))
                                ))
                            )
                        ),
                        JSON_OBJECT('type', 'textarea', 'key', 'utilityRequirements',
                            'label', '水電、氣體、管線、網路及其他公用需求',
                            'input', TRUE, 'rows', 3,
                            'validate', JSON_OBJECT('maxLength', 2000)),
                        JSON_OBJECT('type', 'textarea', 'key', 'maintenancePlan',
                            'label', '維護與備品計畫', 'input', TRUE, 'rows', 3,
                            'validate', JSON_OBJECT('maxLength', 2000)),
                        JSON_OBJECT(
                            'type', 'columns', 'key', 'warrantyTrainingColumns', 'input', FALSE,
                            'columns', JSON_ARRAY(
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'warrantyRequirement',
                                        'label', '保固需求', 'input', TRUE,
                                        'validate', JSON_OBJECT('maxLength', 200))
                                )),
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'textfield', 'key', 'trainingRequirement',
                                        'label', '教育訓練需求', 'input', TRUE,
                                        'validate', JSON_OBJECT('maxLength', 200))
                                )),
                                JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                    JSON_OBJECT('type', 'number', 'key', 'expectedUsefulLife',
                                        'label', '預計使用年限', 'suffix', '年', 'input', TRUE,
                                        'validate', JSON_OBJECT('min', 0, 'max', 100, 'decimalLimit', 1))
                                ))
                            )
                        )
                    )
                )
            ),
            '$.components[6]',
            JSON_OBJECT(
                'type', 'panel',
                'key', 'paymentInformation',
                'title', '外幣與付款條件',
                'theme', 'primary',
                'components', JSON_ARRAY(
                    JSON_OBJECT(
                        'type', 'columns', 'key', 'currencyColumns', 'input', FALSE,
                        'columns', JSON_ARRAY(
                            JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                JSON_OBJECT('type', 'number', 'key', 'exchangeRate',
                                    'label', '換算匯率（1 原幣兌 TWD）',
                                    'input', TRUE, 'defaultValue', 1,
                                    'validate', JSON_OBJECT('required', TRUE, 'min', 0.000001,
                                        'max', 1000000, 'decimalLimit', 6))
                            )),
                            JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                JSON_OBJECT('type', 'number', 'key', 'totalAmountTwd',
                                    'label', 'TWD 換算含稅總額', 'input', TRUE,
                                    'disabled', TRUE, 'persistent', TRUE,
                                    'validate', JSON_OBJECT('min', 0, 'decimalLimit', 2))
                            )),
                            JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                                JSON_OBJECT('type', 'checkbox', 'key', 'crossFiscalYear',
                                    'label', '跨年度案件', 'input', TRUE, 'defaultValue', FALSE)
                            ))
                        )
                    ),
                    JSON_OBJECT('type', 'textarea', 'key', 'paymentTerms',
                        'label', '付款條件', 'input', TRUE, 'rows', 3,
                        'validate', JSON_OBJECT('required', TRUE, 'maxLength', 1000)),
                    JSON_OBJECT('type', 'checkbox', 'key', 'prepaymentRequired',
                        'label', '需要預付款', 'input', TRUE, 'defaultValue', FALSE),
                    JSON_OBJECT('type', 'number', 'key', 'prepaymentPercentage',
                        'label', '預付款比例', 'suffix', '%', 'input', TRUE,
                        'conditional', JSON_OBJECT('show', TRUE,
                            'when', 'prepaymentRequired', 'eq', 'true'),
                        'validate', JSON_OBJECT('min', 0, 'max', 100, 'decimalLimit', 2))
                )
            )
        ),
        '$.components[7]',
        JSON_OBJECT(
            'type', 'panel',
            'key', 'investmentBenefit',
            'title', 'CAPEX 投資效益',
            'theme', 'primary',
            'customConditional', 'show = data.expenseType === "CAPEX";',
            'components', JSON_ARRAY(
                JSON_OBJECT('type', 'textarea', 'key', 'investmentReason',
                    'label', '投資原因', 'input', TRUE, 'rows', 3,
                    'validate', JSON_OBJECT('maxLength', 2000)),
                JSON_OBJECT('type', 'textarea', 'key', 'expectedBenefit',
                    'label', '預期效益', 'input', TRUE, 'rows', 3,
                    'validate', JSON_OBJECT('maxLength', 2000)),
                JSON_OBJECT(
                    'type', 'columns', 'key', 'investmentMetricColumns', 'input', FALSE,
                    'columns', JSON_ARRAY(
                        JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                            JSON_OBJECT('type', 'number', 'key', 'paybackPeriod',
                                'label', '預估回收年限', 'suffix', '年', 'input', TRUE,
                                'validate', JSON_OBJECT('min', 0, 'max', 100, 'decimalLimit', 2))
                        )),
                        JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                            JSON_OBJECT('type', 'number', 'key', 'npv',
                                'label', 'NPV（TWD）', 'input', TRUE,
                                'validate', JSON_OBJECT('min', -999999999999,
                                    'max', 999999999999, 'decimalLimit', 2))
                        )),
                        JSON_OBJECT('width', 4, 'size', 'md', 'components', JSON_ARRAY(
                            JSON_OBJECT('type', 'number', 'key', 'irr',
                                'label', 'IRR', 'suffix', '%', 'input', TRUE,
                                'validate', JSON_OBJECT('min', -100, 'max', 10000,
                                    'decimalLimit', 2))
                        ))
                    )
                ),
                JSON_OBJECT('type', 'textarea', 'key', 'capacityIncrease',
                    'label', '產能提升說明', 'input', TRUE, 'rows', 3,
                    'validate', JSON_OBJECT('maxLength', 2000)),
                JSON_OBJECT('type', 'textarea', 'key', 'qualityImprovement',
                    'label', '品質改善說明', 'input', TRUE, 'rows', 3,
                    'validate', JSON_OBJECT('maxLength', 2000)),
                JSON_OBJECT('type', 'textarea', 'key', 'riskWithoutInvestment',
                    'label', '不投資的影響與風險', 'input', TRUE, 'rows', 3,
                    'validate', JSON_OBJECT('maxLength', 2000))
            )
        )
    ),
    v.CUSTOM_SCRIPT_CONTENT = REPLACE(
        REPLACE(
            REPLACE(
                v.CUSTOM_SCRIPT_CONTENT,
                '  data.totalAmount = money(data.subtotal + data.taxAmount);',
                CONCAT(
                    '  data.totalAmount = money(data.subtotal + data.taxAmount);\n',
                    '  if (data.currency === "TWD") data.exchangeRate = 1;\n',
                    '  const exchangeRate = Number(data.exchangeRate || 0);\n',
                    '  data.totalAmountTwd = money(data.totalAmount * exchangeRate);\n',
                    '  if (!(Number(data.projectTotalAmount) > 0)) {\n',
                    '    data.projectTotalAmount = data.totalAmountTwd;\n',
                    '  }'
                )
            ),
            '["items", "quantity", "unitPrice", "taxRate"].includes(changedKey)',
            '["items", "quantity", "unitPrice", "taxRate", "currency", "exchangeRate"].includes(changedKey)'
        ),
        '      await ctx.setValue("totalAmount", ctx.data.totalAmount);',
        CONCAT(
            '      await ctx.setValue("totalAmount", ctx.data.totalAmount);\n',
            '      await ctx.setValue("totalAmountTwd", ctx.data.totalAmountTwd);\n',
            '      await ctx.setValue("projectTotalAmount", ctx.data.projectTotalAmount);'
        )
    ),
    v.UUSERID = 'SYSTEM',
    v.UDATE = NOW(3)
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND v.VERSION_STATUS = 'DRAFT'
  AND v.CONTENT_SHA256 = @expected_sha256
  AND JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'technicalSpecification', NULL, '$**.key') IS NULL
  AND LOCATE('  data.totalAmount = money(data.subtotal + data.taxAmount);',
      v.CUSTOM_SCRIPT_CONTENT) > 0;

SET @updated_rows = ROW_COUNT();

UPDATE fm_form_version v
SET v.CUSTOM_SCRIPT_CONTENT = REPLACE(
        v.CUSTOM_SCRIPT_CONTENT,
        '    const currentTotal = Number(ctx.data.totalAmount || 0);',
        CONCAT(
            '    const exchangeRate = Number(ctx.data.exchangeRate || 0);\n',
            '    if (!(exchangeRate > 0)) {\n',
            '      return { valid: false, message: "換算匯率必須大於零" };\n',
            '    }\n',
            '    const currentTotal = Number(ctx.data.totalAmountTwd || 0);'
        )
    )
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND @updated_rows = 1;

UPDATE fm_form_version v
SET v.CUSTOM_SCRIPT_CONTENT = REPLACE(
        v.CUSTOM_SCRIPT_CONTENT,
        '    await ctx.redraw();\n    return true;\n  }\n};',
        CONCAT(
            '    if (ctx.data.prepaymentRequired) {\n',
            '      const percentage = Number(ctx.data.prepaymentPercentage || 0);\n',
            '      if (!(percentage > 0 && percentage <= 100)) {\n',
            '        return { valid: false, message: "預付款比例必須大於 0 且不超過 100%" };\n',
            '      }\n',
            '    }\n',
            '    const equipmentCategories = [\n',
            '      "PRODUCTION_EQUIPMENT", "QUALITY_EQUIPMENT",\n',
            '      "EHS_EQUIPMENT", "CONSTRUCTION"\n',
            '    ];\n',
            '    if (equipmentCategories.includes(ctx.data.purchaseCategory)\n',
            '        || ctx.data.involvesConstruction) {\n',
            '      if (!String(ctx.data.installationLocation || "").trim()) {\n',
            '        return { valid: false, message: "設備或工程請購必須填寫安裝／使用地點" };\n',
            '      }\n',
            '      if (!String(ctx.data.technicalSpecification || "").trim()) {\n',
            '        return { valid: false, message: "設備或工程請購必須填寫技術規格" };\n',
            '      }\n',
            '      if (!String(ctx.data.acceptanceCriteria || "").trim()) {\n',
            '        return { valid: false, message: "設備或工程請購必須填寫驗收標準" };\n',
            '      }\n',
            '    }\n',
            '    if (ctx.data.expenseType === "CAPEX") {\n',
            '      if (!String(ctx.data.investmentReason || "").trim()) {\n',
            '        return { valid: false, message: "CAPEX 必須填寫投資原因" };\n',
            '      }\n',
            '      if (!String(ctx.data.expectedBenefit || "").trim()) {\n',
            '        return { valid: false, message: "CAPEX 必須填寫預期效益" };\n',
            '      }\n',
            '    }\n',
            '    await ctx.redraw();\n',
            '    return true;\n',
            '  }\n',
            '};'
        )
    ),
    v.CONTENT_SHA256 = SHA2(CONCAT(
        v.SCHEMA_CONTENT,
        CHAR(10),
        COALESCE(v.UI_SCHEMA_CONTENT, ''),
        CHAR(10),
        COALESCE(REPLACE(
            v.CUSTOM_SCRIPT_CONTENT,
            '    await ctx.redraw();\n    return true;\n  }\n};',
            CONCAT(
                '    if (ctx.data.prepaymentRequired) {\n',
                '      const percentage = Number(ctx.data.prepaymentPercentage || 0);\n',
                '      if (!(percentage > 0 && percentage <= 100)) {\n',
                '        return { valid: false, message: "預付款比例必須大於 0 且不超過 100%" };\n',
                '      }\n',
                '    }\n',
                '    const equipmentCategories = [\n',
                '      "PRODUCTION_EQUIPMENT", "QUALITY_EQUIPMENT",\n',
                '      "EHS_EQUIPMENT", "CONSTRUCTION"\n',
                '    ];\n',
                '    if (equipmentCategories.includes(ctx.data.purchaseCategory)\n',
                '        || ctx.data.involvesConstruction) {\n',
                '      if (!String(ctx.data.installationLocation || "").trim()) {\n',
                '        return { valid: false, message: "設備或工程請購必須填寫安裝／使用地點" };\n',
                '      }\n',
                '      if (!String(ctx.data.technicalSpecification || "").trim()) {\n',
                '        return { valid: false, message: "設備或工程請購必須填寫技術規格" };\n',
                '      }\n',
                '      if (!String(ctx.data.acceptanceCriteria || "").trim()) {\n',
                '        return { valid: false, message: "設備或工程請購必須填寫驗收標準" };\n',
                '      }\n',
                '    }\n',
                '    if (ctx.data.expenseType === "CAPEX") {\n',
                '      if (!String(ctx.data.investmentReason || "").trim()) {\n',
                '        return { valid: false, message: "CAPEX 必須填寫投資原因" };\n',
                '      }\n',
                '      if (!String(ctx.data.expectedBenefit || "").trim()) {\n',
                '        return { valid: false, message: "CAPEX 必須填寫預期效益" };\n',
                '      }\n',
                '    }\n',
                '    await ctx.redraw();\n',
                '    return true;\n',
                '  }\n',
                '};'
            )
        ), '')
    ), 256)
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND @updated_rows = 1;

-- Recalculate once more from the final stored values to avoid assignment-order ambiguity.
UPDATE fm_form_version v
SET v.CONTENT_SHA256 = SHA2(CONCAT(
        v.SCHEMA_CONTENT, CHAR(10), COALESCE(v.UI_SCHEMA_CONTENT, ''),
        CHAR(10), COALESCE(v.CUSTOM_SCRIPT_CONTENT, '')
    ), 256)
WHERE v.TENANT_ID = 'A01'
  AND v.FORM_ID = @purchase_form_id
  AND v.VERSION_NO = 1
  AND @updated_rows = 1;

SELECT @updated_rows AS updated_rows,
       JSON_VALID(v.SCHEMA_CONTENT) AS schema_valid,
       JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'technicalSpecification', NULL, '$**.key') AS technical_path,
       JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'totalAmountTwd', NULL, '$**.key') AS twd_path,
       JSON_SEARCH(v.SCHEMA_CONTENT, 'one', 'investmentReason', NULL, '$**.key') AS investment_path,
       LOCATE('換算匯率必須大於零', v.CUSTOM_SCRIPT_CONTENT) > 0 AS exchange_validation,
       LOCATE('設備或工程請購必須填寫技術規格', v.CUSTOM_SCRIPT_CONTENT) > 0 AS equipment_validation,
       LOCATE('CAPEX 必須填寫投資原因', v.CUSTOM_SCRIPT_CONTENT) > 0 AS capex_validation,
       LOCATE('預付款比例必須大於 0', v.CUSTOM_SCRIPT_CONTENT) > 0 AS prepayment_validation,
       v.CONTENT_SHA256
  FROM fm_form_version v
 WHERE v.TENANT_ID = 'A01'
   AND v.FORM_ID = @purchase_form_id
   AND v.VERSION_NO = 1;

COMMIT;
