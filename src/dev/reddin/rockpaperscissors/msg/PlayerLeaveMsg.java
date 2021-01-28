// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.msg;

import java.io.Serializable;

// sent from client to server when it wishes to leave the game
// sent from server to all clients when a player has left the game
public class PlayerLeaveMsg implements Serializable {
	private String username;

	public PlayerLeaveMsg(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
