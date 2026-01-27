Fix scripts for wx / bht / rzt_db

Overview

This folder contains SQL scripts to detect and fix schema incompatibilities related to `medicine.medicine_id` across three tenant databases: `wx`, `bht`, and `rzt_db`.

Files
- wx_fix.sql: recommended change set for `wx` (detection + conversion + verification)
- bht_fix.sql: recommended change set for `bht`
- rzt_db_fix.sql: recommended change set for `rzt_db`
- generate_drop_fks.sql: helper script that prints ALTER TABLE ... DROP FOREIGN KEY ... statements for candidate FKs that must be dropped before conversions

Safety notes
1) Always backup before applying any DDL: `mysqldump -uroot -p --single-transaction --set-gtid-purged=OFF <db> > <db>_backup.sql`
2) Run scripts on a staging/test DB first.
3) Altering column types and converting collations may take time and incur locks on big tables; prefer low-traffic windows.

Suggested workflow (PowerShell commands)

1) Generate candidate drop-FK statements (review before executing):

```powershell
mysql -uroot -p < db/fix-scripts/generate_drop_fks.sql > db/fix-scripts/drop_fks_candidates.sql

# Inspect the generated file
Get-Content .\db\fix-scripts\drop_fks_candidates.sql
```

2) Backup database(s):

```powershell
mysqldump -uroot -p --single-transaction --set-gtid-purged=OFF wx > .\wx_backup.sql
mysqldump -uroot -p --single-transaction --set-gtid-purged=OFF bht > .\bht_backup.sql
mysqldump -uroot -p --single-transaction --set-gtid-purged=OFF rzt_db > .\rzt_db_backup.sql
```

3) After reviewing `drop_fks_candidates.sql`, if you decide to drop FKs, run (example):

```powershell
# run interactively to confirm
Get-Content .\db\fix-scripts\drop_fks_candidates.sql | mysql -uroot -p
```

4) Run the tenant fix script (example for wx):

```powershell
# Run the script step by step, or run entire file in one go if tested
mysql -uroot -p < db/fix-scripts/wx_fix.sql
```

5) Recreate any needed FKs if not auto-created by the app, then verify:

```powershell
mysql -uroot -p -e "SELECT TABLE_NAME, CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_NAME='medicine' AND REFERENCED_COLUMN_NAME='medicine_id' AND TABLE_SCHEMA='wx';"
```

If you want, I can also produce an interactive PowerShell script that:
- Runs `generate_drop_fks.sql` and shows the candidate drops,
- Prompts for confirmation before dropping FKs,
- Runs per-table ALTER statements with confirmations,
- Produces a final verification report.

Reply with "interactive" if you want that; otherwise run the scripts in test environment and paste back any errors or outputs you want me to interpret.
