package com.virtualpet.backend.catalog.domain;

import org.springframework.data.domain.Sort;

public enum ProductSort {
  NAME_ASC(Sort.by("name").ascending());

  private final Sort sort;

  ProductSort(Sort sort) {
    this.sort = sort;
  }

  public Sort toSort() {
    return sort;
  }

  public static ProductSort fromParam(String value) {
    if (value == null || value.isBlank()) {
      return NAME_ASC;
    }
    return switch (value.toLowerCase()) {
      default -> NAME_ASC;
    };
  }
}
