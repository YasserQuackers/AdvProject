package com.example.simpleportal.Service;

import com.example.simpleportal.Model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent_IdOrderBySemesterAscCatalogCourse_CourseCodeAsc(Long studentId);

    Optional<Enrollment> findByStudent_IdAndCatalogCourse_IdAndSemester(Long studentId, Long catalogCourseId, String semester);
}

