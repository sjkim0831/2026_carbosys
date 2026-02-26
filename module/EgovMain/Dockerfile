# 1단계: 빌드 환경 (Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# 1. 의존성 캐싱을 위해 pom.xml 먼저 복사
COPY pom.xml .
# 2. 소스 코드 복사
COPY src ./src

# 3. 패키징 (테스트 제외)
RUN mvn clean package -DskipTests

# 2단계: 실행 환경 (JRE)
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일 복사
# pom.xml에 <finalName>EgovMain</finalName>이 설정되어 있으므로 EgovMain.jar가 생성됨
COPY --from=build /app/target/EgovMain.jar app.jar

# 컨테이너 실행 명령
# -Duser.timezone=Asia/Seoul 등을 추가할 수 있음
ENTRYPOINT ["java", "-jar", "app.jar"]
