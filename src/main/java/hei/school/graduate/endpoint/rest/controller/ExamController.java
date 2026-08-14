package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GradeRequest;
import hei.school.graduate.endpoint.rest.controller.dto.GradeResponse;
import hei.school.graduate.model.Grade;
import hei.school.graduate.service.ExamService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exams")
@AllArgsConstructor
public class ExamController {
  private final ExamService service;

  @GetMapping("/{id}")
  public ExamResponse getExam(@PathVariable UUID id) {
    return service.getExam(id);
  }

  @PutMapping("/{id}")
  public ExamResponse updateExam(@PathVariable UUID id, @RequestBody ExamRequest request) {
    return service.updateExam(id, request);
  }

  @DeleteMapping("/{id}")
  public void deleteExam(@PathVariable UUID id) {
    service.deleteExam(id);
  }

  @GetMapping("/{id}/grades")
  public List<Grade> getGradesByExamId(@PathVariable UUID id) {
    return service.getGradesByExamId(id);
  }

  @PostMapping("/{id}/grades")
  public GradeResponse addGradeToExam(@PathVariable UUID id, @RequestBody GradeRequest grade) {
    return service.addGradeToExam(id, grade);
  }
}
