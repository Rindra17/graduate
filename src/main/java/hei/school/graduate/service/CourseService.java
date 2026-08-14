package hei.school.graduate.service;

import java.util.List;
import java.util.UUID;

import hei.school.graduate.endpoint.rest.controller.CourseTeacherRequest;
import hei.school.graduate.endpoint.rest.controller.dto.CourseRequest;
import hei.school.graduate.endpoint.rest.controller.dto.ExamRequest;
import hei.school.graduate.model.Course;
import hei.school.graduate.model.Exam;

/**
 * CourseService
 */
public class CourseService {

    public List<Course> getAllCourses() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllCourses'");
    }

    public Course createCourse(CourseRequest newCourse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createCourse'");
    }

    public Course getCourseById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCourseById'");
    }

    public Course updateCourseById(UUID id, CourseRequest newCourse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateCourseById'");
    }

    public void deleteCourseById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCourseById'");
    }

    public List<String> getTeachersByCourseId(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTeachersByCourseId'");
    }

    public void addTeacherToCourse(UUID id, CourseTeacherRequest courseTeacherRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addTeacherToCourse'");
    }

    public void removeTeacherFromCourse(UUID id, UUID teacherId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeTeacherFromCourse'");
    }

    public List<String> getGroupsByCourseId(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGroupsByCourseId'");
    }

    public List<Exam> getExamsByCourseId(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExamsByCourseId'");
    }

    public void createExam(UUID id, ExamRequest examRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createExam'");
    }

}
