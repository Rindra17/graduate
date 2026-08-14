package hei.school.graduate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String TOKEN_COOKIE = "token";

  private final JwtService jwtService;

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

      var userDetails = CustomUserDetails.fromJwtClaims(userId, email, role);

      var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
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
