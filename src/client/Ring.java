package client;

import client.gui.views.HomeView;
import client.gui.views.RingView;
import threads.ThreadController;

public class Ring {
	
	Client c;
	RingView rV;
	ThreadController audio;
	ThreadController checker;
	
	byte[] buffer;
	
	public Ring(Client c) {
		this.c = c;
		buffer = new byte[AudioManager.blockLength];
	}
	
	public void startRing() {
		//Deal with GUI
		rV = new RingView();
		Client.cGUI.changeView(rV);
		
		//Start reader
		audio = AudioManager.getInstance().getSoundWriter("ring1.wav", true);
		audio.start();
	}
	
	public void stopRing() {
		if (audio!=null) audio.end();
		Client.cGUI.changeView(HomeView.getInstance());
	}
}
