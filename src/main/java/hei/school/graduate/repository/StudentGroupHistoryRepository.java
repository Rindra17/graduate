package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JStudentGroupHistory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {
  Optional<JStudentGroupHistory> findByStudent_IdAndEndDateIsNull(UUID studentId);
}
