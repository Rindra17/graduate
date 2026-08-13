package hei.school.graduate.mapper;

import hei.school.graduate.model.CourseTeacher;
import hei.school.graduate.repository.model.JCourseTeacher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseTeacherMapper {

  private final TeacherMapper teacherMapper;
  private final CourseMapper courseMapper;

  public CourseTeacher toDomain(JCourseTeacher entity) {
    if (entity == null) return null;
    return new CourseTeacher(
        entity.getId(),
        teacherMapper.toDomain(entity.getTeacher()),
        courseMapper.toDomain(entity.getCourse()));
  }

  public JCourseTeacher toEntity(CourseTeacher domain, String teacherPasswordHash) {
    if (domain == null) return null;
    return JCourseTeacher.builder()
        .id(domain.id())
        .teacher(teacherMapper.toEntity(domain.teacher(), teacherPasswordHash))
        .course(courseMapper.toEntity(domain.course()))
        .build();
  }
}
