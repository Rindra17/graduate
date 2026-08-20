package hei.school.graduate.endpoint.web;

import hei.school.graduate.service.CohortService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/web/cohorts")
public class CohortWebController {

  private final CohortService cohortService;

  @GetMapping
  public String listCohorts(Model model) {
    var cohorts = cohortService.listAll();
    model.addAttribute("cohorts", cohorts);
    return "cohorts";
  }

  @GetMapping("/{id}/graduates")
  public String graduates(@PathVariable UUID id, Model model) {
    var graduates = cohortService.getCohortGraduates(id);
    var cohort = cohortService.findById(id);
    model.addAttribute("cohort", cohort);
    model.addAttribute("graduates", graduates);
    model.addAttribute("cohortId", id.toString());
    return "graduates";
  }
}
