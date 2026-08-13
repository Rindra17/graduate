package hei.school.graduate.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Grade(UUID id, Student student, Exam exam, BigDecimal score) {}
