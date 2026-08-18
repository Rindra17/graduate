package hei.school.graduate.endpoint.rest.controller;

import hei.school.graduate.endpoint.rest.controller.dto.GroupsRequest;
import hei.school.graduate.model.Groupe;
import hei.school.graduate.service.GroupsService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/groups")
public class GroupsController {

  private final GroupsService groupsService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Groupe addGroup(@RequestBody GroupsRequest groupsRequest) {
    return groupsService.addGroup(groupsRequest);
  }

  @PutMapping("/{id}")
  public Groupe updateGroup(@PathVariable UUID id, @RequestBody GroupsRequest groupsRequest) {
    return groupsService.updateGroup(id, groupsRequest);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGroup(@PathVariable UUID id) {
    groupsService.deleteGroup(id);
  }
}
