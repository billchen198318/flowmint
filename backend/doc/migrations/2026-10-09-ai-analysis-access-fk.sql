-- Run on an existing MariaDB database before deploying the matching schema.
-- Nonzero rows from this query must be investigated; the ALTER will reject orphan references.
SELECT a.TENANT_ID, a.ANALYSIS_ID, COUNT(*) AS ORPHAN_COUNT
  FROM fm_ai_analysis_access a
  LEFT JOIN fm_ai_analysis n
    ON n.TENANT_ID = a.TENANT_ID AND n.ANALYSIS_ID = a.ANALYSIS_ID
 WHERE a.ANALYSIS_ID IS NOT NULL AND n.ANALYSIS_ID IS NULL
 GROUP BY a.TENANT_ID, a.ANALYSIS_ID;

ALTER TABLE fm_ai_analysis_access
  ADD CONSTRAINT FK_FM_AI_ACCESS_ANALYSIS
  FOREIGN KEY (TENANT_ID, ANALYSIS_ID)
  REFERENCES fm_ai_analysis (TENANT_ID, ANALYSIS_ID);
