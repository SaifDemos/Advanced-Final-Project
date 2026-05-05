package utility;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneAnimator {

    private static final Duration DURATION = Duration.millis(150);

    public static void animateAlert(Alert alert) {
        Scene alertScene = alert.getDialogPane().getScene();
        if (alertScene == null)
            return;

        Node root = alertScene.getRoot();
        if (root == null)
            return;

        root.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(DURATION, root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public static void transition(Stage stage, Scene newScene, Runnable onComplete) {
        newScene.setFill(Color.web("#121212"));

        Scene currentScene = stage.getScene();
        if (currentScene == null) {
            stage.setScene(newScene);
            if (onComplete != null)
                onComplete.run();
            return;
        }

        Node currentRoot = currentScene.getRoot();
        if (currentRoot == null) {
            stage.setScene(newScene);
            if (onComplete != null)
                onComplete.run();
            return;
        }

        FadeTransition fadeOut = new FadeTransition(DURATION, currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            stage.setScene(newScene);
            transitionIn(newScene.getRoot());
            if (onComplete != null)
                onComplete.run();
        });

        fadeOut.play();
    }

    public static void transitionIn(Node root) {
        if (root == null)
            return;

        root.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(DURATION, root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeIn.play();
    }

    public static void fadeIn(Node node) {
        fadeIn(node, Duration.millis(400));
    }

    public static void fadeIn(Node node, Duration duration) {
        if (node == null)
            return;
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public static void fadeInStaggered(Duration delay, Node... nodes) {
        if (nodes == null)
            return;
        Duration defaultDuration = Duration.millis(400);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (node == null)
                continue;
            node.setOpacity(0);
            FadeTransition ft = new FadeTransition(defaultDuration, node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(i * delay.toMillis()));
            ft.play();
        }
    }
}