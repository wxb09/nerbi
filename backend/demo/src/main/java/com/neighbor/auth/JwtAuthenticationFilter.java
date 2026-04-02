package com.neighbor.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        System.out.println("=== JWT Filter ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Auth Header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token found, length: " + token.length());
            try {
                Claims claims = jwtService.parse(token);
                System.out.println("Token parsed successfully, userId: " + claims.get("userId"));
                AuthUser authUser = new AuthUser(
                        claims.get("userId", String.class),
                        claims.get("phone", String.class),
                        claims.get("role", String.class)
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                System.out.println("Token validation failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.out.println("No valid Authorization header found");
        }
        filterChain.doFilter(request, response);
    }
}
