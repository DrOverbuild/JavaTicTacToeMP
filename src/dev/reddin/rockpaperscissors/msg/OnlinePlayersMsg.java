// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.msg;

import java.io.Serializable;

// sent from server to client in the event of an update to the logged in player list
public class OnlinePlayersMsg implements Serializable {
	private String[] players;

	public OnlinePlayersMsg(String[] players) {
		this.players = players;
	}

	public String[] getPlayers() {
		return players;
	}

	public void setPlayers(String[] players) {
		this.players = players;
	}
}
