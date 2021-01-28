// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.msg;

import dev.reddin.rockpaperscissors.gamelogic.Move;

import java.io.Serializable;

// sent from client to server when client makes a move
public class MoveMsg implements Serializable {
	Move move; // the actual move
	String username; // player that made the move

	public MoveMsg(Move move, String username) {
		this.move = move;
		this.username = username;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
