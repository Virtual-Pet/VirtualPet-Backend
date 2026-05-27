package com.virtualpet.common.pagination;

import java.util.List;

public record CursorPage<T>(List<T> data, int limit, String nextCursor, boolean hasMore) {

  public static <T> CursorPage<T> of(List<T> data, int limit, String nextCursor) {
    return new CursorPage<>(data, limit, nextCursor, nextCursor != null);
  }

  public static <T> CursorPage<T> empty(int limit) {
    return new CursorPage<>(List.of(), limit, null, false);
  }
}
