package com.sampoom.user.common.config.security;

import com.sampoom.user.common.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // CodeQL [java/spring-disabled-csrf-protection]: suppress - Stateless JWT API라 CSRF 불필요
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Feign용 POST만 허용
                        .requestMatchers(HttpMethod.POST, "/internal/**").hasAuthority("SVC_AUTH")
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/invitations").permitAll()
                        .anyRequest().hasAuthority("ROLE_USER")
                )
                // 기본 폼 로그인 비활성화
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                // 세션 미사용 명시
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CORS 허용 설정
//                .cors(cors -> cors.configurationSource(request -> {
//                    var corsConfig = new CorsConfiguration();
//                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//                    corsConfig.setAllowedOrigins(List.of("https://sampoom.store"
//                            ,"https://samsam.autos"
//                            ,"https://sampoom-management-frontend.vercel.app"
//                            ,"http://localhost:8080"
//                            ,"http://localhost:3000"
//                    ));
//                    corsConfig.setAllowCredentials(true);
//                    corsConfig.setExposedHeaders(List.of("Authorization"));
//                    corsConfig.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Client-Type"));
//                    return corsConfig;
//                }))
//                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex -> ex
//                        // 인증 실패(UnauthorizedException 포함) 시 401 반환
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.setContentType("application/json;charset=UTF-8");
//                        })
//                );
        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 자동 보안 설정 해제
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("UserDetailsService는 사용하지 않습니다.");
        };
    }
}
