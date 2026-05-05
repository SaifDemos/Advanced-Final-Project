package scenes;

import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import classes.Course;
import classes.Lecturer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class EnrollLecturer {
    public Scene getScene(Stage stage) {

        Label title = new Label("Assign Lecturer to Course");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Select Course");
        courseCombo.setPrefWidth(300);

        ComboBox<Lecturer> lecturerCombo = new ComboBox<>();
        lecturerCombo.setPromptText("Select Lecturer");
        lecturerCombo.setPrefWidth(300);

        loadCourses(courseCombo);
        loadLecturers(lecturerCombo);

        Button assignBtn = new Button("Assign");
        assignBtn.setStyle("-fx-pref-width: 200px;");

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-pref-width: 200px;");

        assignBtn.setOnAction(e -> {
            Course c = courseCombo.getValue();
            Lecturer l = lecturerCombo.getValue();

            if (c == null || l == null) {
                Assistor.showWarningAlert("Missing Selection", "Missing Selection",
                        "Please select both course and lecturer.");
                return;
            }

            try (Connection con = DBUtil.dbConnect()) {
                // Check if already assigned
                PreparedStatement checkStmt = con.prepareStatement(
                        "SELECT 1 FROM COURSE_LECTURER WHERE course_id = ? AND lecturer_id = ?");
                checkStmt.setLong(1, c.getCourseId());
                checkStmt.setLong(2, l.getLecturerId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    Assistor.showErrorAlert("Already Assigned", "Assignment Error",
                            "Lecturer " + l.getName() + " is already assigned to " + c.getName() + ".");
                    return;
                }

                // Proceed with assignment
                PreparedStatement pstmt = con.prepareStatement(
                        "INSERT INTO COURSE_LECTURER (course_id, lecturer_id) VALUES (?, ?)");
                pstmt.setLong(1, c.getCourseId());
                pstmt.setLong(2, l.getLecturerId());
                pstmt.executeUpdate();

                // Also add to ArrayLists in memory
                c.assignLecturer(l);

                Assistor.showSuccessAlert("Success", "Assignment Complete",
                        l.getName() + " successfully assigned to " + c.getName() + "!");

                courseCombo.getSelectionModel().clearSelection();
                lecturerCombo.getSelectionModel().clearSelection();

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
        layout.getChildren().addAll(title, courseCombo, lecturerCombo, assignBtn, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(Assistor.createWithBackground(layout, 0.2, 815, 710), 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");
        return scene;
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

    private void loadLecturers(ComboBox<Lecturer> combo) {
        ObservableList<Lecturer> lecturers = FXCollections.observableArrayList();
        try (Connection con = DBUtil.dbConnect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT lecturer_id, name FROM LECTURER ORDER BY name");
            while (rs.next()) {
                Lecturer l = new Lecturer(rs.getLong("lecturer_id"), rs.getString("name"), "", "", "", 0);
                lecturers.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        combo.setItems(lecturers);
        combo.setConverter(new javafx.util.StringConverter<Lecturer>() {
            @Override
            public String toString(Lecturer l) {
                return l.getName() + " (ID: " + l.getLecturerId() + ")";
            }

            @Override
            public Lecturer fromString(String s) {
                return null;
            }
        });
    }
}
