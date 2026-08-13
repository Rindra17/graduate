package hei.school.graduate.mapper;

import hei.school.graduate.model.Student;
import hei.school.graduate.repository.model.JStudent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StudentMapper {

  private final UserMapper userMapper;

  public Student toDomain(JStudent entity) {
    if (entity == null) return null;
    return new Student(
        entity.getId(),
        userMapper.toDomain(entity.getUser()),
        entity.getStudentNumber(),
        entity.getStatus());
  }

  public JStudent toEntity(Student domain, String passwordHash) {
    if (domain == null) return null;
    return JStudent.builder()
        .id(domain.id())
        .user(userMapper.toEntity(domain.user(), passwordHash))
        .studentNumber(domain.studentNumber())
        .status(domain.status())
        .build();
  }
}
