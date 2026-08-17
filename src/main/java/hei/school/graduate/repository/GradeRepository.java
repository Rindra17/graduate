package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JGrade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<JGrade, UUID> {
  List<JGrade> findAllByExam_Id(UUID examId);

  List<JGrade> findAllByStudent_Id(UUID studentId);
}
