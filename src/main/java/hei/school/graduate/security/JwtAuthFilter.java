package hei.school.graduate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.school.graduate.exception.ErrorResponse;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String TOKEN_COOKIE = "token";
  private static final String CHANGE_PASSWORD_PATH = "/auth/change-password";
  private static final String LOGIN_PATH = "/auth/login";

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    var token = extractToken(request);

    if (token != null && jwtService.isTokenValid(token)) {
      var userId = jwtService.extractUserId(token);

      var email = jwtService.extractEmail(token);
      var role = Role.valueOf(jwtService.extractRole(token));
      var mustChangePassword = jwtService.extractMustChangePassword(token);

      var userDetails = CustomUserDetails.fromJwtClaims(userId, email, role, mustChangePassword);

      if (userDetails.mustChangePassword()
          && !isPasswordChangeAllowedPath(request.getRequestURI())) {
        writeError(response, 403, "Password change required");
        return;
      }

      var authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isPasswordChangeAllowedPath(String requestUri) {
    return CHANGE_PASSWORD_PATH.equals(requestUri) || LOGIN_PATH.equals(requestUri);
  }

  private void writeError(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(status, message));
  }

  private String extractToken(HttpServletRequest request) {
    var header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }

    if (request.getCookies() != null) {
      return Arrays.stream(request.getCookies())
          .filter(c -> TOKEN_COOKIE.equals(c.getName()))
          .findFirst()
          .map(Cookie::getValue)
          .orElse(null);
    }

    return null;
  }
}
