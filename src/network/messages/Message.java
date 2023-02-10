package network.messages;

import java.util.HashMap;
import java.util.Map;

import general.Pair;
import general.Triplet;
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

	public Pair<Integer, Integer> splitPorts() {
		//Check message is capable of containing this data
		if (code==null||!code.equals(Code.BroadcastAck)) return null;

		Pair<Integer, Integer> ports = new Pair<>();
		if (data==null) return null; //Info wasnt sent
		String[] split = data.split(",");
		if (split.length!=2) return null;

		ports.a = Integer.parseInt(split[0].replace("cP=", ""));
		if (ports.a==null) return null;
		ports.b = Integer.parseInt(split[1].replace("lP=", ""));
		if (ports.b==null) return null;
		return ports;
	}

	public Triplet<String, Integer, Integer> splitPortsAndIP() {
		//Check message is capable of containing this data
		if (code==null||!code.equals(Code.PingAck)) return null;

		Triplet<String, Integer, Integer> ports = new Triplet<>();
		if (data==null) return null; //Info wasnt sent
		String[] split = data.split(",");
		if (split.length!=3) return null;

		ports.a = split[0].replace("ip=", "");
		if (ports.a==null) return null;
		ports.b = Integer.parseInt(split[1].replace("cP=", ""));
		if (ports.b==null) return null;
		ports.c = Integer.parseInt(split[2].replace("lP=", ""));
		if (ports.c==null) return null;
		return ports;
	}

	public Map<String, String> getPairs() {
		if (data==null) return null; //Info wasnt sent
		Map<String, String> pairs = new HashMap<>();
		
		String[] split = data.split(",");
		for (String pair : split) {
			String[] pSplit = pair.split("=");
			if (pSplit.length!=2) continue;
			pairs.put(pSplit[0], pSplit[1]);
		}
		
		if (pairs.isEmpty()) return null;
		return pairs;
	}

	@Override
	public String toString() {
		String result = "Message{"+code.toString();
		if (!data.equals(" ")) result += ":"+data+"}";
		else result += "}";
		return result;
	}
}
