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

		public void run() {
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			} catch (Exception e) {
				System.out.println("Streams not open");
				return;
			}

			while (true) {
				try {
					Object obj = in.readObject();

					if (obj instanceof String) {
						String data = (String) obj;

						if (data.startsWith("USERNAME:")) {
							String requestedUsername = data.substring(9).trim();
							synchronized (usernames) {
								if (usernames.contains(requestedUsername)) {
									out.writeObject("USERNAME_TAKEN");
								} else {
									usernames.add(requestedUsername);
									username = requestedUsername;
									out.writeObject("USERNAME_ACCEPTED");
								}
							}
						}

						else if (data.startsWith("LOBBY_READY:")) {
							synchronized (waitingLobby) {
								if (waitingLobby.isEmpty()) {
									waitingLobby.add(this);
								} else {
									ClientThread player1 = waitingLobby.poll();
									if (player1 != null) {
										player1.out.writeObject("PLAYER2_JOINED:" + this.username);
										this.out.writeObject("PLAYER1_JOINED:" + player1.username);

										player1.out.writeObject("TURN:YOU");
										this.out.writeObject("TURN:OPPONENT");
									}
								}
							}
						}

						else if (data.startsWith("MOVE:")) {
							int col = Integer.parseInt(data.substring(5));

							synchronized (clients) {
								for (ClientThread ct : clients) {
									if (ct != null && ct.username != null) {
										if (ct == this) {
											ct.out.writeObject("YOUR_MOVE_CONFIRMED:" + col);
											ct.out.writeObject("TURN:OPPONENT");
										} else {
											ct.out.writeObject("OPPONENT_MOVED:" + col);
											ct.out.writeObject("TURN:YOU");
										}
									}
								}
							}
						}

						System.out.println("Client #" + count + " -> " + data);
					}

					// 🟰 NEW: Handle incoming Message objects
					else if (obj instanceof Message) {
						Message msg = (Message) obj;
						broadcastChat(msg);
					}

				} catch (Exception e) {
					System.err.println("Connection lost with client #" + count);
					clients.remove(this);
					if (username != null) {
						synchronized (usernames) {
							usernames.remove(username);
						}
					}
					break;
				}
			}
		}

		// 💬 Broadcast chat message to all clients
		private void broadcastChat(Message msg) {
			synchronized (clients) {
				for (ClientThread ct : clients) {
					try {
						ct.out.writeObject(msg);
					} catch (Exception e) {
						System.err.println("Failed to send chat message to client.");
					}
				}
			}
		}
	}
}
