package com.virtualpet.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class ForcePasswordChangeFilter extends OncePerRequestFilter {

  // Endpoints that the user may call even if password change is required
  private static final Set<String> ALLOWED_PATHS = Set.of(
      "/api/v1/auth/change-password",
      "/api/v1/auth/forgot-password",
      "/api/v1/auth/reset-password",
      "/api/v1/auth/refresh",
      "/api/v1/backoffice/auth/login",
      "/api/v1/customers/login",
      "/api/v1/customers/register"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
      UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
      if (principal.getForcePasswordChange()) {
        String path = request.getServletPath();
        if (!ALLOWED_PATHS.contains(path)) {
          log.debug("Blocking request to {} because user {} must change password", path, principal.getId());
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Password change required");
          return;
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
