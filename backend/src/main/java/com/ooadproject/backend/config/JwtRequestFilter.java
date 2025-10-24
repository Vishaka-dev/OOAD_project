package com.ooadproject.backend.config;

import com.ooadproject.backend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // Skip JWT processing for clearly public endpoints only
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/actuator/") ||
                requestPath.startsWith("/api/auth/") ||
                requestPath.startsWith("/uploads/") ||
                (requestPath.startsWith("/api/products/") && !requestPath.startsWith("/api/products/admin")) ||
                (requestPath.startsWith("/api/categories/") && !requestPath.startsWith("/api/categories/admin"))) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        System.out.println("🔄 JWT Filter - Request URL: " + request.getRequestURL());
        System.out.println("🔄 JWT Filter - Request Method: " + request.getMethod());
        System.out.println("🔄 JWT Filter - Authorization header: " + requestTokenHeader);
        System.out.println("🔄 JWT Filter - All headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            System.out.println("  " + headerName + ": " + request.getHeader(headerName));
        });

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            System.out.println("🔄 JWT Filter - Extracted token: "
                    + jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");
            try {
                username = jwtUtil.extractUsername(jwtToken);
                System.out.println("🔄 JWT Filter - Extracted username: " + username);
            } catch (Exception e) {
                System.out.println("❌ JWT Filter - Unable to get JWT Token: " + e.getMessage());
                logger.warn("Unable to get JWT Token: " + e.getMessage());
            }
        } else {
            System.out.println("❌ JWT Filter - No valid Authorization header found");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                System.out.println("🔄 JWT Filter - Validating token for username: " + username);
                if (jwtUtil.validateToken(jwtToken, username)) {
                    // Extract role from token
                    String role = jwtUtil.extractRole(jwtToken);
                    System.out.println("🔄 JWT Filter - Extracted role: " + role);

                    // Create authorities
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,
                            null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ JWT Filter - Authentication set successfully for user: " + username);
                } else {
                    System.out.println("❌ JWT Filter - Token validation failed for username: " + username);
                }
            } catch (Exception e) {
                System.out.println("❌ JWT Filter - JWT validation failed: " + e.getMessage());
                logger.warn("JWT validation failed: " + e.getMessage());
            }
        } else {
            System.out.println("❌ JWT Filter - Username is null or authentication already exists");
        }

        chain.doFilter(request, response);
    }
}