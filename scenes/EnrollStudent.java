package scenes;

import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import classes.Student;
import classes.Course;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class EnrollStudent {
    public Scene getScene(Stage stage) {

        Label title = new Label("Enroll Student in Course");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Student> studentCombo = new ComboBox<>();
        studentCombo.setPromptText("Select Student");
        studentCombo.setPrefWidth(300);

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Select Course");
        courseCombo.setPrefWidth(300);

        loadStudents(studentCombo);
        loadCourses(courseCombo);

        Button enrollBtn = new Button("Enroll");
        enrollBtn.setStyle("-fx-pref-width: 200px;");

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-pref-width: 200px;");

        enrollBtn.setOnAction(e -> {
            Student s = studentCombo.getValue();
            Course c = courseCombo.getValue();

            if (s == null || c == null) {
                Assistor.showWarningAlert("Missing Selection", "Missing Selection",
                        "Please select both student and course.");
                return;
            }

            try (Connection con = DBUtil.dbConnect()) {
                // Check if already enrolled
                PreparedStatement checkStmt = con.prepareStatement(
                        "SELECT 1 FROM STUDENT_COURSE WHERE student_id = ? AND course_id = ?");
                checkStmt.setLong(1, s.getStudentId());
                checkStmt.setLong(2, c.getCourseId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    Assistor.showErrorAlert("Already Enrolled", "Enrollment Error",
                            "Student " + s.getName() + " is already enrolled in " + c.getName() + ".");
                    return;
                }

                // Proceed with enrollment
                PreparedStatement pstmt = con.prepareStatement(
                        "INSERT INTO STUDENT_COURSE (student_id, course_id) VALUES (?, ?)");
                pstmt.setLong(1, s.getStudentId());
                pstmt.setLong(2, c.getCourseId());
                pstmt.executeUpdate();

                // Also add to ArrayList in memory
                s.enrollInCourse(c);

                Assistor.showSuccessAlert("Success", "Enrollment Complete",
                        s.getName() + " successfully enrolled to " + c.getName() + "!");

                studentCombo.getSelectionModel().clearSelection();
                courseCombo.getSelectionModel().clearSelection();

            } catch (SQLException ex) {
                Assistor.showErrorAlert("Database Error", "Database Error", "Error: " + ex.getMessage());
            }
        });

        backBtn.setOnAction(e -> {
            Dashboard dashboard = new Dashboard();
            SceneAnimator.transition(stage, dashboard.getScene(stage),
                    () -> stage.setTitle("AcademiX | Admin Dashboard"));
        });

        VBox layout = new VBox(20);
        layout.getChildren().addAll(title, studentCombo, courseCombo, enrollBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(Assistor.createWithBackground(layout, 0.2, 815, 710), 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");
        return scene;
    }

    private void loadStudents(ComboBox<Student> combo) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        try (Connection con = DBUtil.dbConnect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT student_id, name FROM STUDENT ORDER BY name");
            while (rs.next()) {
                Student s = new Student(rs.getLong("student_id"), rs.getString("name"), 0, "", "");
                students.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        combo.setItems(students);
        combo.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student s) {
                return s.getName() + " (ID: " + s.getStudentId() + ")";
            }

            @Override
            public Student fromString(String s) {
                return null;
            }
        });
    }

    private void loadCourses(ComboBox<Course> combo) {
        ObservableList<Course> courses = FXCollections.observableArrayList();
        try (Connection con = DBUtil.dbConnect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT course_id, name FROM COURSE ORDER BY name");
            while (rs.next()) {
                Course c = new Course(rs.getLong("course_id"), rs.getString("name"), 0);
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        combo.setItems(courses);
        combo.setConverter(new javafx.util.StringConverter<Course>() {
            @Override
            public String toString(Course c) {
                return c.getName() + " (ID: " + c.getCourseId() + ")";
            }

            @Override
            public Course fromString(String s) {
                return null;
            }
        });
    }

}
