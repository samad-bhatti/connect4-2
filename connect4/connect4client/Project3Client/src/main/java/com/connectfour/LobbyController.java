package com.connectfour;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.Pos;

public class LobbyController {

    private final Client client;
    private final String username;
    private String player1;
    private String player2;

    private Label player1Label;
    private Label player2Label;
    private Label statusLabel;

    // 🎨 Connect Four style
    private static final String BACKGROUND_STYLE = "-fx-background-color: #d0e7ff;";
    private static final String TITLE_STYLE = "-fx-font-family: 'Arial'; -fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #003366;";
    private static final String PLAYER1_STYLE = "-fx-font-family: 'Arial'; -fx-font-size: 18px; -fx-text-fill: #cc0000;"; // 🔴 Red
    private static final String PLAYER2_STYLE = "-fx-font-family: 'Arial'; -fx-font-size: 18px; -fx-text-fill: #FFD700;"; // 🟡 Yellow
    private static final String STATUS_STYLE = "-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-text-fill: #555;";

    public LobbyController(Client client, String username) {
        this.client = client;
        this.username = username;
    }

    public void show(Stage stage) {
        player1 = username;

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(600, 400);
        root.setStyle(BACKGROUND_STYLE);

        Label title = new Label("Lobby");
        title.setStyle(TITLE_STYLE);

        player1Label = new Label("Player 1: " + player1);
        player1Label.setStyle(PLAYER1_STYLE);

        player2Label = new Label("");
        player2Label.setStyle(PLAYER2_STYLE); // Set initial style

        statusLabel = new Label("Waiting for player...");
        statusLabel.setStyle(STATUS_STYLE);

        root.getChildren().addAll(title, player1Label, player2Label, statusLabel);

        Scene scene = new Scene(root);
        stage.setTitle("Lobby");
        stage.setScene(scene);
        stage.show();

        client.setMessageCallback(message -> {
            Platform.runLater(() -> {
                if (message.startsWith("PLAYER2_JOINED:")) {
                    String player2 = message.substring("PLAYER2_JOINED:".length());
                    player2Label.setText("Player 2: " + player2);
                    player2Label.setStyle(PLAYER2_STYLE);
                    statusLabel.setText("Game starting!");
                    startCountdown(stage);
                } else if (message.startsWith("PLAYER1_JOINED:")) {
                    String player1 = message.substring("PLAYER1_JOINED:".length());
                    player1Label.setText("Player 1: " + player1);
                    player1Label.setStyle(PLAYER1_STYLE);
                    player2Label.setText("Player 2: " + username);
                    player2Label.setStyle(PLAYER2_STYLE);
                    statusLabel.setText("Game starting!");
                    startCountdown(stage);
                }
            });
        });

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
                new GameBoard(client, username).show(stage);
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(4);
        timeline.play();
    }
}