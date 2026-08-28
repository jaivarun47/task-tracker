package com.project.tasktracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.error.ErrorResponse;
import com.project.tasktracker.model.User;
import com.project.tasktracker.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    public SessionAuthenticationFilter(SessionService sessionService) {
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip CORS pre-flight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Allow public session creation
        String path = request.getRequestURI();
        if ("/api/sessions".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Only enforce on /api/** endpoints
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            sendError(response, HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_MISSING, "Missing Authorization header", request.getRequestURI());
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            sendError(response, HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_MISSING, "Malformed Authorization header. Expected 'Bearer <token>'", request.getRequestURI());
            return;
        }

        String rawToken = authHeader.substring(7).trim();
        if (rawToken.isEmpty()) {
            sendError(response, HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_MISSING, "Empty Bearer token", request.getRequestURI());
            return;
        }

        try {
            User user = sessionService.authenticate(rawToken);
            request.setAttribute(CurrentUserArgumentResolver.USER_REQUEST_ATTRIBUTE, user);
            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            sendError(response, e.getStatus(), e.getCode(), e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SESSION_INVALID, "Authentication processing error", request.getRequestURI());
        }
    }

    private void sendError(
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode code,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"code\":\"%s\"}",
                Instant.now().toString(),
                status.value(),
                escapeJson(status.getReasonPhrase()),
                escapeJson(message),
                escapeJson(path),
                code.name()
        );
        response.getWriter().write(json);
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
