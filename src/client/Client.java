package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.gui.ClientGUI;
import general.CLI;
import general.Code;
import general.Utils;

public class Client {

	public static ClientGUI cGUI;
	public static AudioManager aM = new AudioManager();
	public String name;
	public List<String> clientList;

	Socket socket;
	ClientListener cL;
	ClientPrinter cP;
	PrintWriter out;
	
	Call call;
	
	public static final String ipRegex = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$";


	public Client(String name) throws Exception {
		this.name = name;
		setup();
		System.out.println("Starting...");

		Thread.sleep(2000);
	}

	public void call() {
		CLI.print(this, "Initiating call");
		
		call = new Call(this);
		call.startCall();
		//cP.print(Utils.format(Code.CallRequest, name));
	}

	public void shutdown() {
		CLI.print(this, "Shutting down...");
		try {
			if (socket!=null) socket.close();
		}
		catch (IOException e) {System.out.println(e.toString());}
		aM.release();
		CLI.print(this, "Done.");
	}

	public void setup() {
		//Set shutdown hook
		Thread shutdownHook = new Thread(() -> shutdown());
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		clientList = new ArrayList<String>();
		cGUI = ClientGUI.initialise(this);
		aM = new AudioManager();
		call = null;

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
		String name = "";
		if (args.length>0) name = args[0];
		Client c = new Client(name);


		/*// Get the input and output streams for the socket
    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

    // Set up the audio input
    AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
    TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
    targetLine.open(format);
    targetLine.start();

    // Read audio data from the microphone and send it to the server
    int numBytesRead;
    byte[] targetData = new byte[targetLine.getBufferSize() / 5];
    while (true) {
      // Read audio data from the microphone
      numBytesRead = targetLine.read(targetData, 0, targetData.length);
      // Send the audio data to the server
      out.println(targetData);
    }

    // Close the streams and socket
    out.close();
    in.close();*/
	}
}

