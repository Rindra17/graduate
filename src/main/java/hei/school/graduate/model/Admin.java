package hei.school.graduate.model;

import java.util.UUID;

public record Admin(UUID id, User user, String reference) {}
