package com.connectfour;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GuiClient extends Application {

	Client client;
	boolean myTurn = false;

	@Override
	public void start(Stage stage) {
		VBox root = new VBox(20);
		root.setPrefSize(600, 400);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-background-color: #d0e7ff;"); // Sky blue to match Scene 2

		Label titleLabel = new Label("Welcome to Connect 4!");
		titleLabel.setFont(new Font("Arial Black", 32));
		titleLabel.setStyle("-fx-text-fill: #003366;"); // Deep blue for contrast

		HBox inputBox = new HBox(10);
		inputBox.setAlignment(Pos.CENTER);
		Label nameLabel = new Label("Enter name:");
		nameLabel.setStyle("-fx-text-fill: #cc0000; -fx-font-size: 16px; -fx-font-weight: bold;");
		TextField usernameField = new TextField();
		usernameField.setPrefWidth(200);
		inputBox.getChildren().addAll(nameLabel, usernameField);

		Button joinButton = new Button("JOIN");
		joinButton.setPrefWidth(100);
		joinButton.setFont(new Font(16));
		joinButton.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-weight: bold;");

		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

		joinButton.setOnAction(e -> {
			String username = usernameField.getText().trim();
			if (username.isEmpty()) {
				errorLabel.setText("Username cannot be empty.");
				return;
			}

			errorLabel.setText("");

			client = new Client(message -> {
				Platform.runLater(() -> {
					if (message.equals("USERNAME_TAKEN")) {
						errorLabel.setText("This name is being used. Please try another username.");
					} else if (message.equals("USERNAME_ACCEPTED")) {
						errorLabel.setText("");
						System.out.println("Username accepted! Proceed to lobby...");

						LobbyController lobby = new LobbyController(client, username);
						lobby.show(stage);
					}
				});
			}, () -> {
				client.send("USERNAME:" + username);
				System.out.println("Username: " + username);
			});

			client.start();
		});

		root.getChildren().addAll(titleLabel, inputBox, joinButton, errorLabel);

		Scene scene = new Scene(root);
		stage.setTitle("Connect Four - Login");
		stage.setScene(scene);
		stage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
