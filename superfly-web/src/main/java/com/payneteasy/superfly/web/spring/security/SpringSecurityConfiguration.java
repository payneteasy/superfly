package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.client.ActionDescriptionCollector;
import com.payneteasy.superfly.client.ScanningActionDescriptionCollector;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.security.InsufficientAuthenticationHandlingFilter;
import com.payneteasy.superfly.security.MultiStepLoginUrlAuthenticationEntryPoint;
import com.payneteasy.superfly.security.SuperflyUsernamePasswordAuthenticationProcessingFilter;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import com.payneteasy.superfly.web.security.SubsystemAuthenticationFilter;
import com.payneteasy.superfly.web.security.SuperflyInitOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.security.SuperflyLocalOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.security.handler.JsonAuthenticationFailureHandler;
import com.payneteasy.superfly.web.security.logout.SuperflyLogoutSuccessHandler;
import com.payneteasy.superfly.web.security.ratelimit.LoginAttemptLimiter;
import com.payneteasy.superfly.web.security.ratelimit.LoginRateLimitFilter;
import com.payneteasy.superfly.service.impl.SubsystemOriginCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Import({SpringSecurityAuthenticationManagerConfiguration.class})
public class SpringSecurityConfiguration {
    private final SuperflyProperties    properties;
    private final LoggerSink            loggerSink;
    private final AuthenticationManager authenticationManager;
    private final LocalSecurityService localSecurityService;

    public SpringSecurityConfiguration(SuperflyProperties properties, LoggerSink loggerSink, AuthenticationManager authenticationManager,
                                       LocalSecurityService localSecurityService) {
        this.properties = properties;
        this.loggerSink = loggerSink;
        this.authenticationManager = authenticationManager;
        this.localSecurityService = localSecurityService;
    }

    private static SubsystemOriginCache.Urls subsystemUrls(ObjectProvider<SubsystemOriginCache> originCache) {
        SubsystemOriginCache cache = originCache.getIfAvailable();
        return cache == null ? SubsystemOriginCache.Urls.EMPTY : cache.getUrls();
    }

    /**
     * TLS is terminated by a reverse proxy, so the request seen here is plain http; the default writer would
     * send nothing. Browsers ignore the header over http, so sending it always is harmless.
     */
    private static void alwaysSendHsts(HeadersConfigurer<HttpSecurity> headers) {
        headers.httpStrictTransportSecurity(hsts -> hsts
                .requestMatcher(AnyRequestMatcher.INSTANCE)
                .maxAgeInSeconds(31536000)
                .includeSubDomains(false));
    }

