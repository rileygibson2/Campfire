package network;


import cli.CLI;
import client.Client;
import client.Special.Type;
import client.gui.GUI;
import client.gui.components.MessageBox;
import general.Functional;
import general.Pair;

public class ConnectionRouter {
	
	Connection c;
	Functional<Object, Pair<Connection, Message>> pingAckAction;
	
	protected ConnectionRouter(Connection c) {
		this.c = c;
		c.setOnUpdate(() -> handleData());
	}
	
	protected void setPingAckAction(Functional<Object, Pair<Connection, Message>> p) {pingAckAction = p;}
	
	protected void start() {c.start();}
	
	/**
	 * Will check initial code and route the connection to the
	 * correct place. Sets the connection's onUpdate method back to
	 * null so it looks like a fresh connection.
	 */
	public void handleData() {
		Message m = Message.decode(c.getData());
		if (m==null) {
			CLI.error("Recieved bad message");
			return;
		}
		c.debug(m, true);
		
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
			//Pass info about this clients port settings into acknowledgement
			c.write(new Message(Code.PingAck, "ip="+Client.getIP().getHostAddress()+",cP="+Client.getConnectPort()+",lP="+Client.getListenPort()));
			c.close();
			break;
			
		case PingAck:
			if (pingAckAction!=null) pingAckAction.submit(new Pair<Connection, Message>(c, m));
			break;
			
		case LocalError: //There was a problem at the other end
			c.write(new Message(Code.LocalErrorAck));
			c.close();
			break;
			
		default:
			break;
		
		}
	}
}
