package hei.school.graduate.model;

import java.util.UUID;

public record User(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Role role,
    String address,
    String password) {}
