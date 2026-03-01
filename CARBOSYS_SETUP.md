# CUBRID Docker 실행 가이드

## 핵심 변경 사항

- DB 파일 저장 위치를 프로젝트 상대경로(`./data`)가 아니라 **WSL 절대경로(`/opt/carbosys/data`)** 기준으로 고정했습니다.
- `docker compose down` / `up -d` 반복 시에도 `/opt/carbosys/data`가 유지되면 DB가 초기화되지 않습니다.
- 브로커는 `CUBRID_COMPONENTS=ALL` + `cubrid.conf(service=server,broker)` 기준으로 자동 기동됩니다.
- **외부 접속 지원**: 브로커(33000) 외에도 실제 데이터 처리를 담당하는 **CAS 포트(33001)**가 외부로 노출되어야 합니다.

---

## 왜 `./data`가 아닌 `/opt/carbosys/data`를 쓰는가

리포지토리 위치가 바뀌거나(`git pull`, 폴더 이동, 다른 경로에서 실행) compose 실행 기준 경로가 달라지면 `./data`가 다른 폴더를 가리킬 수 있습니다.

현재 실제 데이터 경로가 `\\wsl$\Ubuntu\opt\carbosys\data`라면, WSL 내부 경로는 `/opt/carbosys/data`이므로 이 절대경로를 bind mount로 직접 지정해야 기존 데이터를 안정적으로 재사용할 수 있습니다.

---

## 최초 설정 (한 번만)

### 1) 폴더 생성

```bash
mkdir -p infra/cubrid/conf infra/cubrid/init /opt/carbosys/data
chmod 777 /opt/carbosys/data
```

### 2) 설정 파일 확인

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
APPL_SERVER_PORT       =33001
```

### 3) docker-compose 핵심 설정

```yaml
services:
  cubrid:
    environment:
      CUBRID_DB: com
      CUBRID_LOCALE: ko_KR.utf8
      CUBRID_COMPONENTS: ALL
      CUBRID_DATABASES: /opt/carbosys/data
    volumes:
      - "./infra/cubrid/init:/docker-entrypoint-initdb.d"
      - "./infra/cubrid/conf/cubrid.conf:/opt/carbosys/conf/cubrid.conf"
      - "./infra/cubrid/conf/cubrid_broker.conf:/opt/carbosys/conf/cubrid_broker.conf"
      - "${CUBRID_DATA_DIR:-/opt/carbosys/data}:/opt/carbosys/data"
    ports:
      - "33000:33000"
      - "33001:33001"
```

---

## 실행

```bash
docker compose down
docker compose up -d
```

---

## DBeaver 접속 정보

- Host: `localhost`
- Port: `33000`
- Database: `com`
- User: `dba`
- Password: (비어있음)
- JDBC URL: `jdbc:cubrid:localhost:33000:com:::?charset=UTF-8`

---

## 문제 발생 시 점검 순서

1. 컨테이너/포트 상태 확인
```bash
docker compose ps
```

2. 브로커 기동 로그 확인
```bash
docker compose logs cubrid | tail -n 200
```

3. 컨테이너 내부에서 DB 파일 확인 (`/opt/carbosys/data/com*` 존재 여부)
```bash
docker exec cubrid ls -al /opt/carbosys/data
```

4. 호스트의 실제 데이터 폴더 확인 (WSL 기준)
```bash
ls -al /opt/carbosys/data
```

5. 브로커 수동 기동(긴급 조치)
```bash
docker exec cubrid cubrid broker start
```

6. **Failed to connect... 에러 발생 시**
- `33001` 포트가 `docker-compose.yml`에 노출되어 있는지 확인하십시오.
- `cubrid_broker.conf`에 `APPL_SERVER_PORT = 33001`이 설정되어 있는지 확인하십시오.

---

## Project Hub 연동 메타

다중 프로젝트 관리 프로그램(`project-hub`)에서 이 프로젝트를 자동 인식하려면 아래 파일을 기준으로 연결합니다.

- `/opt/carbosys/ops-project.yml`
- `/opt/carbosys/.ops-control/project.json`

프로젝트를 복사해 새 프로젝트를 만들 때는 다음 값만 새 프로젝트명/경로에 맞게 변경하면 됩니다.

- `project.id`, `project.name`
- `paths.projectRoot`, `paths.moduleRoot`
- `paths.containerProjectPath`
- `.ops-control/project.json`의 `projectId`, `projectName`, `rootPath`, `modulePath`, `containerProjectPath`
