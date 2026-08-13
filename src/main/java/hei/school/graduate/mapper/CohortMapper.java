package hei.school.graduate.mapper;

import hei.school.graduate.model.Cohort;
import hei.school.graduate.repository.model.JCohort;
import org.springframework.stereotype.Component;

@Component
public class CohortMapper {

  public Cohort toDomain(JCohort entity) {
    if (entity == null) return null;
    return new Cohort(entity.getId(), entity.getName(), entity.getStartYear(), entity.getEndYear());
  }

  public JCohort toEntity(Cohort domain) {
    if (domain == null) return null;
    return JCohort.builder()
        .id(domain.id())
        .name(domain.name())
        .startYear(domain.startYear())
        .endYear(domain.endYear())
        .build();
  }
}
