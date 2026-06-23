package com.virtualpet.shipments.sse;

import com.virtualpet.shipments.event.ShipmentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventListener {

  private final ShipmentEmitterRegistry registry;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
    log.debug(
        "Broadcasting shipment-update: shipmentId={}, {}→{}",
        event.shipmentId(),
        event.previousStatus(),
        event.newStatus());
    registry.broadcast(event);
  }
}
