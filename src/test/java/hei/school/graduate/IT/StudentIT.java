package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.mapper.UserMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.repository.UserRepository;
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

class StudentIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String STUDENTS_URL = "/students";

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired JwtService jwtService;

  @Autowired UserRepository userRepository;

  @Autowired UserMapper userMapper;

  @PersistenceContext EntityManager entityManager;

  @Autowired TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    transactionTemplate.executeWithoutResult(
        status -> {
          entityManager.createNativeQuery("DELETE FROM STUDENT").executeUpdate();
          entityManager.createNativeQuery("DELETE FROM \"user\"").executeUpdate();
        });
  }

  @Test
  void getStudents_withData_returnsPageOfStudents() {
    registerStudent("student-one@example.com");
    registerStudent("student-two@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "?page=0&size=10", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(10, response.getBody().get("size"));
    assertTrue(((Number) response.getBody().get("totalElements")).intValue() >= 2);
    assertEquals(1, response.getBody().get("totalPages"));
    var students = (List<Map<String, Object>>) response.getBody().get("students");
    assertTrue(students.stream().anyMatch(s -> "student-one@example.com".equals(s.get("email"))));
    assertTrue(students.stream().anyMatch(s -> "student-two@example.com".equals(s.get("email"))));
    assertTrue(students.stream().allMatch(s -> s.containsKey("reference")));
    assertTrue(students.stream().allMatch(s -> s.containsKey("status")));
  }

  @Test
  void getStudents_withPagination_returnsRequestedSlice() {
    registerStudent("page-a@example.com");
    registerStudent("page-b@example.com");
    registerStudent("page-c@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "?page=0&size=2", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(2, response.getBody().get("size"));
    assertEquals(2, ((List<?>) response.getBody().get("students")).size());
    assertEquals(2, response.getBody().get("totalPages"));

    var secondPage =
        testRestTemplate.exchange(
            STUDENTS_URL + "?page=1&size=2", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, secondPage.getStatusCode());
    assertNotNull(secondPage.getBody());
    assertEquals(1, secondPage.getBody().get("page"));
    assertEquals(1, ((List<?>) secondPage.getBody().get("students")).size());
  }

  @Test
  void getStudents_withNoData_returnsEmptyPage() {
    var response =
        testRestTemplate.exchange(STUDENTS_URL, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(10, response.getBody().get("size"));
    assertEquals(0L, ((Number) response.getBody().get("totalElements")).longValue());
    assertEquals(0, response.getBody().get("totalPages"));
    assertTrue(((List<?>) response.getBody().get("students")).isEmpty());
  }

  @Test
  void getStudents_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(STUDENTS_URL, GET, new HttpEntity<>(jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  @Test
  void getStudents_doesNotExposePassword() {
    registerStudent("no-password@example.com");

    var response =
        testRestTemplate.exchange(STUDENTS_URL, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var students = (List<Map<String, Object>>) response.getBody().get("students");
    var student = students.get(0);
    assertFalse(student.containsKey("password"));
  }

  @Test
  void getStudent_asAdmin_returnsStudent() {
    var id = registerStudent("admin-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(id.toString(), response.getBody().get("id"));
    assertEquals("admin-view@example.com", response.getBody().get("email"));
    assertNotNull(response.getBody().get("reference"));
    assertEquals("ACTIVE", response.getBody().get("status"));
  }

  @Test
  void getStudent_asOwner_returnsStudent() {
    var id = registerStudent("owner-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id, GET, new HttpEntity<>(studentHeaders(id)), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(id.toString(), response.getBody().get("id"));
    assertEquals("owner-view@example.com", response.getBody().get("email"));
  }

  @Test
  void getStudent_asOtherStudent_returns403() {
    var id = registerStudent("other-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id,
            GET,
            new HttpEntity<>(studentHeaders(UUID.randomUUID())),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getStudent_asTeacher_returns403() {
    var id = registerStudent("teacher-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor("teacher@example.com", false, Role.TEACHER))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getStudent_unknownId_returns404() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID(),
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Student not found", response.getBody().get("message"));
  }

  @Test
  void getStudent_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID(),
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  @Test
  void getStudent_doesNotExposePassword() {
    var id = registerStudent("single-no-password@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().containsKey("password"));
  }

  private UUID registerStudent(String email) {
    var request =
        RegisterRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email(email)
            .password("password123")
            .role(Role.STUDENT)
            .build();

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    var user = (Map<String, Object>) response.getBody().get("user");
    return UUID.fromString((String) user.get("id"));
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor("students@hei.school", false, Role.ADMIN));
  }

  private HttpHeaders studentHeaders(UUID id) {
    return bearerHeaders(tokenFor("students@hei.school", false, Role.STUDENT, id));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(String email, boolean mustChangePassword, Role role) {
    return tokenFor(email, mustChangePassword, role, UUID.randomUUID());
  }

  private String tokenFor(String email, boolean mustChangePassword, Role role, UUID id) {
    var user =
        new CustomUserDetails(
            new User(id, email, "Student", "Test", role, null, null, mustChangePassword, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
