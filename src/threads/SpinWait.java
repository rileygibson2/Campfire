package threads;

import java.util.concurrent.Callable;

import cli.CLI;

public class SpinWait {

	private Callable<Boolean> condition; //If this returns true then spinwait will release.
	
	public SpinWait(Callable<Boolean> condition) {
		this.condition = condition;
	}
	
	public void run() {
		CLI.debug("Entering spinwait");
		while (true) {
			try {
				//CLI.debug(condition.call().toString());
				if (condition.call()) break;
			}
			catch (Exception e) {
				CLI.error("Spin wait encountered error while checking condition, breaking wait to be safe");
				break;
			}
			
			//Apparently compiler won't honour the while loop unless there is another call here
			try {Thread.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
		}
		CLI.debug("Exiting spinwait");
	}
}
