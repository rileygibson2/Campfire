package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import general.CLI;
import general.Code;
import general.Message;
import general.Utils;

public class ClientListener extends Thread {
	Client c;
	BufferedReader in;

	public ClientListener(Client c) {
		this.c = c;
	}

	@Override
	public void run() {
		if (c.socket==null) return;
		try {
			in = new BufferedReader(new InputStreamReader(c.socket.getInputStream()));
			while (true) {
				String line;
				while ((line = in.readLine()) != null) {
					Message m = new Message(line);
					respond(m);
				}
			}
		}
		catch (IOException e) {System.out.println(e.toString());}
	}
	
	public void respond(Message m) {
		CLI.print(this, m.code+" recieved");
		switch (m.code) {
		case CallAccept:
			break;
		case CallDecline:
			break;
		case CallEnd:
			break;
		case CallRequest:
			CLI.print(this, "Ring from "+m.message);
			c.cGUI.createCallDialog(m.message);
			break;
		case ClientsList:
			String[] split = m.message.split(",");
			c.clientList.clear();
			for (String s : split) {
				if (!s.equals(c.name)) c.clientList.add(s);
			}
			c.cGUI.repaint();
			break;
		case IAm:
			break;
		case WhoIs:
			c.cP.print(Utils.format(Code.IAm, c.name));
			break;
		default:
			break;
		
		}
	}
}
