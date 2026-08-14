package hei.school.graduate.mapper;

import hei.school.graduate.model.Grade;
import hei.school.graduate.repository.model.JGrade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeMapper {

  private final StudentMapper studentMapper;
  private final ExamMapper examMapper;

  public Grade toDomain(JGrade entity) {
    if (entity == null) return null;
    return new Grade(
        entity.getId(),
        studentMapper.toDomain(entity.getStudent()),
        examMapper.toDomain(entity.getExam()),
        entity.getScore());
  }

  public JGrade toEntity(Grade domain) {
    if (domain == null) return null;
    return JGrade.builder()
        .id(domain.id())
        .student(studentMapper.toEntity(domain.student()))
        .exam(examMapper.toEntity(domain.exam()))
        .score(domain.score())
        .build();
  }
}
