package com.example.simpleportal.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.simpleportal.Model.Book;
import com.example.simpleportal.Model.ScheduleSlot;
import com.example.simpleportal.Model.StoreCourse;
import com.example.simpleportal.Model.Student;
import com.example.simpleportal.Service.ActivityLogService;
import com.example.simpleportal.Service.BookRepository;
import com.example.simpleportal.Service.OrderItemRepository;
import com.example.simpleportal.Service.OrderRepository;
import com.example.simpleportal.Service.ScheduleSlotRepository;
import com.example.simpleportal.Service.StoreCourseRepository;
import com.example.simpleportal.Service.StudentRepository;
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StudentRepository      studentRepo;
    private final BookRepository         bookRepo;
    private final StoreCourseRepository  storeCourseRepo;
    private final OrderRepository        orderRepo;
    private final OrderItemRepository    orderItemRepo;
    private final ScheduleSlotRepository scheduleRepo;
    private final ActivityLogService     logService;

    public AdminController(StudentRepository studentRepo,
                           BookRepository bookRepo,
                           StoreCourseRepository storeCourseRepo,
                           OrderRepository orderRepo,
                           OrderItemRepository orderItemRepo,
                           ScheduleSlotRepository scheduleRepo,
                           ActivityLogService logService) {
        this.studentRepo     = studentRepo;
        this.bookRepo        = bookRepo;
        this.storeCourseRepo = storeCourseRepo;
        this.orderRepo       = orderRepo;
        this.orderItemRepo   = orderItemRepo;
        this.scheduleRepo    = scheduleRepo;
        this.logService      = logService;
    }
    @GetMapping("/dashboard")
    public String dashboard(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId,
            Model model) {

        if (!isAdmin(role)) return "redirect:/login";

        long studentCount = studentRepo.findByRoleIgnoreCaseOrderByNameAsc("STUDENT").size();
        long bookCount    = bookRepo.count();
        long courseCount  = storeCourseRepo.count();
        long orderCount   = orderRepo.count();

        model.addAttribute("studentCount", studentCount);
        model.addAttribute("bookCount",    bookCount);
        model.addAttribute("courseCount",  courseCount);
        model.addAttribute("orderCount",   orderCount);
        model.addAttribute("adminName",    resolveAdminName(cookieId));
        model.addAttribute("recentLogs",   logService.getAll().stream().limit(10).toList());
        return "AdminDashboard";
    }
    @GetMapping("/students")
    public String students(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            Model model) {
        if (!isAdmin(role)) return "redirect:/login";
        model.addAttribute("students", studentRepo.findByRoleIgnoreCaseOrderByNameAsc("STUDENT"));
        return "AdminStudents";
    }
    @GetMapping("/books")
    public String books(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @RequestParam(required = false) String q,
            Model model) {
        if (!isAdmin(role)) return "redirect:/login";
        model.addAttribute("books", bookRepo.search(q));
        model.addAttribute("book",  new Book());
        model.addAttribute("q",     q == null ? "" : q);
        return "AdminBooks";
    }

    @PostMapping("/books/add")
    public String addBook(
            @ModelAttribute Book book,
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId) {
        if (!isAdmin(role)) return "redirect:/login";
        // validation
        if (book.getTitle() == null || book.getTitle().isBlank()) return "redirect:/admin/books?error=title";
        bookRepo.save(book);
        logService.log(cookieId, "ADMIN_ADD_BOOK", "Added book: " + book.getTitle());
        return "redirect:/admin/books";
    }

    @PostMapping("/books/delete")
    public String deleteBook(
            @RequestParam Long id,
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId) {
        if (!isAdmin(role)) return "redirect:/login";
        bookRepo.findById(id).ifPresent(b -> {
            logService.log(cookieId, "ADMIN_DELETE_BOOK", "Deleted book: " + b.getTitle());
            bookRepo.delete(b);
        });
        return "redirect:/admin/books";
    }
    @GetMapping("/courses")
    public String courses(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @RequestParam(required = false) String q,
            Model model) {
        if (!isAdmin(role)) return "redirect:/login";
        model.addAttribute("courses", storeCourseRepo.search(q));
        model.addAttribute("course",  new StoreCourse());
        model.addAttribute("q",       q == null ? "" : q);
        return "AdminCourses";
    }

    @PostMapping("/courses/add")
    public String addCourse(
            @ModelAttribute StoreCourse course,
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId) {
        if (!isAdmin(role)) return "redirect:/login";
        if (course.getName() == null || course.getName().isBlank()) return "redirect:/admin/courses?error=name";
        storeCourseRepo.save(course);
        logService.log(cookieId, "ADMIN_ADD_COURSE", "Added store course: " + course.getName());
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/delete")
    public String deleteCourse(
            @RequestParam Long id,
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId) {
        if (!isAdmin(role)) return "redirect:/login";
        storeCourseRepo.findById(id).ifPresent(c -> {
            logService.log(cookieId, "ADMIN_DELETE_COURSE", "Deleted course: " + c.getName());
            storeCourseRepo.delete(c);
        });
        return "redirect:/admin/courses";
    }
    @GetMapping("/schedules")
    public String schedules(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            Model model) {
        if (!isAdmin(role)) return "redirect:/login";
        model.addAttribute("slots",    scheduleRepo.findAll());
        model.addAttribute("students", studentRepo.findByRoleIgnoreCaseOrderByNameAsc("STUDENT"));
        return "AdminScedual";
    }

    @PostMapping("/schedules/add")
    public String addSchedule(
            @RequestParam Long   studentId,
            @RequestParam String day,
            @RequestParam String timeSlot,
            @RequestParam String courseName,
            @RequestParam String location,
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId) {
        if (!isAdmin(role)) return "redirect:/login";
        // validation
        if (day == null || day.isBlank() || courseName == null || courseName.isBlank())
            return "redirect:/admin/schedules?error=fields";
        studentRepo.findById(studentId).ifPresent(s -> {
            ScheduleSlot slot = new ScheduleSlot(s, day.trim(), timeSlot.trim(), courseName.trim(), location.trim());
            scheduleRepo.save(slot);
            logService.log(cookieId, "ADMIN_ADD_SCHEDULE",
                    "Added schedule for student #" + studentId + ": " + courseName + " on " + day);
        });
        return "redirect:/admin/schedules";
    }

    @PostMapping("/schedules/delete")
    public String deleteSchedule(
            @RequestParam Long id,
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId) {
        if (!isAdmin(role)) return "redirect:/login";
        scheduleRepo.findById(id).ifPresent(sl -> {
            logService.log(cookieId, "ADMIN_DELETE_SCHEDULE", "Deleted schedule slot #" + id);
            scheduleRepo.delete(sl);
        });
        return "redirect:/admin/schedules";
    }
    @GetMapping("/orders")
    public String orders(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            @CookieValue(name = PageController.COOKIE_ID,   required = false) String cookieId,
            Model model) {
        if (!isAdmin(role)) return "redirect:/login";
        logService.log(cookieId, "ADMIN_VIEW_ORDERS", "Viewed all orders");
        model.addAttribute("orders", orderRepo.findAll());
        return "Adminorders";
    }
    @GetMapping("/history")
    public String history(
            @CookieValue(name = PageController.COOKIE_ROLE, required = false) String role,
            Model model) {
        if (!isAdmin(role)) return "redirect:/login";
        model.addAttribute("logs", logService.getAll());
        return "history";
    }

    // ── Helpers ────────────────────────────────────────────────
    private boolean isAdmin(String role) { return "ADMIN".equalsIgnoreCase(role); }

    private String resolveAdminName(String cookieId) {
        long id = PageController.parseLong(cookieId, -1L);
        if (id < 0) return "Admin";
        return studentRepo.findById(id).map(Student::getName).orElse("Admin");
    }
}
