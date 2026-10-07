# syntax=docker/dockerfile:1
# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build the executable (shaded) JAR
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
# the maven image sets MAVEN_CONFIG=/root/.m2, which mvnw appends to the arguments as a goal
ENV MAVEN_CONFIG=

# Prefetch dependencies in a layer that only depends on the poms: without sources
# test-compile only resolves dependencies and plugins (go-offline cannot resolve
# the not yet built sibling modules)
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
COPY superfly-remote-api/pom.xml superfly-remote-api/
COPY superfly-spi/pom.xml superfly-spi/
COPY superfly-service/pom.xml superfly-service/
COPY superfly-web/pom.xml superfly-web/
COPY superfly-httpclient-hc5/pom.xml superfly-httpclient-hc5/
COPY superfly-common/pom.xml superfly-common/
COPY superfly-client-web-security/pom.xml superfly-client-web-security/
COPY superfly-client-opt/pom.xml superfly-client-opt/
COPY superfly-spring-security-ee10/pom.xml superfly-spring-security-ee10/
COPY superfly-spi-support/pom.xml superfly-spi-support/
COPY superfly-crypto/pom.xml superfly-crypto/
COPY superfly-wicket/pom.xml superfly-wicket/
COPY superfly-integration-test/pom.xml superfly-integration-test/
COPY superfly-client-core/pom.xml superfly-client-core/
COPY superfly-client-ee8/pom.xml superfly-client-ee8/
COPY superfly-client-ee10/pom.xml superfly-client-ee10/
COPY superfly-spring-security-core/pom.xml superfly-spring-security-core/
COPY superfly-spring-security-ee8/pom.xml superfly-spring-security-ee8/
COPY superfly-wicket-ee8/pom.xml superfly-wicket-ee8/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests -pl superfly-web -am test-compile

COPY . .

# Build the executable JAR with Jetty, the MySQL driver and the connection pool inside
# (skip tests; tests require a live MySQL)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests -pl superfly-web -am package

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Production image — embedded Jetty 12; JNDI datasource built from env vars
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS production

ENV JETTY_PORT=8080
# DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, DB_TIMEZONE and the other JETTY_* settings are read
# from the environment by SuperflyServer (not from command-line properties: the JVM and Jetty echo
# arguments, which would print the password). The logback.xml packed into the JAR keeps
# stored procedure arguments out of the log.

# Non-root user
RUN addgroup -S superfly && adduser -S superfly -G superfly
WORKDIR /app
COPY --from=builder --chown=superfly:superfly /build/superfly-web/target/superfly.jar /app/superfly.jar
USER superfly

EXPOSE ${JETTY_PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:${JETTY_PORT}/ >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "/app/superfly.jar"]
