package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponse {
  private UUID id;
  private UUID studentId;
  private UUID examId;
  private BigDecimal score;
}