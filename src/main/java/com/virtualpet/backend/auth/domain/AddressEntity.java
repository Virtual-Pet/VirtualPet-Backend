package com.virtualpet.backend.auth.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(schema = "auth", name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false)
  private String street;

  @Column(nullable = false)
  private String num;

  @Column(nullable = false)
  private String city;

  @Column(name = "zip_code", nullable = false)
  private String zipCode;

  @Column(name = "is_default", nullable = false)
  @Builder.Default
  private Boolean isDefault = false;
}
