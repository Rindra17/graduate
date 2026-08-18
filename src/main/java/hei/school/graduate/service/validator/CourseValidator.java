package hei.school.graduate.service.validator;

import hei.school.graduate.endpoint.rest.controller.dto.CourseRequest;
import hei.school.graduate.exception.BadRequestException;
import hei.school.graduate.repository.model.JExam;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CourseValidator {

  private static final BigDecimal MAX_TOTAL_WEIGHT = BigDecimal.ONE;

  public static BigDecimal normalizeWeight(BigDecimal weight) {
    if (weight == null || weight.compareTo(BigDecimal.ONE) <= 0) {
      return weight;
    }
    return weight.divide(BigDecimal.valueOf(100));
  }

  public void validate(CourseRequest newCourse) {
    if (newCourse == null) {
      throw new BadRequestException("Course request must not be null");
    }
    if (newCourse.getSemesterId() == null) {
      throw new BadRequestException("semesterId is mandatory");
    }
    if (newCourse.getBranchId() == null) {
      throw new BadRequestException("branchId is mandatory");
    }
    if (newCourse.getCode() == null || newCourse.getCode().isBlank()) {
      throw new BadRequestException("code is mandatory");
    }
    if (newCourse.getTitle() == null || newCourse.getTitle().isBlank()) {
      throw new BadRequestException("title is mandatory");
    }
    if (newCourse.getCredits() != null && newCourse.getCredits() <= 0) {
      throw new BadRequestException("credits must be positive");
    }
  }

  public void validateExamWeight(List<JExam> existingExams, BigDecimal weight) {
    if (weight == null) {
      throw new BadRequestException("weight is mandatory");
    }
    if (weight.signum() <= 0) {
      throw new BadRequestException("weight must be positive");
    }

    BigDecimal existingTotal =
        existingExams.stream()
            .map(JExam::getWeight)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal newTotal = existingTotal.add(weight);

    if (newTotal.compareTo(MAX_TOTAL_WEIGHT) > 0) {
      throw new BadRequestException(
          "Total exam weight for this course would be "
              + newTotal
              + ", which exceeds the maximum of "
              + MAX_TOTAL_WEIGHT);
    }
  }
}
