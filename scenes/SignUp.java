package scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import classes.*;

public class SignUp {

    Student student = new Student(0, null, 0, null, null);
    private boolean passwordVisible = false;
    private PasswordField passwordField;
    private TextField passwordTextField;
    private ImageView eyeIcon;

    private boolean isNumberRegistered(String phone) {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE PHONE = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql2 = "SELECT COUNT(*) FROM LECTURER WHERE PHONE = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql2)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Scene getScene(Stage stage) {

        Label name = new Label("Name : ");
        Label password = new Label("Password : ");
        Label Birth_Date = new Label("Birth Date : ");
        Label PhoneNumber = new Label("Phone Number :");

        TextField PhoneField = new TextField();
        TextField nameField = new TextField();
        passwordField = new PasswordField();
        passwordTextField = new TextField();
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        nameField.setPromptText("Enter Your Name");
        passwordField.setPromptText("Enter Your Password");
        passwordTextField.setPromptText("Enter Your Password");
        PhoneField.setPromptText("Enter Your Phone Number");

        eyeIcon = new ImageView(new Image("file:./images/visible.png"));
        eyeIcon.setFitHeight(20);
        eyeIcon.setFitWidth(20);
        Button toggleBtn = new Button();
        toggleBtn.setGraphic(eyeIcon);
        toggleBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent; -fx-pref-height: 25px; -fx-pref-width: 25px;");
        toggleBtn.setOnAction(e -> togglePasswordVisibility());

        ToggleGroup tg = new ToggleGroup();
        RadioButton r1 = new RadioButton("Male");
        r1.setToggleGroup(tg);
        RadioButton r2 = new RadioButton("Female");
        r2.setToggleGroup(tg);

        DatePicker dp = new DatePicker();
        dp.setPromptText("MM/DD/YYYY");

        Button sign_up = new Button("Sign Up");
        sign_up.setId("signup");

        Button Back = new Button("Back");
        Back.setGraphic(Assistor.createIcon("./images/back.png", 24));

        GridPane g2 = new GridPane();

        g2.add(name, 0, 0);
        g2.add(password, 0, 1);
        g2.add(PhoneNumber, 0, 2);
        g2.add(r1, 0, 3);
        g2.add(Birth_Date, 0, 4);

        HBox passBox = new HBox(5, passwordField, passwordTextField, toggleBtn);

        g2.add(nameField, 1, 0);
        g2.add(passBox, 1, 1);
        g2.add(PhoneField, 1, 2);
        g2.add(r2, 1, 3);
        g2.add(dp, 1, 4);
        g2.add(sign_up, 1, 5);
        g2.add(Back, 1, 6);

        g2.setHgap(20);
        g2.setVgap(20);
        g2.setAlignment(Pos.CENTER);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\- ]*")) {
                nameField.setText(newValue.replaceAll("[^a-zA-Z\\- ]", ""));
            }
            if (newValue.startsWith(" ")) {
                nameField.setText(newValue.replaceFirst(" ", ""));
            }
            if (newValue.startsWith("-")) {
                nameField.setText(newValue.replaceFirst("-", ""));
            }
        });

        PhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                PhoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (PhoneField.getText().length() > 11) {
                String s = PhoneField.getText().substring(0, 11);
                PhoneField.setText(s);
            }
        });

        sign_up.setOnAction(e -> {
            String inputName = nameField.getText();
            String inputPassword = passwordVisible ? passwordTextField.getText() : passwordField.getText();

            String phone = PhoneField.getText();
            String gender = (tg.getSelectedToggle() != null) ? ((RadioButton) tg.getSelectedToggle()).getText() : "";
            String birth = (dp.getValue() != null) ? dp.getValue().toString() : "";

            if (inputName.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Name", "No Name has been entered.");
                return;
            } else if (inputPassword.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Password", "No Password has been entered.");
                return;
            } else if (phone.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Phone Number", "No Phone Number has been entered.");
                return;
            } else if (!phone.matches("\\d+")) {
                Assistor.showErrorAlert("Error", "Invalid Phone Number", "Phone number must contain only numbers.");
                return;
            } else if (phone.length() != 11) {
                Assistor.showErrorAlert("Error", "Invalid Phone Number", "Phone number must be 11 digits.");
                return;
            } else if (gender.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Gender", "No Gender has been selected.");
                return;
            } else if (birth.isEmpty()) {
                Assistor.showErrorAlert("Error", "No BirthDate", "No BirthDate has been entered.");
                return;
            } else if (isNumberRegistered(phone)) {
                Assistor.showErrorAlert("Error", "Phone Already Registered",
                        "This phone number is already registered. Please use another one.");
                return;
            }

            String sql = "INSERT INTO STUDENT (NAME, EMAIL, PASSWORD, PHONE, GENDER, BIRTH_DATE) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection con = DBUtil.dbConnect();
                    PreparedStatement pstmt = con.prepareStatement(sql, new String[] { "student_id" })) {

                String firstName = inputName.split(" ")[0].toLowerCase();

                pstmt.setString(1, inputName);
                pstmt.setString(2, "TEMP_EMAIL");
                pstmt.setString(3, inputPassword);
                pstmt.setString(4, phone);
                pstmt.setString(5, gender);
                pstmt.setDate(6, java.sql.Date.valueOf(dp.getValue()));

                pstmt.executeUpdate();

                long generatedId = 0;
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getLong(1);
                    }
                }

                String autoEmail = firstName + "." + generatedId + "@academix.edu";

                String updateSql = "UPDATE STUDENT SET EMAIL = ? WHERE STUDENT_ID = ?";
                try (PreparedStatement updatePstmt = con.prepareStatement(updateSql)) {
                    updatePstmt.setString(1, autoEmail);
                    updatePstmt.setLong(2, generatedId);
                    updatePstmt.executeUpdate();
                }

                Assistor.showSuccessAlert("Success", "Registration Successful",
                        "Registration Successful!\nWelcome to AcademiX, " + inputName + ".\n\nYour Student ID: "
                                + generatedId + "\nYour College Email: " + autoEmail);

                LogIn login = new LogIn();
                SceneAnimator.transition(stage, login.getScene(stage), () -> stage.setTitle("AcademiX | Login"));

            } catch (SQLException ex) {
                Assistor.showErrorAlert("Error", "Database Error", "Database Error: " + ex.getMessage());
                ex.printStackTrace();
            }

        });

        Back.setOnAction(e -> {
            HomePage page1 = new HomePage();
            SceneAnimator.transition(stage, page1.getScene(stage), () -> stage.setTitle("AcademiX | Welcome!"));
        });

        StackPane root = Assistor.createWithBackground(g2, 0.5);
        Scene scene = new Scene(root, 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");

        return scene;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIcon.setImage(new Image("file:./images/invisible.png"));
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            eyeIcon.setImage(new Image("file:./images/visible.png"));
        }
    }
}
