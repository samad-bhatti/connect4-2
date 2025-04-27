package com.connectfour;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {
	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;

	private Socket socket;

	private Consumer<String> callback;
	private Runnable onConnected;

	private Consumer<Message> messageHandler;
	private String username;

	public Client(Consumer<String> callback, Runnable onConnected) {
		this.callback = callback;
		this.onConnected = onConnected;
	}

	// Add this setter method to allow changing the callback
	public void setMessageCallback(Consumer<String> callback) {
		this.callback = callback;
	}

	public void setMessageHandler(Consumer<Message> handler) {
		this.messageHandler = handler;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void run() {
		try {
			socketClient = new Socket("127.0.0.1", 5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);

			if (onConnected != null) {
				onConnected.run();  // Ready to send after setup
			}

		} catch (Exception e) {
			System.err.println("Connection error");
			e.printStackTrace();
		}

		while (true) {
			try {
				Object obj = in.readObject(); // changed from .toString()

				if (obj instanceof String && callback != null) {
					callback.accept((String) obj); // Handle string messages
				} else if (obj instanceof GameMove && callback != null) {
					GameMove move = (GameMove) obj;
					handleGameMove(move);
				} else if (obj instanceof Message && messageHandler != null) {
					messageHandler.accept((Message) obj); // Handle chat messages
				}

			} catch (Exception e) {
				System.err.println("Error receiving data");
				e.printStackTrace();
				break;
			}
		}
	}
	private void handleGameMove(GameMove move) {
		// Process the move and update the game board
		String playerName = move.getPlayerName();
		int row = move.getRow();
		int col = move.getCol();
		String color = move.getColor();

		// Update the UI or game state with the move
		// Example:
		System.out.println(playerName + " made a move at (" + row + ", " + col + ") with color: " + color);

		// You should update your game board here using this data
		// For example, by calling a method that updates the UI or game state
		// dropDisc(row, col, color);

		// Example of sending back a message:
		// send("MOVE_RECEIVED");
	}
	//send data as a string

	public void send(String data) {
		if (out == null) {
			System.err.println("Output stream not ready. Message not sent.");
			return;
		} else {
			System.out.println("SENDING!!!(Debug) " + data);
		}

		try {
			out.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//send any serializable object (GameMove, Message, etc)
	public void send(Serializable obj) {
		if (out == null) {
			System.err.println("Output stream not ready. Message not sent.");
			return;
		}

		try {
			out.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}