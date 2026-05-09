# SimplePortal

A Spring Boot MVC application demonstrating a student portal with authentication, role-based access, and repository-backed data access.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/simpleportal/
│   │       ├── SimplePortalApplication.java    # Main application entry point
│   │       ├── Controller/
│   │       │   ├── AdminController.java        # Admin page handlers
│   │       │   ├── PageController.java         # Student-facing pages and auth flows
│   │       │   └── StudentController.java      # Student dashboard API endpoints
│   │       ├── Service/
│   │       │   ├── StudentService.java         # Business logic for dashboard data
│   │       │   ├── ActivityLogService.java     # Activity log access
│   │       │   └── repository interfaces       # Data access repositories
│   │       ├── Model/
│   │       │   ├── Student.java                # Student entity and role field
│   │       │   ├── Book.java                   # Book entity
│   │       │   ├── StoreCourse.java            # Store course entity
│   │       │   └── other domain entities       # Orders, schedules, logs, enrollments
│   │       └── SecurityConfig.java             # Spring Security configuration
│   └── resources/
│       ├── application.properties             # App configuration
│       ├── templates/                          # Thymeleaf views
│       └── static/                             # Static assets
└── test/
    └── java/
        └── com/example/simpleportal/          # Unit and integration tests
```

## Architecture Pattern

### Three-Tier Architecture

- **Controller Layer**
  - Handles HTTP requests and responses
  - Routes incoming requests to service methods
  - Validates input and returns views or JSON
- **Service Layer**
  - Contains business logic and orchestration
  - Generates dashboard content and computes GPA
  - Converts request input into application data
- **Repository Layer**
  - Interfaces with H2 database through Spring Data JPA
  - Provides CRUD operations and query methods
  - Abstracts persistence logic from controllers and services
- **Entity Layer**
  - JPA entities represent database tables
  - Includes relationships such as Student → Order, Student → Enrollment
- **DTO / View Layer**
  - Thymeleaf templates and controller model attributes
  - Decouples page rendering from database entities

## Technologies Used

- Spring Boot 4.0.6
- Spring Data JPA
- Spring Security
- Thymeleaf
- H2 Database
- Maven
- JUnit (testing dependencies included)

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on:

```
http://localhost:8080
```

## Features Implemented

- User signup and login
- Role-based authentication (`ADMIN` and `STUDENT`)
- Admin dashboard with student, book, course, order and schedule management
- Student dashboard with course recommendations and GPA display
- Schedule viewing for students
- Profile and activity history pages
- H2 in-memory database support
- Cookie-based session state for basic navigation

## API / Page Endpoints

### Public pages

- `GET /login` — login page
- `GET /signup` — signup page

### Student pages

- `GET /dashboard` — student dashboard
- `GET /schedule` — student schedule view
- `GET /profile` — student profile page
- `GET /history` — student activity history

### Admin pages

- `GET /admin/dashboard` — admin dashboard
- `GET /admin/students` — view students
- `GET /admin/books` — manage books
- `GET /admin/courses` — manage store courses
- `GET /admin/schedules` — manage student schedules
- `GET /admin/orders` — view all orders
- `GET /admin/history` — view all activity logs

### User Management API

This project does not currently expose a generic `/api/users` CRUD API, but the app structure follows the same MVC + repository layering that would support it.

A typical user management API for this architecture would include:

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/search?term=...` | Search users |
| GET | `/api/users/active/list` | Get active users |
| POST | `/api/users` | Create a new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

### Example Requests

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "1234567890"
  }'
```

Get all users:

```bash
curl http://localhost:8080/api/users
```

Update a user:

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@example.com",
    "phone": "0987654321"
  }'
```

## Database

The application uses H2 in-memory database by default.

### H2 Console

If enabled, access the console at:

```
http://localhost:8080/h2-console
```

Default values:

- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## Key Concepts

### Model (Entity)

- Represents the data structure and database schema
- Annotated with JPA annotations (`@Entity`, `@Table`, etc.)
- Examples: `Student`, `Book`, `StoreCourse`, `Order`, `ScheduleSlot`

### View (Page Response)

- Thymeleaf templates render HTML pages
- Controllers pass model attributes to templates
- Decouples UI from database entities

### Controller

- Routes HTTP requests to service methods
- Validates form input and handles responses
- Maps user actions to URLs

### Repository Pattern

- Abstracts data access logic
- Uses Spring Data JPA repository interfaces
- Makes database switching easier

### Service Layer

- Contains business logic and validation
- Manages data transformation between controllers and repositories
- Can handle transactions and dashboard computation

## Best Practices Implemented

- ✅ Separation of Concerns — Each layer has distinct responsibilities
- ✅ Repository Pattern — Data access abstraction
- ✅ MVC Structure — Controllers, Services, Models, Views are separated
- ✅ Input Validation — In controller methods and form handling
- ✅ Role-based Authorization — Admin and student access control
- ✅ RESTful Design — Standard HTTP methods and status handling for endpoints

## Run tests

- **ADMIN**
  - Can access admin pages under `/admin/*`
  - Can add/delete books and store courses
  - Can manage student schedules
  - Can view orders and activity logs
- **STUDENT**
  - Can access dashboard, schedule, profile, and personal history
  - Can view semester courses and GPA data

## How the app works

- `SecurityConfig` protects routes and loads users from the `StudentRepository`
- `PageController` handles login/signup, student dashboard, and page rendering
- `AdminController` handles admin-only functionality and verifies admin role
- `StudentService` builds student dashboard responses based on faculty and semester
- Repositories persist entity data to H2 and provide data queries

## Run tests

```bash
./mvnw test
```

## Notes

- This project uses H2 for development and demo use
- You can extend it with MySQL or PostgreSQL by updating `application.properties`
- Authentication is handled by Spring Security and role cookies

## Next Steps

These enhancements will make the app more complete, testable, and production-ready:

- Improve authentication and authorization
  - Strengthen authentication flows with Spring Security roles and permissions
  - Add password hashing and session handling instead of storing raw passwords
- Add logging and audit support
  - Use SLF4J / Logback for structured logging
  - Log key actions such as login, signup, order creation, and admin updates
- Add integration and unit tests
  - Add Spring Boot integration tests for controllers and services
  - Cover repository behavior and security flows with JUnit tests
- Implement caching where appropriate
  - Cache frequently read values such as course catalogs or dashboard data
  - Use Spring Cache for better performance on repeated queries
- Add API documentation
  - Use Swagger / OpenAPI to document available endpoints and request/response models
  - Provide a browser-based API explorer for developers

## License

This project is open source and can be adapted for learning or extension.
