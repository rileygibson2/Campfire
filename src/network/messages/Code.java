package network.messages;

public enum Code {
	//Instantiation
	WhoIs("i1"),
	IAm("i2"),
	ClientsList("i3"),
	
	Ping("pp"),
	PingAck("pp-ACK"),
	
	Broadcast("bb"),
	BroadcastAck("bb-ACK"),

	//Calls
	CallRequest("cR"),
	CallRequestAck("cR-ACK"),
	
	CallAccept("cA"),
	CallAcceptAck("cA-ACK"),
	
	CallRequestCancel("cRC"),
	CallRequestCancelAck("cRC-ACK"),
	
	CallDecline("cD"),
	CallDeclineAck("cD-ACK"),
	
	CallEnd("cE"),
	CallEndAck("cE-ACK"),
	
	SpecialRequest("sR"),
	SpecialRequestAck("sR-ACK"),
	
	SpecialEnd("sE"),
	SpecialEndAck("sE-ACK"),
	
	InvalidSpecialType("sTI"),
	InvalidSpecialTypeAck("sTI-ACK"),
	
	RequestedClientBusy("cRB"),
	RequestedClientBusyAck("cRB-ACK"),
	
	LocalError("cER"),
	LocalErrorAck("cER-ACK");

	public final String c;
	private Code(String c) {
		this.c = c;
	}
}