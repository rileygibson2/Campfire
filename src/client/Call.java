package client;

import java.util.Arrays;

import client.gui.views.CallView;
import general.Point;
import general.Utils;

public class Call {
	
	Client c;
	CallView callView;
	Thread reader;
	byte[] buffer;
	
	public Call(Client c) {
		this.c = c;
		buffer = new byte[AudioManager.blockLength];
	}
	
	public void startCall() {
		//Deal with GUI
		callView = new CallView();
		Client.cGUI.changeView(callView);
		
		//Start reader
		reader = Client.aM.getInputStreamThread(buffer);
		reader.start();
		
		Thread checker = new Thread() {
			@Override
			public void run() {
				while (true) {
					int[] data = Utils.decodeAmplitude(AudioManager.format, buffer);
					data = Utils.averageAndShrinkAndScale(data, 5, callView.dataBounds);
					callView.addData(data);
					
					//System.out.println(data.length+": "+Arrays.toString(data)+"\n\n\n\n\n\n");
					try {Thread.sleep(30);}
					catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		};
		checker.start();
	}
	
	public void stopCall() {
		
	}
}
