package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.CourseTeacherRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CourseRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.CourseMapper;
import hei.school.graduate.mapper.ExamMapper;
import hei.school.graduate.mapper.GroupeMapper;
import hei.school.graduate.mapper.TeacherMapper;
import hei.school.graduate.model.Course;
import hei.school.graduate.model.Exam;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.model.Teacher;
import hei.school.graduate.repository.CourseGroupRepository;
import hei.school.graduate.repository.CourseRepository;
import hei.school.graduate.repository.CourseTeacherRepository;
import hei.school.graduate.repository.ExamRepository;
import hei.school.graduate.repository.TeacherRepository;
import hei.school.graduate.repository.model.JBranch;
import hei.school.graduate.repository.model.JCourse;
import hei.school.graduate.repository.model.JCourseGroup;
import hei.school.graduate.repository.model.JCourseTeacher;
import hei.school.graduate.repository.model.JExam;
import hei.school.graduate.repository.model.JSemester;
import hei.school.graduate.repository.model.JTeacher;
import hei.school.graduate.service.validator.CourseValidator;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class CourseService {

  private final ExamMapper examMapper;
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final ExamRepository examRepository;
  private final CourseTeacherRepository courseTeacherRepository;
  private final CourseGroupRepository courseGroupRepository;
  private final CourseValidator validator;
  private final CourseMapper courseMapper;
  private final EntityManager entityManager;
  private final TeacherMapper teacherMapper;
  private final GroupeMapper groupeMapper;

  public List<Course> getAllCourses() {
    return courseRepository.findAll().stream().map(courseMapper::toDomain).toList();
  }

  public Course createCourse(CourseRequest newCourse) {
    validator.validate(newCourse);

    JCourse course =
        JCourse.builder()
            .semester(entityManager.getReference(JSemester.class, newCourse.getSemesterId()))
            .branch(entityManager.getReference(JBranch.class, newCourse.getBranchId()))
            .code(newCourse.getCode())
            .title(newCourse.getTitle())
            .credits(newCourse.getCredits())
            .build();

    JCourse saved = courseRepository.save(course);
    return courseMapper.toDomain(saved);
  }

  public Course getCourseById(UUID id) {
    JCourse course = findCourseOrThrow(id);
    return courseMapper.toDomain(course);
  }

  public Course updateCourseById(UUID id, CourseRequest newCourse) {
    validator.validate(newCourse);

    JCourse course = findCourseOrThrow(id);
    course.setSemester(entityManager.getReference(JSemester.class, newCourse.getSemesterId()));
    course.setBranch(entityManager.getReference(JBranch.class, newCourse.getBranchId()));
    course.setCode(newCourse.getCode());
    course.setTitle(newCourse.getTitle());
    course.setCredits(newCourse.getCredits());

    JCourse updated = courseRepository.save(course);
    return courseMapper.toDomain(updated);
  }

  public void deleteCourseById(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw notFound("Course", id);
    }
    courseRepository.deleteById(id);
  }

  public List<Teacher> getTeachersByCourseId(UUID id) {
    findCourseOrThrow(id);
    return courseTeacherRepository.findAllByCourse_Id(id).stream()
        .map(JCourseTeacher::getTeacher)
        .map(teacherMapper::toDomain)
        .toList();
  }

  public void addTeacherToCourse(UUID id, CourseTeacherRequest courseTeacherRequest) {
    JCourse course = findCourseOrThrow(id);

    JTeacher teacher =
        teacherRepository
            .findById(courseTeacherRequest.getTeacherId())
            .orElseThrow(() -> notFound("Teacher", courseTeacherRequest.getTeacherId()));

    boolean alreadyAssigned =
        courseTeacherRepository.findAllByCourse_Id(id).stream()
            .anyMatch(ct -> ct.getTeacher().getId().equals(teacher.getId()));
    if (alreadyAssigned) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Teacher " + teacher.getId() + " is already assigned to course " + id);
    }

    JCourseTeacher courseTeacher = JCourseTeacher.builder().course(course).teacher(teacher).build();
    courseTeacherRepository.save(courseTeacher);
  }

  public void removeTeacherFromCourse(UUID id, UUID teacherId) {
    findCourseOrThrow(id);
    JCourseTeacher courseTeacher =
        courseTeacherRepository.findAllByCourse_Id(id).stream()
            .filter(ct -> ct.getTeacher().getId().equals(teacherId))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher " + teacherId + " is not assigned to course " + id));

    courseTeacherRepository.delete(courseTeacher);
  }

  public List<Groupe> getGroupsByCourseId(UUID id) {
    findCourseOrThrow(id);
    return courseGroupRepository.findAllByCourse_Id(id).stream()
        .map(JCourseGroup::getGroupe)
        .map(groupeMapper::toDomain)
        .toList();
  }

  public List<Exam> getExamsByCourseId(UUID id) {
    findCourseOrThrow(id);
    return examRepository.findAllByCourse_Id(id).stream().map(examMapper::toDomain).toList();
  }

  public void createExam(UUID id, ExamRequest examRequest) {
    JCourse course = findCourseOrThrow(id);

    List<JExam> existingExams = examRepository.findAllByCourse_Id(id);
    validator.validateExamWeight(existingExams, examRequest.getWeight());

    JExam exam =
        JExam.builder()
            .title(examRequest.getTitle())
            .weight(examRequest.getWeight())
            .examDate(
                examRequest.getExamDate() != null ? examRequest.getExamDate() : LocalDate.now())
            .build();

    examRepository.save(exam);
  }

  private JCourse findCourseOrThrow(UUID id) {
    return courseRepository.findById(id).orElseThrow(() -> notFound("course", id));
  }

  private NotFoundException notFound(String resource, UUID id) {
    return new NotFoundException(resource + " " + id + " not found");
  }
}
