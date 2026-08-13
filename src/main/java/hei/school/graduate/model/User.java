package hei.school.graduate.model;

import java.util.UUID;

public record User(
    UUID id,
    String email,
    String firstname,
    String lastname,
    Role role,
    String address,
    String password) {}
