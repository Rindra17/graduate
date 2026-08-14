package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.AuthResult;
import hei.school.graduate.endpoint.rest.controller.dto.ChangePasswordRequest;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterResult;
import hei.school.graduate.exception.EmailAlreadyInUseException;
import hei.school.graduate.exception.InvalidCredentialsException;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.UserMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Role;
import hei.school.graduate.model.User;
import hei.school.graduate.repository.UserRepository;
import hei.school.graduate.repository.model.JAdmin;
import hei.school.graduate.repository.model.JStudent;
import hei.school.graduate.repository.model.JTeacher;
import hei.school.graduate.repository.model.JUser;
import hei.school.graduate.security.JwtService;
import hei.school.graduate.security.RandomPasswordGenerator;
import hei.school.graduate.service.validator.PasswordValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository repository;
  private final UserMapper mapper;
  private final PasswordEncoder encoder;
  private final JwtService service;
  private final RandomPasswordGenerator passwordGenerator;
  private final ReferenceGenerator referenceGenerator;
  private final PasswordValidator passwordValidator;

  @PersistenceContext private EntityManager entityManager;

  @Value("${jwt.expiration}")
  private long expiration;

  @Transactional
  public RegisterResult register(RegisterRequest request) {
    if (repository.findByEmail(request.getEmail()).isPresent()) {
      throw new EmailAlreadyInUseException("Email already in use");
    }

    var role = request.getRole() != null ? request.getRole() : Role.STUDENT;
    var temporaryPassword = passwordGenerator.generate();
    var password = encoder.encode(temporaryPassword);
    var entranceDateTime = LocalDateTime.now();
    var reference = referenceGenerator.generate(role, entranceDateTime);

    var user =
        new User(
            UUID.randomUUID(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            role,
            request.getAddress(),
            password,
            true,
            entranceDateTime);

    var jUser = mapper.toEntity(user);
    entityManager.persist(jUser);
    var saved = mapper.toDomain(jUser);
    createRoleRecord(jUser, reference);

    var token = service.generateToken(new CustomUserDetails(saved));

    return new RegisterResult(saved, temporaryPassword, buildCookie(token));
  }

  public AuthResult login(LoginRequest request) {
    var jUser =
        repository
            .findByEmail(request.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    if (!encoder.matches(request.password(), jUser.getPassword())) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    var user = mapper.toDomain(jUser);
    var token = service.generateToken(new CustomUserDetails(user));

    return new AuthResult(user, buildCookie(token));
  }

  public AuthResult changePassword(UUID userId, ChangePasswordRequest request) {
    passwordValidator.validate(request.newPassword());

    var jUser =
        repository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (!encoder.matches(request.currentPassword(), jUser.getPassword())) {
      throw new InvalidCredentialsException("Current password is incorrect");
    }

    jUser.setPassword(encoder.encode(request.newPassword()));
    jUser.setMustChangePassword(false);

    var saved = mapper.toDomain(repository.save(jUser));
    var token = service.generateToken(new CustomUserDetails(saved));

    return new AuthResult(saved, buildCookie(token));
  }

  private void createRoleRecord(JUser jUser, String reference) {
    switch (jUser.getRole()) {
      case STUDENT ->
          entityManager.persist(
              JStudent.builder().user(jUser).reference(reference).status("ACTIVE").build());
      case TEACHER ->
          entityManager.persist(JTeacher.builder().user(jUser).reference(reference).build());
      case ADMIN ->
          entityManager.persist(JAdmin.builder().user(jUser).reference(reference).build());
    }
  }

  private String buildCookie(String token) {
    return String.format(
        "token=%s; HttpOnly; SameSite=Strict; Path=/; Max-Age=%d",
        token, Duration.ofMillis(expiration).toSeconds());
  }
}
