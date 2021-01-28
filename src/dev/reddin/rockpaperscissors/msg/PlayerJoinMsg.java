// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.msg;

import java.io.Serializable;

// sent from client to server to tell server what username the client is
// sent from server to client to tell all clients that the user is logged in
public class PlayerJoinMsg implements Serializable {
	private String username;

	public PlayerJoinMsg(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
