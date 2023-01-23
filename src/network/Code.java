package network;

public enum Code {
	//Instantiation
	WhoIs("i1"),
	IAm("i2"),
	ClientsList("i3"),

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
	
	RequestedClientBusy("cRB"),
	RequestedClientBusyAck("cRB-ACK"),
	
	CallError("cER"),
	CallErrorAck("cER-ACK");

	public final String c;
	private Code(String c) {
		this.c = c;
	}
}
