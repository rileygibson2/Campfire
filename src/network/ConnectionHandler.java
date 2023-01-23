package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import client.Client;
import general.CLI;

public class ConnectionHandler extends Thread {
	
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private Runnable onUpdate;
	private boolean isListening;

	private byte[] data;

	//Incoming
	protected ConnectionHandler(Socket socket) {
		this.socket = socket;
		isListening = false;
	}

	protected ConnectionHandler(InetAddress address, int port) {
		isListening = false;
		try {
			socket = new Socket(address, port);
		} catch (IOException e) {CLI.error("Could not create socket to "+address+"("+port+") - "+e.getMessage());}
	}
	
	public void setOnUpdate(Runnable r) {onUpdate = r;}

	public byte[] getData() {return data.clone();}
	
	public boolean isListening() {return isListening;}

	@Override
	public void run() {
		listen();
	}

	public void write(Message m) {
		CLI.debug("Writing: "+m.getCode().toString());
		write(m.formatBytes());
	}
	
	public void write(byte[] bytes) {
		if (!getOutputStream()) {
			if (!Client.isShuttingdown()) CLI.error("Output stream was null when trying to write.");
			return;
		}
		try {out.write(bytes);}
		catch (IOException e) {if (!Client.isShuttingdown()) CLI.error("Error trying to write bytes to output stream.");}
		
	}
	
	public void listen() {
		if (isListening) return;
		if (!getInputStream()) {
			CLI.error("Input stream was null when trying to read.");
			return;
		}

		//Listen
		isListening = true;
		byte[] buffer = new byte[1024];
		int bytesRead;
		try {
			while ((bytesRead = in.read(buffer)) != -1) {
				data = Arrays.copyOfRange(buffer, 0, bytesRead);
				if (onUpdate!=null) onUpdate.run();
			}
		}
		catch (IOException e) {if (!Client.isShuttingdown()) CLI.error("There was a problem while listening to the socket");}
		isListening = false;
	}

	private boolean getInputStream() {
		if (!checkSocket()) return false;

		if (in!=null) return true;
		try {
			in = socket.getInputStream();
			return true;
		}
		catch (IOException e) {if (!Client.isShuttingdown()) CLI.error("An error occured trying to generate input stream");}
		return false;
	}

	private boolean getOutputStream() {
		if (!checkSocket()) return false;

		if (out!=null) return true;
		try {
			out = socket.getOutputStream();
			return true;
		}
		catch (IOException e) {if (!Client.isShuttingdown()) CLI.error("An error occured trying to generate output stream");}
		return false;
	}

	public boolean checkSocket() {
		if (socket==null) {
			if (!Client.isShuttingdown()) CLI.error("Socket is null!");
			return false;
		}
		if (socket.isClosed()) {
			if (!Client.isShuttingdown()) CLI.error("Socket is closed!");
			return false;
		}
		if (socket.isBound()&&socket.isConnected()) return true;
		else return false;
	}
	
	public void close() {
		try {
		if (in!=null) in.close();
		if (out!=null) out.close();
		if (socket!=null) socket.close();
		} 
		catch (IOException e) {CLI.error("Problem closing Connection Handler");}
	}
}
