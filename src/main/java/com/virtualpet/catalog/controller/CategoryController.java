package com.virtualpet.catalog.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> list() {
    return ResponseEntity.ok(List.of(
        Map.of("id", "alimentos", "name", "Alimentos", "slug", "alimentos"),
        Map.of("id", "juguetes", "name", "Juguetes", "slug", "juguetes"),
        Map.of("id", "higiene", "name", "Higiene", "slug", "higiene"),
        Map.of("id", "camas", "name", "Camas", "slug", "camas")
    ));
  }
}
