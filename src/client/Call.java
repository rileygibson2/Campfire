package client;

import javax.sound.sampled.SourceDataLine;

import client.gui.views.CallView;
import client.gui.views.HomeView;
import general.CLI;
import general.Utils;
import network.Code;
import network.ConnectionHandler;
import network.Message;
import threads.ThreadController;

public class Call {

	private ConnectionHandler cH;
	CallView cV;
	ThreadController audio;
	SourceDataLine speakerLine;

	byte[] data;

	public Call(ConnectionHandler cH) {
		this.cH = cH;
		cH.setOnUpdate(() -> handleData());
		data = new byte[AudioManager.blockLength];
	}

	public ConnectionHandler getConnectionHandler() {return cH;}

	public void start() {
		//Deal with GUI
		cV = new CallView();
		Client.cGUI.changeView(cV);

		//Check connection is listening
		if (!cH.isListening()) cH.start();

		//Get speaker components
		speakerLine = AudioManager.getInstance().getSpeakerWriter();

		//Start mic reader
		audio = AudioManager.getInstance().getMicrophoneReader(data, () -> handleMicData());
		audio.start();
	}

	/**
	 * Handles data coming in from the AudioManager
	 */
	public void handleMicData() {
		cH.write(data);
	}

	/**
	 * Handles data coming in from the ConnectionHandler
	 */
	public void handleData() {
		byte[] data = cH.getData();

		//Check for codes
		Message m = Message.decode(data);
		if (m!=null) {
			CLI.debug("Recieved: "+m.toString());
			switch (m.getCode()) {
			case CallEnd:
				cH.write(new Message(Code.CallEndAck));
				Client.getInstance().endCall(false);
				return;
			case CallError:
				cH.write(new Message(Code.CallErrorAck));
				Client.getInstance().endCall(false);
				return;
			default:
				break;
			}
		}

		//Write to mic
		//CLI.debug("S"+speakerLine.isOpen()+speakerLine.isActive());
		speakerLine.write(data, 0, data.length);
		
		//Pass to view
		int[] decoded = Utils.decodeAmplitude(AudioManager.format, data);
		decoded = Utils.averageAndShrinkAndScale(decoded, 2, cV.dataBounds);
		cV.addData(decoded);
	}

	public void destroy() {
		if (audio!=null) audio.end();
		AudioManager.getInstance().releaseSpeakerWriter();
		Client.cGUI.changeView(HomeView.getInstance());
	}
}
