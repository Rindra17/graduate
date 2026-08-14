package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.List;

public record AdminPage(
    List<AdminResponse> admins, int page, int size, long totalElements, int totalPages) {}
