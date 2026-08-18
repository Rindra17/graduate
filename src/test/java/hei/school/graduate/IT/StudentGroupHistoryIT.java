package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class StudentGroupHistoryIT extends FacadeIT {

  private static final String STUDENTS_URL = "/students";

  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");
  private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-000000000041");
  private static final UUID OLD_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
  private static final UUID CURRENT_GROUP_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000043");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000044");
  private static final UUID NO_HISTORY_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000045");
  private static final UUID OLD_HISTORY_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000046");
  private static final UUID CURRENT_HISTORY_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000047");
  private static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000048");

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
        "Group History Test Cohort",
        2024,
        2026);
    jdbcTemplate.update(
        "INSERT INTO branch (id, code, name) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
        BRANCH_ID,
        "IT",
        "Information Technology");
    jdbcTemplate.update(
        "INSERT INTO groupe (id, name, cohort_id, branch_id) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        OLD_GROUP_ID,
        "K1",
        COHORT_ID,
        BRANCH_ID);
    jdbcTemplate.update(
        "INSERT INTO groupe (id, name, cohort_id, branch_id) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        CURRENT_GROUP_ID,
        "K2",
        COHORT_ID,
        BRANCH_ID);

    insertUser(ADMIN_USER_ID, "gh.admin@hei.school", Role.ADMIN);
    insertUser(STUDENT_ID, "gh.student@hei.school", Role.STUDENT);
    insertUser(NO_HISTORY_STUDENT_ID, "gh.no-history@hei.school", Role.STUDENT);

    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        STUDENT_ID,
        "STD26030",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        NO_HISTORY_STUDENT_ID,
        "STD26031",
        "ACTIVE");

    jdbcTemplate.update(
        "INSERT INTO student_group_history "
            + "(id, student_id, group_id, start_date, end_date, change_reason) "
            + "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        OLD_HISTORY_ID,
        STUDENT_ID,
        OLD_GROUP_ID,
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2026, 1, 1),
        "Transferred to K2");
    jdbcTemplate.update(
        "INSERT INTO student_group_history "
            + "(id, student_id, group_id, start_date, end_date, change_reason) "
            + "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
        CURRENT_HISTORY_ID,
        STUDENT_ID,
        CURRENT_GROUP_ID,
        LocalDate.of(2026, 1, 1),
        null,
        "Initial assignment");
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
        "GroupHistory",
        "Tester",
        role.name(),
        null,
        false,
        null);
  }

  @Test
  void getGroupHistory_asAdmin_returnsChronologicalHistory() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group/history",
            GET,
            new HttpEntity<>(adminHeaders()),
            List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());

    var first = (Map<String, Object>) response.getBody().get(0);
    assertEquals(OLD_GROUP_ID.toString(), first.get("newGroupId"));
    assertNull(first.get("previousGroupId"));
    assertNull(first.get("previousGroupName"));
    assertEquals("K1", first.get("newGroupName"));

    var second = (Map<String, Object>) response.getBody().get(1);
    assertEquals(CURRENT_GROUP_ID.toString(), second.get("newGroupId"));
    assertEquals(OLD_GROUP_ID.toString(), second.get("previousGroupId"));
    assertEquals("K1", second.get("previousGroupName"));
    assertEquals("K2", second.get("newGroupName"));
    assertEquals("2026-01-01", second.get("transferDate"));
    assertEquals(STUDENT_ID.toString(), second.get("studentId"));
  }

  @Test
  void getGroupHistory_studentWithNoHistory_returnsEmptyList() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + NO_HISTORY_STUDENT_ID + "/group/history",
            GET,
            new HttpEntity<>(adminHeaders()),
            List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void getGroupHistory_unknownStudent_returns404() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID() + "/group/history",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Student not found", response.getBody().get("message"));
  }

  @Test
  void getGroupHistory_asOwner_returns403() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group/history",
            GET,
            new HttpEntity<>(studentHeaders(STUDENT_ID)),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getGroupHistory_asTeacher_returns403() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group/history",
            GET,
            new HttpEntity<>(teacherHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getGroupHistory_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group/history",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor(ADMIN_USER_ID, "gh.admin@hei.school", Role.ADMIN));
  }

  private HttpHeaders studentHeaders(UUID id) {
    return bearerHeaders(tokenFor(id, "gh.student@hei.school", Role.STUDENT));
  }

  private HttpHeaders teacherHeaders() {
    return bearerHeaders(tokenFor(ADMIN_USER_ID, "gh.teacher@hei.school", Role.TEACHER));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(UUID id, String email, Role role) {
    var user =
        new CustomUserDetails(
            new User(id, email, "GroupHistory", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
