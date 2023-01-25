package network;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import client.Client;
import client.gui.GUI;
import client.gui.components.MessageBox;
import client.gui.views.HomeView;
import general.Functional;
import general.Pair;
import threads.ThreadController;

public class NetworkManager extends Thread {

	private static NetworkManager singleton;

	private ServerSocket serverSocket;
	Set<Connection> connections;
	ThreadController connectionChecker;
	Set<InetAddress> localAddresses;

	private boolean isProbablyConnected; //Tentative based on pings every so often
	private boolean fatalError; //Set when a fatal error occurs and no further operations should take place
	private boolean isShutdown;

	private NetworkManager() {
		connections = new HashSet<Connection>();
		isProbablyConnected = false;
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

	public void removeConnection(Connection c) {connections.remove(c);}

	@Override
	public void run() {
		try {checkForFatalError();}
		catch (ConnectionException e) {return;}

		startConnectionChecker();
		CLI.debug("Starting...");

		try {
			serverSocket = new ServerSocket(Client.getListenPort());
			while (!isShutdown) {
				if (serverSocket==null||serverSocket.isClosed()) {
					CLI.error("Server socket became null");
					fatalError();
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
			if (!Client.isShuttingdown()&&!isShutdown) CLI.error("Problem with server socket - "+e.getMessage());
			if (e.getClass()==BindException.class) GUI.getInstance().addMessage("Something else is using the local port "+Client.getListenPort(), MessageBox.error);
			if (!isShutdown) fatalError();
		}
	}

	public Connection generateConnection(boolean verbose) throws ConnectionException {
		checkForFatalError();

		//Preform checks
		InetAddress dest = Client.getIP();
		int port = Client.getConnectPort();
		if (dest==null) throw new ConnectionException("Destination IP is null");
		if (port<1024) throw new ConnectionException("Connect Port is invalid");
		
		//Check not calling self
		if (localAddresses!=null&&isLocalAddress(dest)&&port==Client.getListenPort()) throw new ConnectionException("Cannot call self");

		Connection c = Connection.generate(dest, Client.getConnectPort(), verbose);
		if (c!=null) connections.add(c);
		return c;
	}

	/**
	 * The connection checker periodically queries the other client.
	 * If it cannot connect then the connection status is set to false.
	 * If it can connect then it sends a ping and waits for the ack which will
	 * contain port information about the other client.
	 * 
	 * For these two clients to be connected the provided ip should match this client's
	 * ip, the provided connect port should match this client's listen port
	 * and the provided listen port should match this client's
	 * connect port. If these are not true then the connection status
	 * is set to false.
	 */
	public void startConnectionChecker() {
		ThreadController connectionChecker = new ThreadController(){
			@Override
			public void run() {
				while (isRunning()) {
					try {checkForFatalError();}
					catch (ConnectionException e) {return;}

					//Update all addresses for this computer every 2 mins
					if (getIncrement()>0&&getIncrement()%24==0) buildLocalAddresses();
					
					if (!Client.getInstance().isCommunicating()) { //If in a current communication then don't check
						try {
							Connection c = generateConnection(false);
							ConnectionRouter cR = new ConnectionRouter(c);
							cR.setPingAckAction(new Functional<Object, Pair<Connection, Message>>() {
								@Override
								public void submit(Pair<Connection, Message> p) {
									String data = p.b.getData();
									try {
										//Port info wasnt sent with PingAck
										if (data==null) throw new Exception();
										
										String[] split = data.split(",");
										if (split.length!=3) throw new Exception();

										//Check ip and port info against this client's set
										String ip = split[0].replace("ip=", "");
										if (!isLocalAddress(InetAddress.getByName(ip))) throw new Exception();
										int cPort = Integer.parseInt(split[1].replace("cP=", ""));
										if (cPort!=Client.getListenPort()) throw new Exception();
										int lPort = Integer.parseInt(split[2].replace("lP=", ""));
										if (lPort!=Client.getConnectPort()) throw new Exception();
									}
									catch (Exception e) {
										updateConnectionStatus(false);
										p.a.close();
										return;
									}

									updateConnectionStatus(true);
									p.a.close();
								}
								@Override
								public Object get() {return null;}
							});

							cR.start();
							c.write(new Message(Code.Ping));
						} catch (ConnectionException e) {updateConnectionStatus(false);}
					}
					iterate();
				}
			}
		};

		connectionChecker.setWait(5000);
		connectionChecker.start();
	}

	protected void updateConnectionStatus(boolean connected) {
		if (connected) {
			isProbablyConnected = true;
			HomeView.getInstance().conImage.src = "connected.png";
		}
		else {
			isProbablyConnected = false;
			HomeView.getInstance().conImage.src = "disconnected.png";
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
		if (localAddresses==null) return false;
		for (InetAddress local: localAddresses) {
			if (local.equals(address)) return true;
		}
		return false;
	}

	public void fatalError() {
		fatalError = true;
		shutdown();
	}

	public void checkForFatalError() throws ConnectionException {
		if (fatalError&&!isShutdown) {
			GUI.getInstance().addMessage("Fatal Error - Please restart application", MessageBox.error);
			throw new ConnectionException("A fatal error was previously detected, cannot continue").setFatal();
		}
	}

	public boolean isProbablyConnected() {return isProbablyConnected;}

	public void shutdown() {
		isShutdown = true;
		if (connectionChecker!=null) connectionChecker.end();
		for (Connection c : new HashSet<>(connections)) c.close(); //Uses copy to avoid concurrent modification, as connections remove themselves from the set when closing
		try {
			if (serverSocket!=null) serverSocket.close();
		}
		catch (IOException e) {CLI.error("Problem shutting down Network Handler");}
		CLI.debug("Shutdown");
	}
}
