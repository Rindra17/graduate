package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.ChangePasswordRequest;
import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.LoginResponse;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterResponse;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.User;
import hei.school.graduate.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService service;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public RegisterResponse register(
      @RequestBody RegisterRequest request, HttpServletResponse response) {
    var result = service.register(request);
    response.addHeader(HttpHeaders.SET_COOKIE, result.cookie());

    return new RegisterResponse(result.user(), result.temporaryPassword());
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
    var result = service.login(request);
    response.addHeader(HttpHeaders.SET_COOKIE, result.cookie());

    return new LoginResponse(result.user(), result.token());
  }

  @PostMapping("/change-password")
  @ResponseStatus(HttpStatus.OK)
  public User changePassword(
      @RequestBody ChangePasswordRequest request,
      HttpServletResponse response,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    var result = service.changePassword(userDetails.getUser().id(), request);
    response.addHeader(HttpHeaders.SET_COOKIE, result.cookie());

    return result.user();
  }
}
