package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JTeacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<JTeacher, UUID> {}
