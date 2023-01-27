package network.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import cli.CLI;
import client.Intercom;
import client.gui.GUI;
import client.gui.components.MessageBox;
import network.managers.NetworkManager;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class Connection extends ThreadController {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private Runnable onUpdate;
	private boolean isListening;
	//Used to stop threads using this connection sending errors after a connection was safely closed
	private boolean safelyClosed;

	private byte[] data;

	//Incoming
	public Connection(Socket socket, boolean verbose) {
		this.socket = socket;
		isListening = false;
		safelyClosed = false;
		if (verbose) CLI.debug("Generating socket "+socket.getInetAddress().getHostAddress()+"("+socket.getLocalPort()+":"+socket.getPort()+")");
	}

	public static Connection generate(InetAddress ip, int port, boolean verbose) throws ConnectionException {
		Connection cH = null;
		try {
			Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip,port), 2000);
			cH = new Connection(socket, verbose);
		}
		catch (SocketTimeoutException e) {throw new ConnectionException("Could not create socket to "+ip.getHostAddress()+"("+port+") - Socket timed out", verbose);}
		catch (IOException e) {throw new ConnectionException("Could not create socket to "+ip.getHostAddress()+"("+port+") - "+e.getMessage(), verbose);}
		return cH;
	}

	public void setOnUpdate(Runnable r) {onUpdate = r;}

	public byte[] getData() {return data.clone();}

	public boolean isListening() {return isListening;}

	@Override
	public void run() {
		if (isListening) return;
		try {listen();}
		catch (ConnectionException e) {fatalError();}
	}

	public void write(Message m) {
		debug(m, false);
		write(m.formatBytes());
	}

	public void write(byte[] bytes) {
		try {
			checkOutputStream();
			out.write(bytes);
		}
		catch (IOException e) {
			if (!Intercom.isShuttingdown()) CLI.error("Error trying to write bytes to output stream.");
			fatalError();
		}
		catch (ConnectionException e) {fatalError();}
	}

	/**
	 * Used when writing shutdown codes or fatal error codes, will write and
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
			while (isRunning()) {
				bytesRead = in.read(buffer);
				if (bytesRead==-1) break;
				
				data = Arrays.copyOfRange(buffer, 0, bytesRead);
				if (onUpdate!=null) onUpdate.run();
			}
		}
		catch (IOException e) {if (!safelyClosed) throw new ConnectionException("There was a problem while listening to the socket");}
		isListening = false;
	}
	
	/**
	 * Used during an error with the input or output stream which cannot be
	 * recovered from.
	 * 
	 * Carelessly writes error code and terminates connection, then prints error
	 * message to GUI and resets Client.
	 */
	private void fatalError() {
		if (safelyClosed) return;
		CLI.debug("Carelessly writing error code");
		writeCarelessly(new Message(Code.LocalError).formatBytes());
		close();
		Intercom.getInstance().destroyAll();
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
		if (safelyClosed) throw new ConnectionException("Socket has been safely closed, cannot use", false);
		if (socket==null) throw new ConnectionException("Socket is null!");
		if (socket.isClosed()) throw new ConnectionException("Socket is closed!");
		if (!socket.isBound()) throw new ConnectionException("Socket is not bound!");
		if (!socket.isConnected()) throw new ConnectionException("Socket is not connected!");
	}
	
	/**
	 * Used to decide whether to pring message to console.
	 * 
	 * @param m
	 */
	public void debug(Message m, boolean recieved) {
		if ((!m.getCode().equals(Code.Ping)&&!m.getCode().equals(Code.PingAck)
				||CLI.isVerbose()) ) {
			if (recieved) CLI.debug("Recieved: "+m.toString());
			else CLI.debug("Writing: "+m.toString());
		}
	}

	public void close() {
		try {
			end();
			if (in!=null) in.close();
			if (out!=null) out.close();
			if (socket!=null) socket.close();
			safelyClosed = true;
			NetworkManager.getInstance().removeConnection(this);
		} 
		catch (IOException e) {CLI.error("Problem closing connection");}
	}
}
