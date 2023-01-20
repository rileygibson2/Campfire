package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import server.device.Device;

public class Call {
	Device a, b;
	Thread t1, t2;
	boolean kill;

	public Call(Device a, Device b) {
		this.a = a;
		this.b = b;
		this.kill = false;
	}
	
	public boolean contains(Device d) {
		if (a==d||b==d) return false;
		return true;
	}
	
	public void start() {
		t2 = new Thread("Call "+a.name+" -> "+b.name) {
			@Override
			public void run() {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(a.socket.getInputStream()));
					PrintWriter out = new PrintWriter(b.socket.getOutputStream(), true);
			
					String line;
					while (!kill && (line = in.readLine()) != null) {
						out.println(line);
					}
					in.close();
					out.close();
				}
				catch (IOException e) {throw new Error(e.toString());}
			}
		};
		
		t2 = new Thread("Call "+a.name+" -> "+b.name) {
			@Override
			public void run() {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(b.socket.getInputStream()));
					PrintWriter out = new PrintWriter(a.socket.getOutputStream(), true);
			
					String line;
					while (!kill && (line = in.readLine()) != null) {
						out.println(line);
					}
					in.close();
					out.close();
				}
				catch (IOException e) {throw new Error(e.toString());}
			}
		};
		
		t1.start();
		t2.start();
	}
}
