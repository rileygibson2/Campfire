package threads;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import client.gui.IO;

public class ThreadController extends Thread {

	private boolean stop;
	private boolean hasStarted; //Stops same thread attempting to run twice and throwing an error
	private int initialDelay;
	private int wait = 20; //Wait for each iteration
	private boolean iteratePaint; //Whether to paint upon iteration

	private Object target; //Target object of animation
	List<Object> extras; //A list of extra parameters that can be passed in beyond the target if needed
	Set<Object> elements; //Generic list of thread created elements a thread can use
	private int i; //Thread clock
	
	private Runnable finishAction;

	public ThreadController() {
		this.stop = false;
		this.hasStarted = false;
		initialDelay = 0;
		iteratePaint = true;
	}

	@Override
	public void start() {
		if (!hasStarted) {
			hasStarted = true;
			i = 0;
			super.start();
		}
	}
	
	//Internal use only
	
	protected boolean isRunning() {return !stop;} //To determine if it should continue to loop
	 
	//External use
	
	public void end() {stop = true;}
	
	public boolean hasEnded() {return !this.isAlive();}
	
	public boolean isDoomed() {return stop&&!hasEnded();}

	public void setWait(int wait) {this.wait = wait;}
	
	public void setInitialDelay(int d) {initialDelay = d;}

	public boolean hasElements() {return elements!=null;}

	public Set<?> getElements() {return Collections.unmodifiableSet(elements);}

	public Object getTarget() {return target;}
	
	public void setTarget(Object t) {target = t;}
	
	public void setExtras(List<Object> extras) {this.extras = extras;}
	
	public int getIncrement() {return i;}
	
	public void setFinishAction(Runnable r) {finishAction = r;}
	
	public void setPaintOnIterate(boolean p) {iteratePaint = p;}

	public void doInitialDelay() {
		if (initialDelay>0) {
			try {Thread.sleep(initialDelay);}
			catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	
	public void iterate() {
		i++;
		if (iteratePaint) IO.getInstance().requestPaint();
		sleep(wait);
	}

	public void finish() {
		stop = true;
		if (finishAction!=null) finishAction.run();
		if (iteratePaint) IO.getInstance().requestPaint();
	}
	
	public void sleep(int wait) {
		try {Thread.sleep(wait);}
		catch (InterruptedException e) {e.printStackTrace();}
	}
}
