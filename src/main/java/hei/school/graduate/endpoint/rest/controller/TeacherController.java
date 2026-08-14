package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.TeacherPage;
import hei.school.graduate.endpoint.rest.controller.dto.TeacherResponse;
import hei.school.graduate.service.TeacherService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/teachers")
public class TeacherController {

  private final TeacherService service;

  @GetMapping
  public TeacherPage getTeachers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return service.getTeachers(page, size);
  }

  @GetMapping("/{teacherId}")
  public TeacherResponse getTeacher(@PathVariable UUID teacherId) {
    return service.getTeacher(teacherId);
  }
}
