package hei.school.graduate.service.validator;

import hei.school.graduate.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

  public void validate(String password) {
    if (password == null || password.isBlank()) {
      throw new BadRequestException("New password must not be empty");
    }
  }
}
