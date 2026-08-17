package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.GradeRequest;
import hei.school.graduate.endpoint.rest.controller.dto.GradeResponse;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.GradeMapper;
import hei.school.graduate.model.Grade;
import hei.school.graduate.repository.ExamRepository;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.model.JExam;
import hei.school.graduate.repository.model.JGrade;
import hei.school.graduate.repository.model.JStudent;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final ExamRepository examRepository;
  private final StudentRepository studentRepository;
  private final GradeMapper gradeMapper;

  @Transactional(readOnly = true)
  public List<Grade> getGradesByExam(UUID examId) {
    JExam exam = findExamOrThrow(examId);
    return gradeRepository.findAllByExam_Id(examId).stream().map(gradeMapper::toDomain).toList();
  }

  @Transactional
  public GradeResponse addGradeToExam(UUID examId, GradeRequest gradeRequest) {
    JExam exam = findExamOrThrow(examId);

    JStudent student =
        studentRepository
            .findById(gradeRequest.getStudentId())
            .orElseThrow(
                () ->
                    new NotFoundException("Student " + gradeRequest.getStudentId() + " not found"));

    JGrade grade =
        JGrade.builder().student(student).exam(exam).score(gradeRequest.getScore()).build();
    JGrade saved = gradeRepository.save(grade);

    return new GradeResponse(
        saved.getId(), saved.getStudent().getId(), saved.getExam().getId(), saved.getScore());
  }

  private JExam findExamOrThrow(UUID examId) {
    return examRepository
        .findById(examId)
        .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
  }
}
