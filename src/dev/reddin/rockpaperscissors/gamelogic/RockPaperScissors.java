// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.gamelogic;

import dev.reddin.rockpaperscissors.msg.ErrorMsg;
import dev.reddin.rockpaperscissors.msg.MoveMsg;
import dev.reddin.rockpaperscissors.server.ClientThread;
import dev.reddin.rockpaperscissors.server.ServerMain;

import java.io.Serializable;
import java.util.Arrays;

public class RockPaperScissors implements Serializable {
	String player1 = null; // username for player 1
	String player2 = null; // username for player 2

	transient ClientThread player1Client; // client thread for player 1, we use this to send messages to the player
	transient ClientThread player2Client; // client thread for player 2, we use this to send messages to the player

	Move[] player1Rounds = new Move[]{null, null, null}; // moves player 1 made
	Move[] player2Rounds = new Move[]{null, null, null}; // moves player 2 made

	int roundNumber = 0; // current round

	// ------------- GETTERS AND SETTERS -------------
	public String getPlayer1() {
		return player1;
	}

	public void setPlayer1(String player1) {
		this.player1 = player1;
	}

	public String getPlayer2() {
		return player2;
	}

	public void setPlayer2(String player2) {
		this.player2 = player2;
	}

	public Move[] getPlayer1Rounds() {
		return player1Rounds;
	}

	public void setPlayer1Rounds(Move[] player1Rounds) {
		this.player1Rounds = player1Rounds;
	}

	public Move[] getPlayer2Rounds() {
		return player2Rounds;
	}

	public void setPlayer2Rounds(Move[] player2Rounds) {
		this.player2Rounds = player2Rounds;
	}

	public ClientThread getPlayer1Client() {
		return player1Client;
	}

	public void setPlayer1Client(ClientThread player1Client) {
		this.player1Client = player1Client;
	}

	public ClientThread getPlayer2Client() {
		return player2Client;
	}

	public void setPlayer2Client(ClientThread player2Client) {
		this.player2Client = player2Client;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
	}

	// Adds players to a game. Returns true if ready to play, false if not
	public boolean addPlayer(String username, ClientThread client) {
		if (player1 == null) {
			player1 = username;
			player1Client = client;
			return false;
		} else if (player2 == null) {
			player2 = username;
			player2Client = client;
			return true;
		} else {
			client.send(new ErrorMsg("We have enough players."));
			return true;
		}
	}

	// retrieve whether or not the game is over
	public boolean isOver() { return roundNumber >= 3;}

	// retrieve winner if there is one
	public String getWinner() {
		// haven't finished the game yet
		if (roundNumber < 3) {
			return null;
		}

		// win counters for both players
		int player1wins = scorePlayer1();
		int player2wins = scorePlayer2();


		// return player1, player2, or tie depending on player1wins/player2wins comparison
		if (player1wins > player2wins) {
			return player1;
		} else if (player2wins > player1wins) {
			return player2;
		} else {
			return "tie";
		}
	}

	// calculate score of player 1
	public int scorePlayer1() {
		int player1wins = 0;
		for (int i = 0; i < 3; i++) {
			if (player1Rounds[i] == null) {
				break;
			}

			if (player1Rounds[i].tie(player2Rounds[i])) {
				// do nothing if it's a tie
			} else if (player1Rounds[i].defeats(player2Rounds[i])) {
				// player 1 wins
				player1wins++;
			}
		}
		return player1wins;
	}

	// calculate score of player 2
	public int scorePlayer2() {
		int player2wins = 0;
		for (int i = 0; i < 3; i++) {
			if (player2Rounds[i] == null) {
				break;
			}

			if (player2Rounds[i].tie(player1Rounds[i])) {
				// do nothing if it's a tie
			} else if (player2Rounds[i].defeats(player1Rounds[i])) {
				// player 1 wins
				player2wins++;
			}
		}
		return player2wins;
	}

	// make a copy of this object (this fixed some issues in the communication process)
	public RockPaperScissors copy() {
		RockPaperScissors copy = new RockPaperScissors();
		copy.setPlayer1(this.player1);
		copy.setPlayer2(this.player2);
		copy.setPlayer1Rounds(this.player1Rounds);
		copy.setPlayer2Rounds(this.player2Rounds);
		copy.setRoundNumber(this.roundNumber);
		return copy;
	}

	// called when a move is received by server from either client
	public void move(MoveMsg move, ClientThread client) {

		if (player1 == null || player2 == null) {
			client.send(new ErrorMsg("Waiting for another player..."));
			return;
		}

		// check to make sure the right player made a move
		if (!client.equals(player1Client) && !client.equals(player2Client)) {
			client.send(new ErrorMsg("Wrong player!"));
			return;
		}

		if (this.isOver()) {
			client.send(new ErrorMsg("Game is over."));
			return;
		}

		if (move.getUsername().equals(player1) && player1Rounds[roundNumber] == null) {
			// set player 1's move if it hasn't been selected yet
			player1Rounds[roundNumber] = move.getMove();
		} else if (move.getUsername().equals(player2) && player2Rounds[roundNumber] == null) {
			// set player 2's move if it hasn't been selected yet
			player2Rounds[roundNumber] = move.getMove();
		}

		// check if ready for next round
		if (player1Rounds[roundNumber] != null && player2Rounds[roundNumber] != null) {
			roundNumber++;
		}
	}

	@Override
	public String toString() {
		return "RockPaperScissors{" +
				"player1='" + player1 + '\'' +
				", player2='" + player2 + '\'' +
				", player1Rounds=" + Arrays.toString(player1Rounds) +
				", player2Rounds=" + Arrays.toString(player2Rounds) +
				'}';
	}

	public String consoleUpdate() {
		StringBuilder player1RoundsDesc = new StringBuilder();
		StringBuilder player2RoundsDesc = new StringBuilder();

		for (int i = 0; i < 3; i++) {
			player1RoundsDesc.append("  ").append(player1Rounds[i]);
			player2RoundsDesc.append("  ").append(player2Rounds[i]);
		}

		return "Game with " + player1 + " against " + player2 + "\n" +
				"------------------------------------------------\n" +
				" - " + player1 + ": " + player1RoundsDesc + "\n" +
				" - " + player2 + ": " + player2RoundsDesc + "\n" +
				"Round: " + roundNumber + ", Winner: " + getWinner() + "\n" +
				"------------------------------------------------\n";
	}
}