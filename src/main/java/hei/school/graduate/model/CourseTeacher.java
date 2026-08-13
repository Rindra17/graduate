package hei.school.graduate.model;

import java.util.UUID;

public record CourseTeacher(UUID id, Teacher teacher, Course course) {}
