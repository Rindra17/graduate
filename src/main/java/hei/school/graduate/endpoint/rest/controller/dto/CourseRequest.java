package hei.school.graduate.endpoint.rest.controller.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class CourseRequest {
  private UUID semesterId;
  private UUID branchId;
  private String code;
  private String title;
  private Integer credits;
}
