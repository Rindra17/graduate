package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamRequest {
  private String title;
  private BigDecimal weight;
  private LocalDate examDate;
}
