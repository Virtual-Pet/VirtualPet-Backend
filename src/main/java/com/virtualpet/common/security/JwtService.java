package com.virtualpet.common.security;

import com.virtualpet.common.config.VirtualPetProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(VirtualPetProperties properties) {
    this.key = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    this.expirationMs = properties.getJwt().getExpirationMs();
  }

  public String generate(UUID userId, String email, String role) {
    Date now = new Date();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
