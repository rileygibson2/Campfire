package client;

import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;

import cli.CLI;
import client.Special.Type;
import client.gui.GUI;
import client.gui.components.MessageBox;
import network.Client;
import network.connection.Connection;
import network.connection.ConnectionException;
import network.managers.NetworkManager;
import network.messages.Code;
import network.messages.Message;

public class Campfire {

	static Campfire singleton;

	public static GUI cGUI;
	Ring ring;
	Call call;
	Special special;

	private static Client client;
	private static int listenPort;
	private static int connectPort;
	private static int broadcastPort;
	private static int broadcastListenPort;
	private static boolean autoDetect;
	
	private static boolean shutdown;
	private static boolean isProduction;

	private Campfire() {
		setup();
		CLI.debug("Starting...");
	}

	public static Campfire getInstance() {
		if (singleton==null) singleton = new Campfire();
		return singleton;
	}

	public boolean isCommunicating() {
		if (ring!=null||call!=null||special!=null) return true;
		return false;
	}

	/**
	 * Should only ever be called by the ConnectionRouter
	 * @param c
	 */
	public void startRecievingRing(Connection c) {
		if (ring==null&&call==null&&special==null) {
			ring = new Ring(c, true);
			ring.start();
		}
		else {
			c.write(new Message(Code.RequestedClientBusy));
			if (call!=null) CLI.error("In a call, cannot ring");
			if (special!=null) CLI.error("In special, cannot ring");
			if (ring!=null) CLI.error("Already in ring, cannot ring");
		}
	}

	/**
	 * Called by GUI components
	 */
	public void startInitiatingRing() {
		if (ring==null&&call==null&&special==null) {
			Connection c = null;
			try {c = NetworkManager.getConnectionManager().generateConnection(true);}
			catch (ConnectionException e) {connectionExceptionHandle(e);}
			if (c==null) return;

			ring = new Ring(c, false);
			ring.start();
		}
		else {
			if (call!=null) CLI.error("In a call, cannot ring");
			if (special!=null) CLI.error("In special, cannot ring");
			if (ring!=null) CLI.error("Already in ring, cannot ring");
		}
	}

	/**
	 * Called by GUI components
	 */
	public void acceptRing() {
		if (ring!=null) ring.accept();
	}

	/**
	 * Called by GUI components and Ring (upon recieving cancel code)
	 */
	public void cancelRing() {
		if (ring!=null) ring.cancel();
		destroyAll();
	}

	/**
	 * Called by GUI components and Ring (upon recieving decline code)
	 */
	public void declineRing() {
		if (ring!=null) ring.decline();
		destroyAll();
	}

	/**
	 * Should only ever be called by a Ring object
	 */
	protected void startCall() {
		if (call==null) {
			Connection c = ring.stealConnectionHandler(); //Steal from ring
			ring.destroy();
			ring = null;

			if (c==null) {
				cGUI.addMessage("Problem initiating call", MessageBox.error);
				return;
			}
			call = new Call(c);
			call.start();
		}
		else if (call!=null) CLI.error("Already in call, cannot start call");
	}

	/**
	 * Called from GUI component and Call (upon recieving end code)
	 */
	public void endCall(boolean notify) {
		if (call!=null) {
			if (notify) {
				call.stealConnectionHandler().write(new Message(Code.CallEnd));
				GUI.getInstance().addMessage("You ended the call", MessageBox.info);
			}
			else GUI.getInstance().addMessage("They ended the call", MessageBox.info);
		}
		destroyAll();
	}

	/**
	 * Called by GUI components
	 * @param type
	 */
	public void startInitiatingSpecial(Type type) {
		if (ring==null&&call==null&&special==null) {
			Connection c = null;
			try {c = NetworkManager.getConnectionManager().generateConnection(true);}
			catch (ConnectionException e) {connectionExceptionHandle(e);}
			if (c==null) return;

			special = new Special(c, type, false);
			special.start();
		}
		else {
			if (call!=null) CLI.error("In a call, cannot special");
			if (ring!=null) CLI.error("In ring, cannot special");
			if (special!=null) CLI.error("Already in special, cannot special");
		}
	}

