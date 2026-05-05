import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import scenes.HomePage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        
        HomePage homePage = new HomePage();

        Image icon = new Image(getClass().getResourceAsStream("./images/AcademiX-Icon.png"));

        Font.loadFont(getClass().getResourceAsStream("./fonts/Poppins-Regular.ttf"),14);
        
        Scene initialScene = homePage.getScene(primaryStage);
        initialScene.setFill(Color.web("#121212"));
        
        primaryStage.setScene(initialScene);
        primaryStage.setTitle("AcademiX | Welcome!");
        primaryStage.getIcons().add(icon);
        primaryStage.setResizable(false);
        primaryStage.show();
        
    }   

    public static void main(String[] args) {
        launch(args);
    }
}