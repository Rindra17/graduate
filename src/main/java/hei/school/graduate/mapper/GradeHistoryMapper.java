package hei.school.graduate.mapper;

import hei.school.graduate.model.GradeHistory;
import hei.school.graduate.repository.model.JGradeHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeHistoryMapper {

  private final GradeMapper gradeMapper;
  private final UserMapper userMapper;

  public GradeHistory toDomain(JGradeHistory entity) {
    if (entity == null) return null;
    return new GradeHistory(
        entity.getId(),
        gradeMapper.toDomain(entity.getGrade()),
        userMapper.toDomain(entity.getUser()),
        entity.getPreviousScore(),
        entity.getNewScore(),
        entity.getReason(),
        entity.getModificationDate());
  }

  public JGradeHistory toEntity(GradeHistory domain) {
    if (domain == null) return null;
    return JGradeHistory.builder()
        .id(domain.id())
        .grade(gradeMapper.toEntity(domain.grade()))
        .user(userMapper.toEntity(domain.user()))
        .previousScore(domain.previousScore())
        .newScore(domain.newScore())
        .reason(domain.reason())
        .modificationDate(domain.modificationDate())
        .build();
  }
}
