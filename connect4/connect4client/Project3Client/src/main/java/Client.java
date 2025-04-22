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

	public Client(Consumer<String> callback, Runnable onConnected) {
		this.callback = callback;
		this.onConnected = onConnected;
	}

	// ✅ Add this setter method to allow changing the callback (e.g. from LobbyController)
	public void setMessageCallback(Consumer<String> callback) {
		this.callback = callback;
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
				String message = in.readObject().toString();
				if (callback != null) {
					callback.accept(message);
				}
			} catch (Exception e) {
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
}
