package hei.school.graduate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.school.graduate.exception.ErrorResponse;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

  private final CustomUserDetailsService userDetailsService;
  private final JwtAuthFilter jwtAuthFilter;
  private final ObjectMapper objectMapper;

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
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/students/*")
                    .access(studentByIdAccessManager())
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
        var studentId = extractStudentId(context.getRequest());
        if (studentId != null && details.getUser().id().toString().equals(studentId)) {
          return new AuthorizationDecision(true);
        }
      }

      return new AuthorizationDecision(false);
    };
  }

  private static String extractStudentId(HttpServletRequest request) {
    var uri = request.getRequestURI();
    if (uri.startsWith(STUDENTS_PATH_PREFIX)) {
      return uri.substring(STUDENTS_PATH_PREFIX.length());
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
