package scenes;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Platform;

public class SMSApp extends Application {
    private ObservableList<String> messages = FXCollections.observableArrayList();
    private ServerSocket serverSocket;
    private Scene listScene;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SMS");
        Image icon = new Image("file:./images/AcademiX-Icon.png");
        primaryStage.getIcons().add(icon);

        Label header = new Label("SMS");
        header.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        header.setPadding(new Insets(20, 0, 10, 0));

        HBox headerBox = new HBox(header);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle("-fx-background-color: #121212;");

        Image cellIcon = new Image("file:./images/AcademiX-Icon.png");

        ListView<String> listView = new ListView<>(messages);
        listView.setCellFactory(param -> new ListCell<>() {
            private ImageView iconView = new ImageView(cellIcon);
            private Label senderLabel = new Label();
            private Label messageLabel = new Label();
            private VBox vbox = new VBox(senderLabel, messageLabel);
            private HBox cellBox = new HBox(10, iconView, vbox);

            {
                iconView.setFitHeight(24);
                iconView.setFitWidth(24);
                senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
                messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: lightgray;");
                vbox.setSpacing(2);
                cellBox.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String[] parts = item.split(": ", 2);
                    if (parts.length == 2) {
                        senderLabel.setText(parts[0]);
                        String msg = parts[1];
                        messageLabel.setText(msg.length() > 50 ? msg.substring(0, 47) + "..." : msg);
                    } else {
                        senderLabel.setText("AcademiX");
                        messageLabel.setText(item);
                    }
                    setGraphic(cellBox);
                }
                setStyle(
                        "-fx-background-color: #121212; -fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0; -fx-padding: 10;");
            }
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showMessageScene(primaryStage, selected, listScene);
                }
            }
        });

        listView.setStyle("-fx-background-color: #121212; -fx-text-fill: white;");

        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        root.setCenter(listView);
        root.setStyle("-fx-background-color: #121212;");

        listScene = new Scene(root, 400, 600);
        listScene.setFill(Color.web("#121212"));
        listScene.getStylesheets().add("file:./assets/style.css");
        primaryStage.setScene(listScene);
        primaryStage.setResizable(false);
        primaryStage.show();

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345);
                System.out.println("SMS Service listening on port 12345");
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message = in.readLine();
                    if (message != null) {
                        Platform.runLater(() -> messages.add(0, message));
                    }
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void stop() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showMessageScene(Stage stage, String message, Scene listScene) {
        Image icon = new Image("file:./images/AcademiX-Icon.png");
        ImageView iconView = new ImageView(icon);
        iconView.setFitHeight(48);
        iconView.setFitWidth(48);

        Label senderLabel = new Label("AcademiX");
        senderLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        String body = message.contains(": ") ? message.split(": ", 2)[1] : message;
        Label bodyLabel = new Label(body);
        bodyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: lightgray; -fx-wrap-text: true;");
        bodyLabel.setMaxWidth(350);

        VBox msgBox = new VBox(10, iconView, senderLabel, bodyLabel);
        msgBox.setPadding(new Insets(20));
        msgBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8;");
        VBox.setMargin(iconView, new Insets(0, 0, 10, 0));

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> stage.setScene(listScene));

        VBox detailRoot = new VBox(20, msgBox, backBtn);
        detailRoot.setPadding(new Insets(20));
        detailRoot.setStyle("-fx-background-color: #121212;");
        detailRoot.setAlignment(Pos.TOP_CENTER);

        Scene detailScene = new Scene(detailRoot, 400, 600);
        detailScene.setFill(Color.web("#121212"));
        detailScene.getStylesheets().add("file:./assets/style.css");
        stage.setScene(detailScene);
    }
}
