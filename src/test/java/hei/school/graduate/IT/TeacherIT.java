package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.transaction.support.TransactionTemplate;

class TeacherIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String TEACHERS_URL = "/teachers";

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired JwtService jwtService;

  @PersistenceContext EntityManager entityManager;

  @Autowired TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    transactionTemplate.executeWithoutResult(
        status -> {
          entityManager.createNativeQuery("DELETE FROM TEACHER").executeUpdate();
          entityManager.createNativeQuery("DELETE FROM \"user\"").executeUpdate();
        });
  }

  @Test
  void getTeachers_withData_returnsPageOfTeachers() {
    registerTeacher("teacher-one@example.com");
    registerTeacher("teacher-two@example.com");

    var response =
        testRestTemplate.exchange(
            TEACHERS_URL + "?page=0&size=10", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(10, response.getBody().get("size"));
    assertTrue(((Number) response.getBody().get("totalElements")).intValue() >= 2);
    assertEquals(1, response.getBody().get("totalPages"));
    var teachers = (List<Map<String, Object>>) response.getBody().get("teachers");
    assertTrue(teachers.stream().anyMatch(t -> "teacher-one@example.com".equals(t.get("email"))));
    assertTrue(teachers.stream().anyMatch(t -> "teacher-two@example.com".equals(t.get("email"))));
    assertTrue(teachers.stream().allMatch(t -> t.containsKey("reference")));
  }

  @Test
  void getTeachers_withPagination_returnsRequestedSlice() {
    registerTeacher("page-a@example.com");
    registerTeacher("page-b@example.com");
    registerTeacher("page-c@example.com");

    var response =
        testRestTemplate.exchange(
            TEACHERS_URL + "?page=0&size=2", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(2, response.getBody().get("size"));
    assertEquals(2, ((List<?>) response.getBody().get("teachers")).size());
    assertEquals(2, response.getBody().get("totalPages"));
  }

  @Test
  void getTeachers_withNoData_returnsEmptyPage() {
    var response =
        testRestTemplate.exchange(TEACHERS_URL, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(10, response.getBody().get("size"));
    assertEquals(0L, ((Number) response.getBody().get("totalElements")).longValue());
    assertEquals(0, response.getBody().get("totalPages"));
    assertTrue(((List<?>) response.getBody().get("teachers")).isEmpty());
  }

  @Test
  void getTeachers_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(TEACHERS_URL, GET, new HttpEntity<>(jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  @Test
  void getTeachers_asNonAdmin_returns403() {
    var response =
        testRestTemplate.exchange(
            TEACHERS_URL, GET, new HttpEntity<>(nonAdminHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getTeachers_doesNotExposePassword() {
    registerTeacher("no-password@example.com");

    var response =
        testRestTemplate.exchange(TEACHERS_URL, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var teachers = (List<Map<String, Object>>) response.getBody().get("teachers");
    var teacher = teachers.get(0);
    assertFalse(teacher.containsKey("password"));
  }

  private void registerTeacher(String email) {
    var request =
        RegisterRequest.builder()
            .firstName("John")
            .lastName("Smith")
            .email(email)
            .password("password123")
            .role(Role.TEACHER)
            .build();

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor("teachers@hei.school", false, Role.ADMIN));
  }

  private HttpHeaders nonAdminHeaders() {
    return bearerHeaders(tokenFor("student@hei.school", false, Role.STUDENT));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(String email, boolean mustChangePassword, Role role) {
    var user =
        new CustomUserDetails(
            new User(
                UUID.randomUUID(),
                email,
                "Teacher",
                "Test",
                role,
                null,
                null,
                mustChangePassword,
                null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
