package com.virtualpet.backend.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

  private static final String START_TIME_ATTR = "requestStartTime";
  private static final String REQUEST_ID_KEY = "requestId";

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put(REQUEST_ID_KEY, requestId);
    request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
    log.info("→ {} {}", request.getMethod(), request.getRequestURI());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    long duration = System.currentTimeMillis() - (Long) request.getAttribute(START_TIME_ATTR);
    int status = response.getStatus();
    if (status >= 500) {
      log.warn("← {} ({}ms)", status, duration);
    } else {
      log.info("← {} ({}ms)", status, duration);
    }
    MDC.clear();
  }
}
