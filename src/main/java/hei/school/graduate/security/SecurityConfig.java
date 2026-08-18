package hei.school.graduate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.school.graduate.exception.ErrorResponse;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.repository.CourseTeacherRepository;
import hei.school.graduate.repository.ExamRepository;
import hei.school.graduate.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String STUDENTS_PATH_PREFIX = "/students/";
  private static final String TEACHERS_PATH_PREFIX = "/teachers/";
  private static final String EXAMS_PATH_PREFIX = "/exams/";
  private static final String GRADES_STUDENTS_SUFFIX = "/grades-students";

  private final CustomUserDetailsService userDetailsService;
  private final JwtAuthFilter jwtAuthFilter;
  private final ObjectMapper objectMapper;
  private final ExamRepository examRepository;
  private final CourseTeacherRepository courseTeacherRepository;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/register")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/courses", "/courses/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/courses")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/courses/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/courses/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/courses/*/exams")
                    .access(courseTeacherAccessManager())
                    .requestMatchers(HttpMethod.POST, "/courses/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/exams/*/grades-students",
                        "/exams/*/grades-students/*",
                        "/exams/*/grades-students/*/history")
                    .access(examGradesAccessManager())
                    .requestMatchers(HttpMethod.PUT, "/exams/*/grades-students")
                    .access(examGradesAccessManager())
                    .requestMatchers(HttpMethod.POST, "/groups")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/groups/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/groups/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*", "/students/*/group")
                    .access(studentByIdAccessManager())
                    .requestMatchers(HttpMethod.POST, "/students/*/transfer")
                    .access(studentByIdAccessManager())
                    .requestMatchers(HttpMethod.GET, "/students/*/group/history")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*/grades")
                    .access(studentByIdAccessManager())
                    .requestMatchers(HttpMethod.POST, "/students/*/grade-report")
                    .access(studentByIdAccessManager())
                    .requestMatchers(HttpMethod.GET, "/admins")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/teachers")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/teachers/*")
                    .access(teacherByIdAccessManager())
                    .requestMatchers(HttpMethod.GET, "/exams/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/exams/**")
                    .access(examTeacherAccessManager())
                    .requestMatchers(HttpMethod.PUT, "/exams/**")
                    .access(examTeacherAccessManager())
                    .requestMatchers(HttpMethod.DELETE, "/exams/**")
                    .access(examTeacherAccessManager())
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            writeError(response, 403, "Access denied"))
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            writeError(response, 401, "Invalid credentials")))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthorizationManager<RequestAuthorizationContext> studentByIdAccessManager() {
    return (authentication, context) -> {
      var auth = authentication.get();
      if (auth == null || !auth.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      var isAdmin =
          auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .anyMatch("ROLE_ADMIN"::equals);
      if (isAdmin) {
        return new AuthorizationDecision(true);
      }

      var principal = auth.getPrincipal();
      if (principal instanceof CustomUserDetails details
          && details.getUser().role() == Role.STUDENT) {
        var studentId = extractId(context.getRequest(), STUDENTS_PATH_PREFIX);
        if (studentId != null && details.getUser().id().toString().equals(studentId)) {
          return new AuthorizationDecision(true);
        }
      }

      return new AuthorizationDecision(false);
    };
  }

  @Bean
  public AuthorizationManager<RequestAuthorizationContext> teacherByIdAccessManager() {
    return (authentication, context) -> {
      var auth = authentication.get();
      if (auth == null || !auth.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      var isAdmin =
          auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .anyMatch("ROLE_ADMIN"::equals);
      if (isAdmin) {
        return new AuthorizationDecision(true);
      }

      var principal = auth.getPrincipal();
      if (principal instanceof CustomUserDetails details
          && details.getUser().role() == Role.TEACHER) {
        var teacherId = extractId(context.getRequest(), TEACHERS_PATH_PREFIX);
        if (teacherId != null && details.getUser().id().toString().equals(teacherId)) {
          return new AuthorizationDecision(true);
        }
      }

      return new AuthorizationDecision(false);
    };
  }

  @Bean
  public AuthorizationManager<RequestAuthorizationContext> examGradesAccessManager() {
    return (authentication, context) -> {
      var auth = authentication.get();
      if (auth == null || !auth.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      var isAdmin =
          auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .anyMatch("ROLE_ADMIN"::equals);
      if (isAdmin) {
        return new AuthorizationDecision(true);
      }

      var principal = auth.getPrincipal();
      if (principal instanceof CustomUserDetails details) {
        if (details.getUser().role() == Role.STUDENT) {
          var idStudent = extractIdStudent(context.getRequest());
          return new AuthorizationDecision(
              idStudent != null
                  && !isHistoryPath(context.getRequest())
                  && details.getUser().id().equals(idStudent));
        }

        if (details.getUser().role() == Role.TEACHER) {
          var examId = extractExamId(context.getRequest());
          if (examId == null) {
            return new AuthorizationDecision(false);
          }
          var isAssigned =
              examRepository
                  .findById(examId)
                  .map(
                      exam ->
                          courseTeacherRepository
                              .findAllByCourse_Id(exam.getCourse().getId())
                              .stream()
                              .anyMatch(
                                  ct -> ct.getTeacher().getId().equals(details.getUser().id())))
                  .orElse(false);
          return new AuthorizationDecision(isAssigned);
        }
      }

      return new AuthorizationDecision(false);
    };
  }

  @Bean
  public AuthorizationManager<RequestAuthorizationContext> examTeacherAccessManager() {
    return (authentication, context) -> {
      var auth = authentication.get();
      if (auth == null || !auth.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      var isAdmin =
          auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .anyMatch("ROLE_ADMIN"::equals);
      if (isAdmin) {
        return new AuthorizationDecision(true);
      }

      var principal = auth.getPrincipal();
      if (principal instanceof CustomUserDetails details
          && details.getUser().role() == Role.TEACHER) {
        UUID examId = extractExamId(context.getRequest());
        if (examId == null) {
          return new AuthorizationDecision(false);
        }
        return examRepository
            .findById(examId)
            .map(
                exam -> {
                  UUID courseId = exam.getCourse().getId();
                  UUID teacherId = details.getUser().id();
                  boolean assigned =
                      courseTeacherRepository.findAllByCourse_Id(courseId).stream()
                          .anyMatch(ct -> ct.getTeacher().getId().equals(teacherId));
                  return new AuthorizationDecision(assigned);
                })
            .orElse(new AuthorizationDecision(false));
      }

      return new AuthorizationDecision(false);
    };
  }

  @Bean
  public AuthorizationManager<RequestAuthorizationContext> courseTeacherAccessManager() {
    return (authentication, context) -> {
      var auth = authentication.get();
      if (auth == null || !auth.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      var isAdmin =
          auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .anyMatch("ROLE_ADMIN"::equals);
      if (isAdmin) {
        return new AuthorizationDecision(true);
      }

      var principal = auth.getPrincipal();
      if (principal instanceof CustomUserDetails details
          && details.getUser().role() == Role.TEACHER) {
        UUID courseId = extractCourseId(context.getRequest());
        if (courseId == null) {
          return new AuthorizationDecision(false);
        }
        UUID teacherId = details.getUser().id();
        boolean assigned =
            courseTeacherRepository.findAllByCourse_Id(courseId).stream()
                .anyMatch(ct -> ct.getTeacher().getId().equals(teacherId));
        return new AuthorizationDecision(assigned);
      }

      return new AuthorizationDecision(false);
    };
  }

  private static UUID extractIdStudent(HttpServletRequest request) {
    var uri = request.getRequestURI();
    if (!uri.startsWith(EXAMS_PATH_PREFIX)) {
      return null;
    }
    var segments = uri.substring(EXAMS_PATH_PREFIX.length()).split("/");
    if (segments.length < 3 || !GRADES_STUDENTS_SUFFIX.equals("/" + segments[1])) {
      return null;
    }
    try {
      return UUID.fromString(segments[2]);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static boolean isHistoryPath(HttpServletRequest request) {
    var uri = request.getRequestURI();
    if (!uri.startsWith(EXAMS_PATH_PREFIX)) {
      return false;
    }
    var segments = uri.substring(EXAMS_PATH_PREFIX.length()).split("/");
    return segments.length >= 4 && "history".equals(segments[3]);
  }

  private static String extractId(HttpServletRequest request, String pathPrefix) {
    var uri = request.getRequestURI();
    if (uri.startsWith(pathPrefix)) {
      var rest = uri.substring(pathPrefix.length());
      var slash = rest.indexOf('/');
      return slash == -1 ? rest : rest.substring(0, slash);
    }
    return null;
  }

  private static UUID extractExamId(HttpServletRequest request) {
    var uri = request.getRequestURI();
    var parts = uri.split("/");
    if (parts.length >= 3 && "exams".equals(parts[1])) {
      try {
        return UUID.fromString(parts[2]);
      } catch (IllegalArgumentException ignored) {
        return null;
      }
    }
    return null;
  }

  private static UUID extractCourseId(HttpServletRequest request) {
    var uri = request.getRequestURI();
    var parts = uri.split("/");
    if (parts.length >= 3 && "courses".equals(parts[1])) {
      try {
        return UUID.fromString(parts[2]);
      } catch (IllegalArgumentException ignored) {
        return null;
      }
    }
    return null;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    var provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  private void writeError(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(status, message));
  }
}
