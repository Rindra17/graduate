package hei.school.graduate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.model.JCourse;
import hei.school.graduate.repository.model.JExam;
import hei.school.graduate.repository.model.JGrade;
import hei.school.graduate.repository.model.JSemester;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  private static final String ACADEMIC_YEAR = "2024-2025";

  @Mock StudentRepository repository;
  @Mock GradeRepository gradeRepository;
  @InjectMocks StudentService service;

  private UUID studentId;
  private Map<String, JCourse> courses;
  private Map<String, JExam> exams;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    var semester = JSemester.builder().academicYear(ACADEMIC_YEAR).build();
    courses = new HashMap<>();
    exams = new HashMap<>();
    register(semester, "PROG1", "Algorithmics & Programming", 6);
    register(semester, "PROG2", "Advanced Programming", 10);
    register(semester, "DONNEES1", "Databases", 4);
    register(semester, "WEB1", "Web Development", 6);
    register(semester, "SYS1", "Operating Systems", 6);
    register(semester, "LV1", "English", 4);
    register(semester, "SYS2", "Networks", 8);
    register(semester, "WEB2", "Web Frameworks", 8);
    register(semester, "THEORIE1", "Theory", 4);
    register(semester, "MGT1", "Management", 4);
  }

  private static String currentAcademicYear() {
    LocalDate today = LocalDate.now();
    int start = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
    return start + "-" + (start + 1);
  }

  private void register(JSemester semester, String code, String title, int credits) {
    var c =
        JCourse.builder()
            .id(UUID.randomUUID())
            .semester(semester)
            .code(code)
            .title(title)
            .credits(credits)
            .build();
    JExam exam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(c)
            .title("Exam " + code)
            .weight(BigDecimal.ONE)
            .examDate(LocalDate.of(2024, 2, 1))
            .build();
    courses.put(code, c);
    exams.put(code, exam);
  }

  private static JGrade grade(JExam exam, String score) {
    return JGrade.builder().exam(exam).score(new BigDecimal(score)).build();
  }

  private static JExam exam(JCourse course, String title, String weight, LocalDate date) {
    return JExam.builder()
        .id(UUID.randomUUID())
        .course(course)
        .title(title)
        .weight(new BigDecimal(weight))
        .examDate(date)
        .build();
  }

  @Test
  void getStudentGrades_withOneToThreeExamsPerCourse_returnsYearResult() {
    var prog1 = courses.get("PROG1");
    var prog2 = courses.get("PROG2");
    var donnees1 = courses.get("DONNEES1");
    var web1 = courses.get("WEB1");
    var sys1 = courses.get("SYS1");
    var lv1 = courses.get("LV1");
    var sys2 = courses.get("SYS2");
    var web2 = courses.get("WEB2");
    var theorie1 = courses.get("THEORIE1");
    var mgt1 = courses.get("MGT1");
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(
                grade(exam(prog1, "E1", "0.3", LocalDate.of(2024, 1, 10)), "18.00"),
                grade(exam(prog1, "E2", "0.3", LocalDate.of(2024, 4, 10)), "17.00"),
                grade(exam(prog1, "E3", "0.4", LocalDate.of(2024, 6, 10)), "19.00"),
                grade(exam(prog2, "E1", "0.5", LocalDate.of(2024, 2, 1)), "12.00"),
                grade(exam(prog2, "E2", "0.5", LocalDate.of(2024, 5, 1)), "13.00"),
                grade(exam(donnees1, "E1", "0.6", LocalDate.of(2024, 2, 1)), "15.50"),
                grade(exam(donnees1, "E2", "0.4", LocalDate.of(2024, 5, 1)), "14.00"),
                grade(exam(web1, "E1", "1.0", LocalDate.of(2024, 3, 1)), "20.00"),
                grade(exam(sys1, "E1", "0.5", LocalDate.of(2024, 2, 1)), "14.00"),
                grade(exam(sys1, "E2", "0.5", LocalDate.of(2024, 5, 1)), "10.00"),
                grade(exam(lv1, "E1", "0.3", LocalDate.of(2024, 1, 15)), "12.00"),
                grade(exam(lv1, "E2", "0.3", LocalDate.of(2024, 3, 15)), "13.50"),
                grade(exam(lv1, "E3", "0.4", LocalDate.of(2024, 6, 15)), "14.00"),
                grade(exam(sys2, "E1", "0.4", LocalDate.of(2024, 2, 1)), "16.00"),
                grade(exam(sys2, "E2", "0.6", LocalDate.of(2024, 5, 1)), "15.00"),
                grade(exam(web2, "E1", "1.0", LocalDate.of(2024, 3, 1)), "18.00"),
                grade(exam(theorie1, "E1", "0.5", LocalDate.of(2024, 2, 1)), "14.00"),
                grade(exam(theorie1, "E2", "0.5", LocalDate.of(2024, 5, 1)), "16.00"),
                grade(exam(mgt1, "E1", "0.4", LocalDate.of(2024, 2, 1)), "17.00"),
                grade(exam(mgt1, "E2", "0.3", LocalDate.of(2024, 4, 1)), "19.00"),
                grade(exam(mgt1, "E3", "0.3", LocalDate.of(2024, 6, 1)), "18.00")));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(10, result.courses().size());
    var byCode = result.courses().stream().collect(Collectors.toMap(c -> c.courseCode(), c -> c));
    assertEquals(new BigDecimal("18.10"), byCode.get("PROG1").average());
    assertEquals(new BigDecimal("12.50"), byCode.get("PROG2").average());
    assertEquals(new BigDecimal("14.90"), byCode.get("DONNEES1").average());
    assertEquals(new BigDecimal("20.00"), byCode.get("WEB1").average());
    assertEquals(new BigDecimal("12.00"), byCode.get("SYS1").average());
    assertEquals(new BigDecimal("13.25"), byCode.get("LV1").average());
    assertEquals(new BigDecimal("15.40"), byCode.get("SYS2").average());
    assertEquals(new BigDecimal("18.00"), byCode.get("WEB2").average());
    assertEquals(new BigDecimal("15.00"), byCode.get("THEORIE1").average());
    assertEquals(new BigDecimal("17.90"), byCode.get("MGT1").average());
    // (18.10*6 + 12.50*10 + 14.90*4 + 20*6 + 12*6 + 13.25*4 + 15.40*8 + 18*8 +
    //  15*4 + 17.90*4) / 60 = 937 / 60 = 15.6166... -> rounded to 15.62
    assertEquals(new BigDecimal("15.62"), result.yearAverage());
    assertEquals(60, result.creditEarned());
  }

  @Test
  void getStudentGrades_studentNotFound_throwsNotFoundException() {
    when(repository.existsById(studentId)).thenReturn(false);

    var exception =
        assertThrows(
            NotFoundException.class, () -> service.getStudentGrades(studentId, ACADEMIC_YEAR));

    assertEquals("Student not found", exception.getMessage());
  }

  @Test
  void getStudentGrades_withoutGrades_returnsEmptyListAndNullYearAverage() {
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId)).thenReturn(List.of());

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertTrue(result.courses().isEmpty());
    assertNull(result.yearAverage());
    assertEquals(0, result.creditEarned());
  }

  @Test
  void getStudentGrades_withGrades_groupsByCourseAndSumsScoreTimesWeight() {
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(
                grade(exams.get("PROG1"), "18.24"),
                grade(exams.get("PROG2"), "12.35"),
                grade(exams.get("DONNEES1"), "16.69"),
                grade(exams.get("WEB1"), "20.00"),
                grade(exams.get("SYS1"), "14.00"),
                grade(exams.get("LV1"), "13.25"),
                grade(exams.get("SYS2"), "14.84"),
                grade(exams.get("WEB2"), "16.43"),
                grade(exams.get("THEORIE1"), "15.00"),
                grade(exams.get("MGT1"), "18.50")));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(10, result.courses().size());
    assertEquals(
        List.of(
            "DONNEES1",
            "LV1",
            "MGT1",
            "PROG1",
            "PROG2",
            "SYS1",
            "SYS2",
            "THEORIE1",
            "WEB1",
            "WEB2"),
        result.courses().stream().map(c -> c.courseCode()).toList());

    var byCode = result.courses().stream().collect(Collectors.toMap(c -> c.courseCode(), c -> c));
    assertEquals(10, byCode.size());
    for (var course : result.courses()) {
      assertNotNull(course.average(), course.courseCode() + " has no grade");
      assertTrue(
          course.average().compareTo(new BigDecimal("20.00")) <= 0,
          course.courseCode() + " exceeds max grade 20");
    }
    assertEquals(new BigDecimal("18.24"), byCode.get("PROG1").average());
    assertEquals(6, byCode.get("PROG1").credits());
    assertEquals(new BigDecimal("12.35"), byCode.get("PROG2").average());
    assertEquals(10, byCode.get("PROG2").credits());
    assertEquals(new BigDecimal("16.69"), byCode.get("DONNEES1").average());
    assertEquals(new BigDecimal("20.00"), byCode.get("WEB1").average());
    assertEquals(new BigDecimal("14.00"), byCode.get("SYS1").average());
    assertEquals(new BigDecimal("13.25"), byCode.get("LV1").average());
    assertEquals(new BigDecimal("14.84"), byCode.get("SYS2").average());
    assertEquals(new BigDecimal("16.43"), byCode.get("WEB2").average());
    assertEquals(new BigDecimal("15.00"), byCode.get("THEORIE1").average());
    assertEquals(new BigDecimal("18.50"), byCode.get("MGT1").average());
    // (18.24*6 + 12.35*10 + 16.69*4 + 20*6 + 14*6 + 13.25*4 + 14.84*8 + 16.43*8 +
    //  15*4 + 18.5*4) / 60 = 940.86 / 60 = 15.681 -> rounded to 15.68
    assertEquals(new BigDecimal("15.68"), result.yearAverage());
    assertEquals(60, result.creditEarned());
  }

  @Test
  void getStudentGrades_weightsNotSummingToOne_sumsInsteadOfAveraging() {
    JExam lightExam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(courses.get("PROG1"))
            .title("Light Exam")
            .weight(new BigDecimal("0.3"))
            .examDate(LocalDate.of(2024, 2, 1))
            .build();
    JExam heavyExam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(courses.get("PROG1"))
            .title("Heavy Exam")
            .weight(new BigDecimal("0.6"))
            .examDate(LocalDate.of(2024, 6, 1))
            .build();
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(
                JGrade.builder().exam(lightExam).score(new BigDecimal("10")).build(),
                JGrade.builder().exam(heavyExam).score(new BigDecimal("20")).build()));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(1, result.courses().size());
    // 0.3 * 10 + 0.6 * 20 = 3 + 12 = 15
    assertEquals(new BigDecimal("15.00"), result.courses().get(0).average());
    // (15 * 6) / 60 = 1.5
    assertEquals(new BigDecimal("1.50"), result.yearAverage());
    assertEquals(6, result.creditEarned());
  }

  @Test
  void getStudentGrades_withGradesFromOtherYear_filtersByAcademicYear() {
    var otherSemester = JSemester.builder().academicYear("2023-2024").build();
    var otherCourse =
        JCourse.builder()
            .id(UUID.randomUUID())
            .semester(otherSemester)
            .code("OLD1")
            .title("Old Course")
            .credits(3)
            .build();
    JExam otherExam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(otherCourse)
            .title("Old Exam")
            .weight(BigDecimal.ONE)
            .examDate(LocalDate.of(2023, 3, 1))
            .build();
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(
                JGrade.builder().exam(exams.get("PROG1")).score(new BigDecimal("4.00")).build(),
                JGrade.builder().exam(otherExam).score(new BigDecimal("20")).build()));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(1, result.courses().size());
    assertEquals(courses.get("PROG1").getId(), result.courses().get(0).courseId());
    // (4 * 6) / 60 = 0.4
    assertEquals(new BigDecimal("0.40"), result.yearAverage());
    assertEquals(0, result.creditEarned());
  }

  @Test
  void getStudentGrades_withoutAcademicYear_defaultsToCurrentYear() {
    var semester = JSemester.builder().academicYear(currentAcademicYear()).build();
    var currentCourse =
        JCourse.builder()
            .id(UUID.randomUUID())
            .semester(semester)
            .code("CUR1")
            .title("Current Course")
            .credits(2)
            .build();
    JExam currentExam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(currentCourse)
            .title("Current Exam")
            .weight(BigDecimal.ONE)
            .examDate(LocalDate.now())
            .build();
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(JGrade.builder().exam(currentExam).score(new BigDecimal("15")).build()));

    var result = service.getStudentGrades(studentId, null);

    assertEquals(1, result.courses().size());
    assertEquals(currentCourse.getId(), result.courses().get(0).courseId());
    // (15 * 2) / 60 = 0.5
    assertEquals(new BigDecimal("0.50"), result.yearAverage());
    assertEquals(2, result.creditEarned());
  }

  @Test
  void getStudentGrades_withBlankAcademicYear_defaultsToCurrentYear() {
    var semester = JSemester.builder().academicYear(currentAcademicYear()).build();
    var currentCourse =
        JCourse.builder()
            .id(UUID.randomUUID())
            .semester(semester)
            .code("CUR2")
            .title("Current Course")
            .credits(2)
            .build();
    JExam currentExam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(currentCourse)
            .title("Current Exam")
            .weight(BigDecimal.ONE)
            .examDate(LocalDate.now())
            .build();
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(JGrade.builder().exam(currentExam).score(new BigDecimal("15")).build()));

    var result = service.getStudentGrades(studentId, "   ");

    assertEquals(1, result.courses().size());
    assertEquals(currentCourse.getId(), result.courses().get(0).courseId());
    assertEquals(new BigDecimal("0.50"), result.yearAverage());
    assertEquals(2, result.creditEarned());
  }

  @Test
  void getStudentGrades_courseWithNullScore_returnsNullAverage() {
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(List.of(JGrade.builder().exam(exams.get("PROG1")).score(null).build()));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(1, result.courses().size());
    assertNull(result.courses().get(0).average());
    assertNull(result.yearAverage());
    assertEquals(0, result.creditEarned());
  }

  @Test
  void getStudentGrades_ordersCoursesByCode() {
    var courseB =
        JCourse.builder()
            .id(UUID.randomUUID())
            .semester(courses.get("PROG1").getSemester())
            .code("PROG2")
            .title("Other Course")
            .credits(4)
            .build();
    JExam examB =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(courseB)
            .title("Exam B")
            .weight(BigDecimal.ONE)
            .examDate(LocalDate.of(2024, 3, 1))
            .build();
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(
                JGrade.builder().exam(examB).score(new BigDecimal("15")).build(),
                JGrade.builder().exam(exams.get("PROG1")).score(new BigDecimal("4.00")).build()));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(2, result.courses().size());
    assertEquals("PROG1", result.courses().get(0).courseCode());
    assertEquals("PROG2", result.courses().get(1).courseCode());
    // (4 * 6 + 15 * 4) / 60 = (24 + 60) / 60 = 1.4
    assertEquals(new BigDecimal("1.40"), result.yearAverage());
    assertEquals(4, result.creditEarned());
  }

  @Test
  void getStudentGrades_averageRoundedToTwoDecimals() {
    JExam half =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(courses.get("PROG1"))
            .title("Half")
            .weight(new BigDecimal("0.4"))
            .examDate(LocalDate.of(2024, 2, 1))
            .build();
    JExam rest =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(courses.get("PROG1"))
            .title("Rest")
            .weight(new BigDecimal("0.6"))
            .examDate(LocalDate.of(2024, 6, 1))
            .build();
    when(repository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findAllByStudent_Id(studentId))
        .thenReturn(
            List.of(
                JGrade.builder().exam(half).score(new BigDecimal("20.00")).build(),
                JGrade.builder().exam(rest).score(new BigDecimal("17.65")).build()));

    var result = service.getStudentGrades(studentId, ACADEMIC_YEAR);

    assertEquals(1, result.courses().size());
    // 0.4 * 20.00 + 0.6 * 17.65 = 8.0000 + 10.5900 = 18.5900 -> rounded to 18.59
    assertEquals(new BigDecimal("18.59"), result.courses().get(0).average());
  }
}
