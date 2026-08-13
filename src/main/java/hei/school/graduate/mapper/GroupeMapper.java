package hei.school.graduate.mapper;

import hei.school.graduate.model.Groupe;
import hei.school.graduate.repository.model.JGroupe;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GroupeMapper {

  private final CohortMapper cohortMapper;
  private final BranchMapper branchMapper;

  public Groupe toDomain(JGroupe entity) {
    if (entity == null) return null;
    return new Groupe(
        entity.getId(),
        entity.getName(),
        cohortMapper.toDomain(entity.getCohort()),
        branchMapper.toDomain(entity.getBranch()));
  }

  public JGroupe toEntity(Groupe domain) {
    if (domain == null) return null;
    return JGroupe.builder()
        .id(domain.id())
        .name(domain.name())
        .cohort(cohortMapper.toEntity(domain.cohort()))
        .branch(branchMapper.toEntity(domain.branch()))
        .build();
  }
}
