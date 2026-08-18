package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentGradesResponse(
    List<CourseGradesResponse> courses, BigDecimal yearAverage, int creditEarned) {}
