package general;

public interface Functional<G, S> {

	public void submit(S e);
	
	public G get();
}
