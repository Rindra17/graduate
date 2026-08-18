package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.mapper.UserMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.repository.UserRepository;
import hei.school.graduate.security.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
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
import org.springframework.transaction.support.TransactionTemplate;

class StudentIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String STUDENTS_URL = "/students";

  private static final UUID GRADES_COHORT_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000001");
  private static final UUID GRADES_BRANCH_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000002");
  private static final UUID GRADES_SEMESTER_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000003");
  private static final UUID OTHER_SEMESTER_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000004");
  private static final UUID GRADES_COURSE_A_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000005");
  private static final UUID GRADES_COURSE_B_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000006");
  private static final UUID GRADES_COURSE_OTHER_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000007");
  private static final UUID GRADES_EXAM_A1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000008");
  private static final UUID GRADES_EXAM_A2_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000009");
  private static final UUID GRADES_EXAM_A3_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000000a");
  private static final UUID GRADES_EXAM_B1_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000000b");
  private static final UUID GRADES_EXAM_OTHER_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000000c");
  private static final UUID GRADES_COURSE_C_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000000d");
  private static final UUID GRADES_EXAM_C1_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000000e");
  private static final UUID GRADES_EXAM_C2_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000000f");
  private static final UUID GRADES_COURSE_DONNEES1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000010");
  private static final UUID GRADES_COURSE_WEB1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000011");
  private static final UUID GRADES_COURSE_SYS1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000012");
  private static final UUID GRADES_COURSE_LV1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000013");
  private static final UUID GRADES_COURSE_SYS2_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000014");
  private static final UUID GRADES_COURSE_WEB2_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000015");
  private static final UUID GRADES_COURSE_THEORIE1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000016");
  private static final UUID GRADES_COURSE_MGT1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000017");
  private static final UUID GRADES_EXAM_DONNEES1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000018");
  private static final UUID GRADES_EXAM_WEB1_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000019");
  private static final UUID GRADES_EXAM_SYS1_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000001a");
  private static final UUID GRADES_EXAM_LV1_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000001b");
  private static final UUID GRADES_EXAM_SYS2_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000001c");
  private static final UUID GRADES_EXAM_WEB2_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000001d");
  private static final UUID GRADES_EXAM_THEORIE1_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000001e");
  private static final UUID GRADES_EXAM_MGT1_ID =
      UUID.fromString("10000000-0000-0000-0000-00000000001f");

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired JwtService jwtService;

  @Autowired UserRepository userRepository;

  @Autowired UserMapper userMapper;

  @Autowired JdbcTemplate jdbcTemplate;

  @PersistenceContext EntityManager entityManager;

  @Autowired TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    transactionTemplate.executeWithoutResult(
        status -> {
          entityManager.createNativeQuery("DELETE FROM STUDENT").executeUpdate();
          entityManager.createNativeQuery("DELETE FROM \"user\"").executeUpdate();
        });
  }

  @Test
  void getStudents_withData_returnsPageOfStudents() {
    registerStudent("student-one@example.com");
    registerStudent("student-two@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "?page=0&size=10", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(10, response.getBody().get("size"));
    assertTrue(((Number) response.getBody().get("totalElements")).intValue() >= 2);
    assertEquals(1, response.getBody().get("totalPages"));
    var students = (List<Map<String, Object>>) response.getBody().get("students");
    assertTrue(students.stream().anyMatch(s -> "student-one@example.com".equals(s.get("email"))));
    assertTrue(students.stream().anyMatch(s -> "student-two@example.com".equals(s.get("email"))));
    assertTrue(students.stream().allMatch(s -> s.containsKey("reference")));
    assertTrue(students.stream().allMatch(s -> s.containsKey("status")));
  }

  @Test
  void getStudents_withPagination_returnsRequestedSlice() {
    registerStudent("page-a@example.com");
    registerStudent("page-b@example.com");
    registerStudent("page-c@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "?page=0&size=2", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(2, response.getBody().get("size"));
    assertEquals(2, ((List<?>) response.getBody().get("students")).size());
    assertEquals(2, response.getBody().get("totalPages"));

    var secondPage =
        testRestTemplate.exchange(
            STUDENTS_URL + "?page=1&size=2", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, secondPage.getStatusCode());
    assertNotNull(secondPage.getBody());
    assertEquals(1, secondPage.getBody().get("page"));
    assertEquals(1, ((List<?>) secondPage.getBody().get("students")).size());
  }

  @Test
  void getStudents_withNoData_returnsEmptyPage() {
    var response =
        testRestTemplate.exchange(STUDENTS_URL, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().get("page"));
    assertEquals(10, response.getBody().get("size"));
    assertEquals(0L, ((Number) response.getBody().get("totalElements")).longValue());
    assertEquals(0, response.getBody().get("totalPages"));
    assertTrue(((List<?>) response.getBody().get("students")).isEmpty());
  }

  @Test
  void getStudents_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(STUDENTS_URL, GET, new HttpEntity<>(jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  @Test
  void getStudents_doesNotExposePassword() {
    registerStudent("no-password@example.com");

    var response =
        testRestTemplate.exchange(STUDENTS_URL, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var students = (List<Map<String, Object>>) response.getBody().get("students");
    var student = students.get(0);
    assertFalse(student.containsKey("password"));
  }

  @Test
  void getStudent_asAdmin_returnsStudent() {
    var id = registerStudent("admin-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(id.toString(), response.getBody().get("id"));
    assertEquals("admin-view@example.com", response.getBody().get("email"));
    assertNotNull(response.getBody().get("reference"));
    assertEquals("ACTIVE", response.getBody().get("status"));
  }

  @Test
  void getStudent_asOwner_returnsStudent() {
    var id = registerStudent("owner-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id, GET, new HttpEntity<>(studentHeaders(id)), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(id.toString(), response.getBody().get("id"));
    assertEquals("owner-view@example.com", response.getBody().get("email"));
  }

  @Test
  void getStudent_asOtherStudent_returns403() {
    var id = registerStudent("other-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id,
            GET,
            new HttpEntity<>(studentHeaders(UUID.randomUUID())),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getStudent_asTeacher_returns403() {
    var id = registerStudent("teacher-view@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id,
            GET,
            new HttpEntity<>(bearerHeaders(tokenFor("teacher@example.com", false, Role.TEACHER))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getStudent_unknownId_returns404() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID(),
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().get("status"));
    assertEquals("Student not found", response.getBody().get("message"));
  }

  @Test
  void getStudent_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID(),
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  @Test
  void getStudent_doesNotExposePassword() {
    var id = registerStudent("single-no-password@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id, GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().containsKey("password"));
  }

  @Test
  void getStudentGrades_withGrades_returnsGradesGroupedByCourse() {
    var id = registerStudent("grades-student@example.com");
    ensureGradeReferenceData();
    insertGrade(id, GRADES_EXAM_A1_ID, BigDecimal.valueOf(18.00));
    insertGrade(id, GRADES_EXAM_A2_ID, BigDecimal.valueOf(18.40));
    insertGrade(id, GRADES_EXAM_B1_ID, BigDecimal.valueOf(12.35));
    insertGrade(id, GRADES_EXAM_DONNEES1_ID, BigDecimal.valueOf(16.69));
    insertGrade(id, GRADES_EXAM_WEB1_ID, BigDecimal.valueOf(20.00));
    insertGrade(id, GRADES_EXAM_SYS1_ID, BigDecimal.valueOf(14.00));
    insertGrade(id, GRADES_EXAM_LV1_ID, BigDecimal.valueOf(13.25));
    insertGrade(id, GRADES_EXAM_SYS2_ID, BigDecimal.valueOf(14.84));
    insertGrade(id, GRADES_EXAM_WEB2_ID, BigDecimal.valueOf(16.43));
    insertGrade(id, GRADES_EXAM_THEORIE1_ID, BigDecimal.valueOf(15.00));
    insertGrade(id, GRADES_EXAM_MGT1_ID, BigDecimal.valueOf(18.50));
    insertGrade(id, GRADES_EXAM_OTHER_ID, BigDecimal.valueOf(18));

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades?academicYear=" + currentAcademicYear(),
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var courses = (List<Map<String, Object>>) response.getBody().get("courses");
    assertNotNull(courses);
    assertEquals(10, courses.size());
    // (18.24*6 + 13.25*4 + 16.43*8 + 14.84*8 + 15.00*4 + 16.69*4 + 18.50*4 +
    // 14.00*6 +
    // 20.00*6 + 12.35*10) / 60 = 940.86 / 60 = 15.681
    assertEquals(15.68, ((Number) response.getBody().get("yearAverage")).doubleValue(), 0.001);
    assertEquals(60, response.getBody().get("creditEarned"));

    Map<String, Map<String, Object>> byCode = new HashMap<>();
    for (var course : courses) {
      byCode.put((String) course.get("courseCode"), course);
    }
    assertEquals(10, byCode.size());

    assertCourse(byCode, "PROG1", GRADES_COURSE_A_ID, 6, 18.24);
    assertCourse(byCode, "PROG2", GRADES_COURSE_B_ID, 10, 12.35);
    assertCourse(byCode, "DONNEES1", GRADES_COURSE_DONNEES1_ID, 4, 16.69);
    assertCourse(byCode, "WEB1", GRADES_COURSE_WEB1_ID, 6, 20.00);
    assertCourse(byCode, "SYS1", GRADES_COURSE_SYS1_ID, 6, 14.00);
    assertCourse(byCode, "LV1", GRADES_COURSE_LV1_ID, 4, 13.25);
    assertCourse(byCode, "SYS2", GRADES_COURSE_SYS2_ID, 8, 14.84);
    assertCourse(byCode, "WEB2", GRADES_COURSE_WEB2_ID, 8, 16.43);
    assertCourse(byCode, "THEORIE1", GRADES_COURSE_THEORIE1_ID, 4, 15.00);
    assertCourse(byCode, "MGT1", GRADES_COURSE_MGT1_ID, 4, 18.50);
    assertTrue(byCode.get("OLD1") == null);
  }

  private static void assertCourse(
      Map<String, Map<String, Object>> byCode,
      String code,
      UUID expectedId,
      int expectedCredits,
      double expectedAverage) {
    var course = byCode.get(code);
    assertNotNull(course);
    assertEquals(expectedId.toString(), course.get("courseId"));
    assertEquals(expectedCredits, course.get("credits"));
    assertEquals(expectedAverage, ((Number) course.get("average")).doubleValue(), 0.001);
    assertFalse(course.containsKey("exams"));
  }

  @Test
  void getStudentGrades_courseNoteIsSumOfScoreTimesWeight() {
    var id = registerStudent("grades-sum-formula@example.com");
    ensureGradeReferenceData();
    insertGrade(id, GRADES_EXAM_C1_ID, BigDecimal.valueOf(10));
    insertGrade(id, GRADES_EXAM_C2_ID, BigDecimal.valueOf(20));

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades?academicYear=" + currentAcademicYear(),
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var courses = (List<Map<String, Object>>) response.getBody().get("courses");
    assertNotNull(courses);
    assertEquals(1, courses.size());
    assertEquals(GRADES_COURSE_C_ID.toString(), courses.get(0).get("courseId"));
    assertEquals(11.0, ((Number) courses.get(0).get("average")).doubleValue(), 0.001);
    assertEquals(0.37, ((Number) response.getBody().get("yearAverage")).doubleValue(), 0.001);
    assertEquals(2, response.getBody().get("creditEarned"));
  }

  @Test
  void getStudentGrades_wrongAcademicYear_returnsEmptyList() {
    var id = registerStudent("grades-wrong-year@example.com");
    ensureGradeReferenceData();
    insertGrade(id, GRADES_EXAM_A1_ID, BigDecimal.valueOf(10));

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades?academicYear=1999-2000",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(((List<?>) response.getBody().get("courses")).isEmpty());
    assertTrue(response.getBody().get("yearAverage") == null);
    assertEquals(0, response.getBody().get("creditEarned"));
  }

  @Test
  void getStudentGrades_withoutAcademicYear_defaultsToCurrentYear() {
    var id = registerStudent("grades-default-year@example.com");
    ensureGradeReferenceData();
    insertGrade(id, GRADES_EXAM_A1_ID, BigDecimal.valueOf(10));

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    var courses = (List<Map<String, Object>>) response.getBody().get("courses");
    assertNotNull(courses);
    assertEquals(GRADES_COURSE_A_ID.toString(), courses.get(0).get("courseId"));
  }

  @Test
  void getStudentGrades_withoutGrades_returnsEmptyList() {
    var id = registerStudent("grades-no-grades@example.com");
    ensureGradeReferenceData();

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades", GET, new HttpEntity<>(adminHeaders()), Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(((List<?>) response.getBody().get("courses")).isEmpty());
    assertTrue(response.getBody().get("yearAverage") == null);
    assertEquals(0, response.getBody().get("creditEarned"));
  }

  @Test
  void getStudentGrades_asOwner_returnsGrades() {
    var id = registerStudent("grades-owner@example.com");
    ensureGradeReferenceData();
    insertGrade(id, GRADES_EXAM_A1_ID, BigDecimal.valueOf(10));

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades",
            GET,
            new HttpEntity<>(studentHeaders(id)),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().get("courses"));
  }

  @Test
  void getStudentGrades_asOtherStudent_returns403() {
    var id = registerStudent("grades-other@example.com");

    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + id + "/grades",
            GET,
            new HttpEntity<>(studentHeaders(UUID.randomUUID())),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  void getStudentGrades_unknownStudent_returns404() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID() + "/grades",
            GET,
            new HttpEntity<>(adminHeaders()),
            Map.class);

    assertEquals(NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Student not found", response.getBody().get("message"));
  }

  @Test
  void getStudentGrades_withoutAuth_returns401() {
    var response =
        testRestTemplate.exchange(
            STUDENTS_URL + "/" + UUID.randomUUID() + "/grades",
            GET,
            new HttpEntity<>(jsonHeaders()),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }

  private UUID registerStudent(String email) {
    var request =
        RegisterRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email(email)
            .password("password123")
            .role(Role.STUDENT)
            .build();

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    var user = (Map<String, Object>) response.getBody().get("user");
    return UUID.fromString((String) user.get("id"));
  }

  private void ensureGradeReferenceData() {
    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM cohort WHERE id = ?", Integer.class, GRADES_COHORT_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO cohort (id, name, start_year, end_year) VALUES (?, ?, ?, ?)",
          GRADES_COHORT_ID,
          "Grades Cohort",
          2024,
          2026);
    }

    if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branch WHERE id = ?", Integer.class, GRADES_BRANCH_ID)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO branch (id, code, name) VALUES (?, ?, ?)",
          GRADES_BRANCH_ID,
          "CS",
          "Computer Science");
    }

    insertSemester(GRADES_SEMESTER_ID, GRADES_COHORT_ID, currentAcademicYear());
    insertSemester(OTHER_SEMESTER_ID, GRADES_COHORT_ID, "2020-2021");

    insertCourse(GRADES_COURSE_A_ID, GRADES_SEMESTER_ID, "PROG1", "Algorithmics & Programming", 6);
    insertCourse(GRADES_COURSE_B_ID, GRADES_SEMESTER_ID, "PROG2", "Advanced Programming", 10);
    insertCourse(GRADES_COURSE_C_ID, GRADES_SEMESTER_ID, "STAT1", "Statistics", 2);
    insertCourse(GRADES_COURSE_DONNEES1_ID, GRADES_SEMESTER_ID, "DONNEES1", "Databases", 4);
    insertCourse(GRADES_COURSE_WEB1_ID, GRADES_SEMESTER_ID, "WEB1", "Web Development", 6);
    insertCourse(GRADES_COURSE_SYS1_ID, GRADES_SEMESTER_ID, "SYS1", "Operating Systems", 6);
    insertCourse(GRADES_COURSE_LV1_ID, GRADES_SEMESTER_ID, "LV1", "English", 4);
    insertCourse(GRADES_COURSE_SYS2_ID, GRADES_SEMESTER_ID, "SYS2", "Networks", 8);
    insertCourse(GRADES_COURSE_WEB2_ID, GRADES_SEMESTER_ID, "WEB2", "Web Frameworks", 8);
    insertCourse(GRADES_COURSE_THEORIE1_ID, GRADES_SEMESTER_ID, "THEORIE1", "Theory", 4);
    insertCourse(GRADES_COURSE_MGT1_ID, GRADES_SEMESTER_ID, "MGT1", "Management", 4);
    insertCourse(GRADES_COURSE_OTHER_ID, OTHER_SEMESTER_ID, "OLD1", "Old Course", 5);

    insertExam(GRADES_EXAM_A1_ID, GRADES_COURSE_A_ID, "Exam A1", "0.4", LocalDate.of(2024, 1, 15));
    insertExam(GRADES_EXAM_A2_ID, GRADES_COURSE_A_ID, "Exam A2", "0.6", LocalDate.of(2024, 6, 15));
    insertExam(GRADES_EXAM_A3_ID, GRADES_COURSE_A_ID, "Exam A3", "0.5", LocalDate.of(2024, 11, 15));
    insertExam(GRADES_EXAM_B1_ID, GRADES_COURSE_B_ID, "Exam B1", "1.0", LocalDate.of(2024, 3, 1));
    insertExam(GRADES_EXAM_C1_ID, GRADES_COURSE_C_ID, "Exam C1", "0.3", LocalDate.of(2024, 2, 1));
    insertExam(GRADES_EXAM_C2_ID, GRADES_COURSE_C_ID, "Exam C2", "0.4", LocalDate.of(2024, 7, 1));
    insertExam(
        GRADES_EXAM_DONNEES1_ID,
        GRADES_COURSE_DONNEES1_ID,
        "Exam DONNEES1",
        "1.0",
        LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_WEB1_ID, GRADES_COURSE_WEB1_ID, "Exam WEB1", "1.0", LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_SYS1_ID, GRADES_COURSE_SYS1_ID, "Exam SYS1", "1.0", LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_LV1_ID, GRADES_COURSE_LV1_ID, "Exam LV1", "1.0", LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_SYS2_ID, GRADES_COURSE_SYS2_ID, "Exam SYS2", "1.0", LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_WEB2_ID, GRADES_COURSE_WEB2_ID, "Exam WEB2", "1.0", LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_THEORIE1_ID,
        GRADES_COURSE_THEORIE1_ID,
        "Exam THEORIE1",
        "1.0",
        LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_MGT1_ID, GRADES_COURSE_MGT1_ID, "Exam MGT1", "1.0", LocalDate.of(2024, 2, 1));
    insertExam(
        GRADES_EXAM_OTHER_ID, GRADES_COURSE_OTHER_ID, "Exam Old", "1.0", LocalDate.of(2020, 3, 1));
  }

  private void insertSemester(UUID id, UUID cohortId, String academicYear) {
    if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM semester WHERE id = ?", Integer.class, id)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO semester (id, cohort_id, semester_number, academic_year) VALUES (?, ?, ?,"
              + " ?)",
          id,
          cohortId,
          1,
          academicYear);
    }
  }

  private void insertCourse(UUID id, UUID semesterId, String code, String title, int credits) {
    if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM course WHERE id = ?", Integer.class, id)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO course (id, semester_id, branch_id, code, title, credits) VALUES (?, ?, ?,"
              + " ?, ?, ?)",
          id,
          semesterId,
          GRADES_BRANCH_ID,
          code,
          title,
          credits);
    }
  }

  private void insertExam(UUID id, UUID courseId, String title, String weight, LocalDate date) {
    if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exam WHERE id = ?", Integer.class, id)
        == 0) {
      jdbcTemplate.update(
          "INSERT INTO exam (id, course_id, title, weight, exam_date) VALUES (?, ?, ?, ?, ?)",
          id,
          courseId,
          title,
          new BigDecimal(weight),
          date);
    }
  }

  private void insertGrade(UUID studentId, UUID examId, BigDecimal score) {
    jdbcTemplate.update(
        "DELETE FROM grade WHERE student_id = ? AND exam_id = ?", studentId, examId);
    jdbcTemplate.update(
        "INSERT INTO grade (id, student_id, exam_id, score) VALUES (?, ?, ?, ?)",
        UUID.randomUUID(),
        studentId,
        examId,
        score);
  }

  private static String currentAcademicYear() {
    LocalDate today = LocalDate.now();
    int start = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
    return start + "-" + (start + 1);
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor("students@hei.school", false, Role.ADMIN));
  }

  private HttpHeaders studentHeaders(UUID id) {
    return bearerHeaders(tokenFor("students@hei.school", false, Role.STUDENT, id));
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(String email, boolean mustChangePassword, Role role) {
    return tokenFor(email, mustChangePassword, role, UUID.randomUUID());
  }

  private String tokenFor(String email, boolean mustChangePassword, Role role, UUID id) {
    var user =
        new CustomUserDetails(
            new User(id, email, "Student", "Test", role, null, null, mustChangePassword, null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
