package com.connectfour;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class LobbyController {

    private final Client client;
    private final String username;
    private String player1;
    private String player2;

    private Label player1Label;
    private Label player2Label;
    private Label statusLabel;

    public LobbyController(Client client, String username) {
        this.client = client;
        this.username = username;
    }

    public void show(Stage stage) {
        player1 = username;

        VBox root = new VBox(15);
        root.setPrefSize(400, 300);
        root.setStyle("-fx-alignment: center;");

        player1Label = new Label("Player 1: " + player1);
        player2Label = new Label(""); // Initially empty
        statusLabel = new Label("Waiting for player...");

        root.getChildren().addAll(player1Label, player2Label, statusLabel);

        Scene scene = new Scene(root);
        stage.setTitle("Lobby");
        stage.setScene(scene);
        stage.show();

        // Listen for pairing messages
        client.setMessageCallback(message -> {
            Platform.runLater(() -> {
                if (message.startsWith("PLAYER2_JOINED:")) {
                    String player2 = message.substring("PLAYER2_JOINED:".length());
                    player2Label.setText("Player 2: " + player2);
                    statusLabel.setText("Game starting!");
                    startCountdown(stage);
                } else if (message.startsWith("PLAYER1_JOINED:")) {
                    String player1 = message.substring("PLAYER1_JOINED:".length());
                    player1Label.setText("Player 1: " + player1);
                    player2Label.setText("Player 2: " + username);
                    statusLabel.setText("Game starting!");
                    startCountdown(stage);
                }
            });
        });

        // Tell server this client is ready for the lobby
        client.send("LOBBY_READY:" + username);
    }

    private void startCountdown(Stage stage) {
        Timeline timeline = new Timeline();
        int[] countdown = {3};

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            if (countdown[0] > 0) {
                statusLabel.setText("Starting in " + countdown[0] + "...");
                countdown[0]--;
            } else {
                statusLabel.setText("Game starting!");
                timeline.stop();

                // TODO: Transition to game board (Scene 3)
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(4);
        timeline.play();
    }
}
