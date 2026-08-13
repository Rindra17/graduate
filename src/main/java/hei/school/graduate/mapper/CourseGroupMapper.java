package hei.school.graduate.mapper;

import hei.school.graduate.model.CourseGroup;
import hei.school.graduate.repository.model.JCourseGroup;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseGroupMapper {

  private final GroupeMapper groupeMapper;
  private final CourseMapper courseMapper;

  public CourseGroup toDomain(JCourseGroup entity) {
    if (entity == null) return null;
    return new CourseGroup(
        entity.getId(),
        groupeMapper.toDomain(entity.getGroupe()),
        courseMapper.toDomain(entity.getCourse()));
  }

  public JCourseGroup toEntity(CourseGroup domain) {
    if (domain == null) return null;
    return JCourseGroup.builder()
        .id(domain.id())
        .groupe(groupeMapper.toEntity(domain.groupe()))
        .course(courseMapper.toEntity(domain.course()))
        .build();
  }
}
