package hei.school.graduate.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Exam(UUID id, Course course, String title, BigDecimal weight, LocalDate examDate) {}
