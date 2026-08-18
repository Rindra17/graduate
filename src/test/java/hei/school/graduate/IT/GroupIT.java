package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.GroupsRequest;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.security.JwtService;
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

class GroupIT extends FacadeIT {

  private static final String GROUPS_URL = "/groups";

  private static final UUID TEST_COHORT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID TEST_BRANCH_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000011");
  private static final UUID TEST_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    ensureGroupReferenceData();
  }

  private void ensureGroupReferenceData() {
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM cohort WHERE id = ?", Integer.class, TEST_COHORT_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO cohort (id, name, start_year, end_year) VALUES (?, ?, ?, ?)",
          TEST_COHORT_ID,
          "Group Test Cohort",
          2024,
          2026);
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branch WHERE id = ?", Integer.class, TEST_BRANCH_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO branch (id, code, name) VALUES (?, ?, ?)",
          TEST_BRANCH_ID,
          "IT",
          "Information Technology");
    }
  }

  @Test
  void createGroup_validRequest_returns201() {
    var request = new GroupsRequest("K1", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().get("id"));
    assertEquals("K1", response.getBody().get("name"));
  }

  @Test
  void createGroup_studentRole_returns403() {
    var request = new GroupsRequest("K2", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, studentHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void createGroup_unauthorized_returns401() {
    var request = new GroupsRequest("K3", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void createGroup_nullName_returns400() {
    var request = new GroupsRequest(null, TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createGroup_emptyName_returns400() {
    var request = new GroupsRequest("", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createGroup_nameTooLong_returns400() {
    var request = new GroupsRequest("K12", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createGroup_nullCohortId_returns400() {
    var request = new GroupsRequest("K4", null, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createGroup_nullBranchId_returns400() {
    var request = new GroupsRequest("K5", TEST_COHORT_ID, null);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createGroup_nonExistentCohort_returns404() {
    var request = new GroupsRequest("K6", UUID.randomUUID(), TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void createGroup_nonExistentBranch_returns404() {
    var request = new GroupsRequest("K7", TEST_COHORT_ID, UUID.randomUUID());

    var response =
        testRestTemplate.exchange(
            GROUPS_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateGroup_validRequest_returns200() {
    UUID groupId = createTestGroup("U1");

    var request = new GroupsRequest("U2", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL + "/" + groupId, PUT, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("U2", response.getBody().get("name"));
  }

  @Test
  void updateGroup_notFound_returns404() {
    var request = new GroupsRequest("U3", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL + "/" + UUID.randomUUID(),
            PUT,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateGroup_studentRole_returns403() {
    UUID groupId = createTestGroup("U4");

    var request = new GroupsRequest("U5", TEST_COHORT_ID, TEST_BRANCH_ID);

    var response =
        testRestTemplate.exchange(
            GROUPS_URL + "/" + groupId,
            PUT,
            new HttpEntity<>(request, studentHeaders()),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void deleteGroup_validId_returns204() {
    UUID groupId = createTestGroup("D1");

    var response =
        testRestTemplate.exchange(
            GROUPS_URL + "/" + groupId, DELETE, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NO_CONTENT, response.getStatusCode());
  }

  @Test
  void deleteGroup_notFound_returns404() {
    var response =
        testRestTemplate.exchange(
            GROUPS_URL + "/" + UUID.randomUUID(),
            DELETE,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void deleteGroup_studentRole_returns403() {
    UUID groupId = createTestGroup("D2");

    var response =
        testRestTemplate.exchange(
            GROUPS_URL + "/" + groupId, DELETE, new HttpEntity<>(studentHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  private UUID createTestGroup(String name) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO groupe (id, name, cohort_id, branch_id) VALUES (?, ?, ?, ?)",
        id,
        name,
        TEST_COHORT_ID,
        TEST_BRANCH_ID);
    return id;
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
    String email = (role == Role.ADMIN) ? "group.admin@hei.school" : "group.student@hei.school";
    var user =
        new CustomUserDetails(
            new User(UUID.randomUUID(), email, "Group", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
