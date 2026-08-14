package hei.school.graduate.endpoint.rest.controller.dto;

import hei.school.graduate.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

  private String firstName;
  private String lastName;
  private String email;
  private String address;
  private Role role;
  private String password;
}
