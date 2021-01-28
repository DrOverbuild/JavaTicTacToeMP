// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.server;

import dev.reddin.rockpaperscissors.gamelogic.RockPaperScissors;
import dev.reddin.rockpaperscissors.msg.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

// controller for server process
// keeps track of the current game, server object, logged in players,
// and the view.
public class ServerMain implements ServerHandler{
	// server object
	private Server server;
	// view for server process
	private ServerView view;
	// connects logged in players with their connected client thread
	private Map<String, ClientThread> loggedInPlayers = new HashMap<>();
	// players waiting for a game. if more than 2 players are logged on, they must wait their turn
	private Queue<String> gameQueue = new LinkedList<>();
	// only one game on the server at a time
	private RockPaperScissors currentGame = null;

	// launches the server
	public static void main(String[] args) {
		new ServerMain();
	}

	// set up view and wait for user to cliek start
	public ServerMain() {
		this.view = new ServerView(this);
		view.log("Click 'Start' to start server.");
	}

	// getters and setters
	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Map<String, ClientThread> getLoggedInPlayers() {
		return loggedInPlayers;
	}

	public void setLoggedInPlayers(Map<String, ClientThread> loggedInPlayers) {
		this.loggedInPlayers = loggedInPlayers;
	}

	public Queue<String> getGameQueue() {
		return gameQueue;
	}

	public void setGameQueue(Queue<String> gameQueue) {
		this.gameQueue = gameQueue;
	}

	public RockPaperScissors getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(RockPaperScissors currentGame) {
		this.currentGame = currentGame;
	}

	// searches the key set of logged in players to find the username associated with the client
	public String userNameForClient(ClientThread client) {
		for (String username: loggedInPlayers.keySet()) {
			if (loggedInPlayers.get(username).equals(client)) {
				return username;
			}
		}

		return null;
	}

	// sets the server object and starts is
	public void start() {
		this.server = new Server(this);
		server.start();
	}

	// stops the server, reseting everything
	public void stop() {
		this.currentGame = null;
		view.updateGame(null);
		this.loggedInPlayers.clear();
		this.server.stop();
	}

	// called when a player joins the server and submits a username
	// this method notifies connected clients of the event and
	// sends them an updated list of logged in players.
	// also updates UI and checks attempts starting a game
	private void playerJoin(ClientThread client, PlayerJoinMsg msg) {
		// add player to loggedInPlayer and connect to client
		loggedInPlayers.put(msg.getUsername(), client);

		// update gui
		view.updatePlayers(loggedInPlayers.keySet());
		view.log(msg.getUsername() + " has joined.");

		// make sure everyone logged in knows this player joined
		server.sendToAllClients(msg);

		// send the player everyone already on here
		client.send(new OnlinePlayersMsg(loggedInPlayers.keySet().toArray(new String[0])));

		// get the player to start waiting for game
		gameQueue.add(msg.getUsername());

		// but who knows, maybe it was the game that was waiting for him...
		tryToStartGame(null, null);
	}

	// called when a player sends a chat message
	public void playerChatMsg(ChatMsg chat) {
		server.sendToAllClients(chat); // notify all players
		view.log(chat.getUsername() + " > " + chat.getChat()); // update view
	}

	// starts a game if there are enough players in the game queue
	public boolean tryToStartGame(ClientThread client, MoveMsg msg) {
		// if there's no game or the previous game is finished...
		if (currentGame == null || currentGame.isOver()) {
			// ...set up new one...
			currentGame = new RockPaperScissors();
			// ...and reset game queue...
			gameQueue = new LinkedList<>(loggedInPlayers.keySet());
		}

		// check that the game needs two players and that the queue has at least two players
		if ((currentGame.getPlayer1() == null && currentGame.getPlayer2() == null) && gameQueue.size() >= 2) {
			// get the next player in the queue
			String player = gameQueue.poll();
			// get client
			ClientThread client1 = loggedInPlayers.get(player);

			// update log
			view.log("Adding " + player + " to game.");
			// add to game
			currentGame.addPlayer(player, client1);

			// same thing for next player in queue
			player = gameQueue.poll();
			ClientThread client2 = loggedInPlayers.get(player);

			view.log("Adding " + player + " to game.");
			currentGame.addPlayer(player, client2);

			// once both players are added to game,
			// we need to check if this method was called
			// because a player made an initial move to start
			// the game
			if (msg != null && client != null) {
				// if so, make that move
				currentGame.move(msg,client);
			}

			// send updated game to both players
			client1.send(currentGame);
			client2.send(currentGame);

			// update view
			view.updateGame(currentGame);
			return true;
		}

		view.updateGame(currentGame);

		return false;
	}

