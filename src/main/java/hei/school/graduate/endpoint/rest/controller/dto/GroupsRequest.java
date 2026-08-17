package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;

public record GroupsRequest(String name, UUID cohortId, UUID branchId) {}
