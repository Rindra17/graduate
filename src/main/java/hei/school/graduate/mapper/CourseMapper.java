package hei.school.graduate.mapper;

import hei.school.graduate.model.Course;
import hei.school.graduate.repository.model.JCourse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseMapper {

  private final SemesterMapper semesterMapper;
  private final BranchMapper branchMapper;

  public Course toDomain(JCourse entity) {
    if (entity == null) return null;
    return new Course(
        entity.getId(),
        semesterMapper.toDomain(entity.getSemester()),
        branchMapper.toDomain(entity.getBranch()),
        entity.getCode(),
        entity.getTitle(),
        entity.getCredits());
  }

  public JCourse toEntity(Course domain) {
    if (domain == null) return null;
    return JCourse.builder()
        .id(domain.id())
        .semester(semesterMapper.toEntity(domain.semester()))
        .branch(branchMapper.toEntity(domain.branch()))
        .code(domain.code())
        .title(domain.title())
        .credits(domain.credits())
        .build();
  }
}
