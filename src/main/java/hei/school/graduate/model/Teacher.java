package hei.school.graduate.model;

import java.util.UUID;

public record Teacher(UUID id, User user, String employeeNumber) {}
