// Jasper Reddin
// 001221899
// OS Project 2

package dev.reddin.rockpaperscissors.client;

import java.io.*;
import java.net.Socket;

// representation of a configurable client
public class Client implements Runnable{
	private int port; // port to server
	private String host; // host to server

	private ClientHandler handler; // interface that responds to server events

	private Socket server; // socket providing connection to the server
	private ObjectInputStream in; // reads incoming messages from server
	private ObjectOutputStream out; // sends messages to server
	private Thread th; // the thread that listens to incoming data

	private boolean stopped = false; // once set to true, the listening thread stops

	// Creates a new Client with host, port, and empty handler (handler that does nothing)
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		handler = new ClientHandlerAdapter();
	}

	// Creates a new client with host, port, and given handler
	public Client(int port, String host, ClientHandler handler) {
		this.port = port;
		this.host = host;
		this.handler = handler;
	}

	// returns the interface that responds to server events
	public ClientHandler getHandler() {
		return handler;
	}

	public void setHandler(ClientHandler handler) {
		this.handler = handler;
	}

	// called when an object is received. Calls the handler's handleMsg() method
	public synchronized void handle(Object msg) {
		try {
			handler.handleMsg(msg); // toll whichever class is listening that we received a message
		} catch (Exception e) {
			// need to catch any exception so we don't crash the listen thread due to an unhandled exception in interface
			System.out.println("Exception handling message from server");
			e.printStackTrace();
		}
	}

	// sends the given message to the server
	public synchronized void send(Object msg) {
		try {
			out.writeUnshared(msg); // write to the output stream
			out.flush(); // make sure the whole object is sent
			out.reset(); // reset the outputstream's 'cache' so objects that are sent again have the changes they need
		} catch (IOException e) {
			// if something happens here, it's probably because we lost connection
			System.out.println("Could not sent message: " + e.getMessage());
			System.out.println("Stopping server.");
			stop(); // do what we need to do to stop the server
		}
	}

	// make connection to server and start listening thread, and call the handler's clientConnected() method
	public void start() {
		try {
			// make connection
			server = new Socket(host, port);
			// set up object streams
			in = new ObjectInputStream(server.getInputStream());
			out = new ObjectOutputStream(server.getOutputStream());
		} catch (IOException e) {
			System.out.println("Could not start server: " + e.getMessage());
			return;
		}

		// if connection was successful, start listening thread (the thread's runnable is in this class)
		th = new Thread(this);
		th.start();

		// let whoever cares know that we are connected.
		handler.clientConnected();
	}

	// stop listening thread and close sockets and streams... called external to disconnect, called internally when something goes wrong
	public void stop() {
		// stop the listening thread
		stopped = true;

		try {
			// close object streams
			in.close();
			out.close();

			// close socket
			server.close();
		} catch (IOException e) {
			System.out.println("Exception closing server: " + e.getMessage());
			e.printStackTrace();
		}

		// let whoever cares know we are disconnected
		handler.clientDisconnected();
	}

	// Runnable method for listening thread
	@Override
	public void run() {
		while(!stopped) {
			try {
				// read data
				Object o = in.readUnshared();
				// handle data in another method
				handle(o);
			} catch (IOException e) {
				// if something goes wrong here it's because we have disconnected
				System.out.println("Exception reading message: " + e.getMessage());
				System.out.println("Stopping client");
				stop();
			} catch (ClassNotFoundException e) {
				System.out.println("Received unknown object");
			}
		}
	}
}

// empty handler class that does nothing (this is to prevent NullPointerException
// if we fail to set up handler for the client)
class ClientHandlerAdapter implements ClientHandler {

	@Override
	public void clientConnected() {

	}

	@Override
	public void clientDisconnected() {

	}

	@Override
	public void handleMsg(Object msg) {

	}
}