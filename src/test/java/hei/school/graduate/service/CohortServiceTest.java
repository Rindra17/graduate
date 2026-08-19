package hei.school.graduate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import hei.school.graduate.exception.NotFoundException;
import hei.school.graduate.mapper.CohortMapper;
import hei.school.graduate.model.Cohort;
import hei.school.graduate.repository.CohortRepository;
import hei.school.graduate.repository.model.JCohort;
import hei.school.graduate.service.validator.CohortValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CohortServiceTest {

  @Mock CohortRepository cohortRepository;
  @Mock CohortMapper cohortMapper;
  @Mock CohortValidator validator;
  @InjectMocks CohortService service;

  @Test
  void listAll_returnsMappedCohorts() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    JCohort j1 = JCohort.builder().id(id1).name("Promo 2024").startYear(2021).endYear(2024).build();
    JCohort j2 = JCohort.builder().id(id2).name("Promo 2025").startYear(2022).endYear(2025).build();
    Cohort c1 = new Cohort(id1, "Promo 2024", 2021, 2024);
    Cohort c2 = new Cohort(id2, "Promo 2025", 2022, 2025);

    when(cohortRepository.findAll()).thenReturn(List.of(j1, j2));
    when(cohortMapper.toDomain(j1)).thenReturn(c1);
    when(cohortMapper.toDomain(j2)).thenReturn(c2);

    var result = service.listAll();

    assertEquals(2, result.size());
    assertEquals("Promo 2024", result.get(0).name());
    assertEquals("Promo 2025", result.get(1).name());
  }

  @Test
  void listAll_emptyRepository_returnsEmptyList() {
    when(cohortRepository.findAll()).thenReturn(List.of());

    var result = service.listAll();

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  void findById_existingId_returnsCohort() {
    UUID id = UUID.randomUUID();
    JCohort jCohort =
        JCohort.builder().id(id).name("Promo 2025").startYear(2022).endYear(2025).build();
    Cohort expected = new Cohort(id, "Promo 2025", 2022, 2025);

    when(cohortRepository.findById(id)).thenReturn(Optional.of(jCohort));
    when(cohortMapper.toDomain(jCohort)).thenReturn(expected);

    var result = service.findById(id);

    assertNotNull(result);
    assertEquals(id, result.id());
    assertEquals("Promo 2025", result.name());
    assertEquals(2022, result.startYear());
    assertEquals(2025, result.endYear());
  }

  @Test
  void findById_unknownId_throwsNotFoundException() {
    UUID unknownId = UUID.randomUUID();

    when(cohortRepository.findById(unknownId)).thenReturn(Optional.empty());

    var exception = assertThrows(NotFoundException.class, () -> service.findById(unknownId));

    assertEquals("cohort " + unknownId + " not found", exception.getMessage());
  }
}
