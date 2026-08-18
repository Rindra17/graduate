package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.TransferRequest;
import hei.school.graduate.endpoint.rest.controller.dto.TransferResponse;
import hei.school.graduate.exception.BadRequestException;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.GroupeMapper;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.repository.GroupRepository;
import hei.school.graduate.repository.StudentGroupHistoryRepository;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.model.JStudentGroupHistory;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class StudentGroupService {

  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;
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

  @Transactional
  public TransferResponse transferStudent(UUID studentId, TransferRequest request) {
    if (request == null) {
      throw new BadRequestException("Transfer request must not be null");
    }
    if (request.groupId() == null) {
      throw new BadRequestException("groupId is mandatory");
    }

    var student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found"));

    var targetGroup =
        groupRepository
            .findById(request.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found"));

    LocalDate today = LocalDate.now();
    JStudentGroupHistory current =
        studentGroupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId).orElse(null);

    if (current != null && current.getGroup().getId().equals(targetGroup.getId())) {
      throw new BadRequestException("Student is already in group " + targetGroup.getId());
    }

    if (current != null) {
      current.setEndDate(today);
      studentGroupHistoryRepository.save(current);
    }

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(student)
            .group(targetGroup)
            .startDate(today)
            .endDate(null)
            .changeReason(request.reason())
            .build());

    UUID previousGroupId = current == null ? null : current.getGroup().getId();
    return new TransferResponse(
        studentId, previousGroupId, targetGroup.getId(), today, request.reason());
  }
}
