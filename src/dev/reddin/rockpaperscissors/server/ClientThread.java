// Jasper Reddin
// 001221899
// OS Project 2
package dev.reddin.rockpaperscissors.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

// represents connection to a client
// when a client is connected, this class starts a new thread that listens for data
public class ClientThread implements Runnable{
	private Server server; // class that created this object, we will use it to call server.handle(o)
	private Socket connection; // actual connection to the client
	private int id; // id for identifying the client
	private ObjectOutputStream out; // output stream for sending data to client
	private ObjectInputStream in; // input stream that receives data
	private Thread thread; // listener thread

	// creates a new client thread with server object that's create this and the associated socket
	public ClientThread(Server server, Socket connection) {
		this.server = server;
		this.connection = connection;
		this.id = connection.getPort(); // set the id to the socket's port that's connected.
	}

	public int getId() {
		return id;
	}

	// runnable for the listener thread
	@Override
	public void run() {
		boolean stopped = false; // using this check we safely complete the thread
		while (!stopped) {
			try {
				// this blocks until a full object is read
				Object msg = in.readUnshared(); // reads message
				server.handle(this, msg); // let the server handle the received data
			} catch (IOException e) {
				// if client gets disconnected safely stop this thread and notify server
				System.out.println("Stopping thread: " + e.getMessage());
				stopped = true; // stopping thread
				server.disconnectClient(this); // notify server
			} catch (ClassNotFoundException e) {
				// ignore incoming data if we receive an object of a class that not loaded
				System.out.println("Received unknown object from client " + getId());
			}
		}
	}

	// starts the listening thread
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	// opens object streams
	public void open() throws IOException {
		out = new ObjectOutputStream(connection.getOutputStream());
		in = new ObjectInputStream(connection.getInputStream());
	}

	// sends data to the client
	public void send(Object msg) {
		try {
			out.writeUnshared(msg); // write without checking object 'cache'
			out.flush(); // make sure all data is sent
			out.reset(); // reset object 'cache'
		} catch (IOException e) {
			// if something goes wrong, notify server
			System.out.println("Could not send data to client " + id + ": " + e.getMessage());
			server.disconnectClient(this);
		}
	}

	// closes the connection and object streams
	public void close() throws IOException {
		if (out != null) {
			out.close();
		}

		if (in != null) {
			in.close();
		}

		if (connection != null) {
			connection.close();
		}
	}

	// equals method, only checks if the id is the same.
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClientThread that = (ClientThread) o;
		return getId() == that.getId();
	}

	// hashcode, used by collections to check for equality
	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