    /**
     * Subsystem RPC is authenticated by X-Subsystem-* headers on every request. It must not create an
     * HttpSession: otherwise the returned JSESSIONID would keep ROLE_SUBSYSTEM without the headers.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain remotingSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(antPathRequestMatcher("/remoting/sso.service/**"))
            .headers(SpringSecurityConfiguration::alwaysSendHsts)
            .authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority("ROLE_SUBSYSTEM"))
            // RPC clients get a status, not a redirect to the login form.
            .exceptionHandling(httpSecurity -> httpSecurity.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(securityContext ->
                    securityContext.securityContextRepository(new RequestAttributeSecurityContextRepository()))
            // Token-based auth, no cookies: see the note on the main chain.
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterAt(x509AuthenticationFilter(), X509AuthenticationFilter.class)
            .addFilterBefore(subsystemAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<SubsystemOriginCache> originCache) throws Exception {
        http.securityMatcher("/**")  // Обрабатываем все пути
            .headers(headers -> {
                alwaysSendHsts(headers);
                headers
                // X-Content-Type-Options, X-Frame-Options: DENY by Spring Security defaults.
                // CSP: unsafe-inline required for Wicket/jQuery inline scripts; all assets served locally.
                // Subsystem origins are added to form-action/style-src (login redirects, custom login CSS).
                .addHeaderWriter(new SubsystemCspHeaderWriter(() -> subsystemUrls(originCache)))
                // Keeps SSO tokens and target URLs out of Referer on outgoing navigation.
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.SAME_ORIGIN));
            })
            .authorizeHttpRequests(
                    auth ->
                            auth
                                    // rest-api servlet: only remote-auth endpoints (they check the
                                    // subsystem bearer token themselves). Must precede /sso/** permitAll,
                                    // otherwise anything mapped under /sso/check/ becomes public.
                                    .requestMatchers(antPathRequestMatcher("/sso/check/check-password/**"),
                                                     antPathRequestMatcher("/sso/check/check-otp/**"))
                                    .permitAll()
                                    .requestMatchers(antPathRequestMatcher("/sso/check/**"))
                                    .denyAll()
                                    // SSO pages are mounted or reached through page instances, never by class name;
                                    // the SSO Wicket app refuses non-SSO pages too (defense in depth).
                                    .requestMatchers(antPathRequestMatcher("/sso/wicket/bookmarkable/**"))
                                    .denyAll()
                                    .requestMatchers(antPathRequestMatcher("/favicon.ico"),
                                                     antPathRequestMatcher("/css/**"),
                                                     antPathRequestMatcher("/login*"),
                                                     antPathRequestMatcher("/sso/**"),
                                                     antPathRequestMatcher("/management/version.txt")
                                    )
                                    .permitAll()
                                    .requestMatchers(antPathRequestMatcher("/remoting/sso.service/**"))
                                    .hasAuthority("ROLE_SUBSYSTEM")
                                    .anyRequest()
                                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_ACTION_TEMP_PASSWORD"))
            .exceptionHandling(httpSecurity ->
                                       httpSecurity.authenticationEntryPoint(authenticationEntryPoint())
            )
            .securityContext(securityContext -> {
                securityContext.securityContextRepository(new HttpSessionSecurityContextRepository());
                securityContext.requireExplicitSave(false);
            })
            .logout(logout -> logout
                    // logout changes state, so it is accepted only as POST
                    .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults()
                            .matcher(HttpMethod.POST, "/j_spring_security_logout"))
                    .logoutSuccessHandler(logoutSuccessHandler()))
            // CSRF disabled here intentionally: state-changing REST endpoints use token-based auth
            // (X-Subsystem-Token or Authorization: Bearer), not cookies. Wicket pages are protected by
            // SameOriginResourceIsolationPolicy (BaseApplication); the login forms by CsrfValidator.
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
        ;

        // Добавляем кастомные фильтры
        http.addFilterAt(x509AuthenticationFilter(), X509AuthenticationFilter.class)
            .addFilterAfter(loginRateLimitFilter(), X509AuthenticationFilter.class)
            .addFilterBefore(subsystemAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(passwordAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(initOtpAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(otpAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
        ;
        // Must run after ExceptionTranslationFilter (it turns the exception into the step-specific redirect)
        // and before AuthorizationFilter. Public paths are skipped, as they were security="none" in 1.7.x.
        http.addFilterBefore(new SkipMatchingRequestsFilter(
                insufficientAuthenticationHandlingFilter(),
                new OrRequestMatcher(
                        antPathRequestMatcher("/favicon.ico"),
                        antPathRequestMatcher("/css/**"),
                        antPathRequestMatcher("/login*"),
                        antPathRequestMatcher("/sso/**"),
                        antPathRequestMatcher("/management/version.txt"))),
                AuthorizationFilter.class);

        return http.build();
    }

    private AntPathRequestMatcher antPathRequestMatcher(String path) {
        return new AntPathRequestMatcher(path);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        MultiStepLoginUrlAuthenticationEntryPoint result = new MultiStepLoginUrlAuthenticationEntryPoint("/login");
        result.setInsufficientAuthenticationMapping(
                Map.of(
                        UsernamePasswordAuthenticationToken.class, "/login-step2",
                        LocalNeedOTPToken.class, "/login-setup"
                ));
        return result;
    }


    @Bean
    public X509AuthenticationFilter x509AuthenticationFilter() {
        X509AuthenticationFilter filter = new X509AuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        filter.setAuthenticationFailureHandler(new JsonAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public SubsystemAuthenticationFilter subsystemAuthenticationFilter() {
        SubsystemAuthenticationFilter filter = new SubsystemAuthenticationFilter(
                antPathRequestMatcher("/remoting/sso.service/**"),
                authenticationManager
        );
        filter.setSuccessHandler((request, response, authentication) -> {});
        filter.setFailureHandler(new JsonAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public LoginAttemptLimiter loginAttemptLimiter() {
        Integer ipLimit = properties.loginIpLimit();
        return LoginAttemptLimiter.install(ipLimit == null ? LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP : ipLimit);
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter() {
        return new LoginRateLimitFilter(loginAttemptLimiter());
    }

    private static AuthenticationFailureHandler loginFailureHandler() {
        return LoginRateLimitFilter.recordingFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login"));
    }

    @Bean
    public SuperflyUsernamePasswordAuthenticationProcessingFilter passwordAuthenticationProcessingFilter() {
        SuperflyUsernamePasswordAuthenticationProcessingFilter filter = new SuperflyUsernamePasswordAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(loginFailureHandler());
        filter.setCsrfValidator(csrfValidator());
        changeSessionIdOnLogin(filter);
        return filter;
    }

    @Bean
    public SuperflyLocalOTPAuthenticationProcessingFilter otpAuthenticationProcessingFilter() {
        SuperflyLocalOTPAuthenticationProcessingFilter filter = new SuperflyLocalOTPAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(loginFailureHandler());
        filter.setCsrfValidator(csrfValidator());
        changeSessionIdOnLogin(filter);
        return filter;
    }

    @Bean
    public SuperflyInitOTPAuthenticationProcessingFilter initOtpAuthenticationProcessingFilter() {
        SuperflyInitOTPAuthenticationProcessingFilter filter = new SuperflyInitOTPAuthenticationProcessingFilter();
        filter.setLocalSecurityService(localSecurityService);
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(loginFailureHandler());
        filter.setCsrfValidator(csrfValidator());
        changeSessionIdOnLogin(filter);
        return filter;
    }

    /**
     * The filters are wired by hand, so Spring Security does not give them a session fixation strategy
     * (the default does nothing). The id is changed on every step; session attributes, including the
     * login CSRF token, are kept.
     */
    private static void changeSessionIdOnLogin(AbstractAuthenticationProcessingFilter filter) {
        filter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
    }

    @Bean
    public InsufficientAuthenticationHandlingFilter insufficientAuthenticationHandlingFilter() {
        InsufficientAuthenticationHandlingFilter filter = new InsufficientAuthenticationHandlingFilter();
        filter.setInsufficientAuthenticationClasses(new Class[]{CompoundAuthentication.class});
        return filter;
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = List.of(
                new RoleVoter(),
                new AuthenticatedVoter(),
                new WebExpressionVoter()
        );
        return new AffirmativeBased(decisionVoters);
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        SuperflyLogoutSuccessHandler handler = new SuperflyLogoutSuccessHandler("/");
        handler.setLoggerSink(loggerSink);
        return handler;
    }

    @Bean
    public HttpFirewall customHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    @Bean
    public CsrfValidator csrfValidator() {
        return new CsrfValidatorImpl(properties.csrfLoginValidatorEnable());
    }

    @Bean
    public ActionDescriptionCollector scanningActionDescriptionCollector() {
        ScanningActionDescriptionCollector collector = new ScanningActionDescriptionCollector();
        collector.setBasePackages(new String[]{
                "com.payneteasy.superfly.web.wicket",
        });
        collector.setAnnotationClass(Secured.class);
        return collector;
    }


}
