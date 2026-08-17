package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.GradeRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
import java.math.BigDecimal;
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
  private static final UUID GRADE_ID = UUID.fromString("00000000-0000-0000-0000-000000000008");
  private static final UUID SECOND_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000009");
  private static final UUID HISTORY_ID_1 = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  private static final UUID HISTORY_ID_2 = UUID.fromString("00000000-0000-0000-0000-00000000000b");
  private static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000000c");
  private static final UUID UPDATE_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-00000000000e");
  private static final UUID UPDATE_GRADE_ID =
      UUID.fromString("00000000-0000-0000-0000-00000000000f");
  private static final UUID CREATE_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID SECOND_UPDATE_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000011");
  private static final UUID SECOND_UPDATE_GRADE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000012");

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

    insertUser(ADMIN_USER_ID, "admin@hei.school", Role.ADMIN);
    insertUser(ASSIGNED_TEACHER_ID, "assigned@hei.school", Role.TEACHER);
    insertUser(OTHER_TEACHER_ID, "other@hei.school", Role.TEACHER);
    insertUser(STUDENT_ID, "student@hei.school", Role.STUDENT);
    insertUser(SECOND_STUDENT_ID, "other-student@hei.school", Role.STUDENT);
    insertUser(UPDATE_STUDENT_ID, "update-student@hei.school", Role.STUDENT);
    insertUser(CREATE_STUDENT_ID, "create-student@hei.school", Role.STUDENT);
    insertUser(SECOND_UPDATE_STUDENT_ID, "second-update-student@hei.school", Role.STUDENT);

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
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        SECOND_STUDENT_ID,
        "STD26002",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        UPDATE_STUDENT_ID,
        "STD26004",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        CREATE_STUDENT_ID,
        "STD26005",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        SECOND_UPDATE_STUDENT_ID,
        "STD26006",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO course_teacher (id, teacher_id, course_id) VALUES (?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        UUID.randomUUID(),
        ASSIGNED_TEACHER_ID,
        COURSE_ID);
    jdbcTemplate.update(
        "INSERT INTO grade (id, student_id, exam_id, score) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        GRADE_ID,
        STUDENT_ID,
        EXAM_ID,
        BigDecimal.valueOf(16.5));
    jdbcTemplate.update(
        "INSERT INTO grade (id, student_id, exam_id, score) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        UPDATE_GRADE_ID,
        UPDATE_STUDENT_ID,
        EXAM_ID,
        BigDecimal.valueOf(10.0));
    jdbcTemplate.update(
        "INSERT INTO grade (id, student_id, exam_id, score) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        SECOND_UPDATE_GRADE_ID,
        SECOND_UPDATE_STUDENT_ID,
        EXAM_ID,
        BigDecimal.valueOf(8.0));
    jdbcTemplate.update(
        "INSERT INTO grade_history "
            + "(id, grade_id, user_id, previous_score, new_score, reason, modification_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        HISTORY_ID_1,
        GRADE_ID,
        ASSIGNED_TEACHER_ID,
        null,
        BigDecimal.valueOf(12.0),
        "Initial grade",
        java.sql.Timestamp.valueOf("2026-01-01 00:00:00"));
    jdbcTemplate.update(
        "INSERT INTO grade_history "
            + "(id, grade_id, user_id, previous_score, new_score, reason, modification_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        HISTORY_ID_2,
        GRADE_ID,
        ASSIGNED_TEACHER_ID,
        BigDecimal.valueOf(12.0),
        BigDecimal.valueOf(16.5),
        "Correction",
        java.sql.Timestamp.valueOf("2026-01-02 00:00:00"));
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

  @Test
  void updateGrade_admin_updatesExistingGradeAndRecordsHistory() {
    var request = new GradeRequest(UPDATE_STUDENT_ID, BigDecimal.valueOf(13.0), "Recheck");

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.ADMIN, ADMIN_USER_ID))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(UPDATE_STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(EXAM_ID.toString(), response.getBody().get("examId"));
    assertEquals(UPDATE_GRADE_ID.toString(), response.getBody().get("id"));
    assertEquals("13.0", response.getBody().get("score").toString());

    var historyCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM grade_history WHERE grade_id = ?",
            Integer.class,
            UPDATE_GRADE_ID);
    assertEquals(1, historyCount);
    var previousScore =
        jdbcTemplate.queryForObject(
            "SELECT previous_score FROM grade_history WHERE grade_id = ?",
            BigDecimal.class,
            UPDATE_GRADE_ID);
    assertEquals(0, previousScore.compareTo(new BigDecimal("10.0")));
    var newScore =
        jdbcTemplate.queryForObject(
            "SELECT new_score FROM grade_history WHERE grade_id = ?",
            BigDecimal.class,
            UPDATE_GRADE_ID);
    assertEquals(0, newScore.compareTo(new BigDecimal("13.0")));
    var reason =
        jdbcTemplate.queryForObject(
            "SELECT reason FROM grade_history WHERE grade_id = ?", String.class, UPDATE_GRADE_ID);
    assertEquals("Recheck", reason);
  }

  @Test
  void updateGrade_assignedTeacher_returns200() {
    var request = new GradeRequest(SECOND_UPDATE_STUDENT_ID, BigDecimal.valueOf(12.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.TEACHER, ASSIGNED_TEACHER_ID))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void updateGrade_otherTeacher_returns403() {
    var request = new GradeRequest(UPDATE_STUDENT_ID, BigDecimal.valueOf(12.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.TEACHER, OTHER_TEACHER_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void updateGrade_student_returns403() {
    var request = new GradeRequest(UPDATE_STUDENT_ID, BigDecimal.valueOf(12.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.STUDENT, STUDENT_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void updateGrade_unauthorized_returns401() {
    var request = new GradeRequest(UPDATE_STUDENT_ID, BigDecimal.valueOf(12.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void updateGrade_createsGradeIfNotExists() {
    var request = new GradeRequest(CREATE_STUDENT_ID, BigDecimal.valueOf(17.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.ADMIN, ADMIN_USER_ID))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(CREATE_STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(EXAM_ID.toString(), response.getBody().get("examId"));
    assertEquals("17.0", response.getBody().get("score").toString());
    assertNotNull(response.getBody().get("id"));

    var gradeId = UUID.fromString(response.getBody().get("id").toString());
    var gradeScore =
        jdbcTemplate.queryForObject(
            "SELECT score FROM grade WHERE id = ?", BigDecimal.class, gradeId);
    assertEquals(0, gradeScore.compareTo(new BigDecimal("17.0")));
    var previousScore =
        jdbcTemplate.queryForObject(
            "SELECT previous_score FROM grade_history WHERE grade_id = ?",
            BigDecimal.class,
            gradeId);
    assertEquals(null, previousScore);
  }

  @Test
  void updateGrade_studentNotFound_returns404() {
    var request = new GradeRequest(UUID.randomUUID(), BigDecimal.valueOf(12.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.ADMIN, ADMIN_USER_ID))),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateGrade_examNotFound_returns404() {
    var request = new GradeRequest(UPDATE_STUDENT_ID, BigDecimal.valueOf(12.0));

    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + UUID.randomUUID() + "/grades-students",
            PUT,
            new HttpEntity<>(request, bearerHeaders(tokenFor(Role.ADMIN, ADMIN_USER_ID))),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getStudentGradeForExam_admin_returns200() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(EXAM_ID.toString(), response.getBody().get("examId"));
    assertEquals("16.5", response.getBody().get("score").toString());
    assertEquals(GRADE_ID.toString(), response.getBody().get("id"));
  }

  @Test
  void getStudentGradeForExam_assignedTeacher_returns200() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.TEACHER, ASSIGNED_TEACHER_ID))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(STUDENT_ID.toString(), response.getBody().get("studentId"));
  }

  @Test
  void getStudentGradeForExam_otherTeacher_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.TEACHER, OTHER_TEACHER_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getStudentGradeForExam_ownStudent_returns200() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.STUDENT, STUDENT_ID))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(EXAM_ID.toString(), response.getBody().get("examId"));
    assertEquals("16.5", response.getBody().get("score").toString());
  }

  @Test
  void getStudentGradeForExam_otherStudent_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + SECOND_STUDENT_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.STUDENT, STUDENT_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getStudentGradeForExam_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID,
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void getStudentGradeForExam_gradeNotFound_returns404() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + OTHER_TEACHER_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getStudentGradeForExam_examNotFound_returns404() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + UUID.randomUUID() + "/grades-students/" + STUDENT_ID,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getStudentGradeHistoryForExam_admin_returns200AndHistory() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(STUDENT_ID.toString(), response.getBody().get("id"));
    assertEquals(EXAM_ID.toString(), response.getBody().get("examId"));

    var grades = (List<Map<String, Object>>) response.getBody().get("grades");
    assertNotNull(grades);
    assertEquals(2, grades.size());
    assertEquals("12.0", grades.get(0).get("grade").toString());
    assertEquals(HISTORY_ID_1.toString(), grades.get(0).get("id"));
    assertEquals("Initial grade", grades.get(0).get("reason"));
    assertNotNull(grades.get(0).get("modificationDate"));
    assertEquals("16.5", grades.get(1).get("grade").toString());
    assertEquals(HISTORY_ID_2.toString(), grades.get(1).get("id"));
    assertEquals("Correction", grades.get(1).get("reason"));
    assertNotNull(grades.get(1).get("modificationDate"));
    assertEquals("16.5", response.getBody().get("currentScore").toString());
  }

  @Test
  void getStudentGradeHistoryForExam_assignedTeacher_returns200() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.TEACHER, ASSIGNED_TEACHER_ID))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getStudentGradeHistoryForExam_ownStudent_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.STUDENT, STUDENT_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getStudentGradeHistoryForExam_otherTeacher_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.TEACHER, OTHER_TEACHER_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getStudentGradeHistoryForExam_otherStudent_returns403() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + SECOND_STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.STUDENT, STUDENT_ID))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getStudentGradeHistoryForExam_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void getStudentGradeHistoryForExam_examNotFound_returns404() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + UUID.randomUUID() + "/grades-students/" + STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getStudentGradeHistoryForExam_noHistory_returnsEmptyGrades() {
    var response =
        testRestTemplate.exchange(
            GRADES_URL + "/" + EXAM_ID + "/grades-students/" + SECOND_STUDENT_ID + "/history",
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor(Role.ADMIN, UUID.randomUUID()))),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var grades = (List<Map<String, Object>>) response.getBody().get("grades");
    assertNotNull(grades);
    assertEquals(0, grades.size());
    assertEquals(null, response.getBody().get("currentScore"));
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
