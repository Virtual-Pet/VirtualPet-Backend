package com.virtualpet.common.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {

  private final UUID id;
  private final String email;
  private final String passwordHash;
  private final String role;
  private final boolean forcePasswordChange;

  public UserPrincipal(UUID id, String email, String passwordHash, String role, boolean forcePasswordChange) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.forcePasswordChange = forcePasswordChange;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
    return List.of(new SimpleGrantedAuthority(authority));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  public boolean getForcePasswordChange() {
    return this.forcePasswordChange;
  }
}
