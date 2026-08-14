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
import hei.school.graduate.endpoint.rest.controller.dto.CourseRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CourseTeacherRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
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

class CourseIT extends FacadeIT {

  private static final String COURSES_URL = "/courses";

  private static final UUID TEST_COHORT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final UUID TEST_SEMESTER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TEST_BRANCH_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    ensureCourseReferenceData();
  }

  private void ensureCourseReferenceData() {
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
  }

  @Test
  void getAllCourses_returns200AndList() {
    var response =
        testRestTemplate.exchange(COURSES_URL, GET, new HttpEntity<>(adminHeaders()), List.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getAllCourses_unauthorized_returns401() {
    var response =
        testRestTemplate.exchange(COURSES_URL, GET, new HttpEntity<>(jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void getCourseById_notFound_returns404() {
    var randomId = UUID.randomUUID();
    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void createCourse_validRequest_returns200Or201() {
    var request = courseRequest("PROG1", "Algorithmics & Programming", 6);

    var response =
        testRestTemplate.exchange(
            COURSES_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
  }

  @Test
  void createCourse_studentRole_returns403() {
    var request = courseRequest("PROG2", "Object Oriented Programming", 5);

    var response =
        testRestTemplate.exchange(
            COURSES_URL, POST, new HttpEntity<>(request, studentHeaders()), Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }

  @Test
  void updateCourse_notFound_returns404() {
    var randomId = UUID.randomUUID();
    var request = courseRequest("UPD1", "Updated Title", 4);

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId,
            PUT,
            new HttpEntity<>(request, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void deleteCourse_notFound_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId, DELETE, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getTeachersByCourseId_notFound_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId + "/teachers",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addTeacherToCourse_nonExistentTeacher_returns404() {
    var courseId = UUID.randomUUID();
    var teacherReq = teacherRequest(UUID.randomUUID());

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + courseId + "/teachers",
            POST,
            new HttpEntity<>(teacherReq, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeTeacherFromCourse_notFound_returns404() {
    var courseId = UUID.randomUUID();
    var teacherId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + courseId + "/teachers/" + teacherId,
            DELETE,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getGroupsByCourseId_notFound_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId + "/groups",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getExamsByCourseId_notFound_returns404() {
    var randomId = UUID.randomUUID();

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId + "/exams",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  void createExam_notFoundCourse_returns404() {
    var randomId = UUID.randomUUID();
    var examRequest =
        ExamRequest.builder()
            .title("Final Exam")
            .weight(BigDecimal.valueOf(0.5))
            .examDate(LocalDate.now().plusDays(10))
            .build();

    var response =
        testRestTemplate.exchange(
            COURSES_URL + "/" + randomId + "/exams",
            POST,
            new HttpEntity<>(examRequest, adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  private CourseRequest courseRequest(String code, String title, int credits) {
    return CourseRequest.builder()
        .semesterId(TEST_SEMESTER_ID)
        .branchId(TEST_BRANCH_ID)
        .code(code)
        .title(title)
        .credits(credits)
        .build();
  }

  private CourseTeacherRequest teacherRequest(UUID teacherId) {
    return CourseTeacherRequest.builder().teacherId(teacherId).build();
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
            new User(UUID.randomUUID(), email, "Course", "Tester", role, null, null, false, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
