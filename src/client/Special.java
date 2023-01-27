package client;

import cli.CLI;
import client.gui.GUI;
import client.gui.components.MessageBox;
import client.gui.views.HomeView;
import client.gui.views.SpecialView;
import network.connection.Connection;
import network.messages.Code;
import network.messages.Message;
import threads.ThreadController;

public class Special {

	public enum Type {
		PinaColada,
		Smoko
	}
	private Type type;

	Connection c;
	ThreadController audio;
	private ThreadController writer;
	boolean recieving;
	boolean ended; //Used to avoid errors from threads still executing after call has ended

	byte[] buffer;

	protected Special(Connection c, Type type, boolean recieving) {
		this.type = type;
		this.recieving = recieving;
		this.c = c;
		ended = false;
		c.setOnUpdate(() -> handleData());
		buffer = new byte[AudioManager.blockLength];
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
		//Check connection is listening
		if (!c.isListening()) c.start();

		if (!recieving) { //Repeatedly send call request
			Message m = new Message(Code.SpecialRequest, ""+type.ordinal());
			writer = new ThreadController() {
				@Override
				public void run() {
					while (isRunning()) {
						c.write(m);
						iterate();
					}
					finish();
				}
			};
			writer.setPaintOnIterate(false);
			writer.setWait(1000);
			writer.start();
		}
		else { //Recieving should start immediatly while sender has to wait for ack
			Intercom.cGUI.changeView(new SpecialView(type, recieving));
			startAudio();
		}
	}

	private void startAudio() {
		if (type==null) CLI.error("Cannot start special audio, type is null");
		String path = "";
		switch (type) {
		case PinaColada: path = "escape.wav"; break;
		case Smoko: path = "smoko.wav"; break;
		}
		audio = AudioManager.getInstance().getSoundWriter(path, false);
		audio.start();
	}

	public void handleData() {
		if (c==null) {
			if (!ended&&!Intercom.isShuttingdown()) CLI.error("Connection unexpectedly became null");
			return;
		}

		Message m = Message.decode(c.getData());
		if (m==null) {
			CLI.error("Recieved bad message");
			return;
		}
		c.debug(m, true);

		switch (m.getCode()) {
		case SpecialRequest:
			//Grab type and start audio
			c.write(new Message(Code.SpecialRequestAck));
			break;

		case SpecialRequestAck:
			//Client has acked so don't need to send request anymore
			if (writer!=null) writer.end();
			if (!recieving) {
				Intercom.cGUI.changeView(new SpecialView(type, recieving));
				startAudio();
			}
			break;

		case SpecialEnd:
			c.write(new Message(Code.SpecialEndAck));
			Intercom.getInstance().endSpecial(false);
			break;
			
		case InvalidSpecialType:
			//Special type was computed as invalid on the other end
			c.write(new Message(Code.InvalidSpecialTypeAck));
			Intercom.getInstance().destroyAll(); //Reset client
			GUI.getInstance().addMessage("The special type was invalid", MessageBox.error);
			break;

		case LocalError:
			c.write(new Message(Code.LocalErrorAck));
			c.close();
			Intercom.getInstance().destroyAll(); //Reset client
			GUI.getInstance().addMessage("There was an error with the special", MessageBox.error);
			break;

		case Ping:
			c.write(new Message(Code.PingAck));
			break;

		default: break;
		}

	}

	public void destroy() {
		ended = true;
		if (audio!=null) audio.end();
		if (writer!=null) writer.end();
		if (c!=null) c.close();
		Intercom.cGUI.changeView(HomeView.getInstance());
	}
}
