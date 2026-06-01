package com.virtualpet.common.cache;

import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Computes weak ETags over a payload and short-circuits with 304 when the client's If-None-Match
 * matches. Use from controllers that serve cacheable read-only resources.
 */
@Component
@RequiredArgsConstructor
public class ETagSupport {

  private final ObjectMapper objectMapper;

  public <T> ResponseEntity<T> withETag(T body, HttpServletRequest request, CacheControl cache) {
    String etag = computeETag(body);
    String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
    if (etag.equals(ifNoneMatch)) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).cacheControl(cache).build();
    }
    return ResponseEntity.ok().eTag(etag).cacheControl(cache).body(body);
  }

  public String computeETag(Object body) {
    try {
      byte[] payload = objectMapper.writeValueAsBytes(body);
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(payload);
      return "W/\"" + HexFormat.of().formatHex(hash, 0, 16) + "\"";
    } catch (JacksonException e) {
      throw new IllegalStateException("Failed to compute ETag", e);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
