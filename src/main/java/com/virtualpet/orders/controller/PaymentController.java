package com.virtualpet.orders.controller;

import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.orders.dto.CheckoutDTO.PaymentResponseDTO;
import com.virtualpet.orders.service.PaymentService;
import com.virtualpet.orders.service.WebhookService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final WebhookService webhookService;

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public PaymentResponseDTO getPayment(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID id) {
    return paymentService.getPayment(id, currentUser.getId());
  }

  @PostMapping("/webhook/{provider}")
  @ResponseStatus(HttpStatus.OK)
  public void receiveWebhook(
      @PathVariable String provider, @RequestBody Map<String, Object> payload) {
    webhookService.handleWebhook(provider, payload);
  }
}
