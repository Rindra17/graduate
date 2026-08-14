package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;

class AuthIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String LOGIN_URL = "/auth/login";

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired JwtService jwtService;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
  }

  @Test
  void register_validRequest_returns201AndUser() {
    var request = registerRequest("reg@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("reg@example.com", response.getBody().get("email"));
    assertEquals("John", response.getBody().get("firstName"));
    assertTrue(response.getHeaders().containsKey("Set-Cookie"));
  }

  @Test
  void register_withoutAdmin_returns401() {
    var request = registerRequest("unauth@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  @Test
  void register_duplicateEmail_returns409() {
    var request = registerRequest("dup@example.com");

    testRestTemplate.exchange(
        REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(409, response.getBody().get("status"));
    assertEquals("Email already in use", response.getBody().get("message"));
  }

  @Test
  void login_validCredentials_returns202AndUser() {
    var email = "login-test@example.com";
    var password = "secure123";
    registerUser(email, password);

    var loginRequest = new LoginRequest(email, password);

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(ACCEPTED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(email, response.getBody().get("email"));
    assertTrue(response.getHeaders().containsKey("Set-Cookie"));
  }

  @Test
  void login_wrongPassword_returns401() {
    var email = "wrongpw@example.com";
    registerUser(email, "correct");

    var loginRequest = new LoginRequest(email, "wrongPass");

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid email or password", response.getBody().get("message"));
  }

  @Test
  void login_nonExistentEmail_returns401() {
    var loginRequest = new LoginRequest("noone@example.com", "password");

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid email or password", response.getBody().get("message"));
  }

  private void registerUser(String email, String password) {
    var request = registerRequest(email);
    request.setPassword(password);

    testRestTemplate.exchange(
        REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);
  }

  private RegisterRequest registerRequest(String email) {
    return RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email(email)
        .password("password123")
        .build();
  }

  private HttpHeaders adminHeaders() {
    var headers = jsonHeaders();
    var admin =
        new CustomUserDetails(
            new User(
                UUID.randomUUID(), "admin@hei.school", "Admin", "Admin", Role.ADMIN, null, null));
    headers.setBearerAuth(jwtService.generateToken(admin));
    return headers;
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
