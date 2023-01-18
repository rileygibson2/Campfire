package general;

public class Message {

	public Code code;
	public String message;
	
	public Message(String m) {
		String[] split = String.valueOf(m.toCharArray()).split(":");
		if (split.length!=2) throw new Error("Recieved malformed message.");
		code = Utils.getCode(split[0]);
		message = split[1];
	}
}
