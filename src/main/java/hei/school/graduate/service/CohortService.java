package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.CohortRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CohortResultResponse;
import hei.school.graduate.endpoint.rest.controller.dto.CohortStudentResult;
import hei.school.graduate.endpoint.rest.controller.dto.CourseGradesResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GraduateStudentResponse;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.CohortMapper;
import hei.school.graduate.model.Cohort;
import hei.school.graduate.repository.CohortRepository;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.GroupRepository;
import hei.school.graduate.repository.SemesterRepository;
import hei.school.graduate.repository.StudentGroupHistoryRepository;
import hei.school.graduate.repository.model.JCohort;
import hei.school.graduate.repository.model.JCourse;
import hei.school.graduate.repository.model.JExam;
import hei.school.graduate.repository.model.JGrade;
import hei.school.graduate.repository.model.JGroupe;
import hei.school.graduate.repository.model.JStudent;
import hei.school.graduate.repository.model.JStudentGroupHistory;
import hei.school.graduate.service.validator.CohortValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CohortService {

  private static final int CREDITS_PER_YEAR = 60;
  private static final BigDecimal PASSING_THRESHOLD = BigDecimal.TEN;

  private final CohortRepository cohortRepository;
  private final CohortMapper cohortMapper;
  private final CohortValidator validator;
  private final GroupRepository groupRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final GradeRepository gradeRepository;
  private final SemesterRepository semesterRepository;

  public List<Cohort> listAll() {
    return cohortRepository.findAll().stream().map(cohortMapper::toDomain).toList();
  }

  public Cohort findById(UUID id) {
    return cohortMapper.toDomain(findCohortOrThrow(id));
  }

  public Cohort createCohort(CohortRequest request) {
    validator.validate(request);

    JCohort cohort =
        JCohort.builder()
            .name(request.getName())
            .startYear(request.getStartYear())
            .endYear(request.getStartYear() + 3)
            .build();

    JCohort saved = cohortRepository.save(cohort);
    return cohortMapper.toDomain(saved);
  }

  public CohortResultResponse getCohortResults(UUID id) {
    JCohort cohort = findCohortOrThrow(id);
    List<String> academicYears = semesterRepository.findAcademicYearsByCohortId(id);

    List<JStudent> students = findStudentsByCohortId(id);

    List<CohortStudentResult> studentResults =
        students.stream()
            .map(
                student -> {
                  Double average = computeAverage(student);
                  return CohortStudentResult.builder()
                      .studentId(student.getId().toString())
                      .firstName(student.getUser().getFirstName())
                      .lastName(student.getUser().getLastName())
                      .email(student.getUser().getEmail())
                      .reference(student.getReference())
                      .status(student.getStatus())
                      .average(average)
                      .build();
                })
            .toList();

    long graduates =
        students.stream().filter(student -> hasEarnedDiploma(student, academicYears)).count();

    return CohortResultResponse.builder()
        .cohortId(cohort.getId().toString())
        .cohortName(cohort.getName())
        .totalStudents(students.size())
        .graduates((int) graduates)
        .failed(students.size() - (int) graduates)
        .students(studentResults)
        .build();
  }

  public List<GraduateStudentResponse> getCohortGraduates(UUID cohortId) {
    findCohortOrThrow(cohortId);

    List<String> academicYears = semesterRepository.findAcademicYearsByCohortId(cohortId);
    List<JStudent> students = findStudentsByCohortId(cohortId);

    return students.stream()
        .filter(student -> hasEarnedDiploma(student, academicYears))
        .map(
            student -> {
              Double average = computeAverage(student);
              return GraduateStudentResponse.builder()
                  .studentId(student.getId().toString())
                  .firstName(student.getUser().getFirstName())
                  .lastName(student.getUser().getLastName())
                  .email(student.getUser().getEmail())
                  .reference(student.getReference())
                  .average(average)
                  .build();
            })
        .sorted(Comparator.comparing(GraduateStudentResponse::getAverage).reversed())
        .toList();
  }

  private boolean hasEarnedDiploma(JStudent student, List<String> academicYears) {
    if (academicYears.isEmpty()) {
      return false;
    }
    for (String academicYear : academicYears) {
      if (computeYearCredits(student, academicYear) < CREDITS_PER_YEAR) {
        return false;
      }
    }
    return true;
  }

  private int computeYearCredits(JStudent student, String academicYear) {
    List<JGrade> grades = gradeRepository.findAllByStudent_Id(student.getId());

    Map<UUID, JCourse> courses = new LinkedHashMap<>();
    Map<UUID, BigDecimal> noteByCourse = new LinkedHashMap<>();

    for (JGrade grade : grades) {
      JExam exam = grade.getExam();
      JCourse course = exam.getCourse();
      if (course.getSemester() == null
          || !academicYear.equals(course.getSemester().getAcademicYear())) {
        continue;
      }
      courses.put(course.getId(), course);
      if (exam.getWeight() == null || grade.getScore() == null) {
        continue;
      }
      noteByCourse.merge(
          course.getId(), exam.getWeight().multiply(grade.getScore()), BigDecimal::add);
    }

    List<CourseGradesResponse> courseGrades = new ArrayList<>();
    for (JCourse course : courses.values()) {
      BigDecimal note = noteByCourse.get(course.getId());
      courseGrades.add(
          new CourseGradesResponse(
              course.getId(),
              course.getCode(),
              course.getTitle(),
              course.getCredits(),
              note == null ? null : note.setScale(2, RoundingMode.HALF_UP)));
    }

    return creditEarned(courseGrades);
  }

  private static int creditEarned(List<CourseGradesResponse> courses) {
    int total = 0;
    for (CourseGradesResponse course : courses) {
      if (course.average() != null
          && course.credits() != null
          && course.average().compareTo(BigDecimal.TEN) >= 0) {
        total += course.credits();
      }
    }
    return total;
  }

  private Double computeAverage(JStudent student) {
    List<JGrade> grades = gradeRepository.findAllByStudent_Id(student.getId());
    if (grades.isEmpty()) {
      return null;
    }

    BigDecimal totalWeightedScore = BigDecimal.ZERO;
    BigDecimal totalWeight = BigDecimal.ZERO;

    for (JGrade grade : grades) {
      if (grade.getScore() != null && grade.getExam().getWeight() != null) {
        totalWeightedScore =
            totalWeightedScore.add(grade.getScore().multiply(grade.getExam().getWeight()));
        totalWeight = totalWeight.add(grade.getExam().getWeight());
      }
    }

    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    return totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP).doubleValue();
  }

  private List<JStudent> findStudentsByCohortId(UUID cohortId) {
    List<JGroupe> groups = groupRepository.findAllByCohort_Id(cohortId);
    if (groups.isEmpty()) {
      return List.of();
    }

    List<UUID> groupIds = groups.stream().map(JGroupe::getId).toList();
    List<JStudentGroupHistory> histories =
        studentGroupHistoryRepository.findAllByGroup_IdIn(groupIds);

    Map<UUID, JStudent> uniqueStudents = new LinkedHashMap<>();
    for (JStudentGroupHistory history : histories) {
      uniqueStudents.putIfAbsent(history.getStudent().getId(), history.getStudent());
    }
    return new ArrayList<>(uniqueStudents.values());
  }

  private JCohort findCohortOrThrow(UUID id) {
    return cohortRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("cohort " + id + " not found"));
  }
}
