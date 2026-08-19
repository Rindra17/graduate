package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JStudentGroupHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {
  List<JStudentGroupHistory> findAllByGroup_IdIn(List<UUID> groupIds);

  Optional<JStudentGroupHistory> findByStudent_IdAndEndDateIsNull(UUID studentId);

  List<JStudentGroupHistory> findAllByStudent_IdOrderByStartDateAsc(UUID studentId);

  List<JStudentGroupHistory> findAllByGroup_IdIn(List<UUID> groupIds);
}
