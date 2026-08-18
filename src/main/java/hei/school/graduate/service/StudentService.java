package hei.school.graduate.service;

import static java.io.File.createTempFile;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import hei.school.graduate.endpoint.event.EventProducer;
import hei.school.graduate.endpoint.event.model.SendEmailRequested;
import hei.school.graduate.endpoint.rest.controller.dto.CourseGradesResponse;
import hei.school.graduate.endpoint.rest.controller.dto.GradeReportResponse;
import hei.school.graduate.endpoint.rest.controller.dto.StudentGradesResponse;
import hei.school.graduate.endpoint.rest.controller.dto.StudentPage;
import hei.school.graduate.endpoint.rest.controller.dto.StudentResponse;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.file.bucket.BucketComponent;
import hei.school.graduate.model.ReportTemplate;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.model.JCourse;
import hei.school.graduate.repository.model.JExam;
import hei.school.graduate.repository.model.JGrade;
import hei.school.graduate.repository.model.JStudent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentService {

  private final StudentRepository repository;
  private final GradeRepository gradeRepository;
  private final EventProducer<SendEmailRequested> eventProducer;
  private final BucketComponent bucketComponent;

  public StudentPage getStudents(int page, int size) {
    var result = repository.findAll(PageRequest.of(page, size));

    var students = result.getContent().stream().map(this::toResponse).toList();

    return new StudentPage(
        students,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  public StudentResponse getStudent(UUID studentId) {
    var entity =
        repository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found"));
    return toResponse(entity);
  }

  private static final BigDecimal CREDITS_PER_YEAR = BigDecimal.valueOf(60);

  public StudentGradesResponse getStudentGrades(UUID studentId, String academicYear) {
    if (!repository.existsById(studentId)) {
      throw new NotFoundException("Student not found");
    }

    String year =
        (academicYear == null || academicYear.isBlank()) ? currentAcademicYear() : academicYear;

    Map<UUID, JCourse> courses = new LinkedHashMap<>();
    Map<UUID, BigDecimal> noteByCourse = new LinkedHashMap<>();
    for (JGrade grade : gradeRepository.findAllByStudent_Id(studentId)) {
      JExam exam = grade.getExam();
      JCourse course = exam.getCourse();
      if (course.getSemester() == null || !year.equals(course.getSemester().getAcademicYear())) {
        continue;
      }
      courses.put(course.getId(), course);
      if (exam.getWeight() == null || grade.getScore() == null) {
        continue;
      }
      noteByCourse.merge(
          course.getId(), exam.getWeight().multiply(grade.getScore()), BigDecimal::add);
    }

    List<CourseGradesResponse> result = new ArrayList<>();
    for (JCourse course : courses.values()) {
      BigDecimal note = noteByCourse.get(course.getId());
      result.add(
          new CourseGradesResponse(
              course.getId(),
              course.getCode(),
              course.getTitle(),
              course.getCredits(),
              note == null ? null : note.setScale(2, RoundingMode.HALF_UP)));
    }

    result.sort(Comparator.comparing(CourseGradesResponse::courseCode));
    return new StudentGradesResponse(result, yearAverage(result), creditEarned(result));
  }

  public GradeReportResponse gradeReportRequest(UUID studentId, String academicYear) {
    var student =
        repository
            .findById(studentId)
            .orElseThrow(
                () -> new NotFoundException("Student with id: " + studentId + " not found"));

    var grades = getStudentGrades(studentId, academicYear);

    var reportTemplate =
        new ReportTemplate(student.getUser(), student.getReference(), academicYear, grades)
            .toString();

    var reportLink = createReport(student.getReference(), academicYear, reportTemplate);

    var event =
        SendEmailRequested.builder()
            .to(student.getUser().getEmail())
            .firstName(student.getUser().getFirstName())
            .lastName(student.getUser().getLastName())
            .reference(student.getReference())
            .academicYear(academicYear)
            .reportLink(reportLink)
            .build();

    eventProducer.accept(List.of(event));

    return new GradeReportResponse(
        "Email sent to " + student.getUser().getEmail(), studentId, academicYear);
  }

  @SneakyThrows
  public String createReport(String reference, String year, String gradeReportContent) {
    var fileSuffix = ".pdf";
    var filePrefix = "Grade-Report-" + reference + "-" + year;
    var bucketKey = filePrefix + fileSuffix;
    var fileToUpload = createTempFile(filePrefix, fileSuffix);

    byte[] pdfBytes = generatePdf(gradeReportContent);
    writeByteInfoFile(pdfBytes, fileToUpload);

    bucketComponent.upload(fileToUpload, bucketKey);
    return bucketComponent.presign(bucketKey, Duration.ofMinutes(30)).toString();
  }

  private void writeByteInfoFile(byte[] content, File file) throws IOException {
    try (var fos = new FileOutputStream(file)) {
      fos.write(content);
    }
  }

  private byte[] generatePdf(String htmlContent) {
    try (var outputStream = new ByteArrayOutputStream()) {
      var builder = new PdfRendererBuilder();
      builder.useFastMode().withHtmlContent(htmlContent, "").toStream(outputStream);
      builder.run();
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("failed to generate PDF", e);
    }
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

  private static BigDecimal yearAverage(List<CourseGradesResponse> courses) {
    BigDecimal weightedSum = BigDecimal.ZERO;
    boolean hasNote = false;
    for (CourseGradesResponse course : courses) {
      if (course.average() == null || course.credits() == null) {
        continue;
      }
      hasNote = true;
      weightedSum =
          weightedSum.add(course.average().multiply(BigDecimal.valueOf(course.credits())));
    }
    if (!hasNote) {
      return null;
    }
    return weightedSum.divide(CREDITS_PER_YEAR, 2, RoundingMode.HALF_UP);
  }

  private static String currentAcademicYear() {
    LocalDate today = LocalDate.now();
    int start = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
    return start + "-" + (start + 1);
  }

  private StudentResponse toResponse(JStudent entity) {
    return new StudentResponse(
        entity.getId(),
        entity.getUser().getFirstName(),
        entity.getUser().getLastName(),
        entity.getUser().getEmail(),
        entity.getReference(),
        entity.getStatus());
  }
}
