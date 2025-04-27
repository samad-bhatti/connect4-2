package com.connectfour;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private boolean myTurn = true;  // toggle after each move
    private Color myColor = Color.RED;

    public GameBoard(Client client, String username) {
        this.client = client;
        this.username = username;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f4f8ff;");

        // Game board grid
        board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(5);
        board.setVgap(5);
        board.setPadding(new Insets(10));
        board.setStyle("-fx-background-color: #3366cc; -fx-border-color: #003366; -fx-border-width: 2px;");

        buildGameBoard();

        // Buttons under each column
        HBox buttonRow = new HBox(5);
        buttonRow.setAlignment(Pos.CENTER);
        for (int col = 0; col < COLS; col++) {
            int finalCol = col;
            Button dropButton = new Button("↓");
            dropButton.setPrefWidth(70);
            dropButton.setStyle("-fx-font-size: 18px;");
            dropButton.setOnAction(e -> handleMove(finalCol));
            buttonRow.getChildren().add(dropButton);
        }

        VBox boardWithButtons = new VBox(5, board, buttonRow);
        boardWithButtons.setAlignment(Pos.CENTER);

        // Turn indicator
        VBox turnBox = new VBox(30);
        turnBox.setPadding(new Insets(20));
        turnBox.setAlignment(Pos.CENTER);

        turnIndicator1 = new Circle(40, Color.RED);
        turnIndicator2 = new Circle(40, Color.GOLD);
        turnIndicator2.setOpacity(0.3); // Start with Player 1

        Label player1Label = new Label("Player 1");
        Label player2Label = new Label("Player 2");
        player1Label.setStyle("-fx-font-size: 16px; -fx-text-fill: #003366;");
        player2Label.setStyle("-fx-font-size: 16px; -fx-text-fill: #003366;");
        turnBox.getChildren().addAll(player1Label, turnIndicator1, player2Label, turnIndicator2);

        // Chat
        VBox chatBox = new VBox(5);
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
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

        root.setCenter(boardWithButtons);
        root.setRight(turnBox);
        root.setBottom(chatBox);

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Connect Four - Game");
        stage.show();

        // Handle chat messages
        client.setMessageHandler(message -> Platform.runLater(() -> {
            if (message instanceof Message m) {
                chatArea.appendText(m.toString() + "\n");
            }
        }));
    }

    private void buildGameBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(70, 70);
                cell.setStyle("-fx-background-color: white; -fx-border-color: black;");
                Circle disc = new Circle(30);
                disc.setFill(Color.TRANSPARENT);
                discs[row][col] = disc;
                cell.getChildren().add(disc);
                board.add(cell, col, row);
            }
        }
    }

//    private void handleMove(int col) {
//        if (!myTurn) return;
//
//        int row = getAvailableRow(col);
//        if (row == -1) return;
//
//        discs[row][col].setFill(myColor);
//        String colorName = myColor.equals(Color.RED) ? "RED" : "YELLOW";
//
//        // Send the move to the server for broadcast
//        GameMove move = new GameMove(username, row, col, colorName);
//        client.send("MOVE:" + move);
//        System.out.println("The move is sent: " + move);
//        toggleTurn();
//    }

    private void handleMove(int col) {
        if (!myTurn) return;

        int row = getAvailableRow(col);
        if (row == -1) return;

        discs[row][col].setFill(myColor);
        String colorName = myColor.equals(Color.RED) ? "RED" : "YELLOW";

        // Send just the column number to the server
        client.send("MOVE:" + col);
        System.out.println("The move is sent for column: " + col);
        toggleTurn();
    }

    private int getAvailableRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (discs[row][col].getFill().equals(Color.TRANSPARENT)) {
                return row;
            }
        }
        return -1;
    }

    private void toggleTurn() {
        myTurn = !myTurn;
        boolean redTurn = myColor.equals(Color.RED);
        myColor = redTurn ? Color.GOLD : Color.RED;
        turnIndicator1.setOpacity(redTurn ? 0.3 : 1.0);
        turnIndicator2.setOpacity(redTurn ? 1.0 : 0.3);
    }
}
