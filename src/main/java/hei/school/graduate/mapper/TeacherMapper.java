package hei.school.graduate.mapper;

import hei.school.graduate.model.Teacher;
import hei.school.graduate.repository.model.JTeacher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TeacherMapper {

  private final UserMapper userMapper;

  public Teacher toDomain(JTeacher entity) {
    if (entity == null) return null;
    return new Teacher(
        entity.getId(), userMapper.toDomain(entity.getUser()), entity.getEmployeeNumber());
  }

  public JTeacher toEntity(Teacher domain, String passwordHash) {
    if (domain == null) return null;
    return JTeacher.builder()
        .id(domain.id())
        .user(userMapper.toEntity(domain.user(), passwordHash))
        .employeeNumber(domain.employeeNumber())
        .build();
  }
}
