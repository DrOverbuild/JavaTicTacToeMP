// Jasper Reddin
// 001221899
// OS Project 2

package dev.reddin.rockpaperscissors.client;

// basic handler for client events
public interface ClientHandler {
	// called when client is connected
	void clientConnected();

	// called when client is disconnected, either because we meant to or due to a problem with connection
	void clientDisconnected();

	// called when receiving an object
	void handleMsg(Object msg);
}
