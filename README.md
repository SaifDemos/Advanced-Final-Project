# AcademiX — University Management System

A JavaFX desktop application for managing university academic operations: student/lecturer/course management, enrollment, and user authentication with OTP-based password recovery.

## Prerequisites

- Java 21+ (required for JavaFX 25.0.2)
- JavaFX SDK 25.0.2 (path hardcoded in shell scripts: `/home/saifdemos/programs/javafx-sdk-25.0.2/lib` — update scripts if your installation differs)
- PostgreSQL 12+ running at `127.0.0.1:5432`
- PostgreSQL JDBC Driver `postgresql-42.7.10.jar` (co-located with JavaFX SDK lib dir)

## Build & Run

```bash
./compile.sh     # Compile all Java files
./run.sh         # Launch main application
./run-sms.sh     # Launch SMS App (required for OTP password reset)
./clean.sh       # Clean compiled .class files
```

## Project Structure

| Directory | Description |
|-----------|-------------|
| `scenes/` | All JavaFX scene classes (UI screens) |
| `classes/` | Data model classes |
| `utility/` | Utility/helper classes |
| `images/` | UI assets (icons, backgrounds) |
| `fonts/` | Custom fonts (Poppins, Bebas Neue) |

---

## OOP Concepts — Code Examples for Presentation

### 1. Classes (6+ classes)

| Class | File | Purpose |
|-------|------|---------|
| `Person` | `classes/Person.java` | Abstract base class for people |
| `Student` | `classes/Student.java` | Student model, extends Person |
| `Lecturer` | `classes/Lecturer.java` | Lecturer model, extends Person |
| `Course` | `classes/Course.java` | Course model with lecturer assignment |
| `Collage` | `classes/Collage.java` | College data (name, dean, departments) |
| `Department` | `classes/Department.java` | Department model (name + code) |
| `DBUtil` | `utility/DBUtil.java` | Database connection helper |
| `Assistor` | `utility/Assistor.java` | Alert dialogs and UI helpers |

---

### 2. Abstract Class + Abstract Method

**`classes/Person.java`** — Abstract class with abstract method:
```java
public abstract class Person {
    private final SimpleStringProperty name;
    private final SimpleLongProperty id;

    public Person(String name, long id) { ... }

    public abstract String getRoleDescription();   // ← abstract method

    @Override
    public String toString() {                     // ← override from Object
        return "Role: " + getRoleDescription() + "\nName: " + getName() + "\nID: " + getId();
    }
}
```

**Implemented in subclasses:**
- `Student.java:28` → `return "Student";`
- `Lecturer.java:26` → `return "Lecturer";`

---

### 3. Inheritance + Method Overriding

`Student` and `Lecturer` both **extend** `Person` and **override** `getRoleDescription()`:

```java
// classes/Student.java
public class Student extends Person {
    @Override
    public String getRoleDescription() { return "Student"; }
}

// classes/Lecturer.java
public class Lecturer extends Person {
    @Override
    public String getRoleDescription() { return "Lecturer"; }
}
```

`Department` also has its own `toString()` override:
```java
// classes/Department.java:12-15
@Override
public String toString() {
    return name + " (" + code + ")";
}
```

---

### 4. Method Overloading

**`utility/Assistor.java :27`** — `createWithBackground` is overloaded (same name, different parameters):

```java
// Version 1: uses default dimensions (815 x 665)
public static StackPane createWithBackground(Node content, double opacity) {
    return createWithBackground(content, opacity, DEFAULT_WIDTH, DEFAULT_HEIGHT);
}

// Version 2: custom dimensions
public static StackPane createWithBackground(Node content, double opacity, double width, double height) {
    ...
}
```

**`utility/SceneAnimator.java :78`** — `fadeIn` is overloaded:
```java
public static void fadeIn(Node node) { ... }          // default duration
public static void fadeIn(Node node, Duration duration) { ... }  // custom duration
```

---

### 5. Association, Aggregation & Composition

**Association** (uses relationship):
- `Student` ↔ `Course` via `STUDENT_COURSE` junction table (many-to-many)
- `Lecturer` ↔ `Course` via `COURSE_LECTURER` junction table (many-to-many)

**Aggregation** (has-a, lifetime independent):
```java
// classes/Collage.java:14,22-32
private List<Department> departments;  // College HAS departments, but departments can exist independently

public Collage() {
    this.departments = new ArrayList<>();
    this.departments.add(new Department("Physics Department", 101));
    // ...
}
```

**Composition** (owns-a, lifetime dependent — owned object dies with owner):
```java
// classes/Student.java:16,23
private final ArrayList<Course> enrolledCourses;  // Exists only within Student

public Student(...) {
    this.enrolledCourses = new ArrayList<>();
}
```

---

### 6. ArrayList Usage

ArrayLists are used throughout the model classes:

```java
// classes/Student.java:16
private final ArrayList<Course> enrolledCourses;

// classes/Lecturer.java:13
private final ArrayList<Course> coursesTeaching;

// classes/Course.java:11
private final ArrayList<Lecturer> assignedLecturers;

// classes/Collage.java:14
private List<Department> departments;  // ArrayList internally
```

