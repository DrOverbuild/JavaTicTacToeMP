// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.msg;

import java.io.Serializable;

// sent between client and server when player makes a chat message
public class ChatMsg implements Serializable {
	private String username;
	private String chat;

	public ChatMsg(String username, String chat) {
		this.username = username;
		this.chat = chat;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getChat() {
		return chat;
	}

	public void setChat(String chat) {
		this.chat = chat;
	}
}
