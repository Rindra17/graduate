package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.GroupTransferResponse;
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
import java.util.ArrayList;
import java.util.List;
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

  @Transactional(readOnly = true)
  public List<GroupTransferResponse> getStudentGroupHistory(UUID studentId) {
    studentRepository
        .findById(studentId)
        .orElseThrow(() -> new NotFoundException("Student not found"));

    var history = studentGroupHistoryRepository.findAllByStudent_IdOrderByStartDateAsc(studentId);

    List<GroupTransferResponse> responses = new ArrayList<>();
    UUID previousGroupId = null;
    String previousGroupName = null;
    for (JStudentGroupHistory entry : history) {
      responses.add(
          new GroupTransferResponse(
              entry.getId(),
              studentId,
              previousGroupId,
              previousGroupName,
              entry.getGroup().getId(),
              entry.getGroup().getName(),
              entry.getStartDate(),
              entry.getChangeReason()));
      previousGroupId = entry.getGroup().getId();
      previousGroupName = entry.getGroup().getName();
    }
    return responses;
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
