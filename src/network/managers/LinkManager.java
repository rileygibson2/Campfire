package network.managers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cli.CLI;
import client.Intercom;
import client.gui.GUI;
import client.gui.components.MessageBox;
import general.GetterSubmitter;
import general.Pair;
import general.Triplet;
import network.Client;
import network.connection.Connection;
import network.connection.ConnectionException;
import network.connection.ConnectionRouter;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class LinkManager extends AbstractManager {
	
	private ThreadController checker;
	private static boolean isProbablyLinked;

	public LinkManager() {
		super();
		isProbablyLinked = false;
	}
	
	@Override
	protected void start() {
		startChecker();
	}

	/**
	 * The connection checker periodically queries the other client.
	 * If it cannot connect then the connection status is set to false.
	 * If it can connect then it sends a ping and waits for the ack which will
	 * contain port information about the other client.
	 */
	public void startChecker() {
		checker = new ThreadController() {
			@Override
			public void run() {
				CLI.debug("Starting...");

				while (isRunning()) {
					//Update all addresses for this computer every 2 mins
					if (getIncrement()>0&&getIncrement()%24==0) NetworkManager.buildLocalAddresses();

					if (!Intercom.getInstance().isCommunicating()) { //If in a current communication then don't check
						try {
							Connection c = NetworkManager.getConnectionManager().generateConnection(false);
							ConnectionRouter cR = new ConnectionRouter(c);
							cR.setPingAckAction(new GetterSubmitter<Object, Pair<Connection, Message>>() {
								@Override
								public void submit(Pair<Connection, Message> p) {
									updateLinkStatus(verifyPortsAndIP(p.b));
									p.a.close();
								}
								@Override
								public Object get() {return null;}
							});

							cR.start();
							c.write(new Message(Code.Ping));
						} catch (ConnectionException e) {updateLinkStatus(false);}
					}
					iterate();
				}
			}
		};
		
		checker.setWait(2000);
		checker.start();
	}

	public boolean isProbablyLinked() {return isProbablyLinked;}

	/**
	 * For two clients to be connected the provided ip should match this client's
	 * ip, the provided connect port should match this client's listen port
	 * and the provided listen port should match this client's
	 * connect port. If these are not true then the connection status
	 * is set to false.
	 * 
	 * @param m
	 * @return
	 */
	public static boolean verifyPortsAndIP(Message m) {
		Triplet<String, Integer, Integer> ports = m.splitPortsAndIP();
		if (ports==null) return false;

		//Check port and ip info against this client's set
		try {
			if (ports.a.isEmpty()) return false;
			if (!NetworkManager.isLocalAddress(InetAddress.getByName(ports.a))) return false;
		} catch (UnknownHostException e) {CLI.debug(e.toString()); return false;}
		if (ports.b!=Intercom.getListenPort()) return false;
		if (ports.c!=Intercom.getConnectPort()) return false;
		return true;
	}

	protected static void updateLinkStatus(boolean linked) {
		//Show message for change of link status
		if (linked!=isProbablyLinked) {
			if (linked) GUI.getInstance().addMessage("Intercom connected", MessageBox.update);
			else GUI.getInstance().addMessage("Intercom disconnected", MessageBox.update);
		}
		isProbablyLinked = linked;
		
		/*
		 * If unlinked and in auto detect mode then try grab a client from
		 * the pool found by broadcasts
		 */
		if (!linked&&Intercom.isAutoDetectEnabled()
			&&(Intercom.getClient()==null
				||Intercom.getClient().equals(Client.nullClient)
				||Intercom.getClient().failedRecently())) {
			
			if (BroadcastManager.getPotentialClients()==null) return;
			
			for (Client c : BroadcastManager.getPotentialClients()) {
				if (c.equals(Intercom.getClient())) continue; //Intercom is already set with this detected client
				Intercom.setClient(c);
				GUI.getInstance().addMessage("Intercom detected at "+c.getAddress().getHostAddress(), MessageBox.update);
				break;
			}
		}
	}

	@Override
	public boolean hasShutdown() {
		return shutdown&&checker.hasEnded();
	}

	@Override
	public void shutdown() {
		if (shutdown) return;
		super.shutdown();
		if (checker!=null) checker.hasEnded();
		CLI.debug("Shutdown");
		super.shutdown();
	}
}
