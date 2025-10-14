package com.sampoom.backend.user.config;

import com.sampoom.backend.user.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        // CodeQL [java/spring-disabled-csrf-protection]: suppress - Stateless JWT API so CSRF is unnecessary
        http
                .csrf(csrf -> csrf.disable())   // JWT 는 CSRF 보호가 필요없음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signup",
                                "/email/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll() // 콘솔 접근 허용
                        .anyRequest().authenticated()
                )
                // 기본 폼 로그인 비활성화
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                // 세션 미사용 명시
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CORS 허용 설정
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
//                    corsConfig.setAllowedOrigins(List.of("*"));
//                    배포용 CORS 설정
                    corsConfig.setAllowedOrigins(List.of("https://sampoom.store"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
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
