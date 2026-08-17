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
public class GradeRequest {
  private UUID studentId;
  private BigDecimal score;
  private String reason;

  public GradeRequest(UUID studentId, BigDecimal score) {
    this(studentId, score, null);
  }
}