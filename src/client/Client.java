package client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.Special.Type;
import client.gui.GUI;
import general.CLI;
import network.Code;
import network.ConnectionHandler;
import network.Message;
import network.NetworkManager;

public class Client {

	static Client singleton;

	public static GUI cGUI;
	Ring ring;
	Call call;
	Special special;

	public static final String ipRegex = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$";
	private String clientIP;
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

	/**
	 * Should only ever be called by the NetworkManager
	 * @param cH
	 */
	public void startRecievingRing(ConnectionHandler cH) {
		if (ring==null&&call==null&&special==null) {
			ring = new Ring(cH, true);
			ring.start();
		}
		else {
			cH.write(new Message(Code.RequestedClientBusy));
			
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
			ConnectionHandler cH = NetworkManager.getInstance().generateConnectionHandler();
			ring = new Ring(cH, false);
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
	 * Called by GUI components
	 * Should only be used by initiating client to cancel a ring
	 */
	public void cancelRing() {
		if (ring!=null) {
			ring.cancel();
			ring.destroy();
			ring = null;
		}
	}

	/**
	 * Called by GUI components
	 * Should only be used by recieving client to decline a ring
	 */
	public void declineRing() {
		if (ring!=null) {
			ring.decline();
			ring.destroy();
			ring = null;
		}
	}
	
	/**
	 * Should only ever be called by a Ring object
	 */
	protected void startCall() {
		if (call==null) {
			endSpecial(); //Deal with this
			ConnectionHandler cH = ring.getConnectionHandler(); //Steal from ring
			ring.destroy();
			ring = null;

			call = new Call(cH);
			call.start();
		}
		else if (call!=null) CLI.error("Already in call, cannot start call");
		//cP.print(Utils.format(Code.CallRequest, name));
	}

	/**
	 * Called from GUI component
	 */
	public void endCall(boolean notify) {
		if (call!=null) {
			if (notify) call.getConnectionHandler().write(new Message(Code.CallEnd));
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

	public String getIP() {return clientIP;}

	public void setIP(String ip) {
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

