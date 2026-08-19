package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.file.bucket.BucketComponent;
import hei.school.graduate.file.hash.FileHash;
import hei.school.graduate.file.hash.FileHashAlgorithm;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import java.io.File;
import java.net.URL;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;

class CohortWebIT extends FacadeIT {

  private static final String WEB_COHORTS_URL = "/web/cohorts";

  private static final UUID TEST_COHORT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID TEST_BRANCH_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEST_SEMESTER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEST_GROUP_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID TEST_COURSE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID TEST_EXAM_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000005");
  private static final UUID GRAD_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000010");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @MockBean BucketComponent bucketComponent;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    seedData();
    mockS3();
  }

  @SneakyThrows
  private void mockS3() {
    doAnswer(invocation -> new FileHash(FileHashAlgorithm.SHA256, "fake-hash"))
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    when(bucketComponent.presign(anyString(), any()))
        .thenReturn(new URL("https://fake-s3.example.com/graduates.xlsx"));
  }

  private void seedData() {
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM cohort WHERE id = ?", Integer.class, TEST_COHORT_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO cohort (id, name, start_year, end_year) VALUES (?, ?, ?, ?)",
          TEST_COHORT_ID,
          "Test Cohort",
          2024,
          2027);
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branch WHERE id = ?", Integer.class, TEST_BRANCH_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO branch (id, code, name) VALUES (?, ?, ?)",
          TEST_BRANCH_ID,
          "CS",
          "Computer Science");
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM semester WHERE id = ?", Integer.class, TEST_SEMESTER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO semester (id, cohort_id, semester_number, academic_year) VALUES (?, ?, ?, ?)",
          TEST_SEMESTER_ID,
          TEST_COHORT_ID,
          1,
          "2024-2025");
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM groupe WHERE id = ?", Integer.class, TEST_GROUP_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO groupe (id, name, cohort_id, branch_id) VALUES (?, ?, ?, ?)",
          TEST_GROUP_ID,
          "G1",
          TEST_COHORT_ID,
          TEST_BRANCH_ID);
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM \"user\" WHERE id = ?", Integer.class, GRAD_USER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO \"user\" (id, email, password_hash, firstname, lastname, role, must_change_password) VALUES (?, ?, ?, ?, ?, ?, ?)",
          GRAD_USER_ID,
          "grad@hei.school",
          "hash",
          "Grad",
          "Student",
          "STUDENT",
          false);
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student WHERE user_id = ?", Integer.class, GRAD_USER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?)",
          GRAD_USER_ID,
          "STD24001",
          "ACTIVE");
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student_group_history WHERE student_id = ? AND group_id = ?",
            Integer.class,
            GRAD_USER_ID,
            TEST_GROUP_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO student_group_history (id, student_id, group_id, start_date) VALUES (?, ?, ?, CURRENT_DATE)",
          UUID.randomUUID(),
          GRAD_USER_ID,
          TEST_GROUP_ID);
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM course WHERE id = ?", Integer.class, TEST_COURSE_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO course (id, semester_id, branch_id, code, title, credits) VALUES (?, ?, ?, ?, ?, ?)",
          TEST_COURSE_ID,
          TEST_SEMESTER_ID,
          TEST_BRANCH_ID,
          "PROG1",
          "Programming",
          60);
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM exam WHERE id = ?", Integer.class, TEST_EXAM_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO exam (id, course_id, title, weight) VALUES (?, ?, ?, ?)",
          TEST_EXAM_ID,
          TEST_COURSE_ID,
          "Final",
          1.0);
    }
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM grade WHERE exam_id = ? AND student_id = ?",
            Integer.class,
            TEST_EXAM_ID,
            GRAD_USER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO grade (id, exam_id, student_id, score) VALUES (?, ?, ?, ?)",
          UUID.randomUUID(),
          TEST_EXAM_ID,
          GRAD_USER_ID,
          15.0);
    }
  }

  @Test
  void listCohorts_adminRole_returns200WithHtml() {
    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL, GET, new HttpEntity<>(adminHeaders()), String.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("Cohorts"));
    assertTrue(response.getBody().contains("Test Cohort"));
  }

  @Test
  void listCohorts_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL, GET, new HttpEntity<>(jsonHeaders()), String.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void listCohorts_studentRole_returns403() {
    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL, GET, new HttpEntity<>(studentHeaders()), String.class);

    assertEquals(
        org.springframework.http.HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void graduates_existingCohort_returns200WithHtml() {
    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL + "/" + TEST_COHORT_ID + "/graduates",
            GET,
            new HttpEntity<>(adminHeaders()),
            String.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("Graduates"));
    assertTrue(response.getBody().contains("Test Cohort"));
    assertTrue(response.getBody().contains("Download Excel"));
  }

  @Test
  void graduates_unknownId_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL + "/" + randomId + "/graduates",
            GET,
            new HttpEntity<>(adminHeaders()),
            String.class);

    assertEquals(
        org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void graduates_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL + "/" + TEST_COHORT_ID + "/graduates",
            GET,
            new HttpEntity<>(jsonHeaders()),
            String.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void graduates_studentRole_returns403() {
    var response =
        testRestTemplate.exchange(
            WEB_COHORTS_URL + "/" + TEST_COHORT_ID + "/graduates",
            GET,
            new HttpEntity<>(studentHeaders()),
            String.class);

    assertEquals(
        org.springframework.http.HttpStatus.FORBIDDEN, response.getStatusCode());
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

  private String tokenFor(Role role) {
    String email = (role == Role.ADMIN) ? "admin1@hei.school" : "student1@hei.school";
    var user =
        new CustomUserDetails(
            new User(UUID.randomUUID(), email, "Cohort", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    return headers;
  }
}
