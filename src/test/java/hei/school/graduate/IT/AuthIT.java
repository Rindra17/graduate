package hei.school.graduate.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.school.graduate.conf.FacadeIT;
import hei.school.graduate.endpoint.rest.controller.dto.ChangePasswordRequest;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.mapper.UserMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.repository.AdminRepository;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.TeacherRepository;
import hei.school.graduate.repository.UserRepository;
import hei.school.graduate.repository.model.JStudent;
import hei.school.graduate.security.JwtService;
import hei.school.graduate.service.CustomUserDetailsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.support.TransactionTemplate;

class AuthIT extends FacadeIT {

  private static final String REGISTER_URL = "/auth/register";
  private static final String LOGIN_URL = "/auth/login";
  private static final String CHANGE_PASSWORD_URL = "/auth/change-password";

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired JwtService jwtService;

  @Autowired CustomUserDetailsService userDetailsService;

  @Autowired UserRepository userRepository;

  @Autowired StudentRepository studentRepository;

  @Autowired TeacherRepository teacherRepository;
  @Autowired AdminRepository adminRepository;

  @Autowired UserMapper userMapper;

  @PersistenceContext EntityManager entityManager;

  @Autowired TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    testRestTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
  }

  @Test
  void register_validRequest_returns201AndUser() {
    var request = registerRequest("reg@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("reg@example.com", userOf(response.getBody()).get("email"));
    assertEquals("John", userOf(response.getBody()).get("firstName"));
    assertNotNull(response.getBody().get("temporaryPassword"));
    assertTrue(response.getHeaders().containsKey("Set-Cookie"));
  }

  @Test
  void register_validRequest_withAdminCookie_returns201() {
    var request = registerRequest("cookie@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminCookieHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("cookie@example.com", userOf(response.getBody()).get("email"));
  }

  //
  // @Test
  // void register_withoutAdmin_returns401() {
  // var request = registerRequest("unauth@example.com");
  //
  // var response = testRestTemplate.exchange(
  // REGISTER_URL, POST, new HttpEntity<>(request, jsonHeaders()), Map.class);
  //
  // assertEquals(UNAUTHORIZED, response.getStatusCode());
  // assertNotNull(response.getBody());
  // assertEquals(401, response.getBody().get("status"));
  // assertEquals("Invalid credentials", response.getBody().get("message"));
  // }
  //
  // @Test
  // void register_withNonAdminRole_returns403() {
  // var request = registerRequest("student@example.com");
  //
  // var response = testRestTemplate.exchange(
  // REGISTER_URL, POST, new HttpEntity<>(request, studentHeaders()), Map.class);
  //
  // assertEquals(FORBIDDEN, response.getStatusCode());
  // assertNotNull(response.getBody());
  // assertEquals(403, response.getBody().get("status"));
  // assertEquals("Access denied", response.getBody().get("message"));
  // }
  //
  @Test
  void register_duplicateEmail_returns409() {
    var request = registerRequest("dup@example.com");

    testRestTemplate.exchange(
        REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(409, response.getBody().get("status"));
    assertEquals("Email already in use", response.getBody().get("message"));
  }

  @Test
  void login_validCredentials_returns202AndUser() {
    var email = "login-test@example.com";
    var temporaryPassword = registerUser(email).temporaryPassword();

    var loginRequest = new LoginRequest(email, temporaryPassword);

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(ACCEPTED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(email, userOf(response.getBody()).get("email"));
    assertNotNull(response.getBody().get("token"));
    assertTrue(response.getHeaders().containsKey("Set-Cookie"));
  }

  @Test
  void login_newlyRegisteredUser_mustChangePasswordIsTrue() {
    var email = "must-change@example.com";
    var temporaryPassword = registerUser(email).temporaryPassword();

    var loginRequest = new LoginRequest(email, temporaryPassword);

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(ACCEPTED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(true, userOf(response.getBody()).get("mustChangePassword"));
  }

  @Test
  void login_wrongPassword_returns401() {
    var email = "wrongpw@example.com";
    registerUser(email);

    var loginRequest = new LoginRequest(email, "wrongPass");

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid email or password", response.getBody().get("message"));
  }

  @Test
  void login_nonExistentEmail_returns401() {
    var loginRequest = new LoginRequest("noone@example.com", "password");

    var response =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().get("status"));
    assertEquals("Invalid email or password", response.getBody().get("message"));
  }

  @Test
  void changePassword_validRequest_returns200AndClearsFlag() {
    var user = registerUser("change-me@example.com");

    var token = tokenFor(user.id(), true);

    var changeRequest = new ChangePasswordRequest(user.temporaryPassword(), "brandNew123");

    var response =
        testRestTemplate.exchange(
            CHANGE_PASSWORD_URL,
            POST,
            new HttpEntity<>(changeRequest, bearerHeaders(token)),
            Map.class);

    assertEquals(OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(false, response.getBody().get("mustChangePassword"));
    assertTrue(response.getHeaders().containsKey("Set-Cookie"));
  }

  @Test
  void changePassword_wrongCurrentPassword_returns401() {
    var user = registerUser("wrong-current@example.com");

    var token = tokenFor(user.id(), true);

    var changeRequest = new ChangePasswordRequest("wrongCurrent", "brandNew123");

    var response =
        testRestTemplate.exchange(
            CHANGE_PASSWORD_URL,
            POST,
            new HttpEntity<>(changeRequest, bearerHeaders(token)),
            Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Current password is incorrect", response.getBody().get("message"));
  }

  @Test
  void changePassword_thenLoginWithNewPassword_clearsFlag() {
    var user = registerUser("full-flow@example.com");

    var changeResponse =
        testRestTemplate.exchange(
            CHANGE_PASSWORD_URL,
            POST,
            new HttpEntity<>(
                new ChangePasswordRequest(user.temporaryPassword(), "brandNew123"),
                bearerHeaders(tokenFor(user.id(), true))),
            Map.class);

    assertEquals(OK, changeResponse.getStatusCode());

    var loginRequest = new LoginRequest(user.email(), "brandNew123");

    var loginResponse =
        testRestTemplate.exchange(
            LOGIN_URL, POST, new HttpEntity<>(loginRequest, jsonHeaders()), Map.class);

    assertEquals(ACCEPTED, loginResponse.getStatusCode());
    assertNotNull(loginResponse.getBody());
    assertEquals(false, userOf(loginResponse.getBody()).get("mustChangePassword"));
  }

  @Test
  void mustChangePasswordUser_blockedFromOtherEndpoints_returns403() {
    var request = registerRequest("blocked@example.com");

    var response =
        testRestTemplate.exchange(
            REGISTER_URL,
            POST,
            new HttpEntity<>(request, bearerHeaders(tokenFor("blocked@example.com", true))),
            Map.class);

    assertEquals(FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(403, response.getBody().get("status"));
    assertEquals("Password change required", response.getBody().get("message"));
  }

  @Test
  void userDetailsService_loadsExistingUser() {
    var email = "loaduser@example.com";
    registerUser(email);

    var details = userDetailsService.loadUserByUsername(email);

    assertNotNull(details);
    assertEquals(email, details.getUsername());
  }

  @Test
  void userDetailsService_unknownEmail_throws() {
    assertThrows(
        UsernameNotFoundException.class,
        () -> userDetailsService.loadUserByUsername("ghost@example.com"));
  }

  @Test
  void register_studentRole_returns201WithStudent() {
    var request = registerRequest("student-role@example.com", Role.STUDENT);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("STUDENT", userOf(response.getBody()).get("role"));
  }

  @Test
  void register_teacherRole_returns201WithTeacher() {
    var request = registerRequest("teacher-role@example.com", Role.TEACHER);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("TEACHER", userOf(response.getBody()).get("role"));
  }

  @Test
  void register_adminRole_returns201WithAdmin() {
    var request = registerRequest("admin-role@example.com", Role.ADMIN);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("ADMIN", userOf(response.getBody()).get("role"));
  }

  @Test
  void register_studentRole_createsStudentRecord() {
    var count = countInYear(Role.STUDENT);
    var request = registerRequest("createstd@example.com", Role.STUDENT);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    var id = UUID.fromString((String) userOf(response.getBody()).get("id"));
    var student = studentRepository.findById(id).orElseThrow();
    assertEquals(
        String.format("STD%02d%03d", LocalDateTime.now().getYear() % 100, count + 1),
        student.getReference());
    assertEquals("ACTIVE", student.getStatus());
    assertEquals(id, student.getUser().getId());
    assertNotNull(userOf(response.getBody()).get("entranceDateTime"));
  }

  @Test
  void register_teacherRole_createsTeacherRecord() {
    var count = countInYear(Role.TEACHER);
    var request = registerRequest("createtcr@example.com", Role.TEACHER);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    var id = UUID.fromString((String) userOf(response.getBody()).get("id"));
    var teacher = teacherRepository.findById(id).orElseThrow();
    assertEquals(
        String.format("TCR%02d%03d", LocalDateTime.now().getYear() % 100, count + 1),
        teacher.getReference());
    assertEquals(id, teacher.getUser().getId());
  }

  @Test
  void register_adminRole_createsAdminRecord() {
    var count = countInYear(Role.ADMIN);
    var request = registerRequest("createadm@example.com", Role.ADMIN);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    var id = UUID.fromString((String) userOf(response.getBody()).get("id"));
    var admin = adminRepository.findById(id).orElseThrow();
    assertEquals(
        String.format("ADM%02d%03d", LocalDateTime.now().getYear() % 100, count + 1),
        admin.getReference());
    assertEquals(id, admin.getUser().getId());
  }

  @Test
  void register_twoStudents_referencesIncrement() {
    var count = countInYear(Role.STUDENT);

    var first =
        testRestTemplate.exchange(
            REGISTER_URL,
            POST,
            new HttpEntity<>(registerRequest("increment-a@example.com"), adminHeaders()),
            Map.class);
    var second =
        testRestTemplate.exchange(
            REGISTER_URL,
            POST,
            new HttpEntity<>(registerRequest("increment-b@example.com"), adminHeaders()),
            Map.class);

    assertEquals(CREATED, first.getStatusCode());
    assertEquals(CREATED, second.getStatusCode());
    var secondId = UUID.fromString((String) userOf(second.getBody()).get("id"));
    var secondStudent = studentRepository.findById(secondId).orElseThrow();
    assertEquals(
        String.format("STD%02d%03d", LocalDateTime.now().getYear() % 100, count + 2),
        secondStudent.getReference());
  }

  private long countInYear(Role role) {
    var startOfYear = LocalDateTime.now().toLocalDate().withDayOfYear(1).atStartOfDay();
    return userRepository.countByRoleAndEntranceDateTimeBetween(
        role, startOfYear, startOfYear.plusYears(1));
  }

  @Test
  void register_student_continuesExistingReferenceAndIsolatedPerYear() {
    var now = LocalDateTime.now();
    var year = now.getYear();
    var countBefore = countInYear(Role.STUDENT);

    persistUserAndStudent("existing-std@example.com", "STD25001", now.minusYears(1));
    persistUserAndStudent(
        "existing-this-year@example.com",
        String.format("STD%02d%03d", year % 100, countBefore + 1),
        now);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL,
            POST,
            new HttpEntity<>(registerRequest("next-std@example.com"), adminHeaders()),
            Map.class);

    assertEquals(CREATED, response.getStatusCode());
    var id = UUID.fromString((String) userOf(response.getBody()).get("id"));
    var student = studentRepository.findById(id).orElseThrow();
    assertEquals(String.format("STD%02d%03d", year % 100, countBefore + 2), student.getReference());
  }

  private void persistUserAndStudent(String email, String reference, LocalDateTime entrance) {
    transactionTemplate.executeWithoutResult(
        status -> {
          var id = UUID.randomUUID();
          var jUser =
              userMapper.toEntity(
                  new User(
                      id,
                      email,
                      "Existing",
                      "Student",
                      Role.STUDENT,
                      null,
                      "hash",
                      true,
                      entrance));
          entityManager.persist(jUser);
          entityManager.persist(
              JStudent.builder().user(jUser).reference(reference).status("ACTIVE").build());
        });
  }

  private RegisteredUser registerUser(String email) {
    var request = registerRequest(email);

    var response =
        testRestTemplate.exchange(
            REGISTER_URL, POST, new HttpEntity<>(request, adminHeaders()), Map.class);

    assertEquals(CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    var userMap = userOf(response.getBody());
    return new RegisteredUser(
        email,
        (String) response.getBody().get("temporaryPassword"),
        UUID.fromString((String) userMap.get("id")));
  }

  private Map<String, Object> userOf(Map<String, Object> body) {
    return (Map<String, Object>) body.get("user");
  }

  private record RegisteredUser(String email, String temporaryPassword, UUID id) {}

  private RegisterRequest registerRequest(String email) {
    return registerRequest(email, Role.STUDENT);
  }

  private RegisterRequest registerRequest(String email, Role role) {
    return RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email(email)
        .password("password123")
        .role(role)
        .build();
  }

  private HttpHeaders adminHeaders() {
    return bearerHeaders(tokenFor("auth@hei.school", false));
  }

  private HttpHeaders studentHeaders() {
    return bearerHeaders(tokenFor("auth@hei.school", false, Role.STUDENT));
  }

  private HttpHeaders adminCookieHeaders() {
    var headers = jsonHeaders();
    headers.add(HttpHeaders.COOKIE, "token=" + tokenFor("auth@hei.school", false));
    return headers;
  }

  private HttpHeaders bearerHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String tokenFor(String email, boolean mustChangePassword) {
    return tokenFor(email, mustChangePassword, Role.ADMIN);
  }

  private String tokenFor(UUID id, boolean mustChangePassword) {
    return jwtService.generateToken(
        new CustomUserDetails(
            new User(
                id,
                "auth@hei.school",
                "Auth",
                "User",
                Role.STUDENT,
                null,
                null,
                mustChangePassword,
                null)));
  }

  private String tokenFor(String email, boolean mustChangePassword, Role role) {
    var user =
        new CustomUserDetails(
            new User(
                UUID.randomUUID(),
                email,
                "Auth",
                "User",
                role,
                null,
                null,
                mustChangePassword,
                null));
    return jwtService.generateToken(user);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
