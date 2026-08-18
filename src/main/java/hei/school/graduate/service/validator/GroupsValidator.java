package hei.school.graduate.service.validator;

import hei.school.graduate.endpoint.rest.controller.dto.GroupsRequest;
import hei.school.graduate.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class GroupsValidator {

  public void validate(GroupsRequest request) {
    if (request == null) {
      throw new BadRequestException("Groups request must not be null");
    }
    if (request.name() == null || request.name().isBlank()) {
      throw new BadRequestException("name is mandatory");
    }
    if (request.name().length() != 2) {
      throw new BadRequestException("name must be exactly 2 characters (e.g. \"K1\", \"J3\")");
    }
    if (request.cohortId() == null) {
      throw new BadRequestException("cohortId is mandatory");
    }
    if (request.branchId() == null) {
      throw new BadRequestException("branchId is mandatory");
    }
  }
}
