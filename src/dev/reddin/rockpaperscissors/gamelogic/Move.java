// Jasper Reddin
// 001221899
// OS Project 2

package dev.reddin.rockpaperscissors.gamelogic;

// represents the move made by a player
public enum Move {
	ROCK, PAPER, SCISSORS;

	// check if this move defeats the given move
	public boolean defeats(Move move) {
		if (move == null) return false;

		// simple calculation for defeat. We only win if their move is
		// the move immediately to the left of this one or, if we are
		// the first, the one at the far right end.
		int difference = this.ordinal() - move.ordinal();
		return difference == 1 || difference == -2;
	}

	// check if this move ties with the given move
	public boolean tie(Move move) {
		return this.equals(move);
	}
}
 // ROCK < PAPER < SCISSORS < ROCK