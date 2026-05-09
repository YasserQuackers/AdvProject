package com.example.simpleportal.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @Column(nullable = false)
    private String action;

    @Column(length = 500)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();


    public ActivityLog() {}

    public ActivityLog(Student student, String action, String detail) {
        this.student   = student;
        this.action    = action;
        this.detail    = detail;
        this.timestamp = LocalDateTime.now();
    }
    public Long          getId()        { return id; }
    public void          setId(Long id) { this.id = id; }

    public Student       getStudent()         { return student; }
    public void          setStudent(Student s){ this.student = s; }

    public String        getAction()          { return action; }
    public void          setAction(String a)  { this.action = a; }

    public String        getDetail()          { return detail; }
    public void          setDetail(String d)  { this.detail = d; }

    public LocalDateTime getTimestamp()           { return timestamp; }
    public void          setTimestamp(LocalDateTime t){ this.timestamp = t; }
}
