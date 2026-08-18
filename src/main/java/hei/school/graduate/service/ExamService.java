package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamResponse;
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
import hei.school.graduate.service.validator.CourseValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final StudentRepository studentRepository;
  private final GradeMapper gradeMapper;
  private final CourseValidator courseValidator;

  public ExamResponse getExam(UUID id) {
    JExam exam =
        examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam not found"));
    return new ExamResponse(
        exam.getId(),
        exam.getCourse().getTitle(),
        exam.getTitle(),
        exam.getWeight(),
        exam.getExamDate());
  }

  public ExamResponse updateExam(UUID id, ExamRequest request) {
    JExam exam =
        examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam not found"));

    List<JExam> otherExams =
        examRepository.findAllByCourse_Id(exam.getCourse().getId()).stream()
            .filter(e -> !e.getId().equals(id))
            .toList();
    BigDecimal weight = CourseValidator.normalizeWeight(request.getWeight());
    courseValidator.validateExamWeight(otherExams, weight);

    exam.setTitle(request.getTitle());
    exam.setWeight(weight);
    exam.setExamDate(request.getExamDate());

    JExam updated = examRepository.save(exam);
    return new ExamResponse(
        updated.getId(),
        updated.getCourse().getTitle(),
        updated.getTitle(),
        updated.getWeight(),
        updated.getExamDate());
  }

  public void deleteExam(UUID id) {
    examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam not found"));
    examRepository.deleteById(id);
  }

  public List<Grade> getGradesByExamId(UUID id) {
    if (!examRepository.existsById(id)) {
      throw new NotFoundException("Exam not found");
    }
    return gradeRepository.findAllByExam_Id(id).stream().map(gradeMapper::toDomain).toList();
  }

  public GradeResponse addGradeToExam(UUID examId, GradeRequest request) {
    JExam exam =
        examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam not found"));

    JStudent student =
        studentRepository
            .findById(request.studentId())
            .orElseThrow(() -> new NotFoundException("Student not found"));

    JGrade grade = JGrade.builder().exam(exam).student(student).score(request.score()).build();

    JGrade saved = gradeRepository.save(grade);
    return new GradeResponse(
        saved.getId(), saved.getStudent().getId(), saved.getExam().getId(), saved.getScore());
  }
}
