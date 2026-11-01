# ===================================================================
# Kkumz Server Lightweight JRE Runtime (Pre-built JAR Execution)
# ===================================================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata fontconfig ttf-dejavu curl && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

COPY Server/kkumz-server-admin/target/kkumz-server-admin-*.jar /app/app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1536m -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev} -jar /app/app.jar"]
