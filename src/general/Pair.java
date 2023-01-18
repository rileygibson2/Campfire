package general;

public class Pair<A, B> {
	public A a;
	public B b;
	
	public Pair() {}
	
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String toString() {
		return "["+a.toString()+", "+b.toString()+"]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		Pair<A, B> p = (Pair<A, B>) o;
		if (a.equals(p.a)&&b.equals(p.b)) return true;
		return false;
	}
	
	public Pair<A, B> clone() {
		return new Pair<A, B>(a, b);
	}
}

