package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JCourseTeacher;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTeacherRepository extends JpaRepository<JCourseTeacher, UUID> {
  List<JCourseTeacher> findAllByCourse_Id(UUID courseId);
}
