package hei.school.graduate.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hei.school.graduate.endpoint.rest.controller.dto.AuthResult;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.exception.EmailAlreadyInUseException;
import hei.school.graduate.exception.InvalidCredentialsException;
import hei.school.graduate.mapper.UserMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.repository.UserRepository;
import hei.school.graduate.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository repository;
  private final UserMapper mapper;
  private final PasswordEncoder encoder;
  private final JwtService service;

  @Value("${jwt.expiration}")
  private long expiration;

  public AuthResult register(RegisterRequest request) {
    if (repository.findByEmail(request.getEmail()).isPresent()) {
      throw new EmailAlreadyInUseException("Email already in use");
    }

    var role = request.getRole() != null ? request.getRole() : Role.STUDENT;
    var password = encoder.encode(request.getPassword());

    var user = new User(UUID.randomUUID(),
        request.getEmail(),
        request.getFirstName(),
        request.getLastName(),
        role,
        request.getAddress(),
        password);

    var saved = mapper.toDomain(repository.save(mapper.toEntity(user)));
    var token = service.generateToken(new CustomUserDetails(saved));

    return new AuthResult(saved, buildCookie(token));
  }

  public AuthResult login(LoginRequest request) {
    var jUser = repository.findByEmail(request.email())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    if (!encoder.matches(request.password(), jUser.getPassword())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    var user = mapper.toDomain(jUser);
    var token = service.generateToken(new CustomUserDetails(user));

    return new AuthResult(user, buildCookie(token));
  }

  private String buildCookie(String token) {
    return String.format(
        "token=%s; HttpOnly; SameSite=Strict; Path=/; Max-Age=%d",
        token, Duration.ofMillis(expiration).toSeconds());
  }
}
