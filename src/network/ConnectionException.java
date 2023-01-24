package network;

import cli.CLI;
import client.Client;

public class ConnectionException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private boolean isFatal;
	
	public ConnectionException(String message) {
		super(message);
		if (!Client.isShuttingdown()) CLI.error("ConnectionException: "+message);
	}
	
	public ConnectionException(String message, boolean verbose) {
		super(message);
		if (verbose&&!Client.isShuttingdown()) CLI.error("ConnectionException: "+message);
	}
	
	/**
	 * Returns this to allow for chaining
	 * @return
	 */
	public ConnectionException setFatal() {
		isFatal = true;
		return this;
	}
	
	public boolean isFatal() {return isFatal;}
}
