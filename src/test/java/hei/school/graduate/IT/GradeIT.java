package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import java.math.BigDecimal;
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

class GradeIT extends FacadeIT {

  private static final String GRADES_URL = "/exams";

  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID SEMESTER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID COURSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID EXAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
  private static final UUID ASSIGNED_TEACHER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000005");
  private static final UUID OTHER_TEACHER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000006");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    seedData();
  }

  private void seedData() {
    jdbcTemplate.update(
        "INSERT INTO cohort (id, name, start_year, end_year) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        COHORT_ID,
        "Grade Test Cohort",
        2024,
        2026);
    jdbcTemplate.update(
        "INSERT INTO branch (id, code, name) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
        BRANCH_ID,
        "CS",
        "Computer Science");
    jdbcTemplate.update(
        "INSERT INTO semester (id, cohort_id, semester_number, academic_year) "
            + "VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        SEMESTER_ID,
        COHORT_ID,
        1,
        "2024-2025");
    jdbcTemplate.update(
        "INSERT INTO course (id, semester_id, branch_id, code, title, credits) "
            + "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        COURSE_ID,
        SEMESTER_ID,
        BRANCH_ID,
        "PROG4",
        "Programming 4",
        6);
    jdbcTemplate.update(
        "INSERT INTO exam (id, course_id, title, weight, exam_date) "
            + "VALUES (?, ?, ?, ?, CURRENT_DATE) ON CONFLICT (id) DO NOTHING",
        EXAM_ID,
        COURSE_ID,
        "Final Exam",
        BigDecimal.valueOf(0.5));

    insertUser(ASSIGNED_TEACHER_ID, "assigned@hei.school", Role.TEACHER);
    insertUser(OTHER_TEACHER_ID, "other@hei.school", Role.TEACHER);
    insertUser(STUDENT_ID, "student@hei.school", Role.STUDENT);

    jdbcTemplate.update(
        "INSERT INTO teacher (user_id, reference) VALUES (?, ?) ON CONFLICT (user_id) DO NOTHING",
        ASSIGNED_TEACHER_ID,
        "TCR26001");
    jdbcTemplate.update(
        "INSERT INTO teacher (user_id, reference) VALUES (?, ?) ON CONFLICT (user_id) DO NOTHING",
        OTHER_TEACHER_ID,
        "TCR26002");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        STUDENT_ID,
        "STD26001",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO course_teacher (id, teacher_id, course_id) VALUES (?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        UUID.randomUUID(),
        ASSIGNED_TEACHER_ID,
        COURSE_ID);
  }

  private void insertUser(UUID id, String email, Role role) {
    jdbcTemplate.update(
        "INSERT INTO \"user\" "
            + "(id, email, password_hash, firstname, lastname, role, address,"
            + " must_change_password, entrance_date_time) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        id,
        email,
        "hash",
        "Grade",
        "Tester",
        role.name(),
        null,
        false,
        null);
  }

  @Test
  void getGrades_admin_returns200() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            java.util.List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getGrades_assignedTeacher_returns200() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.TEACHER, ASSIGNED_TEACHER_ID))),
            java.util.List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getGrades_otherTeacher_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.TEACHER, OTHER_TEACHER_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getGrades_student_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.STUDENT, STUDENT_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getGrades_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void getGrades_examNotFound_returns404() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + UUID.randomUUID() + "/grades-students",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  private String tokenFor(Role role, UUID id) {
    return jwtService.generateToken(
        new CustomUserDetails(
            new User(id, "grade@hei.school", "Grade", "Tester", role, null, null, false, null)));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
