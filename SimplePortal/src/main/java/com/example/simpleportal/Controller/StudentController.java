package com.example.simpleportal.Controller;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.simpleportal.Model.Student;
import com.example.simpleportal.Service.StudentRepository;
import com.example.simpleportal.Service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;
    private final StudentRepository studentRepository;

    public StudentController(StudentService service, StudentRepository studentRepository) {
        this.service = service;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/portal")
    public ResponseEntity<Map<String,Object>> getStudentDashboard(@RequestParam String name,@RequestParam int semester ,@RequestParam String faculty) {
        Student student = studentRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        Map<String, Object> data = service.processStudentData(student.getName(), semester, faculty);
        return ResponseEntity.ok(data);
    }
}