package com.virtualpet.common.pagination;

import com.virtualpet.common.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Opaque, URL-safe cursor encoding. Format: base64url(sortKey + '|' + id). Clients must treat
 * cursors as opaque strings.
 */
@Component
public class CursorCodec {

  private static final String SEP = "|";
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  public String encode(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    String raw = cursor.sortKey() + SEP + cursor.id();
    return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  public Cursor decode(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      String raw = new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
      int sep = raw.lastIndexOf(SEP);
      if (sep <= 0 || sep == raw.length() - 1) {
        throw new IllegalArgumentException("missing separator");
      }
      String sortKey = raw.substring(0, sep);
      UUID id = UUID.fromString(raw.substring(sep + 1));
      return new Cursor(sortKey, id);
    } catch (IllegalArgumentException ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor");
    }
  }
}
