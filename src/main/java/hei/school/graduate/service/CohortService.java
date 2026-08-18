package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.CohortRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CohortResultResponse;
import hei.school.graduate.endpoint.rest.controller.dto.CohortStudentResult;
import hei.school.graduate.endpoint.rest.controller.dto.GraduateStudentResponse;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.CohortMapper;
import hei.school.graduate.model.Cohort;
import hei.school.graduate.repository.CohortRepository;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.GroupRepository;
import hei.school.graduate.repository.StudentGroupHistoryRepository;
import hei.school.graduate.repository.model.JCohort;
import hei.school.graduate.repository.model.JGrade;
import hei.school.graduate.repository.model.JGroupe;
import hei.school.graduate.repository.model.JStudent;
import hei.school.graduate.repository.model.JStudentGroupHistory;
import hei.school.graduate.service.validator.CohortValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CohortService {

  private static final BigDecimal PASSING_THRESHOLD = BigDecimal.TEN;

  private final CohortRepository cohortRepository;
  private final CohortMapper cohortMapper;
  private final CohortValidator validator;
  private final GroupRepository groupRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final GradeRepository gradeRepository;

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
        studentResults.stream()
            .filter(
                s ->
                    s.getAverage() != null
                        && BigDecimal.valueOf(s.getAverage()).compareTo(PASSING_THRESHOLD) >= 0)
            .count();

    return CohortResultResponse.builder()
        .cohortId(cohort.getId().toString())
        .cohortName(cohort.getName())
        .totalStudents(students.size())
        .graduates((int) graduates)
        .failed(students.size() - (int) graduates)
        .students(studentResults)
        .build();
  }

  public List<GraduateStudentResponse> getCohortGraduates(UUID id) {
    findCohortOrThrow(id);

    List<JStudent> students = findStudentsByCohortId(id);

    return students.stream()
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
        .filter(
            s ->
                s.getAverage() != null
                    && BigDecimal.valueOf(s.getAverage()).compareTo(PASSING_THRESHOLD) >= 0)
        .toList();
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

  private JCohort findCohortOrThrow(UUID id) {
    return cohortRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("cohort " + id + " not found"));
  }
}
