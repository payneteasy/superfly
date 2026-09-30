package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.service.LoggerSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Централизованный PCI DSS audit trail для двух каналов аутентификации subsystem-ов:
 *
 * <ul>
 *   <li><b>SUBSYSTEM_AUTH</b> — header-based: {@code X-Subsystem-Name} + {@code X-Subsystem-Token}
 *       (обрабатывается {@link SubsystemAuthenticationProvider})</li>
 *   <li><b>X509_AUTH</b> — client certificate: CN сертификата проверяется против БД
 *       (обрабатывается {@code X509PreAuthenticatedAuthenticationProvider})</li>
 * </ul>
 *
 * <p>Spring Security публикует {@link AbstractAuthenticationEvent} автоматически после
 * каждого вызова {@code AuthenticationManager.authenticate()}, поэтому слушатель даёт
 * единую точку аудита без изменения фильтров или провайдеров.</p>
 *
 * <p>Каждое событие пишется в два канала:
 * <ol>
 *   <li>{@code log.info/warn} — попадает в обычный application log (видно ops-команде)</li>
 *   <li>{@link LoggerSink#info} — попадает в отдельный structured audit log
 *       (PCI DSS 10.2.1: требует отдельного, защищённого от изменения audit trail)</li>
 * </ol>
 * </p>
 */
@Component
public class SecurityAuditApplicationListener implements ApplicationListener<AbstractAuthenticationEvent> {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditApplicationListener.class);

    private final LoggerSink loggerSink;

    public SecurityAuditApplicationListener(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        Authentication auth = event.getAuthentication();

        // Аудируем только наши два канала; форм-логин и остальные провайдеры — вне scope
        if (!isAuditedType(auth)) {
            return;
        }

        String eventType = resolveEventType(auth);
        String identity  = extractIdentity(auth);
        String sourceIp  = resolveSourceIp();

        if (event instanceof AuthenticationSuccessEvent) {
            log.info("Auth success type={} identity={} ip={}", eventType, identity, sourceIp);
            loggerSink.info(log, eventType, true, identity);
        } else if (event instanceof AbstractAuthenticationFailureEvent failure) {
            log.warn("Auth failure type={} identity={} ip={} cause={}",
                    eventType, identity, sourceIp, failure.getException().getMessage());
            loggerSink.info(log, eventType, false, identity);
        }
    }

    private boolean isAuditedType(Authentication auth) {
        return auth instanceof SubsystemAuthenticationToken
                || auth instanceof PreAuthenticatedAuthenticationToken;
    }

    private String resolveEventType(Authentication auth) {
        if (auth instanceof SubsystemAuthenticationToken) return "SUBSYSTEM_AUTH";
        if (auth instanceof PreAuthenticatedAuthenticationToken) return "X509_AUTH";
        return "AUTH";
    }

    private String extractIdentity(Authentication auth) {
        if (auth instanceof SubsystemAuthenticationToken) {
            return "subsystem=" + auth.getName();
        }
        // X509: Spring передаёт полный SubjectDN ("CN=billing, O=Acme, C=US") —
        // логируем только CN, чтобы не утекала оргинформация из сертификата
        return "cn=" + extractCn(auth.getName());
    }

    private String extractCn(String dn) {
        if (dn == null) return "unknown";
        String upper = dn.toUpperCase();
        int idx = upper.indexOf("CN=");
        if (idx < 0) return dn;
        int end = dn.indexOf(',', idx);
        String cn = end < 0 ? dn.substring(idx + 3) : dn.substring(idx + 3, end);
        return cn.trim();
    }

    private String resolveSourceIp() {
        try {
            // RequestContextHolder заполнен только в request-потоке (не в async/scheduled)
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest().getRemoteAddr() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
