# Root Structure Guide

## Purpose

Keep repository root focused on source and infrastructure, while separating runtime artifacts and operational data.

## Top-level Directory Roles

- `module/`: Application service modules and source code.
- `infra/`: Infrastructure definitions (compose, DB init, Jenkins, etc.).
- `scripts/`: Automation and operational scripts.
- `docs/`: Team-facing documentation and conventions.

## Runtime/Data Directories (Local Only)

The directories below are local runtime/output areas and must not be tracked by Git:

- `data/`
- `logs/`
- `file/`
- `backup/`
- `var/`
- `wsl.localhost/`

## Database Backup and Secret Files

Do not version CUBRID backup dumps or key files:

- `infra/cubrid/conf/com_bk0_keys`
- `infra/cubrid/conf/db_backup_*.sql`
- `infra/cubrid/conf/db_backup_*_indexes`
- `infra/cubrid/conf/db_backup_*_objects`
- `infra/cubrid/conf/db_backup_*_schema`

## Build Artifact Policy

- Build outputs remain under each module's `target/`.
- Do not place generated JARs or temporary runtime files at repository root.

## Notes

- If runtime paths change in `docker-compose.yml` or scripts, update this guide and `.gitignore` together.
