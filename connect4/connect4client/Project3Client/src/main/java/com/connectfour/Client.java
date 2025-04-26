package com.connectfour;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {

	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;

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
			System.err.println("❌ Connection error");
			e.printStackTrace();
		}

		while (true) {
			try {
				Object obj = in.readObject(); // changed from .toString()

				if (obj instanceof String && callback != null) {
					callback.accept((String) obj); // system messages
				} else if (obj instanceof Message && messageHandler != null) {
					messageHandler.accept((Message) obj); // chat messages
				}

			} catch (Exception e) {
				System.err.println("❌ Error receiving data");
				e.printStackTrace();
				break;
			}
		}
	}

	public void send(String data) {
		if (out == null) {
			System.err.println("⚠️ Output stream not ready. Message not sent.");
			return;
		}

		try {
			out.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void send(Message msg) {
		if (out == null) {
			System.err.println("⚠️ Output stream not ready. Message not sent.");
			return;
		}

		try {
			out.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
