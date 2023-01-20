package server.device;

import java.io.IOException;
import java.io.PrintWriter;

public class DevicePrinter {
	Device d;
	PrintWriter out;
	
	public DevicePrinter(Device d) {
		this.d = d;
		try {
			this.out = new PrintWriter(d.socket.getOutputStream(), true);
		}
		catch (IOException e) {System.out.println(e.toString());}
	}
	
	public void print(String message) {
		out.println(message);
	}
}
