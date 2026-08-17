package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExamResponse(
    UUID id, String courseName, String title, BigDecimal weight, LocalDate examDate) {}
