-- COMTNINSTTINFO schema backup/migration
-- Purpose: apply missing columns on target DB before app deployment

ALTER TABLE COMTNINSTTINFO ADD COLUMN CHARGER_NM VARCHAR(100);
ALTER TABLE COMTNINSTTINFO ADD COLUMN CHARGER_EMAIL VARCHAR(100);
ALTER TABLE COMTNINSTTINFO ADD COLUMN CHARGER_TEL VARCHAR(20);
ALTER TABLE COMTNINSTTINFO ADD COLUMN RJCT_PNTTM DATETIME;
ALTER TABLE COMTNINSTTINFO ADD COLUMN RJCT_RSN VARCHAR(1000);

-- verification
SELECT attr_name, data_type, prec
FROM db_attribute
WHERE class_name='comtninsttinfo'
  AND attr_name IN ('charger_nm','charger_email','charger_tel','rjct_pnttm','rjct_rsn')
ORDER BY attr_name;
