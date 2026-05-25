package com.virtualpet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestRedisConfiguration.class)
class BackendApplicationTests {

  @Test
  void contextLoads() {}
}
