package hei.school.graduate.endpoint.rest.controller.dto;

import hei.school.graduate.model.User;

public record RegisterResponse(User user, String temporaryPassword) {}
