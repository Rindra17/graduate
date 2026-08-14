package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.List;

public record StudentPage(
    List<StudentResponse> students, int page, int size, long totalElements, int totalPages) {}
