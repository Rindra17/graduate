package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.CohortRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CohortResultResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GraduateStudentResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GraduatesDownloadResponse;
import hei.school.graduate.model.Cohort;
import hei.school.graduate.service.CohortService;
import hei.school.graduate.service.GraduateExportService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/cohorts")
public class CohortController {

  private final CohortService cohortService;
  private final GraduateExportService graduateExportService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Cohort createCohort(@RequestBody CohortRequest request) {
    return cohortService.createCohort(request);
  }

  @GetMapping("/{id}/result")
  public CohortResultResponse getCohortResults(@PathVariable UUID id) {
    return cohortService.getCohortResults(id);
  }

  @GetMapping("/{id}/graduate")
  public List<GraduateStudentResponse> getCohortGraduates(@PathVariable UUID id) {
    return cohortService.getCohortGraduates(id);
  }

  @GetMapping("/{id}/graduate/export")
  public ResponseEntity<GraduatesDownloadResponse> exportGraduates(@PathVariable UUID id) {
    String url = graduateExportService.generateGraduatesDownloadUrl(id.toString());
    return ResponseEntity.ok(GraduatesDownloadResponse.builder().downloadUrl(url).build());
  }
}
