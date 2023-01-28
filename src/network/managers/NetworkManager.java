package network.managers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import general.GetterSubmitter;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;

public class NetworkManager extends AbstractManager {

	private static NetworkManager singleton;

	private static ConnectionManager connectionManager;
	private static LinkManager linkManager;
	private static BroadcastManager broadcastManager;

	private static Set<InetAddress> localAddresses; //All addresses associated with this computer

	private NetworkManager() {
		super();
		
		linkManager = new LinkManager();
		broadcastManager = new BroadcastManager();
		connectionManager = new ConnectionManager();
		connectionManager.setFatalErrorAction(() -> {
			
		});
		
		buildLocalAddresses();
	}

	public static NetworkManager getInstance() {
		if (singleton==null) singleton = new NetworkManager();
		return singleton;
	}

	public static ConnectionManager getConnectionManager() {return connectionManager;}

	public static LinkManager getLinkManager() {return linkManager;}

	public static BroadcastManager getBroadcastManager() {return broadcastManager;}

	public void start() {
		linkManager.start();
		broadcastManager.start();
		connectionManager.start();
	}

	public static void buildLocalAddresses() {
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

	public static Set<InetAddress> getLocalAddresses() {return localAddresses;}

	public void printLocalAddresses() {
		if (localAddresses==null) {
			CLI.debug("Local addresses are null");
			return;
		}
		CLI.debug("Local Addresses:");
		for (InetAddress a : localAddresses) CLI.debug(a.toString());
	}

	public static boolean isLocalAddress(InetAddress address) {
		if (address==null) return false;
		if (localAddresses==null) return false;
		for (InetAddress local: localAddresses) {
			if (local.equals(address)) return true;
		}
		return false;
	}

	@Override
	public boolean hasShutdown() {
		if (connectionManager.hasShutdown()
				&&linkManager.hasShutdown()
				&&broadcastManager.hasShutdown()) return true;
		return false;
	}
	
	@Override
	public void shutdown() {
		if (shutdown) return;
		if (connectionManager!=null) connectionManager.shutdown();
		if (linkManager!=null) linkManager.shutdown();
		if (broadcastManager!=null) broadcastManager.shutdown();
		CLI.debug("Shutdown");
		super.shutdown();
	}
}
