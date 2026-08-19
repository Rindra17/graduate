package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JGroupe;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<JGroupe, UUID> {
  List<JGroupe> findAllByCohort_Id(UUID cohortId);
}
