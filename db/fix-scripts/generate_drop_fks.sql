-- generate_drop_fks.sql
-- 运行此脚本会在 stdout 输出需要先 DROP 的外键语句（按 DATA_TYPE 或 COLLATION 不匹配检测）
-- 用法示例（PowerShell）：
-- mysql -uroot -p < db/fix-scripts/generate_drop_fks.sql > db/fix-scripts/drop_fks_candidates.sql

SELECT CONCAT('ALTER TABLE `',k.TABLE_SCHEMA,'`.`',k.TABLE_NAME,'` DROP FOREIGN KEY `',k.CONSTRAINT_NAME,'`;') AS drop_stmt,
       k.TABLE_SCHEMA,k.TABLE_NAME,k.CONSTRAINT_NAME,
       cc.DATA_TYPE AS referencing_data_type, IFNULL(cc.COLLATION_NAME,'') AS referencing_collation,
       rc.DATA_TYPE AS referenced_data_type, IFNULL(rc.COLLATION_NAME,'') AS referenced_collation
FROM information_schema.KEY_COLUMN_USAGE k
JOIN information_schema.COLUMNS rc ON rc.TABLE_SCHEMA=k.REFERENCED_TABLE_SCHEMA AND rc.TABLE_NAME=k.REFERENCED_TABLE_NAME AND rc.COLUMN_NAME=k.REFERENCED_COLUMN_NAME
JOIN information_schema.COLUMNS cc ON cc.TABLE_SCHEMA=k.TABLE_SCHEMA AND cc.TABLE_NAME=k.TABLE_NAME AND cc.COLUMN_NAME=k.COLUMN_NAME
WHERE k.REFERENCED_TABLE_NAME='medicine' AND k.REFERENCED_COLUMN_NAME='medicine_id'
  AND k.TABLE_SCHEMA IN ('wx','bht','rzt_db')
  AND (cc.DATA_TYPE<>rc.DATA_TYPE OR IFNULL(cc.COLLATION_NAME,'')<>IFNULL(rc.COLLATION_NAME,''))
ORDER BY k.TABLE_SCHEMA, k.TABLE_NAME;

