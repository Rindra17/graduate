package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.endpoint.rest.controller.dto.GradeRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.jdbc.core.JdbcTemplate;

class ExamIT extends FacadeIT {

  private static final String EXAMS_URL = "/exams";

  private static final UUID TEST_COHORT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID TEST_SEMESTER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEST_BRANCH_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEST_COURSE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID TEST_EXAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID TEST_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000005");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    ensureExamReferenceData();
  }

  private void ensureExamReferenceData() {
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM cohort WHERE id = ?", Integer.class, TEST_COHORT_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO cohort (id, name, start_year, end_year) VALUES (?, ?, ?, ?)",
          TEST_COHORT_ID,
          "Test Cohort",
          2024,
          2026);
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
          "INSERT INTO semester (id, cohort_id, semester_number, academic_year) VALUES (?, ?, ?,"
              + " ?)",
          TEST_SEMESTER_ID,
          TEST_COHORT_ID,
          1,
          "2024-2025");
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM course WHERE id = ?", Integer.class, TEST_COURSE_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO course (id, semester_id, branch_id, code, title, credits) VALUES (?, ?, ?,"
              + " ?, ?, ?)",
          TEST_COURSE_ID,
          TEST_SEMESTER_ID,
          TEST_BRANCH_ID,
          "PROG1",
          "Algorithmics & Programming",
          6);
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM exam WHERE id = ?", Integer.class, TEST_EXAM_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO exam (id, course_id, title, weight, exam_date) VALUES (?, ?, ?, ?, ?)",
          TEST_EXAM_ID,
          TEST_COURSE_ID,
          "Midterm Exam",
          BigDecimal.valueOf(0.5),
          LocalDate.now().plusDays(10));
    } else {
      jdbcTemplate.update(
          "UPDATE exam SET title = ?, weight = ?, exam_date = ? WHERE id = ?",
          "Midterm Exam",
          BigDecimal.valueOf(0.5),
          LocalDate.now().plusDays(10),
          TEST_EXAM_ID);
    }

    jdbcTemplate.update("DELETE FROM grade WHERE exam_id = ?", TEST_EXAM_ID);

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM \"user\" WHERE id = ?", Integer.class, TEST_STUDENT_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO \"user\" (id, email, password_hash, firstname, lastname, role,"
              + " must_change_password) VALUES (?, ?, ?, ?, ?, 'STUDENT', FALSE)",
          TEST_STUDENT_ID,
          "exam.student@hei.school",
          "hashed-password",
          "Jane",
          "Doe");
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student WHERE user_id = ?", Integer.class, TEST_STUDENT_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?)",
          TEST_STUDENT_ID,
          "STU00001",
          "ACTIVE");
    }
  }

  @Test
  void getExam_validId_returnsExam() {
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_EXAM_ID.toString(), response.getBody().get("id"));
    assertEquals("Midterm Exam", response.getBody().get("title"));
    assertEquals("Algorithmics & Programming", response.getBody().get("courseName"));
    assertEquals(0.5, ((Number) response.getBody().get("weight")).doubleValue());
    assertNotNull(response.getBody().get("examDate"));
  }

  @Test
  void getExam_notFound_returns404() {
    var randomId = UUID.randomUUID();
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + randomId, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getExam_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID, GET, new HttpEntity<>(jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void updateExam_validRequest_returnsUpdatedExam() {
    var request =
        ExamRequest.builder()
            .title("Updated Exam")
            .weight(BigDecimal.valueOf(0.6))
            .examDate(LocalDate.now().plusDays(20))
            .build();

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID,
            PUT,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_EXAM_ID.toString(), response.getBody().get("id"));
    assertEquals("Updated Exam", response.getBody().get("title"));
    assertEquals(0.6, ((Number) response.getBody().get("weight")).doubleValue());
  }

  @Test
  void updateExam_notFound_returns404() {
    var randomId = UUID.randomUUID();
    var request =
        ExamRequest.builder()
            .title("Updated Exam")
            .weight(BigDecimal.valueOf(0.6))
            .examDate(LocalDate.now().plusDays(20))
            .build();

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + randomId, PUT, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateExam_studentRole_returns403() {
    var request =
        ExamRequest.builder()
            .title("Updated Exam")
            .weight(BigDecimal.valueOf(0.6))
            .examDate(LocalDate.now().plusDays(20))
            .build();

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID,
            PUT,
            new HttpEntity<>(request, studentHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void deleteExam_validId_returns200ThenGone() {
    var deleteResponse =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID, DELETE, new HttpEntity<>(adminHeaders()), Map.class);

    assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

    var getResponse =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, getResponse.getStatusCode());
  }

  @Test
  void deleteExam_notFound_returns404() {
    var randomId = UUID.randomUUID();
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + randomId, DELETE, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void deleteExam_studentRole_returns403() {
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID, DELETE, new HttpEntity<>(studentHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getGradesByExamId_withoutGrades_returnsEmptyList() {
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID + "/grades",
            GET,
            new HttpEntity<>(adminHeaders()),
            List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void getGradesByExamId_notFound_returns404() {
    var randomId = UUID.randomUUID();
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + randomId + "/grades",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addGradeToExam_validRequest_returnsGrade() {
    var request = new GradeRequest(TEST_STUDENT_ID, BigDecimal.valueOf(15.5));

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID + "/grades",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(TEST_STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(TEST_EXAM_ID.toString(), response.getBody().get("examId"));
    assertEquals(15.5, ((Number) response.getBody().get("score")).doubleValue());
  }

  @Test
  void addGradeToExam_examNotFound_returns404() {
    var randomId = UUID.randomUUID();
    var request = new GradeRequest(TEST_STUDENT_ID, BigDecimal.valueOf(15.5));

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + randomId + "/grades",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addGradeToExam_studentNotFound_returns404() {
    var request = new GradeRequest(UUID.randomUUID(), BigDecimal.valueOf(15.5));

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID + "/grades",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addGradeToExam_studentRole_returns403() {
    var request = new GradeRequest(TEST_STUDENT_ID, BigDecimal.valueOf(15.5));

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID + "/grades",
            POST,
            new HttpEntity<>(request, studentHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
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
    String email = (role == Role.ADMIN) ? "exam.admin@hei.school" : "exam.student@hei.school";
    var user =
        new CustomUserDetails(
            new User(UUID.randomUUID(), email, "Exam", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
