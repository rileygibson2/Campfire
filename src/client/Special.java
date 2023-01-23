package client;

import client.gui.views.HomeView;
import client.gui.views.RingView;
import client.gui.views.SpecialView;
import threads.ThreadController;

public class Special {
	
	public enum Type {
		PinaColada,
		Smoko
	}
	private Type type;
	
	RingView rV;
	ThreadController audio;
	ThreadController checker;
	boolean recieving;
	
	byte[] buffer;
	
	public Special(Type type) {
		this.type = type;
		buffer = new byte[AudioManager.blockLength];
	}
	
	public void startSpecial() {
		Client.cGUI.changeView(new SpecialView(type));
		
		//Start reader
		String path = "";
		switch (type) {
		case PinaColada: path = "escape.wav"; break;
		case Smoko: path = "smoko.wav"; break;
		}
		audio = AudioManager.getInstance().getSoundWriter(path, true);
		audio.start();
	}
	
	public void stopSpecial() {
		if (audio!=null) audio.end();
		Client.cGUI.changeView(HomeView.getInstance());
	}
}
