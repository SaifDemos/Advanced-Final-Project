package scenes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.HBox;
import javafx.geometry.*;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class LogIn {

    private static final Map<String, String> otpStorage = new HashMap<>();
    private static final Random random = new Random();
    private boolean passwordVisible = false;
    private PasswordField passwordField;
    private TextField passwordTextField;
    private ImageView eyeIcon;

    public Scene getScene(Stage stage) {

        Label email_1 = new Label("Email / ID :");
        Label password_1 = new Label("Password :");

        TextField emailField = new TextField();
        passwordField = new PasswordField();
        passwordTextField = new TextField();
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        emailField.setPromptText("Enter Your Email or ID");
        passwordField.setPromptText("Enter Your Password");
        passwordTextField.setPromptText("Enter Your Password");

        eyeIcon = new ImageView(new Image("file:./images/visible.png"));
        eyeIcon.setFitHeight(20);
        eyeIcon.setFitWidth(20);
        Button toggleBtn = new Button();
        toggleBtn.setGraphic(eyeIcon);
        toggleBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent; -fx-pref-height: 25px; -fx-pref-width: 25px;");
        toggleBtn.setOnAction(e -> togglePasswordVisibility());

        Button login = new Button("Login");
        Button Back = new Button("Back");
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");

        login.setGraphic(Assistor.createIcon("./images/login.png", 24));
        login.setContentDisplay(ContentDisplay.LEFT);
        login.setGraphicTextGap(8);

        Back.setGraphic(Assistor.createIcon("./images/back.png", 24));

        GridPane g3 = new GridPane();

        g3.add(email_1, 0, 0);
        g3.add(emailField, 1, 0);
        HBox passBox = new HBox(5, passwordField, passwordTextField, toggleBtn);

        g3.add(password_1, 0, 1);
        g3.add(passBox, 1, 1);
        g3.add(forgotPasswordLink, 1, 2);
        g3.add(login, 1, 3);
        g3.add(Back, 1, 4);

        g3.setVgap(20);
        g3.setHgap(20);

        g3.setAlignment(Pos.CENTER);

        login.setOnAction(eventAction -> {
            String input = emailField.getText();
            String inputPassword = passwordVisible ? passwordTextField.getText() : passwordField.getText();

            if (input.isEmpty() || inputPassword.isEmpty()) {
                Assistor.showErrorAlert("Error", "Missing Input", "Please Enter both Email/ID and Password.");
                return;
            }

            try (Connection con = DBUtil.dbConnect()) {
                String userEmail = null;
                boolean isStudent = false;

                // 1. Try ADMIN by EMAIL
                String adminSql = "SELECT * FROM ADMINS WHERE EMAIL = ? AND PASSWORD = ?";
                try (PreparedStatement adminPstmt = con.prepareStatement(adminSql)) {
                    adminPstmt.setString(1, input);
                    adminPstmt.setString(2, inputPassword);
                    ResultSet rsAdmin = adminPstmt.executeQuery();

                    if (rsAdmin.next()) {
                        Dashboard dashBoard = new Dashboard();
                        SceneAnimator.transition(stage, dashBoard.getScene(stage),
                                () -> stage.setTitle("AcademiX | Admin Dashboard"));
                        return;
                    }
                }

                // 2. Try ADMIN by ADMIN_ID
                try {
                    long adminId = Long.parseLong(input);
                    String adminSqlById = "SELECT * FROM ADMINS WHERE ADMIN_ID = ? AND PASSWORD = ?";
                    try (PreparedStatement adminPstmt = con.prepareStatement(adminSqlById)) {
                        adminPstmt.setLong(1, adminId);
                        adminPstmt.setString(2, inputPassword);
                        ResultSet rsAdmin = adminPstmt.executeQuery();

                        if (rsAdmin.next()) {
                            Dashboard dashBoard = new Dashboard();
                            SceneAnimator.transition(stage, dashBoard.getScene(stage),
                                    () -> stage.setTitle("AcademiX | Admin Dashboard"));
                            return;
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Input is not a number, skip ADMIN_ID lookup
                }

                // 3. Try STUDENT by EMAIL
                String studentSql = "SELECT * FROM STUDENT WHERE EMAIL = ? AND PASSWORD = ?";
                try (PreparedStatement studentPstmt = con.prepareStatement(studentSql)) {
                    studentPstmt.setString(1, input);
                    studentPstmt.setString(2, inputPassword);
                    ResultSet rsStudent = studentPstmt.executeQuery();

                    if (rsStudent.next()) {
                        userEmail = rsStudent.getString("EMAIL");
                        isStudent = true;
                    }
                }

                // 4. If not found by email, try by STUDENT_ID
                if (!isStudent) {
                    try {
                        long studentId = Long.parseLong(input);
                        String studentSqlById = "SELECT * FROM STUDENT WHERE STUDENT_ID = ? AND PASSWORD = ?";
                        try (PreparedStatement studentPstmt = con.prepareStatement(studentSqlById)) {
                            studentPstmt.setLong(1, studentId);
                            studentPstmt.setString(2, inputPassword);
                            ResultSet rsStudent = studentPstmt.executeQuery();

                            if (rsStudent.next()) {
                                userEmail = rsStudent.getString("EMAIL");
                                isStudent = true;
                            }
                        }
                    } catch (NumberFormatException ex) {
                        // Input is not a number, skip STUDENT_ID lookup
                    }
                }

                // 5. If student found, go to profile
                if (isStudent && userEmail != null) {
                    StudentProfile studentprofile = new StudentProfile(userEmail);
                    SceneAnimator.transition(stage, studentprofile.getScene(stage),
                            () -> stage.setTitle("AcademiX | My Profile"));
                    return;
                }

                // 6. No user found
                Assistor.showErrorAlert("Error", "Invalid Credentials", "Invalid Email or Password");

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Back.setOnAction(e -> {
            HomePage homePage = new HomePage();
            SceneAnimator.transition(stage, homePage.getScene(stage), () -> stage.setTitle("AcademiX | Welcome!"));
        });

        forgotPasswordLink.setOnAction(e -> showPhoneDialog(stage));

        StackPane root = Assistor.createWithBackground(g3, 0.5);
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

    private void showPhoneDialog(Stage ownerStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Forgot Password - Step1");
        dialog.setResizable(false);

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter your 11-digit phone number");

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                phoneField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (phoneField.getText().length() > 11) {
                phoneField.setText(phoneField.getText().substring(0, 11));
            }
        });

        Button sendOtp = new Button("Send OTP");
        Button cancel = new Button("Cancel");

        sendOtp.setOnAction(e -> {
            String phone = phoneField.getText();
            if (phone.isEmpty() || phone.length() != 11) {
                Assistor.showErrorAlert("Error", "Invalid Phone", "Phone number must be exactly 11 digits.");
                return;
            }

            sendOtp.setDisable(true);

            Task<String[]> task = new Task<>() {
                @Override
                protected String[] call() throws Exception {
                    try (Connection con = DBUtil.dbConnect()) {
                        String sql = "SELECT EMAIL, NAME FROM STUDENT WHERE PHONE = ?";
                        PreparedStatement pstmt = con.prepareStatement(sql);
                        pstmt.setString(1, phone);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            return new String[] { rs.getString("EMAIL"), rs.getString("NAME") };
                        }
                        return null;
                    }
                }
            };

            task.setOnSucceeded(event -> {
                sendOtp.setDisable(false);
                if (task.getValue() != null) {
                    String studentName = task.getValue()[1];
                    String otp = String.valueOf(100000 + random.nextInt(900000));
                    String formattedOtp = otp.substring(0, 3) + "-" + otp.substring(3);
                    otpStorage.put(phone, otp);

                    String message = "AcademiX: Hi " + studentName + ", Your OTP code to reset your password is "
                            + formattedOtp;

                    new Thread(() -> {
                        try (Socket s = new Socket("localhost", 12345);
                                PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                            out.println(message);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();

                    dialog.close();
                    System.out.println("Scheduling OTP dialog...");
                    Platform.runLater(() -> {
                        try {
                            System.out.println("Showing OTP dialog now...");
                            Stage otpDialog = new Stage();
                            otpDialog.setTitle("Forgot Password - Step 2");
                            otpDialog.setResizable(false);

                            Label otpLabel = new Label("Enter OTP:");
                            Label passLabel = new Label("New Password:");
                            Label confirmLabel = new Label("Confirm Password:");

                            TextField otpField = new TextField();
                            otpField.setPromptText("6-digit code from SMS");

                            otpField.textProperty().addListener((observable, oldValue, newValue) -> {
                                if (!newValue.matches("\\d*")) {
                                    otpField.setText(newValue.replaceAll("[^\\d]", ""));
                                }
                                if (otpField.getText().length() > 6) {
                                    String s = otpField.getText().substring(0, 6);
                                    otpField.setText(s);
                                }
                            });

                            PasswordField newPassField = new PasswordField();
                            newPassField.setPromptText("Enter new password");

                            PasswordField confirmPassField = new PasswordField();
                            confirmPassField.setPromptText("Confirm new password");

                            Button resetBtn = new Button("Reset Password");
                            Button cancelBtn = new Button("Cancel");

                            resetBtn.setOnAction(ev -> {
                                String otpVal = otpField.getText();
                                String newPass = newPassField.getText();
                                String confirmPass = confirmPassField.getText();

                                if (otpVal.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                                    Assistor.showErrorAlert("Error", "Missing Input", "All fields are required.");
                                    return;
                                }

                                if (!newPass.equals(confirmPass)) {
                                    Assistor.showErrorAlert("Error", "Password Mismatch", "Passwords do not match.");
                                    return;
                                }

                                String storedOtp = otpStorage.get(phone);
                                if (storedOtp == null || !storedOtp.equals(otpVal)) {
                                    Assistor.showErrorAlert("Error", "Invalid OTP",
                                            "The OTP you entered is incorrect.");
                                    return;
                                }

                                resetBtn.setDisable(true);

                                Task<Void> resetTask = new Task<>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        try (Connection con = DBUtil.dbConnect()) {
                                            String sql = "UPDATE STUDENT SET PASSWORD = ? WHERE PHONE = ?";
                                            PreparedStatement pstmt = con.prepareStatement(sql);
                                            pstmt.setString(1, newPass);
                                            pstmt.setString(2, phone);
                                            pstmt.executeUpdate();
                                        }
                                        return null;
                                    }
                                };

                                resetTask.setOnSucceeded(ev2 -> {
                                    otpStorage.remove(phone);
                                    Assistor.showSuccessAlert("Success", "Password Reset",
                                            "Your password has been reset successfully.");
                                    otpDialog.close();
                                });

                                resetTask.setOnFailed(ev2 -> {
                                    resetBtn.setDisable(false);
                                    Assistor.showErrorAlert("Error", "Database Error",
                                            "Failed to reset password: " + resetTask.getException().getMessage());
                                });

                                new Thread(resetTask).start();
                            });

                            cancelBtn.setOnAction(ev -> otpDialog.close());

                            GridPane grid = new GridPane();
                            grid.add(otpLabel, 0, 0);
                            grid.add(otpField, 1, 0);
                            grid.add(passLabel, 0, 1);
                            grid.add(newPassField, 1, 1);
                            grid.add(confirmLabel, 0, 2);
                            grid.add(confirmPassField, 1, 2);
                            grid.add(resetBtn, 0, 3);
                            grid.add(cancelBtn, 1, 3);
                            grid.setVgap(15);
                            grid.setHgap(15);
                            grid.setAlignment(Pos.CENTER);
                            grid.setPadding(new Insets(30));

                            Scene dialogScene = new Scene(grid);
                            dialogScene.getStylesheets().add("file:./assets/style.css");
                            otpDialog.setScene(dialogScene);
                            System.out.println("OTP dialog: about to call showAndWait()");
                            otpDialog.showAndWait();
                            System.out.println("OTP dialog: showAndWait() returned");
                        } catch (Exception ex) {
                            System.err.println("Error in OTP dialog: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });
                } else {
                    Assistor.showErrorAlert("Error", "Phone Not Found", "This phone number is not registered.");
                }
            });

            task.setOnFailed(event -> {
                sendOtp.setDisable(false);
                Assistor.showErrorAlert("Error", "Database Error",
                        "Failed to check phone: " + task.getException().getMessage());
            });

            new Thread(task).start();
        });

        cancel.setOnAction(e -> dialog.close());

        GridPane grid = new GridPane();
        grid.add(phoneLabel, 0, 0);
        grid.add(phoneField, 1, 0);
        grid.add(sendOtp, 0, 1);
        grid.add(cancel, 1, 1);
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(30));

        Scene dialogScene = new Scene(grid);
        dialogScene.getStylesheets().add("file:./assets/style.css");
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
}
