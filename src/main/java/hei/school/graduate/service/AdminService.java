package hei.school.graduate.service;

import hei.school.graduate.endpoint.rest.controller.dto.AdminPage;
import hei.school.graduate.endpoint.rest.controller.dto.AdminResponse;
import hei.school.graduate.repository.AdminRepository;
import hei.school.graduate.repository.model.JAdmin;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminService {

  private final AdminRepository repository;

  public AdminPage getAdmins(int page, int size) {
    var result = repository.findAll(PageRequest.of(page, size));

    var admins = result.getContent().stream().map(this::toResponse).toList();

    return new AdminPage(
        admins,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  private AdminResponse toResponse(JAdmin entity) {
    return new AdminResponse(
        entity.getId(),
        entity.getUser().getFirstName(),
        entity.getUser().getLastName(),
        entity.getUser().getEmail(),
        entity.getReference());
  }
}
