package com.virtualpet.common.pagination;

import java.time.Instant;
import java.util.UUID;

/**
 * Decoded cursor payload. Sort key is an ISO instant string for time-ordered lists; the id is a
 * unique tiebreaker for stable pagination.
 */
public record Cursor(String sortKey, UUID id) {

  public static Cursor of(Instant instant, UUID id) {
    return new Cursor(instant.toString(), id);
  }

  public Instant sortKeyAsInstant() {
    return Instant.parse(sortKey);
  }
}
