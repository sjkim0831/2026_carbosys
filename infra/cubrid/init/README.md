# CUBRID Init Scripts

`docker-compose.yml`의 CUBRID 컨테이너는 이 디렉터리를 `/docker-entrypoint-initdb.d`로 마운트합니다.

- 초기화 시 실행할 SQL 또는 스크립트를 이 위치에 추가하세요.
- 예: `001_schema.sql`, `010_seed_data.sql`
