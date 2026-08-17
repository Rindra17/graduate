package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.CohortRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CohortResultResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GraduateStudentResponse;
import hei.school.graduate.model.Cohort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CohortService {

  public Cohort createCohort(CohortRequest request) {
    throw new UnsupportedOperationException("Unimplemented method 'createCohort'");
  }

  public CohortResultResponse getCohortResults(UUID id) {
    throw new UnsupportedOperationException("Unimplemented method 'getCohortResults'");
  }

  public List<GraduateStudentResponse> getCohortGraduates(UUID id) {
    throw new UnsupportedOperationException("Unimplemented method 'getCohortGraduates'");
  }
}
