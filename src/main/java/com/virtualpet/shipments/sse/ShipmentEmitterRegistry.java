package com.virtualpet.shipments.sse;

import com.virtualpet.shipments.event.ShipmentStatusChangedEvent;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEmitterRegistry {

  /**
   * Sentinel stored in place of a {@code null} order filter, since {@link ConcurrentHashMap}
   * forbids null values. Represents "subscribe to all shipment updates".
   */
  private static final UUID ALL_ORDERS = new UUID(0L, 0L);

  private final ConcurrentHashMap<SseEmitter, UUID> emitters = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public void register(SseEmitter emitter, UUID orderIdFilter) {
    emitters.put(emitter, orderIdFilter == null ? ALL_ORDERS : orderIdFilter);
    Runnable cleanup = () -> emitters.remove(emitter);
    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(
        e -> {
          log.debug("SSE emitter error (orderId={}): {}", orderIdFilter, e.getMessage());
          emitters.remove(emitter);
        });
  }

  public void broadcast(ShipmentStatusChangedEvent event) {
    String json;
    try {
      json =
          objectMapper.writeValueAsString(
              new Payload(
                  event.shipmentId(),
                  event.orderId(),
                  event.newStatus().name(),
                  event.previousStatus().name(),
                  event.updatedAt().toString()));
    } catch (Exception e) {
      log.error("Failed to serialize ShipmentStatusChangedEvent", e);
      return;
    }

    emitters.forEach(
        (emitter, filter) -> {
          if (!filter.equals(ALL_ORDERS) && !filter.equals(event.orderId())) {
            return;
          }
          try {
            emitter.send(SseEmitter.event().name("shipment-update").data(json));
          } catch (IOException ex) {
            log.debug("Stale SSE emitter removed during broadcast: {}", ex.getMessage());
            emitters.remove(emitter);
            emitter.completeWithError(ex);
          }
        });
  }

  private record Payload(
      UUID shipmentId, UUID orderId, String status, String previousStatus, String updatedAt) {}
}
