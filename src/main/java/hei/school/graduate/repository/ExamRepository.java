package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JExam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<JExam, UUID> {
  List<JExam> findAllByCourse_Id(UUID courseId);
}
