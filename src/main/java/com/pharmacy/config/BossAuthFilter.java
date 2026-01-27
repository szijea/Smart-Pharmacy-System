package com.pharmacy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.controller.BossController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class BossAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // The login and tenants endpoints are public/semi-public and do not need a token.
        if (path.equals("/api/boss/login") || path.equals("/api/boss/tenants")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");

        if (token != null && BossController.activeTokens.containsKey(token)) {
            // Token is valid, proceed with the request.
            filterChain.doFilter(request, response);
        } else {
            // Token is invalid or missing, send an unauthorized error.
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "Unauthorized: Invalid or missing token.")));
        }
    }
}
