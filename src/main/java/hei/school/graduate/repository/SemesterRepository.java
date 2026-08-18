package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JSemester;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<JSemester, UUID> {

  @Query(
      "SELECT DISTINCT s.academicYear FROM JSemester s WHERE s.cohort.id = :cohortId ORDER BY s.academicYear")
  List<String> findAcademicYearsByCohortId(@Param("cohortId") UUID cohortId);
}
