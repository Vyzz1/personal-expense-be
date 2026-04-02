FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM build AS builder
WORKDIR /app
RUN java -Djarmode=layertools -jar target/*.jar extract

FROM eclipse-temurin:21-jdk-alpine AS jre-builder
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,jdk.jfr,jdk.management,jdk.unsupported,jdk.crypto.ec \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-2 \
    --output /javaruntime

FROM alpine:3.19
WORKDIR /app

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-builder /javaruntime $JAVA_HOME

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8080

ARG ACTIVE_PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=${ACTIVE_PROFILE}

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
