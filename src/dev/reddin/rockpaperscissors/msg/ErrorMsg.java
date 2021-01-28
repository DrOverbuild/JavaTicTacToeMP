// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.msg;

import java.io.Serializable;

// sent from server to client if something is wrong
public class ErrorMsg implements Serializable {
	private String error;

	public ErrorMsg(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
