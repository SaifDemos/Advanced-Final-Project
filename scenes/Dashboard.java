package scenes;

import java.util.Optional;
import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.geometry.*;

import java.sql.*;

public class Dashboard {

    public Scene getScene(Stage stage) {

        ImageView studentIconView = Assistor.createIcon("./images/student.png", 100);
        ImageView lecturerIconView = Assistor.createIcon("./images/teacher.png", 100);
        ImageView courseIconView = Assistor.createIcon("./images/book.png", 100);

        Label studentCount = new Label("N/A");
        studentCount.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label studentLabel = new Label("Students");
        studentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox studentBox = new VBox(10);
        studentBox.setOpacity(0);
        studentBox.getChildren().addAll(studentIconView, studentCount, studentLabel);
        studentBox.setAlignment(Pos.CENTER);
        studentBox.setPrefSize(180, 200);
        studentBox.setStyle(
                "-fx-background-color: #1f1f1f; -fx-border-color: #2e2e2e; -fx-border-width: 1; -fx-padding: 20; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label lecturerCount = new Label("N/A");
        lecturerCount.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label lecturerLabel = new Label("Lecturers");
        lecturerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox lecturerBox = new VBox(10);
        lecturerBox.setOpacity(0);
        lecturerBox.getChildren().addAll(lecturerIconView, lecturerCount, lecturerLabel);
        lecturerBox.setAlignment(Pos.CENTER);
        lecturerBox.setPrefSize(180, 200);
        lecturerBox.setStyle(
                "-fx-background-color: #1f1f1f; -fx-border-color: #2e2e2e; -fx-border-width: 1; -fx-padding: 20; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label courseCount = new Label("N/A");
        courseCount.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label courseLabel = new Label("Courses");
        courseLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox courseBox = new VBox(10);
        courseBox.setOpacity(0);
        courseBox.getChildren().addAll(courseIconView, courseCount, courseLabel);
        courseBox.setAlignment(Pos.CENTER);
        courseBox.setPrefSize(180, 200);
        courseBox.setStyle(
                "-fx-background-color: #1f1f1f; -fx-border-color: #2e2e2e; -fx-border-width: 1; -fx-padding: 20; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox statsBox = new HBox(30);
        statsBox.getChildren().addAll(studentBox, lecturerBox, courseBox);
        statsBox.setAlignment(Pos.CENTER);

        Button mng_stu = new Button("Manage Students");
        mng_stu.setStyle("-fx-pref-width: 200px");
        Button mng_lect = new Button("Manage Lecturers");
        mng_lect.setStyle("-fx-pref-width: 200px");
        Button mng_courses = new Button("Manage Courses");
        mng_courses.setStyle("-fx-pref-width: 200px");
        Button enroll_student = new Button("Enroll Course to Student");
        enroll_student.setStyle("-fx-pref-width: 200px");
        Button enroll_lecturer = new Button("Enroll Lecturer to Course");
        enroll_lecturer.setStyle("-fx-pref-width: 200px");
        Button aboutApp = new Button("About");
        aboutApp.setStyle("-fx-pref-width: 200px");
        Button logout = new Button("Log Out");
        logout.setId("logout-btn");
        logout.setStyle("-fx-pref-width: 200px");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(20);
        buttonGrid.setAlignment(Pos.CENTER);

        buttonGrid.add(mng_stu, 0, 0);
        buttonGrid.add(mng_lect, 1, 0);
        buttonGrid.add(mng_courses, 0, 1);
        buttonGrid.add(enroll_student, 1, 1);
        buttonGrid.add(enroll_lecturer, 0, 2);
        buttonGrid.add(aboutApp, 1, 2);
        buttonGrid.add(logout, 0, 3, 2, 1);
        GridPane.setHalignment(logout, HPos.CENTER);

        logout.setOnAction(e -> {
            Optional<ButtonType> result = Assistor.showConfirmationAlert("Confirmation", "Are you sure?!",
                    "Do you really want to log out of your account?");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                LogIn signIn = new LogIn();
                SceneAnimator.transition(stage, signIn.getScene(stage), () -> stage.setTitle("AcademiX | Login"));
            }
        });

        aboutApp.setOnAction(e -> {
            About about = new About();
            SceneAnimator.transition(stage, about.getScene(stage), () -> stage.setTitle("AcademiX | Help"));
        });

        mng_stu.setOnAction(e -> {
            ManageStudents manageStudents = new ManageStudents();
            SceneAnimator.transition(stage, manageStudents.getScene(stage),
                    () -> stage.setTitle("AcademiX | Student Management"));
        });

        mng_lect.setOnAction(e -> {
            ManageLecturer manageLecturer = new ManageLecturer();
            SceneAnimator.transition(stage, manageLecturer.getScene(stage),
                    () -> stage.setTitle("AcademiX | Lecturer Management"));
        });

        mng_courses.setOnAction(e -> {
            ManageCourses manageCourses = new ManageCourses();
            SceneAnimator.transition(stage, manageCourses.getScene(stage),
                    () -> stage.setTitle("AcademiX | Course Management"));
        });

        enroll_student.setOnAction(e -> {
            EnrollStudent enrollStudent = new EnrollStudent();
            SceneAnimator.transition(stage, enrollStudent.getScene(stage),
                    () -> stage.setTitle("AcademiX | Enroll Student"));
        });

        enroll_lecturer.setOnAction(e -> {
            EnrollLecturer enrollLecturer = new EnrollLecturer();
            SceneAnimator.transition(stage, enrollLecturer.getScene(stage),
                    () -> stage.setTitle("AcademiX | Assign Lecturer"));
        });

        loadStatsCounts(studentCount, lecturerCount, courseCount);

        VBox mainLayout = new VBox(30);
        mainLayout.getChildren().addAll(statsBox, buttonGrid);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(30, 20, 20, 20));

        StackPane root = Assistor.createWithBackground(mainLayout, 0.2);
        Scene scene = new Scene(root, 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");

        javafx.util.Duration delay = javafx.util.Duration.millis(150);
        SceneAnimator.fadeInStaggered(delay, studentBox, lecturerBox, courseBox);

        return scene;
    }

    private void loadStatsCounts(Label studentCount, Label lecturerCount, Label courseCount) {
        try (Connection con = DBUtil.dbConnect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM STUDENT");
            if (rs.next()) {
                studentCount.setText(String.valueOf(rs.getInt(1)));
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM LECTURER");
            if (rs.next()) {
                lecturerCount.setText(String.valueOf(rs.getInt(1)));
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM COURSE");
            if (rs.next()) {
                courseCount.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}