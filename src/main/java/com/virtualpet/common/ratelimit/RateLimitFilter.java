package com.virtualpet.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Applies fixed-window rate limiting to selected routes (auth endpoints). Denied requests get 429
 * with a Retry-After header and a problem+json body.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

  private static final List<RateLimitRule> RULES =
      List.of(
          new RateLimitRule("POST", "/api/v1/customers/login", 10, Duration.ofMinutes(10)),
          new RateLimitRule("POST", "/api/v1/backoffice/auth/login", 5, Duration.ofMinutes(10)));

  private final RateLimiter rateLimiter;
  private final ObjectMapper objectMapper;
  private final boolean enabled;

  public RateLimitFilter(
      RateLimiter rateLimiter,
      ObjectMapper objectMapper,
      @org.springframework.beans.factory.annotation.Value("${virtualpet.ratelimit.enabled:true}")
          boolean enabled) {
    this.rateLimiter = rateLimiter;
    this.objectMapper = objectMapper;
    this.enabled = enabled;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 10;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!enabled) {
      chain.doFilter(request, response);
      return;
    }
    RateLimitRule rule = matchRule(request);
    if (rule == null) {
      chain.doFilter(request, response);
      return;
    }

    String clientKey = clientKey(request);
    RateLimitDecision decision =
        rateLimiter.tryAcquire(rule.scope(), clientKey, rule.limit(), rule.window());
    if (decision.allowed()) {
      chain.doFilter(request, response);
      return;
    }

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(decision.retryAfterSeconds()));
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
    pd.setTitle(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
    pd.setDetail("Rate limit exceeded");
    pd.setType(URI.create("https://api.virtualpet.com/problems/too-many-requests"));
    pd.setInstance(URI.create(request.getRequestURI()));
    pd.setProperty("timestamp", Instant.now().toString());
    pd.setProperty("retryAfterSeconds", decision.retryAfterSeconds());
    String requestId = MDC.get("requestId");
    if (requestId != null) {
      pd.setProperty("requestId", requestId);
    }
    objectMapper.writeValue(response.getOutputStream(), pd);
  }

  private RateLimitRule matchRule(HttpServletRequest request) {
    for (RateLimitRule rule : RULES) {
      if (rule.matches(request.getMethod(), request.getRequestURI())) {
        return rule;
      }
    }
    return null;
  }

  private String clientKey(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      int comma = forwarded.indexOf(',');
      return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
    }
    return request.getRemoteAddr();
  }
}
