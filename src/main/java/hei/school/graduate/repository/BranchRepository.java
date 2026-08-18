package hei.school.graduate.repository;

import hei.school.graduate.repository.model.JBranch;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<JBranch, UUID> {}
