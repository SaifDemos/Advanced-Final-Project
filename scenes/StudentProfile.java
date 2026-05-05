package scenes;

import java.sql.*;
import java.util.Optional;

import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

public class StudentProfile {

    private String userEmail; // To store who logged in
    private String userId; // To store the logged in user's ID

    // Update constructor to receive the email from LogIn scene
    public StudentProfile(String email) {
        this.userEmail = email;
    }

    public Scene getScene(Stage stage) {
        Label welcome = new Label("Welcome back!");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label name = new Label("Name :");
        Label txtname = new Label("Loading...");

        Label id = new Label("Id :");
        Label txtid = new Label("Loading...");

        Label major = new Label("Major :");
        Label txtmajor = new Label("Loading...");

        Label minor = new Label("Minor :");
        Label txtminor = new Label("Loading...");

        Label cgpa = new Label("CGPA :");
        Label txtcgpa = new Label("Loading...");

        Button Info = new Button("Account Info");

        Button logout = new Button(" Log Out", Assistor.createIcon("./images/logout.png", 24));
        logout.setId("logout-btn");

        // --- Fetch Real Data ---
        loadDataFromDB(txtname, txtid, txtmajor, txtminor, txtcgpa, welcome);

// --- Layout ---
        GridPane g4 = new GridPane();
        g4.add(welcome, 0, 0, 2, 1);
        GridPane.setHalignment(welcome, HPos.CENTER);

        g4.add(name, 0, 1);
        g4.add(txtname, 1, 1);
        g4.add(id, 0, 2);
        g4.add(txtid, 1, 2);
        g4.add(major, 0, 3);
        g4.add(txtmajor, 1, 3);
        g4.add(minor, 0, 4);
        g4.add(txtminor, 1, 4);
        g4.add(cgpa, 0, 5);
        g4.add(txtcgpa, 1, 5);
        g4.add(Info, 0, 6);
        g4.add(logout, 1, 6);

        g4.setHgap(15);
        g4.setVgap(15);
        g4.setAlignment(Pos.CENTER);

        // --- Actions ---
        Info.setOnAction(e -> {
            AccountInfo accountInfo = new AccountInfo(userEmail, userId);
            SceneAnimator.transition(stage, accountInfo.getScene(stage), () -> stage.setTitle("AcademiX | Account Info"));
        });

        logout.setOnAction(e -> {
            Optional<ButtonType> result = Assistor.showConfirmationAlert("Confirmation", "Are you sure?!", "Do you really want to log out of your account?");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                LogIn signIn = new LogIn();
                SceneAnimator.transition(stage, signIn.getScene(stage), () -> stage.setTitle("AcademiX | Login"));
            }
        });

        Scene scene = new Scene(Assistor.createWithBackground(g4, 0.2, 815, 665), 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");
        return scene;
    }

    private void loadDataFromDB(Label n, Label i, Label ma, Label mi, Label c, Label w) {
        String sql = "SELECT * FROM STUDENT WHERE EMAIL = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String studentName = rs.getString("NAME");
                n.setText(studentName);
                userId = String.valueOf(rs.getLong("student_id"));
                i.setText(userId);
                ma.setText(rs.getString("MAJOR"));
                mi.setText(rs.getString("MINOR"));
                c.setText(String.format("%.2f", rs.getFloat("CGPA")));
                w.setText("Welcome back, " + studentName + "!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}