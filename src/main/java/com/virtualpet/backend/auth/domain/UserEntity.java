package com.virtualpet.backend.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(schema = "auth", name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  @Column(name = "email_verified", nullable = false)
  @Builder.Default
  private Boolean emailVerified = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  // Relaciones 1:1 con los perfiles. Cascade ALL permite guardar el user y el perfil de una.
  @OneToOne(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private CustomerEntity customerProfile;

  @OneToOne(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private EmployeeEntity employeeProfile;

  // Relación 1:N con direcciones
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<AddressEntity> addresses = new ArrayList<>();

  // Helper para sincronizar la relación bidireccional
  public void setCustomerProfile(CustomerEntity customer) {
    this.customerProfile = customer;
    customer.setUser(this);
  }

  public void addAddress(AddressEntity address) {
    addresses.add(address);
    address.setUser(this);
  }

  // --- Métodos de UserDetails ---
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getPassword() {
    return this.passwordHash;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.active;
  }

  @Override
  public boolean isEnabled() {
    return this.active;
  }
}
