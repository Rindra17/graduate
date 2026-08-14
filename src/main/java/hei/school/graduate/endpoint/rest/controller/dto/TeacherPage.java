package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.List;

public record TeacherPage(
    List<TeacherResponse> teachers, int page, int size, long totalElements, int totalPages) {}
