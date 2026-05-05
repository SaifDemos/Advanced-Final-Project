package scenes;

import javafx.stage.*;
import utility.Assistor;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.geometry.*;

public class HomePage {

    public Scene getScene(Stage stage) {

        Button btn_Login = new Button("Login");
        Button btn_Signup = new Button("Sign Up");
        Button btn_Exit = new Button("Exit");

        btn_Login.setGraphic(Assistor.createIcon("./images/login.png", 24));
        btn_Login.setContentDisplay(ContentDisplay.LEFT);
        btn_Login.setGraphicTextGap(8);

        btn_Signup.setGraphic(Assistor.createIcon("./images/signup.png", 24));
        btn_Signup.setContentDisplay(ContentDisplay.LEFT);
        btn_Signup.setGraphicTextGap(8);

        Image logoImg = new Image("file:./images/AcademiX.png");
        ImageView logoView = new ImageView(logoImg);
        logoView.setFitHeight(310);
        logoView.setPreserveRatio(true);

        GridPane g1 = new GridPane();
        g1.add(btn_Login, 0, 1);
        g1.add(btn_Signup, 1, 1);
        g1.add(btn_Exit, 0, 2);
        GridPane.setColumnSpan(btn_Exit, 2);
        GridPane.setHalignment(btn_Exit, HPos.CENTER);

        g1.setVgap(20);
        g1.setHgap(20);

        g1.setAlignment(Pos.CENTER);

        VBox Logo = new VBox(10);

        Image javafx = new Image("file:./images/AcademiX.png");
        ImageView javafxv = new ImageView(javafx);

        javafxv.setFitWidth(400);
        javafxv.setPreserveRatio(true);

        Logo.setAlignment(Pos.CENTER);
        Logo.getChildren().addAll(javafxv, g1);

        StackPane root = Assistor.createWithBackground(Logo, 0.5, 815, 665);

        btn_Signup.setOnAction(e -> {
            SignUp signupPage = new SignUp();
            SceneAnimator.transition(stage, signupPage.getScene(stage), () -> stage.setTitle("AcademiX | Signup"));
        });

        btn_Login.setOnAction(e -> {
            LogIn login = new LogIn();
            SceneAnimator.transition(stage, login.getScene(stage), () -> stage.setTitle("AcademiX | Login"));
        });

        btn_Exit.setOnAction(e -> {
            stage.close();
        });

        Scene scene = new Scene(root, 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");

        return scene;
    }

    public VBox getRootContent() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRootContent'");
    }
}