	/**
	 * Should only ever be called by the ConnectionRouter
	 * @param c
	 */
	public void startRecievingSpecial(Connection c, Type type) {
		if (ring==null&&call==null&&special==null) {
			special = new Special(c, type, true);
			special.start();
		}
		else {
			c.write(new Message(Code.RequestedClientBusy));
			if (call!=null) CLI.error("In a call, cannot special");
			if (ring!=null) CLI.error("In ring, cannot special");
			if (special!=null) CLI.error("Already in special, cannot special");
		}
	}

	/**
	 * Called from GUI component and Special (upon recieving end code)
	 */
	public void endSpecial(boolean notify) {
		if (special!=null) {
			if (notify) {
				special.stealConnectionHandler().write(new Message(Code.SpecialEnd));
				GUI.getInstance().addMessage("You ended the Special", MessageBox.info);
			}
			else GUI.getInstance().addMessage("They ended the Special", MessageBox.info);
		}
		destroyAll();
	}

	public void destroyAll() {
		if (ring!=null) {
			ring.destroy();
			ring = null;
		}
		if (call!=null) {
			call.destroy();
			call = null;
		}
		if (special!=null) {
			special.destroy();
			special = null;
		}
	}

	public void connectionExceptionHandle(ConnectionException e) {
		Color col = MessageBox.error;
		String m = "";
		if (e.getMessage().contains("Connection refused")) m = "Connection to "+client.getIP()+":"+getConnectPort()+" refused";
		else if (e.getMessage().contains("Cannot call self")) m = "You cannot call yourself";
		else if (e.getMessage().contains("Client is null")) {
			if (autoDetect) m = "No Intercoms detected at the moment";
			else m = "Please specify the Intercom IP or turn on Auto Detect";
			col = MessageBox.info;
		}
		else if (e.getMessage().contains("Client's IP is null")) m = "There was an error with the client's IP";
		else if (e.getMessage().contains("Connect Port is invalid")) m = "Connecting on port "+getConnectPort()+" is not allowed";
		else if (e.getMessage().contains("Socket timed out")) m = "Timed out trying to connect to "+client.getIP()+":"+getConnectPort();
		else if (e.getMessage().contains("Network is unreachable")) m = "Check your internet connection";
		else if (e.getMessage().contains("Host is down")) m = "The host "+client.getIP()+" is not running an Intercom";
		else m = "Error making connection to "+client.getIP()+":"+getConnectPort();

		//Fatal errors have already been reported
		if (!e.isFatal()) cGUI.addMessage(m, col);
	}

	public static void setClient(Client c) {
		if (c==null||c.equals(Client.nullClient)) {
			CLI.error("Client not set - null");
			return;
		}
		client = c;
		CLI.debug("Client set as "+c.toString());
	}
	
	public static Client getClient() {return client;}

	public static void setIP(String ip) {
		InetAddress address = null;
		try {address = InetAddress.getByName(ip);}
		catch (UnknownHostException e) {
			CLI.error("IP not set - invalid");
			cGUI.addMessage("IP "+ip+" is invalid ", MessageBox.error);
			return;
		}

		if (ip.equals(client.getIP())) return;
		client.setAddress(address);
		CLI.debug("IP set as: "+client.getIP());
		cGUI.addMessage("IP set as "+client.getIP(), MessageBox.ok);
	}

	public static int getListenPort() {return listenPort;}

	public static void setListenPort(String p, boolean restart) {
		try {setListenPort(Integer.parseInt(p), restart);}
		catch (NumberFormatException e) {
			CLI.error("Listen Port not set - invalid");
			cGUI.addMessage("Port "+p+" is invalid", MessageBox.error);
		}
	}

	public static void setListenPort(int p, boolean restart) {
		if (p==listenPort) return;
		if (p>1024) {
			listenPort = p;
			CLI.debug("Listen Port set as: "+p);
			cGUI.addMessage("Listen port set as "+p, MessageBox.ok);
			if (restart) NetworkManager.getConnectionManager().restart();
		}
		else {
			CLI.error("Listen Port not set - reserved");
			cGUI.addMessage("Listening on port "+p+" is not allowed", MessageBox.error);
		}
	}

