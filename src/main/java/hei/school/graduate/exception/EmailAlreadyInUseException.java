package hei.school.graduate.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends ApiException {
  public EmailAlreadyInUseException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
