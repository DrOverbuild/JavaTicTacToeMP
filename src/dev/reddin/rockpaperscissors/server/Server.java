// Jasper Reddin
// 001221899
// OS Project 2

package dev.reddin.rockpaperscissors.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Server class -- starts up serversocket and on a separate thread listens for new connections
// When clients connect, disconnect, and receive message, this class is responsible for letting
// whoever cares know what has happened
public class Server implements Runnable{
	private boolean stopped = false; // check for safely stopping accept connections thread

	private ServerSocket server; // socket that accepts connections
	private Thread thread; // thread that accepts connections

	private ServerHandler handler; // this is 'whoever cares' mentioned in the head comment

	List<ClientThread> connections = new ArrayList<>(); // keeping track of connected clients

	// if no handler is provided, set up server with empty handler so we don't run into
	// null pointer exception when server events happen
	public Server() {
		handler = new ServerHandlerAdapter();
	}

	// set up server with proved handler
	public Server(ServerHandler handler) {
		this.handler = handler;
	}

	// getters and setters
	public ServerHandler getHandler() {
		return handler;
	}

	public void setHandler(ServerHandler handler) {
		this.handler = handler;
	}

	public List<ClientThread> getConnections() {
		return new ArrayList<>(connections);
	}

	// called whenever the accepting thread accepts a new socket connection
	// takes care of opening the connection and starting the client thread and
	// letting whoever cares know that someone joined
	public void connectClient(Socket socket) {
		// create client thread object
		ClientThread client = new ClientThread(this, socket);
		try {
			// open the streams
			client.open();
			// start the listening thread
			client.start();
			// add to current connections
			connections.add(client);
			// notify whomever it concerns
			handler.clientConnected(client);
		} catch (IOException e) {
			// something happened opening the streams, so we ignore it
			System.out.println("Error opening ClientThread");
			e.printStackTrace();
		}
	}

	// called when we want to kick a client off or when a client disconnects for any reason
	public synchronized void disconnectClient(ClientThread client) {
		try {
			// make sure we close the streams and socket
			client.close();
		} catch (IOException e) { }
		// prevent memory leak by removing client from current connections
		connections.remove(client);

		// notify whomever it concerns
		handler.clientDisconnected(client);
	}

	// handles incoming data, ensuring the object that cares about the incoming
	// data isn't stupid and causes an uncaught exception. if an uncaught exception
	// occurs here, the client's thread would crash an not because of an IO problem
	public synchronized void handle(ClientThread client, Object msg) {
		try {
			handler.handleMessage(client, msg);
		} catch (Exception e) {
			System.out.println("Exception handling message from client");
			e.printStackTrace();
		}
	}

	// convenience method that sends data to all clients
	public void sendToAllClients(Object msg) {
		// creates a copy of the list in case sending this message causes the client to disconnect
		for (ClientThread client: new ArrayList<>(connections)) {
			client.send(msg);
		}
	}

	// starts the serversocket and the accepting thread, and notifies the handler
	public void start() {
		try {
			// open the socket
			server = new ServerSocket(5000);
		} catch (IOException e) {
			System.out.println("Could not start server: " + e.getMessage());
			return;
		}

		// start accepting new connections thread
		thread = new Thread(this);
		thread.start();

		// notify whoever cares
		handler.serverStarted();
	}

	// stopped the accepting threads, disconnects all clients, and closes serversocket
	public void stop() {
		// safe way to stop accepting thread
		boolean oldStopped = stopped; // old stopped because this method can get called multiple times
		stopped = true;

		// disconnect all the clients
		for (ClientThread client: new ArrayList<>(connections)){
			disconnectClient(client);
		}

		// close the server socket
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("Exception closing server");
			e.printStackTrace();
		}

		// notify whomever it concerns
		if (!oldStopped) {
			handler.serverStopped();
		}

	}

	// runnable for accepting connections thread
	@Override
	public void run() {
		while (!stopped) {
			try {
				// when new client asks to connect, accept connection and set it up properly
				connectClient(server.accept());
			} catch (IOException e) {
				// if something goes wrong here, shut the server down
				e.printStackTrace();
				stop();
			}
		}
	}
}

// empty adapter that does nothing in response to server events.
class ServerHandlerAdapter implements ServerHandler {

	@Override
	public void serverStarted() { }

	@Override
	public void serverStopped() { }

	@Override
	public void clientConnected(ClientThread client) { }

	@Override
	public void clientDisconnected(ClientThread client) { }

	@Override
	public void handleMessage(ClientThread client, Object msg) { }
}