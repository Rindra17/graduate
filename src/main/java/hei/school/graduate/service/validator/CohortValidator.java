package hei.school.graduate.service.validator;

import hei.school.graduate.endpoint.rest.controller.dto.CohortRequest;
import hei.school.graduate.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class CohortValidator {

  public void validate(CohortRequest request) {
    if (request == null) {
      throw new BadRequestException("Cohort request must not be null");
    }
    if (request.getName() == null || request.getName().isBlank()) {
      throw new BadRequestException("name is mandatory");
    }
    if (request.getStartYear() == null) {
      throw new BadRequestException("startYear is mandatory");
    }
    if (request.getStartYear() <= 0) {
      throw new BadRequestException("startYear must be positive");
    }
  }
}
