package network.managers;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import client.Intercom;
import client.gui.GUI;
import client.gui.components.MessageBox;
import network.Client;
import network.connection.Connection;
import network.connection.ConnectionException;
import network.connection.ConnectionRouter;

public class NetworkManager extends Thread {

	private static NetworkManager singleton;

	private ServerSocket serverSocket;
	private Set<Connection> connections;
	private LinkManager connectionChecker;
	private Set<InetAddress> localAddresses; //All addresses associated with this computer
	private static BroadcastManager broadcastManager;

	private boolean isProbablyLinked; //Tentative based on pings every so often
	private boolean isShutdown; //Set when shutdown normally

	private boolean fatalError; //Set when a fatal error occurs and no further operations should take place
	private String fatalErrorMessage; //To allow causes for fatal errors to be reproduced in a message every time the fatal error is checked

	private NetworkManager() {
		connections = new HashSet<Connection>();
		isProbablyLinked = false;
		fatalError = false;
		isShutdown = false;
		buildLocalAddresses();
	}

	public static NetworkManager getInstance() {
		if (singleton==null) singleton = new NetworkManager();
		return singleton;
	}

	public static void restart() {
		if (singleton==null) return;
		CLI.debug("Restarting...");
		singleton.shutdown();
		singleton = new NetworkManager();
		singleton.start();
	}

	public void restartBroadcastListener() {
		if (singleton==null) return;
		CLI.debug("Restarting broadcasts...");
		BroadcastManager.startListener();
	}

	public static BroadcastManager getBroadcastManager() {return broadcastManager;}

	public void removeConnection(Connection c) {connections.remove(c);}

	@Override
	public void run() {
		try {checkForFatalError();}
		catch (ConnectionException e) {return;}

		CLI.debug("Starting...");

		//Start connection checker
		if (connectionChecker!=null) connectionChecker.end();
		connectionChecker = new LinkManager();
		connectionChecker.start();

		//Start broadcast manager
		if (broadcastManager!=null) broadcastManager.end();
		broadcastManager = new BroadcastManager();
		broadcastManager.start();

		//Start server socket
		try {
			serverSocket = new ServerSocket(Intercom.getListenPort());
			while (!isShutdown) {
				if (serverSocket==null||serverSocket.isClosed()) {
					CLI.error("Server socket became null");
					fatalError("Server socket became null");
					return;
				}

				Socket clientSocket = serverSocket.accept();
				Connection c = new Connection(clientSocket, false);
				connections.add(c);

				//Route to correct place
				new ConnectionRouter(c).start();
			}
		}
		catch (IOException e) {
			if (e.getClass()==BindException.class) {
				if (!isShutdown) fatalError("Something else is using the local port "+Intercom.getListenPort());
			}
			if (!Intercom.isShuttingdown()&&!isShutdown) {
				CLI.error("Problem with server socket - "+e.getMessage());
				fatalError("Problem with server socket");
			}
		}
	}

	public Connection generateConnection(boolean verbose) throws ConnectionException {
		checkForFatalError();

		try {
			//Preform checks
			if (Intercom.getClient()==null||Intercom.getClient()==Client.nullClient) throw new ConnectionException("Client is null", verbose);
			InetAddress dest = Intercom.getClient().getAddress();
			int port = Intercom.getConnectPort();
			if (dest==null) throw new ConnectionException("Client's IP is null", verbose);
			if (port<1024) throw new ConnectionException("Connect Port is invalid", verbose);

			//Check not calling self
			if (localAddresses!=null&&isLocalAddress(dest)&&port==Intercom.getListenPort()) throw new ConnectionException("Cannot call self", verbose);

			Connection c = Connection.generate(dest, Intercom.getConnectPort(), verbose);
			Intercom.getClient().setFailedRecently(false);
			if (c!=null) connections.add(c);
			return c;
		}
		catch (ConnectionException e) {
			//Exception only caught so can set recently failed on Client
			if (Intercom.getClient()!=null) Intercom.getClient().setFailedRecently(true);
			throw e;
		}
	}

	public void buildLocalAddresses() {
		try {
			boolean initial = (localAddresses==null);
			localAddresses = new HashSet<InetAddress>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) localAddresses.add(addresses.nextElement());
			}

			if (initial) CLI.debug("Built local addresses");
			else CLI.debug("Rebuilt local addresses");
		}
		catch (SocketException e) {
			CLI.error("There was an error building local addresses");
			localAddresses = null;
		}
	}

	public void printLocalAddresses() {
		if (localAddresses==null) {
			CLI.debug("Local addresses are null");
			return;
		}
		CLI.debug("Local Addresses:");
		for (InetAddress a : localAddresses) CLI.debug(a.toString());
	}

	public boolean isLocalAddress(InetAddress address) {
		if (address==null) return false;
		if (localAddresses==null) return false;
		for (InetAddress local: localAddresses) {
			if (local.equals(address)) return true;
		}
		return false;
	}

	public void fatalError(String message) {
		fatalError = true;
		fatalErrorMessage = message;
		GUI.getInstance().addMessage(fatalErrorMessage, MessageBox.error);
		GUI.getInstance().addMessage("Fatal Error - Please restart application", MessageBox.error);
		shutdown();
	}

	public void checkForFatalError() throws ConnectionException {
		if (fatalError) {
			GUI.getInstance().addMessage(fatalErrorMessage, MessageBox.error);
			GUI.getInstance().addMessage("Fatal Error - Please restart application", MessageBox.error);
			throw new ConnectionException("A fatal error was previously detected, cannot continue").setFatal();
		}
	}

	public void setIsProbablyLinked(boolean p) {isProbablyLinked = p;}

	public boolean isProbablyLinked() {return isProbablyLinked;}

	public void shutdown() {
		isShutdown = true;
		if (connectionChecker!=null) connectionChecker.end();

		if (broadcastManager!=null) broadcastManager.shutdown();
		//BroadcastManager.endListener(); //End static listener

		for (Connection c : new HashSet<>(connections)) c.close(); //Uses copy to avoid concurrent modification, as connections remove themselves from the set when closing
		try {
			if (serverSocket!=null) serverSocket.close();
		}
		catch (IOException e) {CLI.error("Problem shutting down Network Handler");}
		CLI.debug("Shutdown");
	}
}
