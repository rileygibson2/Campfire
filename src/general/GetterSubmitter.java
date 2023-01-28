package general;

public interface GetterSubmitter<G, S> {

	public void submit(S e);
	
	public G get();
}
