package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.StudentPage;
import hei.school.graduate.endpoint.rest.controller.dto.StudentResponse;
import hei.school.graduate.endpoint.rest.controller.dto.TransferRequest;
import hei.school.graduate.endpoint.rest.controller.dto.TransferResponse;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.service.StudentGroupService;
import hei.school.graduate.service.StudentService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/students")
public class StudentController {

  private final StudentService service;
  private final StudentGroupService studentGroupService;

  @GetMapping
  public StudentPage getStudents(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return service.getStudents(page, size);
  }

  @GetMapping("/{studentId}")
  public StudentResponse getStudent(@PathVariable UUID studentId) {
    return service.getStudent(studentId);
  }

  @GetMapping("/{studentId}/group")
  public Groupe getStudentGroup(@PathVariable UUID studentId) {
    return studentGroupService.getStudentGroup(studentId);
  }

  @PostMapping("/{studentId}/transfer")
  public TransferResponse transferStudent(
      @PathVariable UUID studentId, @RequestBody TransferRequest request) {
    return studentGroupService.transferStudent(studentId, request);
  }
}
