package hei.school.graduate.security;

import hei.school.graduate.model.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final long expiration;

  public JwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
    byte[] keyBytes = HexFormat.of().parseHex(secret);
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    this.expiration = expiration;
  }

  public String generateToken(CustomUserDetails userDetails) {
    var now = new Date();
    return Jwts.builder()
        .subject(userDetails.getUser().id().toString())
        .claim("role", userDetails.getUser().role().name())
        .claim("email", userDetails.getUser().email())
        .claim("mustChangePassword", userDetails.getUser().mustChangePassword())
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expiration))
        .signWith(signingKey)
        .compact();
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(parseClaims(token).getSubject());
  }

  public String extractEmail(String token) {
    return parseClaims(token).get("email", String.class);
  }

  public String extractRole(String token) {
    return parseClaims(token).get("role", String.class);
  }

  public boolean extractMustChangePassword(String token) {
    return parseClaims(token).get("mustChangePassword", Boolean.class);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }
}
