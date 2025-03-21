package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.client.ActionDescriptionCollector;
import com.payneteasy.superfly.client.ScanningActionDescriptionCollector;
import com.payneteasy.superfly.client.XmlActionDescriptionCollector;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.security.InsufficientAuthenticationHandlingFilter;
import com.payneteasy.superfly.security.MultiStepLoginUrlAuthenticationEntryPoint;
import com.payneteasy.superfly.security.SuperflyUsernamePasswordAuthenticationProcessingFilter;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.security.x509.X509EFailureHandler;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import com.payneteasy.superfly.web.security.SuperflyInitOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.security.SuperflyLocalOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.security.logout.SuperflyLogoutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@Import({SpringSecurityAuthenticationManagerConfiguration.class})
public class SpringSecurityConfiguration {
    private final SuperflyProperties    properties;
    private final LoggerSink            loggerSink;
    private final AuthenticationManager authenticationManager;

    public SpringSecurityConfiguration(SuperflyProperties properties, LoggerSink loggerSink, AuthenticationManager authenticationManager) {
        this.properties = properties;
        this.loggerSink = loggerSink;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")  // Обрабатываем все пути
            .authorizeHttpRequests(
                    auth ->
                            auth
                                    .requestMatchers(antPathRequestMatcher("/favicon.ico"),
                                                     antPathRequestMatcher("/css/**"),
                                                     antPathRequestMatcher("/login*"),
                                                     antPathRequestMatcher("/sso/**"),
                                                     antPathRequestMatcher("/management/version.txt")
                                    )
                                    .permitAll()
                                    .requestMatchers(antPathRequestMatcher("/remoting/sso.xremoting.service"),
                                                     antPathRequestMatcher("/remoting/sso.hessian.service")
                                    )
                                    .hasAuthority("ROLE_SUBSYSTEM")
                                    .requestMatchers(antPathRequestMatcher("/remoting/sso.service/**"),
                                                     antPathRequestMatcher("/remoting/oauth2.hessian.service"),
                                                     antPathRequestMatcher("/remoting/basic.hessian.service")
                                    )
                                    .anonymous()
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
                    .logoutUrl("/j_spring_security_logout")
                    .logoutSuccessHandler(logoutSuccessHandler()))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
        ;

        // Добавляем кастомные фильтры
        http.addFilterAt(x509AuthenticationFilter(), X509AuthenticationFilter.class)
            .addFilterAt(passwordAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(initOtpAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(otpAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(insufficientAuthenticationHandlingFilter(), UsernamePasswordAuthenticationFilter.class)
        ;

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
        filter.setAuthenticationFailureHandler(new X509EFailureHandler());
        return filter;
    }

    @Bean
    public SuperflyUsernamePasswordAuthenticationProcessingFilter passwordAuthenticationProcessingFilter() {
        SuperflyUsernamePasswordAuthenticationProcessingFilter filter = new SuperflyUsernamePasswordAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login"));
        filter.setCsrfValidator(csrfValidator());
        return filter;
    }

    @Bean
    public SuperflyLocalOTPAuthenticationProcessingFilter otpAuthenticationProcessingFilter() {
        SuperflyLocalOTPAuthenticationProcessingFilter filter = new SuperflyLocalOTPAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login"));
        filter.setCsrfValidator(csrfValidator());
        return filter;
    }

    @Bean
    public SuperflyInitOTPAuthenticationProcessingFilter initOtpAuthenticationProcessingFilter() {
        SuperflyInitOTPAuthenticationProcessingFilter filter = new SuperflyInitOTPAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login"));
        filter.setCsrfValidator(csrfValidator());
        return filter;
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
                "com.payneteasy.superfly.demo.web.wicket",
                "com.payneteasy.superfly.web.wicket",
        });
        collector.setAnnotationClass(Secured.class);
        return collector;
    }


    @Bean
    public ActionDescriptionCollector xmlActionDescriptionCollector() {
        XmlActionDescriptionCollector collector = new XmlActionDescriptionCollector();
        collector.setResource(new ClassPathResource("actions.xml"));
        return collector;
    }
}
