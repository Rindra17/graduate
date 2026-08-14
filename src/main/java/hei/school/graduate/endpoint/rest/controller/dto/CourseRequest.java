package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseRequest {
  private UUID semesterId;
  private UUID branchId;
  private String code;
  private String title;
  private Integer credits;
}
