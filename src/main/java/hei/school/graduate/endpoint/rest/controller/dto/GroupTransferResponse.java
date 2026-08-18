package hei.school.graduate.endpoint.rest.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GroupTransferResponse(
    UUID id,
    UUID studentId,
    UUID previousGroupId,
    String previousGroupName,
    UUID newGroupId,
    String newGroupName,
    LocalDate transferDate,
    String reason) {}
