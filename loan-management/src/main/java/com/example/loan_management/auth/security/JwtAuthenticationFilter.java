
package com.example.loan_management.auth.security;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            rejectRequest(response, "Invalid authentication token.");
            return;
        }

        try {
            // Validate JWT signature and expiration.
            if (!jwtService.isTokenValid(token)) {
                log.warn(
                        "JWT validation failed for {} {}",
                        request.getMethod(),
                        request.getRequestURI()
                );

                rejectRequest(
                        response,
                        "Invalid or expired authentication token."
                );
                return;
            }

            String email = jwtService.extractEmail(token);

            if (email == null || email.isBlank()) {
                rejectRequest(response, "Invalid authentication token.");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository
                        .findByEmailIgnoreCase(email.trim())
                        .orElse(null);

                if (user == null) {
                    log.warn("JWT user not found: {}", email);
                    rejectRequest(response, "User account not found.");
                    return;
                }

                if (user.getRole() == null || user.getRole().isBlank()) {
                    log.warn("Role is not configured for user ID {}", user.getId());
                    rejectRequest(response, "User role is not configured.");
                    return;
                }

                if (!"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
                    log.warn(
                            "Inactive account attempted access: userId={}, status={}",
                            user.getId(),
                            user.getAccountStatus()
                    );

                    rejectRequest(
                            response,
                            "Account is not active. Please contact the administrator."
                    );
                    return;
                }

                String normalizedRole = user.getRole()
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replaceFirst("^ROLE_", "");

                String authority = "ROLE_" + normalizedRole;

                // Diagnostic logging: do not log the JWT itself.
                log.info(
                        "JWT user={}, databaseRole={}, grantedAuthority={}, status={}",
                        user.getEmail(),
                        user.getRole(),
                        authority,
                        user.getAccountStatus()
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority(authority)
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                log.info(
                        "SecurityContext user={}, authorities={}",
                        SecurityContextHolder.getContext()
                                .getAuthentication().getName(),
                        SecurityContextHolder.getContext()
                                .getAuthentication().getAuthorities()
                );
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();

            log.error(
                    "JWT authentication/request processing failed: method={}, uri={}, error={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage(),
                    e
            );

            if (!response.isCommitted()) {
                rejectRequest(
                        response,
                        "Invalid or expired authentication token."
                );
            }
        }
    }

    private void rejectRequest(
            HttpServletResponse response,
            String message) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"message\":\"" + message + "\"}"
        );
    }
}
