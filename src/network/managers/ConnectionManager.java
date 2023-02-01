package network.managers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import client.Campfire;
import network.Client;
import network.connection.Connection;
import network.connection.ConnectionException;
import network.connection.ConnectionRouter;
import threads.ThreadController;

public class ConnectionManager extends AbstractManager {
	
	private ServerSocket serverSocket;
	private ThreadController server;
	private Set<Connection> connections;

	public ConnectionManager() {
		super();
		connections = new HashSet<Connection>();
	}
	
	@Override
	protected void start() {
		startServer();
	}

	public void startServer() {
		server = new ThreadController() {
			@Override
			public void run() {
				try {checkForFatalError(true);}
				catch (ConnectionException e) {return;}
				CLI.debug("Starting...");

				//Start server socket
				try {
					serverSocket = new ServerSocket(Campfire.getListenPort());
					while (isRunning()&&!shutdown) {
						if (serverSocket==null||serverSocket.isClosed()) throw new IOException("Server socket is closed or null");

						Socket client = serverSocket.accept();
						Connection c = new Connection(client, false);
						connections.add(c);

						//Route to correct place
						new ConnectionRouter(c).start();
					}
				}
				catch (IOException e) {
					if (!shutdown&&!Campfire.isShuttingdown()) {
						fatalError("There was a problem with the connection listener - "+e.getMessage(), true);
					}
				}
				finally {shutdown();}
			}
		};
		
		server.start();
	}

	public Connection generateConnection(boolean verbose) throws ConnectionException {
		checkForFatalError(verbose);

		try {
			//Preform checks
			if (Campfire.getClient()==null||Campfire.getClient()==Client.nullClient) throw new ConnectionException("Client is null", verbose);
			InetAddress dest = Campfire.getClient().getAddress();
			int port = Campfire.getConnectPort();
			if (dest==null) throw new ConnectionException("Client's IP is null", verbose);
			if (port<1024) throw new ConnectionException("Connect Port is invalid", verbose);

			//Check not calling self
			if (NetworkManager.getLocalAddresses()!=null
					&&NetworkManager.isLocalAddress(dest)
					&&port==Campfire.getListenPort()) throw new ConnectionException("Cannot call self", verbose);

			Connection c = Connection.generate(dest, Campfire.getConnectPort(), verbose);
			Campfire.getClient().setFailedRecently(false);
			if (c!=null) connections.add(c);
			
			
			//Successful connection so update current client to signify that
			Campfire.getClient().setFailedRecently(false);
			Campfire.getClient().resetTimestamp();
			return c;
		}
		catch (ConnectionException e) {
			//Exception only caught so can set recently failed on Client
			if (Campfire.getClient()!=null) Campfire.getClient().setFailedRecently(true);
			throw e;
		}
	}

	public void removeConnection(Connection c) {connections.remove(c);}
	
	@Override
	public boolean hasShutdown() {
		return shutdown&&server.hasEnded();
	}

	@Override
	public void shutdown() {
		if (shutdown) return;
		super.shutdown();
		if (server!=null) server.end();
		for (Connection c : new HashSet<>(connections)) c.close(); //Uses copy to avoid concurrent modification, as connections remove themselves from the set when closing
		try {
			if (serverSocket!=null) serverSocket.close();
		}
		catch (IOException e) {CLI.error("Problem shutting down ConnectionManager");}
		CLI.debug("Shutdown");
	}
}
