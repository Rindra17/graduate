package hei.school.graduate.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import hei.school.graduate.exception.model.ExceptionBody;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ExceptionBody> handleApiException(
      ApiException exception, HttpServletRequest request) {
    return ResponseEntity.status(exception.getStatus().value())
        .body(
            new ExceptionBody(
                exception.getStatus().value(),
                exception.getStatus().getReasonPhrase(),
                exception.getMessage(),
                request.getPathInfo(),
                Instant.now()));
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ExceptionBody> handleMethodArgumentTypeMismatchOrNotValidException(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(
            new ExceptionBody(
                400, "Bad Request", exception.getMessage(), request.getPathInfo(), Instant.now()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionBody> handleException(
      Exception exception, HttpServletRequest request) {
    return ResponseEntity.status(500)
        .body(
            new ExceptionBody(
                500,
                "An internal error has occurred",
                exception.getMessage(),
                request.getPathInfo(),
                Instant.now()));
  }
}
