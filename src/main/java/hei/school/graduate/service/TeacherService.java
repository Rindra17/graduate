package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.TeacherPage;
import hei.school.graduate.endpoint.rest.controller.dto.TeacherResponse;
import hei.school.graduate.repository.TeacherRepository;
import hei.school.graduate.repository.model.JTeacher;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TeacherService {

  private final TeacherRepository repository;

  public TeacherPage getTeachers(int page, int size) {
    var result = repository.findAll(PageRequest.of(page, size));

    var teachers = result.getContent().stream().map(this::toResponse).toList();

    return new TeacherPage(
        teachers,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  private TeacherResponse toResponse(JTeacher entity) {
    return new TeacherResponse(
        entity.getId(),
        entity.getUser().getFirstName(),
        entity.getUser().getLastName(),
        entity.getUser().getEmail(),
        entity.getReference());
  }
}
