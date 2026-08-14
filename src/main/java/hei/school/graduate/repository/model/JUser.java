package hei.school.graduate.repository.model;

import hei.school.graduate.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JUser {

  @Id
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String password;

  @Column(name = "firstname")
  private String firstName;

  @Column(name = "lastname")
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  private String address;

  @Column(name = "must_change_password", nullable = false)
  private boolean mustChangePassword;

  @Column(name = "entrance_date_time")
  private LocalDateTime entranceDateTime;
}
