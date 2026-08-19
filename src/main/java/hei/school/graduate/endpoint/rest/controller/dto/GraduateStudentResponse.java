package hei.school.graduate.endpoint.rest.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraduateStudentResponse {
  private String studentId;
  private String firstName;
  private String lastName;
  private String email;
  private String reference;
  private Double average;
}
