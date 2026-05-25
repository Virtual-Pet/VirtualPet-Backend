package com.virtualpet.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

  private final RequestIdFilter filter = new RequestIdFilter();

  @Test
  void honorsExistingRequestIdHeader() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/products");
    req.addHeader(RequestIdFilter.HEADER, "trace-abc-123");
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertThat(res.getHeader(RequestIdFilter.HEADER)).isEqualTo("trace-abc-123");
  }

  @Test
  void generatesARequestIdWhenHeaderAbsent() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/products");
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertThat(res.getHeader(RequestIdFilter.HEADER)).isNotBlank();
  }

  @Test
  void clearsMdcAfterRequest() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/products");
    MockHttpServletResponse res = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(req, res, chain);

    assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
  }
}
