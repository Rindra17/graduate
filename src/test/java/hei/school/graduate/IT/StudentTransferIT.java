package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.TransferRequest;
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

class StudentTransferIT extends FacadeIT {

  private static final String STUDENTS_URL = "/students";

  private static final UUID COHORT_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
  private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
  private static final UUID CURRENT_GROUP_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000032");
  private static final UUID TARGET_GROUP_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000033");
  private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000034");
  private static final UUID NO_GROUP_STUDENT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000035");
  private static final UUID CURRENT_HISTORY_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000036");
  private static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000037");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    jdbcTemplate.update("DELETE FROM STUDENT_GROUP_HISTORY");
    seedData();
  }

  private void seedData() {
    jdbcTemplate.update(
        "INSERT INTO cohort (id, name, start_year, end_year) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        COHORT_ID,
        "Transfer Test Cohort",
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
        CURRENT_GROUP_ID,
        "K1",
        COHORT_ID,
        BRANCH_ID);
    jdbcTemplate.update(
        "INSERT INTO groupe (id, name, cohort_id, branch_id) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (id) DO NOTHING",
        TARGET_GROUP_ID,
        "K2",
        COHORT_ID,
        BRANCH_ID);

    insertUser(ADMIN_USER_ID, "tr.admin@hei.school", Role.ADMIN);
    insertUser(STUDENT_ID, "tr.student@hei.school", Role.STUDENT);
    insertUser(NO_GROUP_STUDENT_ID, "tr.no-group@hei.school", Role.STUDENT);

    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        STUDENT_ID,
        "STD26020",
        "ACTIVE");
    jdbcTemplate.update(
        "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id) DO NOTHING",
        NO_GROUP_STUDENT_ID,
        "STD26021",
        "ACTIVE");

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
        "Transfer",
        "Tester",
        role.name(),
        null,
        false,
        null);
  }

  @Test
  void transferStudent_validRequest_returns200AndNewGroup() {
    var request = new TransferRequest(TARGET_GROUP_ID, "Redoublement");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(CURRENT_GROUP_ID.toString(), response.getBody().get("previousGroupId"));
    assertEquals(TARGET_GROUP_ID.toString(), response.getBody().get("newGroupId"));
    assertEquals("Redoublement", response.getBody().get("reason"));
    assertNotNull(response.getBody().get("transferDate"));

    var currentGroup =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/group",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(OK, currentGroup.getStatusCode());
    assertEquals(TARGET_GROUP_ID.toString(), currentGroup.getBody().get("id"));
  }

  @Test
  void transferStudent_initialAssignment_returnsNullPreviousGroup() {
    var request = new TransferRequest(TARGET_GROUP_ID, null);

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + NO_GROUP_STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNull(response.getBody().get("previousGroupId"));
    assertEquals(TARGET_GROUP_ID.toString(), response.getBody().get("newGroupId"));
  }

  @Test
  void transferStudent_sameGroup_returns400() {
    var request = new TransferRequest(CURRENT_GROUP_ID, "No change");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().get("status"));
  }

  @Test
  void transferStudent_missingGroupId_returns400() {
    var request = new TransferRequest(null, "No target");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().get("status"));
  }

  @Test
  void transferStudent_unknownStudent_returns404() {
    var request = new TransferRequest(TARGET_GROUP_ID, "Transfer");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID() + "/transfer",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Student not found", response.getBody().get("message"));
  }

  @Test
  void transferStudent_unknownGroup_returns404() {
    var request = new TransferRequest(UUID.randomUUID(), "Transfer");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Group not found", response.getBody().get("message"));
  }

  @Test
  void transferStudent_asOwner_returns200() {
    var request = new TransferRequest(TARGET_GROUP_ID, "Self transfer");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, studentHeaders(STUDENT_ID)),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(STUDENT_ID.toString(), response.getBody().get("studentId"));
    assertEquals(TARGET_GROUP_ID.toString(), response.getBody().get("newGroupId"));
  }

  @Test
  void transferStudent_asOtherStudent_returns403() {
    var request = new TransferRequest(TARGET_GROUP_ID, "Transfer");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, studentHeaders(NO_GROUP_STUDENT_ID)),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void transferStudent_withoutAuth_returns401() {
    var request = new TransferRequest(TARGET_GROUP_ID, "Transfer");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + STUDENT_ID + "/transfer",
            POST,
            new HttpEntity<>(request, jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor(ADMIN_USER_ID, "tr.admin@hei.school", Role.ADMIN));
  }

  private HttpHeaders studentHeaders(UUID id) {
    return bearerHeaders(tokenFor(id, "tr.student@hei.school", Role.STUDENT));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(UUID id, String email, Role role) {
    var user =
        new CustomUserDetails(
            new User(id, email, "Transfer", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
