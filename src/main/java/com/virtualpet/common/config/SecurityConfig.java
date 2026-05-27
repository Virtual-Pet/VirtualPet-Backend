package com.virtualpet.common.config;

import com.virtualpet.common.security.JwtAuthenticationFilter;
import com.virtualpet.common.security.ForcePasswordChangeFilter;
import com.virtualpet.common.security.ProblemDetailEntryPoints;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ForcePasswordChangeFilter forcePasswordChangeFilter;
  private final ProblemDetailEntryPoints problemEntryPoints;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(frame -> frame.disable()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(problemEntryPoints.authenticationEntryPoint())
                    .accessDeniedHandler(problemEntryPoints.accessDeniedHandler()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/h2-console/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/health")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/customers/register",
                        "/api/v1/auth/**",
                        "/api/v1/backoffice/auth/login",
                        "/api/v1/customers/register",
                        "/api/v1/customers/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST, "/api/v1/payments/webhook/**", "/api/v1/fake-provider/**")
                    .permitAll()
                    .requestMatchers("/api/v1/cart/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/backoffice/auth/me")
                    .hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/checkout")
                    .hasRole("CUSTOMER")
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/{id}")
                    .hasRole("CUSTOMER")
                    .requestMatchers("/api/v1/orders/**", "/api/v1/auth/me")
                    .hasAnyRole("CUSTOMER", "EMPLOYEE")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(forcePasswordChangeFilter, JwtAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
