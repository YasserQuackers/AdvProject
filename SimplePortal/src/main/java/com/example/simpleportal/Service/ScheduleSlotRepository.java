package com.example.simpleportal.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.simpleportal.Model.ScheduleSlot;

import java.util.List;

public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
    List<ScheduleSlot> findByStudent_IdOrderByDayAscTimeSlotAsc(Long studentId);

    @Query("""
            select s from ScheduleSlot s
            where s.student.id = :studentId
              and (:day is null or lower(s.day) = lower(:day))
            order by s.day asc, s.timeSlot asc
            """)
    List<ScheduleSlot> findForStudent(@Param("studentId") Long studentId, @Param("day") String day);
}

