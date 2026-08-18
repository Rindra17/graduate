package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;

public record GradeReportResponse(String message, UUID studentId, String academicYear) {}
