package com.sampoom.user.common.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        ErrorStatus error = ErrorStatus.ACCESS_DENIED; // 새 항목 정의 필요 (예: 11403, "접근 권한이 없습니다.")

        ApiResponse<Void> body = ApiResponse.errorWithCode(
                error.getCode(),
                error.getMessage()
        );

        response.setStatus(error.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));

        log.warn("[Security] {} {} -> {}", request.getMethod(), request.getRequestURI(), error.getMessage());
    }
}