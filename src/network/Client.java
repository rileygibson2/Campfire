package network;

import java.net.InetAddress;
import java.time.Instant;

public class Client {

	private InetAddress address;
	private int connectPort;
	private int listenPort;
	boolean failedRecently; //Whether failed to connect to this client last time
	private Instant timestamp;
	
	static final int timeout = 10;
	public final static Client nullClient = new Client(null, 0, 0);
	
	public Client(InetAddress address) {
		this.address = address;
		timestamp = Instant.now();
		failedRecently = false;
	}
	
	public Client(InetAddress address, int connectPort, int listenPort) {
		this.address = address;
		this.connectPort = connectPort;
		this.listenPort = listenPort;
		timestamp = Instant.now();
		failedRecently = false;
	}
	
	public void setAddress(InetAddress a) {address = a;}
	
	public InetAddress getAddress() {return address;}
	
	public String getIP() {
		if (address==null) return "";
		else return address.getHostAddress();
	}
	
	public int getConnectPort() {return connectPort;}
	
	public int getListenPort() {return listenPort;}
	
	public void resetTimestamp() {timestamp = Instant.now();}
	
	public boolean failedRecently() {return failedRecently;}
	
	public void setFailedRecently(boolean f) {failedRecently = f;}
	
	public boolean isExpired() {
        return Instant.now().getEpochSecond()-timestamp.getEpochSecond()>timeout;
    }
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Client)) return false;
		Client p = (Client) o;
		
		if (address==null||p.address==null) return false;
		if (address.equals(p.address)&&connectPort==p.connectPort&&listenPort==p.listenPort) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String ip = "none";
		if (address!=null) ip = address.getHostAddress();
		return "<"+ip+": Connect-"+connectPort+" Listen-"+listenPort+">";
	}
}
