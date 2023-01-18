package general;

public class Utils {

	public static String format(Code c) {
		return c.c+":"+" ";
	}
	
	public static String format(Code c, String message) {
		return c.c+":"+message;
	}
	
	public static Code getCode(String c) {
		for (Code code : Code.values()) {
	        if (code.c.equals(c)) return code;
	    }
		return null;
	}
}
