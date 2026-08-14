package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.StudentPage;
import hei.school.graduate.endpoint.rest.controller.dto.StudentResponse;
import hei.school.graduate.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentService {

  private final StudentRepository repository;

  public StudentPage getStudents(int page, int size) {
    var result = repository.findAll(PageRequest.of(page, size));

    var students = result.getContent().stream()
        .map(
            entity -> new StudentResponse(
                entity.getId(),
                entity.getUser().getFirstName(),
                entity.getUser().getLastName(),
                entity.getUser().getEmail(),
                entity.getReference(),
                entity.getStatus()))
        .toList();

    return new StudentPage(
        students,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }
}
