package client;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cli.CLI;
import client.Special.Type;
import client.gui.GUI;
import client.gui.components.MessageBox;
import network.Code;
import network.Connection;
import network.ConnectionException;
import network.Message;
import network.NetworkManager;

public class Client {

	static Client singleton;

	public static GUI cGUI;
	Ring ring;
	Call call;
	Special special;

	public static final String ipRegex = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$";
	private static String clientIP;
	private static int listenPort;
	private static int connectPort;
	private static boolean shutdown;

	private Client() {
		setup();
		CLI.debug("Starting...");
	}

	public static Client getInstance() {
		if (singleton==null) singleton = new Client();
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
			try {c = NetworkManager.getInstance().generateConnection(true);}
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
			try {c = NetworkManager.getInstance().generateConnection(true);}
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
		if (e.getMessage().contains("Connection refused")) m = "Connection to "+getIP()+" refused";
		else if (e.getMessage().contains("Cannot call self")) m = "The address "+getIP()+":"+getConnectPort()+" is in use by this computer";
		else if (e.getMessage().contains("Destination IP is null")) {
			m = "Please specify the IP of the other Intercom";
			col = MessageBox.info;
		}
		else if (e.getMessage().contains("Destination IP is invalid")) m = "The IP "+getIP()+" is invalid";
		else if (e.getMessage().contains("Connect Port is invalid")) m = "Connecting on port "+getConnectPort()+" is not allowed";
		else if (e.getMessage().contains("Socket timed out")) m = "Timed out trying to connect to "+getIP()+":"+getConnectPort();
		else if (e.getMessage().contains("Network is unreachable")) m = "Check your internet connection";
		else if (e.getMessage().contains("Host is down")) m = "The host "+getIP()+" is unavailable";
		else m = "Error making connection to "+getIP()+":"+getConnectPort();
		
		//Fatal errors have already been reported
		if (!e.isFatal()) cGUI.addMessage(m, col);
	}

	public static String getIP() {return clientIP;}

	public static void setIP(String ip) {
		if (ip.matches(ipRegex)) {
			clientIP = ip;
			CLI.debug("IP set as: "+clientIP);
		}
	}

	public static int getListenPort() {return listenPort;}

	public static void setListenPort(int p) {
		listenPort = p;
		CLI.debug("Listen Port set as: "+listenPort);
	}

	public static int getConnectPort() {return connectPort;}

	public static void setConnectPort(int p) {
		connectPort = p;
		CLI.debug("Connect Port set as: "+connectPort);
	}

	public static boolean isShuttingdown() {return shutdown;}

	public void shutdown() {
		shutdown = true;
		CLI.debug("Shutting down...");
	
		//Deal with active things
		if (ring!=null) {
			Connection cH = ring.stealConnectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.CallError).formatBytes());
		}
		if (call!=null) {
			Connection cH = call.stealConnectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.CallError).formatBytes());
		}
		if (special!=null) {
			Connection cH = special.stealConnectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.CallError).formatBytes());
		}
		
		//Call other shutdowns
		NetworkManager.getInstance().shutdown();
		AudioManager.getInstance().release();
		CLI.debug("Shutdown done.");
	}

	private void setup() {
		//Set shutdown hook
		Thread shutdownHook = new Thread(() -> shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		cGUI = GUI.initialise(this);
		call = null;
		ring = null;

		clientIP = "127.0.0.1";
		listenPort = 5000;
		connectPort = 5000;
	}

	public static ExecutorService getExecutor() {
		return Executors.newSingleThreadExecutor();

	}

	public static void main(String[] args) throws Exception {
		Client.getInstance();

		if (args.length==1) {
			if (Integer.parseInt(args[0])==1) {
				Client.setListenPort(5001);
				Client.setConnectPort(5000);
			}
			else {
				Client.setListenPort(5000);
				Client.setConnectPort(5001);
			}
		}
		NetworkManager.getInstance().start();
	}
}

