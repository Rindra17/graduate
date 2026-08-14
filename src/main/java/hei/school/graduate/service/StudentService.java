package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.StudentPage;
import hei.school.graduate.endpoint.rest.controller.dto.StudentResponse;
import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.repository.StudentRepository;
import hei.school.graduate.repository.model.JStudent;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentService {

  private final StudentRepository repository;

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
