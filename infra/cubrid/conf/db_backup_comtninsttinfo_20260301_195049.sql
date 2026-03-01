-- COMTNINSTTINFO schema normalization backup
-- Generated at: 2026-03-01T10:51:20Z

ALTER TABLE COMTNINSTTINFO ADD COLUMN CHARGER_NM VARCHAR(100);
ALTER TABLE COMTNINSTTINFO ADD COLUMN CHARGER_EMAIL VARCHAR(100);
ALTER TABLE COMTNINSTTINFO ADD COLUMN CHARGER_TEL VARCHAR(20);

-- verification
SELECT attr_name, data_type
FROM db_attribute
WHERE class_name='comtninsttinfo'
  AND attr_name IN ('charger_nm','charger_email','charger_tel')
ORDER BY attr_name;
