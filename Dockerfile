# syntax=docker/dockerfile:1
# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build WAR and collect runtime JARs
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Prefetch dependencies separately so this layer is cached
COPY pom.xml ./
COPY */pom.xml ./
# Hack: copy individual module poms while preserving directory structure
RUN --mount=type=cache,target=/root/.m2 \
    find . -name "pom.xml" -exec dirname {} \; | xargs -I{} mkdir -p {} 2>/dev/null; true

COPY . .

# Build WAR (skip tests; tests require a live MySQL)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests -pl superfly-web -am package

# Download JARs that are test-scoped but required at Jetty runtime
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B dependency:copy \
      -Dartifact=com.mysql:mysql-connector-j:8.2.0:jar \
      -DoutputDirectory=/build/jetty-lib && \
    ./mvnw -B dependency:copy \
      -Dartifact=org.apache.commons:commons-dbcp2:2.13.0:jar \
      -DoutputDirectory=/build/jetty-lib && \
    ./mvnw -B dependency:copy \
      -Dartifact=org.apache.commons:commons-pool2:2.12.0:jar \
      -DoutputDirectory=/build/jetty-lib

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Production image — Jetty 12 + WAR + JNDI datasource from env vars
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS production

ARG JETTY_VERSION=12.0.32
ENV JETTY_HOME=/opt/jetty
ENV JETTY_BASE=/var/lib/jetty
ENV JETTY_PORT=8080

# Install Jetty standalone
RUN apk add --no-cache curl gettext && \
    curl -fsSL "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-home/${JETTY_VERSION}/jetty-home-${JETTY_VERSION}.tar.gz" \
    | tar xz -C /opt && \
    mv /opt/jetty-home-${JETTY_VERSION} ${JETTY_HOME}

# Set up Jetty base with required modules
RUN mkdir -p ${JETTY_BASE}/webapps ${JETTY_BASE}/lib/ext && \
    cd ${JETTY_BASE} && \
    java -jar ${JETTY_HOME}/start.jar \
      --add-modules=server,http,deploy,webapp,jndi,plus,annotations,logging-jetty

# Copy WAR and extra JARs
COPY --from=builder /build/superfly-web/target/superfly.war ${JETTY_BASE}/webapps/ROOT.war
COPY --from=builder /build/jetty-lib/ ${JETTY_BASE}/lib/ext/

# Copy context descriptor and entrypoint
COPY docker/jetty/ROOT.xml ${JETTY_BASE}/webapps/ROOT.xml
COPY docker/jetty/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Non-root user
RUN addgroup -S jetty && adduser -S jetty -G jetty && \
    chown -R jetty:jetty ${JETTY_BASE}
USER jetty

EXPOSE ${JETTY_PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:${JETTY_PORT}/ >/dev/null 2>&1 || exit 1

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-jar", "/opt/jetty/start.jar"]

# ─────────────────────────────────────────────────────────────────────────────
# Stage 3: Development image — Maven + hot-reload via jetty:run
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS development
WORKDIR /app

# Pre-fetch dependencies
COPY pom.xml ./
COPY */pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B dependency:go-offline -pl superfly-web -am || true

COPY . .

EXPOSE 8080

# jetty:run uses jetty-env.conf for JNDI — override via -Djetty.jndi.* if needed
CMD ["./mvnw", "-pl", "superfly-web", "jetty:run"]
