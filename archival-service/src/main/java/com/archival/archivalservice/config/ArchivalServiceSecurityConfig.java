package com.archival.archivalservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class ArchivalServiceSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger/yaml"
                        ).permitAll()
                        .requestMatchers("api/v1/archival/**").permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    static class JwtAuthFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestURI = httpRequest.getRequestURI();
            if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
                chain.doFilter(request, response);
                return;
            }
            String username = httpRequest.getHeader("X-Username");
            String roles = httpRequest.getHeader("X-Roles");

            if (username == null || roles == null) {
                // Log missing headers for debugging
                System.out.println("Missing X-Username or X-Roles headers: username=" + username + ", roles=" + roles);
            } else {
                System.out.println("Setting authentication context for user: " + username + " with roles: " + roles);
                List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                        .collect(Collectors.toList());

                UserDetails userDetails = new User(username, "", authorities);
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()));
            }

            chain.doFilter(request, response);
        }
    }
}
