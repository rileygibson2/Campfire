package client;

import client.gui.views.HomeView;
import client.gui.views.RingView;
import general.CLI;
import network.Code;
import network.ConnectionHandler;
import network.Message;
import threads.ThreadController;

public class Ring {
	
	private ConnectionHandler cH;
	private ThreadController audio;
	private ThreadController writer;
	boolean recieving;
	
	public Ring(ConnectionHandler cH, boolean recieving) {
		this.recieving = recieving;
		this.cH = cH;
		cH.setOnUpdate(() -> handleData());
	}
	
	public ConnectionHandler getConnectionHandler() {return cH;}
	
	public void start() {
		String path = "";
		cH.start();
		
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
						CLI.debug("Writing call request");
						cH.write(m.formatBytes());
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
		cH.write(new Message(Code.CallAccept));
	}
	
	/**
	 * Triggered by client
	 * Only an initiating client can cancel the ring
	 */
	public void cancel() {
		if (recieving) return;
		cH.write(new Message(Code.CallRequestCancel));
	}
	
	/**
	 * Triggered by client
	 * Only an recieving client can decline the ring
	 */
	public void decline() {
		if (!recieving) return; 
		cH.write(new Message(Code.CallDecline));
	}
	
	public void handleData() {
		Message m = Message.decode(cH.getData());
		if (m==null) CLI.error("Recieved bad message");
		CLI.debug("Recieved: "+m.toString());
		
		switch (m.getCode()) {
		case CallRequest:
			//Already displaying call visuals so just ack
			cH.write(new Message(Code.CallRequestAck));
			break;
			
		case CallRequestAck:
			//Client has acked so don't need to send request anymore
			if (writer!=null) writer.end();
			break;
			
		case CallAccept:
			//Ack the accept and move into call state
			if (writer!=null) writer.end();
			cH.write(new Message(Code.CallAcceptAck));
			Client.getInstance().startCall();
			break;
			
		case CallAcceptAck:
			//Recieved ack so move into call state
			Client.getInstance().startCall();
			break;
			
		case CallRequestCancel:
			//Stop this ring, show cancel message saying initiator cancelled and stop ring
			cH.write(new Message(Code.CallRequestCancelAck));
			Client.getInstance().cancelRing(); //Reset client's ring object
			break;
			
		case CallDecline:
			//Stop this ring, show decline message saying reciever declined and stop ring
			cH.write(new Message(Code.CallDeclineAck));
			Client.getInstance().declineRing(); //Reset client's ring object
			break;
			
		case RequestedClientBusy:
			//Stop this ring, show busy message saying reciever is busy and stop ring
			cH.write(new Message(Code.RequestedClientBusyAck));
			Client.getInstance().cancelRing(); //Reset client's ring object
			break;
		default: break;
		}
		
	}
	
	public void destroy() {
		if (audio!=null) audio.end();
		if (writer!=null) writer.end();
		Client.cGUI.changeView(HomeView.getInstance());
	}
}
