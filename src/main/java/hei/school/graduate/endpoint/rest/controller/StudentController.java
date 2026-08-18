package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.GradeReportRequest;
import hei.school.graduate.endpoint.rest.controller.dto.GradeReportResponse;
import hei.school.graduate.endpoint.rest.controller.dto.StudentGradesResponse;
import hei.school.graduate.endpoint.rest.controller.dto.StudentPage;
import hei.school.graduate.endpoint.rest.controller.dto.StudentResponse;
import hei.school.graduate.service.StudentService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/students")
public class StudentController {

  private final StudentService service;

  @GetMapping
  public StudentPage getStudents(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return service.getStudents(page, size);
  }

  @GetMapping("/{studentId}")
  public StudentResponse getStudent(@PathVariable UUID studentId) {
    return service.getStudent(studentId);
  }

  @GetMapping("/{studentId}/grades")
  public StudentGradesResponse getStudentGrades(
      @PathVariable UUID studentId, @RequestParam(required = false) String academicYear) {
    return service.getStudentGrades(studentId, academicYear);
  }

  @PostMapping("/{studentId}/grade-report")
  @ResponseStatus(HttpStatus.CREATED)
  public GradeReportResponse requestGradeReport(
      @PathVariable UUID studentId, @RequestBody GradeReportRequest request) {
    return service.gradeReportRequest(studentId, request.academicYear());
  }
}
