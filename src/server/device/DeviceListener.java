package server.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import general.CLI;
import general.Code;
import general.Utils;
import server.Message;

public class DeviceListener extends Thread {
	Device d;
	BufferedReader in;

	public DeviceListener(Device d) {
		this.d= d;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(d.socket.getInputStream()));
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
		//CLI.print(this, m.code+" recieved");
		switch (m.code) {
		case CallAccept:
			break;
		case CallDecline:
			break;
		case CallEnd:
			break;
		case CallRequest:
			Device b = d.s.getDevice(m.message);
			if (b==null) d.dP.print(Utils.format(Code.CallError));
			
			if (d.s.canCall(d, b)) {
				d.dP.print(Utils.format(Code.RingingOtherClient));
				d.s.ringClient(d, b);
			}
			else {
				d.dP.print(Utils.format(Code.CallDecline));
			}
			break;
		case ClientsList:
			break;
		case IAm:
			d.name = m.message;
			//CLI.print(this, "New device with name "+d.name);
			d.s.updateClients();
			break;
		case WhoIs:
			d.dP.print(Utils.format(Code.IAm, d.name));
			break;
		default:
			break;
		
		}
	}
}
