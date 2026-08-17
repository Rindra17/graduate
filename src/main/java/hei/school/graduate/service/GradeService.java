package hei.school.graduate.service;

import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.GradeMapper;
import hei.school.graduate.model.Grade;
import hei.school.graduate.repository.ExamRepository;
import hei.school.graduate.repository.GradeRepository;
import hei.school.graduate.repository.model.JExam;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final ExamRepository examRepository;
  private final GradeMapper gradeMapper;

  @Transactional(readOnly = true)
  public List<Grade> getGradesByExam(UUID examId) {
    JExam exam = findExamOrThrow(examId);
    return gradeRepository.findAllByExam_Id(examId).stream().map(gradeMapper::toDomain).toList();
  }

  private JExam findExamOrThrow(UUID examId) {
    return examRepository
        .findById(examId)
        .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
  }
}
