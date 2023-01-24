package client;

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
	 * Should only ever be called by the NetworkManager
	 * @param cH
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
			catch (ConnectionException e) {
				String m = "";
				if (e.getMessage().contains("Connection refused")) m = "Connection refused";
				else m = "Error making connection";
				
				//Fatal errors have already been reported
				if (!e.isFatal()) cGUI.addMessage(m, MessageBox.error);
			}
			
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
		endSpecial(); //Deal with this
		ring.accept();
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
			endSpecial(); //Deal with this
			Connection c = ring.stealConectionHandler(); //Steal from ring
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
				call.stealConectionHandler().write(new Message(Code.CallEnd));
				GUI.getInstance().addMessage("You ended the call", MessageBox.info);
			}
			else GUI.getInstance().addMessage("They ended the call", MessageBox.info);
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
	}

	public void startSpecial(Type type) {
		if (special==null&&ring==null&&call==null) {
			CLI.debug("Initiating special "+type);
			special = new Special(type);
			special.startSpecial();
		}
		else {
			if (call!=null) CLI.error("In a call, cannot special");
			if (ring!=null) CLI.error("In ring, cannot special");
			if (special!=null) CLI.error("Already in special, cannot special");
		}
	}

	public void endSpecial() {
		if (special!=null) {
			special.stopSpecial();
			special = null;
		}
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
	
		if (ring!=null) {
			Connection cH = ring.stealConectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.CallError).formatBytes());
		}
		if (call!=null) {
			Connection cH = call.stealConectionHandler();
			if (cH!=null) cH.writeCarelessly(new Message(Code.CallError).formatBytes());
		}
		
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

