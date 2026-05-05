package scenes;

import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import classes.Collage;
import classes.Department;
import java.sql.*;
import java.util.List;

public class About {
    public Scene getScene(Stage stage) {

        Collage collage = new Collage();
        loadStatsCounts(collage);

        // College title
        Label collegeTitle = new Label(collage.getCollageName());
        collegeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label universityLabel = new Label(collage.getUniversityName());
        universityLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa;");

        // Stats boxes (horizontal row)
        HBox statsRow = new HBox(20,
                createStatBox(collage.getNumberOfStudents(), "Students"),
                createStatBox(collage.getNumberOfDoctors(), "Lecturers"),
                createStatBox(collage.getNumberOfCourses(), "Courses"));
        statsRow.setAlignment(Pos.CENTER);

        // Details section
        VBox detailsBox = new VBox(8);
        detailsBox.setStyle(
                "-fx-background-color: #1f1f1f; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #2e2e2e; -fx-border-radius: 8;");
        detailsBox.getChildren().addAll(
                createDetailRow("Dean", collage.getDeanName()),
                createDetailRow("Address", collage.getAddress()),
                createDetailRow("Established", String.valueOf(collage.getEstablishedYear())));

        // Departments section
        Label deptTitle = new Label("Departments");
        deptTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox deptList = new VBox(5);
        List<Department> depts = collage.getDepartments();
        for (Department d : depts) {
            Label deptLabel = new Label("• " + d.toString());
            deptLabel.setStyle("-fx-text-fill: #cccccc;");
            deptList.getChildren().add(deptLabel);
        }

        ScrollPane deptScroll = new ScrollPane(deptList);
        deptScroll.setFitToWidth(true);
        deptScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        deptScroll.setPrefHeight(120);

        // Back button
        Button Back = new Button("Back");
        Back.setStyle("-fx-translate-y: 15px;");

        Back.setOnAction(e -> {
            Dashboard dashboard = new Dashboard();
            SceneAnimator.transition(stage, dashboard.getScene(stage),
                    () -> stage.setTitle("AcademiX | Admin Dashboard"));
        });

        VBox mainLayout = new VBox(15);
        mainLayout.getChildren().addAll(collegeTitle, universityLabel, detailsBox, statsRow, deptTitle, deptScroll,
                Back);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(Assistor.createWithBackground(mainLayout, 0.2, 815, 710), 815, 650);
        scene.getStylesheets().add("file:./assets/style.css");

        return scene;
    }

    private VBox createStatBox(int count, String label) {
        Label countLabel = new Label(String.valueOf(count));
        countLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        VBox box = new VBox(8, countLabel, descLabel);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(120, 80);
        box.setStyle(
                "-fx-background-color: #1f1f1f; -fx-border-color: #2e2e2e; -fx-border-width: 1; -fx-padding: 10; -fx-background-radius: 8; -fx-border-radius: 8;");
        return box;
    }

    private HBox createDetailRow(String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaaaaa;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: white;");
        HBox row = new HBox(10, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void loadStatsCounts(Collage collage) {
        try (Connection con = DBUtil.dbConnect()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM STUDENT");
            if (rs.next()) {
                collage.setNumberOfStudents(rs.getInt(1));
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM LECTURER");
            if (rs.next()) {
                collage.setNumberOfDoctors(rs.getInt(1));
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM COURSE");
            if (rs.next()) {
                collage.setNumberOfCourses(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}