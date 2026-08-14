package hei.school.graduate.endpoint.rest.controller.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}
