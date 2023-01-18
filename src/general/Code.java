package general;

public enum Code {
	//Instantiation
	WhoIs("i1"),
	IAm("i2"),
	ClientsList("i3"),

	//Calls
	CallRequest("cR"),
	CallError("cRE"),
	RequestedClientBusy("cRB"),
	RingingOtherClient("cRR"),
	CallAccept("cA"),
	CallDecline("cD"),
	CallEnd("cE");

	public final String c;
	private Code(String c) {
		this.c = c;
	}
}
