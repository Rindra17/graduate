package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.CourseRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CourseTeacherRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.model.Course;
import hei.school.graduate.model.Exam;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.model.Teacher;
import hei.school.graduate.service.CourseService;
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
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {
  private final CourseService courseService;

  @GetMapping
  public List<Course> getAllCourses() {
    return courseService.getAllCourses();
  }

  @PostMapping
  public Course createCourse(@RequestBody CourseRequest newCourse) {
    return courseService.createCourse(newCourse);
  }

  @GetMapping("/{id}")
  public Course getCourseById(@PathVariable UUID id) {
    return courseService.getCourseById(id);
  }

  @PutMapping("/{id}")
  public Course updateCourseById(@PathVariable UUID id, @RequestBody CourseRequest newCourse) {
    return courseService.updateCourseById(id, newCourse);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourseById(@PathVariable UUID id) {
    courseService.deleteCourseById(id);
  }

  @GetMapping("/{id}/teachers")
  public List<Teacher> getTeachersByCourseId(@PathVariable UUID id) {
    return courseService.getTeachersByCourseId(id);
  }

  @PostMapping("/{id}/teachers")
  public void addTeacherToCourse(
      @PathVariable UUID id, @RequestBody CourseTeacherRequest courseTeacherRequest) {
    courseService.addTeacherToCourse(id, courseTeacherRequest);
  }

  @DeleteMapping("/{id}/teachers/{teacherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeTeacherFromCourse(@PathVariable UUID id, @PathVariable UUID teacherId) {
    courseService.removeTeacherFromCourse(id, teacherId);
  }

  @GetMapping("/{id}/groups")
  public List<Groupe> getGroupsByCourseId(@PathVariable UUID id) {
    return courseService.getGroupsByCourseId(id);
  }

  @GetMapping("/{id}/exams")
  public List<Exam> getExamsByCourseId(@PathVariable UUID id) {
    return courseService.getExamsByCourseId(id);
  }

  @PostMapping("/{id}/exams")
  @ResponseStatus(HttpStatus.CREATED)
  public void createExam(@PathVariable UUID id, @RequestBody ExamRequest examRequest) {
    courseService.createExam(id, examRequest);
  }
}
