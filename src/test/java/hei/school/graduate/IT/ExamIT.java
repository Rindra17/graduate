package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
      UUID.fromString("10000000-0000-0000-0000-000000000000");
  private static final UUID TEST_SEMESTER_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000001");
  private static final UUID TEST_BRANCH_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000002");
  private static final UUID TEST_COURSE_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000003");
  private static final UUID TEST_EXAM_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");
  private static final UUID TEST_STUDENT_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000005");
  private static final UUID TEST_TEACHER_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000006");
  private static final UUID OTHER_COURSE_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000007");
  private static final UUID OTHER_EXAM_ID = UUID.fromString("10000000-0000-0000-0000-000000000008");

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

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM \"user\" WHERE id = ?", Integer.class, TEST_TEACHER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO \"user\" (id, email, password_hash, firstname, lastname, role,"
              + " must_change_password) VALUES (?, ?, ?, ?, ?, 'TEACHER', FALSE)",
          TEST_TEACHER_ID,
          "exam.teacher@hei.school",
          "hashed-password",
          "John",
          "Teacher");
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM teacher WHERE user_id = ?", Integer.class, TEST_TEACHER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO teacher (user_id, reference) VALUES (?, ?)", TEST_TEACHER_ID, "TCH00001");
    }

    jdbcTemplate.update(
        "DELETE FROM course_teacher WHERE teacher_id = ? AND course_id = ?",
        TEST_TEACHER_ID,
        TEST_COURSE_ID);
    jdbcTemplate.update(
        "INSERT INTO course_teacher (teacher_id, course_id) VALUES (?, ?)",
        TEST_TEACHER_ID,
        TEST_COURSE_ID);

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM course WHERE id = ?", Integer.class, OTHER_COURSE_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO course (id, semester_id, branch_id, code, title, credits) VALUES (?, ?, ?,"
              + " ?, ?, ?)",
          OTHER_COURSE_ID,
          TEST_SEMESTER_ID,
          TEST_BRANCH_ID,
          "PROG2",
          "Other Course",
          4);
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM exam WHERE id = ?", Integer.class, OTHER_EXAM_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO exam (id, course_id, title, weight, exam_date) VALUES (?, ?, ?, ?, ?)",
          OTHER_EXAM_ID,
          OTHER_COURSE_ID,
          "Other Exam",
          BigDecimal.valueOf(0.4),
          LocalDate.now().plusDays(15));
    } else {
      jdbcTemplate.update(
          "UPDATE exam SET title = ?, weight = ?, exam_date = ? WHERE id = ?",
          "Other Exam",
          BigDecimal.valueOf(0.4),
          LocalDate.now().plusDays(15),
          OTHER_EXAM_ID);
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
  void deleteExam_validId_returns204ThenGone() {
    var deleteResponse =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID, DELETE, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NO_CONTENT, deleteResponse.getStatusCode());

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
  void createExam_teacherAssignedToCourse_returns201() {
    var request =
        ExamRequest.builder()
            .title("Teacher Created Exam")
            .weight(BigDecimal.valueOf(0.3))
            .examDate(LocalDate.now().plusDays(5))
            .build();

    var response =
        testRestTemplate.exchange(
            "/courses/" + TEST_COURSE_ID + "/exams",
            POST,
            new HttpEntity<>(request, teacherHeaders()),
            Map.class);

    assertEquals(CREATED, response.getStatusCode());
  }

  @Test
  void createExam_teacherNotAssignedToCourse_returns403() {
    var request =
        ExamRequest.builder()
            .title("Unauthorized Exam")
            .weight(BigDecimal.valueOf(0.3))
            .examDate(LocalDate.now().plusDays(5))
            .build();

    var response =
        testRestTemplate.exchange(
            "/courses/" + OTHER_COURSE_ID + "/exams",
            POST,
            new HttpEntity<>(request, teacherHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void updateExam_teacherAssignedToCourse_returns200() {
    var request =
        ExamRequest.builder()
            .title("Teacher Updated Exam")
            .weight(BigDecimal.valueOf(0.6))
            .examDate(LocalDate.now().plusDays(20))
            .build();

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + TEST_EXAM_ID,
            PUT,
            new HttpEntity<>(request, teacherHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Teacher Updated Exam", response.getBody().get("title"));
  }

  @Test
  void updateExam_teacherNotAssignedToCourse_returns403() {
    var request =
        ExamRequest.builder()
            .title("Unauthorized Update")
            .weight(BigDecimal.valueOf(0.6))
            .examDate(LocalDate.now().plusDays(20))
            .build();

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + OTHER_EXAM_ID,
            PUT,
            new HttpEntity<>(request, teacherHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void deleteExam_teacherAssignedToCourse_returns204() {
    UUID deleteExamId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO exam (id, course_id, title, weight, exam_date) VALUES (?, ?, ?, ?, ?)",
        deleteExamId,
        TEST_COURSE_ID,
        "Exam To Delete",
        BigDecimal.valueOf(0.1),
        LocalDate.now().plusDays(1));

    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + deleteExamId, DELETE, new HttpEntity<>(teacherHeaders()), Map.class);

    assertEquals(NO_CONTENT, response.getStatusCode());
  }

  @Test
  void deleteExam_teacherNotAssignedToCourse_returns403() {
    var response =
        testRestTemplate.exchange(
            EXAMS_URL + "/" + OTHER_EXAM_ID, DELETE, new HttpEntity<>(teacherHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(Role.ADMIN);
  }

  private HttpHeaders studentHeaders() {
    return bearerHeaders(Role.STUDENT);
  }

  private HttpHeaders teacherHeaders() {
    var headers = jsonHeaders();
    headers.setBearerAuth(tokenForId(TEST_TEACHER_ID, Role.TEACHER, "exam.teacher@hei.school"));
    return headers;
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

  private String tokenForId(UUID id, Role role, String email) {
    var user =
        new CustomUserDetails(new User(id, email, "Exam", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
