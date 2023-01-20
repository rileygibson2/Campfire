package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import general.CLI;
import general.Code;
import general.Utils;
import server.device.Device;

public class Server {

	ServerSocket serverSocket;
	Set<Device> devices;
	Set<Call> calls;

	public Server() throws Exception {
		setup();
		run();
	}

	public void run() throws Exception {
		System.out.println("Running...");

		while (true) {
			// Wait for an incoming connection
			Socket clientSocket = serverSocket.accept();
			devices.add(new Device(this, clientSocket));
		}
	}
	
	public boolean canCall(Device a, Device b) {
		for (Call c : calls) {
			if (c.contains(a)||c.contains(b)) return false;
		}
		return true;
	}
	
	public void ringClient(Device a, Device b) {
		b.dP.print(Utils.format(Code.CallRequest, b.name));
	}
	
	public void updateClients() {
		String s = "";
		for (Device d : devices) s += d.name+",";
		for (Device d: devices) d.dP.print(Utils.format(Code.ClientsList, s));
	}
	
	public Device getDevice(String name) {
		for (Device d : devices) {
			if (d.name.equals(name)) return d;
		}
		return null;
	}

	public void setup() throws IOException {
		//Set shutdown hook
		Thread shutdownHook = new Thread(() -> shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		devices = new HashSet<Device>();
		calls = new HashSet<Call>();
		serverSocket = new ServerSocket(5000);
	}

	public void shutdown() {
		//CLI.print(this, "Shutting down...");
		try {
			if (serverSocket.isBound()) serverSocket.close();
			for (Device d : devices) {
				if (d.socket.isConnected()) d.socket.close();
			}
		}
		catch (IOException e) {System.out.println(e.toString());}
		//CLI.print(this, "Done.");
	}

	public static void main(String[] args) throws Exception {
		Server s = new Server();
	}
}

