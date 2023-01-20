package client;

import client.gui.views.CallView;
import client.gui.views.HomeView;
import general.Utils;
import threads.ThreadController;

public class Call {
	
	Client c;
	CallView cV;
	ThreadController audio;
	ThreadController checker;
	
	byte[] buffer;
	
	public Call(Client c) {
		this.c = c;
		buffer = new byte[AudioManager.blockLength];
	}
	
	public void startCall() {
		//Deal with GUI
		cV = new CallView();
		Client.cGUI.changeView(cV);
		
		//Start reader
		audio = AudioManager.getInstance().getMicrophoneReader(buffer);
		audio.start();
		
		checker = new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) {
					int[] data = Utils.decodeAmplitude(AudioManager.format, buffer);
					data = Utils.averageAndShrinkAndScale(data, 2, cV.dataBounds);
					cV.addData(data);
					
					//System.out.println(data.length+": "+Arrays.toString(data)+"\n\n\n\n\n\n");
					try {Thread.sleep(10);}
					catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		};
		checker.start();
	}
	
	public void stopCall() {
		if (checker!=null) checker.end();
		if (audio!=null) audio.end();
		Client.cGUI.changeView(HomeView.getInstance());
	}
}
