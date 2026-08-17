package hei.school.graduate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.UUID;

public record User(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Role role,
    String address,
    @JsonIgnore String password,
    boolean mustChangePassword,
    LocalDateTime entranceDateTime) {}
