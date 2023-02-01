package client;

import javax.sound.sampled.SourceDataLine;

import cli.CLI;
import client.gui.GUI;
import client.gui.components.MessageBox;
import client.gui.views.CallView;
import client.gui.views.HomeView;
import general.Utils;
import network.connection.Connection;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class Call {

	private Connection c;
	CallView cV;
	ThreadController audio;
	SourceDataLine speakerLine;
	boolean ended; //Used to avoid errors from threads still executing after call has ended

	byte[] data;

	protected Call(Connection c) {
		this.c = c;
		c.setOnUpdate(() -> handleData());
		data = new byte[AudioManager.blockLength];
		ended = false;
	}

	/**
	 * Can only be used to obtain a connection handler once,
	 * as will set this objects version to null to prevent and duplicate use.
	 * Whoever uses this method now has the  responsibility to close the object they obtain.
	 * 
	 * @return
	 */
	protected Connection stealConnectionHandler() {
		Connection c = this.c;
		this.c = null;
		return c;
	}

	public void start() {
		//Deal with GUI
		cV = new CallView();
		Campfire.cGUI.changeView(cV);

		//Check connection is listening
		if (!c.isListening()) c.start();

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
		if (c==null) {
			if (!ended&&!Campfire.isShuttingdown()) CLI.error("Connection unexpectedly became null");
			return;
		}
		c.write(data);
	}

	/**
	 * Handles data coming in from the ConnectionHandler
	 */
	public void handleData() {
		if (c==null) {
			if (!ended&&!Campfire.isShuttingdown()) CLI.error("Connection unexpectedly became null");
			return;
		}
		byte[] data = c.getData();

		//Check for codes
		Message m = Message.decode(data);
		if (m!=null) {
			c.debug(m, true);
			
			switch (m.getCode()) {
			case CallEnd:
				c.write(new Message(Code.CallEndAck));
				Campfire.getInstance().endCall(false);
				return;
				
			case LocalError:
				c.write(new Message(Code.LocalErrorAck));
				c.close();
				Campfire.getInstance().destroyAll(); //Reset client
				GUI.getInstance().addMessage("There was an error with the call", MessageBox.error);
				return;
				
			case Ping:
				c.write(new Message(Code.PingAck));
				break;
				
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
		ended = true;
		if (audio!=null) audio.end();
		if (c!=null) c.close();
		AudioManager.getInstance().releaseSpeakerWriter();
		Campfire.cGUI.changeView(HomeView.getInstance());
	}
}
