package hei.school.graduate.exception.model;

import java.time.Instant;

public record ExceptionBody(int status, String error, String message, String path, Instant timestamp) {}
