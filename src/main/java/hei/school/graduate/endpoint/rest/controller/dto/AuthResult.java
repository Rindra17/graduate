package hei.school.graduate.endpoint.rest.controller.dto;

import hei.school.graduate.model.User;

public record AuthResult(User user, String cookie) {
}
