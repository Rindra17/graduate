package hei.school.graduate.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.HandlerMethod;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleApiException_returnsStatusAndMessage() {
    var ex = new InvalidCredentialsException("Invalid email or password");

    var response = handler.handleApiException(ex);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals(401, response.getBody().status());
    assertEquals("Invalid email or password", response.getBody().message());
  }

  @Test
  void handleAccessDenied_returns403() {
    var response = handler.handleAccessDenied(new AccessDeniedException("denied"));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals(403, response.getBody().status());
    assertEquals("Access denied", response.getBody().message());
  }

  @Test
  void handleAuthentication_returns401() {
    var response =
        handler.handleAuthentication(new BadCredentialsException("Bad credentials"));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals(401, response.getBody().status());
    assertEquals("Invalid credentials", response.getBody().message());
  }

  @Test
  void handleValidation_returns400WithFieldErrors() throws NoSuchMethodException {
    var method = SampleController.class.getMethod("create", String.class);
    var parameter = new HandlerMethod(new SampleController(), method).getMethodParameters()[0];
    var bindingResult = new BeanPropertyBindingResult(new Object(), "object");
    bindingResult.addError(new FieldError("object", "email", "must not be blank"));

    var response = handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().status());
    assertEquals("email: must not be blank", response.getBody().message());
  }

  @Test
  void handleNotReadable_returns400() {
    var response =
        handler.handleNotReadable(
            new HttpMessageNotReadableException("malformed", new StubInputMessage()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().status());
    assertEquals("Malformed request body", response.getBody().message());
  }

  @Test
  void handleUnexpected_returns500() {
    var response = handler.handleUnexpected(new IllegalStateException("boom"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(500, response.getBody().status());
    assertEquals("Internal server error", response.getBody().message());
  }

  private static class SampleController {
    public void create(String email) {}
  }

  private static class StubInputMessage implements HttpInputMessage {

    @Override
    public org.springframework.http.HttpHeaders getHeaders() {
      return new org.springframework.http.HttpHeaders();
    }

    @Override
    public java.io.InputStream getBody() throws IOException {
      return java.io.InputStream.nullInputStream();
    }
  }
}
