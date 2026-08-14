package hei.school.graduate.service;

import hei.school.graduate.model.Role;
import hei.school.graduate.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReferenceGenerator {

  private final UserRepository userRepository;

  public String generate(Role role, LocalDateTime entranceDateTime) {
    var prefix =
        switch (role) {
          case ADMIN -> "ADM";
          case TEACHER -> "TCR";
          case STUDENT -> "STD";
        };
    var year = entranceDateTime.getYear() % 100;
    var startOfYear = entranceDateTime.toLocalDate().withDayOfYear(1).atStartOfDay();
    var sequence =
        userRepository.countByRoleAndEntranceDateTimeBetween(
                role, startOfYear, startOfYear.plusYears(1))
            + 1;
    return prefix + String.format("%02d%03d", year, sequence);
  }
}
