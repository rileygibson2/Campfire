package threads;

import java.util.Collections;
import java.util.Set;

import client.Client;

public class ThreadController extends Thread {

	private Object target;

	private boolean stop;
	private boolean hasRun; //Stops same thread attempting to run twice and throwing an error
	private int wait = 20;

	Set<Object> elements; //Generic list of elements an animation can use
	private int i;

	public ThreadController() {
		this.stop = false;
		this.hasRun = false;
	}

	@Override
	public void start() {
		if (!hasRun()) {
			hasRun = true;
			i = 0;
			super.start();
		}
	}
	
	public void end() {this.stop = true;}

	public boolean isRunning() {return !this.stop;}

	public boolean hasRun() {return this.stop&&this.hasRun;}

	public void setSpeed(int s) {this.wait = s;}

	public boolean hasElements() {return elements!=null;}

	public Set<?> getElements() {return Collections.unmodifiableSet(elements);}

	public Object getTarget() {return this.target;}
	
	public void setTarget(Object t) {this.target = t;}
	
	public int getIncrement() {return this.i;}

	public void iterate() {
		i++;
		Client.cGUI.repaint();
		try {Thread.sleep(wait);}
		catch (InterruptedException e) {e.printStackTrace();}
	}

	public void finish() {

	}
}
