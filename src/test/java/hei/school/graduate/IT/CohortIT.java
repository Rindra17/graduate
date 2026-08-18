package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.CohortRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
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

class CohortIT extends FacadeIT {

  private static final String COHORTS_URL = "/cohorts";

  private static final UUID TEST_COHORT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID TEST_BRANCH_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID TEST_STUDENT_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000010");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    ensureCohortReferenceData();
  }

  private void ensureCohortReferenceData() {
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
            "SELECT COUNT(*) FROM \"user\" WHERE id = ?", Integer.class, TEST_STUDENT_USER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO \"user\" (id, email, password_hash, firstname, lastname, role,"
              + " must_change_password) VALUES (?, ?, ?, ?, ?, ?, ?)",
          TEST_STUDENT_USER_ID,
          "cohortstudent@hei.school",
          "hash",
          "Alice",
          "Dupont",
          "STUDENT",
          false);
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student WHERE user_id = ?", Integer.class, TEST_STUDENT_USER_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO student (user_id, reference, status) VALUES (?, ?, ?)",
          TEST_STUDENT_USER_ID,
          "STU001",
          "ACTIVE");
    }

    List<UUID> existingGroups =
        jdbcTemplate.queryForList(
            "SELECT id FROM groupe WHERE cohort_id = ?", UUID.class, TEST_COHORT_ID);
    UUID groupId;
    if (existingGroups.isEmpty()) {
      groupId = UUID.randomUUID();
      jdbcTemplate.update(
          "INSERT INTO groupe (id, name, cohort_id, branch_id) VALUES (?, ?, ?, ?)",
          groupId,
          "Group A",
          TEST_COHORT_ID,
          TEST_BRANCH_ID);
    } else {
      groupId = existingGroups.get(0);
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student_group_history WHERE student_id = ? AND group_id = ?",
            Integer.class,
            TEST_STUDENT_USER_ID,
            groupId)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO student_group_history (id, student_id, group_id, start_date) VALUES (?, ?,"
              + " ?, CURRENT_DATE)",
          UUID.randomUUID(),
          TEST_STUDENT_USER_ID,
          groupId);
    }
  }

  @Test
  void createCohort_validRequest_returns201() {
    var request = CohortRequest.builder().name("Promo 2025").startYear(2025).build();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Promo 2025", response.getBody().get("name"));
    assertEquals(2025, response.getBody().get("startYear"));
  }

  @Test
  void createCohort_blankName_returns400() {
    var request = CohortRequest.builder().name("").startYear(2025).build();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createCohort_nullStartYear_returns400() {
    var request = CohortRequest.builder().name("Promo 2025").startYear(null).build();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createCohort_unauthorized_returns401() {
    var request = CohortRequest.builder().name("Promo 2025").startYear(2025).build();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL, POST, new HttpEntity<>(request, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void getCohortResults_existingCohort_returns200() {
    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + TEST_COHORT_ID + "/result",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_COHORT_ID.toString(), response.getBody().get("cohortId"));
    assertEquals("Test Cohort", response.getBody().get("cohortName"));
  }

  @Test
  void getCohortResults_unknownId_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + randomId + "/result",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getCohortResults_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + TEST_COHORT_ID + "/result",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void getCohortGraduates_existingCohort_returns200() {
    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + TEST_COHORT_ID + "/graduate",
            GET,
            new HttpEntity<>(adminHeaders()),
            List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getCohortGraduates_unknownId_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + randomId + "/graduate",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getCohortGraduates_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + TEST_COHORT_ID + "/graduate",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void createCohort_studentRole_returns403() {
    var request = CohortRequest.builder().name("Promo 2025").startYear(2025).build();

    var response =
        testRestTemplate.exchange(
            COHORTS_URL, POST, new HttpEntity<>(request, studentHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getCohortResults_studentRole_returns403() {
    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + TEST_COHORT_ID + "/result",
            GET,
            new HttpEntity<>(studentHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void getCohortGraduates_studentRole_returns403() {
    var response =
        testRestTemplate.exchange(
            COHORTS_URL + "/" + TEST_COHORT_ID + "/graduate",
            GET,
            new HttpEntity<>(studentHeaders()),
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
    String email = (role == Role.ADMIN) ? "admin1@hei.school" : "student1@hei.school";
    var user =
        new CustomUserDetails(
            new User(UUID.randomUUID(), email, "Cohort", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
