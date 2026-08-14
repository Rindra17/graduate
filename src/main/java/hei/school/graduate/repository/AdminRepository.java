package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JAdmin;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<JAdmin, UUID> {}