Managed via methods like `enrollInCourse(Course course)` and `assignLecturer(Lecturer lecturer)`.

---

### 7. Exception Handling

**Try-catch blocks** are used extensively for database operations:

```java
// utility/DBUtil.java:14-32
public static Connection dbConnect() {
    try {
        Class.forName("org.postgresql.Driver");
        con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    } catch (ClassNotFoundException e) {
        System.err.println("PostgreSQL JDBC Driver not found.");
    } catch (SQLException ex) {
        System.err.println("Failed to connect to the database.");
    }
    return con;
}
```

**Background tasks** use `Task<>` with `setOnFailed` handlers (`LogIn.java:247-261`, `LogIn.java:342-367`).

---

### 8. GUI — JavaFX

- **No FXML**: All UI built programmatically in Java code
- **Scenes** (in `scenes/`): `HomePage`, `LogIn`, `SignUp`, `Dashboard`, `ManageStudents`, `ManageLecturer`, `ManageCourses`, `EnrollStudent`, `EnrollLecturer`, `StudentProfile`, `AccountInfo`, `About`, `SMSApp`
- **Password visibility toggle**: dual `PasswordField` + `TextField` with `setVisible()`/`setManaged()` swap, wrapped in `HBox` with toggle button (`LogIn.java:40-56`, `SignUp.java:77-93`, `scenes/AccountInfo.java:122-140`)
- **Custom fonts**: Poppins loaded via `Font.loadFont()` in `MainApp.java:19`
- **Alerts**: Standardized via `utility/Assistor.java` (Success uses AcademiX icon, Warning/Error do not)

---

### 9. Database (JDBC + PostgreSQL)

- **Connection**: `utility/DBUtil.java` — static `dbConnect()` method with hardcoded credentials
- **Tables**:
  - `STUDENT` (student_id, name, email, password, phone, gender, birth_date, cgpa, major, minor)
  - `LECTURER` (lecturer_id, name, phone, birth_date, gender, salary)
  - `COURSE` (course_id, name, record_hours)
  - `STUDENT_COURSE` (junction table)
  - `COURSE_LECTURER` (junction table)
  - `ADMINS` (admin_id, email, password)
- **Delete constraint**: Junction tables must be cleaned before deleting main records (see `ManageLecturer.java:597-611`, `ManageCourses.java:271-280`, `ManageStudents.java:667-681`)
- **Auto-email generation**: On signup, email is auto-generated as `firstName.studentId@academix.edu` (`SignUp.java:213-220`)

---

## Bonus: Networking (Socket Programming)

The OTP password reset feature uses **Java Socket programming** for client-server communication between the main app and the SMS App.

### Components
1. **SMS Server** (`scenes/SMSApp.java`):
   - Standalone JavaFX `Application` that opens a `ServerSocket` on port `12345`.
   - Runs a background loop accepting client connections and reading OTP messages.
   - Displays received messages in a `ListView` UI.

   Key code:
   ```java
   // scenes/SMSApp.java:27-29, 111-128
   public class SMSApp extends Application {
       private ServerSocket serverSocket;
       
       @Override
       public void start(Stage stage) {
           new Thread(() -> {
               try (ServerSocket server = new ServerSocket(12345)) {
                   serverSocket = server;
                   while (!serverSocket.isClosed()) {
                       Socket clientSocket = server.accept();
                       BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                       String message = in.readLine();
                       if (message != null) {
                           Platform.runLater(() -> messages.add(0, message));
                       }
                       in.close();
                       clientSocket.close();
                   }
               } catch (IOException e) { e.printStackTrace(); }
           }).start();
       }
   }
   ```

2. **SMS Client** (`scenes/LogIn.java` Forgot Password flow):
   - When an OTP is generated, a new thread creates a `Socket` connection to `localhost:12345`.
   - Sends the OTP message via `PrintWriter`.

   Key code:
   ```java
   // scenes/LogIn.java:274-281
   new Thread(() -> {
       try (Socket s = new Socket("localhost", 12345);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
           out.println(message); // OTP message sent to SMS server
       } catch (Exception ex) { ex.printStackTrace(); }
   }).start();
   ```

### Flow
1. Launch SMS Server first: `./run-sms.sh` (starts `SMSApp` on port 12345)
2. User clicks "Forgot Password?" → enters registered phone number
3. OTP is generated, then sent via socket to the SMS Server
4. SMS Server receives and displays the OTP message in its UI

---

## Architecture Overview

```
MainApp.java → HomePage.java → LogIn/SignUp → Dashboard (admin) | StudentProfile (student)
                                    ↓
                            Forgot Password? → SMSApp (port 12345, socket-based OTP)
```

## Important Notes

- Password visibility toggle: `PasswordField` + `TextField` swap via `HBox` container with eye icon button
- `Collage.java`: hardcoded college name "Faculty of Science", dean, and departments
- Window is fixed-size (`setResizable(false)` in `MainApp.java:27`)
- No build tool (Maven/Gradle), no tests, no linter configured
