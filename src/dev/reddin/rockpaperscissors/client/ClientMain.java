// Jasper Reddin
// 001221899
// OS Project 2

package dev.reddin.rockpaperscissors.client;

import dev.reddin.rockpaperscissors.gamelogic.Move;
import dev.reddin.rockpaperscissors.gamelogic.RockPaperScissors;
import dev.reddin.rockpaperscissors.msg.*;

import javax.swing.*;
import java.util.Arrays;

// Main class for the client process. Handles client events and updates
// the view controller accordingly. ClientViewController will talk to the
// main class when user interacts with the view.
public class ClientMain implements ClientHandler {
	Client client; // connection to server
	String username = "default_user"; // username we have chosen
	RockPaperScissors currentGame = null; // current game that is in progress (received by server)
	ClientView view = null; // JFrame for showing the game

	// create new main object with host and port
	public ClientMain(String host, int port) {
		// create client with given host and port
		this.client = new Client(port, host, this);
		// create new client view controller, this builds the frame and displays it
		view = new ClientView(this);
	}

	// getter/setter for username
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	// start the client (makes connection to server)
	public void start() {
		client.start();
	}

	// main class, processes the arguments and prompts for needed arguments via
	// JOptionPanes if args are not present. Creates new instance of this class
	// and starts the client after preparing username, host, and port.
	public static void main(String[] args) {
		String username; // provided username
		String ipStr; // host and port separated by colon
		String host; // actual host
		int port; // actual port

		// check if username is provided in args
		if (args.length >= 2) {
			username = args[1];
		} else { // if not, prompt with an input dialog
			username = JOptionPane.showInputDialog("Enter username");
		}

		// check if IP is provided in args
		if (args.length >= 1) {
			ipStr = args[0];
		} else { // if not, prompt with input dialog
			ipStr = JOptionPane.showInputDialog("Enter IP (ex. 120.0.0.1:5000)");
		}

		// split ipStr into host and port
		String[] split = ipStr.split(":");
		host = split[0];
		port = Integer.parseInt(split[1]);

		// set up main and connect
		ClientMain main = new ClientMain(host, port);
		main.setUsername(username);
		main.start();
	}

	// ClientHandler method, called when client is connected
	@Override
	public void clientConnected() {
		// display in log
		view.log(" > Connected...");
		// notify server what our desired username is
		client.send(new PlayerJoinMsg(username));
	}

	// ClientHandler method, called when client is disconnected
	@Override
	public void clientDisconnected() {
		// display in log
		view.log(" > Disconnected from server.");
	}

	// ClientHandler method, called when we received data from the server
	@Override
	public void handleMsg(Object msg) {
		// first step is to figure out what the received data actually is
		if (msg instanceof PlayerJoinMsg) {
			// Here the server is telling us someone joined the game
			PlayerJoinMsg playerJoinMsg = ((PlayerJoinMsg) msg);
			// display in log
			view.log(" > " + playerJoinMsg.getUsername() + " has joined.");
		} else if (msg instanceof OnlinePlayersMsg) {
			// here the server is letting us know who is online (sent when we log on or when someone else logs on)
			OnlinePlayersMsg onlinePlayersMsg = ((OnlinePlayersMsg) msg);
			// update the players view in ClientViewController
			view.updatePlayers(Arrays.asList(onlinePlayersMsg.getPlayers()));
		} else if (msg instanceof PlayerLeaveMsg) {
			// here the server is telling us a player left the game
			PlayerLeaveMsg playerLeaveMsg = ((PlayerLeaveMsg) msg);
			// update in log
			view.log(" > " + playerLeaveMsg.getUsername() + " has left.");
		} else if (msg instanceof ChatMsg) {
			// here the server is telling us someone has sent a chat message
			ChatMsg chat = ((ChatMsg) msg);
			// display in log
			view.log(chat.getUsername() + " > " + chat.getChat());
		} else if (msg instanceof RockPaperScissors) {
			// here the server is telling us to update the our game view.
			// this is because either someone made a move, or the game ended,
			// or a new game has started.
			// when this happens, simply update the game view.
			this.updateGame(((RockPaperScissors) msg));
		} else if (msg instanceof ErrorMsg) {
			// something went wrong somewhere so let's just tell display in log
			ErrorMsg error = ((ErrorMsg) msg);
			view.log("Oops... " + error.getError());
		}
	}

	// updates the game view and displays in log what the status of the game is
	private void updateGame(RockPaperScissors msg) {
		currentGame = msg;
//		System.out.println(currentGame.consoleUpdate());

		// update game view
		view.updateGame(msg);

		// check if we have both players
		if (currentGame.getPlayer1() == null || currentGame.getPlayer2() == null) {
			view.log(" > Waiting for another player...");
			return;
		}

		// check if we're in this game
		if (!currentGame.getPlayer1().equals(username) && !currentGame.getPlayer2().equals(username)) {
			view.log(" > This is not your game.");
			return;
		}

		// check if gam is over
		if (currentGame.isOver()) {
			view.log(" > Game is over. Waiting for new game...");
		}
	}

	// called by ClientViewController, tells server this player made a move
	public void move(Move move) {
		client.send(new MoveMsg(move, username));
	}

	// called by ClientViewController, tells server player sent a chat message
	public void chat(String msg) {
		client.send(new ChatMsg(username, msg));
	}

	// called by ClientViewController, called when "Logout" is clicked, tells server we're leaving.
	public void stop() {
		client.send(new PlayerLeaveMsg(username));
		client.stop();
	}
}
