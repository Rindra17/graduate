package hei.school.graduate.endpoint.rest.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TransferResponse(
    UUID studentId, UUID previousGroupId, UUID newGroupId, LocalDate transferDate, String reason) {}
