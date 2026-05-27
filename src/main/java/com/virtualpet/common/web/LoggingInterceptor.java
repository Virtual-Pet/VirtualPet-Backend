package com.virtualpet.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

  private static final String START_TIME_ATTR = "requestStartTime";

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
    log.info("→ {} {}", request.getMethod(), request.getRequestURI());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Object start = request.getAttribute(START_TIME_ATTR);
    long duration = start == null ? 0 : System.currentTimeMillis() - (Long) start;
    int status = response.getStatus();
    if (status >= 500) {
      log.warn("← {} ({}ms)", status, duration);
    } else {
      log.info("← {} ({}ms)", status, duration);
    }
  }
}
