package hei.school.graduate.model;

import java.util.UUID;

public record Student(UUID id, User user, String studentNumber, String status) {}
