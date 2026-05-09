package com.example.simpleportal.Controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.simpleportal.Model.ActivityLog;
import com.example.simpleportal.Model.LoginForm;
import com.example.simpleportal.Model.ScheduleSlot;
import com.example.simpleportal.Model.SignupForm;
import com.example.simpleportal.Model.Student;
import com.example.simpleportal.Service.ActivityLogService;
import com.example.simpleportal.Service.ScheduleSlotRepository;
import com.example.simpleportal.Service.StudentRepository;
import com.example.simpleportal.Service.StudentService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class PageController {
    static final String COOKIE_EMAIL = "student_email";
    static final String COOKIE_ID    = "student_id";
    static final String COOKIE_ROLE  = "student_role";

    private static final String REDIRECT_LOGIN  = "redirect:/login";
    private static final String REDIRECT_SIGNUP = "redirect:/signup";

    private final StudentService         studentService;
    private final StudentRepository      studentRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final ActivityLogService     logService;

    public PageController(StudentService studentService,
                          StudentRepository studentRepository,
                          ScheduleSlotRepository scheduleSlotRepository,
                          ActivityLogService logService) {
        this.studentService         = studentService;
        this.studentRepository      = studentRepository;
        this.scheduleSlotRepository = scheduleSlotRepository;
        this.logService             = logService;
    }

    @GetMapping("/")
    public String home() { return REDIRECT_LOGIN; }
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute LoginForm form, HttpServletResponse response) {
        String email    = trim(form.getUsername());
        String password = trim(form.getPassword());
        if (email.isEmpty() || password.isEmpty())
            return REDIRECT_LOGIN + "?error=empty";

        Optional<Student> opt = studentRepository.login(email, password);
        if (opt.isEmpty())
            return REDIRECT_LOGIN + "?error=invalid";

        Student s = opt.get();
        addCookie(response, COOKIE_EMAIL, s.getEmail(), days(7));
        addCookie(response, COOKIE_ID,    String.valueOf(s.getId()), days(7));
        addCookie(response, COOKIE_ROLE,  s.getRole(), days(7));

        logService.log(s, "LOGIN", "Logged in");

        if (s.isAdmin()) return "redirect:/admin/dashboard";
        return "redirect:/dashboard?name=" + encode(s.getName())
                + "&faculty=" + encode(nullSafe(s.getFaculty(), "CS"))
                + "&semester=1";
    }
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@ModelAttribute SignupForm form) {

        // --- input validation ---
        String email    = trim(form.getEmail());
        String password = trim(form.getPassword());
        String confirm  = trim(form.getConfirmPassword());

        if (email.isEmpty() || password.isEmpty() || form.getId() == null
                || trim(form.getPhone()).isEmpty() || trim(form.getAddress()).isEmpty())
            return REDIRECT_SIGNUP + "?error=missing";
        if (!email.contains("@") || !email.contains("."))
            return REDIRECT_SIGNUP + "?error=email";
        if (password.length() < 6)
            return REDIRECT_SIGNUP + "?error=short";
        if (!password.equals(confirm))
            return REDIRECT_SIGNUP + "?error=mismatch";
        if (studentRepository.findByEmailIgnoreCase(email).isPresent())
            return REDIRECT_SIGNUP + "?error=exists";

        String role = "ADMIN".equalsIgnoreCase(trim(form.getRole())) ? "ADMIN" : "STUDENT";
        String name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        Student s = new Student(
                form.getId(), name, email,
                trim(form.getPhone()), trim(form.getAddress()),
                password,
                nullSafe(form.getFaculty(), "CS"),
                0.0f, 0L, role);
        studentRepository.save(s);
        logService.log(s, "SIGNUP", "Account created with role=" + role);

        return REDIRECT_LOGIN + "?registered=true";
    }
    @GetMapping("/logout")
    public String logout(@CookieValue(name = COOKIE_ID, required = false) String cookieId,
                         HttpServletResponse response) {
        logService.log(cookieId, "LOGOUT", "Logged out");
        deleteCookie(response, COOKIE_EMAIL);
        deleteCookie(response, COOKIE_ID);
        deleteCookie(response, COOKIE_ROLE);
        return REDIRECT_LOGIN;
    }
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) Integer semester,
            @CookieValue(name = COOKIE_ID,   required = false) String cookieId,
            @CookieValue(name = COOKIE_ROLE, required = false) String cookieRole,
            Model model) {
        if ("ADMIN".equalsIgnoreCase(cookieRole))
            return "redirect:/admin/dashboard";

        Student loggedIn = resolveById(cookieId);
        String  n  = name    != null ? name    : (loggedIn != null ? loggedIn.getName()    : "");
        String  f  = faculty != null ? faculty : (loggedIn != null ? nullSafe(loggedIn.getFaculty(),"CS") : "CS");
        int     sem = semester != null ? semester : 1;

        model.addAttribute("name",     n);
        model.addAttribute("faculty",  f);
        model.addAttribute("semester", sem);
        model.addAttribute("student",  loggedIn);

        if (!n.isEmpty() && !f.isEmpty()) {
            Map<String, Object> data = studentService.processStudentData(n, sem, f);
            model.addAttribute("data", data);
        }
        return "DashBoarStudent";
    }

    @GetMapping("/schedule")
    public String schedule(
            @RequestParam(required = false) String day,
            @CookieValue(name = COOKIE_ID,   required = false) String cookieId,
            @CookieValue(name = COOKIE_ROLE, required = false) String cookieRole,
            Model model) {

        if ("ADMIN".equalsIgnoreCase(cookieRole))
            return "redirect:/admin/dashboard";

        long studentId = parseLong(cookieId, 1L);
        List<ScheduleSlot> slots = scheduleSlotRepository.findForStudent(studentId, day);
        model.addAttribute("slots",   slots);
        model.addAttribute("day",     day == null ? "" : day);
        model.addAttribute("student", resolveById(cookieId));
        return "Schedule";
    }

    @GetMapping("/profile")
    public String profile(
            @CookieValue(name = COOKIE_EMAIL, required = false) String cookieEmail,
            @CookieValue(name = COOKIE_ID,    required = false) String cookieId,
            Model model) {

        Student student = resolveByEmail(cookieEmail);
        List<ActivityLog> logs = student != null
                ? logService.getForStudent(student.getId()) : List.of();
        model.addAttribute("student", student);
        model.addAttribute("logs",    logs);
        return "profile";
    }

    @GetMapping("/history")
    public String history(
            @CookieValue(name = COOKIE_ID,   required = false) String cookieId,
            @CookieValue(name = COOKIE_ROLE, required = false) String cookieRole,
            Model model) {

        if ("ADMIN".equalsIgnoreCase(cookieRole))
            return "redirect:/admin/history";

        long sid = parseLong(cookieId, 1L);
        model.addAttribute("logs",    logService.getForStudent(sid));
        model.addAttribute("student", resolveById(cookieId));
        return "history";
    }

    private Student resolveById(String rawId) {
        long id = parseLong(rawId, -1L);
        return id < 0 ? null : studentRepository.findById(id).orElse(null);
    }

    private Student resolveByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return studentRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    static long parseLong(String raw, long fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try { return Long.parseLong(raw.trim()); } catch (NumberFormatException e) { return fallback; }
    }

    private String trim(String s)                    { return s == null ? "" : s.trim(); }
    private String nullSafe(String s, String def)    { return (s == null || s.isBlank()) ? def : s; }
    private String encode(String v)                  { return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8); }
    private int    days(int d)                       { return d * 86400; }

    void addCookie(HttpServletResponse r, String name, String value, int maxAge) {
        String enc = value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
        r.addHeader("Set-Cookie", name + "=" + enc + "; Max-Age=" + maxAge + "; Path=/; SameSite=Lax");
    }

    void deleteCookie(HttpServletResponse r, String name) {
        r.addHeader("Set-Cookie", name + "=; Max-Age=0; Path=/; SameSite=Lax");
    }
}
