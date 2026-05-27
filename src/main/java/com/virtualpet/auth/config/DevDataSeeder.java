package com.virtualpet.auth.config;

import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.repository.EmployeeRepository;
import com.virtualpet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev") // ⚠️ Clave: Evita que se creen estos usuarios en Producción
public class DevDataSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    log.info("🌱 Iniciando verificación de usuarios de desarrollo...");

    String adminEmail = "admin@virtualpet.local";

    // 1. Creación del SúperAdministrador (Si no existe)
    if (userRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
      log.info("Súper Admin no encontrado. Creando credenciales iniciales...");

      // A. Creamos la Identidad (Auth)
      UserEntity adminUser =
          UserEntity.builder()
              .email(adminEmail)
              .passwordHash(passwordEncoder.encode("admin123"))
              .role(UserRole.ROLE_ADMIN)
              .active(true)
              .emailVerified(true)
              .forcePasswordChange(
                  false) // Lo dejamos en false para que puedas loguearte directo a probar
              .build();

      adminUser = userRepository.save(adminUser);

      // B. Creamos el Perfil Físico (Backoffice)
      EmployeeEntity adminProfile =
          EmployeeEntity.builder()
              .userId(adminUser.getId()) // Vinculación mediante UUID
              .name("Súper")
              .lastname("Administrador")
              .legajo("ADM-0001")
              .warehouseId(1) // Asumimos depósito central
              .build();

      employeeRepository.save(adminProfile);
      log.info("✅ Admin creado: {} / admin123", adminEmail);
    }

    // 2. Mantenimiento de tus usuarios de prueba existentes
    userRepository
        .findByEmailIgnoreCase("staff@virtualpet.local")
        .ifPresent(
            u -> {
              u.setRole(UserRole.ROLE_EMPLOYEE); // Ajustado a EMPLOYEE según tu nuevo enum
              u.setPasswordHash(passwordEncoder.encode("staff123"));
              userRepository.save(u);
            });

    userRepository
        .findByEmailIgnoreCase("cliente@demo.local")
        .ifPresent(
            u -> {
              u.setRole(UserRole.ROLE_CUSTOMER);
              u.setPasswordHash(passwordEncoder.encode("cliente123"));
              userRepository.save(u);
            });

    log.info("🌳 Usuarios de prueba listos para usar.");
  }
}