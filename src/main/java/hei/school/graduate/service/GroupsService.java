package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.GroupsRequest;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.GroupeMapper;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.repository.BranchRepository;
import hei.school.graduate.repository.CohortRepository;
import hei.school.graduate.repository.GroupRepository;
import hei.school.graduate.repository.model.JBranch;
import hei.school.graduate.repository.model.JCohort;
import hei.school.graduate.repository.model.JGroupe;
import hei.school.graduate.service.validator.GroupsValidator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupsService {

  private final GroupRepository groupRepository;
  private final CohortRepository cohortRepository;
  private final BranchRepository branchRepository;
  private final GroupeMapper groupeMapper;
  private final GroupsValidator groupsValidator;

  public Groupe addGroup(GroupsRequest request) {
    groupsValidator.validate(request);

    JCohort cohort =
        cohortRepository
            .findById(request.cohortId())
            .orElseThrow(() -> new NotFoundException("Cohort not found"));
    JBranch branch =
        branchRepository
            .findById(request.branchId())
            .orElseThrow(() -> new NotFoundException("Branch not found"));

    JGroupe entity = JGroupe.builder().name(request.name()).cohort(cohort).branch(branch).build();

    return groupeMapper.toDomain(groupRepository.save(entity));
  }

  public Groupe updateGroup(UUID id, GroupsRequest request) {
    groupsValidator.validate(request);

    JGroupe existing =
        groupRepository.findById(id).orElseThrow(() -> new NotFoundException("Group not found"));

    JCohort cohort =
        cohortRepository
            .findById(request.cohortId())
            .orElseThrow(() -> new NotFoundException("Cohort not found"));
    JBranch branch =
        branchRepository
            .findById(request.branchId())
            .orElseThrow(() -> new NotFoundException("Branch not found"));

    existing.setName(request.name());
    existing.setCohort(cohort);
    existing.setBranch(branch);

    return groupeMapper.toDomain(groupRepository.save(existing));
  }

  public void deleteGroup(UUID id) {
    if (!groupRepository.existsById(id)) {
      throw new NotFoundException("Group not found");
    }
    groupRepository.deleteById(id);
  }
}
