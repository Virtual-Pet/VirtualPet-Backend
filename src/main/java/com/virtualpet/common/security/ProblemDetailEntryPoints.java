package com.virtualpet.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Bridges Spring Security's authentication / authorization failures to RFC 7807 problem responses
 * so unauthenticated requests get 401 and forbidden ones get 403 with consistent envelopes.
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailEntryPoints {

  private final ObjectMapper objectMapper;

  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, ex) -> write(request, response, HttpStatus.UNAUTHORIZED, ex);
  }

  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, ex) -> write(request, response, HttpStatus.FORBIDDEN, ex);
  }

  private void write(
      HttpServletRequest request, HttpServletResponse response, HttpStatus status, Exception ex)
      throws IOException {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    pd.setTitle(status.getReasonPhrase());
    pd.setType(URI.create("https://api.virtualpet.com/problems/" + slug(status)));
    pd.setInstance(URI.create(request.getRequestURI()));
    pd.setProperty("timestamp", Instant.now().toString());
    String requestId = MDC.get("requestId");
    if (requestId != null) {
      pd.setProperty("requestId", requestId);
    }
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), pd);
  }

  private static String slug(HttpStatus status) {
    return status.name().toLowerCase().replace('_', '-');
  }
}
