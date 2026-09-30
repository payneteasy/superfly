# syntax=docker/dockerfile:1
# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build WAR and collect runtime JARs
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
      -DoutputDirectory=/build/jetty-lib && \
    ./mvnw -B dependency:copy \
      -Dartifact=commons-logging:commons-logging:1.3.5:jar \
      -DoutputDirectory=/build/jetty-lib

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Production image — Jetty 12 + WAR + JNDI datasource from env vars
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS production

ARG JETTY_VERSION=12.0.36
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
      --add-modules=server,http,ee10-deploy,ee10-webapp,ee10-annotations,ee10-plus,ee10-jndi,ext,logging-jetty

# Copy WAR and extra JARs
COPY --from=builder /build/superfly-web/target/superfly.war ${JETTY_BASE}/webapps/ROOT.war
COPY --from=builder /build/jetty-lib/ ${JETTY_BASE}/lib/ext/

# Copy context descriptor and entrypoint
COPY docker/jetty/ROOT.xml ${JETTY_BASE}/webapps/ROOT.xml
# web.xml has no resource-ref, and Jetty binds a webapp-scoped Resource to
# java:comp/env only when one is declared
# (kept out of webapps/: every .xml there is deployed as a context descriptor)
COPY <<'EOF' ${JETTY_BASE}/etc/override-web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee" version="3.0">
    <resource-ref>
        <res-ref-name>jdbc/superfly</res-ref-name>
        <res-type>javax.sql.DataSource</res-type>
        <res-auth>Container</res-auth>
    </resource-ref>
</web-app>
EOF
COPY docker/jetty/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Non-root user
RUN addgroup -S jetty && adduser -S jetty -G jetty && \
    chown -R jetty:jetty ${JETTY_BASE}
USER jetty
# start.jar takes jetty.base from the working directory
WORKDIR ${JETTY_BASE}

EXPOSE ${JETTY_PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:${JETTY_PORT}/ >/dev/null 2>&1 || exit 1

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-jar", "/opt/jetty/start.jar"]
