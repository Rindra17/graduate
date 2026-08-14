package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JStudent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<JStudent, UUID> {}
