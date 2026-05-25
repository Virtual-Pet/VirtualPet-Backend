package com.virtualpet.orders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.virtualpet.orders.domain.Address;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.orders.domain.SessionLineItem;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.domain.SessionTotals;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Wire DTOs for /cart/checkout, /checkout/sessions, /payments. */
public final class CheckoutDTO {

  private CheckoutDTO() {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CheckoutSessionResponse(
      UUID checkoutSessionId,
      SessionStatus status,
      List<SessionLineItem> lineItems,
      SessionTotals totals,
      String currency,
      Address shippingAddress,
      Instant expiresAt) {}

  public record SetShippingAddressRequest(
      @Valid @NotBlank String addressLine,
      @NotBlank String city,
      String state,
      String country,
      @NotBlank String postalCode) {

    public Address toAddress() {
      return new Address(addressLine, city, state, country, postalCode);
    }
  }

  public record PaymentIntentResponse(
      UUID paymentId,
      String provider,
      String providerPaymentId,
      String checkoutUrl,
      BigDecimal amount,
      String currency,
      PaymentStatus status) {}

  public record PaymentResponse(
      UUID paymentId,
      String provider,
      String providerPaymentId,
      PaymentStatus status,
      BigDecimal amount,
      String currency,
      UUID orderId) {}

  public record OrderConfirmationResponse(UUID orderId, UUID shipmentId, String status) {}
}
