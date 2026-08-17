package hei.school.graduate.endpoint.rest.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeHistoryEntry {
  private UUID id;
  private BigDecimal grade;
  private String reason;
  private Instant modificationDate;
}