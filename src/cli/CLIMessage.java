package cli;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CLIMessage {
	
	String name;
	String message;
	String time;
	
	Color color;
	String colorStr;
	
	boolean isError;
	
	public CLIMessage(String name, String message) {
		this.name = name;
		this.message = message;
		isError = false;
		
		time = new SimpleDateFormat("hh:mm:ss").format(new Date());
		color = CLI.getNameColor(name);
		colorStr = CLI.getNameColorString(name);
	}
	
	public void setError() {
		isError = true;
		color = new Color(255, 0, 0);
	}
	
	public String formatForConsole() {
		if (isError) return "["+CLI.bold+CLI.red+"ERROR - "+name.toUpperCase()+" - "+time+CLI.reset+"] "+message;
		return "["+CLI.bold+colorStr+name.toUpperCase()+" - "+time+CLI.reset+"] "+message;
	}
	
	public String formatHeader() {
		if (isError) return "[ERROR - "+name.toUpperCase()+" - "+time+"]";
		return "["+name.toUpperCase()+" - "+time+"]";
	}

}
