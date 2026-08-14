package hei.school.graduate.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final User user;

  public static CustomUserDetails fromJwtClaims(
      UUID id, String email, Role role, boolean mustChangePassword) {
    return new CustomUserDetails(
        new User(id, email, null, null, role, null, null, mustChangePassword, null));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
  }

  @Override
  public String getUsername() {
    return user.email();
  }

  @Override
  public String getPassword() {
    return user.password();
  }

  public User getUser() {
    return user;
  }

  public boolean mustChangePassword() {
    return user.mustChangePassword();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
