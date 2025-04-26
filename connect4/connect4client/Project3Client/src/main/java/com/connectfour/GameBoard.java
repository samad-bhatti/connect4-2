package com.connectfour;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class GameBoard {

    private final Client client;
    private final String username;
    private GridPane board;
    private Circle turnIndicator1;
    private Circle turnIndicator2;
    private TextArea chatArea;
    private TextField chatInput;


    private final int ROWS = 6;
    private final int COLS = 7;
    private Circle[][] discs = new Circle[ROWS][COLS];
    private boolean myTurn = false;
    private Color myColor = Color.RED;  // default, can change later


    public GameBoard(Client client, String username) {
        this.client = client;
        this.username = username;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f4f8ff;");

        // actual game board
        board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(5);
        board.setVgap(5);
        board.setPadding(new Insets(10));
        board.setStyle("-fx-background-color: #3366cc; -fx-border-color: #003366; -fx-border-width: 2px;");
        buildGameBoard();

        // whos turn is it
        VBox turnBox = new VBox(30);
        turnBox.setPadding(new Insets(20));
        turnBox.setAlignment(Pos.CENTER);

        turnIndicator1 = new Circle(40, Color.RED);    // Player 1
        turnIndicator2 = new Circle(40, Color.GOLD);   // Player 2
        turnIndicator2.setOpacity(0.3); // Start with Player 1 active

        Label player1Label = new Label("Player 1");
        Label player2Label = new Label("Player 2");
        player1Label.setStyle("-fx-font-size: 16px; -fx-text-fill: #003366;");
        player2Label.setStyle("-fx-font-size: 16px; -fx-text-fill: #003366;");

        turnBox.getChildren().addAll(player1Label, turnIndicator1, player2Label, turnIndicator2);

        // chat box on the bottom of the screen
        VBox chatBox = new VBox(5);
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefRowCount(4);

        chatInput = new TextField();
        chatInput.setPromptText("Type your message and hit Enter...");
        chatInput.setOnAction(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                client.send(new Message(username, text));
                chatInput.clear();
            }
        });

        chatBox.getChildren().addAll(chatArea, chatInput);
        chatBox.setPadding(new Insets(10));

        root.setCenter(board);
        root.setRight(turnBox);
        root.setBottom(chatBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Connect Four - Game");
        stage.setScene(scene);
        stage.show();

        // === Handle Incoming Messages ===
        client.setMessageHandler(message -> Platform.runLater(() -> {
            if (message instanceof Message) {
                Message m = (Message) message;
                chatArea.appendText(m.toString() + "\n");
            }
        }));
    }

    private void buildGameBoard() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(70, 70);
                cell.setStyle("-fx-background-color: white; -fx-border-color: black;");
                Circle disc = new Circle(30);
                disc.setFill(Color.TRANSPARENT);
                discs[row][col] = disc;
                cell.getChildren().add(disc);

                int finalCol = col;
                cell.setOnMouseClicked((MouseEvent e) -> handleMove(finalCol));


                board.add(cell, col, row);
            }
        }
    }

    private void handleMove(int col) {
        if (!myTurn) return;

        int row = getAvailableRow(col);
        if (row == -1) return; // Column full

        client.send("MOVE:" + col);
        myTurn = false;
    }

    private int getAvailableRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (discs[row][col].getFill().equals(Color.TRANSPARENT)) {
                return row;
            }
        }
        return -1;
    }

    private void applyMove(String player, int col) {
        int row = getAvailableRow(col);
        if (row == -1) return;

        Color color = player.equals(username) ? myColor : (myColor == Color.RED ? Color.GOLD : Color.RED);
        discs[row][col].setFill(color);
    }

    // switch turns for turn indicatpr
    public void setTurn(String currentPlayer) {
        boolean isPlayer1Turn = username.equals(currentPlayer);
        turnIndicator1.setOpacity(isPlayer1Turn ? 1.0 : 0.3);
        turnIndicator2.setOpacity(isPlayer1Turn ? 0.3 : 1.0);
    }
}
