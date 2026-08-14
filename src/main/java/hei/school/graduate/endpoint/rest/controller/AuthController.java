package hei.school.graduate.endpoint.rest.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import hei.school.graduate.endpoint.rest.controller.dto.LoginRequest;
import hei.school.graduate.endpoint.rest.controller.dto.RegisterRequest;
import hei.school.graduate.model.User;
import hei.school.graduate.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService service;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public User register(@RequestBody RegisterRequest request, HttpServletResponse response) {
    var result = service.register(request);
    response.addHeader(HttpHeaders.SET_COOKIE, result.cookie());

    return result.user();
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public User login(@RequestBody LoginRequest request, HttpServletResponse response) {
    var result = service.login(request);
    response.addHeader(HttpHeaders.SET_COOKIE, result.cookie());

    return result.user();
  }
}
