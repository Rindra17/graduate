package hei.school.graduate.model;

import java.time.LocalDate;
import java.util.UUID;

public record StudentGroupHistory(
    UUID id,
    Student student,
    Groupe groupe,
    LocalDate startDate,
    LocalDate endDate,
    String changeReason) {}
