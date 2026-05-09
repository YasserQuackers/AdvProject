package com.example.simpleportal;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import com.example.simpleportal.Controller.CartController;
import com.example.simpleportal.Controller.PageController;
import com.example.simpleportal.Controller.PaymentController;
import com.example.simpleportal.Model.Book;
import com.example.simpleportal.Model.Course;
import com.example.simpleportal.Model.LoginForm;
import com.example.simpleportal.Model.PaymentForm;
import com.example.simpleportal.Model.SignupForm;
import com.example.simpleportal.Model.Student;
import com.example.simpleportal.Service.ActivityLogRepository;
import com.example.simpleportal.Service.BookRepository;
import com.example.simpleportal.Service.CatalogCourseRepository;
import com.example.simpleportal.Service.CourseService;
import com.example.simpleportal.Service.EnrollmentRepository;
import com.example.simpleportal.Service.OrderItemRepository;
import com.example.simpleportal.Service.OrderRepository;
import com.example.simpleportal.Service.ScheduleSlotRepository;
import com.example.simpleportal.Service.StudentRepository;
@SpringBootTest
class SimplePortalApplicationTests {
    @Autowired
    private PageController pageController;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CatalogCourseRepository catalogCourseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private CartController cartController;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ScheduleSlotRepository scheduleSlotRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        enrollmentRepository.deleteAll();
        scheduleSlotRepository.deleteAll();
        activityLogRepository.deleteAll();
        catalogCourseRepository.deleteAll();
        studentRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void signupPersistsStudentWithJpa() {
        SignupForm form = new SignupForm();
        form.setEmail("test@student.com");
        form.setId(1001L);
        form.setAddress("Cairo");
        form.setPassword("123456");
        form.setConfirmPassword("123456");
        form.setPhone("01000000000");
        form.setFaculty("CS");

        String redirect = pageController.handleSignup(form);

        assertThat(redirect).isEqualTo("redirect:/login?registered=true");
        assertThat(studentRepository.findByEmailIgnoreCase("test@student.com")).isPresent();
    }

    @Test
    void loginUsesJpaAndRedirectsToDashboard() {
        studentRepository.save(new Student(
                2002L,
                "jana",
                "jana@student.com",
                "0100",
                "Giza",
                "pass",
                "CS",
                3.2f,
                4L,
                "STUDENT"
        ));

        LoginForm form = new LoginForm();
        form.setUsername("jana@student.com");
        form.setPassword("pass");

        MockHttpServletResponse response = new MockHttpServletResponse();
        String redirect = pageController.handleLogin(form, response);
        assertThat(redirect).isEqualTo("redirect:/dashboard?name=jana&faculty=CS&semester=1");
    }

    @Test
    void booksPageLoadsJpaData() {
        bookRepository.save(new Book("Algorithms", "CLRS", new BigDecimal("500.00"), 5));
        List<Book> result = bookRepository.search("algo");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Algorithms");
    }

    @Test
    void gradesUseJpaRepository() {
        studentRepository.save(new Student(1L, "s1", "s1@test.com", null, null, "pass", "CS", 0.0f, 0L, "STUDENT"));
        courseService.addCourse(new Course(1L, "Java", "CS202", "Dr. X", "2", "A", 3));

        List<Course> courses = courseService.getAllCourses();
        assertThat(courses).hasSize(1);
        assertThat(courses.getFirst().getCourseCode()).isEqualTo("CS202");
    }

    @Test
    void gpaCalculatorUsesGradesAndHours() {
        studentRepository.save(new Student(1L, "s1", "s1@test.com", null, null, "pass", "CS", 0.0f, 0L, "STUDENT"));
        courseService.addCourse(new Course(1L, "Algorithms", "CS301", "Dr. Y", "3", "A", 3));
        courseService.addCourse(new Course(1L, "Physics", "PH201", "Dr. Z", "3", "B", 2));

        double gpa = courseService.calculateGpa(courseService.getAllCourses());
        assertThat(gpa).isCloseTo(3.6, within(0.001));
    }

    @Test
    void cartCheckoutFlowsToPaymentAndSummary() {
        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap paymentModel = new ExtendedModelMap();

        String addRedirect = cartController.addToCart("Book", null, "Clean Code", new BigDecimal("450.00"), "/books", null, session);
        assertThat(addRedirect).isEqualTo("redirect:/books");

        String paymentView = paymentController.payment(null, null, null, true, session, paymentModel);
        assertThat(paymentView).isEqualTo("payment");
        assertThat(paymentModel.getAttribute("cartItems")).isNotNull();

        PaymentForm paymentForm = new PaymentForm();
        paymentForm.setStudentId("1");
        paymentForm.setFullName("Jana Adham");
        paymentForm.setPaymentMethod("Credit Card");
        paymentForm.setCardNumber("1111222233334444");
        paymentForm.setExpiryDate("2027-12");
        paymentForm.setCvv("123");

        ExtendedModelMap summaryModel = new ExtendedModelMap();
        String summaryView = paymentController.processPayment(paymentForm, session, summaryModel);
        assertThat(summaryView).isEqualTo("ordersummary");
        assertThat(summaryModel.getAttribute("fees")).isEqualTo(450);
    }
}
