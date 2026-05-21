package com.virtualpet.backend.auth.config;

import com.virtualpet.backend.auth.domain.UserRole;
import com.virtualpet.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DevUserPasswordUpdater implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    userRepository
        .findByEmailIgnoreCase("staff@virtualpet.local")
        .ifPresent(
            u -> {
              u.setRole(UserRole.ROLE_CUSTOMER);
              u.setPasswordHash(passwordEncoder.encode("staff123"));
              userRepository.save(u);
            });
    userRepository
        .findByEmailIgnoreCase("cliente@demo.local")
        .ifPresent(
            u -> {
              u.setPasswordHash(passwordEncoder.encode("cliente123"));
              userRepository.save(u);
            });
    log.info("Demo users ready: staff@virtualpet.local/staff123, cliente@demo.local/cliente123");
  }
}