	public static int getConnectPort() {return connectPort;}

	public static void setConnectPort(String p) {
		try {setConnectPort(Integer.parseInt(p));}
		catch (NumberFormatException e) {
			CLI.error("Connect Port not set - invalid");
			cGUI.addMessage("Port "+p+" is invalid", MessageBox.error);
		}
	}

	public static void setConnectPort(int p) {
		if (p==connectPort) return;
		if (p>1024) {
			connectPort = p;
			CLI.debug("Connect Port set as: "+p);
			cGUI.addMessage("Connect port set as "+p, MessageBox.ok);
		}
		else {
			CLI.error("Connect Port not set - reserved");
			cGUI.addMessage("Connecting on port "+p+" is not allowed", MessageBox.error);
		}
	}
	
	public static int getBroadcastListenPort() {return broadcastListenPort;}

	public static void setBroadcastListenPort(int p, boolean restart) {
		if (p==broadcastListenPort) return;
		if (p>1024) {
			broadcastListenPort = p;
			CLI.debug("Broadcast Listen Port set as: "+p);
			cGUI.addMessage("Broadcast Listen port set as "+p, MessageBox.ok);
			if (restart) NetworkManager.getBroadcastManager().restart();
		}
		else {
			CLI.error("Broadcast Listen Port not set - reserved");
			cGUI.addMessage("Listening to broadcasts on port "+p+" is not allowed", MessageBox.error);
		}
	}
	
	public static int getBroadcastPort() {return broadcastPort;}

	public static void setBroadcastPort(int p) {
		if (p==broadcastPort) return;
		if (p>1024) {
			broadcastPort = p;
			CLI.debug("Broadcast Port set as: "+p);
			cGUI.addMessage("Broadcast port set as "+p, MessageBox.ok);
		}
		else {
			CLI.error("Broadcast Port not set - reserved");
			cGUI.addMessage("Broadcasting on port "+p+" is not allowed", MessageBox.error);
		}
	}
	
	public static boolean isAutoDetectEnabled() {return autoDetect;}
	
	public static void setAutoDetect(boolean a) {
		autoDetect = a;
		if (autoDetect) {
			CLI.debug("Auto detect enabled");
			GUI.getInstance().addMessage("Auto detect enabled", MessageBox.update);
		}
		else {
			CLI.debug("Auto detect disabled");
			GUI.getInstance().addMessage("Auto detect disabled", MessageBox.update);
		}
	}

	public static boolean isShuttingdown() {return shutdown;}

	public void shutdown() {
		shutdown = true;
		CLI.debug("Shutting down...");

		//Deal with active things
		if (ring!=null) {
			Connection cH = ring.stealConnectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.LocalError).formatBytes());
		}
		if (call!=null) {
			Connection cH = call.stealConnectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.LocalError).formatBytes());
		}
		if (special!=null) {
			Connection cH = special.stealConnectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.LocalError).formatBytes());
		}

		//Call other shutdowns
		NetworkManager.getInstance().shutdown();
		AudioManager.getInstance().shutdown();
		CLI.debug("Shutdown complete.");
	}

	private void setup() {
		//Set shutdown hook
		Thread shutdownHook = new Thread(() -> shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		cGUI = GUI.initialise(this);
		call = null;
		ring = null;

		//try {client = new Client(InetAddress.getByName("127.0.0.1"));}
		//catch (UnknownHostException e) {}
		client = Client.nullClient;
		listenPort = 5000;
		connectPort = 5000;
		broadcastListenPort = 2001;
		broadcastPort = 2000;
		
		autoDetect = true;
		isProduction = false;
	}
	
	public static boolean isProduction() {return isProduction;}

	public static void main(String[] args) throws Exception {
		Campfire.getInstance();

		if (args.length==1) {
			if (Integer.parseInt(args[0])==1) {
				Campfire.setListenPort(5001, false);
				Campfire.setConnectPort(5000);
			}
			else {
				Campfire.setListenPort(5000, false);
				Campfire.setConnectPort(5001);
				Campfire.setBroadcastListenPort(2000, false);
				Campfire.setBroadcastPort(2001);
			}
		}
		NetworkManager.getInstance().start();
	}
}

