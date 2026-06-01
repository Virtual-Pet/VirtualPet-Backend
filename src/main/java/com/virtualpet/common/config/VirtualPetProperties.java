package com.virtualpet.common.config;

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
  private App app = new App();

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
    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Cookie {
      private String sameSite = "Lax";
      private boolean secure = false;
    }
  }

  @Getter
  @Setter
  public static class Cors {
    private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:3001");
  }

  @Getter
  @Setter
  public static class Payment {
    private String provider = "mock";
  }

  @Getter
  @Setter
  public static class App {
    private String frontendUrl = "http://localhost:3000";
  }
}
