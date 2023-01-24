package client;

import cli.CLI;
import client.gui.GUI;
import client.gui.components.MessageBox;
import client.gui.views.HomeView;
import client.gui.views.RingView;
import network.Code;
import network.Connection;
import network.Message;
import threads.ThreadController;

public class Ring {

	private Connection c;
	private ThreadController audio;
	private ThreadController writer;
	boolean recieving;
	boolean ended; //Used to avoid errors from threads still executing after call has ended

	public Ring(Connection c, boolean recieving) {
		this.recieving = recieving;
		this.c = c;
		ended = false;
		c.setOnUpdate(() -> handleData());
	}

	/**
	 * Can only be used to obtain a connection handler once,
	 * as will set this objects version to null to prevent and duplicate use.
	 * Whoever uses this method now has the  responsibility to close the object they obtain.
	 * 
	 * @return
	 */
	public Connection stealConnectionHandler() {
		Connection c = this.c;
		this.c = null;
		return c;
	}

	public void start() {
		String path = "";
		//Check connection is listening
		if (!c.isListening()) c.start();

		if (recieving) { //Recieving a call
			path = "ringIncoming.wav";
		}
		else { //Initiating a call
			path = "ringOutgoing.wav";
			Message m = new Message(Code.CallRequest);

			//Repeatedly send call request
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

		//Deal with view and audio
		Client.cGUI.changeView(new RingView(recieving));
		audio = AudioManager.getInstance().getSoundWriter(path, true);
		audio.start();
	}

	/**
	 * Triggered by client
	 */
	public void accept() {
		if (c==null) {
			if (!ended&&!Client.isShuttingdown()) CLI.error("Connection unexpectedly became null");
			return;
		}
		c.write(new Message(Code.CallAccept));
	}

	/**
	 * Triggered by client
	 * Only an initiating clients send cancel code
	 */
	public void cancel() {
		if (recieving) {
			GUI.getInstance().addMessage("They cancelled the ring", MessageBox.info);
			return;
		}
		GUI.getInstance().addMessage("You cancelled the ring", MessageBox.info);
		c.write(new Message(Code.CallRequestCancel));
	}

	/**
	 * Triggered by client
	 * Only an recieving clients send decline code
	 */
	public void decline() {
		if (!recieving) {
			GUI.getInstance().addMessage("They declined your ring", MessageBox.info);
			return; 
		}
		GUI.getInstance().addMessage("You declined the ring", MessageBox.info);
		c.write(new Message(Code.CallDecline));
	}

	public void handleData() {
		if (c==null) {
			if (!ended&&!Client.isShuttingdown()) CLI.error("Connection unexpectedly became null");
			return;
		}

		Message m = Message.decode(c.getData());
		if (m==null) CLI.error("Recieved bad message");
		CLI.debug("Recieved: "+m.toString());

		switch (m.getCode()) {
		case CallRequest:
			//Already displaying call visuals so just ack
			c.write(new Message(Code.CallRequestAck));
			break;

		case CallRequestAck:
			//Client has acked so don't need to send request anymore
			if (writer!=null) writer.end();
			break;

		case CallAccept:
			//Ack the accept and move into call state
			if (writer!=null) writer.end();
			c.write(new Message(Code.CallAcceptAck));
			Client.getInstance().startCall();
			break;

		case CallAcceptAck:
			//Recieved ack so move into call state
			Client.getInstance().startCall();
			break;

		case CallRequestCancel:
			//Stop this ring, show cancel message saying initiator cancelled and stop ring
			c.write(new Message(Code.CallRequestCancelAck));
			Client.getInstance().cancelRing(); //Reset client's ring object
			break;

		case CallDecline:
			//Stop this ring, show decline message saying reciever declined and stop ring
			c.write(new Message(Code.CallDeclineAck));
			Client.getInstance().declineRing();
			break;

		case RequestedClientBusy:
			//Stop this ring, show busy message saying reciever is busy and stop ring
			c.write(new Message(Code.RequestedClientBusyAck));
			Client.getInstance().destroyAll(); //Reset client's ring object
			GUI.getInstance().addMessage("The client is busy", MessageBox.info);
			break;

		case CallError:
			c.write(new Message(Code.CallErrorAck));
			Client.getInstance().destroyAll(); //Reset client
			GUI.getInstance().addMessage("There was an error with the ring", MessageBox.error);
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
		Client.cGUI.changeView(HomeView.getInstance());
	}
}
