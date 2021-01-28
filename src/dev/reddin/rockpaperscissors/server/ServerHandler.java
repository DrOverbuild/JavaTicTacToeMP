// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.server;

// handler. implementations of this interface really care about the server's business
public interface ServerHandler {
	// called once the server is set up and accepting new connections
	void serverStarted();

	// called when the server is shut down
	void serverStopped();

	// called when a client connects
	void clientConnected(ClientThread client);

	// called when a client disconnects or loses connection
	void clientDisconnected(ClientThread client);

	// called when we receive data from a client
	void handleMessage(ClientThread client, Object msg);
}
