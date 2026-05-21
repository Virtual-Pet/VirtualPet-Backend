package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.auth.repository.UserRepository;
import com.virtualpet.backend.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByEmailIgnoreCase(username)
        .map(
            u ->
                new UserPrincipal(u.getId(), u.getEmail(), u.getPasswordHash(), u.getRole().name()))
        .orElseThrow(
            () -> {
              log.warn("Authentication failed — user not found: {}", username);
              return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });
  }
}
