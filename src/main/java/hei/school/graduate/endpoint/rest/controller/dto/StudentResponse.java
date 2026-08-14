package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;

public record StudentResponse(
    UUID id, String firstName, String lastName, String email, String reference, String status) {}
