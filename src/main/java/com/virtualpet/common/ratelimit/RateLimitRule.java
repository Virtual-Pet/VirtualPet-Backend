package com.virtualpet.common.ratelimit;

import java.time.Duration;

public record RateLimitRule(String method, String pathPrefix, int limit, Duration window) {

  public boolean matches(String requestMethod, String requestPath) {
    return method.equalsIgnoreCase(requestMethod) && requestPath.startsWith(pathPrefix);
  }

  public String scope() {
    return method + ":" + pathPrefix;
  }
}
