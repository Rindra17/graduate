package hei.school.graduate.model;

import java.util.UUID;

public record Cohort(UUID id, String name, Integer startYear, Integer endYear) {}
