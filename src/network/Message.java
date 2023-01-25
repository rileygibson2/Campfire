package network;

import general.Utils;

public class Message {

	private Code code;
	private String data;
	
	public Message(Code c, String m) {
		code = c;
		data = m;
	}
	
	public Message(Code c) {
		this.code = c;
		data = " ";
	}
	
	public static Message decode(byte[] bytes) {
		String[] split = String.valueOf(new String(bytes).toCharArray()).split(":");
		if (split.length!=2) return null;
		
		Code code = Utils.getCode(split[0]);
		if (code==null) return null;
		String data = split[1];
		
		return new Message(code, data);
	}
	
	public Code getCode() {return code;}
	
	public String getData() {return data;}

	public String format() {
		return code.c+":"+data;
	}
	
	public byte[] formatBytes() {
		return new String(code.c+":"+data).getBytes();
	}
	
	@Override
	public String toString() {
		String result = "Message{"+code.toString();
		if (!data.equals(" ")) result += ":"+data+"}";
		else result += "}";
		return result;
	}
}
