import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<>();
	HashSet<String> usernames = new HashSet<>();
	Queue<ClientThread> waitingLobby = new LinkedList<>();

	TheServer server;

	Server() {
		server = new TheServer();
		server.start();
	}

	public class TheServer extends Thread {
		public void run() {
			try (ServerSocket mysocket = new ServerSocket(5555)) {
				System.out.println("Server is waiting for a client!");
				while (true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					clients.add(c);
					c.start();
					count++;
				}
			} catch (Exception e) {
				System.err.println("Server did not launch");
				e.printStackTrace();
			}
		}
	}

	class ClientThread extends Thread {

		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;
		String username = null;

		ClientThread(Socket s, int count) {
			this.connection = s;
			this.count = count;
		}

		public void updateClients(String message) {
			System.out.println("Broadcast: " + message);
		}

		public void run() {
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			} catch (Exception e) {
				System.out.println("Streams not open");
				return;
			}

			updateClients("New client connected: client #" + count);

			while (true) {
				try {
					String data = in.readObject().toString();
					System.out.println("client #" + count + " sent: " + data);

					if (data.startsWith("USERNAME:")) {
						String requestedUsername = data.substring(9).trim();
						synchronized (usernames) {
							if (usernames.contains(requestedUsername)) {
								out.writeObject("USERNAME_TAKEN");
							} else {
								usernames.add(requestedUsername);
								username = requestedUsername;
								out.writeObject("USERNAME_ACCEPTED");
								System.out.println("Username registered: " + username);
							}
						}
						continue;
					}

// Inside your server.java where you handle LOBBY_READY
					if (data.startsWith("LOBBY_READY:")) {
						synchronized (waitingLobby) {
							if (waitingLobby.isEmpty()) {
								waitingLobby.add(this); // First player waits
							} else {
								ClientThread player1 = waitingLobby.poll(); // Match with waiting player
								if (player1 != null) {
									// Notify player1 of player2's name
									player1.out.writeObject("PLAYER2_JOINED:" + username);
									// ✅ Notify player2 of player1's name
									this.out.writeObject("PLAYER1_JOINED:" + player1.username);
								}
							}
						}
						continue;
					}



					updateClients("client #" + count + " said: " + data);

				} catch (Exception e) {
					System.err.println("Connection lost with client #" + count);
					updateClients("Client #" + count + " has left the server.");
					clients.remove(this);

					if (username != null) {
						synchronized (usernames) {
							usernames.remove(username);
							System.out.println("Username removed: " + username);
						}
					}
					break;
				}
			}
		}
	}
}
