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
	GameState gameState;

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
		ClientThread opponent; // track who the opponent is

		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;
		String username = null;
		GameState gameState;

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
										player1.opponent = this;
										this.opponent = player1;

										player1.out.writeObject("PLAYER2_JOINED:" + this.username);
										this.out.writeObject("PLAYER1_JOINED:" + player1.username);
										GameState newGame = new GameState();
										this.gameState = newGame;
										player1.gameState = newGame;

										player1.out.writeObject("TURN:YOU");
										this.out.writeObject("TURN:OPPONENT");

									}
								}
							}
						}

						else if (data.startsWith("MOVE:")) {
							System.out.println("Am I inside?");
							int col = Integer.parseInt(data.substring(5));
							System.out.println("Received move for column: " + col);

							if (gameState.makeMove(col)) {
								System.out.println("Debugging 2");

								if (this != null && this.username != null) {
									this.out.writeObject("YOUR_MOVE_CONFIRMED:" + col);
									this.out.writeObject("TURN:OPPONENT");
									System.out.println("move confirmed");
								}

								if (gameState.checkWin(gameState.getCurrentPlayer().equals("RED") ? "YELLOW" : "RED")) {
									synchronized (clients) {
										for (ClientThread ct : clients) {
											if (ct != null && ct.username != null) {
												ct.out.writeObject("GAME_OVER: " + this.username + " WINS!");
												System.out.println("win");
											}
										}
									}
								}

								if (opponent != null && opponent.username != null) {
									opponent.out.writeObject("OPPONENT_MOVED:" + col);
									opponent.out.writeObject("TURN:YOU");
									System.out.println("opp");
								}
							} else {
								out.writeObject("INVALID_MOVE"); // Send an error message if move is invalid
							}
						}

						System.out.println("Client #" + count + " -> " + data);
					}

					// 🟰 NEW: Handle incoming Message objects
					else if (obj instanceof ServerMessage) {
						ServerMessage msg = (ServerMessage) obj;
						broadcastChat(msg);
					}

				} catch (Exception e) {
					System.err.println("Connection lost with client #" + count);
					synchronized(clients) {
						clients.remove(this);
					}
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
		private void broadcastChat(ServerMessage msg) {
			synchronized (clients) {
				for (ClientThread ct : clients) {
					try {
						ct.out.writeObject(msg);
					} catch (Exception e) {
						System.err.println("Failed to send chat message to client.");
						clients.remove(ct);
					}
				}
			}
		}
	}
}
