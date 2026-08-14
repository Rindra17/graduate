package hei.school.graduate.endpoint.rest.controller.dto;

import hei.school.graduate.model.User;

public record RegisterResult(User user, String temporaryPassword, String cookie) {}
