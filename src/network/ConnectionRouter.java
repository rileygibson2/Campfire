package network;

import cli.CLI;
import client.Client;

public class ConnectionRouter {
	
	Connection c;
	
	public ConnectionRouter(Connection c) {
		this.c = c;
		c.setOnUpdate(() -> handleData());
		c.start();
	}
	
	/**
	 * Will check initial code and route the connection to the
	 * correct place. Sets the connection's onUpdate method back to
	 * null so it looks like a fresh connection.
	 */
	public void handleData() {
		Message m = Message.decode(c.getData());
		if (m==null) CLI.error("Recieved bad message");
		CLI.debug("Recieved: "+m.toString());
		
		switch (m.getCode()) {
		case CallRequest:
			CLI.debug("Routing to recieving ring");
			c.setOnUpdate(null);
			Client.getInstance().startRecievingRing(c);
			break;
		case Ping:
			c.write(new Message(Code.PingAck));
			break;
		default:
			break;
		
		}
	}
}
