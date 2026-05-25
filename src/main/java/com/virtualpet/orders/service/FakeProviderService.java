package com.virtualpet.orders.service;

import com.virtualpet.common.exception.ApiException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * In-process simulator that stands in for a hosted payment provider. The /fake-provider/* endpoint
 * applies an action (approve/reject/processing) and emits the corresponding webhook synchronously —
 * tests can observe the order as confirmed immediately. The controller that exposes it is gated by
 * the production profile; the service itself is benign without traffic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FakeProviderService {

  private final ConcurrentHashMap<String, UUID> pending = new ConcurrentHashMap<>();
  private final WebhookService webhookService;

  public void registerPending(String providerPaymentId, UUID paymentId) {
    pending.put(providerPaymentId, paymentId);
    log.debug("Fake provider registered providerPaymentId={}", providerPaymentId);
  }

  public void applyAction(String providerPaymentId, String action) {
    if (!pending.containsKey(providerPaymentId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Unknown providerPaymentId");
    }
    String normalized = action == null ? "" : action.toLowerCase();
    String status =
        switch (normalized) {
          case "approve" -> "PAID";
          case "reject" -> "FAILED";
          case "processing" -> "PROCESSING";
          default ->
              throw new ApiException(
                  HttpStatus.BAD_REQUEST,
                  "Unknown action: " + action + " (allowed: approve, reject, processing)");
        };
    Map<String, Object> payload = Map.of("providerPaymentId", providerPaymentId, "status", status);
    webhookService.handleWebhook("fake", payload);
    if ("PAID".equals(status) || "FAILED".equals(status)) {
      pending.remove(providerPaymentId);
    }
  }
}
