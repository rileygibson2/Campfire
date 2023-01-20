package general;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CLI {

	public static final String blue = "\033[34m";
	public static final String red = "\033[31m";
	public static final String magenta = "\033[35m";
	public static final String orange = "\033[33m";
	public static final String cyan = "\033[36m";
	public static final String gray = "\033[30m";
	public static final String bold = "\033[1m";
	public static final String reset = "\033[49m\033[39m\033[0m";

	private static Map<String, String> colors;
	private static boolean initialised = false;
	
	private CLI() {};
	
	public static void initialise() {
		colors = new HashMap<String, String>();
		colors.put("Client", blue);
		colors.put("AudioManager", orange);
		colors.put("ThreadController", magenta);
		colors.put("Ring", gray);
		colors.put("Call", cyan);
		initialised = true;
	}
	
	public static void debug(String message) {
		if (!initialised) initialise();
		String time = new SimpleDateFormat("hh:mm:ss").format(new Date()); 
		String name = getCallerClassName();
		String color = colors.get(name);
		if (color==null) color = magenta;
		System.out.println("["+bold+color+name.toUpperCase()+" - "+time+reset+"] "+message);
	}

	public static void error(String message) {
		if (!initialised) initialise();
		String time = new SimpleDateFormat("hh:mm:ss").format(new Date());
		String name = getCallerClassName();
		System.out.println("["+bold+red+"ERROR - "+name.toUpperCase()+" - "+time+reset+"] "+message);
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
