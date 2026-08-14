package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import hei.school.graduate.service.CustomUserDetailsService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class AuthIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String LOGIN_URL = "/auth/login";

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired JwtService jwtService;

  @Autowired CustomUserDetailsService userDetailsService;

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
  void register_validRequest_withAdminCookie_returns201() {
    var request = registerRequest("cookie@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminCookieHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("cookie@example.com", response.getBody().get("email"));
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
  void register_withNonAdminRole_returns403() {
    var request = registerRequest("student@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, studentHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
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

  @Test
  void userDetailsService_loadsExistingUser() {
    var email = "loaduser@example.com";
    registerUser(email, "password123");

    var details = userDetailsService.loadUserByUsername(email);

    assertNotNull(details);
    assertEquals(email, details.getUsername());
  }

  @Test
  void userDetailsService_unknownEmail_throws() {
    assertThrows(
        UsernameNotFoundException.class,
        () -> userDetailsService.loadUserByUsername("ghost@example.com"));
  }

  @Test
  void register_studentRole_returns201WithStudent() {
    var request = registerRequest("student-role@example.com", Role.STUDENT);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("STUDENT", response.getBody().get("role"));
  }

  @Test
  void register_teacherRole_returns201WithTeacher() {
    var request = registerRequest("teacher-role@example.com", Role.TEACHER);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("TEACHER", response.getBody().get("role"));
  }

  @Test
  void register_adminRole_returns201WithAdmin() {
    var request = registerRequest("admin-role@example.com", Role.ADMIN);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("ADMIN", response.getBody().get("role"));
  }

  private void registerUser(String email, String password) {
    var request = registerRequest(email);
    request.setPassword(password);

    testRestTemplate.exchange(
        REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);
  }

  private RegisterRequest registerRequest(String email) {
    return registerRequest(email, Role.STUDENT);
  }

  private RegisterRequest registerRequest(String email, Role role) {
    return RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email(email)
        .password("password123")
        .role(role)
        .build();
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(Role.ADMIN);
  }

  private HttpHeaders studentHeaders() {
    return bearerHeaders(Role.STUDENT);
  }

  private HttpHeaders bearerHeaders(Role role) {
    var headers = jsonHeaders();
    headers.setBearerAuth(tokenFor(role));
    return headers;
  }

  private HttpHeaders adminCookieHeaders() {
    var headers = jsonHeaders();
    headers.add(HttpHeaders.COOKIE, "token=" + tokenFor(Role.ADMIN));
    return headers;
  }

  private String tokenFor(Role role) {
    var user =
        new CustomUserDetails(
            new User(UUID.randomUUID(), "auth@hei.school", "Auth", "User", role, null, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
