package hei.school.graduate.mapper;

import hei.school.graduate.model.Semester;
import hei.school.graduate.repository.model.JSemester;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SemesterMapper {

  private final CohortMapper cohortMapper;

  public Semester toDomain(JSemester entity) {
    if (entity == null) return null;
    return new Semester(
        entity.getId(),
        cohortMapper.toDomain(entity.getCohort()),
        entity.getSemesterNumber(),
        entity.getAcademicYear());
  }

  public JSemester toEntity(Semester domain) {
    if (domain == null) return null;
    return JSemester.builder()
        .id(domain.id())
        .cohort(cohortMapper.toEntity(domain.cohort()))
        .semesterNumber(domain.semesterNumber())
        .academicYear(domain.academicYear())
        .build();
  }
}
