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

class StudentGroupIT extends FacadeIT {

  private static final String STUDENTS_URL = "/students";

  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
  private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
  private static final UUID CURRENT_GROUP_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000022");
  private static final UUID OLD_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000023");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000024");
  private static final UUID NO_GROUP_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000025");
  private static final UUID CURRENT_HISTORY_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000026");
  private static final UUID OLD_HISTORY_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000027");
  private static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000028");

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
        "Student Group Test Cohort",
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

    insertUser(ADMIN_USER_ID, "sg.admin@hei.school", Role.ADMIN);
    insertUser(STUDENT_ID, "sg.student@hei.school", Role.STUDENT);
    insertUser(NO_GROUP_STUDENT_ID, "sg.no-group@hei.school", Role.STUDENT);

    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        STUDENT_ID,
        "STD26010",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        NO_GROUP_STUDENT_ID,
        "STD26011",
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
        "StudentGroup",
        "Tester",
        role.name(),
        null,
        false,
        null);
  }

  @Test
  void getStudentGroup_asAdmin_returnsCurrentGroup() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(CURRENT_GROUP_ID.toString(), response.getBody().get("id"));
    assertEquals("K2", response.getBody().get("name"));
    assertNotNull(response.getBody().get("cohort"));
    assertNotNull(response.getBody().get("branch"));
  }

  @Test
  void getStudentGroup_asOwner_returnsCurrentGroup() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group",
            GET,
            new HttpEntity<>(studentHeaders(STUDENT_ID)),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(CURRENT_GROUP_ID.toString(), response.getBody().get("id"));
  }

  @Test
  void getStudentGroup_asOtherStudent_returns403() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group",
            GET,
            new HttpEntity<>(studentHeaders(NO_GROUP_STUDENT_ID)),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getStudentGroup_unknownStudent_returns404() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID() + "/group",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Student not found", response.getBody().get("message"));
  }

  @Test
  void getStudentGroup_studentWithoutGroup_returns404() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + NO_GROUP_STUDENT_ID + "/group",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Group not found", response.getBody().get("message"));
  }

  @Test
  void getStudentGroup_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor(ADMIN_USER_ID, "sg.admin@hei.school", Role.ADMIN));
  }

  private HttpHeaders studentHeaders(UUID id) {
    return bearerHeaders(tokenFor(id, "sg.student@hei.school", Role.STUDENT));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(UUID id, String email, Role role) {
    var user =
        new CustomUserDetails(
            new User(id, email, "StudentGroup", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
