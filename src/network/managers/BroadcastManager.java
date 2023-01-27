package network.managers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import client.Intercom;
import general.Pair;
import network.Client;
import network.connection.ConnectionException;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class BroadcastManager extends ThreadController {

	static InetAddress broadcastAddress;
	static DatagramSocket socket;
	static Pair<ThreadController, Integer> listener;
	ThreadController clientManager;

	static Set<Client> potentialClients;
	boolean isShutdown;

	public BroadcastManager() {
		setWait(5000);
		potentialClients = new HashSet<Client>();
		isShutdown = false;

		try {broadcastAddress = InetAddress.getByName("255.255.255.255");}
		catch (UnknownHostException e) {
			if (!isShutdown) {
				CLI.error("Problem getting broadcast InetAddress");
				shutdown();
				return;
			}
		}

		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		}
		catch (SocketException e) {
			if (!isShutdown) {
				CLI.error("There was a problem with the broadcast socket - "+e.getMessage());
				shutdown();
				return;
			}
		}

		clientManager = getClientManager();
		startListener();
	}

	public static Set<Client> getPotentialClients() {return potentialClients;}

	public static void addPotentialClient(Client pC) {
		boolean present = false;
		for (Client p : potentialClients) {
			if (p.equals(pC)) present = true;
		}
		if (!present) potentialClients.add(pC);
	}

	public void run() {
		CLI.debug("Starting...");
		clientManager.start();

		while (isRunning()) {
			NetworkManager nM = NetworkManager.getInstance();

			try {nM.checkForFatalError();}
			catch (ConnectionException e) {
				CLI.error("BroadcastManager stopped because of previous fatal error");
				shutdown();
				return;
			}

			//Update all addresses for this computer every 2 mins
			if (getIncrement()>0&&getIncrement()%24==0) nM.buildLocalAddresses();

			try {
				byte[] m = new Message(Code.Broadcast).formatBytes();
				DatagramPacket packet = new DatagramPacket(m, m.length, broadcastAddress, Intercom.getBroadcastPort());
				socket.send(packet);
			} 
			catch (IOException e) {
				if (!isShutdown) {
					CLI.error("There was a problem with the broadcast socket - "+e.getMessage());
					shutdown();
				}
			}
			iterate();
		}
	}

	public static void startListener() {
		if (listener!=null) {
			if (listener.b==Intercom.getBroadcastListenPort()
				&&listener.a.isRunning()&&!listener.a.isDoomed()) {
				return; //No need to restart listener
			}
			listener.a.end(); //End old listener thread so can start new one on new port
		}

		ThreadController listenerThread = new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) { 
					DatagramSocket recieverSocket = null;
					
					try {
						byte[] buffer = new byte[1024];
						DatagramPacket recieved = new DatagramPacket(buffer, buffer.length);
						recieverSocket = new DatagramSocket(Intercom.getBroadcastPort());
						recieverSocket.setSoTimeout(5000);  // set timeout for 10 seconds
						recieverSocket.receive(recieved);

						//Check sender wasn't this computer
						if (Intercom.isProduction()) {
							if (NetworkManager.getInstance().isLocalAddress(recieved.getAddress())) {
								if (CLI.isVerbose()) CLI.debug("Local broadcast response recieved");
								continue;
							}
						}

						//Process data
						Message m = Message.decode(Arrays.copyOfRange(recieved.getData(), 0, recieved.getLength()));
						if (m==null) {
							CLI.error("Recieved bad message");
							continue;
						}

						switch (m.getCode()) {
						case Broadcast:
							byte[] r = new Message(Code.BroadcastAck, "cP="+Intercom.getConnectPort()+",lP="+Intercom.getListenPort()).formatBytes();
							DatagramPacket response = new DatagramPacket(r, r.length, broadcastAddress, Intercom.getBroadcastListenPort());
							new DatagramSocket().send(response);
							break;
							
						case BroadcastAck:
							if (CLI.isVerbose()) CLI.debug("Recieved from "+recieved.getAddress().getHostAddress()+": "+m.toString());
							Pair<Integer, Integer> ports = LinkManager.splitPorts(m);
							
							//Check ports match before adding potential client
							if (ports.a!=Intercom.getListenPort()||ports.b!=Intercom.getConnectPort()) break;
							addPotentialClient(new Client(recieved.getAddress(), ports.a, ports.b));
							break;
							
						default:
							break;

						}
					}
					catch (IOException e) {
						if (e.getClass()==SocketTimeoutException.class) {
							if (CLI.isVerbose()) CLI.debug("Broadcast socket timed out");
						}
						else CLI.error("There was a problem with the broadcast reciever - "+e.getMessage());

					}
					finally {if (recieverSocket!=null) recieverSocket.close();}
				}
			}
		};
		
		listener = new Pair<ThreadController, Integer>(listenerThread, Intercom.getBroadcastListenPort());
		listenerThread.start();
	}
	
	public static void endListener() {
		if (listener!=null&&listener.a!=null) listener.a.end();
		listener = null;
	}

	public ThreadController getClientManager() {
		clientManager = new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) { 
					Set<Client> toRemove = new HashSet<>();
					for (Client c : potentialClients) {
						if (c.isExpired()
								|| c.getConnectPort()!=Intercom.getListenPort()
								|| c.getListenPort()!=Intercom.getConnectPort()) {
							toRemove.add(c);
						}
					}
					potentialClients.removeAll(toRemove);
					iterate();
				}
			}
		};

		clientManager.setWait(3000);
		return clientManager;
	}

	public static void printPotentialClients() {
		if (potentialClients==null) {
			CLI.debug("Potential Clients is null");
			return;
		}
		if (potentialClients.isEmpty()) {
			CLI.debug("There are no current potential clients");
			return;
		}
		CLI.debug("Potential Clients:");
		for (Client pC : potentialClients) CLI.debug(pC.toString());
	}

	public void shutdown() {
		isShutdown = true;
		end();
		if (clientManager!=null) clientManager.end();
		if (socket!=null) socket.close();
		CLI.debug("Shutdown");
	}
}
