package com.virtualpet.auth.config;

import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.repository.CustomerRepository;
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
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final CustomerRepository customerRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    seedAdmin();
    seedEmployee();
    seedCustomer();
    seedRider();
    log.info("Dev users ready.");
  }

  private void seedAdmin() {
    String email = "admin@virtualpet.com";
    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      return;
    }
    UserEntity user =
        userRepository.save(
            UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("admin1234"))
                .role(UserRole.ROLE_ADMIN)
                .active(true)
                .build());
    employeeRepository.save(
        EmployeeEntity.builder()
            .userId(user.getId())
            .name("Super")
            .lastname("Admin")
            .legajo("ADM-0000")
            .warehouseId(1)
            .build());
    log.info("Seeded admin {} / admin1234", email);
  }

  private void seedEmployee() {
    String email = "staff@virtualpet.com";
    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      return;
    }
    UserEntity user =
        userRepository.save(
            UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("staff1234"))
                .role(UserRole.ROLE_EMPLOYEE)
                .active(true)
                .build());
    employeeRepository.save(
        EmployeeEntity.builder()
            .userId(user.getId())
            .name("Empleado")
            .lastname("Demo")
            .legajo("EMP-0001")
            .warehouseId(1)
            .build());
    log.info("Seeded employee {} / staff1234", email);
  }

  private void seedCustomer() {
    String email = "cliente1234@gmail.com";
    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      return;
    }
    UserEntity user =
        userRepository.save(
            UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("cliente1234"))
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build());
    customerRepository.save(
        CustomerEntity.builder().userId(user.getId()).name("Cliente").lastname("Demo").build());
    log.info("Seeded customer {} / cliente1234", email);
  }

  private void seedRider() {
    String email = "rider@virtualpet.com";
    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      return;
    }
    UserEntity user =
        userRepository.save(
            UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("rider1234"))
                .role(UserRole.ROLE_RIDER)
                .active(true)
                .build());
    employeeRepository.save(
        EmployeeEntity.builder()
            .userId(user.getId())
            .name("Repartidor")
            .lastname("Demo")
            .legajo("RDR-0001")
            .warehouseId(1)
            .build());
    log.info("Seeded rider {} / rider1234", email);
  }
}
