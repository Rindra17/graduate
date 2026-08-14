package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeResponse(UUID id, UUID studentId, UUID examId, BigDecimal score) {}
