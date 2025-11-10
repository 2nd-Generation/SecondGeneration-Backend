# 1. Build Stage: Java 21 JDK와 Gradle을 사용하여 앱을 빌드합니다.
FROM eclipse-temurin:21-jdk-jammy AS build-stage

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼 파일 복사
COPY gradlew .
COPY gradle ./gradle

# build.gradle, settings.gradle 파일 복사 (의존성 캐싱을 위해)
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --quiet

# 소스 코드 전체 복사
COPY src ./src

# Gradle 빌드 실행
# (테스트는 CI/CD 파이프라인에서 실행하는 것을 권장하므로 여기서는 스킵)
RUN ./gradlew build -x test --no-daemon

# 2. Final Stage: JRE 21 이미지를 사용하여 실제 앱을 실행합니다.
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Build Stage에서 빌드된 JAR 파일만 복사
# 프로젝트 이름(settings.gradle)과 버전(build.gradle)을 기반으로 JAR 파일 경로 지정
COPY --from=build-stage /app/build/libs/coreclass-0.0.1-SNAPSHOT.jar app.jar

# ------------------------------------------------------------------
# 💡 중요: DB 설정을 외부에서 주입받기 위한 환경 변수
# application.properties의 localhost 설정을 덮어씁니다.
ENV SPRING_DATASOURCE_URL="second-generation-backend-db.c1wmgymweu6m.ap-northeast-2.rds.amazonaws.com"
ENV SPRING_DATASOURCE_USERNAME="admin"
ENV SPRING_DATASOURCE_PASSWORD="admin123!"
# ------------------------------------------------------------------

# 애플리케이션 포트 (Spring Boot 기본값 8080)
EXPOSE 8080

# 컨테이너 시작 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]