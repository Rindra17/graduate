package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;

public record TransferRequest(UUID groupId, String reason) {}
