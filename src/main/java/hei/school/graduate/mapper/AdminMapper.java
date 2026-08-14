package hei.school.graduate.mapper;

import hei.school.graduate.model.Admin;
import hei.school.graduate.repository.model.JAdmin;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AdminMapper {

  private final UserMapper userMapper;

  public Admin toDomain(JAdmin entity) {
    if (entity == null) return null;
    return new Admin(entity.getId(), userMapper.toDomain(entity.getUser()), entity.getReference());
  }

  public JAdmin toEntity(Admin domain) {
    if (domain == null) return null;
    return JAdmin.builder()
        .id(domain.id())
        .user(userMapper.toEntity(domain.user()))
        .reference(domain.reference())
        .build();
  }
}
