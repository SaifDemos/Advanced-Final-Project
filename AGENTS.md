# AGENTS.md

## Build & Run

```bash
./compile.sh     # javac with JavaFX + JDBC on module path
./run.sh         # launches MainApp with JavaFX modules
./run-sms.sh     # launches SMS App (for OTP password reset) — must be running before using "Forgot Password?"
./clean.sh       # find . -name "*.class" -delete
```

**Prerequisites:**
- Java 21+
- JavaFX SDK 25.0.2 at `/home/saifdemos/programs/javafx-sdk-25.0.2/lib` (path hardcoded in all `.sh` scripts)
- PostgreSQL at `127.0.0.1:5432`, user `admin`, pass `admin`, db `postgres`
- JDBC driver: `postgresql-42.7.10.jar` co-located with JavaFX SDK lib dir

No build tool — raw `javac`/`java` with `--module-path` and `--add-modules javafx.controls,javafx.fxml,java.sql`. No tests, linter, or typechecker configured.

## Architecture

- Entry: `MainApp.java` → `HomePage.java` → `LogIn`/`SignUp` → `Dashboard` (admin) | `StudentProfile` (student)
- `scenes/`: all JavaFX scene classes (UI built programmatically, **no FXML**)
- `utility/`: `DBUtil.java` (DB helper), `Assistor.java` (alerts), `SceneAnimator.java` (transitions)
- `classes/`: data models — `Person` (abstract), `Student`, `Lecturer`, `Course`, `Collage`, `Department`
- `assets/style.css`: shared stylesheet referenced via `file:./assets/style.css`
- Enrollment: `EnrollStudent.java` (student → course), `EnrollLecturer.java` (lecturer → course), backed by `STUDENT_COURSE` and `COURSE_LECTURER` junction tables
- SMS App: `scenes/SMSApp.java` — standalone `Application`, listens on port 12345 via `ServerSocket`

## Gotchas

- **Resource paths**: Most assets use `getClass().getResourceAsStream("./images/...")` (leading `./` required), but `LogIn.java`, `SignUp.java`, and `SMSApp.java` use `new Image("file:./images/...")` for icons — two different patterns in the codebase
- **CSS/stylesheets**: referenced as `file:./assets/style.css` (not via classpath)
- Window fixed size: `primaryStage.setResizable(false)` in `MainApp.java:27`
- DB credentials hardcoded in `utility/DBUtil.java:11-13`
- Alert pattern: Success alerts use AcademiX icon (`setGraphic(iconView)`), Warning/Error do not
- **Password visibility toggle**: two fields (`PasswordField` + `TextField`) with `setVisible()`/`setManaged()` swap in an `HBox` with eye icon button — see `LogIn.java`, `SignUp.java`, `AccountInfo.java`
- **OTP Flow**: "Forgot Password?" → phone dialog → DB query in `Task<>` (background) → OTP sent via `Socket` to port 12345 → OTP dialog opens via `Platform.runLater()`
- **Delete constraints**: clean junction tables before main delete:
  - Lecturer: `COURSE_LECTURER` first, then `LECTURER`
  - Course: `COURSE_LECTURER` + `STUDENT_COURSE` first, then `COURSE`
  - Student: `STUDENT_COURSE` first, then `STUDENT`
- `Collage.java`: hardcoded college name "Faculty of Science", dean, departments — not user-configurable
- Auto-generated email on signup: `firstName.studentId@academix.edu`
- DB tables: `STUDENT`, `LECTURER`, `COURSE`, `STUDENT_COURSE`, `COURSE_LECTURER`, `ADMINS`
