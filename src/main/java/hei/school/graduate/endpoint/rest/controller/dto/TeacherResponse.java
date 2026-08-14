package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;

public record TeacherResponse(
    UUID id, String firstName, String lastName, String email, String reference) {}
