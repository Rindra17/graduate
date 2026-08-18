package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JGradeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {
  List<JGradeHistory> findAllByGrade_Exam_IdAndGrade_Student_IdOrderByModificationDateAsc(
      UUID examId, UUID studentId);
}
