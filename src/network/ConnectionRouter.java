package network;


import cli.CLI;
import client.Client;
import client.Special.Type;

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
		case SpecialRequest:
			CLI.debug("Routing to recieving special");
			int index;
			try {index = Integer.parseInt(m.getData());}
			catch (NumberFormatException e) {
				CLI.error("Invalid special type");
				c.write(new Message(Code.InvalidSpecialType));
				return;
			}
			
			Type type;
			try {type = Type.values()[index];}
			catch (ArrayIndexOutOfBoundsException e) {
				CLI.error("Invalid special type");
				c.write(new Message(Code.InvalidSpecialType));
				return;
			}
			
			c.setOnUpdate(null);
			Client.getInstance().startRecievingSpecial(c, type);
			break;
		case Ping:
			c.write(new Message(Code.PingAck));
			break;
		default:
			break;
		
		}
	}
}
