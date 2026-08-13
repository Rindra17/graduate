package hei.school.graduate.mapper;

import hei.school.graduate.model.StudentGroupHistory;
import hei.school.graduate.repository.model.JStudentGroupHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StudentGroupHistoryMapper {

  private final StudentMapper studentMapper;
  private final GroupeMapper groupeMapper;

  public StudentGroupHistory toDomain(JStudentGroupHistory entity) {
    if (entity == null) return null;
    return new StudentGroupHistory(
        entity.getId(),
        studentMapper.toDomain(entity.getStudent()),
        groupeMapper.toDomain(entity.getGroup()),
        entity.getStartDate(),
        entity.getEndDate(),
        entity.getChangeReason());
  }

  public JStudentGroupHistory toEntity(StudentGroupHistory domain, String studentPasswordHash) {
    if (domain == null) return null;
    return JStudentGroupHistory.builder()
        .id(domain.id())
        .student(studentMapper.toEntity(domain.student(), studentPasswordHash))
        .group(groupeMapper.toEntity(domain.groupe()))
        .startDate(domain.startDate())
        .endDate(domain.endDate())
        .changeReason(domain.changeReason())
        .build();
  }
}
