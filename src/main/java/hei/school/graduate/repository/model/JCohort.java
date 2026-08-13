package hei.school.graduate.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "COHORT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JCohort {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "start_year")
  private Integer startYear;

  @Column(name = "end_year")
  private Integer endYear;
}