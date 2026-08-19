package hei.school.graduate.endpoint.rest.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CohortRequest {
  private String name;
  private Integer startYear;
}
