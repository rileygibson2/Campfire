package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import cli.CLI;
import client.Client;
import client.gui.GUI;
import client.gui.components.MessageBox;

public class Connection extends Thread {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private Runnable onUpdate;
	private boolean isListening;
	//Used to stop threads using this connection sending errors after a connection was safely closed
	private boolean safelyClosed;

	private byte[] data;

	//Incoming
	protected Connection(Socket socket, boolean verbose) {
		this.socket = socket;
		isListening = false;
		safelyClosed = false;
		if (verbose) CLI.debug("Generating socket "+socket.getInetAddress().getHostAddress()+"("+socket.getLocalPort()+":"+socket.getPort()+")");
	}

	protected static Connection generate(String ip, int port, boolean verbose) throws ConnectionException {
		Connection cH = null;
		try {
			Socket socket = new Socket(ip, port);
			cH = new Connection(socket, verbose);
		}
		catch (IOException e) {throw new ConnectionException("Could not create socket to "+ip+"("+port+") - "+e.getMessage(), verbose);}
		return cH;
	}

	public void setOnUpdate(Runnable r) {onUpdate = r;}

	public byte[] getData() {return data.clone();}

	public boolean isListening() {return isListening;}

	@Override
	public void run() {
		if (isListening) return;
		try {listen();}
		catch (ConnectionException e) {fatalErrorAction();}
	}

	public void write(Message m) {
		CLI.debug("Writing: "+m.getCode().toString());
		write(m.formatBytes());
	}

	public void write(byte[] bytes) {
		try {
			checkOutputStream();
			out.write(bytes);
		}
		catch (IOException e) {
			if (!Client.isShuttingdown()) CLI.error("Error trying to write bytes to output stream.");
			fatalErrorAction();
		}
		catch (ConnectionException e) {fatalErrorAction();}
	}

	/**
	 * Used when writing shutdown codes or error codes, will write and
	 * swallow any exceptions.
	 * 
	 * @param m
	 */
	public void writeCarelessly(byte[] bytes) {
		try {
			checkOutputStream();
			out.write(bytes);
		}
		catch (IOException | ConnectionException e) {}
	}

	public void listen() throws ConnectionException {
		checkInputStream();

		//Listen
		isListening = true;
		byte[] buffer = new byte[1024];
		int bytesRead;
		try {
			while (true) {
				if (safelyClosed) break;
				bytesRead = in.read(buffer);
				if (bytesRead==-1) break;
				
				data = Arrays.copyOfRange(buffer, 0, bytesRead);
				if (onUpdate!=null) onUpdate.run();
			}
		}
		catch (IOException e) {throw new ConnectionException("There was a problem while listening to the socket");}
		isListening = false;
	}
	
	/**
	 * Used during an error with the input or output stream which cannot be
	 * recovered from.
	 * 
	 * Carelessly writes error code and terminates connection, then prints error
	 * message to GUI and resets Client.
	 */
	private void fatalErrorAction() {
		if (safelyClosed) return;
		CLI.debug("Carelessly writing error code");
		writeCarelessly(new Message(Code.CallError).formatBytes());
		close();
		Client.getInstance().destroyAll();
		GUI.getInstance().addMessage("An error occured with the connection", MessageBox.error);
	}

	private void checkInputStream() throws ConnectionException {
		checkSocket();

		if (in!=null) return;
		try {in = socket.getInputStream();}
		catch (IOException e) {throw new ConnectionException("An error occured obtaining the input stream");}
	}

	private void checkOutputStream() throws ConnectionException {
		checkSocket();

		if (out!=null) return;
		try {out = socket.getOutputStream();}
		catch (IOException e) {throw new ConnectionException("An error occured obtaining the output stream");}
	}

	public void checkSocket() throws ConnectionException {
		if (safelyClosed) throw new ConnectionException("Socket has been closed");
		if (socket==null) throw new ConnectionException("Socket is null!");
		if (socket.isClosed()) throw new ConnectionException("Socket is closed!");
		if (!socket.isBound()) throw new ConnectionException("Socket is not bound!");
		if (!socket.isConnected()) throw new ConnectionException("Socket is not connected!");
	}

	public void close() {
		try {
			if (in!=null) in.close();
			if (out!=null) out.close();
			if (socket!=null) socket.close();
			safelyClosed = true;
		} 
		catch (IOException e) {CLI.error("Problem closing Connection Handler");}
	}
}
