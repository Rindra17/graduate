package hei.school.graduate.mapper;

import hei.school.graduate.model.Exam;
import hei.school.graduate.repository.model.JExam;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExamMapper {

  private final CourseMapper courseMapper;

  public Exam toDomain(JExam entity) {
    if (entity == null) return null;
    return new Exam(
        entity.getId(),
        courseMapper.toDomain(entity.getCourse()),
        entity.getTitle(),
        entity.getWeight(),
        entity.getExamDate());
  }

  public JExam toEntity(Exam domain) {
    if (domain == null) return null;
    return JExam.builder()
        .id(domain.id())
        .course(courseMapper.toEntity(domain.course()))
        .title(domain.title())
        .weight(domain.weight())
        .examDate(domain.examDate())
        .build();
  }
}
