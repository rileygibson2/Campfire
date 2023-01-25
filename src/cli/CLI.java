package cli;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import general.Pair;

public class CLI {

	private static CLIViewer cliV;

	public static final String blue = "\033[34m";
	public static final String red = "\033[31m";
	public static final String magenta = "\033[35m";
	public static final String orange = "\033[33m";
	public static final String cyan = "\033[36m";
	public static final String gray = "\033[30m";
	public static final String bold = "\033[1m";
	public static final String reset = "\033[49m\033[39m\033[0m";

	private static Map<String, Pair<String, Color>> colors;
	private static boolean initialised = false;
	private static boolean verbose = false; //Classes can choose to only log things if this is set

	private CLI() {};

	public static void setVerbose(boolean v) {
		if (v!=verbose) {
			if (v) CLI.cliDebug("Entered verbose mode");
			else CLI.cliDebug("Exited verbose mode");
		}
		verbose = v;
	}

	public static boolean isVerbose() {return verbose;}

	public static boolean viewerActive() {
		if (!initialised) initialise();
		return cliV.isActive();
	}

	public static void showViewer(boolean b) {
		if (!initialised) initialise();
		cliV.setActive(b);
	}

	public static CLIViewer getViewer() {
		if (!initialised) initialise();
		return cliV;
	}

	public static void initialise() {
		colors = new HashMap<String, Pair<String, Color>>();
		colors.put("Client", new Pair<String, Color>(blue, new Color(0, 100, 255)));
		colors.put("AudioManager", new Pair<String, Color>(orange, new Color(200, 255, 0)));
		colors.put("ThreadController", new Pair<String, Color>(magenta, new Color(255, 0, 255)));
		colors.put("Ring", new Pair<String, Color>(gray, new Color(80, 80, 80)));
		colors.put("Call", new Pair<String, Color>(gray, new Color(0, 200, 255)));
		colors.put("Special", new Pair<String, Color>(gray, new Color(0, 200, 255)));
		colors.put("NetworkManager", new Pair<String, Color>(cyan, new Color(0, 200, 255)));
		colors.put("Connection", new Pair<String, Color>(cyan, new Color(0, 200, 255)));
		colors.put("DOM", new Pair<String, Color>(cyan, new Color(0, 200, 255)));
		colors.put("Console", new Pair<String, Color>(orange, new Color(200, 255, 0)));
		cliV = CLIViewer.initialise();

		initialised = true;
	}

	public static void debug(String message) {
		if (!initialised) initialise();
		CLIMessage m = new CLIMessage(getCallerClassName(), message);

		System.out.println(m.formatForConsole());
		cliV.print(m);
	}

	public static void error(String message) {
		if (!initialised) initialise();
		CLIMessage m = new CLIMessage(getCallerClassName(), message);
		m.setError();

		System.out.println(m.formatForConsole());
		cliV.print(m);
	}
	
	/**
	 * For when the CLI class itself wants to print a message
	 * 
	 * @param message
	 */
	private static void cliDebug(String message) {
		if (!initialised) initialise();
		CLIMessage m = new CLIMessage("Console", message);

		System.out.println(m.formatForConsole());
		cliV.print(m);
	}

	public static String getNameColorString(String name) {
		for (Map.Entry<String, Pair<String, Color>> m : colors.entrySet()) {
			if (name.toLowerCase().contains(m.getKey().toLowerCase())) return m.getValue().a;
		}
		return magenta;
	}

	public static Color getNameColor(String name) {
		for (Map.Entry<String, Pair<String, Color>> m : colors.entrySet()) {
			if (name.toLowerCase().contains(m.getKey().toLowerCase())) return m.getValue().b;
		}
		return new Color(255, 0, 255);
	}

	public static String getCallerClassName() { 
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();

		for (int i=1; i<stElements.length; i++) { //Last (first) call will be to get stack trace so start at 1
			StackTraceElement ste = stElements[i];
			if (!ste.getClassName().equals(CLI.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
				String c = ste.getClassName();

				//Strip package name
				return c.substring(c.indexOf(".") + 1);
			}
		}
		return null;
	}
}
