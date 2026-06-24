package com.virtualpet.shipments.controller;

import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.shipments.sse.ShipmentEmitterRegistry;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentSseController {

  private final ShipmentEmitterRegistry registry;
  private final Executor shipmentSseExecutor;

  public ShipmentSseController(
      ShipmentEmitterRegistry registry,
      @Qualifier("shipmentSseExecutor") Executor shipmentSseExecutor) {
    this.registry = registry;
    this.shipmentSseExecutor = shipmentSseExecutor;
  }

  @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @RequestParam(required = false) UUID orderId,
      @AuthenticationPrincipal UserPrincipal currentUser,
      HttpServletResponse response) {

    // Defense-in-depth against response buffering by an upstream proxy/CDN. Caddy already
    // streams text/event-stream live; these matter only if nginx or a caching layer is added.
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setHeader("X-Accel-Buffering", "no");

    SseEmitter emitter = new SseEmitter(300_000L);

    shipmentSseExecutor.execute(
        () -> {
          try {
            emitter.send(SseEmitter.event().name("connected").data("{}"));
            registry.register(emitter, orderId);
            log.debug(
                "SSE subscription registered: userId={}, orderId={}", currentUser.getId(), orderId);
          } catch (Exception e) {
            log.error("Failed SSE handshake for user {}: {}", currentUser.getId(), e.getMessage());
            emitter.completeWithError(e);
          }
        });

    return emitter;
  }
}
