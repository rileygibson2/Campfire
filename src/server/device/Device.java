package server.device;

import java.net.Socket;

import general.Code;
import general.Utils;
import server.Server;

public class Device {
	Server s;

	public String name;
	public Socket socket;
	DeviceListener dL;
	public DevicePrinter dP;

	public Device(Server s, Socket socket) throws Exception {
		this.s = s;
		this.socket = socket;
		dL = new DeviceListener(this);
		dP = new DevicePrinter(this);
		dL.start();
		
		//Get name
		dP.print(Utils.format(Code.WhoIs));
		
	}
}
