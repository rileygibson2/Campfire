package network;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import client.Client;
import client.gui.GUI;
import client.gui.components.MessageBox;
import client.gui.views.HomeView;
import threads.ThreadController;

public class NetworkManager extends Thread {

	private static NetworkManager singleton;

	private ServerSocket serverSocket;
	Set<Connection> connections;
	ThreadController connectionChecker;

	private boolean isProbablyConnected; //Tentative based on pings every so often
	private boolean fatalError; //Set when a fatal error occurs and no further operations should take place
	
	private NetworkManager() {
		connections = new HashSet<Connection>();
		isProbablyConnected = false;
		fatalError = false;
	}

	public static NetworkManager getInstance() {
		if (singleton==null) singleton = new NetworkManager();
		return singleton;
	}

	public void removeConnection(Connection c) {connections.remove(c);}

	@Override
	public void run() {
		try {checkFatalError();}
		catch (ConnectionException e) {return;}
		
		startConnectionChecker();
		
		try {
			Client.getInstance();
			serverSocket = new ServerSocket(Client.getListenPort());
			//serverSocket = new ServerSocket(5000);
			while (true) {
				if (serverSocket==null||serverSocket.isClosed()) {
					CLI.error("Server socket became null");
					setFatalError();
					return;
				}

				Socket clientSocket = serverSocket.accept();
				Connection c = new Connection(clientSocket, false);
				connections.add(c);

				//Route to correct place
				new ConnectionRouter(c);
			}
		}
		catch (IOException e) {
			if (!Client.isShuttingdown()) CLI.error("Problem with server socket - "+e.getMessage());
			if (e.getClass()==BindException.class) GUI.getInstance().addMessage("Something else is using the local port "+Client.getListenPort(), MessageBox.error);
			setFatalError();
		}
	}

	public Connection generateConnection(boolean verbose) throws ConnectionException {
		checkFatalError();
		
		//Preform checks
		InetAddress localAddress = null;
		try {localAddress = InetAddress.getLocalHost();} 
		catch (UnknownHostException e) {CLI.error("Problem getting local address - "+e.getMessage());}
		
		String ip = Client.getIP();
		int port = Client.getConnectPort();
		if (ip==null) throw new ConnectionException("Destination IP is null");
		if (port<1024) throw new ConnectionException("Connect Port is invalid");
		
		InetAddress destAddress;
		try {destAddress = InetAddress.getByName(ip);}
		catch (UnknownHostException e) {throw new ConnectionException("Destination IP is invalid");}
		
		//Check not calling self
		if (localAddress!=null&&ip.equals(localAddress.getHostAddress())&&port==Client.getListenPort()) throw new ConnectionException("Cannot call self");
		
		Connection c = Connection.generate(destAddress, Client.getConnectPort(), verbose);
		if (c!=null) connections.add(c);
		return c;
	}

	public void startConnectionChecker() {
		ThreadController connectionChecker = new ThreadController(){
			@Override
			public void run() {
				while (isRunning()) {
					try {checkFatalError();}
					catch (ConnectionException e) {return;}
					
					if (!Client.getInstance().isCommunicating()) {
						try {
							Connection c = generateConnection(false);
							c.close();
							isProbablyConnected = true;
							HomeView.getInstance().conImage.src = "connected.png";
						} catch (ConnectionException e) {
							isProbablyConnected = false;
							HomeView.getInstance().conImage.src = "disconnected.png";
						}
					}
					iterate();
				}
			}
		};
		connectionChecker.setWait(8000);
		connectionChecker.start();
	}

	public void setFatalError() {
		fatalError = true;
		shutdown();
	}
	
	public void checkFatalError() throws ConnectionException {
		if (fatalError) {
			GUI.getInstance().addMessage("Fatal Error - Please restart application", MessageBox.error);
			throw new ConnectionException("A fatal error was previously detected, cannot continue").setFatal();
		}
	}
	
	public boolean isProbablyConnected() {return isProbablyConnected;}

	public void shutdown() {
		if (connectionChecker!=null) connectionChecker.end();
		for (Connection c : connections) c.close();
		try {
			if (serverSocket!=null) serverSocket.close();
		}
		catch (IOException e) {CLI.error("Problem shutting down Network Handler");}
	}
}
