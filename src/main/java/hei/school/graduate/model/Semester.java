package hei.school.graduate.model;

import java.util.UUID;

public record Semester(UUID id, Cohort cohort, Integer semesterNumber, String academicYear) {}
