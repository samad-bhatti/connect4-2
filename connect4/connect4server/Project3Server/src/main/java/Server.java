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
		ClientThread opponent;
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
									System.out.println(username + " is waiting in the lobby...");
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

										System.out.println("Matched " + player1.username + " vs " + this.username);
									}
								}
							}
						}

						else if (data.startsWith("MOVE:")) {
							int col = Integer.parseInt(data.substring(5));
							System.out.println("Player " + username + " attempted move at column " + col);

							if (gameState != null) {
								if (gameState.makeMove(col, gameState.getCurrentPlayer())) {
									System.out.println(username + " made a valid move at column " + col);

									this.out.writeObject("YOUR_MOVE_CONFIRMED:" + col);
									this.out.writeObject("TURN:OPPONENT");

									if (gameState.checkWin(gameState.getCurrentPlayer().equals("RED") ? "YELLOW" : "RED")) {
										synchronized (clients) {
											for (ClientThread ct : clients) {
												if (ct != null && ct.username != null) {
													ct.out.writeObject("GAME_OVER: " + this.username + " WINS!");
												}
											}
										}
										System.out.println(username + " wins!");
									}

									if (opponent != null && opponent.username != null) {
										opponent.out.writeObject("OPPONENT_MOVED:" + col);
										opponent.out.writeObject("TURN:YOU");
										System.out.println("Move sent to opponent: " + opponent.username);
									}
								} else {
									out.writeObject("INVALID_MOVE"); // invalid column or full column
									System.out.println("Invalid move by " + username);
								}
							} else {
								out.writeObject("INVALID_MOVE_NO_GAME");
								System.out.println("Move attempted but no active game for " + username);
							}
						}

						System.out.println("Client #" + count + " -> " + data);
					}

					else if (obj instanceof ServerMessage) {
						ServerMessage msg = (ServerMessage) obj;
						broadcastChat(msg);
					}

				} catch (Exception e) {
					System.err.println("Connection lost with client #" + count);
					synchronized (clients) {
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
