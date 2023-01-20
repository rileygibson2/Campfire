package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.gui.ClientGUI;
import general.CLI;

public class Client {

	public static ClientGUI cGUI;
	Socket socket;
	ClientListener cL;
	ClientPrinter cP;
	PrintWriter out;

	Ring ring;
	Call call;

	public static final String ipRegex = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$";


	public Client() throws Exception {
		setup();
		CLI.debug("Starting...");

		Thread.sleep(2000);
	}

	public void call() {
		if (call==null) {
			CLI.debug("Initiating call");
			call = new Call(this);
			call.startCall();
		}
		else CLI.debug("Already in call");
		//cP.print(Utils.format(Code.CallRequest, name));
	}

	public void endCall() {
		if (call!=null) {
			call.stopCall();
			call = null;
		}
	}

	public void ring() {
		if (ring==null) {
			CLI.debug("Initiating ring");
			ring = new Ring(this);
			ring.startRing();
		}
		else CLI.debug("Already in ring");
	}

	public void endRing() {
		if (ring!=null) {
			ring.stopRing();
			ring = null;
		}
	}

	public void shutdown() {
		CLI.debug("Shutting down...");
		try {
			if (socket!=null) socket.close();
		}
		catch (IOException e) {CLI.error(e.toString());}
		AudioManager.getInstance().release();
		CLI.debug("Shutdown done.");
	}

	public void setup() {
		//Set shutdown hook
		Thread shutdownHook = new Thread(() -> shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		cGUI = ClientGUI.initialise(this);
		call = null;
		ring = null;

		/*try {socket = new Socket("localhost", 5000);}
		catch (IOException e) {System.out.println("Connection refused");}
		cL = new ClientListener(this);
		cP = new ClientPrinter(this);
		cL.start();*/
	}

	public static ExecutorService getExecutor() {
		return Executors.newSingleThreadExecutor();

	}

	public static void main(String[] args) throws Exception {
		Client c = new Client();
	}
}

