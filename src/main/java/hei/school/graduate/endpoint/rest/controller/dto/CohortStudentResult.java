package hei.school.graduate.endpoint.rest.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CohortStudentResult {
  private String studentId;
  private String firstName;
  private String lastName;
  private String email;
  private String reference;
  private String status;
  private Double average;
}
