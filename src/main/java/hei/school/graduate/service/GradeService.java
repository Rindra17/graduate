package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.GradeHistoryEntry;
import hei.school.graduate.endpoint.rest.controller.dto.GradeHistoryResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GradeRequest;
import hei.school.graduate.endpoint.rest.controller.dto.GradeResponse;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.GradeMapper;
import hei.school.graduate.model.CustomUserDetails;
import hei.school.graduate.model.Grade;
import hei.school.graduate.repository.ExamRepository;
import hei.school.graduate.repository.GradeHistoryRepository;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.UserRepository;
import hei.school.graduate.repository.model.JExam;
import hei.school.graduate.repository.model.JGrade;
import hei.school.graduate.repository.model.JGradeHistory;
import hei.school.graduate.repository.model.JStudent;
import hei.school.graduate.repository.model.JUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final GradeMapper gradeMapper;

  @Transactional(readOnly = true)
  public List<Grade> getGradesByExam(UUID examId) {
    findExamOrThrow(examId);
    return gradeRepository.findAllByExam_Id(examId).stream().map(gradeMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public GradeHistoryResponse getStudentGradeHistoryForExam(UUID examId, UUID studentId) {
    findExamOrThrow(examId);

    var history =
        gradeHistoryRepository.findAllByGrade_Exam_IdAndGrade_Student_IdOrderByModificationDateAsc(
            examId, studentId);

    var grades =
        history.stream()
            .map(
                entry ->
                    new GradeHistoryEntry(
                        entry.getId(),
                        entry.getNewScore(),
                        entry.getReason(),
                        entry.getModificationDate()))
            .toList();
    var currentScore = grades.isEmpty() ? null : grades.get(grades.size() - 1).getGrade();

    return new GradeHistoryResponse(studentId, examId, currentScore, grades);
  }

  @Transactional(readOnly = true)
  public GradeResponse getStudentGradeForExam(UUID examId, UUID studentId) {
    findExamOrThrow(examId);

    JGrade grade =
        gradeRepository
            .findByExam_IdAndStudent_Id(examId, studentId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Grade for student " + studentId + " on exam " + examId + " not found"));

    return new GradeResponse(
        grade.getId(), grade.getStudent().getId(), grade.getExam().getId(), grade.getScore());
  }

  @Transactional
  public GradeResponse upsertGradeForExam(UUID examId, GradeRequest gradeRequest) {
    JExam exam = findExamOrThrow(examId);

    JStudent student = findStudentOrThrow(gradeRequest.getStudentId());

    JGrade grade =
        gradeRepository
            .findByExam_IdAndStudent_Id(examId, gradeRequest.getStudentId())
            .orElseGet(() -> JGrade.builder().student(student).exam(exam).build());

    BigDecimal previousScore = grade.getScore();
    grade.setScore(gradeRequest.getScore());
    JGrade saved = gradeRepository.save(grade);

    saveGradeHistory(saved, previousScore, saved.getScore(), gradeRequest.getReason());

    return new GradeResponse(
        saved.getId(), saved.getStudent().getId(), saved.getExam().getId(), saved.getScore());
  }

  private void saveGradeHistory(
      JGrade grade, BigDecimal previousScore, BigDecimal newScore, String reason) {
    JUser actor = currentActor();
    if (actor == null) {
      return;
    }
    gradeHistoryRepository.save(
        JGradeHistory.builder()
            .grade(grade)
            .user(actor)
            .previousScore(previousScore)
            .newScore(newScore)
            .reason(reason)
            .build());
  }

  private JStudent findStudentOrThrow(UUID studentId) {
    return studentRepository
        .findById(studentId)
        .orElseThrow(() -> new NotFoundException("Student " + studentId + " not found"));
  }

  private JUser currentActor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
      return null;
    }
    return userRepository.findById(details.getUser().id()).orElse(null);
  }

  private JExam findExamOrThrow(UUID examId) {
    return examRepository
        .findById(examId)
        .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
  }
}
