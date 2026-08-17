package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GradeHistoryResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GradeRequest;
import hei.school.graduate.endpoint.rest.controller.dto.GradeResponse;
import hei.school.graduate.model.Grade;
import hei.school.graduate.service.ExamService;
import hei.school.graduate.service.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exams")
@AllArgsConstructor
public class ExamController {

  private final GradeService gradeService;
  private final ExamService examService;

  @GetMapping("/{id}")
  public ExamResponse getExam(@PathVariable UUID id) {
    return examService.getExam(id);
  }

  @PutMapping("/{id}")
  public ExamResponse updateExam(@PathVariable UUID id, @RequestBody ExamRequest request) {
    return examService.updateExam(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExam(@PathVariable UUID id) {
    examService.deleteExam(id);
  }

  @GetMapping("/{id}/grades")
  public List<Grade> getGradesByExamId(@PathVariable UUID id) {
    return examService.getGradesByExamId(id);
  }

  @PostMapping("/{id}/grades")
  public GradeResponse addGradeToExam(@PathVariable UUID id, @RequestBody GradeRequest grade) {
    return examService.addGradeToExam(id, grade);
  }

  @GetMapping("/{id}/grades-students")
  public List<Grade> getGradesByExam(@PathVariable UUID id) {
    return gradeService.getGradesByExam(id);
  }

  @GetMapping("/{id}/grades-students/{idStudent}")
  public GradeResponse getStudentGradeForExam(@PathVariable UUID id, @PathVariable UUID idStudent) {
    return gradeService.getStudentGradeForExam(id, idStudent);
  }

  @GetMapping("/{id}/grades-students/{idStudent}/history")
  public GradeHistoryResponse getStudentGradeHistoryForExam(
      @PathVariable UUID id, @PathVariable UUID idStudent) {
    return gradeService.getStudentGradeHistoryForExam(id, idStudent);
  }

  @PutMapping("/{id}/grades-students")
  public GradeResponse upsertGradeForExam(
      @PathVariable UUID id, @RequestBody GradeRequest gradeRequest) {
    return gradeService.upsertGradeForExam(id, gradeRequest);
  }
}
