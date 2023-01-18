package client;

import java.io.IOException;
import java.io.PrintWriter;

public class ClientPrinter {
	Client c;
	PrintWriter out;
	
	public ClientPrinter(Client c) {
		this.c = c;
		
		if (c.socket==null) return;
		try {
			this.out = new PrintWriter(c.socket.getOutputStream(), true);
		}
		catch (IOException e) {System.out.println(e.toString());}
	}
	
	public void print(String message) {
		out.println(message);
	}
}
