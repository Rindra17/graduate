package hei.school.graduate.model;

import java.util.UUID;

public record Groupe(UUID id, String name, Cohort cohort, Branch branch) {}
