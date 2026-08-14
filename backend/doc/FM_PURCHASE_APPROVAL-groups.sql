-- A01 請購流程簽核群組與初始成員。
-- 已依 2026-08-14 有效員工任職確認；合約／法務群組因無責任人維持 INACTIVE。

START TRANSACTION;

SET @tenant_id = 'A01';
SET @actor = 'SYSTEM';
SET @now = NOW(3);

SELECT COUNT(*) INTO @groups_before
  FROM fm_approval_group
 WHERE TENANT_ID = @tenant_id
   AND GROUP_CODE LIKE 'PURCHASE_%';

SELECT COUNT(*) INTO @members_before
  FROM fm_approval_group_member m
  JOIN fm_approval_group g
    ON g.TENANT_ID = m.TENANT_ID
   AND g.APPROVAL_GROUP_ID = m.APPROVAL_GROUP_ID
 WHERE g.TENANT_ID = @tenant_id
   AND g.GROUP_CODE LIKE 'PURCHASE_%';

INSERT INTO fm_approval_group
    (OID, TENANT_ID, APPROVAL_GROUP_ID, GROUP_CODE, GROUP_NAME,
     ASSIGNMENT_MODE, STATUS, DESCRIPTION, CUSERID, CDATE)
SELECT UUID(), @tenant_id, UUID(), seed.GROUP_CODE, seed.GROUP_NAME,
       seed.ASSIGNMENT_MODE, seed.STATUS, seed.DESCRIPTION, @actor, @now
  FROM (
        SELECT 'PURCHASE_PROCESS_REVIEW' AS GROUP_CODE,
               '製程審查' AS GROUP_NAME, 'CANDIDATE' AS ASSIGNMENT_MODE,
               'ACTIVE' AS STATUS, '產線設備、原物料及製程相容性專業審查。' AS DESCRIPTION
        UNION ALL SELECT 'PURCHASE_EQUIPMENT_REVIEW', '設備工程審查',
               'CANDIDATE', 'ACTIVE', '設備規格、安裝、維護、保固及公用需求審查。'
        UNION ALL SELECT 'PURCHASE_IT_REVIEW', '資訊／資安審查',
               'CANDIDATE', 'ACTIVE', '資訊設備、軟體、設備連網、資料及資安審查。'
        UNION ALL SELECT 'PURCHASE_QUALITY_REVIEW', '品質審查',
               'CANDIDATE', 'ACTIVE', '品質、檢測設備、驗收標準及品質風險審查。'
        UNION ALL SELECT 'PURCHASE_SAFETY_REVIEW', '工安審查',
               'CANDIDATE', 'ACTIVE', '施工、動火、高處、用電及機械安全審查。'
        UNION ALL SELECT 'PURCHASE_ENVIRONMENT_REVIEW', '環保審查',
               'CANDIDATE', 'ACTIVE', '排放、化學品、環保及法規許可審查。'
        UNION ALL SELECT 'PURCHASE_GENERAL_AFFAIRS', '總務審查',
               'CANDIDATE', 'ACTIVE', '辦公設備、用品、總務及一般設施需求審查。'
        UNION ALL SELECT 'PURCHASE_CONTRACT_REVIEW', '合約／法務審查',
               'CANDIDATE', 'INACTIVE', '尚未指定合約或法務責任人；不得用於正式流程。'
        UNION ALL SELECT 'PURCHASE_COMMERCIAL_REVIEW', '採購商務審查',
               'CANDIDATE', 'ACTIVE', '詢比議價、供應商、交期及採購條款審查。'
        UNION ALL SELECT 'PURCHASE_FINANCE_REVIEW', '財務審查',
               'CANDIDATE', 'ACTIVE', '預算、CAPEX/OPEX、資金及付款條件審查。'
        UNION ALL SELECT 'PURCHASE_INVESTMENT_REVIEW', '重大投資審議',
               'ALL', 'ACTIVE', '重大 CAPEX 跨單位投資審議；正式門檻仍須公司確認。'
       ) seed
 WHERE NOT EXISTS (
       SELECT 1
         FROM fm_approval_group existing_group
        WHERE existing_group.TENANT_ID = @tenant_id
          AND existing_group.GROUP_CODE = seed.GROUP_CODE
       );

SET @inserted_groups = ROW_COUNT();

INSERT INTO fm_approval_group_member
    (OID, TENANT_ID, APPROVAL_GROUP_MEMBER_ID, APPROVAL_GROUP_ID,
     EMPLOYEE_ID, PRIORITY, STATUS, EFFECTIVE_FROM, EFFECTIVE_TO,
     CUSERID, CDATE)
