package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseGradesResponse(
    UUID courseId, String courseCode, String courseTitle, Integer credits, BigDecimal average) {}
