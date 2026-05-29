package com.virtualpet.orders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.virtualpet.orders.domain.Address;
import com.virtualpet.orders.domain.PaymentStatus;
import com.virtualpet.orders.domain.SessionLineItem;
import com.virtualpet.orders.domain.SessionStatus;
import com.virtualpet.orders.domain.SessionTotals;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Wire DTOs for /cart/checkout, /checkout/sessions, /payments. */
public final class CheckoutDTO {

  private CheckoutDTO() {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CheckoutSessionResponseDTO(
      UUID checkoutSessionId,
      SessionStatus status,
      List<SessionLineItem> lineItems,
      SessionTotals totals,
      String currency,
      Address shippingAddress,
      Instant expiresAt) {}

  public record SetShippingAddressRequestDTO(
      @Valid @NotBlank String addressLine,
      @NotBlank String city,
      String state,
      String country,
      @NotBlank String postalCode) {

    public Address toAddress() {
      return new Address(addressLine, city, state, country, postalCode);
    }
  }

  public record PaymentIntentResponseDTO(
      UUID paymentId,
      String provider,
      String providerPaymentId,
      String checkoutUrl,
      BigDecimal amount,
      String currency,
      PaymentStatus status) {}

  public record PaymentResponseDTO(
      UUID paymentId,
      String provider,
      String providerPaymentId,
      PaymentStatus status,
      BigDecimal amount,
      String currency,
      UUID orderId) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record OrderConfirmationResponseDTO(
      UUID orderId, UUID shipmentId, String status, String trackingToken) {}

  public record GuestInfoDTO(
      @NotBlank String firstName,
      @NotBlank String lastName,
      @NotBlank @Email String email) {}

  public record GuestLineItemDTO(
      @NotNull UUID skuId,
      @Min(1) int quantity) {}

  public record GuestCheckoutRequestDTO(
      @Valid @NotNull GuestInfoDTO guest,
      @NotEmpty List<@Valid GuestLineItemDTO> lineItems,
      @Valid @NotNull SetShippingAddressRequestDTO shippingAddress) {}
}
