package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CohortResultResponse {
  private String cohortId;
  private String cohortName;
  private int totalStudents;
  private int graduates;
  private int failed;
  private List<CohortStudentResult> students;
}
