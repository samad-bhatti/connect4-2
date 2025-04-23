package com.connectfour;

import javafx.application.Application;
import javafx.stage.Stage;

public class GameOverTest extends Application {

    @Override
    public void start(Stage stage) {
        String result = "WIN"; // Try also "LOSE" and "DRAW"
        String playerName = "TestPlayer";

        // Optional: Dummy Client object just to satisfy constructor
        Client dummyClient = new Client(message -> {}, () -> {});
        dummyClient.start(); // Not strictly needed unless you want to simulate sending

        GameOverController gameOver = new GameOverController(result, playerName, dummyClient);
        gameOver.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