SELECT UUID(), @tenant_id, UUID(), g.APPROVAL_GROUP_ID,
       e.EMPLOYEE_ID, seed.PRIORITY, 'ACTIVE', @now, NULL,
       @actor, @now
  FROM (
        SELECT 'PURCHASE_PROCESS_REVIEW' AS GROUP_CODE,
               'fm00320' AS ACCOUNT, 10 AS PRIORITY
        UNION ALL SELECT 'PURCHASE_PROCESS_REVIEW', 'fm00320e01', 20
        UNION ALL SELECT 'PURCHASE_EQUIPMENT_REVIEW', 'fm00330', 10
        UNION ALL SELECT 'PURCHASE_EQUIPMENT_REVIEW', 'fm00330e01', 20
        UNION ALL SELECT 'PURCHASE_IT_REVIEW', 'fm00830', 10
        UNION ALL SELECT 'PURCHASE_IT_REVIEW', 'fm00830e01', 20
        UNION ALL SELECT 'PURCHASE_QUALITY_REVIEW', 'fm00510', 10
        UNION ALL SELECT 'PURCHASE_QUALITY_REVIEW', 'fm00510e01', 20
        UNION ALL SELECT 'PURCHASE_QUALITY_REVIEW', 'fm00510e03', 30
        UNION ALL SELECT 'PURCHASE_QUALITY_REVIEW', 'fm00510e05', 40
        UNION ALL SELECT 'PURCHASE_SAFETY_REVIEW', 'fm00910', 10
        UNION ALL SELECT 'PURCHASE_SAFETY_REVIEW', 'fm00910e01', 20
        UNION ALL SELECT 'PURCHASE_ENVIRONMENT_REVIEW', 'fm00920', 10
        UNION ALL SELECT 'PURCHASE_ENVIRONMENT_REVIEW', 'fm00920e01', 20
        UNION ALL SELECT 'PURCHASE_GENERAL_AFFAIRS', 'fm00820', 10
        UNION ALL SELECT 'PURCHASE_GENERAL_AFFAIRS', 'fm00820e01', 20
        UNION ALL SELECT 'PURCHASE_COMMERCIAL_REVIEW', 'fm00610', 10
        UNION ALL SELECT 'PURCHASE_COMMERCIAL_REVIEW', 'fm00610e01', 20
        UNION ALL SELECT 'PURCHASE_COMMERCIAL_REVIEW', 'fm00610e03', 30
        UNION ALL SELECT 'PURCHASE_FINANCE_REVIEW', 'fm00710', 10
        UNION ALL SELECT 'PURCHASE_FINANCE_REVIEW', 'fm00710e01', 20
        UNION ALL SELECT 'PURCHASE_INVESTMENT_REVIEW', 'fm00300', 10
        UNION ALL SELECT 'PURCHASE_INVESTMENT_REVIEW', 'fm00400', 20
        UNION ALL SELECT 'PURCHASE_INVESTMENT_REVIEW', 'fm00600', 30
        UNION ALL SELECT 'PURCHASE_INVESTMENT_REVIEW', 'fm00700', 40
        UNION ALL SELECT 'PURCHASE_INVESTMENT_REVIEW', 'fm00100', 50
       ) seed
  JOIN fm_approval_group g
    ON g.TENANT_ID = @tenant_id
   AND g.GROUP_CODE = seed.GROUP_CODE
  JOIN fm_employee e
    ON e.TENANT_ID = @tenant_id
   AND e.ACCOUNT = seed.ACCOUNT
   AND e.STATUS = 'ACTIVE'
 WHERE NOT EXISTS (
       SELECT 1
         FROM fm_approval_group_member existing_member
        WHERE existing_member.TENANT_ID = @tenant_id
          AND existing_member.APPROVAL_GROUP_ID = g.APPROVAL_GROUP_ID
          AND existing_member.EMPLOYEE_ID = e.EMPLOYEE_ID
          AND existing_member.STATUS = 'ACTIVE'
       );

SET @inserted_members = ROW_COUNT();

SELECT COUNT(*) INTO @groups_after
  FROM fm_approval_group
 WHERE TENANT_ID = @tenant_id
   AND GROUP_CODE LIKE 'PURCHASE_%';

SELECT COUNT(*) INTO @members_after
  FROM fm_approval_group_member m
  JOIN fm_approval_group g
    ON g.TENANT_ID = m.TENANT_ID
   AND g.APPROVAL_GROUP_ID = m.APPROVAL_GROUP_ID
 WHERE g.TENANT_ID = @tenant_id
   AND g.GROUP_CODE LIKE 'PURCHASE_%';

SELECT @inserted_groups AS inserted_groups,
       @inserted_members AS inserted_members,
       @groups_before AS groups_before,
       @groups_after AS groups_after,
       @members_before AS members_before,
       @members_after AS members_after;

SELECT g.GROUP_CODE, g.GROUP_NAME, g.ASSIGNMENT_MODE, g.STATUS,
       COUNT(m.OID) AS ACTIVE_MEMBERS
  FROM fm_approval_group g
  LEFT JOIN fm_approval_group_member m
    ON m.TENANT_ID = g.TENANT_ID
   AND m.APPROVAL_GROUP_ID = g.APPROVAL_GROUP_ID
   AND m.STATUS = 'ACTIVE'
 WHERE g.TENANT_ID = @tenant_id
   AND g.GROUP_CODE LIKE 'PURCHASE_%'
 GROUP BY g.GROUP_CODE, g.GROUP_NAME, g.ASSIGNMENT_MODE, g.STATUS
 ORDER BY g.GROUP_CODE;

COMMIT;
