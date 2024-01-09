package com.veeva.vault.custom.app.security;

import com.veeva.vault.custom.app.RequestUtilities;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityConfiguration {
    public static final UserDetails SECURE_USER = User.withUsername("user").password("password").authorities("ROLE_USER").build();
    @Autowired
    private IpAuthenticationProvider ipAuthenticationProvider;

    @Autowired
    RequestUtilities requestUtilities;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> {
            request.requestMatchers("/admin/**").permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll().
                    requestMatchers("/api/**").anonymous();
            }).authenticationProvider(ipAuthenticationProvider)
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable();
        return http.build();
    }
    @Bean
    HttpFirewall allowHttpMethod() {
        List<String> allowedMethods = new ArrayList<String>();
        allowedMethods.add("GET");
        allowedMethods.add("POST");
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowedHttpMethods(allowedMethods);
        return firewall;
    }

    @Bean
    WebSecurityCustomizer fireWall() {
        return (web) -> web.httpFirewall(allowHttpMethod());
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(ipAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(ipAuthenticationProvider);
    }

}