	// called when we received a move msg from client
	// tries to start a game if there is no game
	// then processes that move
	public void gameMove(ClientThread client, MoveMsg msg) {
		// if null or over, start new game
		if (currentGame == null || currentGame.isOver()) {
			tryToStartGame(client, msg);
			return;
		}

		// otherwise, process move and update clients and view
		view.log(msg.getUsername() + " has selected " + msg.getMove());
		currentGame.move(msg, client);

		currentGame.getPlayer1Client().send(currentGame.copy());
		currentGame.getPlayer2Client().send(currentGame.copy());
		view.updateGame(currentGame);

	}

	// called when server is started
	@Override
	public void serverStarted() {
		view.log("Server started."); // update log
		view.enableStopButton(); // enable/disable necessary buttons
		view.disableStartButton();
	}

	// called when server is stopped
	@Override
	public void serverStopped() {
		view.log("Server stopped."); // update log
		view.enableStartButton(); // enable/disable necessary buttons
		view.disableStopButton();
	}

	// called when a client connects. we don't want to do anything about until
	// we get a PlayerJoinMsg from them containing their username
	@Override
	public void clientConnected(ClientThread client) {
		view.log("Client connected: " + client.getId());
	}

	// called when a player leaves or is disconnected somehow
	// make sure they're removed from all our lists, then notify remaining players
	@Override
	public void clientDisconnected(ClientThread client) {
		String username = userNameForClient(client);

		if (username == null) {
			return;
		}

		// remove from all our lists
		loggedInPlayers.remove(username);
		gameQueue.remove(username);

		// notify remaining players
		server.sendToAllClients(new PlayerLeaveMsg(username));

		// update gui
		view.updatePlayers(loggedInPlayers.keySet());
		view.log(username + " has left.");

		// handle client leaving in the middle of the game
		if (currentGame != null) {
			if (username.equals(currentGame.getPlayer1())) {
				currentGame.setRoundNumber(3); // set end of game
				currentGame.setPlayer1(null);
				currentGame.setPlayer1Client(null);
				gameQueue.add(currentGame.getPlayer2()); // move remaining player to game queue
				currentGame.getPlayer2Client().send(currentGame); // send them the final game
			} else if (username.equals(currentGame.getPlayer2())) {
				currentGame.setRoundNumber(3); // set end of game
				currentGame.setPlayer2(null);
				currentGame.setPlayer2Client(null);
				gameQueue.add(currentGame.getPlayer1()); // move remaining player to game queue
				currentGame.getPlayer1Client().send(currentGame); // send them the final game
			}
		}
	}

	// handles incoming data from client
	@Override
	public void handleMessage(ClientThread client, Object msg) {
		// check type of message
		if (msg instanceof PlayerJoinMsg) {
			// player joined the game and provided a username
			playerJoin(client, (PlayerJoinMsg) msg);
		} else if (msg instanceof PlayerLeaveMsg) {
			// player left game
			server.disconnectClient(client);
		} else if (msg instanceof ChatMsg) {
			// player sent a chat message
			playerChatMsg(((ChatMsg) msg));
		} else if (msg instanceof MoveMsg) {
			// player sent a move
			gameMove(client, ((MoveMsg) msg));
		}
	}
}
