package network.managers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cli.CLI;
import client.Campfire;
import client.gui.views.HomeView;
import general.Pair;
import network.Client;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class BroadcastManager extends AbstractManager {

	private DatagramSocket writeSocket;
	private DatagramSocket listenSocket;
	private ThreadController writer;
	private ThreadController listener;
	private ThreadController cleaner;
	private static Set<Client> potentialClients;

	public BroadcastManager() {
		super();
		potentialClients = new HashSet<Client>();
	}

	@Override
	protected void start() {
		startWriter();
		startCleaner();
		startListener();
	}

	public void startWriter() {
		writer = new ThreadController() {
			@Override
			public void run() {
				CLI.debug("Starting...");
				try {
					writeSocket = new DatagramSocket();
					writeSocket.setBroadcast(true);

					while (isRunning()) {
						//Update all addresses for this computer every 2 mins
						if (getIncrement()>0&&getIncrement()%24==0) NetworkManager.buildLocalAddresses();

						//Broadcast message
						byte[] m = new Message(Code.Broadcast).formatBytes();
						DatagramPacket packet = new DatagramPacket(m, m.length, InetAddress.getByName("255.255.255.255"), Campfire.getBroadcastPort());
						writeSocket.send(packet);

						iterate();
					}
				} 
				catch (IOException e) {
					if (shutdown||Campfire.isShuttingdown()) return;
					fatalError("There was a problem with the broadcast writer - "+e.getMessage(), false);
				}
			}
		};

		writer.setWait(5000);
		writer.setPaintOnIterate(false);
		writer.start();
	}

	public void startListener() {
		if (listener!=null) listener.end();

		listener = new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) {
					try {
						byte[] buffer = new byte[1024];
						DatagramPacket recieved = new DatagramPacket(buffer, buffer.length);
						listenSocket = new DatagramSocket(Campfire.getBroadcastPort());
						listenSocket.setSoTimeout(5000);
						listenSocket.receive(recieved);

						//Check sender wasn't this computer
						if (Campfire.isProduction()) {
							if (NetworkManager.isLocalAddress(recieved.getAddress())) {
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
							Message message;
							if (Campfire.getIntercomName()!=null) message = new Message(Code.BroadcastAck, "cP="+Campfire.getConnectPort()+",lP="+Campfire.getListenPort()+",n="+Campfire.getIntercomName());
							else message = new Message(Code.BroadcastAck, "cP="+Campfire.getConnectPort()+",lP="+Campfire.getListenPort());
							
							byte[] r = message.formatBytes();
							DatagramPacket response = new DatagramPacket(r, r.length, InetAddress.getByName("255.255.255.255"), Campfire.getBroadcastListenPort());
							new DatagramSocket().send(response);
							break;

						case BroadcastAck:
							if (CLI.isVerbose()) CLI.debug("Recieved from "+recieved.getAddress().getHostAddress()+": "+m.toString());
							Map<String, String> pairs = m.getPairs();
							if (CLI.isVerbose()) CLI.debug(pairs);
							//Verify data
							if (pairs==null) break;
							int cP, lP;
							try {
								cP = Integer.parseInt(pairs.get("cP"));
								if (cP!=Campfire.getListenPort()) break;
								
								lP = Integer.parseInt(pairs.get("lP"));
								if (lP!=Campfire.getConnectPort()) break;
							}
							catch (NumberFormatException e) {break;}
							
							//Ports match so add as potential client
							if (pairs.get("n")!=null) addPotentialClient(new Client(recieved.getAddress(), cP, lP, pairs.get("n")));
							else addPotentialClient(new Client(recieved.getAddress(), cP, lP));
							break;

						default:
							break;
						}
					}
					catch (IOException e) {
						if (shutdown||Campfire.isShuttingdown()) return;
						if (e.getClass()==SocketTimeoutException.class) {
							if (CLI.isVerbose()) CLI.debug("No responses - timeout");
						}
						else fatalError("There was a problem with the broadcast listener - "+e.getMessage(), false);
					}
					finally {if (listenSocket!=null) listenSocket.close();}
				}
			}
		};

		listener.setPaintOnIterate(false);
		listener.start();
	}

	public void startCleaner() {
		if (cleaner!=null) cleaner.end();

		cleaner = new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) {
					Set<Client> toRemove = new HashSet<>();
					for (Client c : potentialClients) {
						if (c.isExpired()
								|| c.getConnectPort()!=Campfire.getListenPort()
								|| c.getListenPort()!=Campfire.getConnectPort()
								|| c.failedRecently()) {
							toRemove.add(c);
						}
					}

					/*
					 * If in auto mode and a client being removed from potential
					 * clients is the current client then current client should be
					 * set to null to preserve 'auto' behaviour.
					 * 
					 * This should only happen if the cause of the
					 * client being removed is something other than just expired.
					 */
					if (Campfire.isAutoDetectEnabled()&&toRemove.contains(Campfire.getClient())) {
						Campfire.setClient(Client.nullClient);
					}
					potentialClients.removeAll(toRemove);


					//Handle multiple clients button
					if (potentialClients.size()>0) HomeView.getInstance().showUsersButton();
					else HomeView.getInstance().hideUsersButton();

					iterate();
				}
			}
		};

		cleaner.setPaintOnIterate(false);
		cleaner.setWait(2000);
		cleaner.start();
	}

	public static Set<Client> getPotentialClients() {return potentialClients;}

	public static void addPotentialClient(Client pC) {
		for (Client p : potentialClients) {
			if (p.equals(pC)) {
				p.resetTimestamp(); //So to give more time to this client
				p.setName(pC.getName()); //To catch name updates without messing with client
				return;
			}
		}
		
		if (pC.equals(Campfire.getClient())) Campfire.getClient().setName(pC.getName()); //To catch name updates without messing with Intercom's client
		potentialClients.add(pC);
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

	@Override
	public boolean hasShutdown() {
		return shutdown&&writer.hasEnded()&&listener.hasEnded()&&cleaner.hasEnded();
	}

	@Override
	public void shutdown() {
		if (shutdown) return;
		super.shutdown();
		if (writer!=null) writer.end();
		if (listener!=null) listener.end();
		if (cleaner!=null) cleaner.end();
		if (writeSocket!=null) writeSocket.close();
		if (listenSocket!=null) listenSocket.close();
		CLI.debug("Shutdown");
	}
}
