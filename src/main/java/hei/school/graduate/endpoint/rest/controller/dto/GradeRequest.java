package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeRequest(UUID studentId, BigDecimal score) {}
