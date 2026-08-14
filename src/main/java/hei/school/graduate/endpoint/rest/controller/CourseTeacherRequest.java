package hei.school.graduate.endpoint.rest.controller;

import java.util.UUID;
import lombok.Data;

@Data
public class CourseTeacherRequest {
  private UUID teacherId;
}
