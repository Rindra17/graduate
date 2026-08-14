package hei.school.graduate.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "jwt.secret", () -> "aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899");
    registry.add("jwt.expiration", () -> "86400000");
  }
}
