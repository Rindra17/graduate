package hei.school.graduate.model;

import java.util.UUID;

public record Course(
    UUID id, Semester semester, Branch branch, String code, String title, Integer credits) {}
