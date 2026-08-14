package hei.school.graduate.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ADMIN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JAdmin {

  @Id
  @Column(name = "user_id")
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private JUser user;

  private String reference;
}
