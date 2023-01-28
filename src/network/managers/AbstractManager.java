package network.managers;

import cli.CLI;
import client.gui.GUI;
import client.gui.components.MessageBox;
import general.GetterSubmitter;
import network.connection.ConnectionException;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;

public abstract class AbstractManager {
	
	protected boolean shutdown; //Set when elements should or are shutdown
	protected boolean fatalError; //Set when a fatal error occurs and no further operations should take place
	protected String fatalErrorMessage; //To allow causes for fatal errors to be reproduced in a message every time the fatal error is checked
	protected Runnable fatalErrorAction; //To be run upon a fatal error occuring
	
	protected AbstractManager() {
		shutdown = false;
		fatalError = false;
	}
	
	protected abstract void start();
	
	/**
	 * Because managers may have multiple threaded components all which
	 * access the network in some way, each restart method must wait untill
	 * all threads are actually shutdown before proceeding to restart the 
	 * manager so resources are released and the new doesn't trigger components
	 * that will try and bind to resources held by the old components.
	 */
	public void restart() {
		GetterSubmitter<Boolean, Boolean> gS = new GetterSubmitter<>() {
			public Boolean get() {return hasShutdown();}
			public void submit(Boolean e) {
				start();
			}
		};
		AnimationFactory.getAnimation(gS, Animations.CheckCondition).start();
		shutdown();
	}
	
	public void setFatalErrorAction(Runnable r) {fatalErrorAction = r;}
	
	public void fatalError(String message, boolean doGUI) {
		fatalError = true;
		fatalErrorMessage = message;
		CLI.error("Fatal error: "+message);
		if (doGUI) GUI.getInstance().addMessage("Fatal Error - Please restart application", MessageBox.error);
		shutdown();
	}
	
	public void checkForFatalError(boolean verbose) throws ConnectionException {
		if (fatalError) {
			if (verbose) GUI.getInstance().addMessage("Fatal Error - Please restart application", MessageBox.error);
			throw new ConnectionException("A fatal error was previously detected, cannot continue ("+fatalErrorMessage+")", verbose).setFatal();
		}
	}
	
	public abstract boolean hasShutdown();
	
	public void shutdown() {
		shutdown = true;
	};
}
