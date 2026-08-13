package hei.school.graduate.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GradeHistory(
    UUID id,
    Grade grade,
    User user,
    BigDecimal previousScore,
    BigDecimal newScore,
    String reason,
    Instant modificationDate) {}
