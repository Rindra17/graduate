package hei.school.graduate.repository;

import hei.school.graduate.model.Role;
import hei.school.graduate.repository.model.JUser;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {

  Optional<JUser> findByEmail(String email);

  long countByRoleAndEntranceDateTimeBetween(Role role, LocalDateTime start, LocalDateTime end);
}
