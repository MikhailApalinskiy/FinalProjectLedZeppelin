package com.finalProjectLedZeppelin.config;

import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import com.finalProjectLedZeppelin.common.error.ApiError;
import com.finalProjectLedZeppelin.common.logging.RequestIdMdcFilter;
import com.finalProjectLedZeppelin.common.logging.UserIdMdcFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Log4j2
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("PasswordEncoder configured: BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RequestIdMdcFilter requestIdMdcFilter() {
        log.info("Security filter configured: RequestIdMdcFilter");
        return new RequestIdMdcFilter();
    }

    @Bean
    public UserIdMdcFilter userIdMdcFilter() {
        log.info("Security filter configured: UserIdMdcFilter");
        return new UserIdMdcFilter();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            ObjectMapper objectMapper,
            RequestIdMdcFilter requestIdMdcFilter,
            UserIdMdcFilter userIdMdcFilter
    ) {
        log.info("SecurityFilterChain building (stateless=true, publicPaths=[/api/auth/**, /actuator/health])");
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtService);
        log.debug("Security filters order: RequestIdMdcFilter -> JwtAuthFilter -> UserIdMdcFilter");
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) ->
                                writeApiError(response, objectMapper, HttpStatus.UNAUTHORIZED, "Unauthorized", request.getRequestURI())
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeApiError(response, objectMapper, HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI())
                        )
                )
                .addFilterBefore(requestIdMdcFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userIdMdcFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static void writeApiError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
        response.flushBuffer();
    }
}
