package com.virtualpet.backend.shared.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "virtualpet")
@Getter
@Setter
public class VirtualPetProperties {

  private Jwt jwt = new Jwt();
  private Cart cart = new Cart();
  private Cors cors = new Cors();
  private Payment payment = new Payment();
  private Mercadopago mercadopago = new Mercadopago();
  private App app = new App();
  private Mail mail = new Mail();

  @Getter
  @Setter
  public static class Jwt {
    private String secret;
    private long expirationMs = 86400000L;
  }

  @Getter
  @Setter
  public static class Cart {
    private int ttlHours = 72;
  }

  @Getter
  @Setter
  public static class Cors {
    private List<String> allowedOrigins = List.of("http://localhost:3000");
  }

  @Getter
  @Setter
  public static class Payment {
    private String provider = "mock";
  }

  @Getter
  @Setter
  public static class Mercadopago {
    private String accessToken;
    private String webhookSecret;
    private String notificationUrl;
  }

  @Getter
  @Setter
  public static class App {
    private String frontendUrl = "http://localhost:3000";
  }

  @Getter
  @Setter
  public static class Mail {
    /** Dirección remitente (normalmente igual a GMAIL_USERNAME). */
    private String from = "";
  }
}
