package com.virtualpet.common.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

class ETagSupportTest {

  private final ETagSupport etagSupport = new ETagSupport(JsonMapper.builder().build());

  record Payload(String name, int qty) {}

  @Test
  void identicalBodiesProduceIdenticalETags() {
    Payload a = new Payload("kibble", 2);
    Payload b = new Payload("kibble", 2);
    assertThat(etagSupport.computeETag(a)).isEqualTo(etagSupport.computeETag(b));
  }

  @Test
  void differentBodiesProduceDifferentETags() {
    assertThat(etagSupport.computeETag(new Payload("kibble", 2)))
        .isNotEqualTo(etagSupport.computeETag(new Payload("kibble", 3)));
  }

  @Test
  void returns200WithETagWhenIfNoneMatchAbsent() {
    Payload body = new Payload("kibble", 2);
    ResponseEntity<Payload> response =
        etagSupport.withETag(
            body,
            new MockHttpServletRequest(),
            CacheControl.maxAge(java.time.Duration.ofSeconds(300)).cachePublic());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(body);
    assertThat(response.getHeaders().getETag()).isNotBlank();
  }

  @Test
  void returns304WhenIfNoneMatchMatchesCurrentETag() {
    Payload body = new Payload("kibble", 2);
    String etag = etagSupport.computeETag(body);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.IF_NONE_MATCH, etag);

    ResponseEntity<Payload> response =
        etagSupport.withETag(
            body, request, CacheControl.maxAge(java.time.Duration.ofSeconds(300)).cachePublic());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
    assertThat(response.getBody()).isNull();
    assertThat(response.getHeaders().getETag()).isEqualTo(etag);
  }
}
