package general;

public interface Functional<T, E> {

	public void submit(E e);
	
	public T get();
}
