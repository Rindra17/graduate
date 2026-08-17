package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.model.Grade;
import hei.school.graduate.service.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exams")
@AllArgsConstructor
public class ExamController {

  private final GradeService gradeService;

  @GetMapping("/{id}/grades-students")
  public List<Grade> getGradesByExam(@PathVariable UUID id) {
    return gradeService.getGradesByExam(id);
  }
}
