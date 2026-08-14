package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ExamRequest {
  private String title;
  private BigDecimal weight;
  private LocalDate examDate;
}
