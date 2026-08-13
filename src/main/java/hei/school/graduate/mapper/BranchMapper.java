package hei.school.graduate.mapper;

import hei.school.graduate.model.Branch;
import hei.school.graduate.repository.model.JBranch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

  public Branch toDomain(JBranch entity) {
    if (entity == null) return null;
    return new Branch(entity.getId(), entity.getCode(), entity.getName());
  }

  public JBranch toEntity(Branch domain) {
    if (domain == null) return null;
    return JBranch.builder().id(domain.id()).code(domain.code()).name(domain.name()).build();
  }
}
