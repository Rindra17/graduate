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
        entity.getFirstname(),
        entity.getLastname(),
        entity.getRole(),
        entity.getAddress());
  }

  public JUser toEntity(User domain, String passwordHash) {
    if (domain == null) return null;
    return JUser.builder()
        .id(domain.id())
        .email(domain.email())
        .passwordHash(passwordHash)
        .firstname(domain.firstname())
        .lastname(domain.lastname())
        .role(domain.role())
        .address(domain.address())
        .build();
  }
}
