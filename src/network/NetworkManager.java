package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import client.Client;
import general.CLI;

public class NetworkManager extends Thread {

	private static NetworkManager singleton;
	
	private ServerSocket serverSocket;
	Set<ConnectionHandler> handlers;

	private NetworkManager() {
		handlers = new HashSet<ConnectionHandler>();
	}
	
	public static NetworkManager getInstance() {
		if (singleton==null) singleton = new NetworkManager();
		return singleton;
	}

	@Override
	public void run() {
		try {
			Client.getInstance();
			serverSocket = new ServerSocket(Client.getListenPort());
			//serverSocket = new ServerSocket(5000);
			while (true) {
				if (serverSocket==null||serverSocket.isClosed()) {
					CLI.error("Server socket became null - ");
					return;
				}
				
				Socket clientSocket = serverSocket.accept();
				ConnectionHandler cH = new ConnectionHandler(clientSocket);
				handlers.add(cH);
				
				//Route to correct place
				Client.getInstance().startRecievingRing(cH);
			}
		}
		catch (IOException e) {
			if (!Client.isShuttingdown()) CLI.error("Problem with server socket - "+e.getMessage());
		}
	}
	
	public ConnectionHandler generateConnectionHandler() {
		//ConnectionHandler cH = new ConnectionHandler(Client.getInstance().getIP(), Client.getInstance().getPort());
		
		InetAddress address = null;
		try {address = InetAddress.getLocalHost();} 
		catch (UnknownHostException e) {CLI.error("Problem getting local address - "+e.getMessage());}
		
		ConnectionHandler cH = new ConnectionHandler(address, Client.getConnectPort());
		handlers.add(cH);
		return cH;
	}

	public void shutdown() {
		for (ConnectionHandler c : handlers) c.close();
		try {
			if (serverSocket!=null) serverSocket.close();
		}
		catch (IOException e) {CLI.error("Problem shutting down Network Handler");}
	}
}
