package com.virtualpet.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.virtualpet.common.exception.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CursorCodecTest {

  private final CursorCodec codec = new CursorCodec();

  @Test
  void roundTripsAnInstantBasedCursor() {
    UUID id = UUID.randomUUID();
    Instant now = Instant.parse("2026-01-15T10:30:00Z");
    Cursor original = Cursor.of(now, id);

    String encoded = codec.encode(original);
    Cursor decoded = codec.decode(encoded);

    assertThat(decoded.id()).isEqualTo(id);
    assertThat(decoded.sortKeyAsInstant()).isEqualTo(now);
  }

  @Test
  void returnsNullWhenInputIsNullOrBlank() {
    assertThat(codec.encode(null)).isNull();
    assertThat(codec.decode(null)).isNull();
    assertThat(codec.decode("")).isNull();
    assertThat(codec.decode("   ")).isNull();
  }

  @Test
  void rejectsMalformedCursorsWithBadRequest() {
    assertThatThrownBy(() -> codec.decode("not-base64-and-no-pipe"))
        .isInstanceOf(ApiException.class);

    assertThatThrownBy(() -> codec.decode("Zm9v")) // base64("foo") — no pipe
        .isInstanceOf(ApiException.class);
  }

  @Test
  void cursorsAreUrlSafe() {
    Cursor cursor = Cursor.of(Instant.now(), UUID.randomUUID());
    String encoded = codec.encode(cursor);
    assertThat(encoded).doesNotContain("+", "/", "=");
  }
}
