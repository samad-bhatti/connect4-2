package com.connectfour;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameOverController {

    private final String result;     // "WIN", "LOSE", or "DRAW"
    private final String playerName;
    private final Client client;

    public GameOverController(String result, String playerName, Client client) {
        this.result = result;
        this.playerName = playerName;
        this.client = client;
    }

    public void show(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 300);

        Label resultLabel = new Label();
        switch (result) {
            case "WIN" -> resultLabel.setText("You Win!");
            case "LOSE" -> resultLabel.setText("You Lose!");
            case "DRAW" -> resultLabel.setText("It's a Draw!");
        }

        Label playerLabel = new Label("Player: " + playerName);

        Button rematchButton = new Button("Rematch");
        Button returnLobbyButton = new Button("Return to Lobby");

        // Rematch returns to Lobby (Scene 2)
        rematchButton.setOnAction(e -> {
            LobbyController lobby = new LobbyController(client, playerName);
            lobby.show(stage);
            client.send("LOBBY_READY:" + playerName);
        });

        // Return to Lobby goes to Login (Scene 1)
        returnLobbyButton.setOnAction(e -> {
            GuiClient guiClient = new GuiClient();
            try {
                guiClient.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(playerLabel, resultLabel, rematchButton, returnLobbyButton);

        Scene scene = new Scene(root);
        stage.setTitle("Game Over");
        stage.setScene(scene);
        stage.show();
    }
}
