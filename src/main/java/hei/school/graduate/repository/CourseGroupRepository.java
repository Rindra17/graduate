package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JCourseGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseGroupRepository extends JpaRepository<JCourseGroup, UUID> {
  List<JCourseGroup> findAllByCourse_Id(UUID courseId);
}
