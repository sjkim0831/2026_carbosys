# CUBRID Docker 실행 가이드

## 최초 설정 (한 번만)

### 1. 설정 파일 생성

`infra/cubrid/conf/cubrid.conf`:
```conf
[%service]
service=server,broker

[%server]
master_port_id=1534

[%client]
```

`infra/cubrid/conf/cubrid_broker.conf`:
```conf
[%broker_name]
BROKER_PORT             =33000
MIN_NUM_APPL_SERVERS    =5
MAX_NUM_APPL_SERVERS    =40
ACCESS_MODE            =RW
```

### 2. docker-compose.yml 수정

```yaml
services:
  cubrid:
    environment:
      CUBRID_DB: com
      CUBRID_LOCALE: ko_KR.utf8
      CUBRID_COMPONENTS: ALL  # 중요: 브로커 자동 시작
    volumes:
      - "./infra/cubrid/init:/docker-entrypoint-initdb.d"
      - "./infra/cubrid/conf/cubrid.conf:/opt/carbosys/conf/cubrid.conf"
      - "./infra/cubrid/conf/cubrid_broker.conf:/opt/carbosys/conf/cubrid_broker.conf"
      - "cubrid-data:/opt/carbosys/data"
    ports:
      - "33000:33000"
```

---

## /opt/data 폴더 사용 시 (init 대신)

```yaml
services:
  cubrid:
    environment:
      CUBRID_DB: com
      CUBRID_LOCALE: ko_KR.utf8
      CUBRID_COMPONENTS: ALL
      CUBRID_DATABASES: /opt/data  # 변경
    volumes:
      - "./infra/cubrid/init:/docker-entrypoint-initdb.d"
      - "./infra/cubrid/conf/cubrid.conf:/opt/carbosys/conf/cubrid.conf"
      - "./infra/cubrid/conf/cubrid_broker.conf:/opt/carbosys/conf/cubrid_broker.conf"
      - "./data:/opt/data"  # 호스트 폴더 마운트 (이전 데이터 보존)
      - "cubrid-data:/opt/carbosys/data"
```

### 실행
```bash
mkdir -p data
docker compose down
docker compose up -d
```

> 주의: `./data` 폴더에 이전 데이터가 있으면 `init` 스크립트가 실행되지 않음 (이미 DB가 존재하기 때문)
> 처음부터 다시 시작하려면 `docker compose down -v` 후 실행

```bash
docker compose up -d
```

## DBeaver 접속 정보

- Host: `localhost`
- Port: `33000`
- Database: `com`
- User: `dba`
- Password: (비어있음)
- JDBC URL: `jdbc:cubrid:localhost:33000:com:::?charset=UTF-8`

## 문제 발생 시

브로커가 안 켜지면:
```bash
docker exec cubrid cubrid broker start
```

볼륨 초기화 (데이터 삭제):
```bash
docker compose down -v && docker compose up -d
```
