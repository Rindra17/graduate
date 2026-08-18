package hei.school.graduate.service;

import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.GroupeMapper;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.repository.StudentGroupHistoryRepository;
import hei.school.graduate.repository.StudentRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentGroupService {

  private final StudentRepository studentRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final GroupeMapper groupeMapper;

  public Groupe getStudentGroup(UUID studentId) {
    studentRepository
        .findById(studentId)
        .orElseThrow(() -> new NotFoundException("Student not found"));

    var history =
        studentGroupHistoryRepository
            .findByStudent_IdAndEndDateIsNull(studentId)
            .orElseThrow(() -> new NotFoundException("Group not found"));

    return groupeMapper.toDomain(history.getGroup());
  }
}
