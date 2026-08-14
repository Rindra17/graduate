package hei.school.graduate.mapper;

import hei.school.graduate.model.User;
import hei.school.graduate.repository.model.JUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toDomain(JUser entity) {
    if (entity == null) return null;
    return new User(
        entity.getId(),
        entity.getEmail(),
        entity.getFirstName(),
        entity.getLastName(),
        entity.getRole(),
        entity.getAddress(),
        entity.getPassword(),
        entity.isMustChangePassword(),
        entity.getEntranceDateTime());
  }

  public JUser toEntity(User domain) {
    if (domain == null) return null;
    return JUser.builder()
        .id(domain.id())
        .email(domain.email())
        .password(domain.password())
        .firstName(domain.firstName())
        .lastName(domain.lastName())
        .role(domain.role())
        .address(domain.address())
        .mustChangePassword(domain.mustChangePassword())
        .entranceDateTime(domain.entranceDateTime())
        .build();
  }
}
