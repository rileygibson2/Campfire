package network.managers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cli.CLI;
import client.Intercom;
import client.gui.GUI;
import client.gui.components.MessageBox;
import general.Functional;
import general.Pair;
import general.Triplet;
import network.Client;
import network.connection.Connection;
import network.connection.ConnectionException;
import network.connection.ConnectionRouter;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class LinkManager extends ThreadController {

	public LinkManager() {
		setWait(2000);
	}

	/**
	 * The connection checker periodically queries the other client.
	 * If it cannot connect then the connection status is set to false.
	 * If it can connect then it sends a ping and waits for the ack which will
	 * contain port information about the other client.
	 */
	public void run() {
		CLI.debug("Starting...");
		while (isRunning()) {
			NetworkManager nM = NetworkManager.getInstance();

			try {nM.checkForFatalError();}
			catch (ConnectionException e) {
				CLI.error("ConnectionChecker stopped because of previous fatal error");
				return;
			}

			//Update all addresses for this computer every 2 mins
			if (getIncrement()>0&&getIncrement()%24==0) nM.buildLocalAddresses();

			if (!Intercom.getInstance().isCommunicating()) { //If in a current communication then don't check
				try {
					Connection c = nM.generateConnection(false);
					ConnectionRouter cR = new ConnectionRouter(c);
					cR.setPingAckAction(new Functional<Object, Pair<Connection, Message>>() {
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
		Triplet<String, Integer, Integer> ports = splitPortsAndIP(m);
		if (ports==null) return false;

		//Check port and ip info against this client's set
		try {
			if (ports.a.isEmpty()) return false;
			if (!NetworkManager.getInstance().isLocalAddress(InetAddress.getByName(ports.a))) return false;
		} catch (UnknownHostException e) {CLI.debug(e.toString()); return false;}
		if (ports.b!=Intercom.getListenPort()) return false;
		if (ports.c!=Intercom.getConnectPort()) return false;
		return true;
	}

	public static boolean verifyPorts(Message m) {
		Pair<Integer, Integer> ports = splitPorts(m);
		if (ports==null) return false;

		//Check port info against this client's set
		if (ports.a!=Intercom.getListenPort()) return false;
		if (ports.b!=Intercom.getConnectPort()) return false;
		return true;
	}

	public static Pair<Integer, Integer> splitPorts(Message m) {
		String data = m.getData();
		Pair<Integer, Integer> ports = new Pair<>();

		if (data==null) return null; //Info wasnt sent
		String[] split = data.split(",");
		if (split.length!=2) return null;

		ports.a = Integer.parseInt(split[0].replace("cP=", ""));
		if (ports.a==null) return null;
		ports.b = Integer.parseInt(split[1].replace("lP=", ""));
		if (ports.b==null) return null;
		return ports;
	}

	public static Triplet<String, Integer, Integer> splitPortsAndIP(Message m) {
		String data = m.getData();
		Triplet<String, Integer, Integer> ports = new Triplet<>();

		if (data==null) return null; //Info wasnt sent
		String[] split = data.split(",");
		if (split.length!=3) return null;

		ports.a = split[0].replace("ip=", "");
		if (ports.a==null) return null;
		ports.b = Integer.parseInt(split[1].replace("cP=", ""));
		if (ports.b==null) return null;
		ports.c = Integer.parseInt(split[2].replace("lP=", ""));
		if (ports.c==null) return null;
		return ports;
	}

	protected static void updateLinkStatus(boolean linked) {
		NetworkManager.getInstance().setIsProbablyLinked(linked);
		
		/*
		 * If unlinked and in auto detect mode then try grab a client from
		 * the pool found by broadcasts
		 */
		if (Intercom.isAutoDetectEnabled()&&!linked&&(Intercom.getClient()==null||Intercom.getClient().failedRecently())) {
			if (BroadcastManager.getPotentialClients()==null) return;
			
			for (Client c : BroadcastManager.getPotentialClients()) {
				Intercom.setClient(c);
				GUI.getInstance().addMessage("Intercom detected at "+c.getAddress().getHostAddress(), MessageBox.update);
				break;
			}
		}
	}
}
