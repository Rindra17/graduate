package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.model.Role;
import hei.school.graduate.repository.UserRepository;
import hei.school.graduate.repository.model.JUser;

class AuthIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String LOGIN_URL = "/auth/login";

  private static final String PASSWORD = "Password123!";
  private static final String WRONG_PASSWORD = "WrongPassword!";

  private static final String ADMIN_EMAIL = "admin@hei.school";
  private static final String NEW_EMAIL = "johndoe@hei.school";
  private static final String TAKEN_EMAIL = "noobie@hei.school";
  private static final String UNKNOWN_EMAIL = "no-such-user@hei.school";

  @Autowired
  TestRestTemplate testRestTemplate;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @BeforeEach
  void seedUsers() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());

    userRepository.deleteAll();
    userRepository.save(
        JUser.builder()
            .id(UUID.randomUUID())
            .email(ADMIN_EMAIL)
            .password(passwordEncoder.encode(PASSWORD))
            .firstName("Admin")
            .lastName("Istrator")
            .role(Role.ADMIN)
            .build());
    userRepository.save(
        JUser.builder()
            .id(UUID.randomUUID())
            .email(TAKEN_EMAIL)
            .password(passwordEncoder.encode(PASSWORD))
            .firstName("Noo")
            .lastName("Bie")
            .role(Role.STUDENT)
            .build());
  }

  @Test
  void register_then_login_returns_usable_tokens() {
    var token = loginAndGetToken(ADMIN_EMAIL, PASSWORD);

    var registerResponse = registerWithToken(token,
        RegisterRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email(NEW_EMAIL)
            .address("Antananarivo")
            .role(Role.STUDENT)
            .password(PASSWORD)
            .build());

    assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
    assertNotNull(registerResponse.getBody());

    var loginResponse = testRestTemplate.postForEntity(
        LOGIN_URL, new LoginRequest(NEW_EMAIL, PASSWORD), Map.class);

    assertEquals(HttpStatus.ACCEPTED, loginResponse.getStatusCode());
    assertNotNull(loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
  }

  @Test
  void register_with_existing_email_is_rejected() {
    var token = loginAndGetToken(ADMIN_EMAIL, PASSWORD);

    var response = registerWithToken(token,
        RegisterRequest.builder()
            .firstName("Noo")
            .lastName("Bie")
            .email(TAKEN_EMAIL)
            .role(Role.STUDENT)
            .password(PASSWORD)
            .build());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void register_without_authentication_is_rejected() {
    var response = testRestTemplate.postForEntity(
        REGISTER_URL,
        RegisterRequest.builder()
            .firstName("No")
            .lastName("Auth")
            .email("noauth@hei.school")
            .role(Role.STUDENT)
            .password(PASSWORD)
            .build(),
        Map.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void register_as_non_admin_is_rejected() {
    userRepository.save(
        JUser.builder()
            .id(UUID.randomUUID())
            .email(NEW_EMAIL)
            .password(passwordEncoder.encode(PASSWORD))
            .firstName("John")
            .lastName("Doe")
            .role(Role.STUDENT)
            .build());

    var token = loginAndGetToken(NEW_EMAIL, PASSWORD);

    var response = registerWithToken(token,
        RegisterRequest.builder()
            .firstName("Another")
            .lastName("Guy")
            .email("another@hei.school")
            .role(Role.STUDENT)
            .password(PASSWORD)
            .build());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void login_with_valid_credentials_returns_cookie() {
    var response = testRestTemplate.postForEntity(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, PASSWORD), Map.class);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(ADMIN_EMAIL, ((Map<?, ?>) response.getBody()).get("email"));
    assertNotNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
  }

  @Test
  void login_with_wrong_password_returns_401() {
    var response = testRestTemplate.postForEntity(
        LOGIN_URL, new LoginRequest(ADMIN_EMAIL, WRONG_PASSWORD), Map.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(
        ((Map<?, ?>) response.getBody()).get("message").toString().contains("Invalid"));
  }

  @Test
  void login_with_unknown_email_returns_401() {
    var response = testRestTemplate.postForEntity(
        LOGIN_URL, new LoginRequest(UNKNOWN_EMAIL, PASSWORD), Map.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void login_with_malformed_body_returns_400() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var response = testRestTemplate.postForEntity(
        LOGIN_URL, new HttpEntity<>("{not-json", headers), Map.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private String loginAndGetToken(String email, String password) {
    var response = testRestTemplate.postForEntity(
        LOGIN_URL, new LoginRequest(email, password), Map.class);
    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    var setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertNotNull(setCookie);
    return setCookie.substring("token=".length(), setCookie.indexOf(';'));
  }

  private ResponseEntity<Map> registerWithToken(String token, RegisterRequest body) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    return testRestTemplate.exchange(
        REGISTER_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
  }
}
