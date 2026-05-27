package com.virtualpet.common.security;

import com.virtualpet.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

  private SecurityUtils() {}

  public static UserPrincipal currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado");
    }
    return principal;
  }

  public static UUID currentUserId() {
    return currentUser().getId();
  }
}
