package com.virtualpet.orders.controller;

import com.virtualpet.orders.service.FakeProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Drives the in-process fake payment provider. The endpoint is disabled in production via the
 * profile predicate; SecurityConfig also leaves the path open in non-prod.
 */
@RestController
@RequestMapping("/api/v1/fake-provider/payments")
@RequiredArgsConstructor
@Profile("!prod")
public class FakeProviderController {

  private final FakeProviderService fakeProviderService;

  @PostMapping("/{providerPaymentId}/{action}")
  @ResponseStatus(HttpStatus.OK)
  public void apply(@PathVariable String providerPaymentId, @PathVariable String action) {
    fakeProviderService.applyAction(providerPaymentId, action);
  }
}
