package hei.school.graduate.service;

import hei.school.graduate.mapper.UserMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));
    var user = userMapper.toDomain(jUser);
    return new CustomUserDetails(user);
  }
}
